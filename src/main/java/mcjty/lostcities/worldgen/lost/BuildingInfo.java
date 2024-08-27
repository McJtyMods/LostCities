package mcjty.lostcities.worldgen.lost;

import mcjty.lostcities.LostCities;
import mcjty.lostcities.api.*;
import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.varia.ChunkCoord;
import mcjty.lostcities.varia.Counter;
import mcjty.lostcities.varia.QualityRandom;
import mcjty.lostcities.varia.Tools;
import mcjty.lostcities.worldgen.ChunkHeightmap;
import mcjty.lostcities.worldgen.IDimensionInfo;
import mcjty.lostcities.worldgen.LostCityTerrainFeature;
import mcjty.lostcities.worldgen.lost.cityassets.*;
import mcjty.lostcities.worldgen.lost.regassets.data.CitySphereSettings;
import mcjty.lostcities.worldgen.lost.regassets.data.PredefinedBuilding;
import mcjty.lostcities.worldgen.lost.regassets.data.PredefinedStreet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

import static mcjty.lostcities.worldgen.LostCityTerrainFeature.FLOORHEIGHT;

public class BuildingInfo implements ILostChunkInfo {

    public final ChunkCoord coord;
    public final IDimensionInfo provider;
    public final LostCityProfile profile;       // Profile cactive for this chunk: can be different in city sphere worlds

    public final boolean outsideChunk;  // Only used for citysphere worlds and when this chunk is outside
    public int groundLevel;
    public final int waterLevel;

    public boolean isCity;
    public boolean hasBuilding;
    public final MultiPos multiBuildingPos;
    public final ILostCityMultiBuilding multiBuilding;
    public ILostCityBuilding buildingType;

    public final BuildingPart fountainType;
    public final BuildingPart parkType;
    public final BuildingPart bridgeType;
    public final BuildingPart stairType;
    public final BuildingPart frontType;
    private final float stairPriority;      // A random number that indicates if this chunk should get a stair if there are competing stairs around it. The highest wins
    public final BuildingPart railDungeon;    // Dungeon next to rails. Will only generate if there are actually rails next to it
    public final StreetType streetType;

    private int floors;
    public int cellars;
    public BuildingPart[] floorTypes;
    public BuildingPart[] floorTypes2;

    public final boolean[] connectionAtX;
    public final boolean[] connectionAtZ;
    public final boolean noLoot;
    public final float ruinHeight;      // The height (as a percentage between 0 and 1) at which we focus the ruin layer. Set to -1 if this building is not ruined

    public final int highwayXLevel;     // 0 or 1 if there is a highway at this chunk
    public final int highwayZLevel;     // 0 or 1 if there is a highway at this chunk
    public final int cityLevel;         // The first floor of buildings starts at groundLevel + cityLevel * 6

    public final boolean xBridge;       // A boolean indicating that this chunk is a candidate for holding a bridge (no guarantee)
    public final boolean zBridge;       // A boolean indicating that this chunk is a candidate for holding a bridge (no guarantee)

    public final boolean xRailCorridor; // A boolean indicating that this chunk is a candidate for holding a corridor (no guarantee)
    public final boolean zRailCorridor; // A boolean indicating that this chunk is a candidate for holding a corridor (no guarantee)

    public final Block doorBlock;

    // Transient info that is calculated on demand
    private BuildingInfo xmin = null;   // @todo remove
    private BuildingInfo xmax = null;   // @todo remove
    private BuildingInfo zmin = null;   // @todo remove
    private BuildingInfo zmax = null;   // @todo remove
    private DamageArea damageArea = null;
    private Palette palette = null;
    private CompiledPalette compiledPalette = null;
    private Boolean isOcean = null;

    private boolean xBridgeTypeCalculated = false;
    private boolean zBridgeTypeCalculated = false;
    private BuildingPart xBridgeType = null;
    private BuildingPart zBridgeType = null;

    private boolean stairsCalculated = false;
    private Direction stairDirection;
    private boolean actualStairsCalculated = false;
    private Direction actualStairDirection;

    private Boolean horizontalMonorail = null;
    private Boolean verticalMonorail = null;

    private MinMax desiredTerrainCorrectionHeights = null;
    private MinMax desiredMaxHeight1 = null;

    // A list of todo's
    private final List<BlockPos> torchTodo = new ArrayList<>();
    private final Map<BlockPos, Runnable> postTodo = new HashMap<>();

    public static class ConditionTodo {
        private final String condition;
        private final String part;
        private final String building;

        public ConditionTodo(String condition, String part, BuildingInfo info) {
            this.part = part == null ? "<none>" : part;
            this.condition = condition;
            if (info.hasBuilding) {
                this.building = info.getBuildingType();
            } else {
                this.building = "<none>";
            }
        }

        public String getCondition() {
            return condition;
        }

        public String getPart() {
            return part;
        }

        public String getBuilding() {
            return building;
        }
    }

    // BuildingInfo cache
    private static final Map<ChunkCoord, BuildingInfo> BUILDING_INFO_MAP = new HashMap<>();
    private static final Map<ChunkCoord, LostChunkCharacteristics> CITY_INFO_MAP = new HashMap<>();
    private static final Map<ChunkCoord, Integer> CITY_LEVEL_CACHE = new HashMap<>();

    public void addTorchTodo(BlockPos index) {
        torchTodo.add(index);
    }

    public List<BlockPos> getTorchTodo() {
        return torchTodo;
    }

    public void clearTorchTodo() {
        torchTodo.clear();
    }

    public void addPostTodo(BlockPos index, Runnable inf) {
        postTodo.put(index, inf);
    }

    public Map<BlockPos, Runnable> getPostTodo() {
        return postTodo;
    }

    public void clearPostTodo() {
        postTodo.clear();
    }

    public BlockPos getCenter(int y) {
        return new BlockPos((coord.chunkX() << 4) + 8, y, (coord.chunkZ() << 4) + 8);
    }

    public BlockPos getRelativePos(int rx, int y, int rz) {
        return new BlockPos((coord.chunkX() << 4) + rx, y, (coord.chunkZ() << 4) + rz);
    }

    public CompiledPalette getCompiledPalette() {
        if (compiledPalette == null) {
            compiledPalette = new CompiledPalette(palette);
            if (hasBuilding) {
                Palette buildingPalette = buildingType.getLocalPalette(provider.getWorld());
                if (buildingPalette != null) {
                    compiledPalette = new CompiledPalette(compiledPalette, buildingPalette);
                }
            }
        }
        return compiledPalette;
    }

    public DamageArea getDamageArea() {
        if (damageArea == null) {
            damageArea = new DamageArea(coord.chunkX(), coord.chunkZ(), provider, this);
        }
        return damageArea;
    }

    public Style getOutsideStyle() {
        return AssetRegistries.STYLES.get(provider.getWorld(), provider.getWorldStyle().getOutsideStyle());
    }

    private void createPalette(Random rand) {
        Style style;
        if (!isCity) {
            style = getOutsideStyle();
        } else {
            String name = getCityStyle().getStyle();
            style = AssetRegistries.STYLES.getOrThrow(provider.getWorld(), name);
        }
        palette = style.getRandomPalette(provider, rand);
    }

    public BuildingInfo getXmin() {
        if (xmin == null) {
            xmin = getBuildingInfo(coord.west(), provider);
        }
        return xmin;
    }

    public BuildingInfo getXmax() {
        if (xmax == null) {
            xmax = getBuildingInfo(coord.east(), provider);
        }
        return xmax;
    }

    public BuildingInfo getZmin() {
        if (zmin == null) {
            zmin = getBuildingInfo(coord.north(), provider);
        }
        return zmin;
    }

    public BuildingInfo getZmax() {
        if (zmax == null) {
            zmax = getBuildingInfo(coord.south(), provider);
        }
        return zmax;
    }

    public int getMaxHeight() {
        if (hasBuilding) {
            return getCityGroundLevel() + floors * FLOORHEIGHT;
        } else {
            int m = getMaxHighwayLevel();
            if (m >= 0) {
                return groundLevel + m * FLOORHEIGHT;
            } else {
                return getCityGroundLevel();
            }
        }
    }

    public int getCityGroundLevel() {
        return groundLevel + cityLevel * FLOORHEIGHT;
    }

