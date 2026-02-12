package com.yourname.dynamictexture.manager;

import com.yourname.dynamictexture.DynamicTextureLoader;
import com.yourname.dynamictexture.util.NBTHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

public class TextureManager {
    
    public boolean applyCustomTexture(ItemStack stack, String packId, String namespace, String textureName, String modelName) {
        try {
            // Verify texture exists
            Identifier textureId = Identifier.of(namespace, "textures/item/" + textureName + ".png");
            
            // Store in NBT
            NbtCompound nbt = stack.getOrCreateNbt();
            NbtCompound customData = new NbtCompound();
            customData.putString("pack", packId);
            customData.putString("namespace", namespace);
            customData.putString("texture", textureName);
            if (modelName != null && !modelName.isEmpty()) {
                customData.putString("model", modelName);
            }
            
            nbt.put("DynamicTexture", customData);
            
            DynamicTextureLoader.LOGGER.info("Applied texture {} from pack {}", textureName, packId);
            return true;
            
        } catch (Exception e) {
            DynamicTextureLoader.LOGGER.error("Failed to apply texture", e);
            return false;
        }
    }
    
    public CustomTextureData getCustomTexture(ItemStack stack) {
        if (!stack.hasNbt()) {
            return null;
        }
        
        NbtCompound nbt = stack.getNbt();
        if (nbt == null || !nbt.contains("DynamicTexture")) {
            return null;
        }
        
        NbtCompound customData = nbt.getCompound("DynamicTexture");
        return new CustomTextureData(
            customData.getString("pack"),
            customData.getString("namespace"),
            customData.getString("texture"),
            customData.contains("model") ? customData.getString("model") : null
        );
    }
    
    public static class CustomTextureData {
        public final String pack;
        public final String namespace;
        public final String texture;
        public final String model;
        
        public CustomTextureData(String pack, String namespace, String texture, String model) {
            this.pack = pack;
            this.namespace = namespace;
            this.texture = texture;
            this.model = model;
        }
    }
}
