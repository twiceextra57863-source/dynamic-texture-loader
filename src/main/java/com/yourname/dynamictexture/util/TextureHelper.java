package com.yourname.dynamictexture.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.yourname.dynamictexture.DynamicTextureLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Helper class for texture operations
 */
public class TextureHelper {
    private static final MinecraftClient client = MinecraftClient.getInstance();
    private static final Map<String, Identifier> registeredTextures = new HashMap<>();
    
    /**
     * Get texture identifier for custom texture
     */
    public static Identifier getTextureIdentifier(String namespace, String textureName) {
        return Identifier.of(namespace, "textures/item/" + textureName + ".png");
    }
    
    /**
     * Check if texture exists in resource pack
     */
    public static boolean textureExists(String namespace, String textureName) {
        Identifier textureId = getTextureIdentifier(namespace, textureName);
        
        try {
            Optional<Resource> resource = client.getResourceManager().getResource(textureId);
            return resource.isPresent();
        } catch (Exception e) {
            DynamicTextureLoader.LOGGER.error("Error checking texture existence: " + textureId, e);
            return false;
        }
    }
    
    /**
     * Load texture as NativeImage
     */
    @Nullable
    public static NativeImage loadTexture(String namespace, String textureName) {
        Identifier textureId = getTextureIdentifier(namespace, textureName);
        
        try {
            Optional<Resource> resourceOpt = client.getResourceManager().getResource(textureId);
            
            if (resourceOpt.isEmpty()) {
                DynamicTextureLoader.LOGGER.warn("Texture not found: {}", textureId);
                return null;
            }
            
            Resource resource = resourceOpt.get();
            
            try (InputStream stream = resource.getInputStream()) {
                NativeImage image = NativeImage.read(stream);
                DynamicTextureLoader.LOGGER.debug("Loaded texture: {}", textureId);
                return image;
            }
        } catch (Exception e) {
            DynamicTextureLoader.LOGGER.error("Failed to load texture: " + textureId, e);
            return null;
        }
    }
    
    /**
     * Register dynamic texture
     */
    public static Identifier registerTexture(String name, NativeImage image) {
        String key = DynamicTextureLoader.MOD_ID + ":dynamic/" + name;
        
        // Check if already registered
        if (registeredTextures.containsKey(key)) {
            DynamicTextureLoader.LOGGER.debug("Texture already registered: {}", key);
            return registeredTextures.get(key);
        }
        
        Identifier textureId = Identifier.of(DynamicTextureLoader.MOD_ID, "dynamic/" + name);
        
        // Register texture with TextureManager
        client.execute(() -> {
            NativeImageBackedTexture texture = new NativeImageBackedTexture(image);
            client.getTextureManager().registerTexture(textureId, texture);
        });
        
        registeredTextures.put(key, textureId);
        DynamicTextureLoader.LOGGER.info("Registered dynamic texture: {}", textureId);
        
        return textureId;
    }
    
    /**
     * Bind texture for rendering
     */
    public static void bindTexture(Identifier textureId) {
        RenderSystem.setShaderTexture(0, textureId);
    }
    
    /**
     * Get texture dimensions
     */
    @Nullable
    public static TextureDimensions getTextureDimensions(String namespace, String textureName) {
        NativeImage image = loadTexture(namespace, textureName);
        
        if (image != null) {
            TextureDimensions dimensions = new TextureDimensions(
                image.getWidth(),
                image.getHeight()
            );
            image.close();
            return dimensions;
        }
        
        return null;
    }
    
    /**
     * Check if texture is animated (has .mcmeta file)
     */
    public static boolean hasAnimationData(String namespace, String textureName) {
        Identifier mcmetaId = Identifier.of(
            namespace,
            "textures/item/" + textureName + ".png.mcmeta"
        );
        
        try {
            Optional<Resource> resource = client.getResourceManager().getResource(mcmetaId);
            return resource.isPresent();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Get animation frame count
     */
    public static int getFrameCount(String namespace, String textureName) {
        TextureDimensions dimensions = getTextureDimensions(namespace, textureName);
        
        if (dimensions == null) {
            return 1;
        }
        
        // For animated textures, height is usually a multiple of width
        if (dimensions.height > dimensions.width && dimensions.height % dimensions.width == 0) {
            return dimensions.height / dimensions.width;
        }
        
        return 1;
    }
    
    /**
     * Create tinted texture
     */
    @Nullable
    public static NativeImage createTintedTexture(NativeImage original, int color) {
        try {
            int width = original.getWidth();
            int height = original.getHeight();
            
            NativeImage tinted = new NativeImage(width, height, false);
            
            float r = ((color >> 16) & 0xFF) / 255.0f;
            float g = ((color >> 8) & 0xFF) / 255.0f;
            float b = (color & 0xFF) / 255.0f;
            
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    int pixel = original.getColor(x, y);
                    
                    int alpha = (pixel >> 24) & 0xFF;
                    int red = (int) (((pixel >> 16) & 0xFF) * r);
                    int green = (int) (((pixel >> 8) & 0xFF) * g);
                    int blue = (int) ((pixel & 0xFF) * b);
                    
                    int newPixel = (alpha << 24) | (red << 16) | (green << 8) | blue;
                    tinted.setColor(x, y, newPixel);
                }
            }
            
            return tinted;
        } catch (Exception e) {
            DynamicTextureLoader.LOGGER.error("Failed to create tinted texture", e);
            return null;
        }
    }
    