    /**
     * Get the city ground level but lower the level outside cities
     */
    public int getCityGroundLevelOutsideLower() {
        if (isCity) {
            return groundLevel + cityLevel * FLOORHEIGHT;
        } else {
            return groundLevel + cityLevel * FLOORHEIGHT - 1;
        }
    }

    public boolean isValidFloor(int l) {
        return (l + cellars) >= 0 && (l + cellars) < floorTypes.length;
    }

    public BuildingPart getFloor(int l) {
        return floorTypes[l + cellars];
    }

    public BuildingPart getFloorPart2(int l) {
        return floorTypes2[l + cellars];
    }

    public ILostCityBuilding getBuilding() {
        return buildingType;
    }

    public CityStyle getCityStyle() {
        return (CityStyle) getChunkCharacteristics(coord, provider).cityStyle;
    }

    // Version for usage inside the gui
    public static boolean hasBuildingGui(int chunkX, int chunkZ, IDimensionInfo provider, LostChunkCharacteristics characteristics) {
//        Random rand = getBuildingRandom(chunkX, chunkZ, provider.getSeed());
//        rand.nextFloat();       // Compatibility?

        return characteristics.couldHaveBuilding;
    }

    public static synchronized LostChunkCharacteristics getChunkCharacteristicsGui(ChunkCoord key, IDimensionInfo provider) {
        if (CITY_INFO_MAP.containsKey(key)) {
            return CITY_INFO_MAP.get(key);
        } else {
            int chunkX = key.chunkX();
            int chunkZ = key.chunkZ();
            LostCityProfile profile = getProfile(key, provider);
            LostChunkCharacteristics characteristics = new LostChunkCharacteristics();

            characteristics.isCity = isCityRaw(key, provider, profile);
            characteristics.cityLevel = getCityLevel(key, provider);
            Random rand = getBuildingRandom(chunkX, chunkZ, provider.getSeed());
            characteristics.couldHaveBuilding = characteristics.isCity && rand.nextFloat() < profile.BUILDING_CHANCE;
            CITY_INFO_MAP.put(key, characteristics);
            return characteristics;
        }
    }

    public static synchronized LostChunkCharacteristics getChunkCharacteristics(ChunkCoord coord, IDimensionInfo provider) {
        if (CITY_INFO_MAP.containsKey(coord)) {
            return CITY_INFO_MAP.get(coord);
        } else {
            int chunkX = coord.chunkX();
            int chunkZ = coord.chunkZ();
            LostCityProfile profile = getProfile(coord, provider);
            LostChunkCharacteristics characteristics = new LostChunkCharacteristics();

            WorldGenLevel world = provider.getWorld();
            characteristics.isCity = isCityRaw(coord, provider, profile);

            if (!characteristics.isCity) {
                characteristics.multiPos = MultiPos.SINGLE;
                characteristics.multiBuilding = null;
            } else {
                initMultiBuildingSection(characteristics, coord, provider, profile);
            }

            if (characteristics.multiPos.isSingle()) {
                characteristics.cityLevel = getCityLevel(coord, provider);
            } else {
                characteristics.cityLevel = getAverageCityLevel(characteristics, coord, provider);
            }
            Random rand = getBuildingRandom(chunkX, chunkZ, provider.getSeed());
            characteristics.couldHaveBuilding = characteristics.isCity && checkBuildingPossibility(coord, provider, profile, characteristics.multiPos, characteristics.cityLevel, rand);
            if ((profile.isSpace() || profile.isSpheres()) && characteristics.multiPos.isSingle()) {
                // Minimize cities at the edge of the city in an orb
                float dist = CitySphere.getRelativeDistanceToCityCenter(coord, provider);
                if (dist > .7f) {
                    characteristics.couldHaveBuilding = false;
                }
            }

            CityStyle cityStyle;
            // If this is a street we find other chunks connected to this and pick the cityStyle
            // that represents the majority. This is to prevent streets from switching style randomly if two
            // different styled cities mix
            if (characteristics.isCity && !characteristics.couldHaveBuilding) {
                Counter<String> counter = new Counter<>();
                for (int cx = -1; cx <= 1; cx++) {
                    for (int cz = -1; cz <= 1; cz++) {
                        ChunkCoord key = coord.offset(cx, cz);
                        cityStyle = City.getCityStyle(key, provider, profile);
                        counter.add(cityStyle.getName());
                        if (cx == 0 && cz == 0) {
                            counter.add(cityStyle.getName());   // Add this chunk again for a bias
                        }
                    }
                }
                cityStyle = AssetRegistries.CITYSTYLES.get(world, counter.getMostOccuring());
            } else {
                cityStyle = City.getCityStyle(coord, provider, profile);
            }
            characteristics.cityStyle = cityStyle;

            if (characteristics.multiPos.isMulti() && !characteristics.multiPos.isTopLeft()) {
                LostChunkCharacteristics topleft = getTopLeftCityInfo(characteristics, coord, provider);
//                characteristics.multiBuilding = topleft.multiBuilding;
                if (characteristics.multiBuilding != null) {
                    String b = characteristics.multiBuilding.getBuilding(characteristics.multiPos.x(), characteristics.multiPos.z());
                    characteristics.buildingType = AssetRegistries.BUILDINGS.getOrThrow(world, b);
                } else {
                    // @todo is this even possible?
                    characteristics.buildingType = topleft.buildingType;
                    if (characteristics.buildingType == null) {
                        throw new RuntimeException("Topleft building type is not set!");
                    }
                }
            } else {
                PredefinedBuilding predefinedBuilding = City.getPredefinedBuildingAtTopLeft(coord);
                if (characteristics.multiPos.isTopLeft()) {
//                    String name = cityStyle.getRandomMultiBuilding(rand);
//                    if (predefinedBuilding != null) {
//                        name = predefinedBuilding.building();
//                    }
//                    characteristics.multiBuilding = AssetRegistries.MULTI_BUILDINGS.get(world, name);
                    String b = characteristics.multiBuilding.getBuilding(0, 0);
                    characteristics.buildingType = AssetRegistries.BUILDINGS.getOrThrow(world, b);
                } else {
//                    characteristics.multiBuilding = null;
                    String name = cityStyle.getRandomBuilding(rand);
                    if (predefinedBuilding != null) {
                        name = predefinedBuilding.building();
                    }
                    if (name == null) {
                        throw new RuntimeException("Invalid building for multibuilding!");
                    }
                    characteristics.buildingType = AssetRegistries.BUILDINGS.getOrThrow(world, name);
                }
            }

            LostCityEvent.CharacteristicsEvent event = new LostCityEvent.CharacteristicsEvent(world, LostCities.lostCitiesImp,
                    chunkX, chunkZ, characteristics);
            MinecraftForge.EVENT_BUS.post(event);

            CITY_INFO_MAP.put(coord, characteristics);
            return characteristics;
        }
    }

    // Change city status
    public static void setCityRaw(ChunkCoord coord, IDimensionInfo provider, boolean isCity) {
        LostChunkCharacteristics characteristics = getChunkCharacteristics(coord, provider);
        characteristics.isCity = isCity;
    }

    /**
     * Don't use the cache as we're busy building the cache.
     */
    public static boolean isCityRaw(ChunkCoord coord, IDimensionInfo provider, LostCityProfile profile) {
        if (isVoidChunk(coord, provider)) {
            // If we have a void chunk then no city here
            return false;
        } else if (provider.getProfile().isSpace() || provider.getProfile().isSpheres()) {
            if (CitySphere.onCitySphereBorder(coord, provider)) {
                return false;
            } else if (CitySphere.hasMonorailStation(coord, provider)) {
                return false;
            }
        }

        float cityFactor = City.getCityFactor(coord, provider, profile);
        return cityFactor > profile.CITY_THRESHOLD;
    }

    public static boolean isCity(ChunkCoord coord, IDimensionInfo provider) {
        return getChunkCharacteristics(coord, provider).isCity;
    }

