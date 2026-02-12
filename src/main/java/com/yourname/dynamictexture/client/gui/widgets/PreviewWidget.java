package com.yourname.dynamictexture.client.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.yourname.dynamictexture.DynamicTextureLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.render.*;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.joml.Quaternionf;

/**
 * Widget to preview textures and models in the GUI
 */
public class PreviewWidget implements Drawable, Element, Selectable {
    private final MinecraftClient client;
    private final int x;
    private final int y;
    private final int width;
    private final int height;
    
    private String namespace = "minecraft";
    private String textureName = null;
    private String modelName = null;
    private ItemStack previewItem = new ItemStack(Items.DIAMOND_SWORD);
    
    private boolean hasError = false;
    private String errorMessage = null;
    
    private float rotation = 0.0f;
    private boolean autoRotate = true;
    
    public PreviewWidget(int x, int y, int width, int height) {
        this.client = MinecraftClient.getInstance();
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    
    /**
     * Set the texture to preview
     */
    public void setTexture(String namespace, String textureName) {
        this.namespace = namespace;
        this.textureName = textureName;
        this.hasError = false;
        this.errorMessage = null;
        validateTexture();
    }
    
    /**
     * Set the model to preview
     */
    public void setModel(String namespace, String modelName) {
        this.namespace = namespace;
        this.modelName = modelName;
        this.hasError = false;
        this.errorMessage = null;
        validateModel();
    }
    
    /**
     * Set the item to use for preview
     */
    public void setPreviewItem(ItemStack item) {
        if (!item.isEmpty()) {
            this.previewItem = item.copy();
        }
    }
    
    /**
     * Enable/disable auto rotation
     */
    public void setAutoRotate(boolean autoRotate) {
        this.autoRotate = autoRotate;
    }
    
    /**
     * Validate if texture exists
     */
    private void validateTexture() {
        if (textureName == null || textureName.isEmpty()) {
            return;
        }
        
        try {
            Identifier textureId = Identifier.of(namespace, "textures/item/" + textureName + ".png");
            
            // Try to get texture from resource manager
            if (client.getResourceManager().getResource(textureId).isEmpty()) {
                hasError = true;
                errorMessage = "Texture not found!";
                DynamicTextureLoader.LOGGER.warn("Texture not found: {}", textureId);
            } else {
                DynamicTextureLoader.LOGGER.info("Texture validated: {}", textureId);
            }
        } catch (Exception e) {
            hasError = true;
            errorMessage = "Invalid texture path!";
            DynamicTextureLoader.LOGGER.error("Error validating texture", e);
        }
    }
    
    /**
     * Validate if model exists
     */
    private void validateModel() {
        if (modelName == null || modelName.isEmpty()) {
            return;
        }
        
        try {
            Identifier modelId = Identifier.of(namespace, "item/" + modelName);
            
            // Try to get model
            // Note: This is a simplified check
            DynamicTextureLoader.LOGGER.info("Model check: {}", modelId);
        } catch (Exception e) {
            hasError = true;
            errorMessage = "Invalid model path!";
            DynamicTextureLoader.LOGGER.error("Error validating model", e);
        }
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Update rotation
        if (autoRotate) {
            rotation += delta * 2.0f;
            if (rotation >= 360.0f) {
                rotation -= 360.0f;
            }
        }
        
        // Draw background
        context.fill(x, y, x + width, y + height, 0xFF1A1A1A);
        
        // Draw border
        int borderColor = hasError ? 0xFFFF5555 : 0xFF444444;
        context.drawBorder(x, y, width, height, borderColor);
        
        // Draw title
        context.drawCenteredTextWithShadow(
            client.textRenderer,
            Text.literal("Preview"),
            x + width / 2,
            y + 5,
            0xFFFFFF
        );
        
        if (hasError) {
            // Draw error message
            renderError(context);
        } else if (textureName != null || modelName != null) {
            // Draw preview
            renderPreview(context, mouseX, mouseY, delta);
        } else {
            // Draw placeholder
            renderPlaceholder(context);
        }
        
        // Draw info text at bottom
        renderInfo(context);
    }
    
    /**
     * Render the actual preview
     */
    private void renderPreview(DrawContext context, int mouseX, int mouseY, float delta) {
        MatrixStack matrices = context.getMatrices();
        matrices.push();
        
        // Center position
        int centerX = x + width / 2;
        int centerY = y + height / 2 + 10;
        
        // Scale and position
        matrices.translate(centerX, centerY, 100.0f);
        matrices.scale(32.0f, -32.0f, 32.0f);
        
        // Rotation
        matrices.multiply(new Quaternionf().rotationXYZ(
            (float) Math.toRadians(20),
            (float) Math.toRadians(rotation),
            0
        ));
        
        // Get item renderer
        ItemRenderer itemRenderer = client.getItemRenderer();
        BakedModel model = itemRenderer.getModel(previewItem, null, null, 0);
        
        // Render item
        try {
            VertexConsumerProvider.Immediate immediate = client.getBufferBuilders().getEntityVertexConsumers();
            
            itemRenderer.renderItem(
                previewItem,
                ModelTransformationMode.GUI,
                false,
                matrices,
                immediate,
                15728880, // Full brightness
                OverlayTexture.DEFAULT_UV,
                model
            );
            
            immediate.draw();
        } catch (Exception e) {
            DynamicTextureLoader.LOGGER.error("Error rendering preview", e);
        }
        
        matrices.pop();
        
        // Draw grid background
        renderGrid(context);
    }
    
    /**
     * Render grid background
     */
    private void renderGrid(DrawContext context) {
        int gridSize = 16;
        int gridColor1 = 0x40FFFFFF;
        int gridColor2 = 0x20FFFFFF;
        
        for (int i = 0; i < width / gridSize; i++) {
            for (int j = 0; j < height / gridSize; j++) {
                int color = ((i + j) % 2 == 0) ? gridColor1 : gridColor2;
                context.fill(
                    x + i * gridSize,
                    y + j * gridSize + 20,
                    x + (i + 1) * gridSize,
                    y + (j + 1) * gridSize + 20,
                    color
                );
            }
        }
    }
    
    /**
     * Render error message
     */
    private void renderError(DrawContext context) {
        // Error icon (X)
        int centerX = x + width / 2;
        int centerY = y + height / 2;
        
        context.drawCenteredTextWithShadow(
            client.textRenderer,
            Text.literal("✗"),
            centerX,
            centerY - 10,
            0xFFFF5555
        );
        
        // Error message
        if (errorMessage != null) {
            context.drawCenteredTextWithShadow(
                client.textRenderer,
                Text.literal(errorMessage),
                centerX,
                centerY + 10,
                0xFFAAAAAA
            );
        }
    }
    
    /**
     * Render placeholder when no texture/model set
     */
    private void renderPlaceholder(DrawContext context) {
        int centerX = x + width / 2;
        int centerY = y + height / 2;
        
        // Placeholder icon
        context.drawCenteredTextWithShadow(
            client.textRenderer,
            Text.literal("?"),
            centerX,
            centerY - 10,
            0xFF888888
        );
        
        // Placeholder text
        context.drawCenteredTextWithShadow(
            client.textRenderer,
            Text.literal("No preview"),
            centerX,
            centerY + 10,
            0xFF666666
        );
    }
    
    /**
     * Render info text
     */
    private void renderInfo(DrawContext context) {
        if (textureName != null || modelName != null) {
            String info = "";
            
            if (textureName != null) {
                info = "Texture: " + namespace + ":" + textureName;
            }
            
            if (modelName != null) {
                if (!info.isEmpty()) info += " | ";
                info += "Model: " + namespace + ":" + modelName;
            }
            
            // Draw with scrolling if too long
            int maxWidth = width - 10;
            String displayInfo = info;
            
            while (client.textRenderer.getWidth(displayInfo) > maxWidth && displayInfo.length() > 3) {
                displayInfo = displayInfo.substring(0, displayInfo.length() - 4) + "...";
            }
            
            context.drawTextWithShadow(
                client.textRenderer,
                Text.literal(displayInfo),
                x + 5,
                y + height - 15,
                0xFFAAAAAA
            );
        }
    }
    
    @Override
    public void setFocused(boolean focused) {
        // Not focusable
    }
    
    @Override
    public boolean isFocused() {
        return false;
    }
    
    @Override
    public SelectionType getType() {
        return SelectionType.NONE;
    }
    
    @Override
    public void appendNarrations(NarrationMessageBuilder builder) {
        builder.put(
            net.minecraft.client.gui.screen.narration.NarrationPart.TITLE,
            Text.literal("Texture Preview Widget")
        );
    }
    
    /**
     * Check if mouse is over widget
     */
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + width && 
               mouseY >= y && mouseY < y + height;
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isMouseOver(mouseX, mouseY)) {
            // Toggle auto-rotate on click
            autoRotate = !autoRotate;
            return true;
        }
        return false;
    }
          }
