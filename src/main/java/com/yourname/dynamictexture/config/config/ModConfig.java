package com.yourname.dynamictexture.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.yourname.dynamictexture.DynamicTextureLoader;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File(
        FabricLoader.getInstance().getConfigDir().toFile(),
        "dynamictexture.json"
    );
    
    public boolean enableAnimations = true;
    public boolean showPreview = true;
    public int cacheSize = 100;
    
    public static ModConfig load() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                return GSON.fromJson(reader, ModConfig.class);
            } catch (Exception e) {
                DynamicTextureLoader.LOGGER.error("Failed to load config", e);
            }
        }
        ModConfig config = new ModConfig();
        config.save();
        return config;
    }
    
    public void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(this, writer);
        } catch (Exception e) {
            DynamicTextureLoader.LOGGER.error("Failed to save config", e);
        }
    }
}