    private static boolean checkBuildingPossibility(ChunkCoord coord, IDimensionInfo provider, LostCityProfile profile, MultiPos section, int cityLevel, Random rand) {
        boolean b;
        float bc = rand.nextFloat();
        ResourceKey<Level> type = provider.getType();

        PredefinedBuilding predefinedBuilding = City.getPredefinedBuildingAtTopLeft(coord);
        if (predefinedBuilding != null) {
            return true;    // We don't need other tests
        }
        PredefinedStreet predefinedStreet = City.getPredefinedStreet(coord);
        if (predefinedStreet != null) {
            return false;   // No building here
        }

        CityStyle style = City.getCityStyle(coord, provider, profile);
        float buildingChance = profile.BUILDING_CHANCE;
        if (style.getBuildingChance() != null) {
            buildingChance = style.getBuildingChance();
        }

        if (section.isMulti()) {
            // Part of multi-building. We have checked everything above
            b = true;
        } else if (bc >= buildingChance) {
            // Random says we should have no building here
            b = false;
        } else if (hasHighway(coord, provider, profile)) {
            // We are above a highway. Check if we have room for a building
            int maxh = Math.max(Highway.getXHighwayLevel(coord, provider, profile), Highway.getZHighwayLevel(coord, provider, profile));
            b = cityLevel > maxh + 1;       // Allow a building if it is higher than the maximum highway + one
            // Later we will take care to make sure we don't have too many cellars
            // Note that for easy of coding we still disallow multi-buildings above highways
        } else if (hasRailway(coord, provider, profile)) {
            // We are above a railway. Check if we have room for a building
            Railway.RailChunkInfo info = Railway.getRailChunkType(coord, provider, profile);
            if (info.getType() == RailChunkType.STATION_UNDERGROUND) {
                b = false;  // No building directly above the underground station
            } else {
                int maxh = info.getLevel();
                b = cityLevel > maxh + 1;       // Allow a building if it is higher then the maximum railway + one
                // Later we will take care to make sure we don't have too many cellars
                // Note that for easy of coding we still disallow multi-buildings above railways
            }
        } else {
            // General case
            b = true;
        }
        return b;
    }

    /**
     * Initialize the chunk characteristics with the multi building information
     */
    private static void initMultiBuildingSection(LostChunkCharacteristics characteristics, ChunkCoord coord, IDimensionInfo provider, LostCityProfile profile) {
        // If a chunk is occupied according to City then there is a predefined building or street here.
        // Try to look for it
        if (City.isChunkOccupied(provider, coord)) {
            City.PreDefBuildingOffset predefinedBuilding = City.getPredefinedBuilding(provider, coord);
            if (predefinedBuilding != null) {
                if (predefinedBuilding.building().multi()) {
                    MultiBuilding building = AssetRegistries.MULTI_BUILDINGS.getOrThrow(provider.getWorld(), predefinedBuilding.building().building());
                    characteristics.multiPos = new MultiPos(predefinedBuilding.offsetX(), predefinedBuilding.offsetZ(), building.getDimX(), building.getDimZ());
                    characteristics.multiBuilding = building;
                    return;
                }
            }
            characteristics.multiPos = MultiPos.SINGLE;
            characteristics.multiBuilding = null;
            return;
        }

        MultiChunk multiChunk = MultiChunk.getOrCreate(provider, coord);
        MultiChunk.MB multiBuilding = multiChunk.getMultiBuilding(coord);
        if (multiBuilding == null) {
            characteristics.multiPos = MultiPos.SINGLE;
            characteristics.multiBuilding = null;
            return;
        }

        MultiBuilding building = AssetRegistries.MULTI_BUILDINGS.getOrThrow(provider.getWorld(), multiBuilding.name());
        characteristics.multiPos = new MultiPos(multiBuilding.offsetX(), multiBuilding.offsetZ(), building.getDimX(), building.getDimZ());
        characteristics.multiBuilding = building;
    }

    private BuildingInfo calculateTopLeft() {
        if (multiBuildingPos.isTopLeft()) {
            return this;
        }
        ChunkCoord key = coord.offset(-multiBuildingPos.x(), -multiBuildingPos.z());
        return getBuildingInfo(key, provider);
    }

    private static int getAverageCityLevel(LostChunkCharacteristics thisone, ChunkCoord coord, IDimensionInfo provider) {
        int level = 0;
        MultiPos mp = thisone.multiPos;
        int topX = coord.chunkX() - mp.x();
        int topZ = coord.chunkZ() - mp.z();
        for (int x = 0; x < mp.w(); x++) {
            for (int z = 0; z < mp.h(); z++) {
                ChunkCoord key = new ChunkCoord(provider.dimension(), topX + x, topZ + z);
                level += getCityLevel(key, provider);
            }
        }
        return level / (mp.w() * mp.h());
    }

    private static LostChunkCharacteristics getTopLeftCityInfo(LostChunkCharacteristics thisone, ChunkCoord coord, IDimensionInfo provider) {
        if (thisone.multiPos.isTopLeft()) {
            return thisone;
        }
        ChunkCoord key = coord.offset(-thisone.multiPos.x(), -thisone.multiPos.z());
        return getChunkCharacteristics(key, provider);
    }

    public static boolean hasHighway(ChunkCoord coord, IDimensionInfo provider, LostCityProfile profile) {
        return Highway.getXHighwayLevel(coord, provider, profile) >= 0 || Highway.getZHighwayLevel(coord, provider, profile) >= 0;
    }

    public static boolean hasRailway(ChunkCoord coord, IDimensionInfo provider, LostCityProfile profile) {
        return Railway.getRailChunkType(coord, provider, profile).getType() != RailChunkType.NONE;
    }

    public static boolean hasRailwayAtSurface(ChunkCoord coord, IDimensionInfo provider, LostCityProfile profile) {
        return Railway.getRailChunkType(coord, provider, profile).getType().isSurface();
    }

    public Railway.RailChunkInfo getRailInfo() {
        return Railway.getRailChunkType(coord, provider, profile);
    }

    // Return true if a highway at this level would be a tunnel
    public boolean isTunnel(int level) {
        if (isCity) {
            // We need a tunnel if the city goes above this level
            return cityLevel > level;
        }

        // Get the (possbily cached) heightmap for this chunk
        ChunkHeightmap heightmap = provider.getHeightmap(coord);
        // The height at which the highway would be + a threshold of 3
        int highwayHeight = groundLevel + level * FLOORHEIGHT + 3;
        // If there are many places in the chunk above this height we will need a tunnel
        return heightmap.getHeight() > highwayHeight;
    }

    public static void cleanCache() {
        BUILDING_INFO_MAP.clear();
        CITY_INFO_MAP.clear();
        CITY_LEVEL_CACHE.clear();
    }

    public static synchronized BuildingInfo getBuildingInfo(ChunkCoord key, IDimensionInfo provider) {
        if (BUILDING_INFO_MAP.containsKey(key)) {
            return BUILDING_INFO_MAP.get(key);
        }
        BuildingInfo info = new BuildingInfo(key, provider);
        BUILDING_INFO_MAP.put(key, info);
        return info;
    }

    /**
     * Find the correct profile for this chunk. This takes space sphere worlds into account
     */
    public static LostCityProfile getProfile(ChunkCoord coord, IDimensionInfo provider) {
        if ((provider.getProfile().isSpace() || provider.getProfile().isSpheres())) {
            if (CitySphere.intersectsWithCitySphere(coord, provider)) {
                return provider.getProfile();
            } else {
                return provider.getOutsideProfile();
            }
        } else {
            return provider.getProfile();
        }
    }

    // Only used for editing!
    public void setBuildingType(Building building, int cellars, int floors, int groundLevel) {
        buildingType = building;
        hasBuilding = true;
        this.floors = floors;
        this.cellars = cellars;
        this.groundLevel = groundLevel;

        floorTypes = new BuildingPart[floors + cellars + 1];
        floorTypes2 = new BuildingPart[floors + cellars + 1];

        Random rand = getBuildingRandom(coord.chunkX(), coord.chunkZ(), provider.getSeed());

        for (int i = 0; i <= floors + cellars; i++) {
            ConditionContext conditionContext = new ConditionContext(cityLevel + i - cellars, i - cellars, cellars, floors, "<none>", building.getName(), coord) {
                @Override
                public boolean isBuilding() {
                    return true;
                }

                @Override
                public boolean isSphere() {
                    return CitySphere.isInSphere(coord, getCenter(0), provider);
                }

                @Override
                public ResourceLocation getBiome() {
                    Holder<Biome> biome = provider.getWorld().getBiome(getCenter(0));
                    return biome.unwrap().map(ResourceKey::location, b -> provider.getWorld().registryAccess().registry(Registries.BIOME).orElseThrow().getKey(b));
                }
            };
            String randomPart = building.getRandomPart(rand, conditionContext);
            floorTypes[i] = AssetRegistries.PARTS.getOrThrow(provider.getWorld(), randomPart);
            randomPart = building.getRandomPart2(rand, conditionContext);
            floorTypes2[i] = AssetRegistries.PARTS.get(provider.getWorld(), randomPart);    // null is legal
        }
    }

