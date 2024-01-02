package mcjty.lostcities.gui.elements;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.function.Supplier;

public class TextExt extends MultiLineTextWidget {

    private final Screen parent;
    private Component tooltip = null;

    public TextExt(Screen parent, int x, int y, int w, int rows, Font font, Component message) {
        super(x, y, message, font);
        this.parent = parent;
        setMaxWidth(w);
        setMaxRows(rows);
    }

    public TextExt tooltip(Component tooltip) {
        this.tooltip = tooltip;
        return this;
    }


    // @todo 1.19.3
//    @Override
//    public void renderToolTip(PoseStack stack, int x, int y) {
//        if (tooltip != null) {
//            parent.renderTooltip(stack, tooltip, x, y);
//        }
//    }
}
