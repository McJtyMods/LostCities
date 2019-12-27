package mcjty.lostcities.gui.elements;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;

public class ButtonExt extends Button {

    private final Screen parent;
    private String tooltip = null;

    public ButtonExt(Screen parent, int x, int y, int w, int h, String message, IPressable action) {
        super(x, y, w, h, message, action);
        this.parent = parent;
    }

    public ButtonExt tooltip(String tooltip) {
        this.tooltip = tooltip;
        return this;
    }

    @Override
    public void renderToolTip(int x, int y) {
        if (tooltip != null) {
            parent.renderTooltip(tooltip, x, y);
        }
    }
}
