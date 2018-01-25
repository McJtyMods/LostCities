package mcjty.lostcities.dimensions.world.lost;

import mcjty.lostcities.api.RailChunkType;
import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.dimensions.world.LostCityChunkGenerator;
import mcjty.lostcities.varia.ChunkCoord;
import mcjty.lostcities.varia.QualityRandom;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static mcjty.lostcities.api.RailChunkType.*;
import static mcjty.lostcities.dimensions.world.lost.Railway.RailDirection.*;

public class Railway {

    public static final int RAILWAY_LEVEL_OFFSET = -3;

    /*
    Railway grid:

      .   .   .   .   s   .   s   .   .   .   .   .   .   .   .

      .   .   .   .   s   s   s   .   .   .   .   .   .   .   .

      .   .   .   .   S\  0  /S   .   .   .   .   .   .   .   .
                        |   |
      .   .   .   .   S--=S=--S   .   .   .   .   .   .   .   .
                        |   |
      .   .   .   .   S/  .  \S   .   .   .   .   .   .   .   .

      .   .   .   .   s   s   s   .   .   .   .   .   .   .   .


    00 01 02 03 04 05 06 07 08 09 10 11 12 13 14 15 16 17 18 19
    SS >>          ||          << SS >>          ||          <<

     */

    public enum RailDirection {
        BI,
        WEST,
        EAST
    }

    public static class RailChunkInfo {
        private final RailChunkType type;
        private final RailDirection direction;
        private final int level;
        private final int rails;
        private final String part;

        public static final RailChunkInfo NOTHING = new RailChunkInfo(NONE, BI, 0, 0);

        public RailChunkInfo(RailChunkType type, RailDirection direction, int level, int rails) {
            this(type, direction, level, rails, null);
        }

        public RailChunkInfo(RailChunkType type, RailDirection direction, int level, int rails, String part) {
            this.type = type;
            this.direction = direction;
            this.level = level;
            this.rails = rails;
            this.part = part;
        }

        public RailChunkType getType() {
            return type;
        }

        public RailDirection getDirection() {
            return direction;
        }

        public int getLevel() {
            return level;
        }

        public int getRails() {
            return rails;
        }

        public String getPart() {
            return part;
        }
    }

    private static Map<ChunkCoord, RailChunkInfo> railInfo = new HashMap<>();

    public static void cleanCache() {
        railInfo.clear();
    }

