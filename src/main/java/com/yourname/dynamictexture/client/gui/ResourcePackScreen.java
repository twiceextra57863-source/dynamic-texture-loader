package com.yourname.dynamictexture.client.gui;

import com.yourname.dynamictexture.DynamicTextureLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class ResourcePackScreen extends Screen {
    private final Screen parent;
    private List<PackButton> packButtons = new ArrayList<>();
    private String selectedPackId = null;
    private ButtonWidget selectButton;
    private TextFieldWidget searchField;
    private int scrollOffset = 0;
    
    public ResourcePackScreen(Screen parent) {
        super(Text.literal("Dynamic Texture Loader"));
        this.parent = parent;
    }
    
    @Override
    protected void init() {
        // Search field
        this.searchField = new TextFieldWidget(
            this.textRenderer,
            this.width / 2 - 100,
            30,
            200,
            20,
            Text.literal("Search packs...")
        );
        this.searchField.setMaxLength(50);
        this.addSelectableChild(this.searchField);
        
        // Load resource packs
        loadResourcePacks();
        
        // Select button
        this.selectButton = ButtonWidget.builder(
            Text.literal("Select →"),
            button -> openInputScreen()
        )
        .dimensions(this.width / 2 - 154, this.height - 35, 150, 20)
        .build();
        this.selectButton.active = false;
        this.addDrawableChild(this.selectButton);
        
        // Cancel button
        this.addDrawableChild(ButtonWidget.builder(
            ScreenTexts.CANCEL,
            button -> this.close()
        )
        .dimensions(this.width / 2 + 4, this.height - 35, 150, 20)
        .build());
    }
    
    private void loadResourcePacks() {
        packButtons.clear();
        List<String> packs = DynamicTextureLoader.resourcePackManager.getActiveResourcePacks();
        
        int y = 60;
        for (String pack : packs) {
            PackButton btn = new PackButton(
                this.width / 2 - 150,
                y,
                300,
                25,
                pack,
                this
            );
            packButtons.add(btn);
            this.addDrawableChild(btn);
            y += 30;
        }
    }
    
    public void setSelectedPack(String packId) {
        this.selectedPackId = packId;
        this.selectButton.active = packId != null;
        
        // Update all buttons
        for (PackButton btn : packButtons) {
            btn.setSelected(btn.packId.equals(packId));
        }
    }
    
    private void openInputScreen() {
        if (selectedPackId != null) {
            this.client.setScreen(new TextureInputScreen(this, selectedPackId));
        }
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Dark background
        context.fill(0, 0, this.width, this.height, 0xCC000000);
        
        // Header bar
        context.fill(0, 0, this.width, 25, 0xFF2D2D2D);
        
        // Title
        context.drawCenteredTextWithShadow(
            this.textRenderer,
            this.title,
            this.width / 2,
            8,
            0xFFFFFF
        );
        
        // Search field
        this.searchField.render(context, mouseX, mouseY, delta);
        
        // Render buttons
        super.render(context, mouseX, mouseY, delta);
        
        // Selected info
        if (selectedPackId != null) {
            context.fill(10, this.height - 60, this.width - 10, this.height - 45, 0x8055FF55);
            context.drawTextWithShadow(
                this.textRenderer,
                Text.literal("✓ Selected: " + selectedPackId),
                15,
                this.height - 55,
                0xFFFFFF
            );
        }
        
        // Instructions
        context.drawCenteredTextWithShadow(
            this.textRenderer,
            Text.literal("Click a resource pack to select it"),
            this.width / 2,
            this.height - 70,
            0xAAAAAA
        );
    }
    
    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }
    
    // Custom button class (NO PNG needed!)
    static class PackButton extends ButtonWidget {
        private final String packId;
        private final ResourcePackScreen parent;
        private boolean selected = false;
        
        public PackButton(int x, int y, int width, int height, String packId, ResourcePackScreen parent) {
            super(x, y, width, height, Text.literal(packId), 
                btn -> parent.setSelectedPack(packId), 
                DEFAULT_NARRATION_SUPPLIER);
            this.packId = packId;
            this.parent = parent;
        }
        
        public void setSelected(boolean selected) {
            this.selected = selected;
        }
        
        @Override
        public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            MinecraftClient client = MinecraftClient.getInstance();
            
            // Background color based on state
            int bgColor;
            if (selected) {
                bgColor = 0xFF44AA44; // Green when selected
            } else if (this.isHovered()) {
                bgColor = 0xFF555555; // Gray when hovered
            } else {
                bgColor = 0xFF333333; // Dark gray default
            }
            
            // Draw background
            context.fill(this.getX(), this.getY(), 
                        this.getX() + this.width, this.getY() + this.height, 
                        bgColor);
            
            // Draw border
            int borderColor = selected ? 0xFF55FF55 : 0xFF888888;
            context.drawBorder(this.getX(), this.getY(), this.width, this.height, borderColor);
            
            // Draw text
            context.drawTextWithShadow(
                client.textRenderer,
                this.getMessage(),
                this.getX() + 5,
                this.getY() + (this.height - 8) / 2,
                0xFFFFFF
            );
            
            // Checkmark if selected
            if (selected) {
                context.drawTextWithShadow(
                    client.textRenderer,
                    Text.literal("✓"),
                    this.getX() + this.width - 15,
                    this.getY() + (this.height - 8) / 2,
                    0x55FF55
                );
            }
        }
    }
              }
