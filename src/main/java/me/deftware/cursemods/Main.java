package me.deftware.cursemods;

import me.deftware.cursemods.curse.CurseForgeAPI;
import me.deftware.cursemods.gui.GuiModList;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.options.KeyBinding;
import org.lwjgl.glfw.GLFW;

/**
 * @author Deftware
 */
public class Main implements ClientModInitializer {

    private KeyBinding keyBinding;

    @Override
    public void onInitializeClient() {
        this.keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.cursemods.open_ui", GLFW.GLFW_KEY_O, "key.categories.misc"));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (this.keyBinding.wasPressed()) {
                client.openScreen(new GuiModList(CurseForgeAPI.INSTANCE, client.currentScreen));
            }
        });
        new Thread(CurseForgeAPI.INSTANCE).start();
    }

}
