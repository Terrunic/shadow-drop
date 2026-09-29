package net.terrunic.shadowdrop.compat;

//? if <26.1 {
import net.terrunic.shadowdrop.render.ShadowRenderType;
import dev.emi.emi.screen.StackBatcher;
import net.minecraft.world.inventory.InventoryMenu;

public class EmiCompat {
    public static void init() {
        StackBatcher.EXTRA_RENDER_LAYERS.add(
                ShadowRenderType.get(InventoryMenu.BLOCK_ATLAS)
        );
    }
}
//?}
