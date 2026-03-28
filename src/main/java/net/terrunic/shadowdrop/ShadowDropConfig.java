package net.terrunic.shadowdrop;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;
import java.util.Arrays;
import java.util.List;

// Mod config
public class ShadowDropConfig
{
    public static final ForgeConfigSpec CLIENT_SPEC;
    public static final Client CLIENT;

    static
    {
        Pair<Client, ForgeConfigSpec> clientPair = new ForgeConfigSpec.Builder().configure(Client::new);
        CLIENT = clientPair.getLeft();
        CLIENT_SPEC = clientPair.getRight();
    }

    public static void register()
    {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CLIENT_SPEC);
    }

    // Client-side config
    public static class Client
    {
        // Config values
        public final ForgeConfigSpec.BooleanValue modEnabled;
        public final ForgeConfigSpec.ConfigValue<String> shadowColor;
        public final ForgeConfigSpec.IntValue shadowAlpha;
        public final ForgeConfigSpec.IntValue shadowXOffset;
        public final ForgeConfigSpec.IntValue shadowYOffset;
        public final ForgeConfigSpec.IntValue shadowZOffset;
        public final ForgeConfigSpec.BooleanValue shadowsAlways;
        public final ForgeConfigSpec.BooleanValue shadowsInSlots;
        public final ForgeConfigSpec.BooleanValue shadowsInHotbar;
        public final ForgeConfigSpec.BooleanValue shadowsInCursor;
        public final ForgeConfigSpec.BooleanValue cropToSlots;
        public final ForgeConfigSpec.BooleanValue cropToHotbar;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> slotBrColors;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> translucentItems;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> transparentItems;

        public Client(ForgeConfigSpec.Builder builder)
        {
            builder.comment(" Shadow Drop: Client Configuration")
                .comment(" (note: for EMI compatibility, set 'use-batched-renderer' to 'false' in EMI config)")
                .comment("")
                .push("general");

            modEnabled = builder
                .comment(" Enable/disable the mod entirely")
                .define("modEnabled", true);

            shadowColor = builder
                .comment("")
                .comment(" Shadow color (RGB hex)")
                .define("shadowColor","#000000",
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

            shadowZOffset = builder
                .comment("")
                .comment(" Shadow Z offset")
                .defineInRange("shadowZOffset", -50, -100, 0);

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
                .comment(" Items with half shadow alpha (e.g. \"#forge:glass\", \"minecraft:glass\")")
                .defineList("translucentItems",
                    Arrays.asList("#forge:glass", "#forge:glass_panes", "minecraft:beacon"),
                    obj -> obj instanceof String);

            transparentItems = builder
                .comment("")
                .comment(" Items with no shadow (e.g. \"#forge:glass\", \"minecraft:glass\")")
                .defineList("transparentItems",
                    List.of(),
                    obj -> obj instanceof String);

            builder.pop();
        }
    }
}