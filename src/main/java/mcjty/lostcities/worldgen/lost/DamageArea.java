package mcjty.lostcities.worldgen.lost;

import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.varia.ChunkCoord;
import mcjty.lostcities.varia.GeometryTools;
import mcjty.lostcities.varia.Tools;
import mcjty.lostcities.worldgen.IDimensionInfo;
import mcjty.lostcities.worldgen.LostTags;
import mcjty.lostcities.worldgen.lost.cityassets.CompiledPalette;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DamageArea {

    public static final float BLOCK_DAMAGE_CHANCE = .7f;

    private final long seed;
    private final int chunkX;
    private final int chunkZ;
    private final List<Explosion> explosions = new ArrayList<>();
    private final AABB chunkBox;
    private final LostCityProfile profile;

    private final BlockState air;

    public DamageArea(int chunkX, int chunkZ, IDimensionInfo provider, BuildingInfo info) {
        this.seed = provider.getSeed();
        this.profile = info.profile;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.air = Blocks.AIR.defaultBlockState();
        chunkBox = new AABB(chunkX << 4, 0, chunkZ << 4, (chunkX << 4) + 15, 256, (chunkZ << 4) + 15);

        Random damageRandom = new Random(seed + chunkZ * 295075153L + chunkX * 899826547L);

        int offset = (Math.max(info.profile.EXPLOSION_MAXRADIUS, info.profile.MINI_EXPLOSION_MAXRADIUS)+15) / 16;
        for (int cx = chunkX - offset; cx <= chunkX + offset; cx++) {
            for (int cz = chunkZ - offset; cz <= chunkZ + offset; cz++) {
                ChunkCoord coord = new ChunkCoord(provider.getType(), cx, cz);
                if ((!info.profile.EXPLOSIONS_IN_CITIES_ONLY) || BuildingInfo.isCity(coord, provider)) {
                    Explosion explosion = getExplosionAt(coord, provider);
                    if (explosion != null) {
                        if (intersectsWith(explosion.getCenter(), explosion.getRadius())) {
//                            Float chance = BuildingInfo.getBuildingInfo(cx, cz, provider).getChunkCharacteristics(cx, cz, provider).cityStyle.getExplosionChance();
                            Float chance = BuildingInfo.getChunkCharacteristics(coord, provider).cityStyle.getExplosionChance();
                            if (chance == null || damageRandom.nextFloat() < chance) {
                                explosions.add(explosion);
                            }
                        }
                    }
                    explosion = getMiniExplosionAt(coord, provider);
                    if (explosion != null) {
                        if (intersectsWith(explosion.getCenter(), explosion.getRadius())) {
//                            Float chance = BuildingInfo.getBuildingInfo(cx, cz, provider).getChunkCharacteristics(cx, cz, provider).cityStyle.getExplosionChance();
                            Float chance = BuildingInfo.getChunkCharacteristics(coord, provider).cityStyle.getExplosionChance();
                            if (chance == null || damageRandom.nextFloat() < chance) {
                                explosions.add(explosion);
                            }
                        }
                    }
                }
            }
        }
    }

    public BlockState damageBlock(BlockState b, IDimensionInfo provider, int y, float damage, CompiledPalette palette, BlockState liquidChar) {
        if (Tools.hasTag(b.getBlock(), LostTags.NOT_BREAKABLE_TAG)) {
            return b;
        }

        if (Tools.hasTag(b.getBlock(), LostTags.EASY_BREAKABLE_TAG)) {
            damage *= 2.5f;    // As if this block gets double the damage
        }
        if (provider.getRandom().nextFloat() <= damage) {
            BlockState damaged = palette.canBeDamagedToIronBars(b);
            int waterlevel = Tools.getSeaLevel(provider.getWorld());//profile.GROUNDLEVEL - profile.WATERLEVEL_OFFSET;
            if (damage < BLOCK_DAMAGE_CHANCE && damaged != null) {
                if (provider.getRandom().nextFloat() < .7f) {
                    b = damaged;
                } else {
                    b = y <= waterlevel ? liquidChar : air;
                }
            } else {
                b = y <= waterlevel ? liquidChar : air;
            }
        }
        return b;
    }

    private boolean intersectsWith(BlockPos center, int radius) {
        double dmin = GeometryTools.squaredDistanceBoxPoint(chunkBox, center);
        return dmin <= radius * radius;
    }

    private Explosion getExplosionAt(ChunkCoord coord, IDimensionInfo provider) {
        Random randomExplosion = new Random(seed + coord.chunkZ() * 295075153L + coord.chunkX() * 797003437L);
        if (randomExplosion.nextFloat() < profile.EXPLOSION_CHANCE) {
            return new Explosion(profile.EXPLOSION_MINRADIUS + randomExplosion.nextInt(profile.EXPLOSION_MAXRADIUS - profile.EXPLOSION_MINRADIUS),
                    new BlockPos((coord.chunkX() << 4) + randomExplosion.nextInt(16),
                            BuildingInfo.getBuildingInfo(coord, provider).cityLevel * 6 + profile.EXPLOSION_MINHEIGHT + randomExplosion.nextInt(profile.EXPLOSION_MAXHEIGHT - profile.EXPLOSION_MINHEIGHT),
                            (coord.chunkZ() << 4) + randomExplosion.nextInt(16)));
        }
        return null;
    }

    private Explosion getMiniExplosionAt(ChunkCoord coord, IDimensionInfo provider) {
        Random randomMiniExplosion = new Random(seed + coord.chunkZ() * 1400305337L + coord.chunkX() * 573259391L);
        if (randomMiniExplosion.nextFloat() < profile.MINI_EXPLOSION_CHANCE) {
            return new Explosion(profile.MINI_EXPLOSION_MINRADIUS + randomMiniExplosion.nextInt(profile.MINI_EXPLOSION_MAXRADIUS - profile.MINI_EXPLOSION_MINRADIUS),
                    new BlockPos((coord.chunkX() << 4) + randomMiniExplosion.nextInt(16),
                            BuildingInfo.getBuildingInfo(coord, provider).cityLevel * 6 + profile.MINI_EXPLOSION_MINHEIGHT + randomMiniExplosion.nextInt(profile.MINI_EXPLOSION_MAXHEIGHT - profile.MINI_EXPLOSION_MINHEIGHT),
                            (coord.chunkZ() << 4) + randomMiniExplosion.nextInt(16)));
        }
        return null;
    }

    // Return true if this chunk is affected by explosions
    public boolean hasExplosions() {
        return !explosions.isEmpty();
    }

    public List<Explosion> getExplosions() {
        return explosions;
    }

    // Return true if this subchunk (every 16 blocks) is affected by explosions
    public boolean hasExplosions(int y) {
        AABB box = new AABB(chunkX << 4, y << 4, chunkZ << 4, (chunkX << 4) + 15, (y << 4) + 15, (chunkZ << 4) + 15);
        for (Explosion explosion : explosions) {
            double dmin = GeometryTools.squaredDistanceBoxPoint(box, explosion.getCenter());
            if (dmin <= explosion.getRadius() * explosion.getRadius()) {
                return true;
            }
        }
        return false;
    }

    // Return true if this subchunk is completely destroyed by an explosion
    public boolean isCompletelyDestroyed(int y) {
        AABB box = new AABB(chunkX << 4, y * 16, chunkZ << 4, (chunkX << 4) + 15, (y * 16) + 15, (chunkZ << 4) + 15);
        for (Explosion explosion : explosions) {
            double dmax = GeometryTools.maxSquaredDistanceBoxPoint(box, explosion.getCenter());
            int sqdist = explosion.getRadius() * explosion.getRadius();
            if (dmax <= sqdist) {
                // The distance at which this explosion is totally fatal (destroys all blocks)
                double dist = (explosion.getRadius() - 3.0 * explosion.getRadius()) / -3.0;
                dist *= dist;
                if (dmax <= dist) {
                    return true;
                }
            }
        }
        return false;

    }

    // Get the lowest height that is affected by an explosion
    public int getLowestExplosionHeight() {
        // @todo Technically not correct (should also cover below 0 and above 256)
        for (int yy = 0 ; yy < 16 ; yy++) {
            if (hasExplosions(yy)) {
                return yy * 16;
            }
        }
        return -1;
    }

    // Get the lowest height that is affected by an explosion
    public int getHighestExplosionHeight() {
        // @todo Technically not correct (should also cover below 0 and above 256)
        for (int yy = 15 ; yy >= 0 ; yy--) {
            if (hasExplosions(yy)) {
                return yy * 16 + 15;
            }
        }
        return -1;
    }

    // Give an indication of how much damage this chunk got
    public float getDamageFactor() {
        float damage = 0.0f;
        for (Explosion explosion : explosions) {
            double sq = explosion.getCenter().distToCenterSqr(chunkX * 16.0, explosion.getCenter().getY(), chunkZ * 16.0);
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
            double sq = explosion.getCenter().distToCenterSqr(x, y, z);
            if (sq < explosion.getSqradius()) {
                double d = Math.sqrt(sq);
                damage += 3.0f * (explosion.getRadius() - d) / explosion.getRadius();
            }
        }
        return damage;
    }

}
