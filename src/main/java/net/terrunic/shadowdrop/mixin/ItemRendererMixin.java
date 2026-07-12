package net.terrunic.shadowdrop.mixin;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.terrunic.shadowdrop.ShadowDrop;
import net.terrunic.shadowdrop.ShadowDropConfig;
import net.terrunic.shadowdrop.utils.CachedPixel;
import net.terrunic.shadowdrop.utils.ShadowContext;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
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
@Mixin(ItemRenderer.class)
public class ItemRendererMixin
{
    // Shadows
    @Final @Shadow private Minecraft minecraft;

    // Objects shared between item renders
    @Unique private static final Tesselator shadowdrop$tesselator = Tesselator.getInstance();

    // Tracked states
    @Unique private static boolean shadowdrop$isRenderingShadow = false;
    @Unique private static boolean shadowdrop$inRealSlot = false;

    // Cache for optimisation
    @Unique private static final List<CachedPixel> shadowdrop$cachedPixels = new ArrayList<>();
    @Unique private static final List<int[]> shadowdrop$cachedColors = new ArrayList<>();
    @Unique private static int shadowdrop$cachedColorsHash = 0;
    @Unique private static int[] shadowdrop$cachedShadowColor = { 0, 0, 0 };
    @Unique private static int shadowdrop$cachedShadowColorHash = 0;

    @Inject(method = "render(Lnet/minecraft/world/item/ItemStack;" +
                     "Lnet/minecraft/world/item/ItemDisplayContext;" +
                     "ZLcom/mojang/blaze3d/vertex/PoseStack;" +
                     "Lnet/minecraft/client/renderer/MultiBufferSource;" +
                     "IILnet/minecraft/client/resources/model/BakedModel;)V",
            at = @At("HEAD"))
    private void shadowdrop$renderShadow(ItemStack pItemStack, ItemDisplayContext pDisplayContext, boolean pLeftHand, PoseStack pPoseStack, MultiBufferSource pBuffer, int pCombinedLight, int pCombinedOverlay, BakedModel pModel, CallbackInfo ci)
    {
        ShadowDropConfig c = ShadowDropConfig.INSTANCE;
        boolean allContextsDisabled = !(c.hotbarEnabled.get() || c.slotEnabled.get() || c.hoverEnabled.get() || c.cursorEnabled.get() || c.outsideEnabled.get() || c.elsewhereEnabled.get());
        if (pItemStack.isEmpty() || pDisplayContext != ItemDisplayContext.GUI || shadowdrop$isRenderingShadow || c.shadowAlpha.get() == 0 || allContextsDisabled || ShadowDrop.isLevelRendering) return;

        // Refresh pixel cache if remotely called to
        if (ShadowDrop.shouldRefresh) {
            ShadowDrop.shouldRefresh = false;
            shadowdrop$cachedPixels.clear();
        }

        // Get rendering context
        if (shadowdrop$matchesItemOrTag(pItemStack, c.transparentItems.get())) return;

        Matrix4f itemMatrix = pPoseStack.last().pose();
        ShadowContext shadowContext = ShadowContext.ELSEWHERE;
        int itemX = (int)itemMatrix.m30() - 8;
        int itemY = (int)itemMatrix.m31() - 8;
        int itemZ = (int)itemMatrix.m32();
        boolean forceUncrop = false;

        if (ShadowDrop.forcedContext != null) {
            // Force context (user for config preview)
            shadowContext = ShadowDrop.forcedContext;
            ShadowDrop.forcedContext = null;
        }
        else if (minecraft.player != null && minecraft.player.containerMenu.getCarried().equals(pItemStack)) {
            shadowContext = ShadowContext.CURSOR;
        }
        else if (ShadowDrop.hoveredItem == pItemStack) {
            shadowContext = ShadowContext.HOVER;
        }
        else if (shadowdrop$isInHotbarSlot(pItemStack, itemZ)) {
            shadowContext = ShadowContext.HOTBAR;
        }
        else if (shadowdrop$isInSlot(itemX, itemY, itemZ, pItemStack)) {
            shadowContext = ShadowContext.SLOT;
        }
        else if (shadowdrop$inRealSlot) {
            // Fallback for items in real slots but not visual slots
            // Fixes situations like crafting table output being cropped while still treating it as a slot
            shadowContext = ShadowContext.SLOT;
            forceUncrop = true;
        }
        else if (shadowdrop$isSlotScreen()) {
            shadowContext = ShadowContext.OUTSIDE;
        }

        // Validate rendering
        boolean shouldRender = switch (shadowContext) {
            case HOTBAR    -> c.hotbarEnabled.get();
            case SLOT      -> c.slotEnabled.get();
            case HOVER     -> c.hoverEnabled.get();
            case CURSOR    -> c.cursorEnabled.get();
            case OUTSIDE   -> c.outsideEnabled.get();
            case ELSEWHERE -> c.elsewhereEnabled.get();
        };
        if (!shouldRender) return;

        // Valid rendering context, mark mixin as rendering for recursive call
        shadowdrop$isRenderingShadow = true;

        // Mark for cropping
        boolean isCropped = false;
        if (!forceUncrop) {
            isCropped = switch (shadowContext) {
                case HOTBAR    -> c.hotbarCropped.get();
                case SLOT      -> c.slotCropped.get();
                case HOVER     -> c.hoverCropped.get();
                default        -> false;
            };
        }

        // Clean render batch before rendering
        MultiBufferSource.BufferSource bufferSource = pBuffer instanceof MultiBufferSource.BufferSource bs ? bs : null;
        if (bufferSource != null) bufferSource.endBatch();
        float scaleZ = new Vector3f(itemMatrix.m02(), itemMatrix.m12(), itemMatrix.m22()).length() / 16f;

        // Offset item forward
        pPoseStack.last().pose().translateLocal(0, 0, 32 * scaleZ);

        // Render copy of item (to depth mask) to use for its silhouette
        pPoseStack.pushPose();

        RenderSystem.colorMask(false, false, false, false);
        RenderSystem.depthMask(true);
        if (minecraft.screen != null && c.forceDepthRefresh.get()) GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);

