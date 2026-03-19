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
        public final ForgeConfigSpec.IntValue shadowAlpha;
        public final ForgeConfigSpec.IntValue shadowXOffset;
        public final ForgeConfigSpec.IntValue shadowYOffset;
        public final ForgeConfigSpec.BooleanValue onlySlotShadows;
        public final ForgeConfigSpec.BooleanValue cropToHotbarSlots;
        public final ForgeConfigSpec.BooleanValue cropToVisualSlots;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> slotBrColors;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> translucentItems;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> transparentItems;

        public Client(ForgeConfigSpec.Builder builder)
        {
            builder.comment(" Shadow Drop: Client Configuration")
                .comment("")
                .push("general");

            modEnabled = builder
                .comment(" Enable/disable the mod entirely")
                .define("modEnabled", true);

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

            builder.pop();
            builder.push("cropping");

            cropToHotbarSlots = builder
                .comment(" Crop shadows to hotbar slots (regardless of hotbar color)?")
                .define("cropToHotbarSlots", true);

            cropToVisualSlots = builder
                .comment("")
                .comment(" Crop shadows to visual slots (determined by slotBrColors)?")
                .define("cropToVisualSlots", true);

            onlySlotShadows = builder
                .comment("")
                .comment(" Only give shadows to items in visual slots (including the hotbar and items picked up by cursor)?")
                .define("onlySlotShadows", false);

            slotBrColors = builder
                .comment("")
                .comment(" RGB hex colors for detection of visual slots (e.g. \"#FFFFFF\")")
                .comment(" This color is searched for in the bottom-right corner pixel wherever an item is rendered")
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