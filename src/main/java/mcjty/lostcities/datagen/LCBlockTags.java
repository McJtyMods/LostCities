package mcjty.lostcities.datagen;

import mcjty.lostcities.LostCities;
import mcjty.lostcities.worldgen.LostTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.DataGenerator;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.BlockItemTags;
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
            BlockItemTags.SAPLINGS.block(),
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
                tag(LostTags.EASY_BREAKABLE_TAG).add(block.builtInRegistryHolder().key());
            }
        });

        tag(LostTags.ROTATABLE_TAG)
                .addTag(net.minecraft.tags.BlockTags.STAIRS)
                .addTag(net.minecraft.tags.BlockTags.DOORS);
        tag(LostTags.NOT_BREAKABLE_TAG).add(Blocks.BEDROCK.builtInRegistryHolder().key(), Blocks.END_PORTAL.builtInRegistryHolder().key(),
                Blocks.END_PORTAL_FRAME.builtInRegistryHolder().key(), Blocks.END_GATEWAY.builtInRegistryHolder().key());

        tag(LostTags.NEEDSPOI_TAG).add(Blocks.BREWING_STAND.builtInRegistryHolder().key(), Blocks.CAULDRON.builtInRegistryHolder().key(),
                Blocks.BARREL.builtInRegistryHolder().key(), Blocks.BLAST_FURNACE.builtInRegistryHolder().key(), Blocks.SMOKER.builtInRegistryHolder().key(),
                Blocks.COMPOSTER.builtInRegistryHolder().key(), Blocks.FLETCHING_TABLE.builtInRegistryHolder().key(), Blocks.LECTERN.builtInRegistryHolder().key(),
                Blocks.STONECUTTER.builtInRegistryHolder().key(), Blocks.LOOM.builtInRegistryHolder().key(), Blocks.SMITHING_TABLE.builtInRegistryHolder().key(),
                Blocks.GRINDSTONE.builtInRegistryHolder().key());
    }

    @Override
    @Nonnull
    public String getName() {
        return "LostCity Tags";
    }
}
