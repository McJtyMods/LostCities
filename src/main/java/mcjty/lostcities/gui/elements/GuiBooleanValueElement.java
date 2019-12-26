package mcjty.lostcities.gui.elements;

import mcjty.lostcities.gui.GuiLCConfig;
import mcjty.lostcities.gui.LostCitySetup;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class GuiBooleanValueElement extends GuiElement {

    private final GuiLCConfig gui;
    private String label = null;
    private final Button field;
    private final Function<LostCitySetup, Boolean> getter;
    private final BiConsumer<LostCitySetup, Boolean> setter;

    public GuiBooleanValueElement(GuiLCConfig gui, String page, int x, int y, Function<LostCitySetup, Boolean> getter, BiConsumer<LostCitySetup, Boolean> setter) {
        super(page, x, y);
        this.gui = gui;
        this.getter = getter;
        this.setter = setter;
        field = new Button(x, y, 60, 16, getter.apply(gui.getLocalSetup()) ? "On" : "Off", button -> {
            String message = button.getMessage();
            if ("On".equals(message)) {
                button.setMessage("Off");
            } else {
                button.setMessage("On");
            }
            setter.accept(gui.getLocalSetup(), "On".equals(message));
        });
        gui.addWidget(field);
    }

    public GuiBooleanValueElement label(String label) {
        this.label = label;
        return this;
    }

    @Override
    public void render() {
        if (label != null) {
            if (field.visible) {
                gui.drawString(gui.getFont(), label, 10, y + 5, 0xffffffff);
            }
        }
    }

    @Override
    public void update() {
        Boolean result = getter.apply(gui.getLocalSetup());
        field.setMessage(result ? "On" : "Off");
    }

    @Override
    public void setEnabled(boolean b) {
        field.active = b;
    }

    @Override
    public void setBasedOnMode(String mode) {
        field.visible = page.equalsIgnoreCase(mode);
    }
}
