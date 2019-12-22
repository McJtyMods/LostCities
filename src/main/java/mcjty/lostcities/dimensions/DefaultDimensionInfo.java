package mcjty.lostcities.dimensions;

import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.dimensions.world.ChunkHeightmap;
import mcjty.lostcities.dimensions.world.LostCityTerrainFeature;
import mcjty.lostcities.dimensions.world.lost.cityassets.AssetRegistries;
import mcjty.lostcities.dimensions.world.lost.cityassets.WorldStyle;
import net.minecraft.world.IWorld;

import java.util.Random;

public class DefaultDimensionInfo implements IDimensionInfo {

    private IWorld world;
    private final LostCityProfile profile;
    private final WorldStyle style;

    private final LostCityTerrainFeature feature;

    public DefaultDimensionInfo(IWorld world, LostCityProfile profile) {
        this.world = world;
        this.profile = profile;
        style = AssetRegistries.WORLDSTYLES.get("standard");
        feature = new LostCityTerrainFeature(this, profile, getRandom());
        feature.setupStates(profile);
    }

    @Override
    public void setWorld(IWorld world) {
        this.world = world;
    }

    @Override
    public long getSeed() {
        return world.getSeed();
    }

    @Override
    public IWorld getWorld() {
        return world;
    }

    @Override
    public LostCityProfile getProfile() {
        return profile;
    }

    @Override
    public LostCityProfile getOutsideProfile() {
        return null;
    }

    @Override
    public WorldStyle getWorldStyle() {
        return style;
    }

    @Override
    public Random getRandom() {
        return world.getRandom();
    }

    @Override
    public LostCityTerrainFeature getFeature() {
        return feature;
    }

    @Override
    public ChunkHeightmap getHeightmap(int chunkX, int chunkZ) {
        return feature.getHeightmap(chunkX, chunkZ);
    }
}
