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
import net.terrunic.shadowdrop.compat.RaisedCompat;
import net.terrunic.shadowdrop.render.ShadowBufferSource;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

// Mixin to render drop shadows under items in GUI contexts
@Mixin(value = ItemRenderer.class, priority = 500)
public class ItemRendererMixin {
    @Unique
    private final static Matrix4f shadowdrop$screenPose = new Matrix4f();
    @Unique
    private static int[] shadowdrop$cachedShadowColor = {0, 0, 0};
    @Unique
    private static int shadowdrop$cachedShadowColorHash = 0;
    @Unique
    private static boolean shadowdrop$isRenderingShadow = false;
    @Unique
    private final Matrix3f shadowdrop$screenNormal = new Matrix3f();
    @Final
    @Shadow
    private Minecraft minecraft;

    @Inject(method = "render(Lnet/minecraft/world/item/ItemStack;" +
            "Lnet/minecraft/world/item/ItemDisplayContext;" +
            "ZLcom/mojang/blaze3d/vertex/PoseStack;" +
            "Lnet/minecraft/client/renderer/MultiBufferSource;" +
            "IILnet/minecraft/client/resources/model/BakedModel;)V",
            at = @At("HEAD"))
    private void shadowdrop$renderShadowAndTranslate(ItemStack itemStack, ItemDisplayContext displayContext, boolean leftHand, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay, BakedModel model, CallbackInfo ci) {
        if (itemStack.isEmpty() || displayContext != ItemDisplayContext.GUI || shadowdrop$isRenderingShadow || !ShadowDropConfig.CLIENT.modEnabled)
            return;

        if (ShadowDrop.guiRenderDepth > 0) {
            ShadowDrop.guiRenderDepth++;
            return;
        }
        ShadowDrop.guiRenderDepth++;

        shadowdrop$screenPose.set(poseStack.last().pose());
        shadowdrop$screenNormal.set(poseStack.last().normal());

        if (ShadowDropConfig.CLIENT.offsetItems) {
            Matrix4f itemMatrix = poseStack.last().pose();
            float scaleZ = new Vector3f(itemMatrix.m02(), itemMatrix.m12(), itemMatrix.m22()).length() / 16f;
            poseStack.last().pose().translateLocal(0, 0, 32 * scaleZ);
        }

        if (ShadowDrop.shouldRefresh) {
            ShadowDrop.shouldRefresh = false;
        }

        if (shadowdrop$matchesItemOrTag(itemStack, ShadowDropConfig.CLIENT.transparentItems)) return;

        Matrix4f itemMatrix = poseStack.last().pose();
        int itemX = (int) itemMatrix.m30() - 8;
        int itemY = (int) itemMatrix.m31() - 8;
        boolean isInCursor = (minecraft.player != null && minecraft.player.containerMenu.getCarried().equals(itemStack));
        boolean isInSlot = !isInCursor && shadowdrop$isInSlot(itemStack, itemX, itemY, (int) itemMatrix.m32());
        boolean isInHotbar = shadowdrop$isInHotbarSlot(itemStack, itemX, itemY, itemMatrix.m32());

        boolean shouldRender = ShadowDropConfig.CLIENT.shadowsAlways
                || (ShadowDropConfig.CLIENT.shadowsInSlots && isInSlot)
                || (ShadowDropConfig.CLIENT.shadowsInHotbar && isInHotbar)
                || (ShadowDropConfig.CLIENT.shadowsInCursor && isInCursor);
        if (!shouldRender) return;

        int shadowXOffset = ShadowDropConfig.CLIENT.shadowXOffset;
        int shadowYOffset = ShadowDropConfig.CLIENT.shadowYOffset;

        shadowdrop$isRenderingShadow = true;
        GuiGraphics guiGraphics = (GuiGraphics) ShadowDrop.currentGuiGraphics;
        boolean scissorEnabled = false;
        try {
            float scaleZ = new Vector3f(itemMatrix.m02(), itemMatrix.m12(), itemMatrix.m22()).length() / 16f;

            // Render copy of item with wrapped buffer source to draw it as shadow
            int[] shadowColor = shadowdrop$getShadowColor();
            float r = shadowColor[0] / 255f;
            float g = shadowColor[1] / 255f;
            float b = shadowColor[2] / 255f;
            float a = shadowdrop$getShadowAlpha(itemStack) / 255f;

            boolean isHoveredSlot = ShadowDrop.hoveredItem == itemStack;
            boolean isCropped = !isInCursor
                    && !(ShadowDropConfig.CLIENT.uncropUnderCursor && isHoveredSlot && !isInHotbar)
                    && (ShadowDropConfig.CLIENT.cropToSlots && isInSlot
                    || ShadowDropConfig.CLIENT.cropToHotbar && isInHotbar);

            int slotX = (int) shadowdrop$screenPose.m30() - 8;
            int slotY = (int) shadowdrop$screenPose.m31() - 8;

            BufferSource immediate = null;
            if (bufferSource instanceof BufferSource buf) {
                immediate = buf;
            } else if (guiGraphics != null) {
                immediate = guiGraphics.bufferSource();
            }

            if (isCropped && guiGraphics != null) {
                immediate.endBatch();

                int cropWidth = ShadowDrop.isRenderingEmiOutputSlot ? ShadowDrop.emiSlotWidth : 16;
                int cropHeight = ShadowDrop.isRenderingEmiOutputSlot ? ShadowDrop.emiSlotHeight : 16;
                int cropX = slotX - (cropWidth - 16) / 2;
                int cropY = slotY - (cropHeight - 16) / 2;
                guiGraphics.enableScissor(cropX, cropY, cropX + cropWidth, cropY + cropHeight);
                scissorEnabled = true;
            }

            ShadowBufferSource shadowBuffer = new ShadowBufferSource(bufferSource, r, g, b, a);

            PoseStack shadowPoseStack = new PoseStack();
            Matrix4f shadowMatrix = shadowPoseStack.last().pose();
            shadowMatrix.set(shadowdrop$screenPose);
            shadowPoseStack.last().normal().set(shadowdrop$screenNormal);
            shadowMatrix.translate(shadowXOffset / 16f, -shadowYOffset / 16f, 0);
            shadowMatrix.translateLocal(0, 0, -1.5f * scaleZ * 16f);

            shadowMatrix.m20(0f).m21(0f).m22(0f);
            shadowMatrix.m02(0f).m12(0f);

            ((ItemRenderer) (Object) this).render(itemStack, displayContext, leftHand, shadowPoseStack, shadowBuffer, combinedLight, combinedOverlay, model);

            if (immediate != null) {
                shadowBuffer.endShadowBatches(immediate);
            }
        } finally {
            if (scissorEnabled) {
                guiGraphics.disableScissor();
            }
            shadowdrop$isRenderingShadow = false;
        }
    }

