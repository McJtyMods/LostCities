package mcjty.lostcities.varia;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

public class RFToolsTools {

    public static boolean chunkLoaded(World world, BlockPos pos) {
        return world.getChunkProvider().getLoadedChunk(pos.getX() >> 4, pos.getZ() >> 4) != null && world.getChunkFromBlockCoords(pos).isLoaded();
    }

    public static StringBuffer appendIndent(StringBuffer buffer, int indent) {
        return buffer.append(StringUtils.repeat(' ', indent));
    }

    public static void convertNBTtoJson(StringBuffer buffer, NBTTagList tagList, int indent) {
        for (int i = 0 ; i < tagList.tagCount() ; i++) {
            NBTTagCompound compound = tagList.getCompoundTagAt(i);
            appendIndent(buffer, indent).append("{\n");
            convertNBTtoJson(buffer, compound, indent + 4);
            appendIndent(buffer, indent).append("},\n");
        }
    }

    public static void convertNBTtoJson(StringBuffer buffer, NBTTagCompound tagCompound, int indent) {
        boolean first = true;
        for (Object o : tagCompound.getKeySet()) {
            if (!first) {
                buffer.append(",\n");
            }
            first = false;

            String key = (String) o;
            NBTBase tag = tagCompound.getTag(key);
            appendIndent(buffer, indent).append(key).append(':');
            if (tag instanceof NBTTagCompound) {
                NBTTagCompound compound = (NBTTagCompound) tag;
                buffer.append("{\n");
                convertNBTtoJson(buffer, compound, indent + 4);
                appendIndent(buffer, indent).append('}');
            } else if (tag instanceof NBTTagList) {
                NBTTagList list = (NBTTagList) tag;
                buffer.append("[\n");
                convertNBTtoJson(buffer, list, indent + 4);
                appendIndent(buffer, indent).append(']');
            } else {
                buffer.append(tag);
            }
        }
        if (!first) {
            buffer.append("\n");
        }
    }

    public static Map<String, String> modSourceID = null;

    public static String findModID(Object obj) {
        if (modSourceID == null) {
            modSourceID = new HashMap<>();
            for (ModContainer mod : Loader.instance().getModList()) {
                modSourceID.put(mod.getSource().getName(), mod.getModId());
            }

            modSourceID.put("1.8.0.jar", "minecraft");
            modSourceID.put("1.8.8.jar", "minecraft");
            modSourceID.put("1.8.9.jar", "minecraft");
            modSourceID.put("Forge", "minecraft");
        }


        String path;
        try {
            if (obj instanceof Class) {
                path = ((Class) obj).getProtectionDomain().getCodeSource().getLocation().toString();
            } else {
                path = obj.getClass().getProtectionDomain().getCodeSource().getLocation().toString();
            }
        } catch (Exception e) {
            return "<Unknown>";
        }
        try {
            path = URLDecoder.decode(path, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return "<Unknown>";
        }
        String modName = "<Unknown>";
        for (String s : modSourceID.keySet()) {
            if (path.contains(s)) {
                modName = modSourceID.get(s);
                break;
            }
        }
        if (modName.equals("Minecraft Coder Pack")) {
            modName = "minecraft";
        } else if (modName.equals("Forge")) {
            modName = "minecraft";
        }

        return modName;
    }
}
