package mcjty.lostcities.gui;

import mcjty.lostcities.config.LostCityConfiguration;
import mcjty.lostcities.config.LostCityProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;

public class GuiLCConfig extends Screen {

    private final Screen parent;

    private Button profileButton;
    private Button customizeButton;
    private Button doneButton;
    private Button cancelButton;

    private TextFieldWidget rarityField;
    private TextFieldWidget minRadiusField;
    private TextFieldWidget maxRadiusField;

    private LostCitySetup localSetup = new LostCitySetup();

    public GuiLCConfig(Screen parent) {
        super(new StringTextComponent("Lost City Configuration"));
        this.parent = parent;
        localSetup.copyFrom(LostCitySetup.CLIENT_SETUP);
    }

    @Override
    public void tick() {
        rarityField.tick();
        minRadiusField.tick();
        maxRadiusField.tick();
    }

    @Override
    protected void init() {
        super.init();
        this.minecraft.keyboardListener.enableRepeatEvents(true);

        profileButton = addButton(new Button(70, 10, 120, 20, localSetup.getProfileLabel(), p -> localSetup.toggleProfile()));
        customizeButton = addButton(new Button(200, 10, 120, 20, "Customize", p -> localSetup.customize()));
        doneButton = addButton(new Button(10, 250, 120, 20, "Done", p -> done()));
        cancelButton = addButton(new Button(240, 250, 120, 20, "Cancel", p -> cancel()));

        rarityField = addButton(new TextFieldWidget(font, 90, 40, 120, 16, localSetup.getRarityLabel()));
        rarityField.setResponder(s -> localSetup.setRarity(s));

        minRadiusField = addButton(new TextFieldWidget(font, 90, 65, 60, 16, localSetup.getMinRadiusLabel()));
        minRadiusField.setResponder(s -> localSetup.setMinSizeLabel(s));
        maxRadiusField = addButton(new TextFieldWidget(font, 160, 65, 60, 16, localSetup.getMaxRadiusLabel()));
        maxRadiusField.setResponder(s -> localSetup.setMaxSizeLabel(s));
    }

    private void renderExtra() {
        drawString(font, "Profile:", 10, 16, 0xffffffff);
        drawString(font, "Rarity:", 10, 45, 0xffffffff);
        drawString(font, "Radius min/max:", 10, 70, 0xffffffff);
    }

    private void refreshButtons() {
        profileButton.setMessage(localSetup.getProfileLabel());
        rarityField.setText(localSetup.getRarityLabel());
        minRadiusField.setText(localSetup.getMinRadiusLabel());
        maxRadiusField.setText(localSetup.getMaxRadiusLabel());

        customizeButton.active = localSetup.isCustomizable();

        boolean isCustomized = "customized".equals(localSetup.getProfileLabel());
        rarityField.setEnabled(isCustomized);
        minRadiusField.setEnabled(isCustomized);
        maxRadiusField.setEnabled(isCustomized);
    }


    private void cancel() {
        Minecraft.getInstance().displayGuiScreen(parent);
    }

    private void done() {
        LostCitySetup.CLIENT_SETUP.copyFrom(localSetup);
        LostCityProfile customizedProfile = localSetup.getCustomizedProfile();
        if ("customized".equals(localSetup.getProfile()) && customizedProfile != null) {
            LostCityConfiguration.standardProfiles.get("customized").copyFrom(customizedProfile);
        }

        Minecraft.getInstance().displayGuiScreen(parent);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        this.renderBackground();
        refreshButtons();
        renderExtra();
        super.render(mouseX, mouseY, partialTicks);
    }
}
