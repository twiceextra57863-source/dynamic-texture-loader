package com.yourname.dynamictexture.client;

import com.yourname.dynamictexture.DynamicTextureLoader;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {
    public static KeyBinding OPEN_MENU;
    
    public static void register() {
        OPEN_MENU = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.dynamictexture.openmenu",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_T,
            "category.dynamictexture"
        ));
        
        DynamicTextureLoader.LOGGER.info("Keybindings registered");
    }
}
