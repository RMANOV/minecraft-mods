package com.erik.medievalconquest.policy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class SiteLootPlanTest {
    static final List<String> SIX=List.of("medievalconquest:test_0","medievalconquest:test_1","medievalconquest:test_2","medievalconquest:test_3","medievalconquest:test_4","medievalconquest:test_5");
    static List<String> select(List<String> c,int min,int max) { return SiteLootPlan.select(0,"minecraft:overworld",-3,-7,0,c,min,max); }
    @Test void deterministicStrictSubsetAndCatalogPermutation() {
        var chosen=select(SIX,2,5);assertTrue(chosen.size()>=2 && chosen.size()<=5);assertEquals(chosen.size(),new HashSet<>(chosen).size());assertTrue(SIX.containsAll(chosen));
        assertEquals(chosen,select(SIX,2,5));var reversed=new ArrayList<>(SIX);Collections.reverse(reversed);assertEquals(chosen,select(reversed,2,5));
    }
    @Test void multipleSeedsVaryWithoutDemandingEverySeedDiffer() {
        var results=new HashSet<List<String>>();for(long seed=0;seed<128;seed++) {
            var c=SiteLootPlan.select(seed,"minecraft:overworld",-3,-7,0,SIX,2,5);assertTrue(c.size()>=2 && c.size()<=5);results.add(c);
        }assertTrue(results.size()>=2);
    }
    @Test void returnedSelectionHasNoMutableAliases() {
        var source=new ArrayList<>(SIX);var chosen=select(source,2,5);var saved=List.copyOf(chosen);source.clear();
        assertEquals(saved,chosen);assertTrue(chosen.size()>=2);assertThrows(UnsupportedOperationException.class,()->chosen.add("medievalconquest:test_x"));
    }
    @Test void boundsRejectWholeCatalogAndLiteralSixtyFive() {
        assertThrows(IllegalArgumentException.class,()->select(SIX,1,6));
        assertThrows(IllegalArgumentException.class,()->select(SIX,0,3));assertThrows(IllegalArgumentException.class,()->select(SIX,4,3));
        var large=new ArrayList<String>();for(int i=0;i<100;i++)large.add("medievalconquest:test_"+i);
        assertThrows(IllegalArgumentException.class,()->select(large,1,65));
        var valid=select(large,64,64);assertEquals(64,valid.size());
    }
    @Test void rejectsMalformedIdsDimensionVersionAndCatalogCapacity() {
        for(String bad:new String[]{"", " ", "bad", "a:b:c", "A:b", "a:", ":b", " a:b", "a:b ", "a:"+"b".repeat(127)}) {
            var c=new ArrayList<>(SIX);c.set(0,bad);assertThrows(IllegalArgumentException.class,()->select(c,2,5));
            assertThrows(IllegalArgumentException.class,()->SiteLootPlan.select(0,bad,0,0,0,SIX,2,5));
        }
        assertThrows(IllegalArgumentException.class,()->select(null,2,5));var withNull=new ArrayList<>(SIX);withNull.set(0,null);
        assertThrows(IllegalArgumentException.class,()->select(withNull,2,5));var duplicates=new ArrayList<>(SIX);duplicates.set(0,SIX.get(1));
        assertThrows(IllegalArgumentException.class,()->select(duplicates,2,5));
        assertThrows(IllegalArgumentException.class,()->SiteLootPlan.select(0,null,0,0,0,SIX,2,5));
        assertThrows(IllegalArgumentException.class,()->SiteLootPlan.select(0,"minecraft:overworld",0,0,-1,SIX,2,5));
        var large=new ArrayList<String>();for(int i=0;i<513;i++)large.add("medievalconquest:test_"+i);
        assertThrows(IllegalArgumentException.class,()->select(large,2,5));large.remove(512);assertTrue(select(large,2,5).size()>=2);
    }
    @Test void realSelectionLedgerClearRestoreCannotReinitialize() {
        var chosen=select(SIX,2,5);assertTrue(chosen.size()>=2);var ledger=new SiteLedger(2);
        assertTrue(ledger.initializeIfAbsent("overworld:-3,-7:v0",chosen));assertEquals(chosen,ledger.snapshot().get("overworld:-3,-7:v0"));
        ledger.clearContents("overworld:-3,-7:v0");var restored=SiteLedger.fromSnapshot(ledger.snapshot(),2);
        assertTrue(restored.snapshot().containsKey("overworld:-3,-7:v0"));assertEquals(List.of(),restored.snapshot().get("overworld:-3,-7:v0"));
        assertFalse(restored.initializeIfAbsent("overworld:-3,-7:v0",chosen));assertEquals(List.of(),restored.snapshot().get("overworld:-3,-7:v0"));
    }
}
