package me.deftware.cursemods.mixin;

import me.deftware.cursemods.curse.CurseForgeAPI;
import me.deftware.cursemods.gui.GuiModList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.SplashScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MinecraftClient.class)
public class MixinMinecraft {

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/SplashScreen;init(Lnet/minecraft/client/MinecraftClient;)V"))
    private void onSplashScreen(MinecraftClient client) {
        client.openScreen(new GuiModList(CurseForgeAPI.INSTANCE, null));
        SplashScreen.init(client);
    }

}
