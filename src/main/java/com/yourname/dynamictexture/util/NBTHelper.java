package com.yourname.dynamictexture.util;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public class NBTHelper {
    
    public static void setCustomTexture(ItemStack stack, String namespace, String texture, String model) {
        NbtCompound nbt = stack.getOrCreateNbt();
        NbtCompound customData = new NbtCompound();
        customData.putString("namespace", namespace);
        customData.putString("texture", texture);
        if (model != null) {
            customData.putString("model", model);
        }
        nbt.put("DynamicTexture", customData);
    }
    
    public static String getCustomTexture(ItemStack stack) {
        if (!stack.hasNbt()) return null;
        NbtCompound nbt = stack.getNbt();
        if (nbt.contains("DynamicTexture")) {
            return nbt.getCompound("DynamicTexture").getString("texture");
        }
        return null;
    }
}
