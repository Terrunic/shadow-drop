package com.evandev.shadowdrop;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.List;

// Mod config
public class ShadowDropConfig {
    public static final ModConfigSpec CLIENT_SPEC;
    public static final Client CLIENT;

    static {
        Pair<Client, ModConfigSpec> clientPair = new ModConfigSpec.Builder().configure(Client::new);
        CLIENT = clientPair.getLeft();
        CLIENT_SPEC = clientPair.getRight();
    }

    public static void register(ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, CLIENT_SPEC);
    }

    // Client-side config
    public static class Client {
        // Config values
        public final ModConfigSpec.BooleanValue modEnabled;
        public final ModConfigSpec.ConfigValue<String> shadowColor;
        public final ModConfigSpec.IntValue shadowAlpha;
        public final ModConfigSpec.IntValue shadowXOffset;
        public final ModConfigSpec.IntValue shadowYOffset;
        public final ModConfigSpec.BooleanValue offsetItems;
        public final ModConfigSpec.BooleanValue forceDepthRefresh;
        public final ModConfigSpec.BooleanValue shadowsAlways;
        public final ModConfigSpec.BooleanValue shadowsInSlots;
        public final ModConfigSpec.BooleanValue shadowsInHotbar;
        public final ModConfigSpec.BooleanValue shadowsInCursor;
        public final ModConfigSpec.BooleanValue cropToSlots;
        public final ModConfigSpec.BooleanValue cropToHotbar;
        public final ModConfigSpec.BooleanValue uncropUnderCursor;
        public final ModConfigSpec.ConfigValue<List<? extends String>> slotBrColors;
        public final ModConfigSpec.ConfigValue<List<? extends String>> translucentItems;
        public final ModConfigSpec.ConfigValue<List<? extends String>> transparentItems;

        public Client(ModConfigSpec.Builder builder) {
            builder.comment(" Shadow Drop: Client Configuration")
                    .comment(" (check the mod page for more information on compatibility with other mods)")
                    .comment("")
                    .push("general");

            modEnabled = builder
                    .comment(" Enable/disable the mod entirely")
                    .define("modEnabled", true);

            shadowColor = builder
                    .comment("")
                    .comment(" Shadow color (RGB hex)")
                    .define("shadowColor", "#000000",
                            obj -> obj instanceof String && ((String) obj).matches("^#[0-9A-Fa-f]{6}$"));

            shadowAlpha = builder
                    .comment("")
                    .comment(" Shadow opacity (255 is fully opaque)")
                    .defineInRange("shadowAlpha", 99, 0, 255);

            shadowXOffset = builder
                    .comment("")
                    .comment(" Shadow X offset")
                    .defineInRange("shadowXOffset", 1, 0, 4);

            shadowYOffset = builder
                    .comment("")
                    .comment(" Shadow Y offset")
                    .defineInRange("shadowYOffset", 1, 0, 4);

            offsetItems = builder
                    .comment("")
                    .comment(" Items are offset towards the camera to ensure space behind them for shadows")
                    .comment(" Disable if items are rendering above things they shouldn't")
                    .define("offsetItems", true);

            builder.pop();
            builder.push("shadowContexts");

            shadowsAlways = builder
                    .comment(" Items always have shadows (overrules other shadow contexts when true)")
                    .define("shadowsAlways", true);

            shadowsInSlots = builder
                    .comment("")
                    .comment(" Items in slots (see slot detection config) have shadows")
                    .define("shadowsInSlots", true);

            shadowsInHotbar = builder
                    .comment("")
                    .comment(" Items in hotbar slots always have shadows")
                    .define("shadowsInHotbar", true);

            shadowsInCursor = builder
                    .comment("")
                    .comment(" Items being carried by the cursor have shadows")
                    .define("shadowsInCursor", true);

            builder.pop();
            builder.push("cropContexts");

            cropToSlots = builder
                    .comment(" Shadows of items in slots (see slot detection config) should be cropped")
                    .define("cropToSlots", true);

            cropToHotbar = builder
                    .comment("")
                    .comment(" Shadows of items in the hotbar should always be cropped")
                    .define("cropToHotbar", true);

            uncropUnderCursor = builder
                    .comment("")
                    .comment(" Shadows of items under the cursor should be uncropped")
                    .define("uncropUnderCursor", false);

            builder.pop();
            builder.push("slotDetection");

            slotBrColors = builder
                    .comment(" RGB hex colors for detection of visual slots (e.g. \"#FFFFFF\")")
                    .comment(" This color is searched for in the bottom-right corner pixel wherever an item is rendered")
                    .comment(" Adjust for compatibility with resource packs or modded GUIs where this pixel is not #FFFFFF")
                    .defineList("slotBrColors",
                            List.of("#FFFFFF"),
                            obj -> obj instanceof String && ((String) obj).matches("^#[0-9A-Fa-f]{6}$"));

            builder.pop();
            builder.push("exceptions");

            translucentItems = builder
                    .comment(" Items with half shadow alpha (e.g. \"#c:glass\", \"minecraft:glass\")")
                    .defineList("translucentItems",
                            Arrays.asList("#c:glass", "#c:glass_panes", "#forge:glass", "#forge:glass_panes", "minecraft:beacon"),
                            obj -> obj instanceof String);

            transparentItems = builder
                    .comment("")
                    .comment(" Items with no shadow (e.g. \"#forge:glass\", \"minecraft:glass\")")
                    .defineList("transparentItems",
                            List.of(),
                            obj -> obj instanceof String);

            builder.pop();
            builder.push("experimental");

            forceDepthRefresh = builder
                    .comment(" EXPERIMENTAL: force refresh of depth buffer when rendering shadows")
                    .comment(" Fixes shadow rendering in some places (such as square shadows or shadows overlapping incorrectly)")
                    .comment(" May however break rendering elsewhere")
                    .define("forceDepthRefresh", false);

            builder.pop();
        }
    }
}