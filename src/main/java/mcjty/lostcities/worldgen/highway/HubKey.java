package mcjty.lostcities.worldgen.highway;

public record HubKey(int planningCellX, int planningCellZ) implements Comparable<HubKey> {
    @Override
    public int compareTo(HubKey other) {
        int comparison = Integer.compare(planningCellX, other.planningCellX);
        return comparison != 0 ? comparison : Integer.compare(planningCellZ, other.planningCellZ);
    }
}
