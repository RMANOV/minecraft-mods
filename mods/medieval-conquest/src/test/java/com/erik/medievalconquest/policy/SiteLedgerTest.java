package com.erik.medievalconquest.policy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class SiteLedgerTest {
    static final List<String> ITEMS=List.of("medievalconquest:test_a","medievalconquest:test_b");
    @Test void initializesOnlyOnceAndClearKeepsInitializedIdentity() {
        var l=new SiteLedger(2);assertTrue(l.initializeIfAbsent("a",ITEMS));assertTrue(l.initializeIfAbsent("b",ITEMS));
        assertFalse(l.initializeIfAbsent("a",List.of("medievalconquest:test_c")));assertEquals(ITEMS,l.snapshot().get("a"));
        l.clearContents("a");assertTrue(l.snapshot().containsKey("a"));assertEquals(List.of(),l.snapshot().get("a"));
        assertFalse(l.initializeIfAbsent("a",ITEMS));assertEquals(List.of(),l.snapshot().get("a"));
    }
    @Test void capacityFailureNeverEvictsEvenEmptyInitializedSite() {
        var l=new SiteLedger(2);l.initializeIfAbsent("a",ITEMS);l.initializeIfAbsent("b",ITEMS);l.clearContents("a");
        var before=l.snapshot();assertEquals(2,before.size());assertThrows(IllegalStateException.class,()->l.initializeIfAbsent("c",ITEMS));
        assertEquals(before,l.snapshot());assertFalse(l.initializeIfAbsent("a",ITEMS));
        var full=new SiteLedger();for(int i=0;i<4096;i++)assertTrue(full.initializeIfAbsent("site"+i,ITEMS));
        assertThrows(IllegalStateException.class,()->full.initializeIfAbsent("overflow",ITEMS));assertEquals(4096,full.snapshot().size());
    }
    @Test void deepImmutableSnapshotsHaveNoSourceAliases() {
        var input=new ArrayList<>(ITEMS);var l=new SiteLedger();assertTrue(l.initializeIfAbsent("a",input));input.clear();
        var saved=l.snapshot();assertEquals(ITEMS,saved.get("a"));assertThrows(UnsupportedOperationException.class,saved::clear);
        assertThrows(UnsupportedOperationException.class,()->saved.get("a").clear());l.clearContents("a");assertEquals(ITEMS,saved.get("a"));
        var source=new HashMap<String,List<String>>();var list=new ArrayList<>(ITEMS);source.put("a",list);source.put("empty",new ArrayList<>());
        var restored=SiteLedger.fromSnapshot(source,2);list.clear();source.clear();assertEquals(ITEMS,restored.snapshot().get("a"));
        assertTrue(restored.snapshot().containsKey("empty"));assertFalse(restored.initializeIfAbsent("empty",ITEMS));
    }
    @Test void clearUnknownOrInvalidSiteRejected() {
        var l=new SiteLedger();assertThrows(IllegalArgumentException.class,()->l.clearContents("unknown"));
        for(String bad:new String[]{"", " ", " a", "a ", "x".repeat(257)}) {
            assertThrows(IllegalArgumentException.class,()->l.clearContents(bad));assertThrows(IllegalArgumentException.class,()->l.initializeIfAbsent(bad,ITEMS));
        }assertThrows(IllegalArgumentException.class,()->l.clearContents(null));assertThrows(IllegalArgumentException.class,()->l.initializeIfAbsent(null,ITEMS));
        assertTrue(l.initializeIfAbsent("x".repeat(256),ITEMS));
    }
    @Test void completeIncomingDTOValidatedEvenForExistingSite() {
        var l=new SiteLedger();l.initializeIfAbsent("a",ITEMS);var before=l.snapshot();
        assertThrows(IllegalArgumentException.class,()->l.initializeIfAbsent("a",null));assertThrows(IllegalArgumentException.class,()->l.initializeIfAbsent("a",List.of()));
        assertThrows(IllegalArgumentException.class,()->l.initializeIfAbsent("a",List.of("a:b","a:b")));
        var nullList=new ArrayList<String>();nullList.add(null);assertThrows(IllegalArgumentException.class,()->l.initializeIfAbsent("a",nullList));
        for(String bad:new String[]{"", "a", "a:", ":b", "a:b:c", "A:b", " a:b", "a:b ", "a:"+"b".repeat(127)})
            assertThrows(IllegalArgumentException.class,()->l.initializeIfAbsent("a",List.of(bad)));
        var large=new ArrayList<String>();for(int i=0;i<65;i++)large.add("a:item_"+i);
        assertThrows(IllegalArgumentException.class,()->l.initializeIfAbsent("a",large));assertEquals(before,l.snapshot());
        large.remove(64);assertTrue(l.initializeIfAbsent("b",large));assertEquals(64,l.snapshot().get("b").size());
        assertTrue(l.initializeIfAbsent("id-bound",List.of("a:"+"b".repeat(126))));
    }
    @Test void snapshotValidationRejectsAllMalformedNestedRecords() {
        assertThrows(IllegalArgumentException.class,()->SiteLedger.fromSnapshot(null));
        var m=new HashMap<String,List<String>>();m.put(null,ITEMS);assertThrows(IllegalArgumentException.class,()->SiteLedger.fromSnapshot(m));
        m.clear();m.put("a",null);assertThrows(IllegalArgumentException.class,()->SiteLedger.fromSnapshot(m));
        m.clear();m.put("a",List.of("a:b","a:b"));assertThrows(IllegalArgumentException.class,()->SiteLedger.fromSnapshot(m));
        m.clear();var nested=new ArrayList<String>();nested.add(null);m.put("a",nested);assertThrows(IllegalArgumentException.class,()->SiteLedger.fromSnapshot(m));
        m.clear();m.put("a",List.of("bad"));assertThrows(IllegalArgumentException.class,()->SiteLedger.fromSnapshot(m));
        m.clear();var large=new ArrayList<String>();for(int i=0;i<65;i++)large.add("a:item_"+i);m.put("a",large);
        assertThrows(IllegalArgumentException.class,()->SiteLedger.fromSnapshot(m));
        assertThrows(IllegalArgumentException.class,()->SiteLedger.fromSnapshot(Map.of("a",ITEMS,"b",ITEMS),1));
        var many=new HashMap<String,List<String>>();for(int i=0;i<4097;i++)many.put("site"+i,List.of());
        assertThrows(IllegalArgumentException.class,()->SiteLedger.fromSnapshot(many));
    }
    @Test void capacityConstructorAndRestoreRejectInvalidLimits() {
        for(int cap:new int[]{0,-1,4097}) { assertThrows(IllegalArgumentException.class,()->new SiteLedger(cap));assertThrows(IllegalArgumentException.class,()->SiteLedger.fromSnapshot(Map.of(),cap)); }
        var one=new SiteLedger(1);assertTrue(one.initializeIfAbsent("a",ITEMS));assertThrows(IllegalStateException.class,()->one.initializeIfAbsent("b",ITEMS));
    }
}
