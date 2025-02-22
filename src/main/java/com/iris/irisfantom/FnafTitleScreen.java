package com.iris.irisfantom;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

import java.util.Random;


public class FnafTitleScreen extends Screen {

    // ===== АНИМАЦИЯ ДЛЯ ВСЕХ АНИМАТРОНИКОВ (каждый имеет 4 кадра) =====
    // Структурировано как двумерный массив: ANIMATRONIC_FRAMES[animatronicIndex][frameIndex]
    private static final ResourceLocation[][] ANIMATRONIC_FRAMES = new ResourceLocation[][] {
            { // Freddy (индекс 0)
                    new ResourceLocation("irisfantom", "textures/gui/freddy_frame1.png"),
                    new ResourceLocation("irisfantom", "textures/gui/freddy_frame2.png"),
                    new ResourceLocation("irisfantom", "textures/gui/freddy_frame3.png"),
                    new ResourceLocation("irisfantom", "textures/gui/freddy_frame4.png")
            },
            { // Bonnie (индекс 1)
                    new ResourceLocation("irisfantom", "textures/gui/bonnie_frame1.png"),
                    new ResourceLocation("irisfantom", "textures/gui/bonnie_frame2.png"),
                    new ResourceLocation("irisfantom", "textures/gui/bonnie_frame3.png"),
                    new ResourceLocation("irisfantom", "textures/gui/bonnie_frame4.png")
            },
            { // Chica (индекс 2)
                    new ResourceLocation("irisfantom", "textures/gui/chica_frame1.png"),
                    new ResourceLocation("irisfantom", "textures/gui/chica_frame2.png"),
                    new ResourceLocation("irisfantom", "textures/gui/chica_frame3.png"),
                    new ResourceLocation("irisfantom", "textures/gui/chica_frame4.png")
            },
            { // Foxy (индекс 3)
                    new ResourceLocation("irisfantom", "textures/gui/foxy_frame1.png"),
                    new ResourceLocation("irisfantom", "textures/gui/foxy_frame2.png"),
                    new ResourceLocation("irisfantom", "textures/gui/foxy_frame3.png"),
                    new ResourceLocation("irisfantom", "textures/gui/foxy_frame4.png")
            }
    };

    // Общее количество аниматроников
    private static final int TOTAL_ANIMATRONICS = ANIMATRONIC_FRAMES.length;
    // Текущий выбранный аниматроник (0 = Freddy, 1 = Bonnie, 2 = Chica, 3 = Foxy)
    private int currentBgIndex = 0;

    // ===== АНИМАЦИЯ (кадр/тик) для выбранного аниматроника =====
    private int animFrameIndex = 0;
    private int animFrameTick = 0;
    // Длительность показа одного кадра (10 тиков = 0.5 сек при 20 TPS)
    private static final int FRAME_DURATION = 10;

    // ===== ФАЗЫ СЦЕНАРИЯ (IDLE → TWITCH → WAITING) с рандомными длительностями =====
    private static final int STATE_IDLE = 0;
    private static final int STATE_TWITCH = 1;
    private static final int STATE_WAITING = 2;
    private int state = STATE_IDLE;
    private int stateTimer = 0;
    // Диапазоны длительностей фаз (в тиках; 20 тиков = 1 сек)
    private static final int IDLE_MIN_DURATION    = 150; // ~7.5 сек
    private static final int IDLE_MAX_DURATION    = 250; // ~12.5 сек
    private static final int TWITCH_MIN_DURATION  = 5;   // ~0.25 сек
    private static final int TWITCH_MAX_DURATION  = 15;  // ~0.75 сек
    private static final int WAITING_MIN_DURATION = 250; // ~12.5 сек
    private static final int WAITING_MAX_DURATION = 350; // ~17.5 сек
    private int currentPhaseDuration = 0;

    // Смещение для эффекта подёргивания (расширенный диапазон [-10;10])
    private int twitchOffsetX = 0;
    private int twitchOffsetY = 0;

    private final Random random = new Random();

    // ===== РАЗМЕРЫ ИЗОБРАЖЕНИЙ =====
    // Предполагаем, что все изображения – 1920x1080
    private static final int BG_WIDTH  = 1920;
    private static final int BG_HEIGHT = 1080;

    // ===== АНИМИРОВАННЫЙ ШУМ =====
    // Предположим, что у нас 5 кадров шума: noise_1.png ... noise_5.png
    private int noiseFrame = 0;
    private final int totalNoiseFrames = 5;

