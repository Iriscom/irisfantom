package com.iris.irisfantom;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class TransparentButton extends Button {
    public TransparentButton(int x, int y, int width, int height, Component message, Button.OnPress onPress) {
        // Третий параметр – лямбда для создания наррации (рассказчика для доступности)
        // Здесь мы возвращаем пустой компонент, чтобы ничего не выводилось
        super(x, y, width, height, message, onPress, (button) -> Component.empty());
    }



    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        // Масштабирование текста
        poseStack.pushPose();
        float scaleFactor = 2.0F;
        poseStack.scale(scaleFactor, scaleFactor, scaleFactor);

        // Отступ для текста
        int padding = 10;
        // Вычисляем координаты для текста в системе координат с масштабированием
        int scaledTextX = (int)((this.getX() + padding) / scaleFactor);
        // Вертикальное выравнивание по центру кнопки
        int scaledTextY = (int)((this.getY() + (this.height - 8) / 2) / scaleFactor);

        // Отображаем стрелку, если курсор наведен на кнопку
        String buttonText = this.getMessage().getString();
        int arrowWidth = Minecraft.getInstance().font.width(">>");

        // Если курсор находится внутри кнопки, отобразим стрелку
        if (this.isHoveredOrFocused()) {
            // Отображаем стрелку перед текстом
            Minecraft.getInstance().font.draw(poseStack, ">>", scaledTextX - arrowWidth - 2, scaledTextY, 0xFFFFFF);
        }

        // Отображаем сам текст кнопки
        Minecraft.getInstance().font.draw(poseStack, buttonText, scaledTextX, scaledTextY, 0xFFFFFF);

        poseStack.popPose();
    }
}