    /**
     * The station grid repeats every 9 chunks. There is never a station at every 18/18 multiple chunk
     */
    private static RailChunkInfo getRailChunkTypeInternal(int chunkX, int chunkZ, LostCityChunkGenerator provider) {
        Random rand = new QualityRandom(provider.seed + chunkZ * 2600003897L + chunkX * 43600002517L);
        rand.nextFloat();
        rand.nextFloat();

        LostCityProfile profile = BuildingInfo.getProfile(chunkX, chunkZ, provider);

        // @todo make all settings based on rand below configurable
        float r = rand.nextFloat();

        int mx = Math.floorMod(chunkX + 1, 20);       // The +1 to avoid having them on highways
        int mz = Math.floorMod(chunkZ + 1, 20);
        if (mx == 0 && mz == 10) {
            if (!BuildingInfo.isCityRaw(chunkX, chunkZ, provider, profile)) {
                // There is no city here. So no station. But we still need a railway. A station at this
                // point will get a three line rail through it
                if (profile.RAILWAYS_CAN_END) {
                    // Check if there are stations at either side
                    boolean cityEast = BuildingInfo.isCityRaw(chunkX + 10, chunkZ, provider, profile) || BuildingInfo.isCityRaw(chunkX + 10, chunkZ - 10, provider, profile) || BuildingInfo.isCityRaw(chunkX + 10, chunkZ + 10, provider, profile);
                    boolean cityWest = BuildingInfo.isCityRaw(chunkX - 10, chunkZ, provider, profile) || BuildingInfo.isCityRaw(chunkX - 10, chunkZ - 10, provider, profile) || BuildingInfo.isCityRaw(chunkX - 10, chunkZ + 10, provider, profile);
                    if (!cityEast && !cityWest) {
                        return RailChunkInfo.NOTHING;
                    }
                    if (!cityEast) {
                        return new RailChunkInfo(RAILS_END_HERE, WEST, -3, 3);
                    }
                    if (!cityWest) {
                        return new RailChunkInfo(RAILS_END_HERE, EAST, -3, 3);
                    }
                }
                return new RailChunkInfo(HORIZONTAL, BI, RAILWAY_LEVEL_OFFSET, 3);
            }
            return getStationType(chunkX, chunkZ, provider, profile, r, 3, rand.nextFloat() < .5f ? "station_open" : "station_openroof");
        }
        if (mx == 10 && mz == 0) {
            if (!BuildingInfo.isCityRaw(chunkX, chunkZ, provider, profile)) {
                // There is no city here. So no station either. But we still need a railway. A station at this
                // point will get a two line rail through it
                if (profile.RAILWAYS_CAN_END) {
                    // Check if there are stations at either side
                    boolean cityEast = BuildingInfo.isCityRaw(chunkX + 10, chunkZ - 10, provider, profile) || BuildingInfo.isCityRaw(chunkX + 10, chunkZ + 10, provider, profile);
                    boolean cityWest = BuildingInfo.isCityRaw(chunkX - 10, chunkZ - 10, provider, profile) || BuildingInfo.isCityRaw(chunkX - 10, chunkZ + 10, provider, profile);
                    if (!cityEast && !cityWest) {
                        return RailChunkInfo.NOTHING;
                    }
                    if (!cityEast) {
                        return new RailChunkInfo(RAILS_END_HERE, WEST, -3, 2);
                    }
                    if (!cityWest) {
                        return new RailChunkInfo(RAILS_END_HERE, EAST, -3, 2);
                    }
                }
                return new RailChunkInfo(HORIZONTAL, BI, RAILWAY_LEVEL_OFFSET, 2);
            }
            return getStationType(chunkX, chunkZ, provider, profile, r, 2, rand.nextFloat() < .5f ? "station_open" : "station_openroof");
        }
        if (mx == 10 && mz == 10) {
            if (!BuildingInfo.isCityRaw(chunkX, chunkZ, provider, profile)) {
                // There is no city here. So no station either. But we still need a railway. A station at this
                // point will get a single line rail through it
                if (profile.RAILWAYS_CAN_END) {
                    // Check if there are stations at either side
                    boolean cityEast = BuildingInfo.isCityRaw(chunkX + 10, chunkZ, provider, profile);
                    boolean cityWest = BuildingInfo.isCityRaw(chunkX - 10, chunkZ, provider, profile);
                    if (!cityEast && !cityWest) {
                        return RailChunkInfo.NOTHING;
                    }
                    if (!cityEast) {
                        return new RailChunkInfo(RAILS_END_HERE, WEST, -3, 1);
                    }
                    if (!cityWest) {
                        return new RailChunkInfo(RAILS_END_HERE, EAST, -3, 1);
                    }
                }
                return new RailChunkInfo(HORIZONTAL, BI, RAILWAY_LEVEL_OFFSET, 1);
            }
            return getStationType(chunkX, chunkZ, provider, profile, r, 1, rand.nextFloat() < .5f ? "station_open" : "station_openroof");
        }
        if (mx == 0 && mz == 0) {
            return RailChunkInfo.NOTHING;
        }

        if (mz == 0 || mz == 10) {
            // Handle the rail sections left or right of every station
            if ((mx >= 16 && mz != 0) || (mx >= 6 && mx <= 9)) {
                RailChunkInfo adjacent = getRailChunkType(chunkX + 1, chunkZ, provider, profile);
                RailDirection direction = adjacent.getDirection();
                if (direction == BI || adjacent.getType() == RAILS_END_HERE) {
                    direction = WEST;
                }
                return testAdjacentRailChunk(r, adjacent, direction, chunkX - 1, chunkZ, provider, profile);
            }
            if ((mx >= 1 && mx <= 4 && mz != 0) || (mx >= 11 && mx <= 14)) {
                RailChunkInfo adjacent = getRailChunkType(chunkX - 1, chunkZ, provider, profile);
                RailDirection direction = adjacent.getDirection();
                if (direction == BI || adjacent.getType() == RAILS_END_HERE) {
                    direction = EAST;
                }
                return testAdjacentRailChunk(r, adjacent, direction, chunkX + 1, chunkZ, provider, profile);
            }
            if (mz == 0 && mx == 5) {
                if (profile.RAILWAYS_CAN_END) {
                    boolean cityWest = BuildingInfo.isCityRaw(chunkX - 5, chunkZ - 10, provider, profile) || BuildingInfo.isCityRaw(chunkX - 5, chunkZ + 10, provider, profile);
                    boolean cityEast = BuildingInfo.isCityRaw(chunkX + 5, chunkZ, provider, profile);
                    if (!cityEast && !cityWest) {
                        return RailChunkInfo.NOTHING;
                    }
                }
                return new RailChunkInfo(DOUBLE_BEND, EAST, RAILWAY_LEVEL_OFFSET, 1);
            }
            if (mz == 0 && mx == 15) {
                if (profile.RAILWAYS_CAN_END) {
                    boolean cityEast = BuildingInfo.isCityRaw(chunkX + 5, chunkZ - 10, provider, profile) || BuildingInfo.isCityRaw(chunkX + 5, chunkZ + 10, provider, profile);
                    boolean cityWest = BuildingInfo.isCityRaw(chunkX - 5, chunkZ, provider, profile);
                    if (!cityEast && !cityWest) {
                        return RailChunkInfo.NOTHING;
                    }
                }
                return new RailChunkInfo(DOUBLE_BEND, WEST, RAILWAY_LEVEL_OFFSET, 1);
            }
            if (mz == 10 && mx == 5) {
                if (profile.RAILWAYS_CAN_END) {
                    boolean cityEast = BuildingInfo.isCityRaw(chunkX + 5, chunkZ, provider, profile);
                    boolean cityWest = BuildingInfo.isCityRaw(chunkX - 5, chunkZ, provider, profile);
                    if (!cityEast && !cityWest) {
                        // Check the double bends
                        RailChunkInfo typeNorth = getRailChunkType(chunkX, chunkZ - 10, provider, profile);
                        if (typeNorth.getType() == NONE) {
                            RailChunkInfo typeSouth = getRailChunkType(chunkX, chunkZ - 10, provider, profile);
                            if (typeSouth.getType() == NONE) {
                                return RailChunkInfo.NOTHING;
                            }
                        }
                    }
                }
                return new RailChunkInfo(THREE_SPLIT, EAST, RAILWAY_LEVEL_OFFSET, 3);
            }
            if (mz == 10 && mx == 15) {
                if (profile.RAILWAYS_CAN_END) {
                    boolean cityEast = BuildingInfo.isCityRaw(chunkX + 5, chunkZ, provider, profile);
                    boolean cityWest = BuildingInfo.isCityRaw(chunkX - 5, chunkZ, provider, profile);
                    if (!cityEast && !cityWest) {
                        // Check the double bends
                        RailChunkInfo typeNorth = getRailChunkType(chunkX, chunkZ - 10, provider, profile);
                        if (typeNorth.getType() == NONE) {
                            RailChunkInfo typeSouth = getRailChunkType(chunkX, chunkZ - 10, provider, profile);
                            if (typeSouth.getType() == NONE) {
                                return RailChunkInfo.NOTHING;
                            }
                        }
                    }
                }
                return new RailChunkInfo(THREE_SPLIT, WEST, RAILWAY_LEVEL_OFFSET, 3);
            }
            return RailChunkInfo.NOTHING;
        }
        if (mx == 5) {
            if (profile.RAILWAYS_CAN_END) {
                RailChunkInfo typeNorth = getRailChunkType(chunkX, chunkZ - (mz % 10), provider, profile);
                RailChunkInfo typeSouth = getRailChunkType(chunkX, chunkZ - (mz % 10) + 10, provider, profile);
                if (typeNorth.getType() == NONE || typeSouth.getType() == NONE) {
                    return RailChunkInfo.NOTHING;
                }
            }
            return new RailChunkInfo(VERTICAL, EAST, RAILWAY_LEVEL_OFFSET, 1);
        }
        if (mx == 15) {
            if (profile.RAILWAYS_CAN_END) {
                RailChunkInfo typeNorth = getRailChunkType(chunkX, chunkZ - (mz % 10), provider, profile);
                RailChunkInfo typeSouth = getRailChunkType(chunkX, chunkZ - (mz % 10) + 10, provider, profile);
                if (typeNorth.getType() == NONE || typeSouth.getType() == NONE) {
                    return RailChunkInfo.NOTHING;
                }
            }
            return new RailChunkInfo(VERTICAL, WEST, RAILWAY_LEVEL_OFFSET, 1);
        }

        return RailChunkInfo.NOTHING;
    }

