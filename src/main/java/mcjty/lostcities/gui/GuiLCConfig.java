package mcjty.lostcities.gui;

import mcjty.lostcities.config.LostCityConfiguration;
import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.dimensions.world.lost.BuildingInfo;
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
import java.util.Random;

public class GuiLCConfig extends Screen {

    private final Screen parent;

    private Button profileButton;
    private Button customizeButton;
    private Button modeButton;
    private Button doneButton;
    private Button cancelButton;
    private Button randomizeButton;

    private String mode = "Cities";

    private long seed = 3439249320423L;
    private Random random = new Random();

    private List<GuiElement> elements = new ArrayList<>();

    private LostCitySetup localSetup = new LostCitySetup(this::refreshPreview);

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

        profileButton = addButton(new Button(70, 10, 100, 20, localSetup.getProfileLabel(), p -> {
            localSetup.toggleProfile();
            updateValues();
        }));
        customizeButton = addButton(new Button(180, 10, 100, 20, "Customize", p -> {
            localSetup.customize();
            updateValues();
        }));
        modeButton = addButton(new Button(290, 10, 100, 20, "Cities", p -> toggleMode()));

        doneButton = addButton(new Button(10, this.height - 30, 120, 20, "Done", p -> done()));
        cancelButton = addButton(new Button(this.width - 130, this.height - 30, 120, 20, "Cancel", p -> cancel()));
        randomizeButton = addButton(new Button(this.width - 35, 35, 30, 20, "Rnd", p -> randomizePreview()));

        int left = 110;
        add(new GuiFloatValueElement(this, "Cities", "Rarity:", left, 40, LostCitySetup::getRarity, LostCitySetup::setRarity));
        add(new GuiFloatValueElement(this, "Cities", "Radius:", left, 65, LostCitySetup::getMinSize, LostCitySetup::setMinSize));
        add(new GuiFloatValueElement(this, "Cities", null, left + 70, 65, LostCitySetup::getMaxSize, LostCitySetup::setMaxSize));
        add(new GuiFloatValueElement(this, "Cities", "Buildings:", left, 90, LostCitySetup::getBuildingRarity, LostCitySetup::setBuildingRarity));

        add(new GuiFloatValueElement(this, "Buildings", "Floors:", left, 40, LostCitySetup::getMinFloors, LostCitySetup::setMinFloors));
        add(new GuiFloatValueElement(this, "Buildings", null, left + 70, 40, LostCitySetup::getMaxFloors, LostCitySetup::setMaxFloors));
        add(new GuiFloatValueElement(this, "Buildings", "Floor Chance:", left, 65, LostCitySetup::getMinFloorsChance, LostCitySetup::setMinFloorsChance));
        add(new GuiFloatValueElement(this, "Buildings", null, left + 70, 65, LostCitySetup::getMaxFloorsChance, LostCitySetup::setMaxFloorsChance));
        add(new GuiFloatValueElement(this, "Buildings", "Cellars:", left, 90, LostCitySetup::getMinCellars, LostCitySetup::setMinCellars));
        add(new GuiFloatValueElement(this, "Buildings", null, left + 70, 90, LostCitySetup::getMaxCellars, LostCitySetup::setMaxCellars));

