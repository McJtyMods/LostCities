package mcjty.lostcities.datagen;

import net.minecraft.data.DataGenerator;
import net.neoforged.neoforge.data.event.GatherDataEvent;

public class DataGenerators {

    public static void gatherData(GatherDataEvent.Server event) {
        DataGenerator generator = event.getGenerator();
        LCBlockTags blockTags = new LCBlockTags(generator, event.getWorldLookupProvider());
        generator.addProvider(true, blockTags);
    }
}
