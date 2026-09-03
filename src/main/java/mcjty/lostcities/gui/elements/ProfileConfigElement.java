package mcjty.lostcities.gui.elements;

import mcjty.lostcities.config.Configuration;
import mcjty.lostcities.gui.GuiLCConfig;
import mcjty.lostcities.varia.ComponentFactory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;

import java.util.ArrayList;
import java.util.List;

/**
 * A scrollable, data-driven editor for every value defined by a Lost Cities profile.
 */
public class ProfileConfigElement extends GuiElement {

    private static final int TOP = 56;
    private static final int BOTTOM_MARGIN = 28;
    private static final int ROW_HEIGHT = 20;

    private final GuiLCConfig gui;
    private final int width;
    private final int bottom;
    private final int editorX;
    private final List<Row> rows = new ArrayList<>();
    private List<String> attributes = List.of();
    private int scrollOffset;
    private boolean enabled;
    private boolean visible;

    public ProfileConfigElement(GuiLCConfig gui, String page, int width, int height) {
        super(page, 0, TOP);
        this.gui = gui;
        this.width = width;
        this.bottom = height - BOTTOM_MARGIN;
        this.editorX = Math.max(170, width / 2);

        int visibleRows = Math.max(1, (bottom - TOP) / ROW_HEIGHT);
        int editorWidth = Math.max(80, width - editorX - 18);
        for (int i = 0; i < visibleRows; i++) {
            rows.add(new Row(TOP + i * ROW_HEIGHT, editorWidth));
        }
        update();
    }

    @Override
    public void render(GuiGraphics graphics) {
        if (!visible) {
            return;
        }

        int first = attributes.isEmpty() ? 0 : scrollOffset + 1;
        int last = Math.min(attributes.size(), scrollOffset + rows.size());
        graphics.drawString(gui.getFont(), "All profile settings (" + first + "-" + last + " of " + attributes.size() + ") - scroll to browse",
                10, 43, 0xffaaaaaa);

        int labelWidth = editorX - 18;
        for (Row row : rows) {
            if (row.attribute != null) {
                graphics.drawString(gui.getFont(), fit(row.attribute, labelWidth), 10, row.rowY + 5,
                        row.error == null ? 0xffffffff : 0xffff5555);
            }
        }
        renderScrollbar(graphics);
    }

    private String fit(String text, int maxWidth) {
        if (gui.getFont().width(text) <= maxWidth) {
            return text;
        }
        String suffix = "...";
        int end = text.length();
        while (end > 0 && gui.getFont().width(text.substring(0, end) + suffix) > maxWidth) {
            end--;
        }
        return text.substring(0, end) + suffix;
    }

    private void renderScrollbar(GuiGraphics graphics) {
        if (attributes.size() <= rows.size()) {
            return;
        }
        int trackTop = TOP;
        int trackHeight = rows.size() * ROW_HEIGHT - 4;
        int thumbHeight = Math.max(12, trackHeight * rows.size() / attributes.size());
        int maximumOffset = attributes.size() - rows.size();
        int thumbY = trackTop + (trackHeight - thumbHeight) * scrollOffset / maximumOffset;
        graphics.fill(width - 8, trackTop, width - 5, trackTop + trackHeight, 0xff333333);
        graphics.fill(width - 8, thumbY, width - 5, thumbY + thumbHeight, 0xffaaaaaa);
    }

    @Override
    public void update() {
        gui.getLocalSetup().get().ifPresentOrElse(profile -> {
            Configuration configuration = profile.toConfiguration();
            attributes = configuration.getValueNames();
            scrollOffset = Math.min(scrollOffset, maximumOffset());
            bindRows(configuration);
        }, () -> {
            attributes = List.of();
            scrollOffset = 0;
            rows.forEach(Row::unbind);
        });
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        rows.forEach(Row::updateVisibility);
    }

    @Override
    public void setBasedOnMode(String mode) {
        visible = page.equalsIgnoreCase(mode);
        rows.forEach(Row::updateVisibility);
    }

    public boolean scroll(double delta) {
        if (!visible || delta == 0 || attributes.size() <= rows.size()) {
            return false;
        }
        int oldOffset = scrollOffset;
        scrollOffset = Math.max(0, Math.min(maximumOffset(), scrollOffset + (delta < 0 ? 3 : -3)));
        if (scrollOffset != oldOffset) {
            gui.getLocalSetup().get().ifPresent(profile -> bindRows(profile.toConfiguration()));
        }
        return true;
    }

    private int maximumOffset() {
        return Math.max(0, attributes.size() - rows.size());
    }

    private void bindRows(Configuration configuration) {
        for (int i = 0; i < rows.size(); i++) {
            int valueIndex = scrollOffset + i;
            if (valueIndex < attributes.size()) {
                String attribute = attributes.get(valueIndex);
                rows.get(i).bind(attribute, configuration.getValue(attribute));
            } else {
                rows.get(i).unbind();
            }
        }
    }

    private class Row {
        private final int rowY;
        private final EditBox textField;
        private final ButtonExt choiceButton;
        private String attribute;
        private Object value;
        private List<?> allowedValues = List.of();
        private boolean textEditor;
        private boolean changing;
        private String error;
        private String baseTooltip;

