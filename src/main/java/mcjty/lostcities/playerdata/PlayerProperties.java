package mcjty.lostcities.playerdata;

import net.neoforged.neoforge.common.capabilities.Capability;
import net.neoforged.neoforge.common.capabilities.CapabilityManager;
import net.neoforged.neoforge.common.capabilities.CapabilityToken;

public class PlayerProperties {

    public static Capability<PlayerSpawnSet> PLAYER_SPAWN_SET
            = CapabilityManager.get(new CapabilityToken<>(){});
}
