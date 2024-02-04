package mcjty.lostcities.datagen;

import mcjty.lostcities.LostCities;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

@Mod.EventBusSubscriber(modid = LostCities.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        if (event.includeServer()) {
            LCBlockTags blockTags = new LCBlockTags(generator, event.getExistingFileHelper());
            generator.addProvider(blockTags);
        }
    }
}
