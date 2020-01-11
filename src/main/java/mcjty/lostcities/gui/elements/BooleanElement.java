package mcjty.lostcities.gui.elements;

import mcjty.lostcities.config.Configuration;
import mcjty.lostcities.gui.GuiLCConfig;

public class BooleanElement extends GuiElement {

    private final GuiLCConfig gui;
    private String label = null;
    private final ButtonExt field;
    private final String attribute;

    public BooleanElement(GuiLCConfig gui, String page, int x, int y, String attribute) {
        super(page, x, y);
        this.gui = gui;
        this.attribute = attribute;
        Boolean c = gui.getLocalSetup().get().map(h -> (Boolean) h.toConfiguration().get(attribute)).orElse(false);
        field = new ButtonExt(gui, x, y, 60, 16, c ? "On" : "Off", button -> {
            String message = button.getMessage();
            if ("On".equals(message)) {
                button.setMessage("Off");
            } else {
                button.setMessage("On");
            }
            gui.getLocalSetup().get().ifPresent(profile -> {
                Configuration configuration = profile.toConfiguration();
                configuration.set(attribute, "On".equals(button.getMessage()));
                profile.copyFromConfiguration(configuration);
                gui.refreshPreview();
            });
        }) {
            @Override
            public void renderToolTip(int x, int y) {
                gui.getLocalSetup().get().ifPresent(h -> {
                    gui.renderTooltip(h.toConfiguration().getValue(attribute).getComment(), x, y);
                });
            }
        };
        gui.addWidget(field);
    }

    public BooleanElement label(String label) {
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
        gui.getLocalSetup().get().ifPresent(profile -> {
            Boolean result = profile.toConfiguration().get(attribute);
            field.setMessage(result ? "On" : "Off");
        });
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
