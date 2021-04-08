package me.deftware.cursemods.gui;

import me.deftware.cursemods.curse.mod.CurseMod;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * @author Deftware
 */
public class GuiDetails extends GuiShell {

    protected final CurseMod mod;

    public GuiDetails(CurseMod mod, Screen parent) {
        super(parent, new LiteralText(mod.getName() + " Description"));
        this.mod = mod;
        this.autoScroll = false;
        this.description.add(new LiteralText("Loading..."));
    }

    @Override
    protected void loadText() {
        CompletableFuture.runAsync(() -> {
            Optional<Document> document = mod.getDescription();
            this.description.clear();
            if (document.isPresent())
                this.loadDescription(document.get().getAllElements());
            else
                this.description.add(new LiteralText("Failed to load description").formatted(Formatting.RED));
        });
    }

    private void loadDescription(Elements elements) {
        for (Element element : elements) {
            String value = element.text();
            switch (element.tag().getName()) {
                case "h1":
                case "h2":
                case "h3":
                    if (!this.description.isEmpty())
                        this.description.add(new LiteralText(""));
                    this.description.add(new LiteralText(value).formatted(Formatting.UNDERLINE, Formatting.GRAY));
                    this.description.add(new LiteralText(""));
                    break;
                case "p":
                    this.description.add(new LiteralText(value));
                    break;
                case "li":
                    this.description.add(new LiteralText("* " + value));
                    break;
                case "ul":
                    this.loadDescription(element.getElementsByTag("li"));
                    break;
            }
        }
    }

    @Override
    protected void addButtons() {
        this.addButton(new ButtonWidget(this.width / 2 - 154, this.height - 28, 150, 20, new LiteralText("Install..."), (buttonWidget) -> {
            this.client.openScreen(new GuiInstall(mod, this.parent));
        }));
        this.addButton(new ButtonWidget(this.width / 2 + 4, this.height - 28, 150, 20, new LiteralText("Back"), (buttonWidget) -> {
            this.onClose();
        }));
    }

}