    // ===== МУЗЫКА И ПРОЧИЕ ЭЛЕМЕНТЫ =====
    private SimpleSoundInstance musicInstance;
    private static SimpleSoundInstance activeMusicInstance = null;

    public FnafTitleScreen() {
        super(Component.literal("FNaF Title Screen"));
        // Устанавливаем начальную длительность для фазы IDLE (рандомно)
        currentPhaseDuration = random.nextInt(IDLE_MAX_DURATION - IDLE_MIN_DURATION + 1) + IDLE_MIN_DURATION;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        // Останавливаем музыку, если она уже играла
        if (activeMusicInstance != null) {
            Minecraft.getInstance().getSoundManager().stop(activeMusicInstance);
            activeMusicInstance = null;
        }
        // Добавляем кнопки (Games, Settings, Close)
        this.addRenderableWidget(new TransparentButton(
                centerX - 200, centerY + 20, 200, 20,
                Component.literal("Games"),
                button -> Minecraft.getInstance().setScreen(new SelectWorldScreen(this))
        ));
        this.addRenderableWidget(new TransparentButton(
                centerX - 200, centerY + 40, 200, 20,
                Component.literal("Settings"),
                button -> Minecraft.getInstance().setScreen(new OptionsScreen(Minecraft.getInstance().screen, Minecraft.getInstance().options))
        ));
        this.addRenderableWidget(new TransparentButton(
                centerX - 200, centerY + 60, 200, 20,
                Component.literal("Close"),
                button -> Minecraft.getInstance().stop()
        ));
        // Запускаем фоновую музыку (если она зарегистрирована)
        SoundEvent musicSound = ModSounds.MUSIC.get();
        if (musicSound != null) {
            this.musicInstance = SimpleSoundInstance.forAmbientAddition(musicSound);
            activeMusicInstance = this.musicInstance;
            Minecraft.getInstance().getSoundManager().play(this.musicInstance);
        } else {
            System.err.println("SoundEvent irisfantom:music не найден!");
        }
        super.init();
    }

    @Override
    public void tick() {
        super.tick();

        // ===== ОБНОВЛЕНИЕ АНИМАЦИИ ВЫБРАННОГО АНИМАТРОНИКА =====
        animFrameTick++;
        if (animFrameTick >= FRAME_DURATION) {
            animFrameIndex = (animFrameIndex + 1) % ANIMATRONIC_FRAMES[currentBgIndex].length;
            animFrameTick = 0;
        }

        // ===== ОБНОВЛЕНИЕ ФАЗ СЦЕНАРИЯ =====
        stateTimer++;
        if (stateTimer >= currentPhaseDuration) {
            switch (state) {
                case STATE_IDLE:
                    state = STATE_TWITCH;
                    // TWITCH длится от 5 до 15 тиков
                    currentPhaseDuration = random.nextInt(TWITCH_MAX_DURATION - TWITCH_MIN_DURATION + 1) + TWITCH_MIN_DURATION;
                    // Генерируем случайное смещение в диапазоне [-10;10]
                    twitchOffsetX = random.nextInt(21) - 10;
                    twitchOffsetY = random.nextInt(21) - 10;
                    break;
                case STATE_TWITCH:
                    state = STATE_WAITING;
                    // WAITING длится от 250 до 350 тиков
                    currentPhaseDuration = random.nextInt(WAITING_MAX_DURATION - WAITING_MIN_DURATION + 1) + WAITING_MIN_DURATION;
                    break;
                case STATE_WAITING:
                    // Выбираем случайного аниматроника, отличный от текущего
                    int newIndex = currentBgIndex;
                    while (newIndex == currentBgIndex) {
                        newIndex = random.nextInt(TOTAL_ANIMATRONICS);
                    }
                    currentBgIndex = newIndex;
                    // Сбрасываем анимацию для нового аниматроника
                    animFrameIndex = 0;
                    animFrameTick = 0;
                    state = STATE_IDLE;
                    // IDLE длится от 150 до 250 тиков
                    currentPhaseDuration = random.nextInt(IDLE_MAX_DURATION - IDLE_MIN_DURATION + 1) + IDLE_MIN_DURATION;
                    break;
            }
            stateTimer = 0;
        }
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        // Применяем смещение, если находимся в фазе TWITCH
        int offsetX = (state == STATE_TWITCH) ? twitchOffsetX : 0;
        int offsetY = (state == STATE_TWITCH) ? twitchOffsetY : 0;

        // ===== ВЫБОР ТЕКСТУРЫ =====
        ResourceLocation textureToRender = ANIMATRONIC_FRAMES[currentBgIndex][animFrameIndex];
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, textureToRender);
        blit(poseStack,
                offsetX, offsetY,               // Смещение при TWITCH
                this.width, this.height,         // Область на экране
                0, 0, BG_WIDTH, BG_HEIGHT,       // Область текстуры
                BG_WIDTH, BG_HEIGHT);

