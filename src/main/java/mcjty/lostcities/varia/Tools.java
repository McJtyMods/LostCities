package mcjty.lostcities.varia;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.commons.lang3.StringUtils;

public class Tools {

    public static IBlockState stringToState(String s) {
        if (s.contains("@")) {
            String[] split = StringUtils.split(s, '@');
            Block value = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(split[0]));
            if (value == null) {
                throw new RuntimeException("Cannot find block: '" + split[0] + "'!");
            }
            try {
                int meta = Integer.parseInt(split[1]);
                return value.getStateFromMeta(meta);
            } catch (NumberFormatException e) {
                throw new RuntimeException("Bad meta for: '" + s + "'!");
            }
        } else {
            Block value = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(s));
            if (value == null) {
                throw new RuntimeException("Cannot find block: '" + s + "'!");
            }
            return value.getDefaultState();
        }
    }

    public static String stateToString(IBlockState state) {
        int meta = state.getBlock().getMetaFromState(state);
        if (meta == 0) {
            return state.getBlock().getRegistryName().toString();
        } else {
            return state.getBlock().getRegistryName().toString() + "@" + meta;
        }
    }

}
