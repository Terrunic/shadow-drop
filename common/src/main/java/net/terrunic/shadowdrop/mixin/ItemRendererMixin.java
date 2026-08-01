package net.terrunic.shadowdrop.mixin;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.terrunic.shadowdrop.ShadowDrop;
import net.terrunic.shadowdrop.ShadowDropConfig;
import net.terrunic.shadowdrop.render.ShadowBufferSource;
import net.terrunic.shadowdrop.util.CachedPixel;
import net.terrunic.shadowdrop.util.PixelReader;
import net.terrunic.shadowdrop.util.ShadowContext;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

// Mixin to render drop shadows under items in GUI contexts
@Mixin(value = ItemRenderer.class, priority = 500)
public class ItemRendererMixin {
    // Cache
    @Unique
    private static final int shadowdrop$MAX_CACHED_PIXELS = 4096;
    @Unique
    private static final List<CachedPixel> shadowdrop$cachedPixels = new ArrayList<>();
    @Unique
    private static final ByteBuffer shadowdrop$pixelBuffer = BufferUtils.createByteBuffer(16);
    @Unique
    private final static Matrix4f shadowdrop$screenPose = new Matrix4f();
    @Unique
    private static int[] shadowdrop$cachedShadowColor = {0, 0, 0};
    @Unique
    private static int shadowdrop$cachedShadowColorHash = 0;
    // Trackers
    @Unique
    private static boolean shadowdrop$isRenderingShadow = false;
    @Unique
    private final Matrix3f shadowdrop$screenNormal = new Matrix3f();
    // Shadows
    @Final
    @Shadow
    private Minecraft minecraft;

