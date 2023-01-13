package mcjty.lostcities.datagen;

import mcjty.lostcities.LostCities;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LostCities.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        if (event.includeServer()) {
            BlockTags blockTags = new BlockTags(generator, event.getLookupProvider(), event.getExistingFileHelper());
            generator.addProvider(event.includeServer(), blockTags);
        }
    }
}
