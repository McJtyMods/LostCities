package mcjty.lostcities.datagen;

import mcjty.lostcities.LostCities;
import mcjty.lostcities.worldgen.LostTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class LCBlockTags extends BlockTagsProvider {

    public LCBlockTags(DataGenerator generator, CompletableFuture<HolderLookup.Provider> lookupProvider , ExistingFileHelper helper) {
        super(generator.getPackOutput(), lookupProvider, LostCities.MODID, helper);
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
        tag(LostTags.EASY_BREAKABLE_TAG).addTags(Tags.Blocks.GLASS);
        for (Block block : ForgeRegistries.BLOCKS.getValues()) {
            if (block.defaultBlockState().getLightEmission() > 0) {
                tag(LostTags.LIGHTS_TAG).add(block);
            }
        }

        tag(LostTags.ROTATABLE_TAG).addTag(net.minecraft.tags.BlockTags.STAIRS);
        tag(LostTags.NOT_BREAKABLE_TAG).add(Blocks.BEDROCK, Blocks.END_PORTAL, Blocks.END_PORTAL_FRAME, Blocks.END_GATEWAY);
    }

    @Override
    @Nonnull
    public String getName() {
        return "LostCity Tags";
    }
}
