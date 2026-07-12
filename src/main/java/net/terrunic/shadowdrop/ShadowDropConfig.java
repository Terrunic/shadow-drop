package net.terrunic.shadowdrop;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;
import java.util.Arrays;
import java.util.List;

// Mod config
public class ShadowDropConfig
{
    public static final ForgeConfigSpec SPEC;
    public static final ShadowDropConfig INSTANCE;

    static {
        Pair<ShadowDropConfig, ForgeConfigSpec> pair = new ForgeConfigSpec.Builder().configure(ShadowDropConfig::new);
        INSTANCE = pair.getLeft();
        SPEC = pair.getRight();
    }

    // Config values
    public final ForgeConfigSpec.BooleanValue hotbarEnabled;
    public final ForgeConfigSpec.BooleanValue hotbarCropped;
    public final ForgeConfigSpec.BooleanValue slotEnabled;
    public final ForgeConfigSpec.BooleanValue slotCropped;
    public final ForgeConfigSpec.BooleanValue hoverEnabled;
    public final ForgeConfigSpec.BooleanValue hoverCropped;
    public final ForgeConfigSpec.BooleanValue cursorEnabled;
    public final ForgeConfigSpec.BooleanValue outsideEnabled;
    public final ForgeConfigSpec.BooleanValue elsewhereEnabled;

    public final ForgeConfigSpec.ConfigValue<String> shadowColor;
    public final ForgeConfigSpec.IntValue shadowAlpha;
    public final ForgeConfigSpec.IntValue shadowOffsetX;
    public final ForgeConfigSpec.IntValue shadowOffsetY;

    public final ForgeConfigSpec.ConfigValue<List<? extends String>> slotBrColors;

    public final ForgeConfigSpec.ConfigValue<List<? extends String>> translucentItems;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> transparentItems;

    // Advanced
    public final ForgeConfigSpec.BooleanValue forceDepthRefresh;

    // Builder
    private ShadowDropConfig(ForgeConfigSpec.Builder builder)
    {
        builder.comment(" Shadow Drop: Configuration",
                " (check the mod page for more information on compatibility with other mods)",
                "")
            .push("contexts");

        hotbarEnabled = builder
            .comment(" Items in hotbar have shadows")
            .define("hotbarEnabled", true);
        hotbarCropped = builder
            .comment(" Hotbar shadows are cropped")
            .define("hotbarCropped", true);
        slotEnabled = builder
            .comment(" Items in slots have shadows")
            .define("slotEnabled", true);
        slotCropped = builder
            .comment(" Slot shadows are cropped")
            .define("slotCropped", true);
        hoverEnabled = builder
            .comment(" Items in slots being hovered over have shadows")
            .define("hoverEnabled", true);
        hoverCropped = builder
            .comment(" Items in slots being hovered over are cropped")
            .define("hoverCropped", true);
        cursorEnabled = builder
            .comment(" Items carried by the cursor have shadows")
            .define("cursorEnabled", true);
        outsideEnabled = builder
            .comment(" Items in GUIs with slots have shadows")
            .define("outsideEnabled", false);
        elsewhereEnabled = builder
            .comment(" Items in GUIs without slots have shadows")
            .define("elsewhereEnabled", true);

        builder.pop();
        builder.push("appearance");

        shadowColor = builder
            .comment(" Shadow color (RGB hex)")
            .define("shadowColor", "#000000");
        shadowAlpha = builder
            .comment(" Shadow opacity (percentage, where 100% is fully opaque, and 0% disables the mod")
            .defineInRange("shadowAlpha", 39, 0, 100);
        shadowOffsetX = builder
            .comment(" Shadow X offset")
            .defineInRange("shadowOffsetX", 1, -4, 4);
        shadowOffsetY = builder
            .comment(" Shadow Y offset")
            .defineInRange("shadowOffsetY", 1, -4, 4);

        builder.pop();
        builder.push("slotDetection");

        slotBrColors = builder
            .comment(" List of RGB hex colours for detection of visual slots",
                " This colour is searched for in the bottom-right corner pixel wherever an item is rendered",
                " Adjust for compatibility with resource packs or modded GUIs where this pixel is not #FFFFFF")
            .defineList("slotBrColors",
                List.of("#FFFFFF"), obj -> obj instanceof String);

        builder.pop();
        builder.push("exceptions");

        translucentItems = builder
            .comment(" List of items with half shadow alpha (e.g. \"#forge:glass\", \"minecraft:glass\")")
            .defineListAllowEmpty("translucentItems",
                Arrays.asList("#forge:glass", "#forge:glass_panes", "minecraft:beacon"), e -> e instanceof String);
        transparentItems = builder
            .comment(" List of items with no shadow (e.g. \"#forge:glass\", \"minecraft:glass\")")
            .defineListAllowEmpty("transparentItems",
                List.of(), e -> e instanceof String);

        builder.pop();
        builder.push("advanced");

        forceDepthRefresh = builder
            .comment(" Force refresh of the depth buffer when rendering shadows",
                " Fixes shadow rendering in some places (such as square shadows or shadows overlapping incorrectly)",
                " May break rendering elsewhere!")
            .define("forceDepthRefresh", false);

        builder.pop();
    }
}