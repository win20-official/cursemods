package me.deftware.cursemods;

import me.deftware.cursemods.curse.CurseForgeAPI;
import net.fabricmc.api.ClientModInitializer;

/**
 * @author Deftware
 */
public class Main implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        new Thread(CurseForgeAPI.INSTANCE).start();
    }

}
