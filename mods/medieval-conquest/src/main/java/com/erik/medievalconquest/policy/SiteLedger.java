package com.erik.medievalconquest.policy;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
/** Bounded in-memory initialized-site ledger; empty identities are retained. */
public final class SiteLedger {
    public static final int MAX_SITES=4096, MAX_SITE_ID_CHARS=256, MAX_ITEMS_PER_SITE=64, MAX_ITEM_ID_CHARS=128;
    private final int capacity;
    private final Map<String,List<String>> sites = new HashMap<>();
    public SiteLedger() { this(MAX_SITES); }
    public SiteLedger(int capacity) {
        if (capacity < 1 || capacity > MAX_SITES) throw new IllegalArgumentException("invalid site capacity");
        this.capacity = capacity;
    }
    public boolean initializeIfAbsent(String site, List<String> items) {
        validateSite(site);
        List<String> copy = validatedItems(items, false);
        if (sites.containsKey(site)) return false;
        if (sites.size() >= capacity) throw new IllegalStateException("site capacity exhausted");
        sites.put(site, copy);
        return true;
    }
    public void clearContents(String site) {
        validateSite(site);
        if (!sites.containsKey(site)) throw new IllegalArgumentException("unknown site");
        sites.put(site, List.of());
    }
    public Map<String,List<String>> snapshot() { return Map.copyOf(sites); }
    public static SiteLedger fromSnapshot(Map<String,List<String>> saved) { return fromSnapshot(saved, MAX_SITES); }
    public static SiteLedger fromSnapshot(Map<String,List<String>> saved, int capacity) {
        SiteLedger ledger = new SiteLedger(capacity);
        if (saved == null || saved.size() > capacity) throw new IllegalArgumentException("invalid site snapshot");
        for (var entry : saved.entrySet()) {
            validateSite(entry.getKey());
            ledger.sites.put(entry.getKey(), validatedItems(entry.getValue(), true));
        }
        return ledger;
    }
    private static void validateSite(String site) {
        if (site == null || site.isBlank() || site.length() > MAX_SITE_ID_CHARS || !site.equals(site.strip()))
            throw new IllegalArgumentException("invalid site identifier");
    }
    private static List<String> validatedItems(List<String> items, boolean allowEmpty) {
        if (items == null || items.size() > MAX_ITEMS_PER_SITE || (!allowEmpty && items.isEmpty()))
            throw new IllegalArgumentException("invalid site contents");
        var unique = new HashSet<String>();
        for (String item : items) {
            SiteLootPlan.validateResourceId(item);
            if (!unique.add(item)) throw new IllegalArgumentException("duplicate site item");
        }
        return List.copyOf(items);
    }
}
