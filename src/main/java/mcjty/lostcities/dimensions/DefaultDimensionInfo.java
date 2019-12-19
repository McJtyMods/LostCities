package mcjty.lostcities.dimensions;

import mcjty.lostcities.config.LandscapeType;
import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.dimensions.world.ChunkHeightmap;
import mcjty.lostcities.dimensions.world.driver.SafeDriver;
import mcjty.lostcities.dimensions.world.lost.cityassets.AssetRegistries;
import mcjty.lostcities.dimensions.world.lost.cityassets.WorldStyle;
import mcjty.lostcities.dimensions.world.terraingen.LostCityTerrainCarver;
import net.minecraft.block.Blocks;
import net.minecraft.world.IWorld;

import java.util.Random;

public class DefaultDimensionInfo implements IDimensionInfo {

    private final IWorld world;
    private final LostCityProfile profile;
    private final WorldStyle style;

    private final LostCityTerrainCarver carver;

    public DefaultDimensionInfo(IWorld world, LostCityProfile profile) {
        this.world = world;
        this.profile = profile;
        style = AssetRegistries.WORLDSTYLES.get("standard");
        carver = new LostCityTerrainCarver(this, profile, getRandom());
        carver.setupChars(profile);
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
    public ChunkHeightmap getHeightmap(int chunkX, int chunkZ) {
        return new ChunkHeightmap(null /* @todo */, LandscapeType.DEFAULT, 65, Blocks.AIR.getDefaultState());    // @todo
    }

    @Override
    public Random getRandom() {
        return world.getRandom();
    }

    @Override
    public ICityCarver getCarver() {
        return carver;
    }
}