        updateValues();
    }

    private void toggleMode() {
        if ("Cities".equals(mode)) {
            mode = "Buildings";
        } else {
            mode = "Cities";
        }
        modeButton.setMessage(mode);
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

    private void randomizePreview() {
        seed = random.nextLong();
        refreshPreview();
    }

    private void refreshPreview() {
        BuildingInfo.cleanCache();
    }

    private void renderExtra() {
        drawString(font, "Profile:", 10, 16, 0xffffffff);
        elements.stream().forEach(GuiElement::render);

        localSetup.get().ifPresent(profile -> {
            if ("Cities".equals(mode)) {
                renderPreviewMap(profile);
            } else {
                renderPreviewCity(profile);
            }
        });
    }

    private void renderPreviewCity(LostCityProfile profile) {
        fill(260, 50, 260+150, 50+100, 0xff0099bb);
        fill(260, 50+100, 260+150, 50+150, 0xff996633);

        float radius = 140;

        Random rand = new Random(seed);

        for (int x = 0 ; x < 20 ; x++) {
            float factor = 0;
            float sqdist = (x * 16 - 10 * 16) * (x * 16 - 10 * 16);
            if (sqdist < radius * radius) {
                float dist = (float) Math.sqrt(sqdist);
                factor = (radius - dist) / radius;
            }
            if (factor > 0) {
                int maxfloors = profile.BUILDING_MAXFLOORS;
                int randdist = (int) (profile.BUILDING_MINFLOORS_CHANCE + (factor + .1f) * (profile.BUILDING_MAXFLOORS_CHANCE - profile.BUILDING_MINFLOORS_CHANCE));
                if (randdist < 1) {
                    randdist = 1;
                }
                int f = profile.BUILDING_MINFLOORS + rand.nextInt(randdist);
                f++;
                if (f > maxfloors) {
                    f = maxfloors;
                }
                int minfloors = profile.BUILDING_MINFLOORS + 1;
                if (f < minfloors) {
                    f = minfloors;
                }
                for (int i = 0; i < f; i++) {
                    fill(260 + 7 * x, 50 + 100 - i * 7 - 7, 260 + 7 * x + 6, 50 + 100 - i * 7 + 6 - 7, 0xffffffff);
                }

                int maxcellars = profile.BUILDING_MAXCELLARS;
                int fb = profile.BUILDING_MINCELLARS + ((maxcellars <= 0) ? 0 : rand.nextInt(maxcellars));
                for (int i = 0; i < fb; i++) {
                    fill(260 + 7 * x, 50 + 100 + i * 7, 260 + 7 * x + 6, 50 + 100 + i * 7 + 6, 0xff333333);
                }
            }
        }
    }

    private void renderPreviewMap(LostCityProfile profile) {
        NullDimensionInfo diminfo = new NullDimensionInfo(profile, seed);
        for (int z = 0; z < 50; z++) {
            for (int x = 0; x < 50; x++) {
                int sx = x * 3 + 260;
                int sz = z * 3 + 50;
                int color = 0xff005500;
                char b = diminfo.getBiomeChar(x, z);
                switch (b) {
                    case 'p': color = 0xff005500; break;
                    case '-': color = 0xff000066; break;
                    case '=': color = 0xff000066; break;
                    case '#': color = 0xff447744; break;
                    case '+': color = 0xff335533; break;
                    case '*': color = 0xffcccc55; break;
                    case 'd': color = 0xffcccc55; break;
                }
                fill(sx, sz, sx + 3, sz + 3, color);
                BuildingInfo info = BuildingInfo.getBuildingInfo(x, z, diminfo);
                if (info.isCity) {
                    color = 0xff995555;
                    if (info.hasBuilding) {
                        color = 0xffffffff;
                    }
                    fill(sx, sz, sx + 2, sz + 2, color);
                }
            }
        }
    }

    private void updateValues() {
        elements.stream().forEach(GuiElement::update);
        refreshPreview();
    }

    private void refreshButtons() {
        profileButton.setMessage(localSetup.getProfileLabel());
        customizeButton.active = localSetup.isCustomizable();

        boolean isCustomized = "customized".equals(localSetup.getProfileLabel());
        modeButton.active = localSetup.isCustomizable() || isCustomized;
        elements.stream().forEach(s -> {
            s.setEnabled(isCustomized);
            s.setBasedOnMode(mode);
        });
    }


    private void cancel() {
        BuildingInfo.cleanCache();
        Minecraft.getInstance().displayGuiScreen(parent);
    }

    private void done() {
        BuildingInfo.cleanCache();
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
