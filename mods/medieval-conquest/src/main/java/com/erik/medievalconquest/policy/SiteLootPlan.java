package com.erik.medievalconquest.policy;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.SplittableRandom;
/** Deterministic bounded strict-subset selection, independent of catalog order. */
public final class SiteLootPlan {
    private SiteLootPlan() {}
    public static List<String> select(long seed, String dimension, int x, int z, int version, List<String> catalog, int min, int max) {
        validateResourceId(dimension);
        if (catalog == null || catalog.size() > 512 || version < 0 || min < 1
                || max < min || max > SiteLedger.MAX_ITEMS_PER_SITE || max >= catalog.size())
            throw new IllegalArgumentException("invalid catalog, version or selection bounds");
        var sorted = new ArrayList<String>(catalog.size());
        var unique = new HashSet<String>();
        for (String id : catalog) {
            validateResourceId(id);
            if (!unique.add(id)) throw new IllegalArgumentException("duplicate catalog item");
            sorted.add(id);
        }
        Collections.sort(sorted);
        long combined = seed ^ dimension.hashCode() ^ (x * 341873128712L)
                ^ (z * 132897987541L) ^ (version * 869001L);
        var random = new SplittableRandom(combined);
        int count = min + random.nextInt(max - min + 1);
        for (int i = sorted.size() - 1; i > 0; i--)
            Collections.swap(sorted, i, random.nextInt(i + 1));
        return List.copyOf(sorted.subList(0, count));
    }
    static void validateResourceId(String id) {
        if (id == null || id.length() > SiteLedger.MAX_ITEM_ID_CHARS
                || !id.matches("[a-z0-9_.-]+:[a-z0-9/._-]+"))
            throw new IllegalArgumentException("invalid resource identifier");
    }
}
