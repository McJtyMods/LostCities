package mcjty.lostcities.api;

import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * LostCityEvent is fired whenever an event involving a Lost City chunk generation occurs. <br>
 * If a method utilizes this {@link Event} as its parameter, the method will
 * receive every child event of this class.<br>
 * <br>
 * All children of this event are fired on the {@link MinecraftForge#EVENT_BUS}.
 **/
public class LostCityEvent extends Event {

    private final World world;
    private final ILostChunkGenerator generator;
    private final int chunkX;
    private final int chunkZ;

    public LostCityEvent(World world, ILostChunkGenerator generator, int chunkX, int chunkZ) {
        this.world = world;
        this.generator = generator;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    }

    public World getWorld() {
        return world;
    }

    public ILostChunkGenerator getGenerator() {
        return generator;
    }

    public int getChunkX() {
        return chunkX;
    }

    public int getChunkZ() {
        return chunkZ;
    }

    /**
     * CharacteristicsEvent is fired when Lost Cities tries to determine chunk chracteristics.<br>
     * This event is fired right when The Lost Cities tries to decide if a chunk should contain
     * a building and what type of building. All fields in the given characteristic object can be modified.
     * Note that you can get access to the asset registries for buildings, multi buildings, and city styles
     * from the given ILostChunkGenerator instance. <br>
     * NOTE! This will be called for every chunk (city or normal). <br>
     * WARNING! Do *not* call ILostChunkGenerator.getChunkInfo() from here as that might cause infinite
     * recursion!
     * <br>
     * {@link #characteristics} contains the {@link LostChunkCharacteristics} that was generated for this chunk. <br>
     * <br>
     * This event is not {@link Cancelable}.<br>
     * <br>
     * This event does not have a result. {@link HasResult}<br>
     * <br>
     * This event is fired on the {@link MinecraftForge#EVENT_BUS}.
     **/
    public static class CharacteristicsEvent extends LostCityEvent {
        private final LostChunkCharacteristics characteristics;

        public CharacteristicsEvent(World world, ILostChunkGenerator generator, int chunkX, int chunkZ, LostChunkCharacteristics characteristics) {
            super(world, generator, chunkX, chunkZ);
            this.characteristics = characteristics;
        }

        public LostChunkCharacteristics getCharacteristics() {
            return characteristics;
        }
    }

    /**
     * PreGenCityChunkEvent is fired right before generation of a city chunk (street or building).<br>
     * This is fired right before generation of a city chunk. If you cancel this event then The Lost Cities
     * will not generate the street or building. Everything else will still be generated (like the subways, highways,
     * and the stone up to city level for this chunk). If you don't cancel this then you can still modify the primer
     * but keep in mind that the street or building will be generated after this and might overwrite what you did.<br>
     * NOTE! This will only be called for city chunks (buildings or street). <br>
     * <br>
     * {@link #primer} contains the {@link ChunkPrimer} for this chunk. This primer will already be filled with stone up to city level. <br>
     * <br>
     * This event is {@link Cancelable}.<br>
     * <br>
     * This event does not have a result. {@link HasResult}<br>
     * <br>
     * This event is fired on the {@link MinecraftForge#EVENT_BUS}.
     **/
    @Cancelable
    public static class PreGenCityChunkEvent extends LostCityEvent {
        private final ChunkPrimer primer;

        public PreGenCityChunkEvent(World world, ILostChunkGenerator generator, int chunkX, int chunkZ, ChunkPrimer primer) {
            super(world, generator, chunkX, chunkZ);
            this.primer = primer;
        }

        public ChunkPrimer getPrimer() {
            return primer;
        }
    }

    /**
     * PostGenCityChunkEvent is fired right after generation of the street or building of a city chunk (street or building).<br>
     * This is fired right after generation of the street or building but before highways, subways and other stuff like that.
     * This is mostly useful in case you want to modify the standard Lost City building/street after it has been generated.<br>
     * NOTE! This will only be called for city chunks (buildings or street). <br>
     * <br>
     * {@link #primer} contains the {@link ChunkPrimer} for this chunk. This primer will already have the building and street stuff in it. <br>
     * <br>
     * This event is not {@link Cancelable}.<br>
     * <br>
     * This event does not have a result. {@link HasResult}<br>
     * <br>
     * This event is fired on the {@link MinecraftForge#EVENT_BUS}.
     **/
    public static class PostGenCityChunkEvent extends LostCityEvent {
        private final ChunkPrimer primer;

        public PostGenCityChunkEvent(World world, ILostChunkGenerator generator, int chunkX, int chunkZ, ChunkPrimer primer) {
            super(world, generator, chunkX, chunkZ);
            this.primer = primer;
        }

        public ChunkPrimer getPrimer() {
            return primer;
        }
    }

    /**
     * PreExplosionEvent fired after chunk generation but before explosion damage is done.<br>
     * If you cancel this event then no explosion damage will be done. This event is the final chance
     * to modify the chunk before explosion damage is calculated.
     * NOTE! This will be called for every chunk (city or normal). <br>
     * <br>
     * {@link #primer} contains the {@link ChunkPrimer} for this chunk. <br>
     * <br>
     * This event is {@link Cancelable}.<br>
     * <br>
     * This event does not have a result. {@link HasResult}<br>
     * <br>
     * This event is fired on the {@link MinecraftForge#EVENT_BUS}.
     **/
    @Cancelable
    public static class PreExplosionEvent extends LostCityEvent {
        private final ChunkPrimer primer;

        public PreExplosionEvent(World world, ILostChunkGenerator generator, int chunkX, int chunkZ, ChunkPrimer primer) {
            super(world, generator, chunkX, chunkZ);
            this.primer = primer;
        }

        public ChunkPrimer getPrimer() {
            return primer;
        }
    }

}
