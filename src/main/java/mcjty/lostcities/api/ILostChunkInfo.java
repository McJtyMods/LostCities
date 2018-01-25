package mcjty.lostcities.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

public interface ILostChunkInfo {

    /**
     * Return true if this chunk represents a part of a city (building or street)
     */
    boolean isCity();

    /**
     * If this chunk hosts the center of a city then this will retrieve the city information
     * of that city.
     */
    @Nullable
    ILostCityInfo getCityInfo();

    /**
     * Return the name of the building if there is a building here (name from asset registry)
     * Returns null otherwise. If isCity() returns true and this returns null then we have
     * a 'street' chunk
     */
    String getBuildingType();

    /**
     * Return the kind of railway type at this spot
     */
    @Nonnull
    RailChunkType getRailType();

    /**
     * Return the citylevel of the railway structure. The underground subway system runs
     * at level -3 but surface stations can go up to city level
     */
    int getRailLevel();

    /**
     * Get the level of the city/landscape at this point. 0 is the lowest level. You can get
     * the real height uwing ILostChunkGenerator.getRealHeight(level).
     * If there is a building at this chunk then this is the level of the first floor.
     * If there is no city here then this represents a rough estimate of the height of the
     * terrain
     */
    int getCityLevel();

    /**
     * Get the number of floors of a building (above ground) including the top part of a
     * building
     */
    int getNumFloors();

    /**
     * Get the number of cellars of a building (below ground)
     * @return
     */
    int getNumCellars();


    /**
     * Give an indication of how much damage a subchunk has gotten from explosions.
     * Subchunks are 16x16x16 chunks so chunkY*16 is the actual height. This will get
     * the damage of the center of the subchunk
     * If this returns 0 there was no damage
     * Anything above 1 will completely destroy blocks
     * Note that this damage is independent from the ruin system. Use getRuinLevel()
     * to detect that.
     */
    float getDamage(int chunkY);

    /**
     * If ruins are enabled this will return the average height level at which the
     * building in this chunk will be ruined. Actual destruction of blocks occurs
     * randomly around this level. If this returns -1 the building is not ruined (but
     * it can still be damaged by explosions).
     */
    int getRuinLevel();

    /**
     * Get all explosions that affect this chunk (this includes explosions outside the current
     * chunk but big enough to affect this chunk).
     */
    Collection<ILostExplosion> getExplosions();

    /**
     * Return the maximum highway level at this chunk. Returns -1 if there is no highway here
     */
    int getMaxHighwayLevel();

    /**
     * Get a sphere that contains this chunk (if there is one)
     */
    @Nullable
    ILostSphere getSphere();
}
