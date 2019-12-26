package mcjty.lostcities.gui;

import mcjty.lostcities.config.LostCityConfiguration;
import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.dimensions.world.lost.BuildingInfo;
import mcjty.lostcities.gui.elements.GuiBooleanValueElement;
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
import java.util.Arrays;
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

    private final static List<String> modes = Arrays.asList("Cities", "Buildings", "Damage");
    private String mode = modes.get(0);

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
        int yoffs = 21;

        int y = 40;
        add(new GuiFloatValueElement(this, "Cities", "Rarity:", left, y, LostCitySetup::getRarity, LostCitySetup::setRarity)); y += yoffs;
        add(new GuiFloatValueElement(this, "Cities", "Radius:", left, y, LostCitySetup::getMinSize, LostCitySetup::setMinSize));
        add(new GuiFloatValueElement(this, "Cities", null, left + 55, y, LostCitySetup::getMaxSize, LostCitySetup::setMaxSize)); y += yoffs;
        add(new GuiFloatValueElement(this, "Cities", "Buildings:", left, y, LostCitySetup::getBuildingRarity, LostCitySetup::setBuildingRarity));

        y = 40;
        add(new GuiFloatValueElement(this, "Buildings", "Floors:", left, y, LostCitySetup::getMinFloors, LostCitySetup::setMinFloors));
        add(new GuiFloatValueElement(this, "Buildings", null, left + 55, y, LostCitySetup::getMaxFloors, LostCitySetup::setMaxFloors)); y += yoffs;
        add(new GuiFloatValueElement(this, "Buildings", "Floor Chance:", left, y, LostCitySetup::getMinFloorsChance, LostCitySetup::setMinFloorsChance));
        add(new GuiFloatValueElement(this, "Buildings", null, left + 55, y, LostCitySetup::getMaxFloorsChance, LostCitySetup::setMaxFloorsChance)); y += yoffs;
        add(new GuiFloatValueElement(this, "Buildings", "Cellars:", left, y, LostCitySetup::getMinCellars, LostCitySetup::setMinCellars));
        add(new GuiFloatValueElement(this, "Buildings", null, left + 55, y, LostCitySetup::getMaxCellars, LostCitySetup::setMaxCellars));

        y = 40;
        add(new GuiBooleanValueElement(this, "Damage", "Rubble", left, y, LostCitySetup::getRubble, LostCitySetup::setRubble)); y += yoffs;
        add(new GuiFloatValueElement(this, "Damage", "Ruins:", left, y, LostCitySetup::getRuins, LostCitySetup::setRuins)); y += yoffs;
        add(new GuiFloatValueElement(this, "Damage", "Ruin level:", left, y, LostCitySetup::getRuinMinLevel, LostCitySetup::setRuinMinLevel));
        add(new GuiFloatValueElement(this, "Damage", null, left + 55, y, LostCitySetup::getRuinMaxLevel, LostCitySetup::setRuinMaxLevel)); y += yoffs;
        add(new GuiFloatValueElement(this, "Damage", "Explosion radius:", left, y, LostCitySetup::getExplosionMinLevel, LostCitySetup::setExplosionMinLevel));
        add(new GuiFloatValueElement(this, "Damage", null, left + 55, y, LostCitySetup::getExplosionMaxLevel, LostCitySetup::setExplosionMaxLevel)); y += yoffs;
        add(new GuiFloatValueElement(this, "Damage", "Explosion height:", left, y, LostCitySetup::getExplosionMinHeight, LostCitySetup::setExplosionMinHeight));
        add(new GuiFloatValueElement(this, "Damage", null, left + 55, y, LostCitySetup::getExplosionMaxHeight, LostCitySetup::setExplosionMaxHeight)); y += yoffs;

        updateValues();
    }

    private void toggleMode() {
        int idx = modes.indexOf(mode);
        idx++;
        if (idx >= modes.size()) {
            idx = 0;
        }
        mode = modes.get(idx);
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
            } else if ("Buildings".equals(mode)) {
                renderPreviewCity(profile, false);
            } else {
                renderPreviewCity(profile, true);
            }
        });
    }

    private void renderPreviewCity(LostCityProfile profile, boolean showDamage) {
        int base = 50 + 120;
        fill(260, 50, 260 + 150, base, 0xff0099bb);
        fill(260, base, 260 + 150, 50 + 150, 0xff996633);

        float radius = 190;
        int dimHor = 10;
        int dimVer = 4;

        Random rand = new Random(seed);

        for (int x = 0; x < 14; x++) {
            float factor = 0;
            float sqdist = (x * 16 - 12 * 16) * (x * 16 - 12 * 16);
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
                    fill(260 + dimHor * x, base - i * dimVer - dimVer, 260 + dimHor * x + dimHor - 1, base - i * dimVer + dimVer - 1 - dimVer, 0xffffffff);
                }

                int maxcellars = profile.BUILDING_MAXCELLARS;
                int fb = profile.BUILDING_MINCELLARS + ((maxcellars <= 0) ? 0 : rand.nextInt(maxcellars + 1));
                for (int i = 0; i < fb; i++) {
                    fill(260 + dimHor * x, base + i * dimVer, 260 + dimHor * x + dimHor - 1, base + i * dimVer + dimVer - 1, 0xff333333);
                }
            }
        }

//        profile.EXPLOSION_CHANCE
        if (showDamage) {
            float horFactor = 1.0f * dimHor / 16.0f;
            float verFactor = 1.0f * dimVer / 6.0f;
            int cx = 260 + 75;
            int cz = (int) (base - (profile.EXPLOSION_MINHEIGHT-65) * verFactor);
            Random rnd = new Random(333);
            int explosionRadius = profile.EXPLOSION_MAXRADIUS;
            for (int x = (int) (cx - explosionRadius * horFactor); x <= cx + explosionRadius * horFactor; x++) {
                for (int z = (int) (cz - explosionRadius * verFactor); z <= cz + explosionRadius * verFactor; z++) {
                    double sqdist = (cx - x) * (cx - x) / horFactor / horFactor + (cz - z) * (cz - z) / verFactor / verFactor;
                    double dist = Math.sqrt(sqdist);
                    if (dist < explosionRadius - 3) {
                        double damage = 3.0f * (explosionRadius - dist) / explosionRadius;
                        if (rnd.nextFloat() < damage) {
                            fill(x, z, x + 1, z + 1, 0x66ff0000);
                        }
                    }
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
