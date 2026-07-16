package mcjty.lostcities.worldgen.highway;

public record HighwayConnectionKey(HubKey first, HubKey second) implements Comparable<HighwayConnectionKey> {
    public HighwayConnectionKey {
        if (first.compareTo(second) >= 0) {
            throw new IllegalArgumentException("Highway connection endpoints must be distinct and canonical");
        }
    }

    public static HighwayConnectionKey of(HubKey a, HubKey b) {
        return a.compareTo(b) < 0 ? new HighwayConnectionKey(a, b) : new HighwayConnectionKey(b, a);
    }

    public HubKey owner() {
        return first;
    }

    @Override
    public int compareTo(HighwayConnectionKey other) {
        int comparison = first.compareTo(other.first);
        return comparison != 0 ? comparison : second.compareTo(other.second);
    }
}
