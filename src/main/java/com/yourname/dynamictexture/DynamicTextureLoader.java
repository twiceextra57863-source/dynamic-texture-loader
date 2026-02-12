package com.yourname.dynamictexture;

import com.yourname.dynamictexture.client.KeyBindings;
import com.yourname.dynamictexture.client.gui.ResourcePackScreen;
import com.yourname.dynamictexture.config.ModConfig;
import com.yourname.dynamictexture.manager.ProfileManager;
import com.yourname.dynamictexture.manager.ResourcePackManager;
import com.yourname.dynamictexture.manager.TextureManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DynamicTextureLoader implements ClientModInitializer {
    public static final String MOD_ID = "dynamictexture";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    
    // Managers
    public static ResourcePackManager resourcePackManager;
    public static TextureManager textureManager;
    public static ProfileManager profileManager;
    public static ModConfig config;
    
    @Override
    public void onInitializeClient() {
        LOGGER.info("Dynamic Texture Loader initializing...");
        
        // Initialize managers
        resourcePackManager = new ResourcePackManager();
        textureManager = new TextureManager();
        profileManager = new ProfileManager();
        config = ModConfig.load();
        
        // Register keybindings
        KeyBindings.register();
        
        // Register tick event for keybind handling
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (KeyBindings.OPEN_MENU.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new ResourcePackScreen(null));
                }
            }
        });
        
        LOGGER.info("Dynamic Texture Loader initialized successfully!");
    }
    
    public static MinecraftClient getClient() {
        return MinecraftClient.getInstance();
    }
}
