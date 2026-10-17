package net.terrunic.shadowdrop.mixin;

//? if <26.1 {
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.longs.Long2BooleanOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.terrunic.shadowdrop.ShadowDrop;
import net.terrunic.shadowdrop.ShadowDropConfig;
import net.terrunic.shadowdrop.render.ShadowBufferSource;
import net.terrunic.shadowdrop.util.PixelReader;
import net.terrunic.shadowdrop.util.ShadowContext;
import net.terrunic.shadowdrop.util.ShadowStyle;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.ByteBuffer;

// Mixin to render drop shadows under items in GUI contexts
@Mixin(value = ItemRenderer.class, priority = 500)
public class ItemRendererMixin {
    // Cache
    @Unique
    private static final int shadowdrop$MAX_CACHED_PIXELS = 4096;
    @Unique
    private static final Long2BooleanOpenHashMap shadowdrop$cachedPixels = new Long2BooleanOpenHashMap();
    @Unique
    private static final ByteBuffer shadowdrop$pixelBuffer = BufferUtils.createByteBuffer(16);
    @Unique
    private static final ByteBuffer shadowdrop$capturePixelBuffer = BufferUtils.createByteBuffer(4);
    @Unique
    private final static Matrix4f shadowdrop$screenPose = new Matrix4f();
    @Unique
    private static final PoseStack shadowdrop$shadowPoseStack = new PoseStack();
    // Trackers
    @Unique
    private static boolean shadowdrop$isRenderingShadow = false;
    @Unique
    private final Matrix3f shadowdrop$screenNormal = new Matrix3f();
    // Shadows
    @Final
    @Shadow
    private Minecraft minecraft;

    // Track item render nesting around the whole method, so it unwinds even when another mod cancels the render
    @WrapMethod(method = "render(Lnet/minecraft/world/item/ItemStack;" + "Lnet/minecraft/world/item/ItemDisplayContext;" + "ZLcom/mojang/blaze3d/vertex/PoseStack;" + "Lnet/minecraft/client/renderer/MultiBufferSource;" + "IILnet/minecraft/client/resources/model/BakedModel;)V")
    private void shadowdrop$trackRenderDepth(ItemStack itemStack, ItemDisplayContext displayContext, boolean leftHand, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay, BakedModel model, Operation<Void> original) {
        ShadowDrop.guiRenderDepth++;
        try {
            original.call(itemStack, displayContext, leftHand, poseStack, bufferSource, combinedLight, combinedOverlay, model);
        } finally {
            ShadowDrop.guiRenderDepth--;
        }
    }

