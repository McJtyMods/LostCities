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

        // Keep the lighting tag reproducible alongside the other generated block tags.
        builder(LostTags.LIGHTS_TAG).add(
                Blocks.LAVA.builtInRegistryHolder().key(), Blocks.BROWN_MUSHROOM.builtInRegistryHolder().key(), Blocks.TORCH.builtInRegistryHolder().key(), Blocks.WALL_TORCH.builtInRegistryHolder().key(),
                Blocks.FIRE.builtInRegistryHolder().key(), Blocks.SOUL_FIRE.builtInRegistryHolder().key(), Blocks.REDSTONE_TORCH.builtInRegistryHolder().key(), Blocks.REDSTONE_WALL_TORCH.builtInRegistryHolder().key(),
                Blocks.SOUL_TORCH.builtInRegistryHolder().key(), Blocks.SOUL_WALL_TORCH.builtInRegistryHolder().key(), Blocks.GLOWSTONE.builtInRegistryHolder().key(), Blocks.NETHER_PORTAL.builtInRegistryHolder().key(),
                Blocks.JACK_O_LANTERN.builtInRegistryHolder().key(), Blocks.ENCHANTING_TABLE.builtInRegistryHolder().key(), Blocks.BREWING_STAND.builtInRegistryHolder().key(), Blocks.LAVA_CAULDRON.builtInRegistryHolder().key(),
                Blocks.END_PORTAL.builtInRegistryHolder().key(), Blocks.END_PORTAL_FRAME.builtInRegistryHolder().key(), Blocks.DRAGON_EGG.builtInRegistryHolder().key(), Blocks.ENDER_CHEST.builtInRegistryHolder().key(),
                Blocks.BEACON.builtInRegistryHolder().key(), Blocks.LIGHT.builtInRegistryHolder().key(), Blocks.SEA_LANTERN.builtInRegistryHolder().key(), Blocks.END_ROD.builtInRegistryHolder().key(),
                Blocks.END_GATEWAY.builtInRegistryHolder().key(), Blocks.MAGMA_BLOCK.builtInRegistryHolder().key(), Blocks.SEA_PICKLE.builtInRegistryHolder().key(), Blocks.CONDUIT.builtInRegistryHolder().key(),
                Blocks.LANTERN.builtInRegistryHolder().key(), Blocks.SOUL_LANTERN.builtInRegistryHolder().key(), Blocks.CAMPFIRE.builtInRegistryHolder().key(), Blocks.SOUL_CAMPFIRE.builtInRegistryHolder().key(),
                Blocks.SHROOMLIGHT.builtInRegistryHolder().key(), Blocks.CRYING_OBSIDIAN.builtInRegistryHolder().key(), Blocks.AMETHYST_CLUSTER.builtInRegistryHolder().key(), Blocks.LARGE_AMETHYST_BUD.builtInRegistryHolder().key(),
                Blocks.MEDIUM_AMETHYST_BUD.builtInRegistryHolder().key(), Blocks.SMALL_AMETHYST_BUD.builtInRegistryHolder().key(), Blocks.SCULK_SENSOR.builtInRegistryHolder().key(), Blocks.CALIBRATED_SCULK_SENSOR.builtInRegistryHolder().key(),
                Blocks.SCULK_CATALYST.builtInRegistryHolder().key(), Blocks.OCHRE_FROGLIGHT.builtInRegistryHolder().key(), Blocks.VERDANT_FROGLIGHT.builtInRegistryHolder().key(), Blocks.PEARLESCENT_FROGLIGHT.builtInRegistryHolder().key());

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
