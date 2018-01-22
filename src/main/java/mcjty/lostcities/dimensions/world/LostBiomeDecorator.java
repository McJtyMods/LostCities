package mcjty.lostcities.dimensions.world;

import net.minecraft.world.biome.BiomeDecorator;
import net.minecraftforge.event.terraingen.DeferredBiomeDecorator;

public class LostBiomeDecorator extends DeferredBiomeDecorator {

    public LostBiomeDecorator(BiomeDecorator wrappedOriginal) {
        super(wrappedOriginal);
    }


}
