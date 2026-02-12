package com.yourname.dynamictexture.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.yourname.dynamictexture.DynamicTextureLoader;
import com.yourname.dynamictexture.manager.TextureManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Custom item renderer for dynamic textures
 */
public class CustomItemRenderer {
    private static final CustomItemRenderer INSTANCE = new CustomItemRenderer();
    private final MinecraftClient client;
    private final Map<String, BakedModel> customModelCache = new HashMap<>();
    
    private CustomItemRenderer() {
        this.client = MinecraftClient.getInstance();
    }
    
    public static CustomItemRenderer getInstance() {
        return INSTANCE;
    }
    
    /**
     * Render item with custom texture/model
     */
    public void renderCustomItem(
        ItemStack stack,
        ModelTransformationMode renderMode,
        boolean leftHanded,
        MatrixStack matrices,
        VertexConsumerProvider vertexConsumers,
        int light,
        int overlay,
        BakedModel originalModel
    ) {
        // Get custom texture data
        TextureManager.CustomTextureData customData = 
            DynamicTextureLoader.textureManager.getCustomTexture(stack);
        
        if (customData == null) {
            // No custom data, use original rendering
            renderOriginal(stack, renderMode, leftHanded, matrices, vertexConsumers, light, overlay, originalModel);
            return;
        }
        
        // Get or create custom model
        BakedModel customModel = getCustomModel(customData, originalModel);
        
        if (customModel != null && customModel != originalModel) {
            // Render with custom model
            renderWithCustomModel(stack, renderMode, leftHanded, matrices, vertexConsumers, light, overlay, customModel);
        } else {
            // Fallback to texture-only rendering
            renderWithCustomTexture(stack, renderMode, leftHanded, matrices, vertexConsumers, light, overlay, originalModel, customData);
        }
    }
    
    /**
     * Get or load custom model
     */
    @Nullable
    private BakedModel getCustomModel(TextureManager.CustomTextureData data, BakedModel fallback) {
        if (data.model == null || data.model.isEmpty()) {
            return null;
        }
        
        String cacheKey = data.namespace + ":" + data.model;
        
        // Check cache
        if (customModelCache.containsKey(cacheKey)) {
            return customModelCache.get(cacheKey);
        }
        
        try {
            // Try to get model from model manager
            Identifier modelId = Identifier.of(data.namespace, data.model);
            ModelIdentifier modelIdentifier = new ModelIdentifier(modelId, "inventory");
            
            BakedModel model = client.getBakedModelManager().getModel(modelIdentifier);
            
            if (model != null && model != client.getBakedModelManager().getMissingModel()) {
                customModelCache.put(cacheKey, model);
                DynamicTextureLoader.LOGGER.info("Loaded custom model: {}", cacheKey);
                return model;
            } else {
                DynamicTextureLoader.LOGGER.warn("Custom model not found: {}", cacheKey);
                return null;
            }
        } catch (Exception e) {
            DynamicTextureLoader.LOGGER.error("Failed to load custom model: " + cacheKey, e);
            return null;
        }
    }
    
    /**
     * Render with custom model
     */
    private void renderWithCustomModel(
        ItemStack stack,
        ModelTransformationMode renderMode,
        boolean leftHanded,
        MatrixStack matrices,
        VertexConsumerProvider vertexConsumers,
        int light,
        int overlay,
        BakedModel model
    ) {
        ItemRenderer itemRenderer = client.getItemRenderer();
        
        matrices.push();
        
        try {
            // Apply transformations based on render mode
            applyTransformations(matrices, renderMode, leftHanded);
            
            // Render the model
            itemRenderer.renderItem(
                stack,
                renderMode,
                leftHanded,
                matrices,
                vertexConsumers,
                light,
                overlay,
                model
            );
        } catch (Exception e) {
            DynamicTextureLoader.LOGGER.error("Error rendering custom model", e);
        } finally {
            matrices.pop();
        }
    }
    
    /**
     * Render with custom texture only (no model change)
     */
    private void renderWithCustomTexture(
        ItemStack stack,
        ModelTransformationMode renderMode,
        boolean leftHanded,
        MatrixStack matrices,
        VertexConsumerProvider vertexConsumers,
        int light,
        int overlay,
        BakedModel model,
        TextureManager.CustomTextureData data
    ) {
        // Bind custom texture
        Identifier textureId = Identifier.of(
            data.namespace,
            "textures/item/" + data.texture + ".png"
        );
        
        try {
            // Check if texture exists
            if (client.getResourceManager().getResource(textureId).isPresent()) {
                RenderSystem.setShaderTexture(0, textureId);
                DynamicTextureLoader.LOGGER.debug("Rendering with custom texture: {}", textureId);
            } else {
                DynamicTextureLoader.LOGGER.warn("Custom texture not found: {}", textureId);
            }
        } catch (Exception e) {
            DynamicTextureLoader.LOGGER.error("Error binding custom texture", e);
        }
        
        // Render with original model but custom texture
        renderOriginal(stack, renderMode, leftHanded, matrices, vertexConsumers, light, overlay, model);
    }
    
    /**
     * Render with original renderer
     */
    private void renderOriginal(
        ItemStack stack,
        ModelTransformationMode renderMode,
        boolean leftHanded,
        MatrixStack matrices,
        VertexConsumerProvider vertexConsumers,
        int light,
        int overlay,
        BakedModel model
    ) {
        ItemRenderer itemRenderer = client.getItemRenderer();
        
        itemRenderer.renderItem(
            stack,
            renderMode,
            leftHanded,
            matrices,
            vertexConsumers,
            light,
            overlay,
            model
        );
    }
    
    /**
     * Apply transformations based on render mode
     */
    private void applyTransformations(MatrixStack matrices, ModelTransformationMode mode, boolean leftHanded) {
        switch (mode) {
            case FIRST_PERSON_LEFT_HAND:
            case FIRST_PERSON_RIGHT_HAND:
                // First person hand rendering
                matrices.scale(0.75f, 0.75f, 0.75f);
                break;
                
            case THIRD_PERSON_LEFT_HAND:
            case THIRD_PERSON_RIGHT_HAND:
                // Third person hand rendering
                matrices.scale(0.5f, 0.5f, 0.5f);
                break;
                
            case GUI:
                // GUI/inventory rendering
                matrices.scale(1.0f, 1.0f, 1.0f);
                break;
                
            case GROUND:
                // Dropped item rendering
                matrices.scale(0.5f, 0.5f, 0.5f);
                break;
                
            case FIXED:
                // Item frame rendering
                matrices.scale(0.5f, 0.5f, 0.5f);
                break;
                
            case HEAD:
                // Head/helmet rendering
                matrices.scale(1.0f, 1.0f, 1.0f);
                break;
                
            default:
                break;
        }
    }
    
    /**
     * Clear model cache
     */
    public void clearCache() {
        customModelCache.clear();
        DynamicTextureLoader.LOGGER.info("Custom model cache cleared");
    }
    
    /**
     * Get cache size
     */
    public int getCacheSize() {
        return customModelCache.size();
    }
    
    /**
     * Check if item has custom rendering
     */
    public boolean hasCustomRendering(ItemStack stack) {
        return DynamicTextureLoader.textureManager.getCustomTexture(stack) != null;
    }
                                                                    }