    private BuildingInfo(ChunkCoord key, IDimensionInfo provider) {
        this.provider = provider;
        this.coord = key;

        outsideChunk = (provider.getProfile().isSpace() || provider.getProfile().isSpheres()) && !CitySphere.intersectsWithCitySphere(key, provider);
        profile = getProfile(key, provider);

        LostChunkCharacteristics characteristics = getChunkCharacteristics(key, provider);

        cityLevel = characteristics.cityLevel;
        buildingType = characteristics.buildingType;
        multiBuilding = characteristics.multiBuilding;
        multiBuildingPos = characteristics.multiPos;

        Random rand = getBuildingRandom(coord.chunkX(), coord.chunkZ(), provider.getSeed());

        boolean b = characteristics.couldHaveBuilding;
        if (b && multiBuildingPos.isSingle()) {
            if (rand.nextFloat() < getChunkCharacteristics(coord.west(), provider).buildingType.getPrefersLonely()) {
                b = false;
            } else if (rand.nextFloat() < getChunkCharacteristics(coord.east(), provider).buildingType.getPrefersLonely()) {
                b = false;
            } else if (rand.nextFloat() < getChunkCharacteristics(coord.north(), provider).buildingType.getPrefersLonely()) {
                b = false;
            } else if (rand.nextFloat() < getChunkCharacteristics(coord.south(), provider).buildingType.getPrefersLonely()) {
                b = false;
            }
        }

        boolean c = characteristics.isCity;
        if ((provider.getProfile().isSpace() || provider.getProfile().isSpheres()) && CitySphere.isCitySphereCenter(coord, provider)) {
            CitySphereSettings settings = provider.getWorldStyle().getCitysphereSettings();
            if (settings != null) {
                CitySphereSettings.CitySphereCenterType centertype = settings.getCenterType();
                switch (centertype) {
                    case DEFAULT -> {
                    }
                    case STREET -> {
                        c = true;
                        b = false;
                    }
                    case BUILDING -> {
                        c = true;
                        b = true;
                    }
                    case NORMAL -> {
                        c = false;
                    }
                }
            }
        }

        isCity = c;
        hasBuilding = b;

        int wl;
        if (outsideChunk) {
            groundLevel = provider.getOutsideProfile().GROUNDLEVEL;
            wl = provider.getOutsideProfile().SEALEVEL;
        } else {
            groundLevel = provider.getProfile().GROUNDLEVEL;
            wl = provider.getProfile().SEALEVEL;
        }
        waterLevel = wl == -1 ? Tools.getSeaLevel(provider.getWorld()) : wl;

        CityStyle cs = (CityStyle) characteristics.cityStyle;

        // In a multi building we copy all information from the top-left chunk
        if (multiBuildingPos.isMulti() && !multiBuildingPos.isTopLeft()) {
            BuildingInfo topleft = calculateTopLeft();
            highwayXLevel = topleft.highwayXLevel;
            highwayZLevel = topleft.highwayZLevel;
            streetType = topleft.streetType;
            fountainType = topleft.fountainType;
            parkType = topleft.parkType;
            floors = topleft.floors;
            cellars = topleft.cellars;
            doorBlock = topleft.doorBlock;
            bridgeType = topleft.bridgeType;
            stairType = topleft.stairType;
            stairPriority = topleft.stairPriority;
            palette = topleft.palette;
            compiledPalette = topleft.getCompiledPalette();
            noLoot = topleft.noLoot;
            ruinHeight = topleft.ruinHeight;
        } else {
            PredefinedBuilding predefinedBuilding = City.getPredefinedBuildingAtTopLeft(key);
            highwayXLevel = Highway.getXHighwayLevel(key, provider, profile);
            highwayZLevel = Highway.getZHighwayLevel(key, provider, profile);

            if (rand.nextDouble() < profile.PARK_CHANCE) {
                streetType = StreetType.values()[rand.nextInt(StreetType.values().length)];
            } else {
                streetType = StreetType.NORMAL;
            }
            if (rand.nextFloat() < profile.FOUNTAIN_CHANCE) {
                fountainType = AssetRegistries.PARTS.getOrThrow(provider.getWorld(), cs.getRandomFountain(rand));
            } else {
                fountainType = null;
            }
            parkType = AssetRegistries.PARTS.getOrThrow(provider.getWorld(), cs.getRandomPark(rand));
            float cityFactor = City.getCityFactor(coord, provider, profile);

            int maxfloors = getMaxfloors(cs);
            int f = profile.BUILDING_MINFLOORS + rand.nextInt((int) (profile.BUILDING_MINFLOORS_CHANCE + (cityFactor + .1f) * (profile.BUILDING_MAXFLOORS_CHANCE - profile.BUILDING_MINFLOORS_CHANCE)));
            f++;
            if (f > maxfloors + 1) {
                f = maxfloors + 1;
            }
            int minfloors = getMinfloors(cs);
            if (f < minfloors) {
                f = minfloors;
            }

            if ((provider.getProfile().isSpace() || provider.getProfile().isSpheres()) && CitySphere.intersectsWithCitySphere(key, provider)) {
                float reldest = CitySphere.getRelativeDistanceToCityCenter(coord, provider);
                if (reldest > .6f) {
                    f = Math.max(minfloors, f - 2);
                } else if (reldest > .5f) {
                    f = Math.max(minfloors, f - 1);
                }
            }

            int max = provider.getWorld().getMaxBuildHeight() - 2 - FLOORHEIGHT;
            while (getCityGroundLevel() + f * FLOORHEIGHT >= max) {
                f--;
            }
            floors = f;

            int maxcellars = getMaxcellars(cs);
            int mincellars = Math.max(profile.BUILDING_MINCELLARS, buildingType.getMinCellars());
            int fb = mincellars + ((maxcellars <= 0) ? 0 : rand.nextInt(maxcellars + 1));
            if (getMaxHighwayLevel() >= 0) {
                // If we are above a highway we make sure we can't have too many cellars
                fb = Math.min(cityLevel - getMaxHighwayLevel() - 1, fb);
                if (fb < 0) {
                    fb = 0;
                }
            }
            if (fb > maxcellars) {
                fb = maxcellars;
            }
            cellars = fb;

            doorBlock = getRandomDoor(rand);
            bridgeType = AssetRegistries.PARTS.getOrThrow(provider.getWorld(), cs.getRandomBridge(rand));
            stairType = AssetRegistries.PARTS.getOrThrow(provider.getWorld(), cs.getRandomStair(rand));
            stairPriority = rand.nextFloat();
            createPalette(rand);
            float r = rand.nextFloat();
            noLoot = multiBuildingPos.isSingle() && r < profile.BUILDING_WITHOUT_LOOT_CHANCE;
            r = rand.nextFloat();
            if (rand.nextFloat() < profile.RUIN_CHANCE && (predefinedBuilding == null || !predefinedBuilding.preventRuins())) {
                ruinHeight = profile.RUIN_MINLEVEL_PERCENT + (profile.RUIN_MAXLEVEL_PERCENT - profile.RUIN_MINLEVEL_PERCENT) * r;
            } else {
                ruinHeight = -1;
            }
        }

        floorTypes = new BuildingPart[floors + cellars + 1];
        floorTypes2 = new BuildingPart[floors + cellars + 1];

        connectionAtX = new boolean[floors + cellars + 1];
        connectionAtZ = new boolean[floors + cellars + 1];
        Building building = (Building) getBuilding();
        for (int i = 0; i <= floors + cellars; i++) {
            ConditionContext conditionContext = new ConditionContext(cityLevel + i - cellars, i - cellars, cellars, floors, "<none>", building.getName(), coord) {
                @Override
                public boolean isBuilding() {
                    return true;
                }

                @Override
                public boolean isSphere() {
                    return CitySphere.isInSphere(coord, getCenter(0), provider);
                }

                @Override
                public ResourceLocation getBiome() {
                    Holder<Biome> biome = provider.getWorld().getBiome(getCenter(0));
                    return biome.unwrap().map(ResourceKey::location, b -> provider.getWorld().registryAccess().registry(Registries.BIOME).orElseThrow().getKey(b));
                }
            };
            String randomPart = building.getRandomPart(rand, conditionContext);
            if (randomPart == null) {
                throw new RuntimeException("Misconfiguration! Floor were generated for a building where no part condition matches!");
            }
            floorTypes[i] = AssetRegistries.PARTS.getOrThrow(provider.getWorld(), randomPart);
            randomPart = building.getRandomPart2(rand, conditionContext);
            floorTypes2[i] = AssetRegistries.PARTS.get(provider.getWorld(), randomPart);    // null is legal
            connectionAtX[i] = isCity(coord.west(), provider) && (rand.nextFloat() < profile.BUILDING_DOORWAYCHANCE);
            connectionAtZ[i] = isCity(coord.north(), provider) && (rand.nextFloat() < profile.BUILDING_DOORWAYCHANCE);
        }

        if (hasBuilding && cellars > 0) {
            xRailCorridor = false;
            zRailCorridor = false;
        } else {
            xRailCorridor = rand.nextFloat() < profile.CORRIDOR_CHANCE;
            zRailCorridor = rand.nextFloat() < profile.CORRIDOR_CHANCE;
        }

        if (isCity) {
            xBridge = false;
            zBridge = false;
        } else {
            xBridge = rand.nextFloat() < profile.BRIDGE_CHANCE;
            zBridge = rand.nextFloat() < profile.BRIDGE_CHANCE;
        }

        if (rand.nextFloat() < profile.RAILWAY_DUNGEON_CHANCE) {
            if (!hasBuilding || (Railway.RAILWAY_LEVEL_OFFSET < (cityLevel - cellars))) {
                railDungeon = AssetRegistries.PARTS.getOrThrow(provider.getWorld(), getCityStyle().getRandomRailDungeon(rand));
            } else {
                railDungeon = null;
            }
        } else {
            railDungeon = null;
        }

        if (rand.nextFloat() < profile.BUILDING_FRONTCHANCE) {
            frontType = AssetRegistries.PARTS.getOrThrow(provider.getWorld(), getCityStyle().getRandomFront(rand));
        } else {
            frontType = null;
        }
    }

