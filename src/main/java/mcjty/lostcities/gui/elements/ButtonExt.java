package mcjty.lostcities.gui.elements;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

public class ButtonExt extends Button {

    public ButtonExt(int x, int y, int w, int h, Component message, OnPress action) {
        super(x, y, w, h, message, action, DEFAULT_NARRATION);
    }

    @Override
    protected void renderContents(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderDefaultSprite(graphics);
        renderDefaultLabel(graphics.textRendererForWidget(this, GuiGraphics.HoveredTextEffects.NONE));
    }

    public ButtonExt tooltip(Component tooltip) {
        setTooltip(Tooltip.create(tooltip));
        return this;
    }
}
