package com.evandev.shadow_drop.mixin;

import com.evandev.shadow_drop.api.IShadowDropItemState;
import com.evandev.shadow_drop.config.ModConfig;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.state.gui.GuiItemRenderState;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(GuiGraphicsExtractor.class)
public abstract class GuiGraphicsExtractorMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @WrapOperation(method = "item(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;III)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/state/gui/GuiRenderState;addItem(Lnet/minecraft/client/renderer/state/gui/GuiItemRenderState;)V"))
    private void shadowdrop$onQueueItem(GuiRenderState instance, GuiItemRenderState itemState, Operation<Void> original, LivingEntity owner, Level level, ItemStack itemStack, int x, int y, int seed) {
        ModConfig config = ModConfig.get();

        if (!itemStack.isEmpty() && config.enabled) {
            boolean isInCursor = (minecraft.player != null && minecraft.player.containerMenu.getCarried().equals(itemStack));
            boolean isInHotbar = shadowdrop$isInHotbarSlot(itemStack, y);
            boolean needsSlotCheck = !isInCursor && !isInHotbar && config.shadowsInSlots;

            boolean shouldRender = config.shadowsAlways
                    || (config.shadowsInHotbar && isInHotbar)
                    || (config.shadowsInCursor && isInCursor)
                    || needsSlotCheck;

            if (shouldRender) {
                int alpha = shadowdrop$getShadowAlpha(itemStack, config);
                if (alpha > 0) {
                    boolean checkCrop = !isInCursor && (config.cropToHotbar && isInHotbar || config.cropToSlots && needsSlotCheck);

                    ((IShadowDropItemState) (Object) itemState).shadowdrop$setShadowData(
                            shadowdrop$hexColorToRGB(config.shadowColor),
                            alpha,
                            config.shadowXOffset,
                            config.shadowYOffset,
                            checkCrop,
                            needsSlotCheck
                    );
                }
            }
        }

        original.call(instance, itemState);
    }

    @Unique
    private boolean shadowdrop$isInHotbarSlot(ItemStack pItemStack, int y) {
        if (minecraft.player == null) return false;
        for (int i = 0; i < 9; i++) {
            if (minecraft.player.getInventory().getItem(i) == pItemStack) return true;
        }
        return minecraft.player.getOffhandItem() == pItemStack;
    }

    @Unique
    private int shadowdrop$getShadowAlpha(ItemStack stack, ModConfig config) {
        if (shadowdrop$matchesItemOrTag(stack, config.transparentItems)) return 0;
        if (shadowdrop$matchesItemOrTag(stack, config.translucentItems)) return config.shadowAlpha / 2;
        return config.shadowAlpha;
    }

    @Unique
    private boolean shadowdrop$matchesItemOrTag(ItemStack stack, List<String> entries) {
        Identifier itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        for (String entry : entries) {
            if (entry.startsWith("#")) {
                String[] tagParts = entry.substring(1).split(":");
                if (tagParts.length == 2) {
                    TagKey<Item> tag = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(tagParts[0], tagParts[1]));
                    if (stack.is(tag)) return true;
                }
            } else if (itemId.toString().equals(entry)) return true;
        }
        return false;
    }

    @Unique
    private int[] shadowdrop$hexColorToRGB(String hex) {
        if (!hex.startsWith("#") || hex.length() != 7) return new int[]{255, 255, 255};
        try {
            return new int[]{
                    Integer.parseInt(hex.substring(1, 3), 16),
                    Integer.parseInt(hex.substring(3, 5), 16),
                    Integer.parseInt(hex.substring(5, 7), 16)
            };
        } catch (NumberFormatException e) {
            return new int[]{255, 255, 255};
        }
    }
}