package com.yourname.dynamictexture.config;

public class TextureProfile {
    public String name;
    public String packId;
    public String namespace;
    public String texture;
    public String model;
    
    public TextureProfile(String name, String packId, String namespace, String texture, String model) {
        this.name = name;
        this.packId = packId;
        this.namespace = namespace;
        this.texture = texture;
        this.model = model;
    }
}