    /**
     * Create grayscale texture
     */
    @Nullable
    public static NativeImage createGrayscaleTexture(NativeImage original) {
        try {
            int width = original.getWidth();
            int height = original.getHeight();
            
            NativeImage grayscale = new NativeImage(width, height, false);
            
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    int pixel = original.getColor(x, y);
                    
                    int alpha = (pixel >> 24) & 0xFF;
                    int red = (pixel >> 16) & 0xFF;
                    int green = (pixel >> 8) & 0xFF;
                    int blue = pixel & 0xFF;
                    
                    // Calculate grayscale value
                    int gray = (int) (0.299 * red + 0.587 * green + 0.114 * blue);
                    
                    int newPixel = (alpha << 24) | (gray << 16) | (gray << 8) | gray;
                    grayscale.setColor(x, y, newPixel);
                }
            }
            
            return grayscale;
        } catch (Exception e) {
            DynamicTextureLoader.LOGGER.error("Failed to create grayscale texture", e);
            return null;
        }
    }
    
    /**
     * Resize texture
     */
    @Nullable
    public static NativeImage resizeTexture(NativeImage original, int newWidth, int newHeight) {
        try {
            NativeImage resized = new NativeImage(newWidth, newHeight, false);
            
            float xRatio = (float) original.getWidth() / newWidth;
            float yRatio = (float) original.getHeight() / newHeight;
            
            for (int x = 0; x < newWidth; x++) {
                for (int y = 0; y < newHeight; y++) {
                    int srcX = (int) (x * xRatio);
                    int srcY = (int) (y * yRatio);
                    
                    int pixel = original.getColor(srcX, srcY);
                    resized.setColor(x, y, pixel);
                }
            }
            
            return resized;
        } catch (Exception e) {
            DynamicTextureLoader.LOGGER.error("Failed to resize texture", e);
            return null;
        }
    }
    
    /**
     * Blend two textures
     */
    @Nullable
    public static NativeImage blendTextures(NativeImage texture1, NativeImage texture2, float alpha) {
        try {
            int width = Math.min(texture1.getWidth(), texture2.getWidth());
            int height = Math.min(texture1.getHeight(), texture2.getHeight());
            
            NativeImage blended = new NativeImage(width, height, false);
            
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    int pixel1 = texture1.getColor(x, y);
                    int pixel2 = texture2.getColor(x, y);
                    
                    int a1 = (pixel1 >> 24) & 0xFF;
                    int r1 = (pixel1 >> 16) & 0xFF;
                    int g1 = (pixel1 >> 8) & 0xFF;
                    int b1 = pixel1 & 0xFF;
                    
                    int a2 = (pixel2 >> 24) & 0xFF;
                    int r2 = (pixel2 >> 16) & 0xFF;
                    int g2 = (pixel2 >> 8) & 0xFF;
                    int b2 = pixel2 & 0xFF;
                    
                    int a = (int) (a1 * (1 - alpha) + a2 * alpha);
                    int r = (int) (r1 * (1 - alpha) + r2 * alpha);
                    int g = (int) (g1 * (1 - alpha) + g2 * alpha);
                    int b = (int) (b1 * (1 - alpha) + b2 * alpha);
                    
                    int blendedPixel = (a << 24) | (r << 16) | (g << 8) | b;
                    blended.setColor(x, y, blendedPixel);
                }
            }
            
            return blended;
        } catch (Exception e) {
            DynamicTextureLoader.LOGGER.error("Failed to blend textures", e);
            return null;
        }
    }
    
    /**
     * Extract texture from resource pack
     */
    @Nullable
    public static byte[] extractTextureBytes(String namespace, String textureName) {
        Identifier textureId = getTextureIdentifier(namespace, textureName);
        
        try {
            Optional<Resource> resourceOpt = client.getResourceManager().getResource(textureId);
            
            if (resourceOpt.isEmpty()) {
                return null;
            }
            
            Resource resource = resourceOpt.get();
            
            try (InputStream stream = resource.getInputStream();
                 ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                
                byte[] buffer = new byte[4096];
                int bytesRead;
                
                while ((bytesRead = stream.read(buffer)) != -1) {
                    baos.write(buffer, 0, bytesRead);
                }
                
                return baos.toByteArray();
            }
        } catch (Exception e) {
            DynamicTextureLoader.LOGGER.error("Failed to extract texture bytes: " + textureId, e);
            return null;
        }
    }
    
    /**
     * Convert NativeImage to BufferedImage
     */
    @Nullable
    public static BufferedImage nativeImageToBufferedImage(NativeImage nativeImage) {
        try {
            int width = nativeImage.getWidth();
            int height = nativeImage.getHeight();
            
            BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    int color = nativeImage.getColor(x, y);
                    bufferedImage.setRGB(x, y, color);
                }
            }
            
            return bufferedImage;
        } catch (Exception e) {
            DynamicTextureLoader.LOGGER.error("Failed to convert NativeImage to BufferedImage", e);
            return null;
        }
    }
    
    /**
     * Get average color from texture
     */
    public static int getAverageColor(NativeImage image) {
        long totalR = 0, totalG = 0, totalB = 0, totalA = 0;
        int pixelCount = 0;
        
        int width = image.getWidth();
        int height = image.getHeight();
        
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int pixel = image.getColor(x, y);
                
                int alpha = (pixel >> 24) & 0xFF;
                if (alpha > 0) { // Skip transparent pixels
                    totalA += alpha;
                    totalR += (pixel >> 16) & 0xFF;
                    totalG += (pixel >> 8) & 0xFF;
                    totalB += pixel & 0xFF;
                    pixelCount++;
                }
            }
        }
        
        if (pixelCount == 0) {
            return 0;
        }
        
        int avgA = (int) (totalA / pixelCount);
        int avgR = (int) (totalR / pixelCount);
        int avgG = (int) (totalG / pixelCount);
        int avgB = (int) (totalB / pixelCount);
        
        return (avgA << 24) | (avgR << 16) | (avgG << 8) | avgB;
    }
    
    /**
     * Create thumbnail of texture
     */
    @Nullable
    public static NativeImage createThumbnail(String namespace, String textureName, int size) {
        NativeImage original = loadTexture(namespace, textureName);
        
        if (original == null) {
            return null;
        }
        
        NativeImage thumbnail = resizeTexture(original, size, size);
        original.close();
        
        return thumbnail;
    }
    
    /**
     * Validate texture format
     */
    public static boolean isValidTexture(String namespace, String textureName) {
        NativeImage image = loadTexture(namespace, textureName);
        
        if (image == null) {
            return false;
        }
        
        boolean valid = true;
        
        // Check dimensions are power of 2 (optional, for performance)
        int width = image.getWidth();
        int height = image.getHeight();
        
        if (!isPowerOfTwo(width) || !isPowerOfTwo(height)) {
            DynamicTextureLoader.LOGGER.warn(
                "Texture dimensions are not power of 2: {}x{} ({}:{})",
                width, height, namespace, textureName
            );
            // Not necessarily invalid, just a warning
        }
        
        image.close();
        return valid;
    }
    
    /**
     * Check if number is power of 2
     */
    private static boolean isPowerOfTwo(int n) {
        return n > 0 && (n & (n - 1)) == 0;
    }
    
    /**
     * Clear all registered textures
     */
    public static void clearRegisteredTextures() {
        for (Identifier textureId : registeredTextures.values()) {
            try {
                client.getTextureManager().destroyTexture(textureId);
            } catch (Exception e) {
                DynamicTextureLoader.LOGGER.error("Failed to destroy texture: " + textureId, e);
            }
        }
        
        registeredTextures.clear();
        DynamicTextureLoader.LOGGER.info("Cleared all registered textures");
    }
    
    /**
     * Get texture info string
     */
    public static String getTextureInfo(String namespace, String textureName) {
        TextureDimensions dimensions = getTextureDimensions(namespace, textureName);
        
        if (dimensions == null) {
            return "Texture not found";
        }
        
        boolean animated = hasAnimationData(namespace, textureName);
        int frames = animated ? getFrameCount(namespace, textureName) : 1;
        
        return String.format(
            "%dx%d, %s, %d frame%s",
            dimensions.width,
            dimensions.height,
            animated ? "Animated" : "Static",
            frames,
            frames > 1 ? "s" : ""
        );
    }
    
    /**
     * Texture dimensions data class
     */
    public static class TextureDimensions {
        public final int width;
        public final int height;
        
        public TextureDimensions(int width, int height) {
            this.width = width;
            this.height = height;
        }
        
        public boolean isSquare() {
            return width == height;
        }
        
        public int getArea() {
            return width * height;
        }
        
        @Override
        public String toString() {
            return width + "x" + height;
        }
    }
                     }