    @Inject(method = "render(Lnet/minecraft/world/item/ItemStack;" + "Lnet/minecraft/world/item/ItemDisplayContext;" + "ZLcom/mojang/blaze3d/vertex/PoseStack;" + "Lnet/minecraft/client/renderer/MultiBufferSource;" + "IILnet/minecraft/client/resources/model/BakedModel;)V", at = @At("HEAD"))
    private void shadowdrop$renderShadow(ItemStack itemStack, ItemDisplayContext displayContext, boolean leftHand, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay, BakedModel model, CallbackInfo ci) {
        if (shadowdrop$isRenderInvalid(itemStack, displayContext)) return;
        // Only the outermost item render gets a shadow
        if (ShadowDrop.guiRenderDepth > 1) return;

        // Refresh pixel cache if called elsewhere
        if (ShadowDrop.shouldRefresh) {
            ShadowDrop.shouldRefresh = false;
            shadowdrop$cachedPixels.clear();
        }

        // Cancel if marked as transparent
        if (ShadowStyle.isTransparent(itemStack)) return;

        // Z scale of the item pose
        Matrix4f itemMatrix = poseStack.last().pose();
        float scaleZ = (float) Math.sqrt(itemMatrix.m02() * itemMatrix.m02() + itemMatrix.m12() * itemMatrix.m12() + itemMatrix.m22() * itemMatrix.m22()) / 16f;

        // Offset item to ensure space behind for shadow
        if (ShadowDropConfig.CLIENT.offsetItems) {
            itemMatrix.translateLocal(0, 0, 32 * scaleZ);
        }

        // Get shadow context & crop state
        ShadowContext shadowContext = ShadowContext.ELSEWHERE;
        shadowdrop$screenPose.set(itemMatrix);
        shadowdrop$screenNormal.set(poseStack.last().normal());
        GuiGraphics guiGraphics = (GuiGraphics) ShadowDrop.currentGuiGraphics;

        if (ShadowDrop.isHotbarRendering) {
            shadowContext = ShadowContext.HOTBAR;
        } else if (ShadowDrop.isEmiDragRendering) {
            shadowContext = ShadowContext.CURSOR;
        } else if (ShadowDrop.isToastRendering || ShadowDrop.isEmiSidebarRendering) {
            shadowContext = ShadowContext.ELSEWHERE;
        } else if (ShadowDrop.hoveredItem == itemStack) {
            if (!ShadowDrop.hoveredItemRendered) {
                ShadowDrop.hoveredItemRendered = true;
                shadowContext = ShadowContext.HOVER;
            }
        } else if (minecraft.player != null && minecraft.player.containerMenu.getCarried().equals(itemStack)) {
            shadowContext = ShadowContext.CURSOR;
        } else if (shadowdrop$isSlotSized(itemMatrix)) {
            // Items still waiting on a probe are treated as slotted
            Boolean inSlot = shadowdrop$isInSlot((int) itemMatrix.m30() - 8, (int) itemMatrix.m31() - 8, (int) itemMatrix.m32());
            if (inSlot == null || inSlot) shadowContext = ShadowContext.SLOT;
        }

        boolean shouldRender = switch (shadowContext) {
            case HOTBAR -> ShadowDropConfig.CLIENT.hotbarShadows;
            case SLOT -> ShadowDropConfig.CLIENT.slotShadows;
            case HOVER -> ShadowDropConfig.CLIENT.hoverShadows;
            case CURSOR -> ShadowDropConfig.CLIENT.cursorShadows;
            case ELSEWHERE -> ShadowDropConfig.CLIENT.elsewhereShadows;
        };
        if (!shouldRender) return;

        boolean isCropped = guiGraphics != null && switch (shadowContext) {
            case HOTBAR -> ShadowDropConfig.CLIENT.hotbarCropped;
            case SLOT -> ShadowDropConfig.CLIENT.slotCropped;
            case HOVER -> ShadowDropConfig.CLIENT.hoverCropped;
            default -> false;
        };

        // Begin shadow rendering
        shadowdrop$isRenderingShadow = true;

        // Render copy of item with wrapped buffer source to draw it as shadow
        int[] shadowColor = ShadowStyle.getColor();
        float r = shadowColor[0] / 255f;
        float g = shadowColor[1] / 255f;
        float b = shadowColor[2] / 255f;
        float a = ShadowStyle.getAlpha(itemStack) / 255f;
        ShadowBufferSource shadowBuffer = new ShadowBufferSource(bufferSource, r, g, b, a);

        BufferSource immediate = null;
        if (bufferSource instanceof BufferSource buf) {
            immediate = buf;
        } else if (guiGraphics != null) {
            immediate = guiGraphics.bufferSource();
        }

        // Crop to fit; not batched
        if (isCropped) {
            immediate.endBatch();
            int slotX = (int) shadowdrop$screenPose.m30() - 8;
            int slotY = (int) shadowdrop$screenPose.m31() - 8;
            guiGraphics.enableScissor(slotX, slotY, slotX + 16, slotY + 16);
        }

        // Shared pose stack is safe to reuse as shadow rendering never nests
        PoseStack shadowPoseStack = shadowdrop$shadowPoseStack;
        Matrix4f shadowMatrix = shadowPoseStack.last().pose();
        shadowMatrix.set(shadowdrop$screenPose);
        shadowPoseStack.last().normal().set(shadowdrop$screenNormal);
        shadowMatrix.translate(ShadowDropConfig.CLIENT.shadowXOffset / 16f, -ShadowDropConfig.CLIENT.shadowYOffset / 16f, 0);
        shadowMatrix.translateLocal(0, 0, -1.5f * scaleZ * 16f);
        shadowMatrix.m02(0f).m12(0f).m22(0f);

        try {
            ((ItemRenderer) (Object) this).render(itemStack, displayContext, leftHand, shadowPoseStack, shadowBuffer, combinedLight, combinedOverlay, model);
            shadowBuffer.endShadowBatches(immediate);
        } finally {
            if (isCropped) {
                guiGraphics.disableScissor();
            }
            shadowdrop$isRenderingShadow = false;
        }
    }