        Matrix4f shadowMatrix = pPoseStack.last().pose();
        shadowMatrix.translate(c.shadowOffsetX.get()/16f, -c.shadowOffsetY.get()/16f, 0);
        shadowMatrix.translateLocal(0, 0, -16 * scaleZ);

        ((ItemRenderer)(Object)this).render(pItemStack, pDisplayContext, pLeftHand, pPoseStack, pBuffer, pCombinedLight, pCombinedOverlay, pModel);

        pPoseStack.popPose();
        RenderSystem.disableDepthTest();
        if (bufferSource != null) bufferSource.endBatch();
        RenderSystem.enableDepthTest();

        // Render shadow quad (to color mask) onto item copy, leaving shadow behind
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.depthFunc(GL11.GL_GREATER);
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        int x1 = -8 -(isCropped ? c.shadowOffsetX.get() : 0);
        int y1 = -8 -(isCropped ? c.shadowOffsetY.get() : 0);
        int x2 = x1 + 16;
        int y2 = y1 + 16;
        int[] shadowColor = shadowdrop$getShadowColor();
        int r = shadowColor[0];
        int g = shadowColor[1];
        int b = shadowColor[2];
        int a = shadowdrop$getShadowAlpha(pItemStack);

        shadowMatrix.scale(1/16f, -1/16f, 1);
        shadowMatrix.translateLocal(0, 0, -16 * scaleZ);

        BufferBuilder builder = shadowdrop$tesselator.getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        builder.vertex(shadowMatrix, x1, y2, 0).color(r, g, b, a).endVertex();
        builder.vertex(shadowMatrix, x2, y2, 0).color(r, g, b, a).endVertex();
        builder.vertex(shadowMatrix, x2, y1, 0).color(r, g, b, a).endVertex();
        builder.vertex(shadowMatrix, x1, y1, 0).color(r, g, b, a).endVertex();
        BufferUploader.drawWithShader(builder.end());

        // Restore render system
        RenderSystem.depthFunc(GL11.GL_LEQUAL);
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();

