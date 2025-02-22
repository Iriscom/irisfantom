package com.iris.irisfantom;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

import java.util.Timer;
import java.util.TimerTask;

public class MusicController {

    // Метод для воспроизведения музыки с ограничением времени
    public static void playMusicWithDuration(SoundEvent soundEvent, int durationSeconds) {
        Minecraft mc = Minecraft.getInstance();

        // Запуск музыки
        SimpleSoundInstance sound = SimpleSoundInstance.forMusic(soundEvent);
        mc.getSoundManager().play(sound);

        // Остановка через durationSeconds
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                mc.getSoundManager().stop(sound);
            }
        }, durationSeconds * 1000L);
    }
}