    @Inject(method = "render(Lnet/minecraft/world/item/ItemStack;" +
            "Lnet/minecraft/world/item/ItemDisplayContext;" +
            "ZLcom/mojang/blaze3d/vertex/PoseStack;" +
            "Lnet/minecraft/client/renderer/MultiBufferSource;" +
            "IILnet/minecraft/client/resources/model/BakedModel;)V",
            at = @At("RETURN"))
    private void shadowdrop$onRenderReturn(ItemStack itemStack, ItemDisplayContext displayContext, boolean leftHand, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay, BakedModel model, CallbackInfo ci) {
        if (itemStack.isEmpty() || displayContext != ItemDisplayContext.GUI || shadowdrop$isRenderingShadow || !ShadowDropConfig.CLIENT.modEnabled)
            return;

        if (ShadowDrop.guiRenderDepth > 0) {
            ShadowDrop.guiRenderDepth--;
            if (ShadowDrop.guiRenderDepth == 0 && ShadowDropConfig.CLIENT.offsetItems) {
                Matrix4f itemMatrix = poseStack.last().pose();
                float scaleZ = new Vector3f(itemMatrix.m02(), itemMatrix.m12(), itemMatrix.m22()).length() / 16f;
                poseStack.last().pose().translateLocal(0, 0, -32 * scaleZ);
            }
        }
    }

    // Returns whether the bottom right corner of the given window position has valid slot corner color
    @Unique
    private boolean shadowdrop$isInSlot(ItemStack itemStack, int x, int y, int z) {
        if (ShadowDrop.currentRenderingSlot != null || ShadowDrop.isRenderingEmiSlot) {
            // Only consider items at standard slot Z levels to prevent false positives in tooltips/etc
            return Math.abs(z - 150) < 5f || Math.abs(z - 182) < 5f || Math.abs(z - 250) < 5f || Math.abs(z - 282) < 5f;
        }
        return false;
    }

    // Returns whether an item is currently being rendered in a HUD hotbar slot
    @Unique
    private boolean shadowdrop$isInHotbarSlot(ItemStack pItemStack, int x, int y, float z) {
        if (minecraft.player == null || minecraft.screen != null) return false;

        // HUD hotbar items are rendered at Z = 150 (Fabric/Vanilla) or Z = 550 on NeoForge (for some godforsaken reason)
        boolean isValidZ = Math.abs(z - 150) < 1f || Math.abs(z - 182) < 1f || Math.abs(z - 550) < 1f || Math.abs(z - 582) < 1f;
        if (!isValidZ) return false;

        boolean inHotbar = false;
        for (int i = 0; i < 9; i++) {
            if (minecraft.player.getInventory().getItem(i) == pItemStack) {
                inHotbar = true;
                break;
            }
        }
        if (!inHotbar && minecraft.player.getOffhandItem() == pItemStack) {
            inHotbar = true;
        }
        if (!inHotbar) return false;

        Window window = minecraft.getWindow();
        int guiWidth = window.getGuiScaledWidth();
        int guiHeight = window.getGuiScaledHeight();

        int raisedX = RaisedCompat.getHotbarXOffset();
        int raisedY = RaisedCompat.getHotbarYOffset();
        int unshiftedX = x - raisedX;
        int unshiftedY = y - raisedY;

        int expectedY = guiHeight - 19;

        if (Math.abs(unshiftedY - expectedY) > 2) return false;

        int midX = guiWidth / 2;
        for (int i = 0; i < 9; i++) {
            int expectedX = midX - 90 + i * 20 + 2;
            if (Math.abs(unshiftedX - expectedX) <= 2) return true;
        }

        int offhandX1 = midX - 117;
        int offhandX2 = midX + 101;
        return Math.abs(unshiftedX - offhandX1) <= 2 || Math.abs(unshiftedX - offhandX2) <= 2;
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

    @Unique
    private boolean shadowdrop$matchesItemOrTag(ItemStack stack, List<String> entries) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());

        for (String entry : entries) {
            // Entries starting with # are tag ids, otherwise are item ids
            if (entry.startsWith("#")) {
                String[] tagParts = entry.substring(1).split(":");
                if (tagParts.length == 2) {
                    TagKey<Item> tag = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(tagParts[0], tagParts[1]));
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
