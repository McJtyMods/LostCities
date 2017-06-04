package mcjty.lostcities.dimensions.world.lost;

import mcjty.lostcities.config.LostCityConfiguration;
import mcjty.lostcities.varia.GeometryTools;
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

    public DamageArea(long seed, int chunkX, int chunkZ) {
        this.seed = seed;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        chunkBox = new AxisAlignedBB(chunkX * 16, 0, chunkZ * 16, chunkX * 16 + 15, 256, chunkZ * 16 + 15);

        int offset = (LostCityConfiguration.EXPLOSION_MAXRADIUS+15) / 16;
        for (int cx = chunkX - offset; cx <= chunkX + offset; cx++) {
            for (int cz = chunkZ - offset; cz <= chunkZ + offset; cz++) {
                Explosion explosion = getExplosionAt(cx, cz);
                if (explosion != null) {
                    if (intersectsWith(explosion.getCenter(), explosion.getRadius())) {
                        explosions.add(explosion);
                    }
                }
                explosion = getMiniExplosionAt(cx, cz);
                if (explosion != null) {
                    if (intersectsWith(explosion.getCenter(), explosion.getRadius())) {
                        explosions.add(explosion);
                    }
                }
            }
        }
    }

    public IBlockState damageBlock(IBlockState b, IBlockState replacement, Random rand, int x, int y, int z, int index, Style style) {
        float damage = getDamage(x, y, z);
        if (style.isEasyToDestroy(b)) {
            damage *= 2.5f;    // As if this block gets double the damage
        }
        if (style.isLiquid(b)) {
            damage *= 10f;
        }
        if (rand.nextFloat() <= damage) {
            if (damage < BLOCK_DAMAGE_CHANCE && style.canBeDamagedToIronBars(b)) {
                if (rand.nextFloat() < .7f) {
                    b = Blocks.IRON_BARS.getDefaultState();
                } else {
                    b = replacement;
                }
            } else {
                b = replacement;
            }
        }
        return b;
    }

    private boolean intersectsWith(BlockPos center, int radius) {
        double dmin = GeometryTools.squaredDistanceBoxPoint(chunkBox, center);
        return dmin <= radius * radius;
    }

    private Explosion getExplosionAt(int chunkX, int chunkZ) {
        Random rand = new Random(seed + chunkZ * 295075153L + chunkX * 797003437L);
        rand.nextFloat();
        rand.nextFloat();
        if (rand.nextFloat() < LostCityConfiguration.EXPLOSION_CHANCE) {
            return new Explosion(LostCityConfiguration.EXPLOSION_MINRADIUS + rand.nextInt(LostCityConfiguration.EXPLOSION_MAXRADIUS - LostCityConfiguration.EXPLOSION_MINRADIUS),
                    new BlockPos(chunkX * 16 + rand.nextInt(16), LostCityConfiguration.EXPLOSION_MINHEIGHT + rand.nextInt(LostCityConfiguration.EXPLOSION_MAXHEIGHT - LostCityConfiguration.EXPLOSION_MINHEIGHT), chunkZ * 16 + rand.nextInt(16)));
        }
        return null;
    }

    private Explosion getMiniExplosionAt(int chunkX, int chunkZ) {
        Random rand = new Random(seed + chunkZ * 1400305337L + chunkX * 573259391L);
        rand.nextFloat();
        rand.nextFloat();
        if (rand.nextFloat() < LostCityConfiguration.MINI_EXPLOSION_CHANCE) {
            return new Explosion(LostCityConfiguration.MINI_EXPLOSION_MINRADIUS + rand.nextInt(LostCityConfiguration.MINI_EXPLOSION_MAXRADIUS - LostCityConfiguration.MINI_EXPLOSION_MINRADIUS),
                    new BlockPos(chunkX * 16 + rand.nextInt(16), LostCityConfiguration.MINI_EXPLOSION_MINHEIGHT + rand.nextInt(LostCityConfiguration.MINI_EXPLOSION_MAXHEIGHT - LostCityConfiguration.MINI_EXPLOSION_MINHEIGHT), chunkZ * 16 + rand.nextInt(16)));
        }
        return null;
    }

    // Return true if this chunk is affected by explosions
    public boolean hasExplosions() {
        return !explosions.isEmpty();
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
    private float getDamage(int x, int y, int z) {
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
