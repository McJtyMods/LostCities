package mcjty.lostcities.worldgen.lost.cityassets;

import mcjty.lostcities.setup.CustomRegistries;
import mcjty.lostcities.worldgen.lost.regassets.*;
import mcjty.lostcities.worldgen.lost.regassets.StuffSettingsRE;
import net.minecraft.world.level.CommonLevelAccessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AssetRegistries {

    public static final RegistryAssetRegistry<Variant, VariantRE> VARIANTS = new RegistryAssetRegistry<>(CustomRegistries.VARIANTS_REGISTRY_KEY, Variant::new);
    public static final RegistryAssetRegistry<Condition, ConditionRE> CONDITIONS = new RegistryAssetRegistry<>(CustomRegistries.CONDITIONS_REGISTRY_KEY, Condition::new);
    public static final RegistryAssetRegistry<WorldStyle, WorldStyleRE> WORLDSTYLES = new RegistryAssetRegistry<>(CustomRegistries.WORLDSTYLES_REGISTRY_KEY, WorldStyle::new);
    public static final RegistryAssetRegistry<CityStyle, CityStyleRE> CITYSTYLES = new RegistryAssetRegistry<>(CustomRegistries.CITYSTYLES_REGISTRY_KEY, CityStyle::new);
    public static final RegistryAssetRegistry<BuildingPart, BuildingPartRE> PARTS = new RegistryAssetRegistry<>(CustomRegistries.PART_REGISTRY_KEY, BuildingPart::new);
    public static final RegistryAssetRegistry<Building, BuildingRE> BUILDINGS = new RegistryAssetRegistry<>(CustomRegistries.BUILDING_REGISTRY_KEY, Building::new);
    public static final RegistryAssetRegistry<MultiBuilding, MultiBuildingRE> MULTI_BUILDINGS = new RegistryAssetRegistry<>(CustomRegistries.MULTIBUILDINGS_REGISTRY_KEY, MultiBuilding::new);
    public static final RegistryAssetRegistry<Style, StyleRE> STYLES = new RegistryAssetRegistry<>(CustomRegistries.STYLE_REGISTRY_KEY, Style::new);
    public static final RegistryAssetRegistry<Palette, PaletteRE> PALETTES = new RegistryAssetRegistry<>(CustomRegistries.PALETTE_REGISTRY_KEY, Palette::new);
    public static final RegistryAssetRegistry<ScatteredBuilding, ScatteredRE> SCATTERED = new RegistryAssetRegistry<>(CustomRegistries.SCATTERED_REGISTRY_KEY, ScatteredBuilding::new);
    public static final RegistryAssetRegistry<PredefinedCity, PredefinedCityRE> PREDEFINED_CITIES = new RegistryAssetRegistry<>(CustomRegistries.PREDEFINEDCITIES_REGISTRY_KEY, PredefinedCity::new);
    public static final RegistryAssetRegistry<PredefinedSphere, PredefinedSphereRE> PREDEFINED_SPHERES = new RegistryAssetRegistry<>(CustomRegistries.PREDEFINEDSPHERES_REGISTRY_KEY, PredefinedSphere::new);
    public static final RegistryAssetRegistry<StuffObject, StuffSettingsRE> STUFF = new RegistryAssetRegistry<>(CustomRegistries.STUFF_REGISTRY_KEY, StuffObject::new);

    public static final Map<String, List<StuffObject>> STUFF_BY_TAG = new HashMap<>();

    private static boolean loaded = false;

    public static void reset() {
        VARIANTS.reset();
        CONDITIONS.reset();
        WORLDSTYLES.reset();
        PARTS.reset();
        BUILDINGS.reset();
        CITYSTYLES.reset();
        MULTI_BUILDINGS.reset();
        STYLES.reset();
        PALETTES.reset();
        PREDEFINED_CITIES.reset();
        PREDEFINED_SPHERES.reset();
        STUFF.reset();
        STUFF_BY_TAG.clear();
        loaded = false;
    }

    public static void load(CommonLevelAccessor level) {
        if (loaded) {
            return;
        }
        PARTS.loadAll(level);
        BUILDINGS.loadAll(level);
        STUFF.loadAll(level);
        PREDEFINED_CITIES.loadAll(level);
        PREDEFINED_SPHERES.loadAll(level);
        STUFF.getIterable().forEach(stuff -> stuff.getSettings().getTags().forEach(tag -> {
            List<StuffObject> list = STUFF_BY_TAG.computeIfAbsent(tag, k -> new ArrayList<>());
            list.add(stuff);
        }));
        loaded = true;
    }
}