    @Inject(method = "render(Lnet/minecraft/world/item/ItemStack;" + "Lnet/minecraft/world/item/ItemDisplayContext;" + "ZLcom/mojang/blaze3d/vertex/PoseStack;" + "Lnet/minecraft/client/renderer/MultiBufferSource;" + "IILnet/minecraft/client/resources/model/BakedModel;)V", at = @At("HEAD"))
    private void shadowdrop$renderShadow(ItemStack itemStack, ItemDisplayContext displayContext, boolean leftHand, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay, BakedModel model, CallbackInfo ci) {
        if (shadowdrop$isRenderInvalid(itemStack, displayContext)) return;
        if (ShadowDrop.guiRenderDepth++ > 0) return;

        // Refresh pixel cache if called elsewhere
        if (ShadowDrop.shouldRefresh) {
            ShadowDrop.shouldRefresh = false;
            shadowdrop$cachedPixels.clear();
        }

        // Cancel if marked as transparent
        if (shadowdrop$matchesItemOrTag(itemStack, ShadowDropConfig.CLIENT.transparentItems)) return;

        // Cancel for custom renderers
        if (model.isCustomRenderer()) return;

        // Offset item to ensure space behind for shadow
        if (ShadowDropConfig.CLIENT.offsetItems) {
            Matrix4f itemMatrix = poseStack.last().pose();
            float scaleZ = new Vector3f(itemMatrix.m02(), itemMatrix.m12(), itemMatrix.m22()).length() / 16f;
            poseStack.last().pose().translateLocal(0, 0, 32 * scaleZ);
        }

        // Get shadow context & crop state
        ShadowContext shadowContext = ShadowContext.ELSEWHERE;
        shadowdrop$screenPose.set(poseStack.last().pose());
        shadowdrop$screenNormal.set(poseStack.last().normal());
        Matrix4f itemMatrix = poseStack.last().pose();
        GuiGraphics guiGraphics = (GuiGraphics) ShadowDrop.currentGuiGraphics;

        if (ShadowDrop.isHotbarRendering) {
            shadowContext = ShadowContext.HOTBAR;
        } else if (ShadowDrop.hoveredItem == itemStack) {
            if (!ShadowDrop.hoveredItemRendered) {
                ShadowDrop.hoveredItemRendered = true;
                shadowContext = ShadowContext.HOVER;
            }
        } else if (minecraft.player != null && minecraft.player.containerMenu.getCarried().equals(itemStack)) {
            shadowContext = ShadowContext.CURSOR;
        } else if (shadowdrop$isInSlot((int) itemMatrix.m30() - 8, (int) itemMatrix.m31() - 8, (int) itemMatrix.m32())) {
            shadowContext = ShadowContext.SLOT;
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

        float scaleZ = new Vector3f(itemMatrix.m02(), itemMatrix.m12(), itemMatrix.m22()).length() / 16f;

        // Render copy of item with wrapped buffer source to draw it as shadow
        int[] shadowColor = shadowdrop$getShadowColor();
        float r = shadowColor[0] / 255f;
        float g = shadowColor[1] / 255f;
        float b = shadowColor[2] / 255f;
        float a = shadowdrop$getShadowAlpha(itemStack) / 255f;
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

        PoseStack shadowPoseStack = new PoseStack();
        Matrix4f shadowMatrix = shadowPoseStack.last().pose();
        shadowMatrix.set(shadowdrop$screenPose);
        shadowPoseStack.last().normal().set(shadowdrop$screenNormal);
        shadowMatrix.translate(ShadowDropConfig.CLIENT.shadowXOffset / 16f, -ShadowDropConfig.CLIENT.shadowYOffset / 16f, 0);
        shadowMatrix.translateLocal(0, 0, -1.5f * scaleZ * 16f);
        shadowMatrix.m02(0f).m12(0f).m22(0f);

        try {
            ((ItemRenderer) (Object) this).render(itemStack, displayContext, leftHand, shadowPoseStack, shadowBuffer, combinedLight, combinedOverlay, model);

            if (immediate != null) {
                shadowBuffer.endShadowBatches(immediate);
            }
        } finally {
            if (isCropped) {
                guiGraphics.disableScissor();
            }
            shadowdrop$isRenderingShadow = false;
        }
    }

    @Inject(method = "render(Lnet/minecraft/world/item/ItemStack;" + "Lnet/minecraft/world/item/ItemDisplayContext;" + "ZLcom/mojang/blaze3d/vertex/PoseStack;" + "Lnet/minecraft/client/renderer/MultiBufferSource;" + "IILnet/minecraft/client/resources/model/BakedModel;)V", at = @At("RETURN"))
    private void shadowdrop$onRenderReturn(ItemStack itemStack, ItemDisplayContext displayContext, boolean leftHand, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay, BakedModel model, CallbackInfo ci) {
        if (shadowdrop$isRenderInvalid(itemStack, displayContext)) return;
        if (ShadowDrop.guiRenderDepth > 0) {
            ShadowDrop.guiRenderDepth = 0;
        }
    }

    // If item & display context is immediately invalid render state for shadows (cheap)
    @Unique
    private boolean shadowdrop$isRenderInvalid(ItemStack itemStack, ItemDisplayContext displayContext) {
        return shadowdrop$isRenderingShadow || !ShadowDropConfig.CLIENT.modEnabled || ShadowDrop.isLevelRendering || displayContext != ItemDisplayContext.GUI || itemStack.isEmpty();
    }

    // Returns whether the bottom right corner of the given window position has valid slot corner color
    @Unique
    private boolean shadowdrop$isInSlot(int x, int y, int z) {
        // Slot detection compares the live pixel against the GUI background
        // Without that capture there's nothing to compare to, so skip the readback
        ByteBuffer initBuffer = ShadowDrop.guiInitRenderBuffer;
        if (initBuffer == null) return false;

        // Check cache for if current pixel has already been checked
        for (CachedPixel p : shadowdrop$cachedPixels) {
            if (x == p.x() && y == p.y() && z == p.z()) return p.isSlotCorner();
        }

        // Get actual screen position to read bottom-right corner pixel
        Window window = minecraft.getWindow();
        int scale = (int) window.getGuiScale();
        int width = window.getWidth();
        int height = window.getHeight();
        int brX = (x + 16) * scale - 1;
        int brY = height - ((y + 16) * scale + 1);
        int i = (brY * width + brX + 1) * 4;

        // Only probe when the 2x2 read and the capture lookup both sit fully inside the framebuffer
        boolean inBounds = brX >= 0 && brY >= 0 && brX + 2 <= width && brY + 2 <= height && i >= 0 && i + 2 < initBuffer.capacity() && initBuffer.capacity() >= width * height * 4;

        boolean isSlotCorner = false;
        if (inBounds) {
            try {
                // Read screen pixels for slot
                ByteBuffer buffer = shadowdrop$pixelBuffer;
                PixelReader.read(brX, brY, 2, 2, buffer);

                // Outer pixel color
                int r1 = buffer.get(4) & 0xFF;
                int g1 = buffer.get(5) & 0xFF;
                int b1 = buffer.get(6) & 0xFF;
                // Inner pixel color
                int r2 = buffer.get(8) & 0xFF;
                int g2 = buffer.get(9) & 0xFF;
                int b2 = buffer.get(10) & 0xFF;
                // GUI initial render outer pixel color
                int r3 = initBuffer.get(i) & 0xFF;
                int g3 = initBuffer.get(i + 1) & 0xFF;
                int b3 = initBuffer.get(i + 2) & 0xFF;

                // Assume slot corner if outer pixel is different to inner pixel, and is over gui background
                boolean onGuiBackground = !(r1 == r3 && g1 == g3 && b1 == b3);
                isSlotCorner = onGuiBackground && r1 != r2 && g1 != g2 && b1 != b2;

            } catch (Exception ignored) {
            }
        }

        // Cache failures too, so a position that can't be probed doesn't read back every frame
        if (shadowdrop$cachedPixels.size() < shadowdrop$MAX_CACHED_PIXELS) {
            shadowdrop$cachedPixels.add(new CachedPixel(x, y, z, isSlotCorner));
        }

        return isSlotCorner;
    }

    // Get RGB color for item shadow
    @Unique
    private int[] shadowdrop$getShadowColor() {
        String hexColor = ShadowDropConfig.CLIENT.shadowColor;
        int newColorHash = hexColor.hashCode();

        if (shadowdrop$cachedShadowColorHash != newColorHash) {
            shadowdrop$cachedShadowColorHash = newColorHash;
            shadowdrop$cachedShadowColor = shadowdrop$hexColorToRGB(hexColor);
        }
        return shadowdrop$cachedShadowColor;
    }

    // Get alpha for shadow of an item stack
    @Unique
    private int shadowdrop$getShadowAlpha(ItemStack stack) {
        int alpha = ShadowDropConfig.CLIENT.shadowAlpha;
        if (shadowdrop$matchesItemOrTag(stack, ShadowDropConfig.CLIENT.translucentItems)) return alpha / 2;

        return alpha;
    }

    // If item matches list of ids or tags
    @Unique
    private boolean shadowdrop$matchesItemOrTag(ItemStack stack, List<String> entries) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());

        for (String entry : entries) {
            // Entries starting with # are tag ids, otherwise are item ids
            if (entry.startsWith("#")) {
                String[] tagParts = entry.substring(1).split(":");
                if (tagParts.length == 2) {
                    TagKey<Item> tag = TagKey.create(Registries.ITEM, new ResourceLocation(tagParts[0], tagParts[1]));
                    if (stack.is(tag)) return true;
                }
            } else if (itemId.toString().equals(entry)) return true;
        }
        return false;
    }

    // Parse hex color string to RGB array
    @Unique
    private int[] shadowdrop$hexColorToRGB(String hex) {
        // Default to white
        if (!hex.startsWith("#") || hex.length() != 7) return new int[]{255, 255, 255};
        try {
            int r = Integer.parseInt(hex.substring(1, 3), 16);
            int g = Integer.parseInt(hex.substring(3, 5), 16);
            int b = Integer.parseInt(hex.substring(5, 7), 16);
            return new int[]{r, g, b};
        } catch (NumberFormatException e) {
            return new int[]{255, 255, 255};
        }
    }
}
