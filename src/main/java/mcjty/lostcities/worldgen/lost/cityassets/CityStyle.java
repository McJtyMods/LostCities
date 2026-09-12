package mcjty.lostcities.worldgen.lost.cityassets;

import mcjty.lostcities.api.ILostCityCityStyle;
import mcjty.lostcities.varia.ChunkCoord;
import mcjty.lostcities.varia.Tools;
import mcjty.lostcities.worldgen.lost.regassets.CityStyleRE;
import mcjty.lostcities.worldgen.lost.regassets.data.DataTools;
import mcjty.lostcities.worldgen.lost.regassets.data.ObjectSelector;
import mcjty.lostcities.worldgen.lost.regassets.data.StreetParts;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.CommonLevelAccessor;

import java.util.*;

public class CityStyle implements ILostCityCityStyle {

    private final ResourceLocation name;

    private final Set<String> stuffTags = new HashSet<>();

    private final List<ObjectSelector> buildingSelector = new ArrayList<>();
    private final List<ObjectSelector> bridgeSelector = new ArrayList<>();
    private final List<ObjectSelector> largeBridgeSelector = new ArrayList<>();
    private final List<ObjectSelector> parkSelector = new ArrayList<>();
    private final List<ObjectSelector> fountainSelector = new ArrayList<>();
    private final List<ObjectSelector> stairSelector = new ArrayList<>();
    private final List<ObjectSelector> frontSelector = new ArrayList<>();
    private final List<ObjectSelector> railDungeonSelector = new ArrayList<>();
    private final List<ObjectSelector> multiBuildingSelector = new ArrayList<>();
    private StreetParts streetParts = StreetParts.DEFAULT;
    private StreetParts largeStreetParts = StreetParts.DEFAULT;
    private StreetParts tertiaryStreetParts = StreetParts.DEFAULT;

    // Building settings
    private Integer minFloorCount;
    private Integer minCellarCount;
    private Integer maxFloorCount;
    private Integer maxCellarCount;
    private Float buildingChance;   // Optional build chance override

    // Street settings
    private Float fountainChance;
    private Float frontChance;
    private Integer streetWidth;
    private Character streetBlock;
    private Character streetBaseBlock;
    private Character streetVariantBlock;
    private Character borderBlock;
    private Character wallBlock;

    // Park settings
    private Float parkChance;
    private Float openLotParkChance;
    private Boolean avoidFoliage;
    private Boolean parkBorder;
    private Boolean parkElevation;
    private Integer parkStreetThreshold;
    private Character parkElevationBlock;
    private Character grassBlock;

    // Corridor settings
    private Float corridorChance;
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

    // Bridge support settings
    private Character bridgeSupport;
    private String bridgeSupportPart;

    private Float explosionChance;
    private String style;
    private final String inherit;
    private boolean resolveInherit = false;
    private volatile boolean initialized = false;