    private static RailChunkInfo getStationType(int chunkX, int chunkZ, LostCityChunkGenerator provider, LostCityProfile profile, float r, int rails, String part) {
        int cityLevel = BuildingInfo.getCityLevel(chunkX, chunkZ, provider);
        if (cityLevel > 2) {
            // We are too high here. We need an underground station
            return new RailChunkInfo(STATION_UNDERGROUND, BI, RAILWAY_LEVEL_OFFSET, rails);
        }
        // If there is a highway exactly at this spot we cannot have a station. @todo? How to solve this
        int highwayX = Highway.getXHighwayLevel(chunkX, chunkZ, provider, profile);
        int highwayZ = Highway.getZHighwayLevel(chunkX, chunkZ, provider, profile);
        if ((highwayX != -1 && cityLevel >= highwayX) || (highwayZ != -1 && cityLevel >= highwayZ)) {
            // @todo Problem! We cannot have a station here! At least we cannot get stairs to the top here
            // Because this is very rare we just generate an underground station because that looks reasonable
            return new RailChunkInfo(STATION_UNDERGROUND, BI, RAILWAY_LEVEL_OFFSET, rails);
        } else {
            // Check if there is a highway directly adjacent (east/west) to the station. In that case we go to underground station mode
            highwayZ = Highway.getZHighwayLevel(chunkX-1, chunkZ, provider, profile);
            if (highwayZ != -1 && cityLevel >= highwayZ) {
                return new RailChunkInfo(STATION_UNDERGROUND, BI, RAILWAY_LEVEL_OFFSET, rails);
            }
            highwayZ = Highway.getZHighwayLevel(chunkX+1, chunkZ, provider, profile);
            if (highwayZ != -1 && cityLevel >= highwayZ) {
                return new RailChunkInfo(STATION_UNDERGROUND, BI, RAILWAY_LEVEL_OFFSET, rails);
            }
        }

        return r < .5f ? new RailChunkInfo(STATION_SURFACE, BI, cityLevel, rails, part) : new RailChunkInfo(STATION_UNDERGROUND, BI, RAILWAY_LEVEL_OFFSET, rails);
    }

