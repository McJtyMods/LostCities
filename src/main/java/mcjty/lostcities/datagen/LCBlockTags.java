package mcjty.lostcities.datagen;

import mcjty.lostcities.LostCities;
import mcjty.lostcities.worldgen.LostTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.DataGenerator;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class LCBlockTags extends BlockTagsProvider {

    public LCBlockTags(DataGenerator generator, CompletableFuture<HolderLookup.Provider> lookupProvider ) {
        super(generator.getPackOutput(), lookupProvider, LostCities.MODID);
    }

    private static final Set<TagKey<Block>> PLANT_TAGS = Set.of(
            BlockTags.CORAL_PLANTS,
            BlockTags.BAMBOO_BLOCKS,
            BlockTags.LOGS,
            BlockTags.LEAVES,
            BlockTags.SAPLINGS,
            BlockTags.FLOWERS
    );

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        for (TagKey<Block> tag : PLANT_TAGS) {
            tag(LostTags.FOLIAGE_TAG).addTag(tag);
        }
        tag(LostTags.EASY_BREAKABLE_TAG).addTags(Tags.Blocks.GLASS_BLOCKS);
        BuiltInRegistries.BLOCK.stream().forEach(block -> {
            if (block.defaultBlockState().getDestroySpeed(null, null) < 0.6f) {
                tag(LostTags.EASY_BREAKABLE_TAG).add(block);
            }
        });

        // Keep the lighting tag reproducible alongside the other generated block tags.
        tag(LostTags.LIGHTS_TAG).add(
                Blocks.LAVA, Blocks.BROWN_MUSHROOM, Blocks.TORCH, Blocks.WALL_TORCH,
                Blocks.FIRE, Blocks.SOUL_FIRE, Blocks.REDSTONE_TORCH, Blocks.REDSTONE_WALL_TORCH,
                Blocks.SOUL_TORCH, Blocks.SOUL_WALL_TORCH, Blocks.GLOWSTONE, Blocks.NETHER_PORTAL,
                Blocks.JACK_O_LANTERN, Blocks.ENCHANTING_TABLE, Blocks.BREWING_STAND, Blocks.LAVA_CAULDRON,
                Blocks.END_PORTAL, Blocks.END_PORTAL_FRAME, Blocks.DRAGON_EGG, Blocks.ENDER_CHEST,
                Blocks.BEACON, Blocks.LIGHT, Blocks.SEA_LANTERN, Blocks.END_ROD,
                Blocks.END_GATEWAY, Blocks.MAGMA_BLOCK, Blocks.SEA_PICKLE, Blocks.CONDUIT,
                Blocks.LANTERN, Blocks.SOUL_LANTERN, Blocks.CAMPFIRE, Blocks.SOUL_CAMPFIRE,
                Blocks.SHROOMLIGHT, Blocks.CRYING_OBSIDIAN, Blocks.AMETHYST_CLUSTER, Blocks.LARGE_AMETHYST_BUD,
                Blocks.MEDIUM_AMETHYST_BUD, Blocks.SMALL_AMETHYST_BUD, Blocks.SCULK_SENSOR, Blocks.CALIBRATED_SCULK_SENSOR,
                Blocks.SCULK_CATALYST, Blocks.OCHRE_FROGLIGHT, Blocks.VERDANT_FROGLIGHT, Blocks.PEARLESCENT_FROGLIGHT);

        tag(LostTags.ROTATABLE_TAG)
                .addTag(net.minecraft.tags.BlockTags.STAIRS)
                .addTag(net.minecraft.tags.BlockTags.DOORS);
        tag(LostTags.NOT_BREAKABLE_TAG).add(Blocks.BEDROCK, Blocks.END_PORTAL, Blocks.END_PORTAL_FRAME, Blocks.END_GATEWAY);

        tag(LostTags.NEEDSPOI_TAG).add(Blocks.BREWING_STAND, Blocks.CAULDRON, Blocks.BARREL, Blocks.BLAST_FURNACE, Blocks.SMOKER,
                Blocks.COMPOSTER, Blocks.FLETCHING_TABLE, Blocks.LECTERN, Blocks.STONECUTTER, Blocks.LOOM, Blocks.SMITHING_TABLE, Blocks.GRINDSTONE);
    }

    @Override
    @Nonnull
    public String getName() {
        return "LostCity Tags";
    }
}
