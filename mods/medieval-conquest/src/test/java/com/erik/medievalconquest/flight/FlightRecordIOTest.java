package com.erik.medievalconquest.flight;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueOutput;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Behavioral source-preparation tests: no compilation or RED run has been admitted yet. */
class FlightRecordIOTest {
    private static final FlightRecord LOCKED =
            new FlightRecord(FlightRecord.Phase.LOCKED, false, 0f);

    private static CompoundTag valid(byte phase, byte custom, float hp) {
        var tag = new CompoundTag();
        tag.putInt("v", 1);
        tag.putByte("p", phase);
        tag.putByte("custom", custom);
        tag.putFloat("hp", hp);
        return tag;
    }

    private static Map<String, Object> malformedPresentValues() {
        var cases = new LinkedHashMap<String, Object>();
        var scalar = new CompoundTag();
        scalar.putString("string", "present, not absent");
        scalar.putInt("integer", 1);
        cases.put("StringTag root", scalar.get("string"));
        cases.put("IntTag root", scalar.get("integer"));
        cases.put("ListTag root", new ListTag());
        cases.put("null raw value is malformed when decodeRaw is called", null);
        var tag = valid((byte) 1, (byte) 1, 20f);
        tag.put("hp", new CompoundTag());
        cases.put("nested CompoundTag hp", tag);
        tag = valid((byte) 1, (byte) 1, 20f);
        tag.put("custom", new ListTag());
        cases.put("ListTag custom", tag);
        tag = valid((byte) 1, (byte) 1, 20f);
        tag.remove("hp");
        cases.put("missing hp", tag);
        tag = valid((byte) 1, (byte) 1, 20f);
        tag.putInt("x", 0);
        cases.put("extra x", tag);
        tag = valid((byte) 1, (byte) 1, 20f);
        tag.putByte("v", (byte) 1);
        cases.put("v ByteTag instead of IntTag", tag);
        tag = valid((byte) 1, (byte) 1, 20f);
        tag.putInt("v", 2);
        cases.put("unknown version", tag);
        tag = valid((byte) 1, (byte) 1, 20f);
        tag.putInt("p", 1);
        cases.put("p IntTag instead of ByteTag", tag);
        for (byte phase : new byte[] {0, 4}) {
            cases.put("invalid phase " + phase, valid(phase, (byte) 1, 20f));
        }
        tag = valid((byte) 1, (byte) 1, 20f);
        tag.putInt("custom", 1);
        cases.put("custom IntTag instead of ByteTag", tag);
        for (byte custom : new byte[] {-1, 2}) {
            cases.put("invalid custom " + custom, valid((byte) 1, custom, 20f));
        }
        tag = valid((byte) 1, (byte) 1, 20f);
        tag.putDouble("hp", 20d);
        cases.put("hp DoubleTag instead of FloatTag", tag);
        for (float hp : new float[] {-1f, Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY}) {
            cases.put("invalid hp " + hp, valid((byte) 1, (byte) 1, hp));
        }
        return cases;
    }

    private static CompoundTag writeRecord(FlightRecord record) {
        var out = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
        FlightRecordIO.write(out, record);
        return assertInstanceOf(CompoundTag.class, out.buildResult().get(FlightRecordIO.FIELD),
                "a present record must emit a field, not disappear or fail by a null cast");
    }

    private static void assertCanonicalLocked(CompoundTag tag) {
        assertEquals(Set.of("v", "p", "custom", "hp"), tag.keySet());
        assertEquals(4, tag.size());
        assertEquals(1, assertInstanceOf(IntTag.class, tag.get("v")).value());
        assertEquals((byte) 3, assertInstanceOf(ByteTag.class, tag.get("p")).value());
        assertEquals((byte) 0, assertInstanceOf(ByteTag.class, tag.get("custom")).value());
        assertEquals(0f, assertInstanceOf(FloatTag.class, tag.get("hp")).value());
    }

    @Test void nestedHpIsLockedRatherThanAbsent() {
        var raw = valid((byte) 1, (byte) 1, 20f);
        raw.put("hp", new CompoundTag());
        assertEquals(LOCKED, FlightRecordIO.decodeRaw(raw));
    }

