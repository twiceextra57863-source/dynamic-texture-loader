package com.yourname.dynamictexture;

import com.yourname.dynamictexture.client.KeyBindings;
import com.yourname.dynamictexture.client.gui.ResourcePackScreen;
import com.yourname.dynamictexture.client.renderer.AnimationHandler;
import com.yourname.dynamictexture.client.renderer.CustomItemRenderer;
import com.yourname.dynamictexture.client.renderer.ModelCache;
import com.yourname.dynamictexture.client.renderer.TextureAtlasManager;
import com.yourname.dynamictexture.config.ModConfig;
import com.yourname.dynamictexture.manager.ProfileManager;
import com.yourname.dynamictexture.manager.ResourcePackManager;
import com.yourname.dynamictexture.manager.TextureManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
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
    
    // Renderers
    public static ModelCache modelCache;
    public static CustomItemRenderer customItemRenderer;
    public static AnimationHandler animationHandler;
    public static TextureAtlasManager textureAtlasManager;
    
    @Override
    public void onInitializeClient() {
        LOGGER.info("Dynamic Texture Loader initializing...");
        
        // Initialize managers
        resourcePackManager = new ResourcePackManager();
        textureManager = new TextureManager();
        profileManager = new ProfileManager();
        config = ModConfig.load();
        
        // Initialize renderers
        modelCache = new ModelCache(config.cacheSize);
        customItemRenderer = CustomItemRenderer.getInstance();
        animationHandler = AnimationHandler.getInstance();
        textureAtlasManager = TextureAtlasManager.getInstance();
        
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
        
        // Register resource reload listener
        registerResourceReloadListener();
        
        LOGGER.info("Dynamic Texture Loader initialized successfully!");
    }
    
    /**
     * Register resource reload listener to clear caches
     */
    private void registerResourceReloadListener() {
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(
            new SimpleSynchronousResourceReloadListener() {
                @Override
                public Identifier getFabricId() {
                    return Identifier.of(MOD_ID, "resource_reload");
                }
                
                @Override
                public void reload(ResourceManager manager) {
                    LOGGER.info("Resource packs reloaded, clearing caches...");
                    
                    // Clear all caches
                    modelCache.clear();
                    customItemRenderer.clearCache();
                    animationHandler.clearCache();
                    textureAtlasManager.clearCache();
                    
                    LOGGER.info("Caches cleared successfully");
                }
            }
        );
    }
    
    public static MinecraftClient getClient() {
        return MinecraftClient.getInstance();
    }
}
