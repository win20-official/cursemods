package me.deftware.cursemods.gui;

import lombok.SneakyThrows;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Deftware
 */
public abstract class GuiShell extends Screen {

    protected StringListWidget listWidget;

    protected List<MutableText> description = new ArrayList<>();
    protected final Screen parent;

    protected ButtonWidget backButton;
    protected boolean buttons = true, autoScroll = true;

    public GuiShell(Screen parent, Text title) {
        super(title);
        this.parent = parent;
        loadText();
    }

    protected abstract void loadText();

    @Override
    protected void init() {
        this.listWidget = new StringListWidget(this.client, this.width, this.height, GuiModList.headerSize, this.height - (this.buttons ? 32 : 5), client.textRenderer.fontHeight + 4);
        this.children.add(this.listWidget);
        this.addButtons();
    }

    protected void log(String text, Formatting formatting, Object... args) {
        MutableText mutableText = new LiteralText(
                String.format("[*] " + text, args)
        ).formatted(formatting);
        this.description.add(mutableText);
    }

    protected void addButtons() {
        String text = "<- Back";
        this.backButton = this.addButton(new ButtonWidget(GuiModList.headerOffset, GuiModList.headerOffset, textRenderer.getWidth(text) + 15, 20, new LiteralText(text), (buttonWidget) -> {
            this.onClose();
        }));
    }

    @Override
    public void tick() {
        if (this.listWidget.size() != this.description.size()) {
            for (int i = this.listWidget.size(); i < this.description.size(); i++) {
                this.listWidget.addString(this.description.get(i));
                if (autoScroll)
                    this.listWidget.setScrollAmount(this.listWidget.getMaxScroll());
            }
        }
    }

    @Override
    public void onClose() {
        this.client.openScreen(this.parent);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.listWidget.render(matrices, mouseX, mouseY, delta);
        drawCenteredText(matrices, this.textRenderer, getTitle(), width / 2, GuiModList.headerSize / 2 - this.textRenderer.fontHeight / 2, 0xFFFFFF);
        super.render(matrices, mouseX, mouseY, delta);
    }

    public static class StringListWidget extends AlwaysSelectedEntryListWidget<StringListEntry> {

        public StringListWidget(MinecraftClient minecraftClient, int width, int height, int top, int bottom, int itemHeight) {
            super(minecraftClient,  width, height, top, bottom, itemHeight);
        }

        @Override
        public int getRowWidth() {
            return width - 2;
        }

        @Override
        protected int getScrollbarPositionX() {
            return width - 6;
        }

        public int size() {
            return this.getEntryCount();
        }

        public void clear() {
            this.setScrollAmount(0);
            this.clearEntries();
        }

        public void addString(MutableText text) {
            if (this.client.textRenderer.getWidth(text) > getRowWidth()) {
                for (OrderedText line : this.client.textRenderer.wrapLines(text, getRowWidth() - 50)) {
                    this.addEntry(new StringListEntry(this, line, getRowWidth() - 50));
                }
            } else
                this.addEntry(new StringListEntry(this, text.asOrderedText(), getRowWidth() - 50));
        }

    }

    private static class StringListEntry extends AlwaysSelectedEntryListWidget.Entry<StringListEntry> {

        private final MinecraftClient client = MinecraftClient.getInstance();
        private final TextRenderer textRenderer = client.textRenderer;

        private final OrderedText text;
        private final StringListWidget list;

        @SneakyThrows
        public StringListEntry(StringListWidget list, OrderedText text, int entryWidth) {
           this.text = text;
           this.list = list;
        }

        @Override
        public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            this.textRenderer.drawWithShadow(matrices, text, x, y, 0xFFFFFF);
        }

    }

}
