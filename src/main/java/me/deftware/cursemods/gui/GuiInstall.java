package me.deftware.cursemods.gui;

import com.google.gson.internal.bind.util.ISO8601Utils;
import me.deftware.cursemods.curse.CurseForgeAPI;
import me.deftware.cursemods.curse.mod.CurseMod;
import me.deftware.cursemods.curse.mod.Dependency;
import me.deftware.cursemods.curse.mod.ModFile;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

import java.io.File;
import java.text.CharacterIterator;
import java.text.ParsePosition;
import java.text.StringCharacterIterator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * @author Deftware
 */
public class GuiInstall extends GuiShell {

    private final Map<Integer, CurseMod> dependencies = new HashMap<>();
    private boolean completed = false, advanced = true;
    private final CurseMod mod;

    public GuiInstall(CurseMod mod, Screen parent) {
        super(parent, new LiteralText("Installing " + mod.getName()));
        this.mod = mod;
        this.buttons = false;
    }

    @Override
    protected void loadText() {
        CompletableFuture.runAsync(() -> {
            long startInstall = System.currentTimeMillis();
            this.log("Installing to %s", Formatting.AQUA, CurseForgeAPI.PATH_PROVIDER.getModsFolder());
            if (FabricLoader.getInstance().isDevelopmentEnvironment())
                this.log("Slug: %s, ID: %s", Formatting.YELLOW, this.mod.getSlug(), this.mod.getId());
            try {
                this.mod.fetchFiles();
                ModFile modFile = this.mod.getOptimalFile()
                        .orElseThrow(() -> new Exception("Unable to find compatible jar, install cancelled"));
                this.log("Resolving dependencies...", Formatting.GOLD);
                this.recursiveDependencySearch(this.mod);
                this.calculateSize(modFile);
                for (CurseMod dependency : dependencies.values())
                    install(dependency, Formatting.GRAY);
                this.install(this.mod, Formatting.AQUA);
                long installTime = (System.currentTimeMillis() - startInstall) / 1000;
                this.log("%s installed successfully in %s seconds", Formatting.GREEN, this.mod.getName(), installTime);
                this.log("You need to restart Minecraft in order for the mod to load", Formatting.GREEN);
            } catch (Exception ex) {
                this.log(ex.getMessage(), Formatting.RED);
                this.cleanup();
            }
            this.backButton.active = this.completed = true;
        });
    }

    @Override
    protected void addButtons() {
        super.addButtons();
        this.backButton.active = completed;
    }

    @Override
    public void onClose() {
        if (completed)
            super.onClose();
    }

    private void calculateSize(ModFile modFile) {
        int installSize = modFile.getFileLength();
        if (!dependencies.isEmpty()) {
            installSize += dependencies.values().stream().mapToInt(
                    d -> d.getOptimalFile().orElseThrow(() -> new RuntimeException("Unable to resolve " + d.getName())).getFileLength()
            ).sum();
        }
        this.log("Total install size %s", Formatting.WHITE, getSize(installSize));
    }

    private void cleanup() {
        this.log("Cleaning up downloaded unused files...", Formatting.GRAY);
        for (CurseMod dependency : this.dependencies.values())
            this.cleanup(dependency);
        this.cleanup(this.mod);
    }

    private void cleanup(CurseMod mod) {
        File file = CurseForgeAPI.PATH_PROVIDER.getModFile(mod);
        if (file.exists() && file.isFile() && !file.delete())
            this.log("Unable to delete %s", Formatting.RED, file.getName());
    }

    private void install(CurseMod dependency, Formatting color) throws Exception {
        this.log("Installing %s", color, dependency.getName());
        ModFile dependencyFile = dependency.getOptimalFile()
                .orElseThrow(() -> new Exception("Could not find dependency jar, this should not happen!"));
        if (CurseForgeAPI.PATH_PROVIDER.isInstalled(dependency)) {
            this.log("- Skipping, already installed", color);
        } else {
            if (advanced)
                this.log("- Found release %s", color, ISO8601Utils.parse(dependencyFile.getFileDate(), new ParsePosition(0)).toString());
            this.log("- Downloading %s (%s)", color, dependencyFile.getFileName(), getSize(dependencyFile.getFileLength()));
            if (!dependencyFile.download(dependency))
                throw new Exception("Unable to download jar");
        }
    }

    private void recursiveDependencySearch(CurseMod mod) throws Exception {
        // Fetch latest downloads
        mod.fetchFiles();
        Optional<ModFile> modFile = mod.getOptimalFile();
        if (modFile.isPresent()) {
            if (mod != this.mod)
                this.dependencies.put(mod.getId(), mod);
            for (Dependency dependency : modFile.get().getDependencies()) {
                // Some mods list themselves as a dependency creating an infinite loop (*cough* sodium *cough*)
                if (dependency.getAddonId() == this.mod.getId())
                    continue;
                // Make sure we haven't indexed this dependency before, as
                // other dependencies might already have declared it
                if (!this.dependencies.containsKey(dependency.getAddonId())) {
                    Optional<CurseMod> dep = dependency.resolve();
                    if (!dep.isPresent())
                        throw new Exception("Failed to resolve dependency " + dependency.getAddonId());
                    CurseMod depMod = dep.get();
                    this.recursiveDependencySearch(depMod);
                }
            }
        } else {
            this.log("Skipping dependency %s", Formatting.YELLOW, mod.getName());
            this.log("- No suitable jar found", Formatting.YELLOW);
        }
    }

    /*
        From https://programming.guide/java/formatting-byte-size-to-human-readable-format.html
     */
    public static String getSize(long bytes) {
        long absB = bytes == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(bytes);
        if (absB < 1024)
            return bytes + " B";
        long value = absB;
        CharacterIterator ci = new StringCharacterIterator("KMGTPE");
        for (int i = 40; i >= 0 && absB > 0xfffccccccccccccL >> i; i -= 10) {
            value >>= 10;
            ci.next();
        }
        value *= Long.signum(bytes);
        return String.format("%.1f %ciB", value / 1024.0, ci.current());
    }

}
