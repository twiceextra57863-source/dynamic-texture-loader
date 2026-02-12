package com.yourname.dynamictexture.client.renderer;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.yourname.dynamictexture.DynamicTextureLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;

import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles animated textures (.mcmeta files)
 */
public class AnimationHandler {
    private static final AnimationHandler INSTANCE = new AnimationHandler();
    private final MinecraftClient client;
    private final Map<String, AnimationData> animationCache = new HashMap<>();
    
    private AnimationHandler() {
        this.client = MinecraftClient.getInstance();
    }
    
    public static AnimationHandler getInstance() {
        return INSTANCE;
    }
    
    /**
     * Get animation data for texture
     */
    public AnimationData getAnimationData(String namespace, String textureName) {
        String key = namespace + ":" + textureName;
        
        // Check cache
        if (animationCache.containsKey(key)) {
            return animationCache.get(key);
        }
        
        // Load .mcmeta file
        AnimationData data = loadAnimationData(namespace, textureName);
        
        if (data != null) {
            animationCache.put(key, data);
        }
        
        return data;
    }
    
    /**
     * Load animation data from .mcmeta file
     */
    private AnimationData loadAnimationData(String namespace, String textureName) {
        try {
            Identifier mcmetaId = Identifier.of(
                namespace,
                "textures/item/" + textureName + ".png.mcmeta"
            );
            
            var resourceOpt = client.getResourceManager().getResource(mcmetaId);
            
            if (resourceOpt.isEmpty()) {
                // No animation data
                return null;
            }
            
            Resource resource = resourceOpt.get();
            
            try (InputStreamReader reader = new InputStreamReader(resource.getInputStream())) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                
                if (json.has("animation")) {
                    JsonObject animation = json.getAsJsonObject("animation");
                    
                    int frametime = animation.has("frametime") ? 
                        animation.get("frametime").getAsInt() : 1;
                    
                    boolean interpolate = animation.has("interpolate") ? 
                        animation.get("interpolate").getAsBoolean() : false;
                    
                    DynamicTextureLoader.LOGGER.info(
                        "Loaded animation data for {}: frametime={}, interpolate={}",
                        textureName, frametime, interpolate
                    );
                    
                    return new AnimationData(frametime, interpolate);
                }
            }
        } catch (Exception e) {
            DynamicTextureLoader.LOGGER.error("Failed to load animation data", e);
        }
        
        return null;
    }
    
    /**
     * Check if texture is animated
     */
    public boolean isAnimated(String namespace, String textureName) {
        return getAnimationData(namespace, textureName) != null;
    }
    
    /**
     * Calculate current frame
     */
    public int getCurrentFrame(AnimationData data, int totalFrames) {
        if (data == null || totalFrames <= 1) {
            return 0;
        }
        
        long time = System.currentTimeMillis();
        long frameTime = data.frametime * 50; // Convert to milliseconds (1 tick = 50ms)
        
        int currentFrame = (int) ((time / frameTime) % totalFrames);
        
        return currentFrame;
    }
    
    /**
     * Clear animation cache
     */
    public void clearCache() {
        animationCache.clear();
        DynamicTextureLoader.LOGGER.info("Animation cache cleared");
    }
    
    /**
     * Animation data class
     */
    public static class AnimationData {
        public final int frametime;
        public final boolean interpolate;
        
        public AnimationData(int frametime, boolean interpolate) {
            this.frametime = frametime;
            this.interpolate = interpolate;
        }
    }
}
