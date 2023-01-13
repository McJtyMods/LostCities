package mcjty.lostcities.gui.elements;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.function.Supplier;

public class ButtonExt extends Button {

    private final Screen parent;
    private Component tooltip = null;

    public ButtonExt(Screen parent, int x, int y, int w, int h, Component message, OnPress action) {
        super(x, y, w, h, message, action, new CreateNarration() {
            @Override
            public MutableComponent createNarrationMessage(Supplier<MutableComponent> supplier) {
                return Component.empty();
            }
        });
        this.parent = parent;
    }

    public ButtonExt tooltip(Component tooltip) {
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
