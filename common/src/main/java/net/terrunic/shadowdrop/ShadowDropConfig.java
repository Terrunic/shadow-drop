package net.terrunic.shadowdrop;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.terrunic.shadowdrop.platform.Services;

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
            ShadowDrop.LOGGER.error("Failed to save Shadow Drop config!", e);
        }
    }

    public static class Client {
        public boolean modEnabled = true;
        public String shadowColor = "#000000";
        public int shadowAlpha = 99;
        public int shadowXOffset = 1;
        public int shadowYOffset = 1;
        public boolean offsetItems = true;
        public boolean hotbarShadows = true;
        public boolean slotShadows = true;
        public boolean hoverShadows = true;
        public boolean cursorShadows = true;
        public boolean elsewhereShadows = true;
        public boolean hotbarCropped = true;
        public boolean slotCropped = true;
        public boolean hoverCropped = true;
        public List<String> translucentItems = new ArrayList<>(Arrays.asList("#c:glass_blocks", "#c:glass_panes", "#forge:glass", "#forge:glass_panes", "minecraft:beacon"));
        public List<String> transparentItems = new ArrayList<>();
    }
}
