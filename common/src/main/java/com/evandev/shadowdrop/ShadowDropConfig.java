package com.evandev.shadowdrop;

import com.evandev.shadowdrop.platform.Services;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ShadowDropConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = Services.PLATFORM.getConfigDirectory().resolve("shadowdrop.json").toFile();
    public static Client CLIENT = new Client();

    public static void load() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                CLIENT = GSON.fromJson(reader, Client.class);
                if (CLIENT == null) {
                    CLIENT = new Client();
                }
            } catch (Exception e) {
                CLIENT = new Client();
                save();
            }
        } else {
            CLIENT = new Client();
            save();
        }
    }

    public static void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(CLIENT, writer);
        } catch (IOException e) {
            // Log or ignore
        }
    }

    public static class Client {
        public boolean modEnabled = true;
        public String shadowColor = "#000000";
        public int shadowAlpha = 99;
        public int shadowXOffset = 1;
        public int shadowYOffset = 1;
        public boolean offsetItems = true;
        public boolean shadowsAlways = true;
        public boolean shadowsInSlots = true;
        public boolean shadowsInHotbar = true;
        public boolean shadowsInCursor = true;
        public boolean cropToSlots = true;
        public boolean cropToHotbar = true;
        public boolean uncropUnderCursor = false;
        public List<String> slotBrColors = new ArrayList<>(List.of("#FFFFFF"));
        public List<String> translucentItems = new ArrayList<>(Arrays.asList("#c:glass", "#c:glass_panes", "#forge:glass", "#forge:glass_panes", "minecraft:beacon"));
        public List<String> transparentItems = new ArrayList<>();
        public boolean forceDepthRefresh = false;
    }
}
