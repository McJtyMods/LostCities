package mcjty.lostcities;

import mcjty.lostcities.proxy.CommonProxy;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@Mod(modid = LostCities.MODID, name="RFTools Dimensions",
        dependencies =
                        "required-after:compatlayer@[" + LostCities.COMPATLAYER_VER + ",);" +
                        "after:Forge@[" + LostCities.MIN_FORGE10_VER + ",);" +
                        "after:forge@[" + LostCities.MIN_FORGE11_VER + ",)",
        version = LostCities.VERSION,
        acceptableRemoteVersions = "*",
        acceptedMinecraftVersions = "[1.10,1.12)")
public class LostCities {
    public static final String MODID = "lostcities";
    public static final String VERSION = "0.0.3beta";
    public static final String MIN_FORGE10_VER = "12.18.1.2082";
    public static final String MIN_FORGE11_VER = "13.19.0.2176";
    public static final String COMPATLAYER_VER = "0.1.6";

    @SidedProxy(clientSide="mcjty.lostcities.proxy.ClientProxy", serverSide="mcjty.lostcities.proxy.ServerProxy")
    public static CommonProxy proxy;

    @Mod.Instance("lostcities")
    public static LostCities instance;

    public static boolean chisel = false;
    public static boolean biomesoplenty = false;

    /**
     * Run before anything else. Read your config, create blocks, items, etc, and
     * register them with the GameRegistry.
     */
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        chisel = Loader.isModLoaded("chisel");
        biomesoplenty = Loader.isModLoaded("biomesoplenty") || Loader.isModLoaded("BiomesOPlenty");
        this.proxy.preInit(e);
    }

    /**
     * Do your mod setup. Build whatever data structures you care about. Register recipes.
     */
    @Mod.EventHandler
    public void init(FMLInitializationEvent e) {
        this.proxy.init(e);
    }

    @Mod.EventHandler
    public void serverLoad(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandDebug());
        event.registerServerCommand(new CommandSaveAssets());
    }

    /**
     * Handle interaction with other mods, complete your setup based on this.
     */
    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent e) {
        this.proxy.postInit(e);
    }
}
