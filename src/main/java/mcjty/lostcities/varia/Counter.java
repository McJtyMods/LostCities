package mcjty.lostcities.varia;

import java.util.HashMap;
import java.util.Map;

public class Counter<T> {
    private Map<T, Integer> internalMap = new HashMap<>();

    public void add(T key) {
        if (!internalMap.containsKey(key)) {
            internalMap.put(key, 0);
        }
        internalMap.put(key, internalMap.get(key)+1);
    }

    public Map<T, Integer> getMap() {
        return internalMap;
    }

    public int get(T key) {
        if (internalMap.containsKey(key)) {
            return internalMap.get(key);
        } else {
            return 0;
        }
    }

    public T getMostOccuring() {
        T max = null;
        int maxCount = -1;
        for (Map.Entry<T, Integer> entry : internalMap.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                max = entry.getKey();
            }
        }
        return max;
    }
}
