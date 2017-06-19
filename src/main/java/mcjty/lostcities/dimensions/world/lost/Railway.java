package mcjty.lostcities.dimensions.world.lost;

import mcjty.lostcities.dimensions.world.LostCityChunkGenerator;
import mcjty.lostcities.varia.ChunkCoord;
import mcjty.lostcities.varia.QualityRandom;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static mcjty.lostcities.dimensions.world.lost.Railway.RailChunkType.*;
import static mcjty.lostcities.dimensions.world.lost.Railway.RailDirection.*;

public class Railway {

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

    public static enum RailChunkType {
        NONE,
        STATION_SURFACE,
        STATION_UNDERGROUND,
        STATION_EXTENSION_SURFACE,
        STATION_EXTENSION_UNDERGROUND,
        GOING_DOWN_TWO_FROM_SURFACE,
        GOING_DOWN_ONE_FROM_SURFACE,
        GOING_DOWN_FURTHER,
        HORIZONTAL,
        THREE_SPLIT,
        VERTICAL,
        DOUBLE_BEND
    }

    public static enum RailDirection {
        BI,
        WEST,
        EAST
    }

    public static class RailChunkInfo {
        private final RailChunkType type;
        private final RailDirection direction;
        private final int level;
        private final int rails;

        public static final RailChunkInfo NOTHING = new RailChunkInfo(NONE, BI, 0, 0);

        public RailChunkInfo(RailChunkType type, RailDirection direction, int level, int rails) {
            this.type = type;
            this.direction = direction;
            this.level = level;
            this.rails = rails;
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
    }

    private static Map<ChunkCoord, RailChunkInfo> railInfo = new HashMap<>();

    public static void cleanCache() {
        railInfo.clear();
    }

    /**
     * The station grid repeats every 9 chunks. There is never a station at every 18/18 multiple chunk
     */
    private static RailChunkInfo getRailChunkTypeInternal(int chunkX, int chunkZ, LostCityChunkGenerator provider) {
        Random rand = new QualityRandom(provider.seed +chunkZ * 2600003897L + chunkX * 43600002517L);
        rand.nextFloat();
        rand.nextFloat();

        // @todo make all settings based on rand below configurable
        float r = rand.nextFloat();

        int mx = Math.floorMod(chunkX+1, 20);       // The +1 to avoid having them on highways
        int mz = Math.floorMod(chunkZ+1, 20);
        if (mx == 0 && mz == 10) {
            int cityLevel = BuildingInfo.getCityLevel(chunkX, chunkZ, provider);
            if (cityLevel > 2) {
                // We are too high here. We need an underground station
                return new RailChunkInfo(STATION_UNDERGROUND, BI, -3, 3);
            }
            if (!BuildingInfo.isCityRaw(chunkX, chunkZ, provider)) {
                // There is no city here. So no station either. But we still need a railway. A station at this
                // point will get a three line rail through it
                // @todo with a random chance we don't even have rails here
                return new RailChunkInfo(HORIZONTAL, BI, -3, 3);
            }
            return r < .5f ? new RailChunkInfo(STATION_SURFACE, BI, cityLevel, 3) : new RailChunkInfo(STATION_UNDERGROUND, BI, -3, 3);
        }
        if (mx == 10 && mz == 0) {
            int cityLevel = BuildingInfo.getCityLevel(chunkX, chunkZ, provider);
            if (cityLevel > 2) {
                // We are too high here. We need an underground station
                return new RailChunkInfo(STATION_UNDERGROUND, BI, -3, 2);
            }
            if (!BuildingInfo.isCityRaw(chunkX, chunkZ, provider)) {
                // There is no city here. So no station either. But we still need a railway. A station at this
                // point will get a two line rail through it
                // @todo with a random chance we don't even have rails here
                return new RailChunkInfo(HORIZONTAL, BI, -3, 2);
            }
            return r < .5f ? new RailChunkInfo(STATION_SURFACE, BI, cityLevel, 2) : new RailChunkInfo(STATION_UNDERGROUND, BI, -3, 2);
        }
        if (mx == 10 && mz == 10) {
            int cityLevel = BuildingInfo.getCityLevel(chunkX, chunkZ, provider);
            if (cityLevel > 2) {
                // We are too high here. We need an underground station
                return new RailChunkInfo(STATION_UNDERGROUND, BI, -3, 1);
            }
            if (!BuildingInfo.isCityRaw(chunkX, chunkZ, provider)) {
                // There is no city here. So no station either. But we still need a railway. A station at this
                // point will get a single line rail through it
                // @todo with a random chance we don't even have rails here
                return new RailChunkInfo(HORIZONTAL, BI, -3, 1);
            }
            return r < .5f ? new RailChunkInfo(STATION_SURFACE, BI, cityLevel, 0) : new RailChunkInfo(STATION_UNDERGROUND, BI, -3, 1);
        }
        if (mx == 0 && mz == 0) {
            return RailChunkInfo.NOTHING;
        }

        if (mz == 0 || mz == 10) {
            // Handle the rail sections left or right of every station
            if ((mx >= 16 && mz != 0) || (mx >= 6 && mx <= 9)) {
                RailChunkInfo adjacent = getRailChunkType(chunkX + 1, chunkZ, provider);
                RailDirection direction = adjacent.getDirection();
                if (direction == BI) {
                    direction = WEST;
                }
                return testAdjacentRailChunk(r, adjacent, direction);
            }
            if ((mx >= 1 && mx <= 4 && mz != 0) || (mx >= 11 && mx <= 14)) {
                RailChunkInfo adjacent = getRailChunkType(chunkX - 1, chunkZ, provider);
                RailDirection direction = adjacent.getDirection();
                if (direction == BI) {
                    direction = EAST;
                }
                return testAdjacentRailChunk(r, adjacent, direction);
            }
            if (mz == 0 && mx == 5) {
                return new RailChunkInfo(DOUBLE_BEND, EAST, -3, 1);
            }
            if (mz == 0 && mx == 15) {
                return new RailChunkInfo(DOUBLE_BEND, WEST, -3, 1);
            }
            if (mz == 10 && mx == 5) {
                return new RailChunkInfo(THREE_SPLIT, EAST, -3, 3);
            }
            if (mz == 10 && mx == 15) {
                return new RailChunkInfo(THREE_SPLIT, WEST, -3, 3);
            }
            return RailChunkInfo.NOTHING;
        }
        if (mx == 5 || mx == 15) {
            return new RailChunkInfo(VERTICAL, BI, -3, 1);
        }

        return RailChunkInfo.NOTHING;
    }

