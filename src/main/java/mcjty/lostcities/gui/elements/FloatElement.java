package mcjty.lostcities.gui.elements;

import com.mojang.blaze3d.matrix.MatrixStack;
import mcjty.lostcities.config.Configuration;
import mcjty.lostcities.gui.GuiLCConfig;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.text.StringTextComponent;

public class FloatElement extends GuiElement {

    private final GuiLCConfig gui;
    private String label = null;
    private String prefix = null;
    private final TextFieldWidget field;
    private final String attribute;

    public FloatElement(GuiLCConfig gui, String page, int x, int y, String attribute) {
        super(page, x, y);
        this.gui = gui;
        this.attribute = attribute;
        Float c = gui.getLocalSetup().get().map(h -> (Float) h.toConfiguration().get(attribute)).orElse(0.0f);
        field = new TextFieldWidget(gui.getFont(), x, y, 45, 16, new StringTextComponent(Float.toString(c))) {
            @Override
            public void renderToolTip(MatrixStack stack, int x, int y) {
                    gui.getLocalSetup().get().ifPresent(h -> {
                        gui.renderTooltip(stack, h.toConfiguration().getValue(attribute).getComment(), x, y);
                    });
            }
        };
        field.setResponder(s -> {
            gui.getLocalSetup().get().ifPresent(profile -> {
                Configuration configuration = profile.toConfiguration();

                float value = 0;
                try {
                    value = Float.parseFloat(s);
                } catch (NumberFormatException e) {
                    return;
                }
                Configuration.Value val = configuration.getValue(attribute);
                val.set(value);
                if (val.constrain()) {
                    // It was constraint to min/max. Restore the field
                    setValue(val.get());
                }
                profile.copyFromConfiguration(configuration);
                gui.refreshPreview();
            });
        });
        gui.addWidget(field);
    }

    public FloatElement prefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public FloatElement label(String label) {
        this.label = label;
        return this;
    }

    @Override
    public void tick() {
        field.tick();
    }

    @Override
    public void render(MatrixStack stack) {
        if (field.visible) {
            if (label != null) {
                gui.drawString(stack, gui.getFont(), label, 10, y + 5, 0xffffffff);
            }
            if (prefix != null) {
                gui.drawString(stack, gui.getFont(), prefix, x - 8, y + 5, 0xffffffff);
            }
        }
    }

    @Override
    public void update() {
        gui.getLocalSetup().get().ifPresent(profile -> {
            Object result = profile.toConfiguration().get(attribute);
            setValue(result);
        });
    }

    private void setValue(Object result) {
        if (result instanceof Float) {
            field.setText(Float.toString((Float)result));
        } else if (result instanceof Integer) {
            field.setText(Integer.toString((Integer)result));
        }
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
