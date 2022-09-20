package mcjty.lostcities.worldgen.lost.cityassets;

import mcjty.lostcities.api.ILostCityCityStyle;
import mcjty.lostcities.varia.Tools;
import mcjty.lostcities.worldgen.lost.regassets.CityStyleRE;
import mcjty.lostcities.worldgen.lost.regassets.data.DataTools;
import mcjty.lostcities.worldgen.lost.regassets.data.ObjectSelector;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.CommonLevelAccessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CityStyle implements ILostCityCityStyle {

    private final ResourceLocation name;

    private final List<ObjectSelector> buildingSelector = new ArrayList<>();
    private final List<ObjectSelector> bridgeSelector = new ArrayList<>();
    private final List<ObjectSelector> parkSelector = new ArrayList<>();
    private final List<ObjectSelector> fountainSelector = new ArrayList<>();
    private final List<ObjectSelector> stairSelector = new ArrayList<>();
    private final List<ObjectSelector> frontSelector = new ArrayList<>();
    private final List<ObjectSelector> railDungeonSelector = new ArrayList<>();
    private final List<ObjectSelector> multiBuildingSelector = new ArrayList<>();

    // Building settings
    private Integer minFloorCount;
    private Integer minCellarCount;
    private Integer maxFloorCount;
    private Integer maxCellarCount;
    private Float buildingChance;   // Optional build chance override

    // Street settings
    private Integer streetWidth;
    private Character streetBlock;
    private Character streetBaseBlock;
    private Character streetVariantBlock;
    private Character borderBlock;
    private Character wallBlock;

    // Park settings
    private Character parkElevationBlock;
    private Character grassBlock;

    // Corridor settings
    private Character corridorRoofBlock;
    private Character corridorGlassBlock;

    // Rail settings
    private Character railMainBlock;

    // Sphere settings
    private Character sphereBlock;          // Used for 'space' landscape type
    private Character sphereSideBlock;      // Used for 'space' landscape type
    private Character sphereGlassBlock;     // Used for 'space' landscape type

    // General settings
    private Character ironbarsBlock;
    private Character glowstoneBlock;
    private Character leavesBlock;
    private Character rubbleDirtBlock;

    private Float explosionChance;
    private String style;
    private final String inherit;
    private boolean resolveInherit = false;

    public CityStyle(CityStyleRE object) {
        name = object.getRegistryName();
        inherit = object.getInherit();
        style = object.getStyle();
        explosionChance = object.getExplosionChance();
        object.getBuildingSettings().ifPresent(s -> {
            buildingChance = s.getBuildingChance();
            maxCellarCount = s.getMaxCellarCount();
            maxFloorCount = s.getMaxFloorCount();
            minCellarCount = s.getMinCellarCount();
            minFloorCount = s.getMinFloorCount();
        });
        object.getCorridorSettings().ifPresent(s -> {
            corridorGlassBlock = s.getCorridorGlassBlock();
            corridorRoofBlock = s.getCorridorRoofBlock();
        });
        object.getRailSettings().ifPresent(s -> {
            railMainBlock = s.getRailMainBlock();
        });
        object.getParkSettings().ifPresent(s -> {
            grassBlock = s.getGrassBlock();
            parkElevationBlock = s.getParkElevationBlock();
        });
        object.getSphereSettings().ifPresent(s -> {
            sphereBlock = s.getSphereBlock();
            sphereGlassBlock = s.getSphereGlassBlock();
            sphereSideBlock = s.getSphereSideBlock();
        });
        object.getStreetSettings().ifPresent(s -> {
            borderBlock = s.getBorderBlock();
            streetBaseBlock = s.getStreetBaseBlock();
            streetBlock = s.getStreetBlock();
            streetVariantBlock = s.getStreetVariantBlock();
            wallBlock = s.getWallBlock();
            streetWidth = s.getStreetWidth();
        });
        object.getGeneralSettings().ifPresent(s -> {
            glowstoneBlock = s.getGlowstoneBlock();
            ironbarsBlock = s.getIronbarsBlock();
            leavesBlock = s.getLeavesBlock();
            rubbleDirtBlock = s.getRubbleDirtBlock();
        });
        object.getSelectors().ifPresent(s -> {
            s.getBridgeSelector().ifPresent(bridgeSelector::addAll);
            s.getBuildingSelector().ifPresent(buildingSelector::addAll);
            s.getFountainSelector().ifPresent(fountainSelector::addAll);
            s.getFrontSelector().ifPresent(frontSelector::addAll);
            s.getParkSelector().ifPresent(parkSelector::addAll);
            s.getMultiBuildingSelector().ifPresent(multiBuildingSelector::addAll);
            s.getRailDungeonSelector().ifPresent(railDungeonSelector::addAll);
            s.getStairSelector().ifPresent(stairSelector::addAll);
        });
    }

    @Override
    public String getName() {
        return DataTools.toName(name);
    }

    @Override
    public ResourceLocation getId() {
        return name;
    }

    @Override
    public String getStyle() {
        return style;
    }

    @Override
    public Float getExplosionChance() {
        return explosionChance;
    }

    @Override
    public int getStreetWidth() {
        return streetWidth;
    }

    @Override
    public Integer getMinFloorCount() {
        return minFloorCount;
    }

    @Override
    public Integer getMinCellarCount() {
        return minCellarCount;
    }

    @Override
    public Integer getMaxFloorCount() {
        return maxFloorCount;
    }

    @Override
    public Integer getMaxCellarCount() {
        return maxCellarCount;
    }

    @Override
    public Float getBuildingChance() {
        return buildingChance;
    }

    @Override
    public Character getGrassBlock() {
        return grassBlock;
    }

    @Override
    public Character getIronbarsBlock() {
        return ironbarsBlock;
    }

    @Override
    public Character getGlowstoneBlock() {
        return glowstoneBlock;
    }

    @Override
    public Character getLeavesBlock() {
        return leavesBlock;
    }

    public Character getRubbleDirtBlock() {
        return rubbleDirtBlock;
    }

    @Override
    public Character getStreetBlock() {
        return streetBlock;
    }

    @Override
    public Character getStreetBaseBlock() {
        return streetBaseBlock;
    }

    @Override
    public Character getStreetVariantBlock() {
        return streetVariantBlock;
    }

    @Override
    public Character getRailMainBlock() {
        return railMainBlock;
    }

    @Override
    public Character getParkElevationBlock() {
        return parkElevationBlock;
    }

    @Override
    public Character getCorridorRoofBlock() {
        return corridorRoofBlock;
    }

    @Override
    public Character getCorridorGlassBlock() {
        return corridorGlassBlock;
    }

    @Override
    public Character getBorderBlock() {
        return borderBlock;
    }

    @Override
    public Character getWallBlock() {
        return wallBlock;
    }

    public Character getSphereBlock() {
        return sphereBlock;
    }

    public Character getSphereSideBlock() {
        return sphereSideBlock;
    }

    public Character getSphereGlassBlock() {
        return sphereGlassBlock;
    }

    @Override
    public void init(CommonLevelAccessor level) {
        if (!resolveInherit) {
            resolveInherit = true;
            if (inherit != null) {
                CityStyle inheritFrom = AssetRegistries.CITYSTYLES.getOrThrow(level, inherit);
                if (style == null) {
                    style = inheritFrom.getStyle();
                }
                buildingSelector.addAll(inheritFrom.buildingSelector);
                bridgeSelector.addAll(inheritFrom.bridgeSelector);
                parkSelector.addAll(inheritFrom.parkSelector);
                fountainSelector.addAll(inheritFrom.fountainSelector);
                stairSelector.addAll(inheritFrom.stairSelector);
                frontSelector.addAll(inheritFrom.frontSelector);
                railDungeonSelector.addAll(inheritFrom.railDungeonSelector);
                multiBuildingSelector.addAll(inheritFrom.multiBuildingSelector);
                if (explosionChance == null) {
                    explosionChance = inheritFrom.explosionChance;
                }
                if (streetWidth == null) {
                    streetWidth = inheritFrom.streetWidth;
                }
                if (minFloorCount == null) {
                    minFloorCount = inheritFrom.minFloorCount;
                }
                if (minCellarCount == null) {
                    minCellarCount = inheritFrom.minCellarCount;
                }
                if (maxFloorCount == null) {
                    maxFloorCount = inheritFrom.maxFloorCount;
                }
                if (maxCellarCount == null) {
                    maxCellarCount = inheritFrom.maxCellarCount;
                }
                if (buildingChance == null) {
                    buildingChance = inheritFrom.buildingChance;
                }
                if (streetBlock == null) {
                    streetBlock = inheritFrom.streetBlock;
                }
                if (streetBaseBlock == null) {
                    streetBaseBlock = inheritFrom.streetBaseBlock;
                }
                if (streetVariantBlock == null) {
                    streetVariantBlock = inheritFrom.streetVariantBlock;
                }
                if (parkElevationBlock == null) {
                    parkElevationBlock = inheritFrom.parkElevationBlock;
                }
                if (corridorRoofBlock == null) {
                    corridorRoofBlock = inheritFrom.corridorRoofBlock;
                }
                if (corridorGlassBlock == null) {
                    corridorGlassBlock = inheritFrom.corridorGlassBlock;
                }
                if (railMainBlock == null) {
                    railMainBlock = inheritFrom.railMainBlock;
                }
                if (borderBlock == null) {
                    borderBlock = inheritFrom.borderBlock;
                }
                if (wallBlock == null) {
                    wallBlock = inheritFrom.wallBlock;
                }
                if (sphereBlock == null) {
                    sphereBlock = inheritFrom.sphereBlock;
                }
                if (sphereSideBlock == null) {
                    sphereSideBlock = inheritFrom.sphereSideBlock;
                }
                if (sphereGlassBlock == null) {
                    sphereGlassBlock = inheritFrom.sphereGlassBlock;
                }
            }
        }
    }

    private static String getRandomFromList(RandomSource random, List<ObjectSelector> list) {
        ObjectSelector fromList = Tools.getRandomFromList(random, list, ObjectSelector::factor);
        if (fromList == null) {
            return null;
        } else {
            return fromList.value();
        }
    }

    private static String getRandomFromList(Random random, List<ObjectSelector> list) {
        ObjectSelector fromList = Tools.getRandomFromList(random, list, ObjectSelector::factor);
        if (fromList == null) {
            return null;
        } else {
            return fromList.value();
        }
    }

    public String getRandomStair(Random random) {
        return getRandomFromList(random, stairSelector);
    }

    public String getRandomFront(Random random) {
        return getRandomFromList(random, frontSelector);
    }

    public String getRandomRailDungeon(Random random) {
        return getRandomFromList(random, railDungeonSelector);
    }

    public String getRandomPark(Random random) {
        return getRandomFromList(random, parkSelector);
    }

    public String getRandomBridge(Random random) {
        return getRandomFromList(random, bridgeSelector);
    }

    public String getRandomFountain(Random random) {
        return getRandomFromList(random, fountainSelector);
    }

    public String getRandomBuilding(Random random) {
        return getRandomFromList(random, buildingSelector);
    }

    public String getRandomMultiBuilding(Random random) {
        return getRandomFromList(random, multiBuildingSelector);
    }

    public boolean hasMultiBuildings() {
        return !multiBuildingSelector.isEmpty();
    }
}