        // ===== ЭФФЕКТ ИСКАЖЕНИЯ ПРАВОЙ ЧАСТИ ЭКРАНА =====
        int splitX = (int) (this.width * 0.75);
        drawDistortedSection(poseStack, splitX, 0, this.width - splitX, this.height);

        // ===== ТЕКСТ "FIVE NIGHTS AT FREDDY'S" =====
        int textX = 50;
        int textY = this.height / 2 - 120;
        poseStack.pushPose();
        poseStack.scale(2.0F, 2.0F, 2.0F);
        int scaledX = (int) (textX / 2.0F);
        int scaledY = (int) (textY / 2.0F);
        drawString(poseStack, this.font, "Five", scaledX, scaledY, 0xFFFFFF);
        drawString(poseStack, this.font, "Nights", scaledX, scaledY + 12, 0xFFFFFF);
        drawString(poseStack, this.font, "at", scaledX, scaledY + 24, 0xFFFFFF);
        drawString(poseStack, this.font, "Freddy's", scaledX, scaledY + 36, 0xFFFFFF);
        poseStack.popPose();

        // ===== ТЕКСТ ВЕРСИИ И КОПИРАЙТА =====
        String modVersion = "v 1.0.0";
        String creditText = "@irisFantom 2025";
        if (this.font != null) {
            int textWidthCredit = this.font.width(creditText);
            drawString(poseStack, this.font, creditText, this.width - textWidthCredit - 5, this.height - 12, 0xFFFFFF);
            drawString(poseStack, this.font, modVersion, 5, this.height - 12, 0xFFFFFF);
        }

        // Рисуем кнопки и прочие элементы
        super.render(poseStack, mouseX, mouseY, partialTicks);

        // ===== АНИМИРОВАННЫЙ ШУМ =====
        renderAnimatedNoise(poseStack);
    }

    /**
     * Рисует эффект искажения правой части экрана.
     */
    private void drawDistortedSection(PoseStack poseStack, int x, int y, int width, int height) {
        float time = (System.currentTimeMillis() % 1000L) / 1000.0f;
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        for (int i = 0; i < width; i++) {
            float u1 = (float) i / width;
            float u2 = (float) (i + 1) / width;
            float distortion = (float) Math.sin(u1 * Math.PI * 2 + time * 2 * Math.PI) * 5;
            float x1 = x + i + distortion;
            float x2 = x + i + 1 + distortion;
            buffer.vertex(x1, y, 0).uv(u1, 0).endVertex();
            buffer.vertex(x2, y, 0).uv(u2, 0).endVertex();
            buffer.vertex(x2, y + height, 0).uv(u2, 1).endVertex();
            buffer.vertex(x1, y + height, 0).uv(u1, 1).endVertex();
        }
        tessellator.end();
        RenderSystem.disableBlend();
    }

    /**
     * Рисует анимированный шум поверх экрана.
     * Файлы noise_1.png … noise_5.png должны быть в assets/irisfantom/textures/gui/.
     */
    private void renderAnimatedNoise(PoseStack poseStack) {
        String noiseTextureName = "irisfantom:textures/gui/noise_" + (noiseFrame + 1) + ".png";
        ResourceLocation noiseTexture = new ResourceLocation(noiseTextureName);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, noiseTexture);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(770, 771, 1, 0);
        blit(poseStack,
                0, 0, this.width, this.height,
                0, 0, 1920, 720,
                1920, 720);
        RenderSystem.disableBlend();
        noiseFrame = (noiseFrame + 1) % totalNoiseFrames;
    }

    @Override
    public void removed() {
        super.removed();
        if (this.musicInstance != null) {
            Minecraft.getInstance().getSoundManager().stop(this.musicInstance);
            if (activeMusicInstance == this.musicInstance) {
                activeMusicInstance = null;
            }
            this.musicInstance = null;
        }
    }
}
