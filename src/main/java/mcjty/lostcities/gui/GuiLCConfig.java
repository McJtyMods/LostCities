package mcjty.lostcities.gui;

import mcjty.lostcities.api.LostChunkCharacteristics;
import mcjty.lostcities.config.LostCityConfiguration;
import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.gui.elements.ButtonExt;
import mcjty.lostcities.worldgen.lost.BuildingInfo;
import mcjty.lostcities.worldgen.lost.Highway;
import mcjty.lostcities.worldgen.lost.Railway;
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

    private final static List<String> modes = Arrays.asList("Cities", "Buildings", "Damage", "Various");
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

        profileButton = addButton(new ButtonExt(this, 70, 10, 100, 20, localSetup.getProfileLabel(), p -> {
            localSetup.toggleProfile();
            updateValues();
        }).tooltip("Select a standard profile for your Lost City worldgen"));
        customizeButton = addButton(new ButtonExt(this, 180, 10, 100, 20, "Customize", p -> {
            localSetup.customize();
            updateValues();
        }).tooltip("Create a customized version of the currently selected profile"));
        modeButton = addButton(new ButtonExt(this, 290, 10, 100, 20, "Cities", p -> toggleMode())
            .tooltip("Switch between different configuration pages"));

        doneButton = addButton(new Button(10, this.height - 30, 120, 20, "Done", p -> done()));
        cancelButton = addButton(new Button(this.width - 130, this.height - 30, 120, 20, "Cancel", p -> cancel()));
        randomizeButton = addButton(new ButtonExt(this,this.width - 35, 35, 30, 20, "Rnd", p -> randomizePreview())
            .tooltip("Randomize the seed for the preview (does not affect the generated world)"));

        int left = 110;
        int yoffs = 21;

        int y = 40;
        add(new GuiFloatValueElement(this, "Cities", left, y, LostCitySetup::getRarity, LostCitySetup::setRarity).label("Rarity:").tooltip("The chance that a given chunk is the center of a city"));
        y += yoffs;
        add(new GuiFloatValueElement(this, "Cities", left, y, LostCitySetup::getCityThreshold, LostCitySetup::setCityThreshold).label("Threshold:").tooltip("This value helps determine how overlapping city spheres are merged"));
        y += yoffs;
        add(new GuiFloatValueElement(this, "Cities", left, y, LostCitySetup::getMinSize, LostCitySetup::setMinSize).label("Radius:").tooltip("Minimum radius for a city"));
        add(new GuiFloatValueElement(this, "Cities", left + 55, y, LostCitySetup::getMaxSize, LostCitySetup::setMaxSize).tooltip("Maximum radius for a city"));
        y += yoffs;
        add(new GuiFloatValueElement(this, "Cities", left, y, LostCitySetup::getBuildingRarity, LostCitySetup::setBuildingRarity).label("Buildings:").tooltip("The chance that a city chunk will also contain a building"));

        y = 40;
        add(new GuiFloatValueElement(this, "Buildings", left, y, LostCitySetup::getMinFloors, LostCitySetup::setMinFloors).label("Floors:").tooltip("The minimum amount of floors every building has"));
        add(new GuiFloatValueElement(this, "Buildings", left + 55, y, LostCitySetup::getMaxFloors, LostCitySetup::setMaxFloors).tooltip("The maximum amount of floors for a building"));
        y += yoffs;
        add(new GuiFloatValueElement(this, "Buildings", left, y, LostCitySetup::getMinFloorsChance, LostCitySetup::setMinFloorsChance).label("Floor Chance:").tooltip("A number that helps determine a random number of floors based on how far from the city center we are"));
        add(new GuiFloatValueElement(this, "Buildings", left + 55, y, LostCitySetup::getMaxFloorsChance, LostCitySetup::setMaxFloorsChance).tooltip("A number that helps determine a random number of floors based on how far from the city center we are"));
        y += yoffs;
        add(new GuiFloatValueElement(this, "Buildings", left, y, LostCitySetup::getMinCellars, LostCitySetup::setMinCellars).label("Cellars:").tooltip("Minimum amount of cellars under a building (if cellars are possible)"));
        add(new GuiFloatValueElement(this, "Buildings", left + 55, y, LostCitySetup::getMaxCellars, LostCitySetup::setMaxCellars).tooltip("Maximum amount of cellars under a building"));

        left = 70;
        y = 40;
        add(new GuiBooleanValueElement(this, "Damage", left, y, LostCitySetup::getRubble, LostCitySetup::setRubble).label("Rubble:").tooltip("If enabled a random rubble layer is added in the cities"));
        y += yoffs;
        add(new GuiFloatValueElement(this, "Damage", left, y, LostCitySetup::getRuins, LostCitySetup::setRuins).label("Ruins:").prefix("%").tooltip("The chance that a building is ruined"));
        add(new GuiFloatValueElement(this, "Damage", left + 80, y, LostCitySetup::getRuinMinLevel, LostCitySetup::setRuinMinLevel).prefix("-").tooltip("The minimum height percentage at which a building can start becoming ruined"));
        add(new GuiFloatValueElement(this, "Damage", left + 140, y, LostCitySetup::getRuinMaxLevel, LostCitySetup::setRuinMaxLevel).prefix("+").tooltip("The maximum height percentage at which a building can start becoming ruined"));
        y += yoffs;

        add(new GuiFloatValueElement(this, "Damage", left, y, LostCitySetup::getExplosionChance, LostCitySetup::setExplosionChance).label("Explosion:").prefix("%").tooltip("The chance for a big explosion to occur in a given chunk"));
        add(new GuiFloatValueElement(this, "Damage", left + 80, y, LostCitySetup::getExplosionMinLevel, LostCitySetup::setExplosionMinLevel).prefix("-").tooltip("The minimum radius for a big explosion"));
        add(new GuiFloatValueElement(this, "Damage", left + 140, y, LostCitySetup::getExplosionMaxLevel, LostCitySetup::setExplosionMaxLevel).prefix("+").tooltip("The maximum radius for a big explosion"));
        y += yoffs;
        add(new GuiFloatValueElement(this, "Damage", left + 80, y, LostCitySetup::getExplosionMinHeight, LostCitySetup::setExplosionMinHeight).label("Height:").tooltip("The minimum height level at which the center of an explosion can occur"));
        add(new GuiFloatValueElement(this, "Damage", left + 140, y, LostCitySetup::getExplosionMaxHeight, LostCitySetup::setExplosionMaxHeight).tooltip("The maximum height level at which the center of an explosion can occur"));
        y += yoffs;

        add(new GuiFloatValueElement(this, "Damage", left, y, LostCitySetup::getMiniExplosionChance, LostCitySetup::setMiniExplosionChance).label("Min/exp:").prefix("%").tooltip("The chance for a small explosion to occur in a given chunk"));
        add(new GuiFloatValueElement(this, "Damage", left + 80, y, LostCitySetup::getMiniExplosionMinLevel, LostCitySetup::setMiniExplosionMinLevel).prefix("-").tooltip("The minimum radius for a small  explosion"));
        add(new GuiFloatValueElement(this, "Damage", left + 140, y, LostCitySetup::getMiniExplosionMaxLevel, LostCitySetup::setMiniExplosionMaxLevel).prefix("+").tooltip("The maximum radius for a small explosion"));
        y += yoffs;
        add(new GuiFloatValueElement(this, "Damage", left + 80, y, LostCitySetup::getMiniExplosionMinHeight, LostCitySetup::setMiniExplosionMinHeight).label("Height:").tooltip("The minimum height level at which the center of an explosion can occur"));
        add(new GuiFloatValueElement(this, "Damage", left + 140, y, LostCitySetup::getMiniExplosionMaxHeight, LostCitySetup::setMiniExplosionMaxHeight).tooltip("The maximum height level at which the center of an explosion can occur"));
        y += yoffs;

        left = 110;
        y = 40;
        add(new GuiBooleanValueElement(this, "Various", left, y, LostCitySetup::getSpawners, LostCitySetup::setSpawners).label("Spawners:").tooltip("Enable or disable mob spawners in buildings"));
        y += yoffs;
        add(new GuiBooleanValueElement(this, "Various", left, y, LostCitySetup::getLighting, LostCitySetup::setLighting).label("Lighting:").tooltip("If enabled friendly torches are added to various buildings"));
        y += yoffs;
        add(new GuiBooleanValueElement(this, "Various", left, y, LostCitySetup::getLoot, LostCitySetup::setLoot).label("Loot:").tooltip("If enabled there will be loot in chests"));
        y += yoffs;
        add(new GuiFloatValueElement(this, "Various", left, y, LostCitySetup::getVineChance, LostCitySetup::setVineChance).label("Vines:").tooltip("The chance that a vine will be created on the side of a building"));
        y += yoffs;
        add(new GuiFloatValueElement(this, "Various", left, y, LostCitySetup::getLeafBlocksChance, LostCitySetup::setLeafBlocksChance).label("Leafs:").tooltip("The chance for random leaf blocks on the ground"));
        y += yoffs;
        y += yoffs;
        add(new GuiBooleanValueElement(this, "Various", left, y, LostCitySetup::getNetherGen, LostCitySetup::setNetherGen).label("Nether:").tooltip("If enabled there will be cities in the Nether (experimental)"));

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
        Highway.cleanCache();
        Railway.cleanCache();
    }

    private void renderExtra() {
        drawString(font, "Profile:", 10, 16, 0xffffffff);
        elements.stream().forEach(GuiElement::render);

        localSetup.get().ifPresent(profile -> {
            if ("Cities".equals(mode)) {
                renderPreviewMap(profile);
            } else if ("Buildings".equals(mode)) {
                renderPreviewCity(profile, false);
            } else if ("Damage".equals(mode)) {
                renderPreviewCity(profile, true);
            } else {
            }
        });
    }

    private void renderPreviewCity(LostCityProfile profile, boolean showDamage) {
        int base = 50 + 120;
        int leftRender = 270;
        fill(leftRender, 50, leftRender + 150, base, 0xff0099bb);
        fill(leftRender, base, leftRender + 150, 50 + 150, 0xff996633);

        float radius = 190;
        int dimHor = 10;
        int dimVer = 4;

        Random rand = new Random(seed);

        for (int x = 0; x < 14; x++) {
            float factor = 0;
            float sqdist = (x * 16 - 7 * 16) * (x * 16 - 7 * 16);
            if (sqdist < radius * radius) {
                float dist = (float) Math.sqrt(sqdist);
                factor = (radius - dist) / radius;
            }
            if (factor > 0 && x > 0) {
                int maxfloors = profile.BUILDING_MAXFLOORS;
                int randdist = (int) (profile.BUILDING_MINFLOORS_CHANCE + (factor + .1f) * (profile.BUILDING_MAXFLOORS_CHANCE - profile.BUILDING_MINFLOORS_CHANCE));
                if (randdist < 1) {
                    randdist = 1;
                }
                int f = profile.BUILDING_MINFLOORS + rand.nextInt(randdist);
                f++;
                if (f > maxfloors+1) {
                    f = maxfloors+1;
                }
                int minfloors = profile.BUILDING_MINFLOORS + 1;
                if (f < minfloors) {
                    f = minfloors;
                }
                for (int i = 0; i < f; i++) {
                    fill(leftRender + dimHor * x, base - i * dimVer - dimVer, leftRender + dimHor * x + dimHor - 1, base - i * dimVer + dimVer - 1 - dimVer, 0xffffffff);
                }

                int maxcellars = profile.BUILDING_MAXCELLARS;
                int fb = profile.BUILDING_MINCELLARS + ((maxcellars <= 0) ? 0 : rand.nextInt(maxcellars + 1));
                for (int i = 0; i < fb; i++) {
                    fill(leftRender + dimHor * x, base + i * dimVer, leftRender + dimHor * x + dimHor - 1, base + i * dimVer + dimVer - 1, 0xff333333);
                }
            }
        }

//        profile.EXPLOSION_CHANCE
        if (showDamage) {
            float horFactor = 1.0f * dimHor / 16.0f;
            float verFactor = 1.0f * dimVer / 6.0f;
            int cx = leftRender + 75;
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
            cx = leftRender + 35;
            cz = (int) (base - (profile.MINI_EXPLOSION_MINHEIGHT-65) * verFactor);
            explosionRadius = profile.MINI_EXPLOSION_MAXRADIUS;
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
                LostChunkCharacteristics characteristics = BuildingInfo.getChunkCharacteristics(x, z, diminfo);
                if (characteristics.isCity) {
                    color = 0xff995555;
                    if (BuildingInfo.hasBuildingGui(x, z, diminfo, characteristics)) {
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
        refreshPreview();
        Minecraft.getInstance().displayGuiScreen(parent);
    }

    private void done() {
        refreshPreview();
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
        for(Widget widget : this.buttons) {
            if (widget.isHovered()) {
                widget.renderToolTip(mouseX - 0, mouseY - 0);
                break;
            }
        }
    }
}
