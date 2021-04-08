package me.deftware.cursemods.api;

import me.deftware.cursemods.curse.CurseForgeAPI;
import me.deftware.cursemods.curse.mod.CurseMod;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.nio.file.Path;

/**
 * @author Deftware
 */
public interface ModPathProvider {

    default Path getModsFolder() {
        return FabricLoader.getInstance().getGameDir().resolve("mods");
    }

    default Path getAssetsDir() {
        return FabricLoader.getInstance().getGameDir().resolve("assets");
    }

    default File getModFile(CurseMod mod) {
        return getModsFolder().resolve(mod.getSlug() + ".jar").toFile();
    }

    default boolean isInstalled(CurseMod curseMod) {
        File file = this.getModFile(curseMod);
        if (file.exists() && file.isFile())
            return true;
        // Maybe it's installed under a different file name
        File modsDir = CurseForgeAPI.PATH_PROVIDER.getModsFolder().toFile();
        for (File mod : modsDir.listFiles())
            if (mod.getName().toLowerCase().contains(curseMod.getSlug()))
                return true;
        return false;
    }

}
