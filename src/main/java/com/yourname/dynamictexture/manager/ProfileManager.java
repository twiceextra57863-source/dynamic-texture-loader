package com.yourname.dynamictexture.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.yourname.dynamictexture.DynamicTextureLoader;
import com.yourname.dynamictexture.config.TextureProfile;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class ProfileManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final File profilesDir;
    private final List<TextureProfile> profiles = new ArrayList<>();
    
    public ProfileManager() {
        this.profilesDir = new File(FabricLoader.getInstance().getConfigDir().toFile(), "dynamictexture/profiles");
        if (!profilesDir.exists()) {
            profilesDir.mkdirs();
        }
        loadProfiles();
    }
    
    public void saveProfile(TextureProfile profile) {
        try {
            File file = new File(profilesDir, profile.name + ".json");
            try (FileWriter writer = new FileWriter(file)) {
                GSON.toJson(profile, writer);
            }
            if (!profiles.contains(profile)) {
                profiles.add(profile);
            }
            DynamicTextureLoader.LOGGER.info("Saved profile: {}", profile.name);
        } catch (Exception e) {
            DynamicTextureLoader.LOGGER.error("Failed to save profile", e);
        }
    }
    
    public void loadProfiles() {
        profiles.clear();
        File[] files = profilesDir.listFiles((dir, name) -> name.endsWith(".json"));
        if (files != null) {
            for (File file : files) {
                try (FileReader reader = new FileReader(file)) {
                    TextureProfile profile = GSON.fromJson(reader, TextureProfile.class);
                    profiles.add(profile);
                } catch (Exception e) {
                    DynamicTextureLoader.LOGGER.error("Failed to load profile: {}", file.getName(), e);
                }
            }
        }
        DynamicTextureLoader.LOGGER.info("Loaded {} profiles", profiles.size());
    }
    
    public List<TextureProfile> getProfiles() {
        return new ArrayList<>(profiles);
    }
  }