    public boolean hasHorizontalMonorail() {
        if (horizontalMonorail == null) {
            horizontalMonorail = CitySphere.hasHorizontalMonorail(coord, provider);
        }
        return horizontalMonorail;
    }

    public boolean hasVerticalMonorail() {
        if (verticalMonorail == null) {
            verticalMonorail = CitySphere.hasVerticalMonorail(coord, provider);
        }
        return verticalMonorail;
    }

    public boolean hasMonorail() {
        return hasHorizontalMonorail() || hasVerticalMonorail();
    }

    private int getMaxcellars(CityStyle cs) {
        int maxcellars = profile.BUILDING_MAXCELLARS + cityLevel;
        if (buildingType.getMaxCellars() != -1) {
            maxcellars = Math.min(maxcellars, buildingType.getMaxCellars());
        }
        if (buildingType.getMinCellars() != -1) {
            maxcellars = Math.max(maxcellars, buildingType.getMinCellars());
        }
        if (cs.getMaxCellarCount() != null) {
            maxcellars = Math.min(maxcellars, cs.getMaxCellarCount());
        }
        if (cs.getMinCellarCount() != null) {
            maxcellars = Math.max(maxcellars, cs.getMinCellarCount());
        }
        return maxcellars;
    }

    private int getMinfloors(CityStyle cs) {
        int minfloors = profile.BUILDING_MINFLOORS + 1;    // +1 because this doesn't count the top
        if (buildingType.getMinFloors() != -1) {
            minfloors = Math.max(minfloors, buildingType.getMinFloors());
        }
        if (cs.getMinFloorCount() != null) {
            minfloors = Math.max(minfloors, cs.getMinFloorCount());
        }
        return minfloors;
    }

    private int getMaxfloors(CityStyle cs) {
        int maxfloors = profile.BUILDING_MAXFLOORS;
        if (buildingType.getMaxFloors() != -1) {
            maxfloors = Math.min(maxfloors, buildingType.getMaxFloors());
        }
        if (cs.getMaxFloorCount() != null) {
            maxfloors = Math.min(maxfloors, cs.getMaxFloorCount());
        }
        return maxfloors;
    }

    public Boolean getAllowDoors() {
        return buildingType.getAllowDoors();
    }

    public Boolean getAllowFillers() {
        return buildingType.getAllowFillers();
    }

    public int getHighwayXLevel() {
        return Highway.getXHighwayLevel(coord, provider, profile);
    }

    public int getHighwayZLevel() {
        return Highway.getZHighwayLevel(coord, provider, profile);
    }


    /**
     * Return true if this is a void chunk (only for floating island worldtype). This does
     * not use the cache so it is safe to use when the cache is building
     */
    public static boolean isVoidChunk(ChunkCoord coord, IDimensionInfo provider) {
        if (provider.getProfile().isFloating()) {
            return provider.getHeightmap(coord).getHeight() <= 0;
        } else {
            return false;
        }
    }


    /**
     * This function does not use the cache. So safe to use when the cache is building
     * This function uses its own cache.
     */
    public static synchronized int getCityLevel(ChunkCoord key, IDimensionInfo provider) {
        if (CITY_LEVEL_CACHE.containsKey(key)) {
            return CITY_LEVEL_CACHE.get(key);
        }
        int result;
        if ((provider.getProfile().isSpace() || provider.getProfile().isSpheres())) {
            result = getCityLevelSpace(key, provider);
        } else if (provider.getProfile().isFloating()) {
            result = getCityLevelFloating(key, provider);
        } else if (provider.getProfile().isCavern()) {
            result =  getCityLevelCavern(key, provider);
        } else {
            result = getCityLevelNormal(key, provider, provider.getProfile());
        }
        CITY_LEVEL_CACHE.put(key, result);
        return result;
    }

    private static int getCityLevelCavern(ChunkCoord coord, IDimensionInfo provider) {
        // @todo for now
        return getCityLevelFloating(coord, provider);
    }


    private static int getCityLevelSpace(ChunkCoord coord, IDimensionInfo provider) {
        if (CitySphere.intersectsWithCitySphere(coord, provider)) {
            // In the sphere
            int chunkX = coord.chunkX();
            int chunkZ = coord.chunkZ();
            float dist = CitySphere.getRelativeDistanceToCityCenter(coord, provider);
            Random rand = new Random(provider.getSeed() + chunkZ * 817505771L + chunkX * 217645177L);
            if (dist < .3f) {
                return 2 + rand.nextInt(2);
            } else if (dist < .4f) {
                return 1 + rand.nextInt(2);
            } else if (dist < .6f) {
                return rand.nextInt(2);
            } else {
                return 0;
            }
        } else {
            return getCityLevelNormal(coord, provider, provider.getOutsideProfile());
        }
    }

    private static int getCityLevelNormal(ChunkCoord coord, IDimensionInfo provider, LostCityProfile profile) {
        ChunkHeightmap heightmap = provider.getHeightmap(coord);
        int height = heightmap.getHeight();
        return getLevelBasedOnHeight(height, profile);
    }

    private static int getCityLevelFloating(ChunkCoord coord, IDimensionInfo provider) {
        int h = provider.getHeightmap(coord).getHeight();
        return getLevelBasedOnHeight(h, provider.getProfile());
    }

    private static int getLevelBasedOnHeight(int height, LostCityProfile profile) {
        if (height < profile.CITY_LEVEL0_HEIGHT) {
            return 0;
        } else if (height < profile.CITY_LEVEL1_HEIGHT) {
            return 1;
        } else if (height < profile.CITY_LEVEL2_HEIGHT) {
            return 2;
        } else if (height < profile.CITY_LEVEL3_HEIGHT) {
            return 3;
        } else {
            return 4;
        }
    }

    private Block getRandomDoor(Random rand) {
        return switch (rand.nextInt(7)) {
            case 0 -> Blocks.BIRCH_DOOR;
            case 1 -> Blocks.ACACIA_DOOR;
            case 2 -> Blocks.DARK_OAK_DOOR;
            case 3 -> Blocks.SPRUCE_DOOR;
            case 4 -> Blocks.OAK_DOOR;
            case 5 -> Blocks.JUNGLE_DOOR;
            case 6 -> Blocks.IRON_DOOR;
            default -> Blocks.OAK_DOOR;
        };
    }