        private Row(int rowY, int editorWidth) {
            this.rowY = rowY;
            textField = gui.addWidget(new EditBox(gui.getFont(), editorX, rowY, editorWidth, 16, ComponentFactory.literal("")));
            textField.setMaxLength(32767);
            textField.setResponder(this::textChanged);
            choiceButton = gui.addWidget(new ButtonExt(editorX, rowY, editorWidth, 16, ComponentFactory.literal(""), button -> cycleChoice()));
        }

        private void bind(String attribute, Configuration.Value<?> definition) {
            if (!attribute.equals(this.attribute)) {
                textField.setFocused(false);
            }
            this.attribute = attribute;
            this.value = definition.get();
            this.allowedValues = definition.getAllowedValues();
            this.textEditor = !(value instanceof Boolean) && allowedValues.isEmpty();
            this.error = null;
            this.baseTooltip = makeTooltip(attribute, definition);

            changing = true;
            if (textEditor) {
                textField.setValue(format(value));
            } else {
                choiceButton.setMessage(ComponentFactory.literal(format(value)));
            }
            changing = false;
            updateTooltip();
            updateVisibility();
        }

        private void unbind() {
            attribute = null;
            value = null;
            allowedValues = List.of();
            error = null;
            updateVisibility();
        }

        private String makeTooltip(String attribute, Configuration.Value<?> definition) {
            StringBuilder tooltip = new StringBuilder(attribute).append('\n').append(definition.getComment().getString());
            if (!definition.getAllowedValues().isEmpty()) {
                tooltip.append("\nAllowed: ");
                for (int i = 0; i < definition.getAllowedValues().size(); i++) {
                    if (i > 0) {
                        tooltip.append(", ");
                    }
                    tooltip.append(definition.getAllowedValues().get(i));
                }
            } else if (definition.getMin() != null || definition.getMax() != null) {
                tooltip.append("\nRange: ").append(definition.getMin()).append(" to ").append(definition.getMax());
            } else if (definition.get() instanceof String[]) {
                tooltip.append("\nEnter a comma-separated list");
            }
            return tooltip.toString();
        }

        private void updateTooltip() {
            String tooltip = error == null || error.isEmpty() ? baseTooltip : baseTooltip + "\nInvalid value: " + error;
            Tooltip widgetTooltip = Tooltip.create(ComponentFactory.literal(tooltip));
            textField.setTooltip(widgetTooltip);
            choiceButton.setTooltip(widgetTooltip);
        }

        private void textChanged(String text) {
            if (changing || attribute == null) {
                return;
            }
            Object parsed = parse(text, value);
            if (parsed == null) {
                return;
            }
            apply(parsed);
        }

        private Object parse(String text, Object oldValue) {
            try {
                if (oldValue instanceof Integer) {
                    return Integer.parseInt(text);
                } else if (oldValue instanceof Float) {
                    return Float.parseFloat(text);
                } else if (oldValue instanceof Double) {
                    return Double.parseDouble(text);
                } else if (oldValue instanceof String[]) {
                    if (text.isBlank()) {
                        return new String[0];
                    }
                    String[] split = text.split(",");
                    List<String> entries = new ArrayList<>();
                    for (String entry : split) {
                        String trimmed = entry.trim();
                        if (!trimmed.isEmpty()) {
                            entries.add(trimmed);
                        }
                    }
                    return entries.toArray(String[]::new);
                } else if (oldValue instanceof String) {
                    return text;
                }
            } catch (NumberFormatException ignored) {
                // Intermediate values such as an empty string or '-' are allowed while typing.
            }
            return null;
        }

        private void cycleChoice() {
            if (attribute == null) {
                return;
            }
            Object next;
            if (value instanceof Boolean bool) {
                next = !bool;
            } else if (!allowedValues.isEmpty()) {
                int index = allowedValues.indexOf(value);
                next = allowedValues.get((index + 1) % allowedValues.size());
            } else {
                return;
            }
            apply(next);
        }

        private void apply(Object proposedValue) {
            GuiLCConfig.ConfigUpdateResult result = gui.updateProfileValue(attribute, proposedValue);
            error = result.error();
            value = result.value();
            changing = true;
            if (textEditor) {
                if (result.applied() && !format(proposedValue).equals(format(value))) {
                    textField.setValue(format(value));
                } else if (!result.applied()) {
                    textField.setValue(format(value));
                }
            } else {
                choiceButton.setMessage(ComponentFactory.literal(format(value)));
            }
            changing = false;
            updateTooltip();
        }

        private String format(Object value) {
            if (value instanceof String[] strings) {
                return String.join(", ", strings);
            }
            return String.valueOf(value);
        }

        private void updateVisibility() {
            boolean rowVisible = visible && attribute != null;
            textField.visible = rowVisible && textEditor;
            choiceButton.visible = rowVisible && !textEditor;
            textField.setEditable(enabled);
            choiceButton.active = enabled;
            if (!rowVisible) {
                textField.setFocused(false);
            }
        }
    }
}
