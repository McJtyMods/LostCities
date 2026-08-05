package mcjty.lostcities.gui.elements;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ButtonExt extends PlainTextButton {

    public ButtonExt(int x, int y, int w, int h, Component message, OnPress action) {
        super(x, y, w, h, message, action, Minecraft.getInstance().font);
    }

    public ButtonExt tooltip(Component tooltip) {
        setTooltip(Tooltip.create(tooltip));
        return this;
    }
}