    public static RailChunkInfo getRailChunkType(int chunkX, int chunkZ, LostCityChunkGenerator provider, LostCityProfile profile) {
        ChunkCoord key = new ChunkCoord(provider.dimensionId, chunkX, chunkZ);
        if (railInfo.containsKey(key)) {
            return railInfo.get(key);
        }
        RailChunkInfo info = getRailChunkTypeInternal(chunkX, chunkZ, provider);
        if (provider.getProfile().isSpace() && CitySphere.onCitySphereBorder(chunkX, chunkZ, provider)) {
            info = RailChunkInfo.NOTHING;
        } else if (info.getType().isStation()) {
            if (!profile.RAILWAY_STATIONS_ENABLED) {
                info = RailChunkInfo.NOTHING;
            }
        } else {
            if (!profile.RAILWAYS_ENABLED) {
                info = RailChunkInfo.NOTHING;
            }
        }
        railInfo.put(key, info);
        return info;
    }

    private static RailChunkInfo testAdjacentRailChunk(float r, RailChunkInfo adjacent, RailDirection direction, int chunkX, int chunkZ, LostCityChunkGenerator provider, LostCityProfile profile) {
        switch (adjacent.getType()) {
            case NONE:
                return RailChunkInfo.NOTHING;
            case STATION_SURFACE:
                // chunkX actually points to the next chunk. If there is a highway there we want to avoid that and go down this level already
                int highwayX = Highway.getXHighwayLevel(chunkX, chunkZ, provider, profile);
                int highwayZ = Highway.getZHighwayLevel(chunkX, chunkZ, provider, profile);
                if ((highwayX != -1 && adjacent.getLevel() == highwayX) || (highwayZ != -1 && adjacent.getLevel() == highwayZ)) {
                    // We have a highway there so go down here by setting r to 1
                    r = 1;
                }
                if (r < .4f) {
                    return new RailChunkInfo(STATION_EXTENSION_SURFACE, direction, adjacent.getLevel(), adjacent.getRails());
                } else if ((adjacent.getLevel() & 1) == 0) {
                    return new RailChunkInfo(GOING_DOWN_ONE_FROM_SURFACE, direction, adjacent.getLevel() - 1, adjacent.getRails());
                } else {
                    return new RailChunkInfo(GOING_DOWN_TWO_FROM_SURFACE, direction, adjacent.getLevel() - 2, adjacent.getRails());
                }
            case STATION_UNDERGROUND:
                return r < .4f
                        ? new RailChunkInfo(STATION_EXTENSION_UNDERGROUND, direction, adjacent.getLevel(), adjacent.getRails())
                        : new RailChunkInfo(HORIZONTAL, direction, adjacent.getLevel(), adjacent.getRails());
            case STATION_EXTENSION_SURFACE:
                if ((adjacent.getLevel() & 1) == 0) {
                    return new RailChunkInfo(GOING_DOWN_ONE_FROM_SURFACE, direction, adjacent.getLevel() - 1, adjacent.getRails());
                } else {
                    return new RailChunkInfo(GOING_DOWN_TWO_FROM_SURFACE, direction, adjacent.getLevel() - 2, adjacent.getRails());
                }
            case STATION_EXTENSION_UNDERGROUND:
                return new RailChunkInfo(HORIZONTAL, direction, adjacent.getLevel(), adjacent.getRails());
            case GOING_DOWN_FURTHER:
            case GOING_DOWN_ONE_FROM_SURFACE:
            case GOING_DOWN_TWO_FROM_SURFACE:
                if (adjacent.getLevel() == RAILWAY_LEVEL_OFFSET) {
                    return new RailChunkInfo(HORIZONTAL, direction, adjacent.getLevel(), adjacent.getRails());
                } else {
                    return new RailChunkInfo(GOING_DOWN_FURTHER, direction, adjacent.getLevel() - 2, adjacent.getRails());
                }
            case RAILS_END_HERE:
                if (direction == adjacent.getDirection()) {
                    return new RailChunkInfo(HORIZONTAL, direction, adjacent.getLevel(), adjacent.getRails());
                } else {
                    return RailChunkInfo.NOTHING;
                }
            case HORIZONTAL:
                return adjacent;
        }
        throw new RuntimeException("This is really impossible!");
    }

