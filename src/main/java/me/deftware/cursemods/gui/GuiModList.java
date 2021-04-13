package me.deftware.cursemods.gui;

import com.google.gson.internal.bind.util.ISO8601Utils;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import me.deftware.cursemods.curse.CurseForgeAPI;
import me.deftware.cursemods.curse.mod.CurseMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;

import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author Deftware
 */
@SuppressWarnings("ConstantConditions")
public class GuiModList extends Screen {

    private List<Text> tooltipText;

    private ButtonWidget installButton, descButton;
    private TextFieldWidget searchBox;
    private ModListWidget listWidget;
    private final Screen parent;

    public final static int headerSize = 32, headerOffset = headerSize / 2 - 20 / 2;
    private final CurseForgeAPI api;

    public GuiModList(CurseForgeAPI api, Screen parent) {
        super(new LiteralText("CurseForge Mod List"));
        this.api = api;
        this.parent = parent;
        if (!this.api.isReady())
            this.api.getReadyCallback().add(() -> this.listWidget.update());
    }

    private Runnable runnable;
    private boolean searching = false;
    private long lastSearch = System.currentTimeMillis();

    @Override
    protected void init() {
        int searchWidth = 200;
        this.listWidget = new ModListWidget(this.client, this.width, this.height, headerSize, this.height - 32, 36, api, this);
        this.searchBox = new ShadowTextField(this.textRenderer, width / 2 - searchWidth / 2, headerOffset, searchWidth, 20, new LiteralText("Search...").formatted(Formatting.GRAY));
        this.children.add(this.listWidget);
        this.children.add(this.searchBox);
        this.searchBox.setChangedListener(text -> {
            if (!text.isEmpty()) {
                if (!searching) {
                    this.lastSearch = System.currentTimeMillis();
                    this.listWidget.setFilter(text.toLowerCase());
                    this.listWidget.update();
                    // Search api
                    runnable = () -> {
                        String searchQuery = this.searchBox.getText();
                        if (!searchQuery.isEmpty()) {
                            this.searching = true;
                            this.api.loadSearchQuery(searchQuery);
                            this.listWidget.update();
                            this.searching = false;
                        }
                    };
                }
            } else {
                this.listWidget.setFilter(text.toLowerCase());
                this.listWidget.update();
            }
        });
        // Buttons
        String text = "<- Back";
        this.addButton(new ButtonWidget(headerOffset, headerOffset, textRenderer.getWidth(text) + 15, 20, new LiteralText(text), (buttonWidget) -> {
            this.onClose();
        }));
        this.installButton = this.addButton(new ButtonWidget(this.width / 2 - 154, this.height - 28, 150, 20, new LiteralText("Install..."), (buttonWidget) -> {
            if (this.listWidget.getSelected() != null) {
                CurseMod mod = this.listWidget.getSelected().mod;
                if (!CurseForgeAPI.PATH_PROVIDER.isInstalled(mod))
                    this.client.openScreen(new GuiInstall(mod, this));
            }
        }));
        this.descButton = this.addButton(new ButtonWidget(this.width / 2 + 4, this.height - 28, 150, 20, new LiteralText("Details"), (buttonWidget) -> {
            if (this.listWidget.getSelected() != null) {
                CurseMod mod = this.listWidget.getSelected().mod;
                this.client.openScreen(new GuiDetails(mod, this));
            }
        }));
    }

    @Override
    public void onClose() {
        this.client.openScreen(this.parent);
    }

    @Override
    public void tick() {
        this.searchBox.tick();
        if (lastSearch + 500 < System.currentTimeMillis() && runnable != null) {
            CompletableFuture.runAsync(runnable);
            runnable = null;
        }
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        return this.searchBox.charTyped(chr, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return super.keyPressed(keyCode, scanCode, modifiers) || this.searchBox.keyPressed(keyCode, scanCode, modifiers);
    }

    private final Text searchingText = new LiteralText("Searching...").formatted(Formatting.GRAY);

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.installButton.active = this.listWidget.getSelected() != null && !CurseForgeAPI.PATH_PROVIDER.isInstalled(this.listWidget.getSelected().mod);
        this.descButton.active = this.listWidget.getSelected() != null;
        this.tooltipText = null;
        this.listWidget.render(matrices, mouseX, mouseY, delta);
        this.searchBox.render(matrices, mouseX, mouseY, delta);
        super.render(matrices, mouseX, mouseY, delta);
        if (this.tooltipText != null)
            this.renderTooltip(matrices, this.tooltipText, mouseX, mouseY);
        if (this.searching)
            this.textRenderer.drawWithShadow(matrices, searchingText, width / 2f + searchBox.getWidth() / 2f + 10, headerSize / 2f - textRenderer.fontHeight / 2f, 0xFFFFFF);
    }

