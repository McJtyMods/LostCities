package mcjty.lostcities.datagen;

import mcjty.lostcities.LostCities;
import net.minecraft.data.DataGenerator;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LostCities.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        if (event.includeServer()) {
            LCBlockTags blockTags = new LCBlockTags(generator, event.getLookupProvider(), event.getExistingFileHelper());
            generator.addProvider(event.includeServer(), blockTags);
        }
    }
}
