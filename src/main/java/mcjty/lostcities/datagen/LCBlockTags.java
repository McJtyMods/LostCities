package mcjty.lostcities.datagen;

import mcjty.lostcities.worldgen.LostTags;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockItemTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class LCBlockTags extends FabricTagsProvider.BlockTagsProvider {

    private static final Set<TagKey<Block>> PLANT_TAGS = Set.of(
            BlockTags.CORAL_PLANTS,
            BlockTags.BAMBOO_BLOCKS,
            BlockTags.LOGS,
            BlockTags.LEAVES,
            BlockItemTags.SAPLINGS.block(),
            BlockTags.FLOWERS
    );

    public LCBlockTags(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        for (TagKey<Block> tag : PLANT_TAGS) {
            builder(LostTags.FOLIAGE_TAG).addOptionalTag(tag);
        }
        builder(LostTags.EASY_BREAKABLE_TAG).addOptionalTag(ConventionalBlockTags.GLASS_BLOCKS);
        BuiltInRegistries.BLOCK.stream().forEach(block -> {
            if (block.defaultBlockState().getDestroySpeed(null, null) < 0.6f) {
                builder(LostTags.EASY_BREAKABLE_TAG).add(block.builtInRegistryHolder().key());
            }
        });

        builder(LostTags.ROTATABLE_TAG).addOptionalTag(BlockTags.STAIRS).addOptionalTag(BlockTags.DOORS);
        builder(LostTags.NOT_BREAKABLE_TAG).add(
                Blocks.BEDROCK.builtInRegistryHolder().key(),
                Blocks.END_PORTAL.builtInRegistryHolder().key(),
                Blocks.END_PORTAL_FRAME.builtInRegistryHolder().key(),
                Blocks.END_GATEWAY.builtInRegistryHolder().key());

        builder(LostTags.NEEDSPOI_TAG).add(
                Blocks.BREWING_STAND.builtInRegistryHolder().key(), Blocks.CAULDRON.builtInRegistryHolder().key(),
                Blocks.BARREL.builtInRegistryHolder().key(), Blocks.BLAST_FURNACE.builtInRegistryHolder().key(),
                Blocks.SMOKER.builtInRegistryHolder().key(), Blocks.COMPOSTER.builtInRegistryHolder().key(),
                Blocks.FLETCHING_TABLE.builtInRegistryHolder().key(), Blocks.LECTERN.builtInRegistryHolder().key(),
                Blocks.STONECUTTER.builtInRegistryHolder().key(), Blocks.LOOM.builtInRegistryHolder().key(),
                Blocks.SMITHING_TABLE.builtInRegistryHolder().key(), Blocks.GRINDSTONE.builtInRegistryHolder().key());
    }

    @Override
    @Nonnull
    public String getName() {
        return "Lost Cities block tags";
    }
}
