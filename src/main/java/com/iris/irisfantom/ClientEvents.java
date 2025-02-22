package com.iris.irisfantom;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Irisfantom.MODID, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onScreenOpen(ScreenEvent.Opening event) {
        // Проверяем, что сейчас открывается именно стандартный TitleScreen
        if (event.getScreen() instanceof TitleScreen) {
            // Заменяем его на свой кастомный
            event.setNewScreen(new FnafTitleScreen());
            Minecraft.getInstance().getSoundManager().stop();

        }
    }
}