    public boolean isStreetSection() {
        return isCity && !hasBuilding;
    }

    public boolean isElevatedParkSection() {
        if (!isStreetSection()) {
            return false;
        }
        if (!getXmin().isStreetSection()) {
            return false;
        }
        if (!getXmax().isStreetSection()) {
            return false;
        }
        if (!getZmin().isStreetSection()) {
            return false;
        }
        if (!getZmax().isStreetSection()) {
            return false;
        }
        int cnt = 0;
        cnt += getXmin().getZmin().isStreetSection() ? 1 : 0;
        cnt += getXmin().getZmax().isStreetSection() ? 1 : 0;
        cnt += getXmax().getZmin().isStreetSection() ? 1 : 0;
        cnt += getXmax().getZmax().isStreetSection() ? 1 : 0;
        return cnt >= 3;
    }

    private Direction getStairDirection() {
        if (!stairsCalculated) {
            stairsCalculated = true;
            if (streetType != StreetType.PARK && !hasBuilding && isCity) {
                if (cityLevel == getXmin().cityLevel - 1 && !getXmin().hasBuilding && getXmin().isCity) {
                    stairDirection = Direction.XMIN;
                } else if (cityLevel == getXmax().cityLevel - 1 && !getXmax().hasBuilding && getXmax().isCity) {
                    stairDirection = Direction.XMAX;
                } else if (cityLevel == getZmin().cityLevel - 1 && !getZmin().hasBuilding && getZmin().isCity) {
                    stairDirection = Direction.ZMIN;
                } else if (cityLevel == getZmax().cityLevel - 1 && !getZmax().hasBuilding && getZmax().isCity) {
                    stairDirection = Direction.ZMAX;
                } else {
                    stairDirection = null;
                }
            } else {
                stairDirection = null;
            }
        }
        return stairDirection;
    }

    // This returns the actual stair direction. It keeps track if there are stair chunks around
    // it those have higher stair priority
    public Direction getActualStairDirection() {
        if (!actualStairsCalculated) {
            actualStairsCalculated = true;
            actualStairDirection = getStairDirection();
            if (actualStairDirection != null) {
                for (int cx = -1; cx <= 1; cx++) {
                    for (int cz = -1; cz <= 1; cz++) {
                        if (cx != 0 || cz != 0) {
                            ChunkCoord key = coord.offset(cx, cz);
                            BuildingInfo adjacent = getBuildingInfo(key, provider);
                            if (adjacent.getStairDirection() != null && adjacent.stairPriority > stairPriority) {
                                actualStairDirection = null;
                                break;
                            }
                        }
                    }
                }
            }
        }
        return actualStairDirection;
    }


    public BuildingPart hasBridge(IDimensionInfo provider, Orientation orientation) {
        return switch (orientation) {
            case X -> hasXBridge(provider);
            case Z -> hasZBridge(provider);
        };
    }

    public boolean hasBridge(IDimensionInfo provider) {
        if (hasXBridge(provider) != null) {
            return true;
        }
        if (hasZBridge(provider) != null) {
            return true;
        }
        return false;
    }

    // To prevent adjacent bridges of the same direction we give the bridges at even chunk Z coordinates higher priority
    public BuildingPart hasXBridge(IDimensionInfo provider) {
        if (xBridgeTypeCalculated) {
            return xBridgeType;
        }
        xBridgeTypeCalculated = true;
        xBridgeType = null;

        if (!xBridge) {
            return null;
        }
        if (!isSuitableForBridge(provider, this)) {
            return null;
        }
        if (coord.chunkZ() % 2 != 0 && (getZmin().hasXBridge(provider) != null || getZmax().hasXBridge(provider) != null)) {
            return null;
        }
        BuildingPart bt = bridgeType;
        BuildingInfo i = getXmin();
        while ((!i.isCity) && i.xBridge && isSuitableForBridge(provider, i)) {
            if (coord.chunkZ() % 2 != 0 && (i.getZmin().hasXBridge(provider) != null || i.getZmax().hasXBridge(provider) != null)) {
                return null;
            }
            bt = i.bridgeType;
            i = i.getXmin();
        }
        if ((!i.isCity) || i.hasBuilding || i.cityLevel > 0) {  // @todo support bridges at higher levels?
            return null;
        }

        BuildingInfo minimum = i;

        i = getXmax();
        while ((!i.isCity) && i.xBridge && isSuitableForBridge(provider, i)) {
            if (coord.chunkZ() % 2 != 0 && (i.getZmin().hasXBridge(provider) != null || i.getZmax().hasXBridge(provider) != null)) {
                return null;
            }
            i = i.getXmax();
        }
        if ((!i.isCity) || i.hasBuilding || i.cityLevel > 0) {
            return null;
        }
        xBridgeType = bt;
        // Here we can automatically mark the rest of the bridge as ok. Saves on calculation
        i = i.getXmin();
        while (i != minimum) {
            i.xBridgeType = bt;
            i.xBridgeTypeCalculated = true;
            i.zBridgeType = null;
            i.zBridgeTypeCalculated = true;
            i = i.getXmin();
        }

        return bt;
    }

    // To prevent adjacent bridges of the same direction we give the bridges at even chunk X coordinates higher priority
    public BuildingPart hasZBridge(IDimensionInfo provider) {
        if (zBridgeTypeCalculated) {
            return zBridgeType;
        }
        zBridgeTypeCalculated = true;
        zBridgeType = null;

        if (!zBridge) {
            return null;
        }
        if (!isSuitableForBridge(provider, this)) {
            return null;
        }
        if (hasXBridge(provider) != null) {
            return null;
        }

        if (coord.chunkX() % 2 != 0 && (getXmin().hasZBridge(provider) != null || getXmax().hasZBridge(provider) != null)) {
            return null;
        }

        BuildingPart bt = bridgeType;
        BuildingInfo i = getZmin();
        while ((!i.isCity) && i.zBridge && isSuitableForBridge(provider, i)) {
            if (i.hasXBridge(provider) != null) {
                return null;
            }
            if (coord.chunkX() % 2 != 0 && (i.getXmin().hasZBridge(provider) != null || i.getXmax().hasZBridge(provider) != null)) {
                return null;
            }

            bt = i.bridgeType;
            i = i.getZmin();
        }

        BuildingInfo minimum = i;

        if ((!i.isCity) || i.hasBuilding || i.cityLevel > 0) {
            return null;
        }
        i = getZmax();
        while ((!i.isCity) && i.zBridge && isSuitableForBridge(provider, i)) {
            if (i.hasXBridge(provider) != null) {
                return null;
            }
            if (coord.chunkX() % 2 != 0 && (i.getXmin().hasZBridge(provider) != null || i.getXmax().hasZBridge(provider) != null)) {
                return null;
            }
            i = i.getZmax();
        }
        if ((!i.isCity) || i.hasBuilding || i.cityLevel > 0) {
            return null;
        }
        zBridgeType = bt;
        // Here we can automatically mark the rest of the bridge as ok. Saves on calculation
        i = i.getZmin();
        while (i != minimum) {
            i.zBridgeType = bt;
            i.zBridgeTypeCalculated = true;
            i.xBridgeType = null;
            i.xBridgeTypeCalculated = true;
            i = i.getZmin();
        }

        return bt;
    }

    public boolean isOcean() {
        if (isOcean != null) {
            return isOcean;
        }
        Holder<Biome> mainBiome = BiomeInfo.getBiomeInfo(provider, coord).getMainBiome();
        isOcean = mainBiome.is(BiomeTags.IS_OCEAN) || mainBiome.is(BiomeTags.IS_DEEP_OCEAN);
        return isOcean;
    }


    private boolean isSuitableForBridge(IDimensionInfo provider, BuildingInfo i) {
        if (provider.getProfile().isSpace() && hasMonorail()) {
            return false;
        }
        return i.cityLevel < cityLevel || LostCityTerrainFeature.isWaterBiome(provider, i.coord);
    }


    public boolean hasXCorridor() {
        if (!xRailCorridor) {
            return false;
        }
        BuildingInfo i = getXmin();
        while (i.canRailGoThrough() && i.xRailCorridor) {
            i = i.getXmin();
        }
        if ((!i.hasBuilding) || i.cellars == 0) {
            return false;
        }
        i = getXmax();
        while (i.canRailGoThrough() && i.xRailCorridor) {
            i = i.getXmax();
        }
        return !((!i.hasBuilding) || i.cellars == 0);
    }

