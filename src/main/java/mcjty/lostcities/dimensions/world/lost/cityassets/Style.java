package mcjty.lostcities.dimensions.world.lost.cityassets;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Style implements IAsset {

    private String name;

    private Map<String, IBlockState> styledBlocks = new HashMap<>();
    private Set<IBlockState> damagedToIronBars = new HashSet<>();

    public Style(String name) {
        this.name = name;
    }

    @Override
    public void readFromJSon(JsonObject object) {
        name = object.get("name").getAsString();
        JsonArray blocksArray = object.get("blocks").getAsJsonArray();
        for (JsonElement element : blocksArray) {
            String n = element.getAsJsonObject().get("name").getAsString();
            String block = element.getAsJsonObject().get("block").getAsString();
            int meta = element.getAsJsonObject().get("meta").getAsInt();
            Block value = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(block));
            if (value == null) {
                // @todo
                throw new RuntimeException("Cannot find block '" + block + "'!");
            }
            styledBlocks.put(n, value.getStateFromMeta(meta));
        }

        JsonArray ironArray = object.get("ironbars").getAsJsonArray();
        for (JsonElement element : ironArray) {
            String block = element.getAsJsonObject().get("block").getAsString();
            int meta = element.getAsJsonObject().get("meta").getAsInt();
            Block value = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(block));
            if (value == null) {
                // @todo
                throw new RuntimeException("Cannot find block '" + block + "'!");
            }
            damagedToIronBars.add(value.getStateFromMeta(meta));
        }
    }

    @Override
    public JsonObject writeToJSon() {
        JsonObject object = new JsonObject();
        object.add("type", new JsonPrimitive("style"));
        object.add("name", new JsonPrimitive(name));
        JsonArray array = new JsonArray();
        for (Map.Entry<String, IBlockState> entry : styledBlocks.entrySet()) {
            JsonObject o = new JsonObject();
            o.add("name", new JsonPrimitive(entry.getKey()));
            o.add("block", new JsonPrimitive(entry.getValue().getBlock().getRegistryName().toString()));
            o.add("meta", new JsonPrimitive(entry.getValue().getBlock().getMetaFromState(entry.getValue())));
            array.add(o);
        }
        object.add("blocks", array);

        array = new JsonArray();
        for (IBlockState state : damagedToIronBars) {
            JsonObject o = new JsonObject();
            o.add("block", new JsonPrimitive(state.getBlock().getRegistryName().toString()));
            o.add("meta", new JsonPrimitive(state.getBlock().getMetaFromState(state)));
            array.add(o);
        }
        object.add("ironbars", array);
        return object;
    }

    public Style(Style... styles) {
        this("__merged__");
        for (Style style : styles) {
            merge(style);
        }
    }

    public void merge(Style style) {
        styledBlocks.putAll(style.styledBlocks);
        damagedToIronBars.addAll(style.damagedToIronBars);
    }

    @Override
    public String getName() {
        return name;
    }

    public boolean isGlass(IBlockState b) {
        return b.getBlock() == Blocks.GLASS || b.getBlock() == Blocks.GLASS_PANE;
    }

    public boolean canBeDamagedToIronBars(IBlockState b) {
        return damagedToIronBars.contains(b);
    }

    public boolean isEasyToDestroy(IBlockState b) {
        return isGlass(b);
    }

    public boolean isLiquid(IBlockState b) {
        return b != null && (b.getBlock() instanceof BlockLiquid || b.getBlock() instanceof BlockDynamicLiquid);
    }

    /// Get a block defined by this style
    public IBlockState get(String name) {
        return styledBlocks.get(name);
    }

    public Style register(String name, IBlockState block) {
        styledBlocks.put(name, block);
        return this;
    }

    public Style register(String name, IBlockState block, boolean ironBars) {
        styledBlocks.put(name, block);
        if (ironBars) {
            damagedToIronBars.add(block);
        }
        return this;
    }
}
