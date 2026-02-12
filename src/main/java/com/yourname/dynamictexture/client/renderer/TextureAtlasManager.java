package com.yourname.dynamictexture.client.renderer;

import com.yourname.dynamictexture.DynamicTextureLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages texture atlas and sprites
 */
public class TextureAtlasManager {
    private static final TextureAtlasManager INSTANCE = new TextureAtlasManager();
    private final MinecraftClient client;
    private final Map<String, Sprite> spriteCache = new HashMap<>();
    
    private TextureAtlasManager() {
        this.client = MinecraftClient.getInstance();
    }
    
    public static TextureAtlasManager getInstance() {
        return INSTANCE;
    }
    
    /**
     * Get sprite for texture
     */
    @Nullable
    public Sprite getSprite(String namespace, String textureName) {
        String key = namespace + ":" + textureName;
        
        // Check cache
        if (spriteCache.containsKey(key)) {
            return spriteCache.get(key);
        }
        
        // Get sprite from atlas
        Sprite sprite = loadSprite(namespace, textureName);
        
        if (sprite != null) {
            spriteCache.put(key, sprite);
            DynamicTextureLoader.LOGGER.debug("Sprite cached: {}", key);
        }
        
        return sprite;
    }
    
    /**
     * Load sprite from texture atlas
     */
    @Nullable
    private Sprite loadSprite(String namespace, String textureName) {
        try {
            Identifier textureId = Identifier.of(namespace, "item/" + textureName);
            
            // Get the block atlas (items use the same atlas)
            SpriteAtlasTexture atlas = client.getSpriteAtlas(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE);
            
            if (atlas != null) {
                Sprite sprite = atlas.getSprite(textureId);
                
                if (sprite != null && sprite != atlas.getSprite(new Identifier("missingno"))) {
                    DynamicTextureLoader.LOGGER.debug("Loaded sprite: {}", textureId);
                    return sprite;
                }
            }
            
            DynamicTextureLoader.LOGGER.warn("Sprite not found: {}", textureId);
        } catch (Exception e) {
            DynamicTextureLoader.LOGGER.error("Failed to load sprite", e);
        }
        
        return null;
    }
    
    /**
     * Register sprite for stitching
     */
    public void registerSprite(Identifier textureId) {
        // TODO: Implement sprite registration for dynamic textures
        DynamicTextureLoader.LOGGER.info("Registering sprite for stitching: {}", textureId);
    }
    
    /**
     * Clear sprite cache
     */
    public void clearCache() {
        int count = spriteCache.size();
        spriteCache.clear();
        DynamicTextureLoader.LOGGER.info("Cleared {} sprites from cache", count);
    }
    
    /**
     * Get cache size
     */
    public int getCacheSize() {
        return spriteCache.size();
    }
}
