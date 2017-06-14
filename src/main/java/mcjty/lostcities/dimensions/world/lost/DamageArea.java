package mcjty.lostcities.dimensions.world.lost;

import mcjty.lostcities.dimensions.world.LostCityChunkGenerator;
import mcjty.lostcities.dimensions.world.lost.cityassets.CompiledPalette;
import mcjty.lostcities.varia.GeometryTools;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DamageArea {

    public static final float BLOCK_DAMAGE_CHANCE = .7f;

    private final long seed;
    private final int chunkX;
    private final int chunkZ;
    private final List<Explosion> explosions = new ArrayList<>();
    private final AxisAlignedBB chunkBox;

    public DamageArea(int chunkX, int chunkZ, LostCityChunkGenerator provider) {
        this.seed = provider.seed;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        chunkBox = new AxisAlignedBB(chunkX * 16, 0, chunkZ * 16, chunkX * 16 + 15, 256, chunkZ * 16 + 15);

        int offset = (provider.profile.EXPLOSION_MAXRADIUS+15) / 16;
        for (int cx = chunkX - offset; cx <= chunkX + offset; cx++) {
            for (int cz = chunkZ - offset; cz <= chunkZ + offset; cz++) {
                if ((!provider.profile.EXPLOSIONS_IN_CITIES_ONLY) || BuildingInfo.isCity(cx, cz, provider)) {
                    Explosion explosion = getExplosionAt(cx, cz, provider);
                    if (explosion != null) {
                        if (intersectsWith(explosion.getCenter(), explosion.getRadius())) {
                            explosions.add(explosion);
                        }
                    }
                    explosion = getMiniExplosionAt(cx, cz, provider);
                    if (explosion != null) {
                        if (intersectsWith(explosion.getCenter(), explosion.getRadius())) {
                            explosions.add(explosion);
                        }
                    }
                }
            }
        }
    }

    public IBlockState damageBlock(IBlockState b, LostCityChunkGenerator provider, int x, int y, int z, CompiledPalette palette) {
        if (b == LostCitiesTerrainGenerator.air) {
            return b;
        }
        float damage = getDamage(x, y, z);
        if (damage < 0.001) {
            return b;
        }

        Block block = b.getBlock();
        if (block == Blocks.BEDROCK || block == Blocks.END_PORTAL || block == Blocks.END_PORTAL_FRAME) {
            return b;
        }

        if (palette.isEasyToDestroy(b)) {
            damage *= 2.5f;    // As if this block gets double the damage
        }
        if (palette.isLiquid(b)) {
            damage *= 10f;
        }
        if (provider.rand.nextFloat() <= damage) {
            IBlockState damaged = palette.canBeDamagedToIronBars(b);
            if (damage < BLOCK_DAMAGE_CHANCE && damaged != null) {
                if (provider.rand.nextFloat() < .7f) {
                    b = damaged;
                } else {
                    b = y < provider.profile.WATERLEVEL ? LostCitiesTerrainGenerator.water : LostCitiesTerrainGenerator.air;
                }
            } else {
                b = y < provider.profile.WATERLEVEL ? LostCitiesTerrainGenerator.water : LostCitiesTerrainGenerator.air;
            }
        }
        return b;
    }

    private boolean intersectsWith(BlockPos center, int radius) {
        double dmin = GeometryTools.squaredDistanceBoxPoint(chunkBox, center);
        return dmin <= radius * radius;
    }

    private Explosion getExplosionAt(int chunkX, int chunkZ, LostCityChunkGenerator provider) {
        Random rand = new Random(seed + chunkZ * 295075153L + chunkX * 797003437L);
        rand.nextFloat();
        rand.nextFloat();
        if (rand.nextFloat() < provider.profile.EXPLOSION_CHANCE) {
            return new Explosion(provider.profile.EXPLOSION_MINRADIUS + rand.nextInt(provider.profile.EXPLOSION_MAXRADIUS - provider.profile.EXPLOSION_MINRADIUS),
                    new BlockPos(chunkX * 16 + rand.nextInt(16), BuildingInfo.getBuildingInfo(chunkX, chunkZ, provider).cityLevel * 6 + provider.profile.EXPLOSION_MINHEIGHT + rand.nextInt(provider.profile.EXPLOSION_MAXHEIGHT - provider.profile.EXPLOSION_MINHEIGHT), chunkZ * 16 + rand.nextInt(16)));
        }
        return null;
    }

    private Explosion getMiniExplosionAt(int chunkX, int chunkZ, LostCityChunkGenerator provider) {
        Random rand = new Random(seed + chunkZ * 1400305337L + chunkX * 573259391L);
        rand.nextFloat();
        rand.nextFloat();
        if (rand.nextFloat() < provider.profile.MINI_EXPLOSION_CHANCE) {
            return new Explosion(provider.profile.MINI_EXPLOSION_MINRADIUS + rand.nextInt(provider.profile.MINI_EXPLOSION_MAXRADIUS - provider.profile.MINI_EXPLOSION_MINRADIUS),
                    new BlockPos(chunkX * 16 + rand.nextInt(16), BuildingInfo.getBuildingInfo(chunkX, chunkZ, provider).cityLevel * 6 + provider.profile.MINI_EXPLOSION_MINHEIGHT + rand.nextInt(provider.profile.MINI_EXPLOSION_MAXHEIGHT - provider.profile.MINI_EXPLOSION_MINHEIGHT), chunkZ * 16 + rand.nextInt(16)));
        }
        return null;
    }

    // Return true if this chunk is affected by explosions
    public boolean hasExplosions() {
        return !explosions.isEmpty();
    }

    // Get the lowest height that is affected by an explosion
    public int getLowestExplosionHeight() {
        int lowest = 1000;
        for (Explosion explosion : explosions) {
            int y = explosion.getCenter().getY() - explosion.getRadius();
            if (y < lowest) {
                lowest = y;
            }
        }
        return lowest > 1 ? lowest : 1;
    }

    // Get the lowest height that is affected by an explosion
    public int getHighestExplosionHeight() {
        int highest = -1000;
        for (Explosion explosion : explosions) {
            int y = explosion.getCenter().getY() + explosion.getRadius();
            if (y > highest) {
                highest = y;
            }
        }
        return highest < 254 ? highest : 254;
    }

    // Give an indication of how much damage this chunk got
    public float getDamageFactor() {
        float damage = 0.0f;
        for (Explosion explosion : explosions) {
            double sq = explosion.getCenter().distanceSq(chunkX * 16, explosion.getCenter().getY(), chunkZ * 16);
            if (sq < explosion.getSqradius()) {
                double d = Math.sqrt(sq);
                damage += 3.0f * (explosion.getRadius() - d) / explosion.getRadius();
            }
        }
        return damage;
    }

    // Get a number indicating how much damage this point should get. 0 Means no damage
    public float getDamage(int x, int y, int z) {
        float damage = 0.0f;
        for (Explosion explosion : explosions) {
            double sq = explosion.getCenter().distanceSq(x, y, z);
            if (sq < explosion.getSqradius()) {
                double d = Math.sqrt(sq);
                damage += 3.0f * (explosion.getRadius() - d) / explosion.getRadius();
            }
        }
        return damage;
    }
}
