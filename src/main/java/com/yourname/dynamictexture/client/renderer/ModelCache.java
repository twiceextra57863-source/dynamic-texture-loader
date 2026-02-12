package com.yourname.dynamictexture.client.renderer;

import com.yourname.dynamictexture.DynamicTextureLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Cache for baked models to improve performance
 */
public class ModelCache {
    private static final int DEFAULT_CACHE_SIZE = 100;
    private static final int MAX_CACHE_SIZE = 500;
    
    private final LRUCache<String, BakedModel> modelCache;
    private final LRUCache<String, Identifier> textureCache;
    private final MinecraftClient client;
    
    public ModelCache() {
        this(DEFAULT_CACHE_SIZE);
    }
    
    public ModelCache(int cacheSize) {
        int size = Math.min(cacheSize, MAX_CACHE_SIZE);
        this.modelCache = new LRUCache<>(size);
        this.textureCache = new LRUCache<>(size);
        this.client = MinecraftClient.getInstance();
    }
    
    /**
     * Get or load a model
     */
    @Nullable
    public BakedModel getModel(String namespace, String modelName) {
        String key = namespace + ":" + modelName;
        
        // Check cache first
        if (modelCache.containsKey(key)) {
            DynamicTextureLoader.LOGGER.debug("Model cache hit: {}", key);
            return modelCache.get(key);
        }
        
        // Load model
        BakedModel model = loadModel(namespace, modelName);
        
        if (model != null && model != getMissingModel()) {
            modelCache.put(key, model);
            DynamicTextureLoader.LOGGER.info("Model cached: {}", key);
        }
        
        return model;
    }
    
    /**
     * Get or load a texture
     */
    @Nullable
    public Identifier getTexture(String namespace, String textureName) {
        String key = namespace + ":" + textureName;
        
        // Check cache first
        if (textureCache.containsKey(key)) {
            DynamicTextureLoader.LOGGER.debug("Texture cache hit: {}", key);
            return textureCache.get(key);
        }
        
        // Create texture identifier
        Identifier textureId = Identifier.of(
            namespace,
            "textures/item/" + textureName + ".png"
        );
        
        // Verify texture exists
        if (client.getResourceManager().getResource(textureId).isPresent()) {
            textureCache.put(key, textureId);
            DynamicTextureLoader.LOGGER.info("Texture cached: {}", key);
            return textureId;
        } else {
            DynamicTextureLoader.LOGGER.warn("Texture not found: {}", textureId);
            return null;
        }
    }
    
    /**
     * Load model from resource manager
     */
    @Nullable
    private BakedModel loadModel(String namespace, String modelName) {
        try {
            Identifier modelId = Identifier.of(namespace, modelName);
            ModelIdentifier modelIdentifier = new ModelIdentifier(modelId, "inventory");
            
            BakedModel model = client.getBakedModelManager().getModel(modelIdentifier);
            
            if (model != null && model != getMissingModel()) {
                DynamicTextureLoader.LOGGER.debug("Loaded model: {}", modelIdentifier);
                return model;
            } else {
                DynamicTextureLoader.LOGGER.warn("Model not found or missing: {}", modelIdentifier);
                return null;
            }
        } catch (Exception e) {
            DynamicTextureLoader.LOGGER.error("Failed to load model: " + namespace + ":" + modelName, e);
            return null;
        }
    }
    
    /**
     * Get missing model reference
     */
    private BakedModel getMissingModel() {
        return client.getBakedModelManager().getMissingModel();
    }
    
    /**
     * Preload models
     */
    public void preloadModels(Map<String, String> models) {
        DynamicTextureLoader.LOGGER.info("Preloading {} models...", models.size());
        
        int loaded = 0;
        for (Map.Entry<String, String> entry : models.entrySet()) {
            String namespace = entry.getKey();
            String modelName = entry.getValue();
            
            if (getModel(namespace, modelName) != null) {
                loaded++;
            }
        }
        
        DynamicTextureLoader.LOGGER.info("Preloaded {}/{} models", loaded, models.size());
    }
    
    /**
     * Preload textures
     */
    public void preloadTextures(Map<String, String> textures) {
        DynamicTextureLoader.LOGGER.info("Preloading {} textures...", textures.size());
        
        int loaded = 0;
        for (Map.Entry<String, String> entry : textures.entrySet()) {
            String namespace = entry.getKey();
            String textureName = entry.getValue();
            
            if (getTexture(namespace, textureName) != null) {
                loaded++;
            }
        }
        
        DynamicTextureLoader.LOGGER.info("Preloaded {}/{} textures", loaded, textures.size());
    }
    
    /**
     * Clear all caches
     */
    public void clear() {
        int modelCount = modelCache.size();
        int textureCount = textureCache.size();
        
        modelCache.clear();
        textureCache.clear();
        
        DynamicTextureLoader.LOGGER.info("Cleared {} models and {} textures from cache", 
            modelCount, textureCount);
    }
    
    /**
     * Clear model cache only
     */
    public void clearModels() {
        int count = modelCache.size();
        modelCache.clear();
        DynamicTextureLoader.LOGGER.info("Cleared {} models from cache", count);
    }
    
    /**
     * Clear texture cache only
     */
    public void clearTextures() {
        int count = textureCache.size();
        textureCache.clear();
        DynamicTextureLoader.LOGGER.info("Cleared {} textures from cache", count);
    }
    
    /**
     * Get cache statistics
     */
    public CacheStats getStats() {
        return new CacheStats(
            modelCache.size(),
            modelCache.maxSize(),
            textureCache.size(),
            textureCache.maxSize()
        );
    }
    
    /**
     * Cache statistics data class
     */
    public static class CacheStats {
        public final int modelCount;
        public final int modelCapacity;
        public final int textureCount;
        public final int textureCapacity;
        
        public CacheStats(int modelCount, int modelCapacity, int textureCount, int textureCapacity) {
            this.modelCount = modelCount;
            this.modelCapacity = modelCapacity;
            this.textureCount = textureCount;
            this.textureCapacity = textureCapacity;
        }
        
        @Override
        public String toString() {
            return String.format(
                "Models: %d/%d, Textures: %d/%d",
                modelCount, modelCapacity,
                textureCount, textureCapacity
            );
        }
    }
    
    /**
     * LRU Cache implementation
     */
    private static class LRUCache<K, V> extends LinkedHashMap<K, V> {
        private final int maxSize;
        
        public LRUCache(int maxSize) {
            super(16, 0.75f, true); // Access order
            this.maxSize = maxSize;
        }
        
        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() > maxSize;
        }
        
        public int maxSize() {
            return maxSize;
        }
    }
          }
