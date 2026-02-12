package com.yourname.dynamictexture.manager;

import com.yourname.dynamictexture.DynamicTextureLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourcePackProfile;

import java.util.ArrayList;
import java.util.List;

public class ResourcePackManager {
    
    public List<String> getActiveResourcePacks() {
        List<String> packs = new ArrayList<>();
        MinecraftClient client = MinecraftClient.getInstance();
        
        for (ResourcePackProfile profile : client.getResourcePackManager().getEnabledProfiles()) {
            packs.add(profile.getName());
        }
        
        DynamicTextureLoader.LOGGER.info("Found {} active resource packs", packs.size());
        return packs;
    }
    
    public ResourcePackProfile getPackByName(String name) {
        MinecraftClient client = MinecraftClient.getInstance();
        
        for (ResourcePackProfile profile : client.getResourcePackManager().getEnabledProfiles()) {
            if (profile.getName().equals(name)) {
                return profile;
            }
        }
        
        return null;
    }
}