    // If item & display context is immediately invalid render state for shadows (cheap)
    @Unique
    private boolean shadowdrop$isRenderInvalid(ItemStack itemStack, ItemDisplayContext displayContext) {
        return shadowdrop$isRenderingShadow || !ShadowDropConfig.CLIENT.modEnabled || ShadowDrop.isLevelRendering || displayContext != ItemDisplayContext.GUI || itemStack.isEmpty();
    }

    // Whether the item is drawn at the normal 16px GUI size, so slot detection can line up with the screen
    // (screens scaled by animation mods move every frame, and would otherwise read back pixels per item per frame)
    @Unique
    private static boolean shadowdrop$isSlotSized(Matrix4f itemMatrix) {
        float scaleX = (float) Math.sqrt(itemMatrix.m00() * itemMatrix.m00() + itemMatrix.m10() * itemMatrix.m10() + itemMatrix.m20() * itemMatrix.m20());
        float scaleY = (float) Math.sqrt(itemMatrix.m01() * itemMatrix.m01() + itemMatrix.m11() * itemMatrix.m11() + itemMatrix.m21() * itemMatrix.m21());
        return Math.abs(scaleX - 16f) < 0.01f && Math.abs(scaleY - 16f) < 0.01f;
    }

    // Returns whether the bottom right corner of the given window position has valid slot corner color, or null if this frame's reads are spent
    @Unique
    private Boolean shadowdrop$isInSlot(int x, int y, int z) {
        // Slot detection compares the live pixel against the GUI background
        if (minecraft.screen == null || !PixelReader.hasCapture()) return false;

        // Check cache for if current pixel has already been checked
        long key = ((long) (x & 0x1FFFFF) << 42) | ((long) (y & 0x1FFFFF) << 21) | (z & 0x1FFFFF);
        if (shadowdrop$cachedPixels.containsKey(key)) return shadowdrop$cachedPixels.get(key);

        // Get actual screen position to read bottom-right corner pixel
        Window window = minecraft.getWindow();
        int scale = (int) window.getGuiScale();
        int width = window.getWidth();
        int height = window.getHeight();
        int brX = (x + 16) * scale - 1;
        int brY = height - ((y + 16) * scale + 1);

        // A capture from before the framebuffer changed size can't be compared against, so take a new one next frame
        if (!PixelReader.captureMatches(width, height)) {
            PixelReader.invalidateCapture();
            ShadowDrop.shouldRefresh = true;
            return false;
        }

        // Only probe when the 2x2 read sits fully inside the framebuffer
        boolean inBounds = brX >= 0 && brY >= 0 && brX + 2 <= width && brY + 2 <= height;

        // Out of reads this frame
        if (inBounds && !PixelReader.tryConsumeRead()) return null;

        boolean isSlotCorner = false;
        if (inBounds) {
            try {
                // Read screen pixels for slot
                ByteBuffer buffer = shadowdrop$pixelBuffer;
                ByteBuffer initBuffer = shadowdrop$capturePixelBuffer;
                PixelReader.read(brX, brY, 2, 2, buffer, brX + 1, brY, 1, 1, initBuffer);

                // Outer pixel color
                int r1 = buffer.get(4) & 0xFF;
                int g1 = buffer.get(5) & 0xFF;
                int b1 = buffer.get(6) & 0xFF;
                // Inner pixel color
                int r2 = buffer.get(8) & 0xFF;
                int g2 = buffer.get(9) & 0xFF;
                int b2 = buffer.get(10) & 0xFF;
                // GUI initial render outer pixel color
                int r3 = initBuffer.get(0) & 0xFF;
                int g3 = initBuffer.get(1) & 0xFF;
                int b3 = initBuffer.get(2) & 0xFF;

                // Assume slot corner if outer pixel is different to inner pixel, and is over gui background
                boolean onGuiBackground = !(r1 == r3 && g1 == g3 && b1 == b3);
                isSlotCorner = onGuiBackground && r1 != r2 && g1 != g2 && b1 != b2;

            } catch (Exception ignored) {
            }
        }

        // Cache failures too, so a position that can't be probed doesn't read back every frame
        if (shadowdrop$cachedPixels.size() >= shadowdrop$MAX_CACHED_PIXELS) {
            shadowdrop$cachedPixels.clear();
        }
        shadowdrop$cachedPixels.put(key, isSlotCorner);

        return isSlotCorner;
    }
}
//?}
