package mcjty.lostcities.gui.elements;

import mcjty.lostcities.gui.GuiLCConfig;
import mcjty.lostcities.gui.LostCitySetup;
import net.minecraft.client.gui.widget.TextFieldWidget;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class GuiFloatValueElement extends GuiElement {

    private final GuiLCConfig gui;
    private String label = null;
    private String prefix = null;
    private final TextFieldWidget field;
    private final Function<LostCitySetup, String> getter;
    private final BiConsumer<LostCitySetup, String> setter;

    public GuiFloatValueElement(GuiLCConfig gui, String page, int x, int y, Function<LostCitySetup, String> getter, BiConsumer<LostCitySetup, String> setter) {
        super(page, x, y);
        this.gui = gui;
        this.getter = getter;
        this.setter = setter;
        field = new TextFieldWidget(gui.getFont(), x, y, 45, 16, getter.apply(gui.getLocalSetup())) {
            @Override
            public void renderToolTip(int x, int y) {
                if (tooltip != null) {
                    gui.renderTooltip(tooltip, x, y);
                }
            }
        };
        field.setResponder(s -> setter.accept(gui.getLocalSetup(), s));
        gui.addWidget(field);
    }

    public GuiFloatValueElement prefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public GuiFloatValueElement label(String label) {
        this.label = label;
        return this;
    }

    @Override
    public void tick() {
        field.tick();
    }

    @Override
    public void render() {
        if (field.visible) {
            if (label != null) {
                gui.drawString(gui.getFont(), label, 10, y + 5, 0xffffffff);
            }
            if (prefix != null) {
                gui.drawString(gui.getFont(), prefix, x - 8, y + 5, 0xffffffff);
            }
        }
    }

    @Override
    public void update() {
        field.setText(getter.apply(gui.getLocalSetup()));
    }

    @Override
    public void setEnabled(boolean b) {
        field.setEnabled(b);
    }

    @Override
    public void setBasedOnMode(String mode) {
        field.setVisible(page.equalsIgnoreCase(mode));
    }
}
