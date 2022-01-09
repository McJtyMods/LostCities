package mcjty.lostcities.gui.elements;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import net.minecraft.client.gui.components.Button.OnPress;

public class ButtonExt extends Button {

    private final Screen parent;
    private Component tooltip = null;

    public ButtonExt(Screen parent, int x, int y, int w, int h, Component message, OnPress action) {
        super(x, y, w, h, message, action);
        this.parent = parent;
    }

    public ButtonExt tooltip(Component tooltip) {
        this.tooltip = tooltip;
        return this;
    }

    @Override
    public void renderToolTip(PoseStack stack, int x, int y) {
        if (tooltip != null) {
            parent.renderTooltip(stack, tooltip, x, y);
        }
    }
}
