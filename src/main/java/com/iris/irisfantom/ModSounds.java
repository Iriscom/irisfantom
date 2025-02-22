package com.iris.irisfantom;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.lang.reflect.Constructor;

public class ModSounds {
    // DeferredRegister для SoundEvent
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, "irisfantom");

    // Регистрация звука "music" через вспомогательный метод createSoundEvent()
    public static final RegistryObject<SoundEvent> MUSIC = SOUND_EVENTS.register("music",
            () -> createSoundEvent(new ResourceLocation("irisfantom", "music")));

    public static void register() {
        SOUND_EVENTS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    // Вспомогательный метод для создания SoundEvent через рефлексию,
    // так как конструктор SoundEvent(ResourceLocation, float, boolean) является приватным.
    private static SoundEvent createSoundEvent(ResourceLocation id) {
        try {
            Constructor<SoundEvent> constructor = SoundEvent.class.getDeclaredConstructor(ResourceLocation.class, float.class, boolean.class);
            constructor.setAccessible(true);
            // Здесь можно задать стандартные значения громкости (1.0F) и isStreaming (false)
            return constructor.newInstance(id, 1.0F, true);
        } catch (Exception e) {
            throw new RuntimeException("Не удалось создать SoundEvent для " + id, e);
        }
    }
}
