package mcjty.lostcities.playerdata;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public class PlayerProperties {

    public static Capability<PlayerSpawnSet> PLAYER_SPAWN_SET
            = CapabilityManager.get(new CapabilityToken<>(){});
}
