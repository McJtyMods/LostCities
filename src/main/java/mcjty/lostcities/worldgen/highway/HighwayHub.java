package mcjty.lostcities.worldgen.highway;

public record HighwayHub(HubKey key, int chunkX, int chunkZ, int potentialScore, int cityLevel) {
    public float strength() {
        return potentialScore / 1_000_000.0f;
    }
}
