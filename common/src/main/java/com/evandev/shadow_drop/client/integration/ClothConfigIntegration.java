package com.evandev.shadow_drop.client.integration;

import com.evandev.shadow_drop.config.ModConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.Collections;

public class ClothConfigIntegration {

    public static Screen createScreen(Screen parent) {
        ModConfig config = ModConfig.get();

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("config.shadow_drop.title"));

        builder.setSavingRunnable(ModConfig::save);

        ConfigCategory general = builder.getOrCreateCategory(Component.translatable("config.shadow_drop.category.general"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        general.addEntry(entryBuilder.startBooleanToggle(Component.translatable("config.shadow_drop.option.enabled"), config.enabled)
                .setDefaultValue(true)
                .setSaveConsumer(newValue -> config.enabled = newValue)
                .build());

        general.addEntry(entryBuilder.startColorField(Component.translatable("config.shadow_drop.option.shadow_color"), hexToInt(config.shadowColor))
                .setDefaultValue(hexToInt("#000000"))
                .setSaveConsumer(newValue -> config.shadowColor = intToHex(newValue))
                .build());

        general.addEntry(entryBuilder.startIntSlider(Component.translatable("config.shadow_drop.option.shadow_alpha"), config.shadowAlpha, 0, 255)
                .setDefaultValue(99)
                .setSaveConsumer(newValue -> config.shadowAlpha = newValue)
                .build());

        general.addEntry(entryBuilder.startIntField(Component.translatable("config.shadow_drop.option.shadow_x_offset"), config.shadowXOffset)
                .setDefaultValue(1)
                .setSaveConsumer(newValue -> config.shadowXOffset = newValue)
                .build());

        general.addEntry(entryBuilder.startIntField(Component.translatable("config.shadow_drop.option.shadow_y_offset"), config.shadowYOffset)
                .setDefaultValue(1)
                .setSaveConsumer(newValue -> config.shadowYOffset = newValue)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(Component.translatable("config.shadow_drop.option.shadows_always"), config.shadowsAlways)
                .setDefaultValue(true)
                .setSaveConsumer(newValue -> config.shadowsAlways = newValue)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(Component.translatable("config.shadow_drop.option.shadows_in_slots"), config.shadowsInSlots)
                .setDefaultValue(true)
                .setSaveConsumer(newValue -> config.shadowsInSlots = newValue)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(Component.translatable("config.shadow_drop.option.shadows_in_hotbar"), config.shadowsInHotbar)
                .setDefaultValue(true)
                .setSaveConsumer(newValue -> config.shadowsInHotbar = newValue)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(Component.translatable("config.shadow_drop.option.shadows_in_cursor"), config.shadowsInCursor)
                .setDefaultValue(true)
                .setSaveConsumer(newValue -> config.shadowsInCursor = newValue)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(Component.translatable("config.shadow_drop.option.crop_to_slots"), config.cropToSlots)
                .setDefaultValue(true)
                .setSaveConsumer(newValue -> config.cropToSlots = newValue)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(Component.translatable("config.shadow_drop.option.crop_to_hotbar"), config.cropToHotbar)
                .setDefaultValue(true)
                .setSaveConsumer(newValue -> config.cropToHotbar = newValue)
                .build());

        general.addEntry(entryBuilder.startStrList(Component.translatable("config.shadow_drop.option.translucent_items"), config.translucentItems)
                .setDefaultValue(Arrays.asList("#c:glass_blocks", "#c:glass_panes", "minecraft:beacon"))
                .setSaveConsumer(newValue -> config.translucentItems = newValue)
                .build());

        general.addEntry(entryBuilder.startStrList(Component.translatable("config.shadow_drop.option.transparent_items"), config.transparentItems)
                .setDefaultValue(Collections.emptyList())
                .setSaveConsumer(newValue -> config.transparentItems = newValue)
                .build());

        return builder.build();
    }

    private static int hexToInt(String hex) {
        try {
            return Integer.parseInt(hex.replace("#", ""), 16);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static String intToHex(int color) {
        return String.format("#%06X", (0xFFFFFF & color));
    }
}