package mcjty.lostcities.gui;

import mcjty.lostcities.config.LostCityConfiguration;
import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.dimensions.world.lost.BuildingInfo;
import mcjty.lostcities.dimensions.world.lost.City;
import mcjty.lostcities.gui.elements.GuiElement;
import mcjty.lostcities.gui.elements.GuiFloatValueElement;
import mcjty.lostcities.setup.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class GuiLCConfig extends Screen {

    private final Screen parent;

    private Button profileButton;
    private Button customizeButton;
    private Button doneButton;
    private Button cancelButton;

    private List<GuiElement> elements = new ArrayList<>();

    private LostCitySetup localSetup = new LostCitySetup();

    public GuiLCConfig(Screen parent) {
        super(new StringTextComponent("Lost City Configuration"));
        this.parent = parent;
        localSetup.copyFrom(LostCitySetup.CLIENT_SETUP);
    }

    private static void selectProfile(String profileName, @Nullable LostCityProfile profile) {
        Config.profileFromClient = profileName;
        if (profile != null) {
            LostCityConfiguration.standardProfiles.get("customized").copyFrom(profile);
            Config.jsonFromClient = profile.toJson().toString();
        }
    }

    public LostCitySetup getLocalSetup() {
        return localSetup;
    }

    public FontRenderer getFont() {
        return this.font;
    }

    @Override
    public void tick() {
        elements.stream().forEach(GuiElement::tick);
    }

    @Override
    protected void init() {
        super.init();
        this.minecraft.keyboardListener.enableRepeatEvents(true);

        profileButton = addButton(new Button(70, 10, 120, 20, localSetup.getProfileLabel(), p -> {
            localSetup.toggleProfile();
            updateValues();
        }));
        customizeButton = addButton(new Button(200, 10, 120, 20, "Customize", p -> {
            localSetup.customize();
            updateValues();
        }));
        doneButton = addButton(new Button(10, this.height - 30, 120, 20, "Done", p -> done()));
        cancelButton = addButton(new Button(240, this.height - 30, 120, 20, "Cancel", p -> cancel()));

        int left = 110;
        add(new GuiFloatValueElement(this, "Rarity:", left, 40, LostCitySetup::getRarity, LostCitySetup::setRarity));
        add(new GuiFloatValueElement(this, "Radius:", left, 65, LostCitySetup::getMinSize, LostCitySetup::setMinSize));
        add(new GuiFloatValueElement(this, null, left + 70, 65, LostCitySetup::getMaxSizeLabel, LostCitySetup::setMaxSize));
        add(new GuiFloatValueElement(this, "Building:", left, 90, LostCitySetup::getMinFloors, LostCitySetup::setMinFloors));
        add(new GuiFloatValueElement(this, null, left + 70, 90, LostCitySetup::getMaxFloors, LostCitySetup::setMaxFloors));
        add(new GuiFloatValueElement(this, "Building Chance:", left, 115, LostCitySetup::getMinFloorsChance, LostCitySetup::setMinFloorsChance));
        add(new GuiFloatValueElement(this, null, left + 70, 115, LostCitySetup::getMaxFloorsChance, LostCitySetup::setMaxFloorsChance));

        updateValues();
    }

    private GuiElement add(GuiElement el) {
        elements.add(el);
        return el;
    }

    public <T extends Widget> T addWidget(T widget) {
        this.buttons.add(widget);
        this.children.add(widget);
        return widget;
    }

    private void renderExtra() {
        drawString(font, "Profile:", 10, 16, 0xffffffff);
        elements.stream().forEach(GuiElement::render);

        for (int z = 0 ; z < 50 ; z++) {
            for (int x = 0 ; x < 50 ; x++) {
                int sx = x*3+200;
                int sz = z*3+100;
                int color = 0xffffffff;
//                float cityFactor = City.getCityFactor(x, z, provider, profile);
//                return cityFactor > profile.CITY_THRESSHOLD;
//                BuildingInfo.isCityRaw(x, z, null, null);
                fill(sx, sz, sx+2, sz+2, color);
            }
        }
    }

    private void updateValues() {
        elements.stream().forEach(GuiElement::update);
    }

    private void refreshButtons() {
        profileButton.setMessage(localSetup.getProfileLabel());
        customizeButton.active = localSetup.isCustomizable();

        boolean isCustomized = "customized".equals(localSetup.getProfileLabel());
        elements.stream().forEach(s -> s.setEnabled(isCustomized));
    }


    private void cancel() {
        Minecraft.getInstance().displayGuiScreen(parent);
    }

    private void done() {
        LostCitySetup.CLIENT_SETUP.copyFrom(localSetup);
        LostCityProfile customizedProfile = localSetup.getCustomizedProfile();
        if ("customized".equals(localSetup.getProfile()) && customizedProfile != null) {
            LostCityConfiguration.standardProfiles.get("customized").copyFrom(customizedProfile);
            selectProfile(localSetup.getProfile(), customizedProfile);
        } else {
            selectProfile(localSetup.getProfile(), null);
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