    @Test void allMalformedPresentTypesAndValuesBecomeCanonicalLocked() {
        var cases = malformedPresentValues();
        assertTrue(cases.size() >= 7, "keep malformed type/value coverage explicit");
        cases.forEach((name, raw) -> assertEquals(LOCKED, FlightRecordIO.decodeRaw(raw), name));
    }

    @Test void malformedRecordsRewriteToOnlyFourCanonicalFields() {
        malformedPresentValues().forEach((name, raw) -> {
            var decoded = FlightRecordIO.decodeRaw(raw);
            assertEquals(LOCKED, decoded, name);
            assertCanonicalLocked(writeRecord(decoded));
        });
    }

    @Test void validActiveAndPendingScalarsPreserveCapturedValues() {
        assertEquals(new FlightRecord(FlightRecord.Phase.ACTIVE, true, 20f),
                FlightRecordIO.decodeRaw(valid((byte) 1, (byte) 1, 20f)));
        assertEquals(new FlightRecord(FlightRecord.Phase.PENDING, true, 1f),
                FlightRecordIO.decodeRaw(valid((byte) 2, (byte) 1, 1f)));
    }

    @Test void validNonCustomRecordsAreNotFabricatedCurses() {
        assertEquals(new FlightRecord(FlightRecord.Phase.ACTIVE, false, 20f),
                FlightRecordIO.decodeRaw(valid((byte) 1, (byte) 0, 20f)));
        assertEquals(new FlightRecord(FlightRecord.Phase.PENDING, false, 1f),
                FlightRecordIO.decodeRaw(valid((byte) 2, (byte) 0, 1f)));
    }

    @Test void lockedPhaseCanonicalizesWithoutUnlocking() {
        assertEquals(LOCKED, FlightRecordIO.decodeRaw(valid((byte) 3, (byte) 1, 20f)));
        assertCanonicalLocked(writeRecord(new FlightRecord(FlightRecord.Phase.LOCKED, true, 20f)));
    }

    @Test void writerUsesExactScalarTypesAndCapturedValues() {
        var raw = writeRecord(new FlightRecord(FlightRecord.Phase.PENDING, true, 1f));
        assertEquals(Set.of("v", "p", "custom", "hp"), raw.keySet());
        assertEquals(1, assertInstanceOf(IntTag.class, raw.get("v")).value());
        assertEquals((byte) 2, assertInstanceOf(ByteTag.class, raw.get("p")).value());
        assertEquals((byte) 1, assertInstanceOf(ByteTag.class, raw.get("custom")).value());
        assertEquals(1f, assertInstanceOf(FloatTag.class, raw.get("hp")).value());
    }

    @Test void absentRecordDiscardsPreviouslyWrittenField() {
        var out = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
        FlightRecordIO.write(out, new FlightRecord(FlightRecord.Phase.ACTIVE, true, 20f));
        assertInstanceOf(CompoundTag.class, out.buildResult().get(FlightRecordIO.FIELD),
                "establish a real present field before testing discard");
        FlightRecordIO.write(out, null);
        assertNull(out.buildResult().get(FlightRecordIO.FIELD));
    }

    @Test void canonicalRecordUsesFewerThan128ActualEncodedBytes() throws IOException {
        var raw = writeRecord(new FlightRecord(FlightRecord.Phase.ACTIVE, true, 20f));
        var bytes = new ByteArrayOutputStream();
        NbtIo.write(raw, new DataOutputStream(bytes));
        assertTrue(bytes.size() < 128, "bound actual fresh canonical NBT, not arbitrary malformed input");
        assertEquals(4, raw.size());
        assertEquals(new FlightRecord(FlightRecord.Phase.ACTIVE, true, 20f), FlightRecordIO.decodeRaw(raw));
    }

    @Test void basicRecordRejectsInvalidHealthAndHasImmutablePendingConversion() {
        assertThrows(IllegalArgumentException.class, () -> new FlightRecord(null, true, 1f));
        for (float hp : new float[] {-1f, Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY}) {
            assertThrows(IllegalArgumentException.class,
                    () -> new FlightRecord(FlightRecord.Phase.ACTIVE, true, hp));
        }
        var active = new FlightRecord(FlightRecord.Phase.ACTIVE, true, 20f);
        assertEquals(new FlightRecord(FlightRecord.Phase.PENDING, true, 20f), active.pending());
        assertEquals(FlightRecord.Phase.ACTIVE, active.phase());
        assertSame(LOCKED, LOCKED.pending());
    }
}
