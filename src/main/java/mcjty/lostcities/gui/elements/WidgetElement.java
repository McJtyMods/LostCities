package mcjty.lostcities.gui.elements;

import net.minecraft.client.gui.components.AbstractWidget;

public class WidgetElement extends GuiElement {

    private final AbstractWidget widget;

    public WidgetElement(AbstractWidget widget, String page, int x, int y) {
        super(page, x, y);
        this.widget = widget;
    }

    @Override
    public void setBasedOnMode(String mode) {
        widget.visible = mode.equalsIgnoreCase(page);
    }
}
