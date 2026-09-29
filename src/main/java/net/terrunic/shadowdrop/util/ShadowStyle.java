package net.terrunic.shadowdrop.util;

import net.minecraft.world.item.ItemStack;
import net.terrunic.shadowdrop.ShadowDropConfig;

// Shadow color and opacity from the config, cached while the config is unchanged
public final class ShadowStyle {
    private static ItemMatcher transparentMatcher = null;
    private static ItemMatcher translucentMatcher = null;
    private static int[] cachedShadowColor = {0, 0, 0};
    private static String cachedShadowColorHex = null;

    private ShadowStyle() {
    }

    // If an item is marked as never having a shadow
    public static boolean isTransparent(ItemStack stack) {
        transparentMatcher = ItemMatcher.of(transparentMatcher, ShadowDropConfig.CLIENT.transparentItems);
        return transparentMatcher.matches(stack);
    }

    // Get alpha for shadow of an item stack
    public static int getAlpha(ItemStack stack) {
        int alpha = ShadowDropConfig.CLIENT.shadowAlpha;
        translucentMatcher = ItemMatcher.of(translucentMatcher, ShadowDropConfig.CLIENT.translucentItems);
        if (translucentMatcher.matches(stack)) return alpha / 2;

        return alpha;
    }

    // Get RGB color for item shadows
    public static int[] getColor() {
        String hexColor = ShadowDropConfig.CLIENT.shadowColor;
        if (!hexColor.equals(cachedShadowColorHex)) {
            cachedShadowColorHex = hexColor;
            cachedShadowColor = hexColorToRGB(hexColor);
        }
        return cachedShadowColor;
    }

    // Parse hex color string to RGB array
    private static int[] hexColorToRGB(String hex) {
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
