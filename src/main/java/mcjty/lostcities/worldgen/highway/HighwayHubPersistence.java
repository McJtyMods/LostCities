package mcjty.lostcities.worldgen.highway;

import java.util.Optional;

/**
 * Persistent backing for the expensive zero-or-one hub decision of a planning cell.
 */
public interface HighwayHubPersistence {

    HighwayHubPersistence NONE = new HighwayHubPersistence() {
        @Override
        public Lookup get(HubKey key) {
            return Lookup.missing();
        }

        @Override
        public void put(HubKey key, Optional<HighwayHub> hub) {
        }
    };

    Lookup get(HubKey key);

    void put(HubKey key, Optional<HighwayHub> hub);

    record Lookup(boolean known, Optional<HighwayHub> hub) {
        public static Lookup missing() {
            return new Lookup(false, Optional.empty());
        }

        public static Lookup known(Optional<HighwayHub> hub) {
            return new Lookup(true, hub);
        }
    }
}