    public boolean hasZCorridor() {
        if (!zRailCorridor) {
            return false;
        }
        BuildingInfo i = getZmin();
        while (i.canRailGoThrough() && i.zRailCorridor) {
            i = i.getZmin();
        }
        if ((!i.hasBuilding) || i.cellars == 0) {
            return false;
        }
        i = getZmax();
        while (i.canRailGoThrough() && i.zRailCorridor) {
            i = i.getZmax();
        }
        return !((!i.hasBuilding) || i.cellars == 0);
    }

    // Return true if it is possible for a rail section to go through here
    public boolean canRailGoThrough() {
        if (!isCity) {
            // There is no city here so no passing possible
            return false;
        }
        if (!hasBuilding) {
            // There is no building here but we have a city so we can pass
            return true;
        }
        // Otherwise we can only pass if this building has no floors below ground
        return cellars == 0;
    }

    // Return true if it is possible for a water corridor to go through here
    public boolean canWaterCorridorGoThrough() {
        if (!isCity) {
            // There is no city here so no passing possible
            return false;
        }
        if (!hasBuilding) {
            // There is no building here but we have a city so we can pass
            return true;
        }
        // Otherwise we can only pass if this building has at most one floor below ground
        return cellars <= 1;
    }

    // Return true if the road from a neighbouring chunk can extend into this chunk
    public boolean doesRoadExtendTo() {
        boolean b = isCity && !hasBuilding;
        if (b) {
            return !isElevatedParkSection();
        }
        return false;
    }

    // Return true if there can be a road connection between the two given chunks
    public static boolean hasRoadConnection(BuildingInfo i1, BuildingInfo i2) {
        if (!i1.doesRoadExtendTo()) {
            return false;
        }
        if (!i2.doesRoadExtendTo()) {
            return false;
        }
        if (Math.abs(i1.cityLevel - i2.cityLevel) <= 0 /* @todo temporary, should be <= 1 */) {
            // We allow a road difference of 1 maximum
            return true;
        }
        return false;
    }

    public static Random getBuildingRandom(int chunkX, int chunkZ, long seed) {
        QualityRandom random = new QualityRandom(seed + chunkZ * 341873128712L + chunkX * 132897987541L);
        return random;
    }

    public static Random getMultiBuildingRandom(int chunkX, int chunkZ, long seed) {
        QualityRandom random = new QualityRandom(seed + chunkZ * 8987899751L + chunkX * 189878980451L);
        return random;
    }

    // Convert a local building level to a global one (where cityLevel == 0)
    public int localToGlobal(int l) {
        return l + cityLevel;
    }

    public int globalToLocal(int l) {
        return l - cityLevel;
    }

    public boolean hasConnectionAt(int level, Orientation orientation) {
        return switch (orientation) {
            case X -> hasConnectionAtX(level);
            case Z -> hasConnectionAtZ(level);
        };
    }

    // Call this from the street reference with the (potential building) as 'adj'
    // 'streetLevel' is the cityLevel at the position of the street
    public boolean hasFrontPartFrom(BuildingInfo adj) {
        BuildingInfo.StreetType st = streetType;
        boolean elevated = isElevatedParkSection();
        if (elevated) {
            st = BuildingInfo.StreetType.PARK;
        }

        if (adj.hasBuilding && adj.frontType != null && st == BuildingInfo.StreetType.NORMAL && cityLevel < adj.cityLevel + adj.getNumFloors()) {
            RailChunkType type = getRailInfo().getType();
            if (type == RailChunkType.STATION_UNDERGROUND) {
                return false;
            }
            if (type == RailChunkType.GOING_DOWN_ONE_FROM_SURFACE) {
                return false;
            }
            if (getMaxHighwayLevel() >= 0) {
                return false;
            }

            int local = adj.globalToLocal(cityLevel);
            if (adj.isValidFloor(local) && adj.getFloor(local).getMetaBoolean(ILostCities.META_DONTCONNECT)) {
                return false;
            }
        } else {
            return false;
        }
        return true;
    }


    // This checks if there can be a connection at minX
    public boolean hasConnectionAtX(int level) {
        if (!isCity) {
            return false;
        }
        if (multiBuildingPos.isRightSide()) {
            return false;
        }
        if (level < 0 || level >= connectionAtX.length) {
            return false;
        }
        if (level < floorTypes.length && floorTypes[level].getMetaBoolean(ILostCities.META_DONTCONNECT)) {
            return false;       // No connection supported
        }
        if (getXmin().hasFrontPartFrom(this)) {
            return true;
        }
        return connectionAtX[level];
    }

    // This checks if there can be a connection at minX
    public boolean hasConnectionAtXFromStreet(int level) {
        if (!isCity) {
            return false;
        }
        if (multiBuildingPos.isRightSide()) {
            return false;
        }
        if (level < 0 || level >= connectionAtX.length) {
            return false;
        }
        if (hasFrontPartFrom(getXmin())) {
            return true;
        }
        return connectionAtX[level];
    }

    // This checks if there can be a connection at minZ
    public boolean hasConnectionAtZ(int level) {
        if (!isCity) {
            return false;
        }
        if (multiBuildingPos.isBottomSide()) {
            return false;
        }
        if (level < 0 || level >= connectionAtZ.length) {
            return false;
        }
        if (level < floorTypes.length && floorTypes[level].getMetaBoolean(ILostCities.META_DONTCONNECT)) {
            return false;       // No connection supported
        }
        if (getZmin().hasFrontPartFrom(this)) {
            return true;
        }
        return connectionAtZ[level];
    }

    // This checks if there can be a connection at minZ
    public boolean hasConnectionAtZFromStreet(int level) {
        if (!isCity) {
            return false;
        }
        if (multiBuildingPos.isBottomSide()) {
            return false;
        }
        if (level < 0 || level >= connectionAtZ.length) {
            return false;
        }
        if (hasFrontPartFrom(getZmin())) {
            return true;
        }
        return connectionAtZ[level];
    }

    /**
     * Get the lowest height of a corner of four chunks (if it is a city chunk).
     * info: reference to the bottom-right chunk. The 0,0 position of this chunk is the reference.
     * Returns 100000 if the corner is not adjacent to any city chunk
     * Also returns 100000 if all corners are city or landscape chunks (as
     * this kind of corner should also have no effect on the landscape beyond those chunks)
     * This is the level 0 version which looks at current chunk corner only
     */
    public int getLowestCityHeightAtChunkCorner() {
        BuildingInfo info00 = getXmin().getZmin();
        BuildingInfo info01 = getXmin();
        BuildingInfo info10 = getZmin();
        if (isCity && info10.isCity && info00.isCity && info01.isCity) {
            return 100000;
        }
        if (!isCity && !info10.isCity && !info00.isCity && !info01.isCity) {
            return 100000;
        }
        // If we come here we have a mix of city and normal chunks
        int h = getCityHeightForChunk();
        h = Math.min(h, info01.getCityHeightForChunk());
        h = Math.min(h, info10.getCityHeightForChunk());
        h = Math.min(h, info00.getCityHeightForChunk());
        return h;
    }

    /*
     * This is used for correcting the terrain and indicates the desired
     * level to which adjacent terrains should interpolate
     */
    public int getCityHeightForChunk() {
        if (isCity) {
            return getCityGroundLevel();
        } else {
            if (isOcean()) {
                return groundLevel - profile.OCEAN_CORRECTION_BORDER;
            } else {
                return 100000;
            }
        }
    }

