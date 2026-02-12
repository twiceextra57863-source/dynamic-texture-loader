package com.yourname.dynamictexture.client.gui;

import com.yourname.dynamictexture.DynamicTextureLoader;
import com.yourname.dynamictexture.client.gui.widgets.PreviewWidget;
import com.yourname.dynamictexture.config.TextureProfile;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

public class TextureInputScreen extends Screen {
    private final Screen parent;
    private final String selectedPack;
    
    private TextFieldWidget namespaceField;
    private TextFieldWidget textureField;
    private TextFieldWidget modelField;
    private ButtonWidget applyButton;
    private PreviewWidget previewWidget; // ← NEW!
    
    private String statusMessage = null;
    private boolean isError = false;
    
    public TextureInputScreen(Screen parent, String selectedPack) {
        super(Text.literal("Configure Texture"));
        this.parent = parent;
        this.selectedPack = selectedPack;
    }
    
    @Override
    protected void init() {
        int centerX = this.width / 2;
        int formWidth = 300;
        int startY = 70;
        
        // Create preview widget (RIGHT SIDE)
        int previewX = centerX + 20;
        int previewY = startY;
        int previewSize = 200;
        
        this.previewWidget = new PreviewWidget(previewX, previewY, previewSize, previewSize);
        
        // Adjust form to LEFT SIDE
        int formX = centerX - formWidth - 20;
        
        // Namespace field
        this.namespaceField = new TextFieldWidget(
            this.textRenderer,
            formX,
            startY + 20,
            formWidth,
            20,
            Text.literal("minecraft")
        );
        this.namespaceField.setMaxLength(50);
        this.namespaceField.setText("minecraft");
        this.namespaceField.setChangedListener(this::onNamespaceChanged);
        this.addSelectableChild(this.namespaceField);
        
        // Texture field
        this.textureField = new TextFieldWidget(
            this.textRenderer,
            formX,
            startY + 70,
            formWidth,
            20,
            Text.literal("Enter texture name")
        );
        this.textureField.setMaxLength(100);
        this.textureField.setPlaceholder(Text.literal("e.g. custom_sword"));
        this.textureField.setChangedListener(this::onTextureChanged);
        this.addSelectableChild(this.textureField);
        
        // Model field
        this.modelField = new TextFieldWidget(
            this.textRenderer,
            formX,
            startY + 120,
            formWidth,
            20,
            Text.literal("Enter model name")
        );
        this.modelField.setMaxLength(100);
        this.modelField.setPlaceholder(Text.literal("Leave empty for texture only"));
        this.modelField.setChangedListener(this::onModelChanged);
        this.addSelectableChild(this.modelField);
        
        // Apply button
        this.applyButton = ButtonWidget.builder(
            Text.literal("Apply Texture"),
            button -> applyTexture()
        )
        .dimensions(formX, startY + 160, formWidth, 25)
        .build();
        this.addDrawableChild(this.applyButton);
        
        // Save Profile button
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Save Profile"),
            button -> saveProfile()
        )
        .dimensions(formX, startY + 190, (formWidth / 2) - 5, 20)
        .build());
        
        // Cancel button
        this.addDrawableChild(ButtonWidget.builder(
            ScreenTexts.CANCEL,
            button -> this.close()
        )
        .dimensions(formX + (formWidth / 2) + 5, startY + 190, (formWidth / 2) - 5, 20)
        .build());
        
        // Update preview with current held item
        if (this.client.player != null) {
            ItemStack heldItem = this.client.player.getMainHandStack();
            if (!heldItem.isEmpty()) {
                this.previewWidget.setPreviewItem(heldItem);
            }
        }
    }
    
    /**
     * Called when namespace field changes
     */
    private void onNamespaceChanged(String namespace) {
        updatePreview();
    }
    
    /**
     * Called when texture field changes
     */
    private void onTextureChanged(String texture) {
        updatePreview();
    }
    
    /**
     * Called when model field changes
     */
    private void onModelChanged(String model) {
        updatePreview();
    }
    
    /**
     * Update preview widget
     */
    private void updatePreview() {
        String namespace = namespaceField.getText().trim();
        String texture = textureField.getText().trim();
        String model = modelField.getText().trim();
        
        if (!texture.isEmpty()) {
            previewWidget.setTexture(namespace, texture);
        }
        
        if (!model.isEmpty()) {
            previewWidget.setModel(namespace, model);
        }
    }
    
    private void applyTexture() {
        String namespace = namespaceField.getText().trim();
        String texture = textureField.getText().trim();
        String model = modelField.getText().trim();
        
        if (texture.isEmpty()) {
            setStatus("❌ Texture name cannot be empty!", true);
            return;
        }
        
        // Get currently held item
        ItemStack heldItem = this.client.player.getMainHandStack();
        if (heldItem.isEmpty()) {
            setStatus("❌ Hold an item in your hand!", true);
            return;
        }
        
        // Apply texture/model
        boolean success = DynamicTextureLoader.textureManager.applyCustomTexture(
            heldItem,
            selectedPack,
            namespace,
            texture,
            model.isEmpty() ? null : model
        );
        
        if (success) {
            setStatus("✓ Texture applied successfully!", false);
            this.client.player.sendMessage(
                Text.literal("✓ Custom texture applied!"),
                true
            );
        } else {
            setStatus("❌ Failed to load texture/model!", true);
        }
    }
    
    private void saveProfile() {
        String namespace = namespaceField.getText().trim();
        String texture = textureField.getText().trim();
        String model = modelField.getText().trim();
        
        if (texture.isEmpty()) {
            setStatus("❌ Cannot save empty profile!", true);
            return;
        }
        
        TextureProfile profile = new TextureProfile(
            "profile_" + System.currentTimeMillis(),
            selectedPack,
            namespace,
            texture,
            model
        );
        
        DynamicTextureLoader.profileManager.saveProfile(profile);
        setStatus("✓ Profile saved!", false);
    }
    
    private void setStatus(String message, boolean error) {
        this.statusMessage = message;
        this.isError = error;
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Background
        context.fill(0, 0, this.width, this.height, 0xCC000000);
        
        // Header
        context.fill(0, 0, this.width, 30, 0xFF2D2D2D);
        context.drawCenteredTextWithShadow(
            this.textRenderer,
            this.title,
            this.width / 2,
            10,
            0xFFFFFF
        );
        
        // Pack info box
        int centerX = this.width / 2;
        context.fill(centerX - 160, 40, centerX + 160, 60, 0xFF444444);
        context.drawCenteredTextWithShadow(
            this.textRenderer,
            Text.literal("Pack: " + selectedPack),
            centerX,
            47,
            0xFFDD55
        );
        
        // Labels
        int formX = centerX - 320;
        int startY = 70;
        
        context.drawTextWithShadow(
            this.textRenderer,
            Text.literal("Namespace:"),
            formX,
            startY + 8,
            0xFFFFFF
        );
        
        context.drawTextWithShadow(
            this.textRenderer,
            Text.literal("Texture Name:"),
            formX,
            startY + 58,
            0xFFFFFF
        );
        
        context.drawTextWithShadow(
            this.textRenderer,
            Text.literal("Model Name (Optional):"),
            formX,
            startY + 108,
            0xFFFFFF
        );
        
        // Render preview widget
        previewWidget.render(context, mouseX, mouseY, delta);
        
        // Render text fields and buttons
        super.render(context, mouseX, mouseY, delta);
        
        // Preview instructions
        context.drawCenteredTextWithShadow(
            this.textRenderer,
            Text.literal("Click preview to toggle rotation"),
            centerX + 120,
            startY + 215,
            0xFF888888
        );
        
        // Status message
        if (statusMessage != null) {
            int color = isError ? 0xFFFF5555 : 0xFF55FF55;
            context.fill(
                centerX - 160,
                this.height - 70,
                centerX + 160,
                this.height - 50,
                isError ? 0x88FF0000 : 0x8800FF00
            );
            context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal(statusMessage),
                centerX,
                this.height - 62,
                color
            );
        }
        
        // Instructions
        context.drawCenteredTextWithShadow(
            this.textRenderer,
            Text.literal("Hold an item and enter the texture/model name from the resource pack"),
            centerX,
            this.height - 35,
            0xAAAAAA
        );
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Let preview widget handle clicks
        if (previewWidget.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }
    }
