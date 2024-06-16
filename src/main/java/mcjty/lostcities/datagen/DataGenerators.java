package mcjty.lostcities.datagen;

import net.minecraft.data.DataGenerator;
import net.neoforged.neoforge.data.event.GatherDataEvent;

public class DataGenerators {

    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        if (event.includeServer()) {
            LCBlockTags blockTags = new LCBlockTags(generator, event.getLookupProvider(), event.getExistingFileHelper());
            generator.addProvider(event.includeServer(), blockTags);
        }
    }
}
