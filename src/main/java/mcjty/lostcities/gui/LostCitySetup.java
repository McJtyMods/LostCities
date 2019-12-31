package mcjty.lostcities.gui;

import mcjty.lostcities.config.LostCityConfiguration;
import mcjty.lostcities.config.LostCityProfile;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class LostCitySetup {

    public static final LostCitySetup CLIENT_SETUP = new LostCitySetup(() -> {});

    private List<String> profiles = null;

    private String profile = null;
    private LostCityProfile customizedProfile = null;

    private final Runnable refreshPreview;

    public LostCitySetup(Runnable refreshPreview) {
        this.refreshPreview = refreshPreview;
    }

    public LostCityProfile getCustomizedProfile() {
        return customizedProfile;
    }

    public boolean isCustomizable() {
        if (profile == null) {
            return false;
        }
        if ("customized".equals(profile)) {
            return false;
        }
        return true;
    }

    public String getProfile() {
        return profile;
    }

    public String getProfileLabel() {
        return profile == null ? "Disabled" : profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
        refreshPreview.run();
    }

    public void copyFrom(LostCitySetup other) {
        this.profile = other.profile;
        this.customizedProfile = other.customizedProfile;
    }

    public void customize() {
        if (profile == null) {
            throw new IllegalStateException("Cannot happen!");
        }
        customizedProfile = new LostCityProfile("customized", false);
        LostCityProfile original = LostCityConfiguration.standardProfiles.get(profile);
        customizedProfile.copyFrom(original);
        profile = "customized";
        refreshPreview.run();
    }

    public Optional<LostCityProfile> get() {
        if (profile == null) {
            return Optional.empty();
        } else if ("customized".equals(profile)) {
            return Optional.ofNullable(customizedProfile);
        } else {
            return Optional.of(LostCityConfiguration.standardProfiles.get(profile));
        }
    }

    public void toggleProfile() {
        if (profiles == null) {
            profiles = new ArrayList<>(LostCityConfiguration.standardProfiles.keySet());
            profiles.sort((o1, o2) -> {
                if ("default".equals(o1)) {
                    return -1;
                }
                if ("default".equals(o2)) {
                    return 1;
                }
                return o1.compareTo(o2);
            });
        }

        if (profile == null) {
            if (customizedProfile != null) {
                profile = "customized";
            } else {
                profile = profiles.get(0);
            }
        } else if ("customized".equals(profile)) {
            profile = profiles.get(0);
        } else {
            int i = profiles.indexOf(profile);
            if (i == -1 || i >= profiles.size()-1) {
                profile = null;
            } else {
                profile = profiles.get(i+1);
            }
        }
        refreshPreview.run();
    }

    private static <T extends Comparable<T>> T constrain(T value, T min, T max) {
        if (value.compareTo(min) < 0) {
            return min;
        }
        if (value.compareTo(max) > 0) {
            return max;
        }
        return value;
    }

    public void setFloatValue(String s, BiConsumer<LostCityProfile, Float> setter, Function<LostCityProfile, Float> getter, float minValue, float maxValue) {
        float value = 0;
        try {
            value = Float.parseFloat(s);
        } catch (NumberFormatException e) {
            return;
        }
        float finalValue = value;
        get().ifPresent(p -> {
            if (getter.apply(p) != finalValue) {
                setter.accept(p, constrain(finalValue, minValue, maxValue));
                refreshPreview.run();
            }
        });
    }

    public void setIntValue(String s, BiConsumer<LostCityProfile, Integer> setter, Function<LostCityProfile, Integer> getter, int minValue, int maxValue) {
        int value = 0;
        try {
            value = Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return;
        }
        int finalValue = value;
        get().ifPresent(p -> {
            if (getter.apply(p) != finalValue) {
                setter.accept(p, constrain(finalValue, minValue, maxValue));
                refreshPreview.run();
            }
        });
    }

    //------------------------------------------
    public String getRarity() {
        return get().map(p -> Float.toString(p.CITY_CHANCE)).orElse("n.a.");
    }

    public void setRarity(String s) {
        setFloatValue(s, (p,f) -> p.CITY_CHANCE = f, p -> p.CITY_CHANCE, 0.0f, 1.0f);
    }

    //------------------------------------------
    public String getCityThreshold() {
        return get().map(p -> Float.toString(p.CITY_THRESHOLD)).orElse("n.a.");
    }

    public void setCityThreshold(String s) {
        setFloatValue(s, (p,f) -> p.CITY_THRESHOLD = f, p -> p.CITY_THRESHOLD, 0.0f, 1.0f);
    }

    //------------------------------------------
    public String getBuildingRarity() {
        return get().map(p -> Float.toString(p.BUILDING_CHANCE)).orElse("n.a.");
    }

    public void setBuildingRarity(String s) {
        setFloatValue(s, (p,f) -> p.BUILDING_CHANCE = f, p -> p.BUILDING_CHANCE, 0.0f, 1.0f);
    }

    //------------------------------------------
    public String getMinSize() {
        return get().map(p -> Integer.toString(p.CITY_MINRADIUS)).orElse("n.a.");
    }

    public void setMinSize(String s) {
        setIntValue(s, (p,f) -> p.CITY_MINRADIUS = f, p -> p.CITY_MINRADIUS, 1, 1000);
    }

    public String getMaxSize() {
        return get().map(p -> Integer.toString(p.CITY_MAXRADIUS)).orElse("n.a.");
    }

    public void setMaxSize(String s) {
        setIntValue(s, (p,f) -> p.CITY_MAXRADIUS = f, p -> p.CITY_MAXRADIUS, 1, 1000);
    }

    //------------------------------------------
    public String getMinFloors() {
        return get().map(p -> Integer.toString(p.BUILDING_MINFLOORS)).orElse("n.a.");
    }

    public void setMinFloors(String s) {
        setIntValue(s, (p,f) -> p.BUILDING_MINFLOORS = f, p -> p.BUILDING_MINFLOORS, 0, 30);
    }

    public String getMaxFloors() {
        return get().map(p -> Integer.toString(p.BUILDING_MAXFLOORS)).orElse("n.a.");
    }

    public void setMaxFloors(String s) {
        setIntValue(s, (p,f) -> p.BUILDING_MAXFLOORS = f, p -> p.BUILDING_MAXFLOORS, 0, 30);
    }

    public String getMinFloorsChance() {
        return get().map(p -> Integer.toString(p.BUILDING_MINFLOORS_CHANCE)).orElse("n.a.");
    }

    public void setMinFloorsChance(String s) {
        setIntValue(s, (p,f) -> p.BUILDING_MINFLOORS_CHANCE = f, p -> p.BUILDING_MINFLOORS_CHANCE, 1, 30);
    }

    public String getMaxFloorsChance() {
        return get().map(p -> Integer.toString(p.BUILDING_MAXFLOORS_CHANCE)).orElse("n.a.");
    }

    public void setMaxFloorsChance(String s) {
        setIntValue(s, (p,f) -> p.BUILDING_MAXFLOORS_CHANCE = f, p -> p.BUILDING_MAXFLOORS_CHANCE, 1, 30);
    }

    //------------------------------------------
    public String getMinCellars() {
        return get().map(p -> Integer.toString(p.BUILDING_MINCELLARS)).orElse("n.a.");
    }

    public void setMinCellars(String s) {
        setIntValue(s, (p,f) -> p.BUILDING_MINCELLARS = f, p -> p.BUILDING_MINCELLARS, 0, 7);
    }

    public String getMaxCellars() {
        return get().map(p -> Integer.toString(p.BUILDING_MAXCELLARS)).orElse("n.a.");
    }

    public void setMaxCellars(String s) {
        setIntValue(s, (p,f) -> p.BUILDING_MAXCELLARS = f, p -> p.BUILDING_MAXCELLARS, 0, 7);
    }

    //------------------------------------------
    public Boolean getRubble() {
        return get().map(p -> p.RUBBLELAYER).orElse(false);
    }

    public void setRubble(Boolean value) {
        get().ifPresent(p -> p.RUBBLELAYER = value);
    }

    //------------------------------------------
    public Boolean getSpawners() {
        return get().map(p -> p.GENERATE_SPAWNERS).orElse(false);
    }

    public void setSpawners(Boolean value) {
        get().ifPresent(p -> p.GENERATE_SPAWNERS = value);
    }

    public Boolean getLighting() {
        return get().map(p -> p.GENERATE_LIGHTING).orElse(false);
    }

    public void setLighting(Boolean value) {
        get().ifPresent(p -> p.GENERATE_LIGHTING = value);
    }

    public Boolean getLoot() {
        return get().map(p -> p.GENERATE_LOOT).orElse(false);
    }

    public void setLoot(Boolean value) {
        get().ifPresent(p -> p.GENERATE_LOOT = value);
    }

    //------------------------------------------
    public String getRuins() {
        return get().map(p -> Float.toString(p.RUIN_CHANCE)).orElse("n.a.");
    }

    public void setRuins(String s) {
        setFloatValue(s, (p,f) -> p.RUIN_CHANCE = f, p -> p.RUIN_CHANCE, 0.0f, 1.0f);
    }

    public String getRuinMinLevel() {
        return get().map(p -> Float.toString(p.RUIN_MINLEVEL_PERCENT)).orElse("n.a.");
    }

    public void setRuinMinLevel(String s) {
        setFloatValue(s, (p,f) -> p.RUIN_MINLEVEL_PERCENT = f, p -> p.RUIN_MINLEVEL_PERCENT, 0.0f, 1.0f);
    }

    public String getRuinMaxLevel() {
        return get().map(p -> Float.toString(p.RUIN_MAXLEVEL_PERCENT)).orElse("n.a.");
    }

    public void setRuinMaxLevel(String s) {
        setFloatValue(s, (p,f) -> p.RUIN_MAXLEVEL_PERCENT = f, p -> p.RUIN_MAXLEVEL_PERCENT, 0.0f, 1.0f);
    }

    //------------------------------------------
    public String getExplosionChance() {
        return get().map(p -> Float.toString(p.EXPLOSION_CHANCE)).orElse("n.a.");
    }

    public void setExplosionChance(String s) {
        setFloatValue(s, (p,f) -> p.EXPLOSION_CHANCE = f, p -> p.EXPLOSION_CHANCE, 0.0f, 1.0f);
    }

    public String getExplosionMinLevel() {
        return get().map(p -> Integer.toString(p.EXPLOSION_MINRADIUS)).orElse("n.a.");
    }

    public void setExplosionMinLevel(String s) {
        setIntValue(s, (p,f) -> p.EXPLOSION_MINRADIUS = f, p -> p.EXPLOSION_MINRADIUS, 0, 3000);
    }

    public String getExplosionMaxLevel() {
        return get().map(p -> Integer.toString(p.EXPLOSION_MAXRADIUS)).orElse("n.a.");
    }

    public void setExplosionMaxLevel(String s) {
        setIntValue(s, (p,f) -> p.EXPLOSION_MAXRADIUS = f, p -> p.EXPLOSION_MAXRADIUS, 0, 1000);
    }

    public String getExplosionMinHeight() {
        return get().map(p -> Integer.toString(p.EXPLOSION_MINHEIGHT)).orElse("n.a.");
    }

    public void setExplosionMinHeight(String s) {
        setIntValue(s, (p,f) -> p.EXPLOSION_MINHEIGHT = f, p -> p.EXPLOSION_MINHEIGHT, 0, 256);
    }

    public String getExplosionMaxHeight() {
        return get().map(p -> Integer.toString(p.EXPLOSION_MAXHEIGHT)).orElse("n.a.");
    }

    public void setExplosionMaxHeight(String s) {
        setIntValue(s, (p,f) -> p.EXPLOSION_MAXHEIGHT = f, p -> p.EXPLOSION_MAXHEIGHT, 0, 256);
    }

    //------------------------------------------
    public String getMiniExplosionChance() {
        return get().map(p -> Float.toString(p.MINI_EXPLOSION_CHANCE)).orElse("n.a.");
    }

    public void setMiniExplosionChance(String s) {
        setFloatValue(s, (p,f) -> p.MINI_EXPLOSION_CHANCE = f, p -> p.MINI_EXPLOSION_CHANCE, 0.0f, 1.0f);
    }

    public String getMiniExplosionMinLevel() {
        return get().map(p -> Integer.toString(p.MINI_EXPLOSION_MINRADIUS)).orElse("n.a.");
    }

    public void setMiniExplosionMinLevel(String s) {
        setIntValue(s, (p,f) -> p.MINI_EXPLOSION_MINRADIUS = f, p -> p.MINI_EXPLOSION_MINRADIUS, 0, 3000);
    }

    public String getMiniExplosionMaxLevel() {
        return get().map(p -> Integer.toString(p.MINI_EXPLOSION_MAXRADIUS)).orElse("n.a.");
    }

    public void setMiniExplosionMaxLevel(String s) {
        setIntValue(s, (p,f) -> p.MINI_EXPLOSION_MAXRADIUS = f, p -> p.MINI_EXPLOSION_MAXRADIUS, 0, 1000);
    }

    public String getMiniExplosionMinHeight() {
        return get().map(p -> Integer.toString(p.MINI_EXPLOSION_MINHEIGHT)).orElse("n.a.");
    }

    public void setMiniExplosionMinHeight(String s) {
        setIntValue(s, (p,f) -> p.MINI_EXPLOSION_MINHEIGHT = f, p -> p.MINI_EXPLOSION_MINHEIGHT, 0, 256);
    }

    public String getMiniExplosionMaxHeight() {
        return get().map(p -> Integer.toString(p.MINI_EXPLOSION_MAXHEIGHT)).orElse("n.a.");
    }

    public void setMiniExplosionMaxHeight(String s) {
        setIntValue(s, (p,f) -> p.MINI_EXPLOSION_MAXHEIGHT = f, p -> p.MINI_EXPLOSION_MAXHEIGHT, 0, 256);
    }

    //------------------------------------------
    public String getVineChance() {
        return get().map(p -> Float.toString(p.VINE_CHANCE)).orElse("n.a.");
    }

    public void setVineChance(String s) {
        setFloatValue(s, (p,f) -> p.VINE_CHANCE = f, p -> p.VINE_CHANCE, 0.0f, 1.0f);
    }

    public String getLeafBlocksChance() {
        return get().map(p -> Float.toString(p.CHANCE_OF_RANDOM_LEAFBLOCKS)).orElse("n.a.");
    }

    public void setLeafBlocksChance(String s) {
        setFloatValue(s, (p,f) -> p.CHANCE_OF_RANDOM_LEAFBLOCKS = f, p -> p.CHANCE_OF_RANDOM_LEAFBLOCKS, 0.0f, 1.0f);
    }

    //------------------------------------------
    public Boolean getNetherGen() {
        return get().map(p -> p.GENERATE_NETHER).orElse(false);
    }

    public void setNetherGen(Boolean value) {
        get().ifPresent(p -> p.GENERATE_NETHER = value);
    }

}