    private static class ModListWidget extends AlwaysSelectedEntryListWidget<ModListEntry> {

        @Getter
        private final GuiModList screen;
        private final CurseForgeAPI api;

        @Setter
        private String filter = "";

        public ModListWidget(MinecraftClient minecraftClient, int width, int height, int top, int bottom, int itemHeight, CurseForgeAPI api, GuiModList screen) {
            super(minecraftClient,  width, height, top, bottom, itemHeight);
            this.api = api;
            this.screen = screen;
            if (this.api.isReady())
                this.update();
        }

        @Override
        public int getRowWidth() {
            return width - 2;
        }

        @Override
        protected int getScrollbarPositionX() {
            return width - 6;
        }

        public void update() {
            this.clearEntries();
            this.setScrollAmount(0);
            this.api.stream()
                    .filter(m -> m.search(filter))
                    .forEach(this::addMod);
        }

        public void addMod(CurseMod mod) {
            this.addEntry(new ModListEntry(this, mod, getRowWidth() - 50));
        }

    }

    private static class ModListEntry extends AlwaysSelectedEntryListWidget.Entry<ModListEntry> {

        private final MinecraftClient client = MinecraftClient.getInstance();
        private final TextRenderer textRenderer = client.textRenderer;

        private final ModListWidget list;
        private final CurseMod mod;

        private final List<Text> tooltip = new ArrayList<>();
        private final List<OrderedText> summary;
        private final Text authors;

        private long time;

        @SneakyThrows
        public ModListEntry(ModListWidget list, CurseMod mod, int entryWidth) {
            this.list = list;
            this.mod = mod;
            this.summary = this.textRenderer.wrapLines(new LiteralText(mod.getSummary()), entryWidth);
            // Tooltip
            this.tooltip.add(new LiteralText(
                    "Updated " + ISO8601Utils.parse(mod.getDateModified(), new ParsePosition(0)).toString()
            ));
            this.tooltip.add(new LiteralText(
                    String.format("Downloads %.0f", mod.getDownloadCount())
            ));
            this.tooltip.add(new LiteralText(
                    "Rank " + mod.getGamePopularityRank()
            ));
            this.authors = new LiteralText(mod.getAuthorsString())
                .formatted(Formatting.GRAY, Formatting.ITALIC);
        }

        @Override
        public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            int textureY = y;
            this.textRenderer.draw(matrices, new LiteralText(mod.getName())
                    .formatted(CurseForgeAPI.PATH_PROVIDER.isInstalled(mod) ? Formatting.GREEN : Formatting.GRAY, Formatting.UNDERLINE), x + 37, y += 3, 0xFFFFFF);
            // Authors
            this.textRenderer.draw(matrices, authors, entryWidth - textRenderer.getWidth(authors) - 5, y, 0xFFFFFF);
            y += 2;
            for (int i = 0; i < summary.size(); i++) {
                if (i > 1)
                    break;
                this.textRenderer.draw(matrices, summary.get(i), x + 37, y += textRenderer.fontHeight, 0xFFFFFF);
            }
            if (mod.getTexture() != null) {
                mod.getTexture().bindTexture();
                if (mod.getTexture().isReady())
                    DrawableHelper.drawTexture(matrices, x, textureY, 0.0F, 0.0F, 32, 32, 32, 32);
            }
            if (hovered)
                this.list.getScreen().tooltipText = this.tooltip;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            list.setSelected(this);
            if (Util.getMeasuringTimeMs() - this.time < 250L) {
                Util.getOperatingSystem().open(this.mod.getWebsiteUrl());
            } else {
                this.time = Util.getMeasuringTimeMs();
            }
            return true;
        }

    }

}
