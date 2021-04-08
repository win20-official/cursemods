package me.deftware.cursemods.api;

import me.deftware.cursemods.curse.Categories;
import me.deftware.cursemods.curse.mod.Category;
import me.deftware.cursemods.curse.mod.CurseMod;
import me.deftware.cursemods.curse.mod.GameVersionData;
import net.minecraft.SharedConstants;

/**
 * @author Deftware
 */
public interface ModLoaderProvider {

    default boolean isLoaderSupported(CurseMod mod) {
        for (Category category : mod.getCategories())
            if (String.valueOf(category.getCategoryId()).equalsIgnoreCase(Categories.Fabric.toString()))
                return true;
        return false;
    }

    default boolean isVersionSupported(CurseMod mod) {
        for (GameVersionData data : mod.getGameVersionLatestFiles())
            if (data.getGameVersion().equalsIgnoreCase(SharedConstants.getGameVersion().getName()))
                return true;
        return false;
    }

}