    /**
     * Given adjacent (city) chunks, calculate the desired height to interpolate the
     * landscape to (minimum/maximum). This is calculated for the reference position of this chunk (0,0 point)
     * This is the level 1 version which looks at adjacent heights only
     */
    private MinMax getDesiredMaxHeightL1() {
        if (desiredMaxHeight1 == null) {
            int h = getLowestCityHeightAtChunkCorner();

            int cx = coord.chunkX();
            int cz = coord.chunkZ();

            // @todo build limit
            if (h < 256) {
                // The L0 height at this corner is fixed so we return that
                desiredMaxHeight1 = new MinMax(
                        h + LostCityTerrainFeature.getRandomizedOffset(cx, cz, profile.TERRAIN_FIX_LOWER_MIN_OFFSET, profile.TERRAIN_FIX_LOWER_MAX_OFFSET),
                        h + LostCityTerrainFeature.getRandomizedOffset(cx, cz, profile.TERRAIN_FIX_UPPER_MIN_OFFSET, profile.TERRAIN_FIX_UPPER_MAX_OFFSET));
                return desiredMaxHeight1;
            }

            MinMax minMax = new MinMax();

            getXmin().getZmin().updateMinMaxL1(minMax, 25 + LostCityTerrainFeature.getHeightOffsetL1(cx - 1, cz - 1));
            getXmin().updateMinMaxL1(minMax, 20 + LostCityTerrainFeature.getHeightOffsetL1(cx - 1, cz));
            getXmin().getZmax().updateMinMaxL1(minMax, 25 + LostCityTerrainFeature.getHeightOffsetL1(cx - 1, cz + 1));

            getZmin().updateMinMaxL1(minMax, 20 + LostCityTerrainFeature.getHeightOffsetL1(cx, cz - 1));
            getZmax().updateMinMaxL1(minMax, 20 + LostCityTerrainFeature.getHeightOffsetL1(cx, cz + 1));

            getXmax().getZmin().updateMinMaxL1(minMax, 25 + LostCityTerrainFeature.getHeightOffsetL1(cx + 1, cz - 1));
            getXmax().updateMinMaxL1(minMax, 20 + LostCityTerrainFeature.getHeightOffsetL1(cx + 1, cz));
            getXmax().getZmax().updateMinMaxL1(minMax, 25 + LostCityTerrainFeature.getHeightOffsetL1(cx + 1, cz + 1));

            desiredMaxHeight1 = minMax;
        }
        return desiredMaxHeight1;
    }

    public static class MinMax {
        public int min;
        public int max;

        public MinMax(int min, int max) {
            this.min = min;
            this.max = max;
        }

        public MinMax(MinMax mm) {
            min = mm.min;
            max = mm.max;
        }

        public MinMax() {
            min = max = 100000;
        }
    }

    /**
     * Given adjacent (city) chunks, calculate the desired height to interpolate the
     * landscape too. This is calculated for the reference position of this chunk (0,0 point)
     * This is the level 2 version which looks at L1 heights of adjacent chunks
     */
    public MinMax getDesiredMaxHeightL2() {
        if (desiredTerrainCorrectionHeights == null) {
            MinMax mm = getDesiredMaxHeightL1();
            // @todo build limit
            if (mm.min < 256) {
                // The L1 height at this corner is fixed so we return that
                desiredTerrainCorrectionHeights = new MinMax(mm);
                return desiredTerrainCorrectionHeights;
            }

            int cx = coord.chunkX();
            int cz = coord.chunkZ();

            MinMax minMax = new MinMax();

            getXmin().getZmin().updateMinMaxL2(minMax, 25 + LostCityTerrainFeature.getHeightOffsetL2(cx - 1, cz - 1));
            getXmin().updateMinMaxL2(minMax, 20 + LostCityTerrainFeature.getHeightOffsetL2(cx - 1, cz));
            getXmin().getZmax().updateMinMaxL2(minMax, 25 + LostCityTerrainFeature.getHeightOffsetL2(cx - 1, cz + 1));

            getZmin().updateMinMaxL2(minMax, 20 + LostCityTerrainFeature.getHeightOffsetL2(cx, cz - 1));
            getZmax().updateMinMaxL2(minMax, 20 + LostCityTerrainFeature.getHeightOffsetL2(cx, cz + 1));

            getXmax().getZmin().updateMinMaxL2(minMax, 25 + LostCityTerrainFeature.getHeightOffsetL2(cx + 1, cz - 1));
            getXmax().updateMinMaxL2(minMax, 20 + LostCityTerrainFeature.getHeightOffsetL2(cx + 1, cz));
            getXmax().getZmax().updateMinMaxL2(minMax, 25 + LostCityTerrainFeature.getHeightOffsetL2(cx + 1, cz + 1));
            desiredTerrainCorrectionHeights = minMax;
        }
        return desiredTerrainCorrectionHeights;
    }

    public void updateMinMaxL2(MinMax minMax, int offs) {
        MinMax h = getDesiredMaxHeightL1();
        if ((h.min - offs) < minMax.min) {
            minMax.min = h.min - offs;
        }
        if ((h.max + offs) < minMax.max) {
            minMax.max = h.max + offs;
        }
    }


    private void updateMinMaxL1(MinMax minMax, int offs) {
        int h = getLowestCityHeightAtChunkCorner();
        if ((h - offs) < minMax.min) {
            minMax.min = h - offs;
        }
        if ((h + offs) < minMax.max) {
            minMax.max = h + offs;
        }
    }


    public enum StreetType {
        NORMAL,
        FULL,
        PARK
    }


    @Override
    public boolean isCity() {
        return this.isCity;
    }

    @Override
    public String getBuildingType() {
        return hasBuilding ? buildingType.getName() : null;
    }

    @Override
    public ResourceLocation getBuildingId() {
        return hasBuilding ? buildingType.getId() : null;
    }

    @Nullable
    @Override
    public MultiBuildingInfo getMultiBuildingInfo() {
        if (multiBuilding != null) {
            return new MultiBuildingInfo(multiBuilding.getId(), multiBuildingPos.x(), multiBuildingPos.z(), multiBuilding.getDimX(), multiBuilding.getDimZ());
        }
        return null;
    }

    @Override
    public int getCityLevel() {
        return cityLevel;
    }

    @Override
    public int getNumFloors() {
        return floors;
    }

    @Override
    public int getNumCellars() {
        return cellars;
    }

    @Override
    public float getDamage(int chunkY) {
        return getDamageArea().getDamage((coord.chunkX() << 4) + 8, (chunkY * 16) + 8, (coord.chunkZ() << 4) + 8);
    }

    @Override
    public Collection<ILostExplosion> getExplosions() {
        return new ArrayList<>(getDamageArea().getExplosions());
    }

    @Override
    public int getMaxHighwayLevel() {
        return Math.max(getHighwayXLevel(), getHighwayZLevel());
    }

    @Nonnull
    @Override
    public RailChunkType getRailType() {
        return getRailInfo().getType();
    }

    @Override
    public int getRailLevel() {
        return getRailInfo().getLevel();
    }

    @Nullable
    @Override
    public ILostCityInfo getCityInfo() {
        // @todo REWRITE FOR THE NEW CITY FACTOR
        if (City.isCityCenter(coord, provider)) {
            return new ILostCityInfo() {
                @Override
                public float getCityRadius() {
                    return City.getCityRadius(coord, provider);
                }

                @Override
                public String getCityStyle() {
                    return City.getCityStyleForCityCenter(coord, provider);
                }
            };
        } else {
            return null;
        }
    }

    @Override
    public int getRuinLevel() {
        if (profile.RUIN_CHANCE <= 0.0) {
            return -1;
        }
        if (ruinHeight < 0) {
            return -1;
        }
        return (int) (getCityGroundLevel() + 1 + (ruinHeight * getNumFloors() * FLOORHEIGHT));
    }

    @Nullable
    @Override
    public ILostSphere getSphere() {
        CitySphere sphere = CitySphere.getCitySphere(coord, provider);
        if (sphere.isEnabled()) {
            return sphere;
        }
        return null;
    }

    @Nullable
    public static CitySphere getSphereInt(int x, int y, int z, IDimensionInfo provider) {
        ChunkCoord coord = new ChunkCoord(provider.dimension(), x >> 4, z >> 4);
        CitySphere sphere = CitySphere.getCitySphere(coord, provider);
        if (sphere.isEnabled()) {
            double sqdist = sphere.getCenterPos().distToCenterSqr(x, y, z);
            if (sqdist <= sphere.getRadius() * sphere.getRadius()) {
                return sphere;
            }
        }
        return null;
    }

    @Nullable
    public static CitySphere getSphereInt(int x, int z, IDimensionInfo provider) {
        ChunkCoord coord = new ChunkCoord(provider.dimension(), x >> 4, z >> 4);
        CitySphere sphere = CitySphere.getCitySphere(coord, provider);
        if (sphere.isEnabled()) {
            double sqdist = sphere.getCenterPos().distToCenterSqr(x, sphere.getCenterPos().getY(), z);
            if (sqdist <= sphere.getRadius() * sphere.getRadius()) {
                return sphere;
            }
        }
        return null;
    }
}
