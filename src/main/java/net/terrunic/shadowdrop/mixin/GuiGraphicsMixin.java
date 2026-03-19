package net.terrunic.shadowdrop.mixin;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import net.terrunic.shadowdrop.CachedPixel;
import net.terrunic.shadowdrop.ShadowDrop;
import net.terrunic.shadowdrop.ShadowDropConfig;
import org.joml.Matrix4f;
import org.joml.Vector4f;
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

// Mixin to also render drop shadows under items rendered in GUI contexts
@Mixin(GuiGraphics.class)
public abstract class GuiGraphicsMixin
{
    // Shadows
    @Final @Shadow private PoseStack pose;
    @Final @Shadow private Minecraft minecraft;
    @Shadow public abstract void flush();
    @Shadow public abstract MultiBufferSource.BufferSource bufferSource();

    // Cache
    @Unique private static List<CachedPixel> shadowdrop$cachedPixels = null;
    @Unique private static List<int[]> shadowdrop$cachedSlotColors = null;
    @Unique private static int shadowdrop$lastColorListHash = 0;

    @Inject(method = "renderItem(Lnet/minecraft/world/entity/LivingEntity;"
                   + "Lnet/minecraft/world/level/Level;"
                   + "Lnet/minecraft/world/item/ItemStack;IIII)V",
            at = @At("HEAD"))
    private void shadowdrop$shadow(LivingEntity entity, Level level, ItemStack stack, int x, int y, int seed, int guiOffset, CallbackInfo ci)
    {
        if (!ShadowDropConfig.CLIENT.modEnabled.get() || stack.isEmpty()) return;

        try
        {
            // Clean render batch
            flush();

            // Is the item being carried by cursor
            boolean isCarried = false;
            if (Minecraft.getInstance().player != null)
            {
                isCarried = Minecraft.getInstance().player.containerMenu.getCarried().equals(stack);
            }

            // Refresh pixel cache if remotely called to
            if (ShadowDrop.shouldRefresh)
            {
                ShadowDrop.shouldRefresh = false;
                shadowdrop$cachedPixels = null;
            }

            // Determine if in slot to apply crop
            boolean isSlot = false;
            boolean doCrop = false;
            boolean cropToHotbarSlots = ShadowDropConfig.CLIENT.cropToHotbarSlots.get();
            boolean onlySlotShadows = ShadowDropConfig.CLIENT.onlySlotShadows.get();
            boolean cropToVisualSlots = ShadowDropConfig.CLIENT.cropToVisualSlots.get();
            if (!isCarried)
            {
                if ((cropToHotbarSlots || onlySlotShadows) && seed > 0 && seed < 11)
                {
                    isSlot = true;
                    doCrop = cropToHotbarSlots;
                }
                else if (cropToVisualSlots || onlySlotShadows)
                {
                    Window window = minecraft.getWindow();
                    isSlot = shadowdrop$isOverSlot(x, y, (int) window.getGuiScale(), window);
                    doCrop = cropToVisualSlots && isSlot;
                }
                if (!isSlot && onlySlotShadows) return;
            }

            // Render item with color mask off to only write to depth
            BakedModel bakedmodel = minecraft.getItemRenderer().getModel(stack, level, entity, seed);
            int shadowXOffset = ShadowDropConfig.CLIENT.shadowXOffset.get();
            int shadowYOffset = ShadowDropConfig.CLIENT.shadowYOffset.get();
            RenderSystem.depthFunc(GL11.GL_ALWAYS);
            RenderSystem.colorMask(false, false, false, false);
            RenderSystem.depthMask(true);
            pose.pushPose();
            pose.translate((x + 8 + shadowXOffset), (y + 8 + shadowYOffset), (float)(140 + (bakedmodel.isGui3d() ? guiOffset : 0)));
            pose.mulPoseMatrix((new Matrix4f()).scaling(1.0F, -1.0F, 1.0F));
            pose.scale(16.0F, 16.0F, 16.0F);
            minecraft.getItemRenderer().render(stack, ItemDisplayContext.GUI, false, pose, bufferSource(), 15728880, OverlayTexture.NO_OVERLAY, bakedmodel);
            flush();
            pose.popPose();

            // Re-enable color, set to greater depth test
            RenderSystem.colorMask(true, true, true, true);
            RenderSystem.depthFunc(GL11.GL_GREATER);
            RenderSystem.depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);

            // Build shadow quad to overlay item
            Matrix4f matrix = pose.last().pose();
            BufferBuilder builder = Tesselator.getInstance().getBuilder();
            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

            // Get shadow quad params
            int x1 = x + shadowXOffset;
            int y1 = y + shadowYOffset;
            int x2 = x1 + 16;
            int y2 = y1 + 16;
            if (doCrop) // Crop by moving quad back
            {
                x2 -= shadowXOffset;
                y2 -= shadowYOffset;
            }
            int a = shadowdrop$getShadowAlpha(stack);
            float z = (float)(100 + (bakedmodel.isGui3d() ? guiOffset : 0));

            // Draw shadow quad
            builder.vertex(matrix, x1, y2, z).color(0, 0, 0, a).endVertex();
            builder.vertex(matrix, x2, y2, z).color(0, 0, 0, a).endVertex();
            builder.vertex(matrix, x2, y1, z).color(0, 0, 0, a).endVertex();
            builder.vertex(matrix, x1, y1, z).color(0, 0, 0, a).endVertex();
            BufferUploader.drawWithShader(builder.end());

            // Restore depth function
            RenderSystem.depthFunc(GL11.GL_LEQUAL);
            RenderSystem.depthMask(true);
            RenderSystem.disableBlend();
        }
        catch (Throwable throwable)
        {
            CrashReport crashreport = CrashReport.forThrowable(throwable, "Rendering item drop shadow");
            CrashReportCategory crashreportcategory = crashreport.addCategory("Shadow being rendered");
            crashreportcategory.setDetail("Registry Name", () -> String.valueOf(ForgeRegistries.ITEMS.getKey(stack.getItem())));
            throw new ReportedException(crashreport);
        }
    }

    // Returns whether the bottom right corner of the given window position has valid slot corner color
    @Unique private boolean shadowdrop$isOverSlot(int x, int y, int scale, Window window)
    {
        // Update cache if config changed
        List<? extends String> colorStrings = ShadowDropConfig.CLIENT.slotBrColors.get();
        int currentHash = colorStrings.hashCode();
        if (shadowdrop$cachedSlotColors == null || shadowdrop$lastColorListHash != currentHash)
        {
            shadowdrop$cachedSlotColors = new ArrayList<>();
            shadowdrop$cachedPixels = new ArrayList<>();
            for (String hexColor : colorStrings)
            {
                shadowdrop$cachedSlotColors.add(shadowdrop$parseHexColor(hexColor));
            }
            shadowdrop$lastColorListHash = currentHash;
        }

        // Get actual screen position to read bottom-right corner pixel
        Vector4f transformed = new Vector4f(x, y, 0, 1);
        ((GuiGraphics)(Object)this).pose().last().pose().transform(transformed);
        x = Math.round(transformed.x());
        y = Math.round(transformed.y());
        int z = Math.round(transformed.z());
        int brX = (x + 16) * scale;
        int brY = window.getHeight() - ((y + 16) * scale + 1);

        // Check cache for if current pixel has already been checked
        if (shadowdrop$cachedPixels == null) shadowdrop$cachedPixels = new ArrayList<>();
        for (CachedPixel ps : shadowdrop$cachedPixels)
        {
            if (x == ps.x() && y == ps.y() && z == ps.z()) return ps.isSlotCorner();
        }

        // Read corner pixel color
        ByteBuffer buffer = BufferUtils.createByteBuffer(3);
        GL11.glReadPixels(brX, brY, 1, 1, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
        int r = buffer.get(0) & 0xFF;
        int g = buffer.get(1) & 0xFF;
        int b = buffer.get(2) & 0xFF;

        // Check if color is of slot corner (specified in config)
        boolean isSlot = false;
        for (int[] color : shadowdrop$cachedSlotColors)
        {
            if (r == color[0] && g == color[1] && b == color[2])
            {
                isSlot = true;
                break;
            }
        }
        // Add result to cache and return it
        shadowdrop$cachedPixels.add(new CachedPixel(x, y, z, isSlot));
        return isSlot;
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
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (itemId == null) return false;

        for (String entry : entries)
        {
            // Entries starting with # are tag ids, otherwise are item ids
            if (entry.startsWith("#"))
            {
                String[] tagParts = entry.substring(1).split(":");
                if (tagParts.length == 2)
                {
                    TagKey<Item> tag = ItemTags.create(new ResourceLocation(tagParts[0], tagParts[1]));
                    if (stack.is(tag)) return true;
                }
            }
            else if (itemId.toString().equals(entry)) return true;
        }
        return false;
    }

    // Parse hex color string to RGB array
    @Unique private int[] shadowdrop$parseHexColor(String hex)
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