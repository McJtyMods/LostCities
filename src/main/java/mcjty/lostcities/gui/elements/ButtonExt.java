package mcjty.lostcities.gui.elements;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ButtonExt extends Button {

    public ButtonExt(int x, int y, int w, int h, Component message, OnPress action) {
        super(x, y, w, h, message, action, supplier -> Component.empty());
    }

    public ButtonExt tooltip(Component tooltip) {
        setTooltip(Tooltip.create(tooltip));
        return this;
    }
}