    public static void main(String[] args) {
        int chunkX = -16;
        int chunkZ = -1;
        int mx = Math.floorMod(chunkX + 1, 20);       // The +1 to avoid having them on highways
        int mz = Math.floorMod(chunkZ + 1, 20);
        System.out.println("mx = " + mx);
        System.out.println("mz = " + mz);

        for (int i = -40 ; i < 40 ; i++) {
            System.out.println("Math.floorMod(" + i + ", 20) = " + Math.floorMod(i, 20));
        }

//
//
//
//        for (int z = 0 ; z < 50 ; z++) {
//            String s = "";
//            for (int x = 0 ; x < 50 ; x++) {
//                RailChunkInfo info = getRailChunkType(x, z, null);
//                switch (info.getType()) {
//                    case NONE:
//                        s += "  ";
//                        break;
//                    case STATION_SURFACE:
//                        s += "Ss";
//                        break;
//                    case STATION_UNDERGROUND:
//                        s += "Su";
//                        break;
//                    case STATION_EXTENSION_SURFACE:
//                        s += "s+";
//                        break;
//                    case STATION_EXTENSION_UNDERGROUND:
//                        s += "u+";
//                        break;
//                    case GOING_DOWN_TWO_FROM_SURFACE:
//                        if (info.getDirection() == WEST) {
//                            s += "<2";
//                        } else {
//                            s += "2>";
//                        }
//                        break;
//                    case GOING_DOWN_ONE_FROM_SURFACE:
//                        if (info.getDirection() == WEST) {
//                            s += "<1";
//                        } else {
//                            s += "1>";
//                        }
//                        break;
//                    case GOING_DOWN_FURTHER:
//                        if (info.getDirection() == WEST) {
//                            s += "<<";
//                        } else {
//                            s += ">>";
//                        }
//                        break;
//                    case HORIZONTAL:
//                        if (info.getRails() > 1) {
//                            s += "==";
//                        } else {
//                            s += "--";
//                        }
//                        break;
//                    case THREE_SPLIT:
//                        s += "=-";
//                        break;
//                    case VERTICAL:
//                        s += "||";
//                        break;
//                    case DOUBLE_BEND:
//                        s += "<>";
//                        break;
//                }
//            }
//            System.out.println("" + s);
//        }
//    }
    }
}