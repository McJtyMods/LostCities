package mcjty.lostcities.gui.elements;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;

import net.minecraft.client.gui.widget.button.Button.IPressable;

public class ButtonExt extends Button {

    private final Screen parent;
    private ITextComponent tooltip = null;

    public ButtonExt(Screen parent, int x, int y, int w, int h, ITextComponent message, IPressable action) {
        super(x, y, w, h, message, action);
        this.parent = parent;
    }

    public ButtonExt tooltip(ITextComponent tooltip) {
        this.tooltip = tooltip;
        return this;
    }

    @Override
    public void renderToolTip(MatrixStack stack, int x, int y) {
        if (tooltip != null) {
            parent.renderTooltip(stack, tooltip, x, y);
        }
    }
}
