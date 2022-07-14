package mcjty.lostcities.editor;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * When a player starts editing a part this structure is kept in memory.
 * WARNING! Not persisted!
 */
public class EditorInfo {
    private final String partName;
    private final BlockPos bottomLocation;
    private final Map<BlockState, Character> reversedPalette = new HashMap<>();

    private static final Map<UUID, EditorInfo> EDITING = new HashMap<>();

    public EditorInfo(String partName, BlockPos bottomLocation) {
        this.partName = partName;
        this.bottomLocation = bottomLocation;
    }

    public void addPaletteEntry(char c, BlockState state) {
        reversedPalette.put(state, c);
    }

    public String getPartName() {
        return partName;
    }

    public BlockPos getBottomLocation() {
        return bottomLocation;
    }

    public Character getPaleteEntry(BlockState state) {
        return reversedPalette.get(state);
    }

    public static EditorInfo getEditorInfo(UUID uuid) {
        return EDITING.get(uuid);
    }

    public static EditorInfo createEditorInfo(UUID uuid, String partName, BlockPos bottomLocation) {
        EditorInfo info = new EditorInfo(partName, bottomLocation);
        EDITING.put(uuid, info);
        return info;
    }
}