    public CityStyle(CityStyleRE object) {
        name = object.getRegistryName();
        inherit = object.getInherit();
        style = object.getStyle();
        stuffTags.add("all");
        if (object.getStuffTags() != null) {
            stuffTags.addAll(object.getStuffTags());
        }
        explosionChance = object.getExplosionChance();
        bridgeSupport = object.getBridgeSupport();
        bridgeSupportPart = object.getBridgeSupportPart();
        object.getProfileOverrides().ifPresent(overrides -> openLotParkChance = overrides.openLotParkChance());
        object.getBuildingSettings().ifPresent(s -> {
            buildingChance = s.getBuildingChance();
            maxCellarCount = s.getMaxCellarCount();
            maxFloorCount = s.getMaxFloorCount();
            minCellarCount = s.getMinCellarCount();
            minFloorCount = s.getMinFloorCount();
        });
        object.getCorridorSettings().ifPresent(s -> {
            corridorChance = s.getCorridorChance();
            corridorGlassBlock = s.getCorridorGlassBlock();
            corridorRoofBlock = s.getCorridorRoofBlock();
        });
        object.getRailSettings().ifPresent(s -> {
            railMainBlock = s.getRailMainBlock();
        });
        object.getParkSettings().ifPresent(s -> {
            parkChance = s.getParkChance();
            avoidFoliage = s.getAvoidFoliage();
            parkBorder = s.getParkBorder();
            parkElevation = s.getParkElevation();
            parkStreetThreshold = s.getParkStreetThreshold();
            grassBlock = s.getGrassBlock();
            parkElevationBlock = s.getParkElevationBlock();
        });
        object.getSphereSettings().ifPresent(s -> {
            sphereBlock = s.getSphereBlock();
            sphereGlassBlock = s.getSphereGlassBlock();
            sphereSideBlock = s.getSphereSideBlock();
        });
        object.getStreetSettings().ifPresent(s -> {
            fountainChance = s.getFountainChance();
            frontChance = s.getFrontChance();
            borderBlock = s.getBorderBlock();
            streetBaseBlock = s.getStreetBaseBlock();
            streetBlock = s.getStreetBlock();
            streetVariantBlock = s.getStreetVariantBlock();
            wallBlock = s.getWallBlock();
            streetWidth = s.getStreetWidth();
            streetParts = s.getParts();
            largeStreetParts = s.getLargeParts();
            tertiaryStreetParts = s.getTertiaryParts();
        });
        object.getGeneralSettings().ifPresent(s -> {
            glowstoneBlock = s.getGlowstoneBlock();
            ironbarsBlock = s.getIronbarsBlock();
            leavesBlock = s.getLeavesBlock();
            rubbleDirtBlock = s.getRubbleDirtBlock();
        });
        object.getSelectors().ifPresent(s -> {
            s.getBridgeSelector().ifPresent(bridgeSelector::addAll);
            s.getLargeBridgeSelector().ifPresent(largeBridgeSelector::addAll);
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

    public StreetParts getStreetParts() {
        return streetParts;
    }

    public StreetParts getLargeStreetParts() {
        return largeStreetParts;
    }

    public StreetParts getTertiaryStreetParts() {
        return tertiaryStreetParts == StreetParts.DEFAULT ? streetParts : tertiaryStreetParts;
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
    public Float getParkChance() { return parkChance; }

    public Float getOpenLotParkChance() { return openLotParkChance; }

    @Override
    public Float getFrontChance() { return frontChance; }

    @Override
    public Float getCorridorChance() { return corridorChance; }

    @Override
    public Boolean getAvoidFoliage() {
        return avoidFoliage;
    }

    @Override
    public Boolean getParkBorder() {
        return parkBorder;
    }

    @Override
    public Integer getParkStreetThreshold() { return parkStreetThreshold; }

    @Override
    public Boolean getParkElevation() {
        return parkElevation;
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

    public Character getBridgeSupport() {
        return bridgeSupport;
    }

    public String getBridgeSupportPart() {
        return bridgeSupportPart;
    }

    @Override
    public Float getFountainChance() {return fountainChance; }

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
        if (initialized) {
            return;
        }
        synchronized (this) {
            if (initialized || resolveInherit) {
                return;
            }
            resolveInherit = true;
            if (inherit != null) {
                CityStyle inheritFrom = AssetRegistries.CITYSTYLES.getOrThrow(level, inherit);
                if (style == null) {
                    style = inheritFrom.getStyle();
                }
                stuffTags.addAll(inheritFrom.stuffTags);
                buildingSelector.addAll(inheritFrom.buildingSelector);
                bridgeSelector.addAll(inheritFrom.bridgeSelector);
                largeBridgeSelector.addAll(inheritFrom.largeBridgeSelector);
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
                if (streetParts == StreetParts.DEFAULT) {
                    streetParts = inheritFrom.streetParts;
                }
                if (largeStreetParts == StreetParts.DEFAULT) {
                    largeStreetParts = inheritFrom.largeStreetParts;
                }
                if (tertiaryStreetParts == StreetParts.DEFAULT) {
                    tertiaryStreetParts = inheritFrom.tertiaryStreetParts;
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
                if (parkChance == null) {
                    parkChance = inheritFrom.parkChance;
                }
                if (openLotParkChance == null) {
                    openLotParkChance = inheritFrom.openLotParkChance;
                }
                if (fountainChance == null) {
                    fountainChance = inheritFrom.fountainChance;
                }
                if (frontChance == null) {
                    frontChance = inheritFrom.frontChance;
                }
                if (corridorChance == null) {
                    corridorChance = inheritFrom.corridorChance;
                }
                if (parkElevation == null) {
                    parkElevation = inheritFrom.parkElevation;
                }
                if (avoidFoliage == null) {
                    avoidFoliage = inheritFrom.avoidFoliage;
                }
                if (parkBorder == null) {
                    parkBorder = inheritFrom.parkBorder;
                }
                if (parkStreetThreshold == null) {
                    parkStreetThreshold = inheritFrom.parkStreetThreshold;
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
                if (bridgeSupport == null) {
                    bridgeSupport = inheritFrom.bridgeSupport;
                }
                if (bridgeSupportPart == null) {
                    bridgeSupportPart = inheritFrom.bridgeSupportPart;
                }
            }
            initialized = true;
        }
    }

    private static String getRandomFromList(Random random, List<ObjectSelector> list, ChunkCoord pos) {
        ObjectSelector fromList = Tools.getRandomFromList(random, list, objectSelector -> {
            if (objectSelector.minSpawnDistance() > 0 || objectSelector.maxSpawnDistance() < Integer.MAX_VALUE) {
                // Distance in objectSelector is in blocks whereas pos is in chunks
                // Objects can only return 'factor' between minSpawnDistance and maxSpawnDistance
                // objectSelector.feather() is used to make the transition at minSpawnDistance and maxSpawnDistance more smooth
                int squaredDist = (pos.chunkX() << 4) * (pos.chunkX() << 4) + (pos.chunkZ() << 4) * (pos.chunkZ() << 4);
                int minDist = objectSelector.minSpawnDistance();
                int maxDist = objectSelector.maxSpawnDistance();
                if (squaredDist < minDist * minDist) {
                    if (objectSelector.feather() <= 0) {
                        return 0.0f;
                    } else {
                        int fd = minDist - objectSelector.feather();
                        if (squaredDist < fd * fd) {
                            return 0.0f;
                        } else {
                            float f = (float) (Math.sqrt(squaredDist) - fd) / (float) (minDist - fd);
                            return f * objectSelector.factor();
                        }
                    }
                } else if (squaredDist > maxDist * maxDist) {
                    if (objectSelector.feather() <= 0) {
                        return 0.0f;
                    } else {
                        int fd = maxDist + objectSelector.feather();
                        if (squaredDist > fd * fd) {
                            return 0.0f;
                        } else {
                            float f = (float) (fd - Math.sqrt(squaredDist)) / (float) (fd - maxDist);
                            return f * objectSelector.factor();
                        }
                    }
                }
            }
            return objectSelector.factor();
        });
        if (fromList == null) {
            return null;
        } else {
            return fromList.value();
        }
    }

    public Set<String> getStuffTags() {
        return stuffTags;
    }

    public String getRandomStair(Random random, ChunkCoord pos) {
        return getRandomFromList(random, stairSelector, pos);
    }

    public String getRandomFront(Random random, ChunkCoord pos) {
        return getRandomFromList(random, frontSelector, pos);
    }

    public String getRandomRailDungeon(Random random, ChunkCoord pos) {
        return getRandomFromList(random, railDungeonSelector, pos);
    }

    public String getRandomPark(Random random, ChunkCoord pos) {
        return getRandomFromList(random, parkSelector, pos);
    }

    public String getRandomBridge(Random random, ChunkCoord pos) {
        return getRandomFromList(random, bridgeSelector, pos);
    }

    public String getRandomLargeBridge(Random random, ChunkCoord pos) {
        return getRandomFromList(random, largeBridgeSelector, pos);
    }

    public String getRandomFountain(Random random, ChunkCoord pos) {
        return getRandomFromList(random, fountainSelector, pos);
    }

    public String getRandomBuilding(Random random, ChunkCoord pos) {
        return getRandomFromList(random, buildingSelector, pos);
    }

    public String getRandomMultiBuilding(Random random, ChunkCoord pos) {
        return getRandomFromList(random, multiBuildingSelector, pos);
    }

    public boolean hasMultiBuildings() {
        return !multiBuildingSelector.isEmpty();
    }

    public List<ObjectSelector> getMultiBuildingSelector() {
        return multiBuildingSelector;
    }
}
