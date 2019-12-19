package mcjty.lostcities.setup;


import mcjty.lostcities.LostCities;
import mcjty.lostcities.dimensions.LostCityCarver;
import net.minecraft.world.gen.carver.WorldCarver;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;

@Mod.EventBusSubscriber(modid = LostCities.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Registration {

    @ObjectHolder("lostcities:lostcitycarver")
    public static WorldCarver LOSTCITY_CARVER;

    @SubscribeEvent
    public static void registerCarvers(final RegistryEvent.Register<WorldCarver<?>> event) {
        event.getRegistry().register(new LostCityCarver().setRegistryName("lostcitycarver"));
    }
}
