package com.evandev.shadow_drop.config;

import com.evandev.shadow_drop.Constants;
import com.evandev.shadow_drop.platform.Services;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = Services.PLATFORM.getConfigDirectory().resolve("shadow_drop.json").toFile();
    private static ModConfig INSTANCE;

    @SerializedName("enabled")
    public boolean enabled = true;

    @SerializedName("shadow_color")
    public String shadowColor = "#000000";

    @SerializedName("shadow_alpha")
    public int shadowAlpha = 99;

    @SerializedName("shadow_x_offset")
    public int shadowXOffset = 1;

    @SerializedName("shadow_y_offset")
    public int shadowYOffset = 1;

    @SerializedName("shadows_always")
    public boolean shadowsAlways = true;

    @SerializedName("shadows_in_slots")
    public boolean shadowsInSlots = true;

    @SerializedName("shadows_in_hotbar")
    public boolean shadowsInHotbar = true;

    @SerializedName("shadows_in_cursor")
    public boolean shadowsInCursor = true;

    @SerializedName("crop_to_slots")
    public boolean cropToSlots = true;

    @SerializedName("crop_to_hotbar")
    public boolean cropToHotbar = true;

    @SerializedName("translucent_items")
    public List<String> translucentItems = new ArrayList<>(Arrays.asList("#c:glass_blocks", "#c:glass_panes", "minecraft:beacon"));

    @SerializedName("transparent_items")
    public List<String> transparentItems = new ArrayList<>();

    public static ModConfig get() {
        if (INSTANCE == null) {
            load();
        }
        return INSTANCE;
    }

    public static void load() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                INSTANCE = GSON.fromJson(reader, ModConfig.class);
            } catch (Exception e) {
                Constants.LOG.error("Failed to load shadow_drop.json", e);
                INSTANCE = new ModConfig();
                save();
            }
        } else {
            INSTANCE = new ModConfig();
            save();
        }
    }

    public static void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(INSTANCE, writer);
        } catch (IOException e) {
            Constants.LOG.error("Failed to save shadow_drop.json", e);
        }
    }
}