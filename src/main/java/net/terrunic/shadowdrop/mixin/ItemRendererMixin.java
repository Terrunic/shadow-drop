package net.terrunic.shadowdrop.mixin;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.terrunic.shadowdrop.CachedPixel;
import net.terrunic.shadowdrop.ShadowDrop;
import net.terrunic.shadowdrop.ShadowDropConfig;
import org.joml.Matrix4f;
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

    // Cache for optimisation
    @Unique private static final List<CachedPixel> shadowdrop$cachedPixels = new ArrayList<>();
    @Unique private static final List<int[]> shadowdrop$cachedColors = new ArrayList<>();
    @Unique private static int shadowdrop$cachedColorsHash = 0;
    @Unique private static int[] shadowdrop$cachedShadowColor = { 0, 0, 0 };
    @Unique private static int shadowdrop$cachedShadowColorHash = 0;

    // Marker for recursive calls
    @Unique private static boolean shadowdrop$isRenderingShadow = false;

    @Inject(method = "render(Lnet/minecraft/world/item/ItemStack;" +
                     "Lnet/minecraft/world/item/ItemDisplayContext;" +
                     "ZLcom/mojang/blaze3d/vertex/PoseStack;" +
                     "Lnet/minecraft/client/renderer/MultiBufferSource;" +
                     "IILnet/minecraft/client/resources/model/BakedModel;)V",
            at = @At("HEAD"))
    private void shadowdrop$renderShadow(ItemStack pItemStack, ItemDisplayContext pDisplayContext, boolean pLeftHand, PoseStack pPoseStack, MultiBufferSource pBuffer, int pCombinedLight, int pCombinedOverlay, BakedModel pModel, CallbackInfo ci)
    {
        if (pItemStack.isEmpty() || pDisplayContext != ItemDisplayContext.GUI || shadowdrop$isRenderingShadow || !ShadowDropConfig.CLIENT.modEnabled.get()) return;

        // Refresh pixel cache if remotely called to
        if (ShadowDrop.shouldRefresh)
        {
            ShadowDrop.shouldRefresh = false;
            shadowdrop$cachedPixels.clear();
        }

        // Get rendering context
        Matrix4f itemMatrix = new Matrix4f(pPoseStack.last().pose());
        boolean isInCursor = (minecraft.player != null && minecraft.player.containerMenu.getCarried().equals(pItemStack));
        boolean isInSlot = !isInCursor && shadowdrop$isInSlot((int)itemMatrix.m30() - 8, (int)itemMatrix.m31() - 8, (int)itemMatrix.m32());
        boolean isInHotbar = shadowdrop$isInHotbarSlot(pItemStack, itemMatrix.m32());

        boolean shouldRender = ShadowDropConfig.CLIENT.shadowsAlways.get()
            || (ShadowDropConfig.CLIENT.shadowsInSlots.get() && isInSlot)
            || (ShadowDropConfig.CLIENT.shadowsInHotbar.get() && isInHotbar)
            || (ShadowDropConfig.CLIENT.shadowsInCursor.get() && isInCursor);
        if (!shouldRender) return;

        int shadowXOffset = ShadowDropConfig.CLIENT.shadowXOffset.get();
        int shadowYOffset = ShadowDropConfig.CLIENT.shadowYOffset.get();
        boolean isCropped = !isInCursor &&
            (ShadowDropConfig.CLIENT.cropToSlots.get() && isInSlot
            || ShadowDropConfig.CLIENT.cropToHotbar.get() && isInHotbar);

        // Valid rendering context, mark mixin as rendering for recursive call
        shadowdrop$isRenderingShadow = true;

        // Clean render batch before rendering
        if (pBuffer instanceof MultiBufferSource.BufferSource bs) bs.endBatch();

        // Render copy of item (to depth mask) to use as shadow
        pPoseStack.pushPose();

        RenderSystem.colorMask(false, false, false, false);
        RenderSystem.depthMask(true);
        pPoseStack.translate(shadowXOffset/16f, -shadowYOffset/16f, -10/16f);

        ((ItemRenderer)(Object)this).render(pItemStack, pDisplayContext, pLeftHand, pPoseStack, pBuffer, pCombinedLight, pCombinedOverlay, pModel);

        pPoseStack.popPose();
        RenderSystem.disableDepthTest();
        if (pBuffer instanceof MultiBufferSource.BufferSource bs) bs.endBatch();
        RenderSystem.enableDepthTest();

        // Render shadow quad (to color mask) to overlay item
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.depthFunc(GL11.GL_GREATER);
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        int x1 = shadowXOffset - 8;
        int y1 = shadowYOffset - 8;
        int x2 = x1 + 16 - (isCropped ? shadowXOffset : 0);
        int y2 = y1 + 16 - (isCropped ? shadowYOffset : 0);
        float z = ShadowDropConfig.CLIENT.shadowZOffset.get();
        int[] shadowColor = shadowdrop$getShadowColor();
        int r = shadowColor[0];
        int g = shadowColor[1];
        int b = shadowColor[2];
        int a = shadowdrop$getShadowAlpha(pItemStack);

        Matrix4f quadMatrix = new Matrix4f(itemMatrix).scale(1/16f, -1/16f, 1/16f);
        BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        builder.addVertex(quadMatrix, x1, y2, z).setColor(r, g, b, a);
        builder.addVertex(quadMatrix, x2, y2, z).setColor(r, g, b, a);
        builder.addVertex(quadMatrix, x2, y1, z).setColor(r, g, b, a);
        builder.addVertex(quadMatrix, x1, y1, z).setColor(r, g, b, a);
        BufferUploader.drawWithShader(builder.buildOrThrow());

        // Restore render system
        RenderSystem.depthFunc(GL11.GL_ALWAYS);
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();

        shadowdrop$isRenderingShadow = false;
    }

    // Returns whether the bottom right corner of the given window position has valid slot corner color
    @Unique private boolean shadowdrop$isInSlot(int x, int y, int z)
    {
        // Update cache if config changed
        List<? extends String> hexColors = ShadowDropConfig.CLIENT.slotBrColors.get();
        int newColorsHash = hexColors.hashCode();

        if (shadowdrop$cachedColorsHash != newColorsHash)
        {
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
        for (CachedPixel p : shadowdrop$cachedPixels)
        {
            if (x == p.x() && y == p.y() && z == p.z()) return p.isSlotCorner();
        }

        // Read corner pixel color
        ByteBuffer buffer = BufferUtils.createByteBuffer(3);
        GL11.glReadPixels(brX, brY, 1, 1, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
        int r = buffer.get(0) & 0xFF;
        int g = buffer.get(1) & 0xFF;
        int b = buffer.get(2) & 0xFF;

        // Check if color is of slot corner
        boolean isSlotCorner = false;
        for (int[] c : shadowdrop$cachedColors)
        {
            if (r == c[0] && g == c[1] && b == c[2])
            {
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
        if (minecraft.player == null || z != 550) return false;

        for (int i = 0; i < 9; i++)
        {
            if (minecraft.player.getInventory().getItem(i) == pItemStack) return true;
        }
        return minecraft.player.getOffhandItem() == pItemStack;
    }

    // Get RGB color for item shadow
    @Unique private int[] shadowdrop$getShadowColor()
    {
        String hexColor = ShadowDropConfig.CLIENT.shadowColor.get();
        int newColorHash = hexColor.hashCode();

        if (shadowdrop$cachedShadowColorHash != newColorHash)
        {
            shadowdrop$cachedShadowColorHash = newColorHash;
            shadowdrop$cachedShadowColor = shadowdrop$hexColorToRGB(hexColor);
        }
        return shadowdrop$cachedShadowColor;
    }

    // Get alpha for shadow of an item stack
    @Unique private int shadowdrop$getShadowAlpha(ItemStack stack)
    {
        int alpha = ShadowDropConfig.CLIENT.shadowAlpha.get();
        if (shadowdrop$matchesItemOrTag(stack, ShadowDropConfig.CLIENT.transparentItems.get())) return 0;
        if (shadowdrop$matchesItemOrTag(stack, ShadowDropConfig.CLIENT.translucentItems.get())) return alpha/2;

        return alpha;
    }

    // Check if item stack matches any given item ids or tags
    @Unique private boolean shadowdrop$matchesItemOrTag(ItemStack stack, List<? extends String> entries)
    {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());

        for (String entry : entries)
        {
            // Entries starting with # are tag ids, otherwise are item ids
            if (entry.startsWith("#"))
            {
                String[] tagParts = entry.substring(1).split(":");
                if (tagParts.length == 2)
                {
                    TagKey<Item> tag = ItemTags.create(ResourceLocation.fromNamespaceAndPath(tagParts[0], tagParts[1]));
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
        try
        {
            int r = Integer.parseInt(hex.substring(1, 3), 16);
            int g = Integer.parseInt(hex.substring(3, 5), 16);
            int b = Integer.parseInt(hex.substring(5, 7), 16);
            return new int[]{ r, g, b };
        }
        catch (NumberFormatException e)
        {
            return new int[]{ 255, 255, 255 };
        }
    }
}
