package com.evandev.shadowdrop.compat;

import dev.emi.emi.screen.StackBatcher;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.inventory.InventoryMenu;

public class EmiCompat {
    public static void init() {
        StackBatcher.EXTRA_RENDER_LAYERS.add(
                RenderType.entityTranslucentCull(InventoryMenu.BLOCK_ATLAS)
        );
    }
}