    public static RailChunkInfo getRailChunkType(int chunkX, int chunkZ, LostCityChunkGenerator provider) {
        ChunkCoord key = new ChunkCoord(chunkX, chunkZ);
        if (railInfo.containsKey(key)) {
            return railInfo.get(key);
        }
        RailChunkInfo info = getRailChunkTypeInternal(chunkX, chunkZ, provider);
        railInfo.put(key, info);
        return info;
    }

    private static RailChunkInfo testAdjacentRailChunk(float r, RailChunkInfo adjacent, RailDirection direction) {
        switch (adjacent.getType()) {
            case NONE:
                return RailChunkInfo.NOTHING;
            case STATION_SURFACE:
                if (r < .4f) {
                    return new RailChunkInfo(STATION_EXTENSION_SURFACE, direction, adjacent.getLevel(), adjacent.getRails());
                } else if ((adjacent.getLevel() & 1) == 0) {
                    return new RailChunkInfo(GOING_DOWN_ONE_FROM_SURFACE, direction, adjacent.getLevel()-1, adjacent.getRails());
                } else {
                    return new RailChunkInfo(GOING_DOWN_TWO_FROM_SURFACE, direction, adjacent.getLevel()-2, adjacent.getRails());
                }
            case STATION_UNDERGROUND:
                return r < .4f
                        ? new RailChunkInfo(STATION_EXTENSION_UNDERGROUND, direction, adjacent.getLevel(), adjacent.getRails())
                        : new RailChunkInfo(HORIZONTAL, direction, adjacent.getLevel(), adjacent.getRails());
            case STATION_EXTENSION_SURFACE:
                if ((adjacent.getLevel() & 1) == 0) {
                    return new RailChunkInfo(GOING_DOWN_ONE_FROM_SURFACE, direction, adjacent.getLevel()-1, adjacent.getRails());
                } else {
                    return new RailChunkInfo(GOING_DOWN_TWO_FROM_SURFACE, direction, adjacent.getLevel()-2, adjacent.getRails());
                }
            case STATION_EXTENSION_UNDERGROUND:
                return new RailChunkInfo(HORIZONTAL, direction, adjacent.getLevel(), adjacent.getRails());
            case GOING_DOWN_FURTHER:
            case GOING_DOWN_ONE_FROM_SURFACE:
            case GOING_DOWN_TWO_FROM_SURFACE:
                if (adjacent.getLevel() == -3) {
                    return new RailChunkInfo(HORIZONTAL, direction, adjacent.getLevel(), adjacent.getRails());
                } else {
                    return new RailChunkInfo(GOING_DOWN_FURTHER, direction, adjacent.getLevel()-2, adjacent.getRails());
                }
            case HORIZONTAL:
                return adjacent;
        }
        throw new RuntimeException("This is really impossible!");
    }

    public static void main(String[] args) {
        for (int z = 0 ; z < 50 ; z++) {
            String s = "";
            for (int x = 0 ; x < 50 ; x++) {
                RailChunkInfo info = getRailChunkType(x, z, null);
                switch (info.getType()) {
                    case NONE:
                        s += "  ";
                        break;
                    case STATION_SURFACE:
                        s += "Ss";
                        break;
                    case STATION_UNDERGROUND:
                        s += "Su";
                        break;
                    case STATION_EXTENSION_SURFACE:
                        s += "s+";
                        break;
                    case STATION_EXTENSION_UNDERGROUND:
                        s += "u+";
                        break;
                    case GOING_DOWN_TWO_FROM_SURFACE:
                        if (info.getDirection() == WEST) {
                            s += "<2";
                        } else {
                            s += "2>";
                        }
                        break;
                    case GOING_DOWN_ONE_FROM_SURFACE:
                        if (info.getDirection() == WEST) {
                            s += "<1";
                        } else {
                            s += "1>";
                        }
                        break;
                    case GOING_DOWN_FURTHER:
                        if (info.getDirection() == WEST) {
                            s += "<<";
                        } else {
                            s += ">>";
                        }
                        break;
                    case HORIZONTAL:
                        if (info.getRails() > 1) {
                            s += "==";
                        } else {
                            s += "--";
                        }
                        break;
                    case THREE_SPLIT:
                        s += "=-";
                        break;
                    case VERTICAL:
                        s += "||";
                        break;
                    case DOUBLE_BEND:
                        s += "<>";
                        break;
                }
            }
            System.out.println("" + s);
        }
    }
}
