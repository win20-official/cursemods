package me.deftware.cursemods.gui;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class ShadowTextField extends TextFieldWidget {

    private final TextRenderer textRenderer;

    public ShadowTextField(TextRenderer textRenderer, int x, int y, int width, int height, Text text) {
        super(textRenderer, x, y, width, height, text);
        this.textRenderer = textRenderer;
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.renderButton(matrices, mouseX, mouseY, delta);
        if (this.isVisible() && !this.isFocused() && this.getText().isEmpty()) {
            // Draw shadow text
            int x = this.x + 4, y =  this.y + (this.height - 8) / 2;
            this.textRenderer.drawWithShadow(matrices, this.getMessage(), x, y, 0xFFFFFF);
        }
    }

}
