package mcjty.lostcities.datagen;

import mcjty.lostcities.LostCities;
import mcjty.lostcities.worldgen.LostTags;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.Set;

public class BlockTags extends BlockTagsProvider {

    public BlockTags(DataGenerator generator, ExistingFileHelper helper) {
        super(generator, LostCities.MODID, helper);
    }

    private static final Set<Material> PLANT_MATERIALS = Set.of(
            Material.PLANT,
            Material.WATER_PLANT,
            Material.REPLACEABLE_WATER_PLANT,
            Material.REPLACEABLE_PLANT,
            Material.REPLACEABLE_FIREPROOF_PLANT,
            Material.BAMBOO_SAPLING,
            Material.BAMBOO,
            Material.WOOD,
            Material.LEAVES);

    @Override
    protected void addTags() {
        for (Block block : ForgeRegistries.BLOCKS.getValues()) {
            if (PLANT_MATERIALS.contains(block.defaultBlockState().getMaterial())) {
                tag(LostTags.FOLIAGE_TAG).add(block);
            }
            if (block.defaultBlockState().getMaterial() == Material.GLASS) {
                tag(LostTags.EASY_BREAKABLE_TAG).add(block);
            }
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