        shadowdrop$isRenderingShadow = false;
    }

    // Returns whether the bottom right corner of the given window position has valid slot corner color
    @Unique private boolean shadowdrop$isInSlot(int x, int y, int z, ItemStack stack)
    {
        // Check if real slot
        shadowdrop$inRealSlot = false;
        if (minecraft.screen instanceof AbstractContainerScreen<?> screen) {
            for (Slot slot : screen.getMenu().slots) {
                if (slot.getItem() == stack) {
                    shadowdrop$inRealSlot = true;
                    break;
                }
            }
        }

        // Update cache if config changed
        List<? extends String> hexColors = ShadowDropConfig.INSTANCE.slotBrColors.get();
        int newColorsHash = hexColors.hashCode();

        if (shadowdrop$cachedColorsHash != newColorsHash) {
            shadowdrop$cachedColorsHash = newColorsHash;
            shadowdrop$cachedColors.clear();
            for (String c : hexColors) shadowdrop$cachedColors.add(shadowdrop$hexColorToRGB(c));
            shadowdrop$cachedPixels.clear();
        }

        // Get actual screen position to read bottom-right corner pixel
        Window window = minecraft.getWindow();
        int scale = (int) window.getGuiScale();
        int brX = (x + 16) * scale;
        int brY = window.getHeight() - ((y + 16) * scale + 1);

        // Check cache for if current pixel has already been checked
        for (CachedPixel p : shadowdrop$cachedPixels) {
            if (x == p.x() && y == p.y() && z == p.z()) return p.isSlotCorner();
        }

        // Read corner pixel color
        ByteBuffer buffer = BufferUtils.createByteBuffer(3);
        GL11.glReadPixels(brX, brY, 1, 1, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, buffer);
        int r = buffer.get(0) & 0xFF;
        int g = buffer.get(1) & 0xFF;
        int b = buffer.get(2) & 0xFF;

        // Check if color is of slot corner
        boolean isSlotCorner = false;
        for (int[] c : shadowdrop$cachedColors) {
            if (r == c[0] && g == c[1] && b == c[2]) {
                isSlotCorner = true;
                break;
            }
        }
        shadowdrop$cachedPixels.add(new CachedPixel(x, y, z, isSlotCorner));
        return isSlotCorner;
    }

    // Returns whether an item is in a HUD hotbar slot
    @Unique private boolean shadowdrop$isInHotbarSlot(ItemStack pItemStack, float z)
    {
        if (minecraft.player == null || z != 150) return false;

        for (int i = 0; i < 9; i++) {
            if (minecraft.player.getInventory().getItem(i) == pItemStack) return true;
        }
        return minecraft.player.getOffhandItem() == pItemStack;
    }

    // Returns whether the current screen should be classified as one with slots
    @Unique private boolean shadowdrop$isSlotScreen()
    {
        if (minecraft.screen != null) {
            if (minecraft.screen instanceof AbstractContainerScreen) return true;

            String screenName = minecraft.screen.getClass().getName();

            if (ShadowDrop.installedEmi && screenName.contains(".emi.")) return true;
            else if (ShadowDrop.installedJei && screenName.contains(".jei.")) return true;
            else if (ShadowDrop.installedRei && screenName.contains(".rei.")) return true;
        }
        return false;
    }

    // Get RGB color for item shadow
    @Unique private int[] shadowdrop$getShadowColor()
    {
        String hexColor = ShadowDropConfig.INSTANCE.shadowColor.get();
        int newColorHash = hexColor.hashCode();

        if (shadowdrop$cachedShadowColorHash != newColorHash) {
            shadowdrop$cachedShadowColorHash = newColorHash;
            shadowdrop$cachedShadowColor = shadowdrop$hexColorToRGB(hexColor);
        }
        return shadowdrop$cachedShadowColor;
    }

    // Get alpha for shadow of an item stack
    @Unique private int shadowdrop$getShadowAlpha(ItemStack stack)
    {
        int alpha = (int) Math.floor(255f * ShadowDropConfig.INSTANCE.shadowAlpha.get()/100f);
        if (shadowdrop$matchesItemOrTag(stack, ShadowDropConfig.INSTANCE.translucentItems.get())) return alpha/2;

        return alpha;
    }

    // Check if item stack matches any given item ids or tags
    @Unique private boolean shadowdrop$matchesItemOrTag(ItemStack stack, List<? extends String> entries)
    {
        if (entries.isEmpty()) return false;

        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (itemId == null) return false;

        for (String entry : entries) {
            // Entries starting with # are tag ids, otherwise are item ids
            if (entry.startsWith("#")) {
                String[] tagParts = entry.substring(1).split(":");
                if (tagParts.length == 2) {
                    TagKey<Item> tag = ItemTags.create(new ResourceLocation(tagParts[0], tagParts[1]));
                    if (stack.is(tag)) return true;
                }
            }
            else if (itemId.toString().equals(entry)) return true;
        }
        return false;
    }

    // Parse hex color string to RGB array
    @Unique private int[] shadowdrop$hexColorToRGB(String hex)
    {
        // Default to white
        if (!hex.startsWith("#") || hex.length() != 7) return new int[]{ 255, 255, 255 };
        try {
            int r = Integer.parseInt(hex.substring(1, 3), 16);
            int g = Integer.parseInt(hex.substring(3, 5), 16);
            int b = Integer.parseInt(hex.substring(5, 7), 16);
            return new int[]{ r, g, b };
        }
        catch (NumberFormatException e) {
            return new int[]{ 255, 255, 255 };
        }
    }
}
