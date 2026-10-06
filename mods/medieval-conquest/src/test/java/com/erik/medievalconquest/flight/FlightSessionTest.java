package com.erik.medievalconquest.flight;

import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Pure controller contracts. FakePort is not native death, Totem, or save-load evidence. */
class FlightSessionTest {
    static final class FakePort implements FlightSession.OutcomePort {
        float health;
        float maxHp = 20f;
        float absorption = 6f;
        boolean removed;
        int setCalls;
        int deathCalls;
        int warnings;
        String lastWarning;
        Runnable onSet = () -> {};

        FakePort(float health) { this.health = health; }
        public UUID id() { return new UUID(0, 99); }
        public float hp() { return health; }
        public boolean dead() { return health <= 0f; }
        public boolean removed() { return removed; }
        public void setHp(float value) {
            setCalls++;
            health = Math.max(0f, Math.min(value, maxHp));
            onSet.run();
        }
        public void dieOnce() { deathCalls++; }
        public void incompatibility(String reason) { warnings++; lastWarning = reason; }
    }

    private static FlightSession started(float hp) {
        var s = new FlightSession();
        s.prepareStart(false, true, true, hp);
        s.finishStart(true);
        assertEquals(new FlightRecord(FlightRecord.Phase.ACTIVE, true, hp), s.record(),
                "successful custom start must capture the real controller state");
        return s;
    }

    private static FlightSession pending(float hp, boolean custom) {
        var s = new FlightSession();
        s.load(new FlightRecord(FlightRecord.Phase.PENDING, custom, hp));
        assertEquals(new FlightRecord(FlightRecord.Phase.PENDING, custom, hp), s.record());
        return s;
    }

    @Test void healthyStartStopAppliesOneHalfHeartTransition() {
        var s = started(20f);
        var p = new FakePort(20f);
        s.ended(true, false);
        assertEquals(new FlightRecord(FlightRecord.Phase.PENDING, true, 20f), s.record());
        s.resolve(p);
        s.ended(false, false);
        s.tick(false);
        s.resolve(p);
        assertEquals(1f, p.hp());
        assertEquals(1, p.setCalls);
        assertEquals(0, p.deathCalls);
        assertEquals(6f, p.absorption);
        assertNull(s.record());
    }

    @Test void lowStartHealingConsumesOneDeathTransition() {
        var s = started(1f);
        var p = new FakePort(20f);
        s.ended(true, false);
        s.resolve(p);
        s.ended(false, false);
        s.tick(false);
        s.resolve(p);
        assertEquals(0f, p.hp());
        assertEquals(1, p.setCalls);
        assertEquals(1, p.deathCalls, "numerical HP0 alone does not replace actual die side effects");
        assertNull(s.record());
        assertTrue(s.blocksCustomStart(false));
    }

    @Test void directPendingDebtIsResolvedByProductionControllerNotTheFake() {
        var s = pending(20f, true);
        var p = new FakePort(20f);
        s.resolve(p);
        assertEquals(1f, p.hp(), "inert resolve must fail a behavioral assertion, not an import or NPE");
        assertEquals(1, p.setCalls);
        assertNull(s.record());
    }

    @Test void failedActualStartConsumesCandidateWithoutCreatingDebt() {
        var s = new FlightSession();
        s.prepareStart(false, true, true, 20f);
        s.finishStart(false);
        s.finishStart(true);
        assertNull(s.record(), "a stale candidate cannot arm a later unrelated flight");
        assertFalse(s.blocksCustomStart(false));
    }

    @Test void ordinaryStartCannotArmWhenCustomEquipmentAppearsMidflight() {
        var s = new FlightSession();
        s.prepareStart(false, false, true, 20f);
        s.finishStart(true);
        s.prepareStart(true, true, true, 1f);
        s.finishStart(true);
        s.ended(true, false);
        var p = new FakePort(20f);
        s.resolve(p);
        assertNull(s.record());
        assertEquals(0, p.setCalls);
    }

    @Test void deadOrNonLivingStartNeverCaptures() {
        var s = new FlightSession();
        s.prepareStart(false, true, true, 0f);
        s.finishStart(true);
        assertNull(s.record());
        s.prepareStart(false, true, false, 20f);
        s.finishStart(true);
        assertNull(s.record());
    }

    @Test void activeContinuationIsAllowedButCannotOverwriteStartCapture() {
        var s = started(20f);
        assertFalse(s.blocksCustomStart(true));
        assertTrue(s.blocksCustomStart(false));
        s.prepareStart(true, true, true, 1f);
        s.finishStart(true);
        assertEquals(new FlightRecord(FlightRecord.Phase.ACTIVE, true, 20f), s.record());
    }

    @Test void gearOrEligibilityChangeCannotEraseCapturedCurse() {
        var s = started(20f);
        s.prepareStart(true, false, true, 1f);
        s.finishStart(true);
        s.ended(true, false);
        var p = new FakePort(20f);
        s.resolve(p);
        assertEquals(1f, p.hp());
        assertEquals(1, p.setCalls);
    }

    @Test void pendingAndLockedDenyNewCustomStartWithoutReplacement() {
        for (var debt : new FlightRecord[] {
                new FlightRecord(FlightRecord.Phase.PENDING, true, 20f),
                new FlightRecord(FlightRecord.Phase.LOCKED, false, 0f)}) {
            var s = new FlightSession();
            s.load(debt);
            assertTrue(s.blocksCustomStart(false));
            s.prepareStart(false, true, true, 20f);
            s.finishStart(true);
            assertEquals(debt, s.record());
        }
    }

    @Test void lockedPersistsThroughLeaveResumeAndReload() {
        var locked = new FlightRecord(FlightRecord.Phase.LOCKED, false, 0f);
        var s = new FlightSession();
        s.load(locked);
        s.leave();
        s.resume();
        s.tick(false);
        var p = new FakePort(20f);
        s.resolve(p);
        assertEquals(locked, s.record());
        assertEquals(0, p.setCalls);
        assertTrue(s.blocksCustomStart(false));
        var reloaded = new FlightSession();
        reloaded.load(s.record());
        assertTrue(reloaded.blocksCustomStart(false));
    }

    @Test void leaveConvertsActiveBeforeSavingAndLoadedDebtResumesOnce() {
        var s = started(20f);
        s.leave();
        assertEquals(new FlightRecord(FlightRecord.Phase.PENDING, true, 20f), s.record());
        var restored = new FlightSession();
        restored.load(s.record());
        restored.resume();
        restored.resume();
        var p = new FakePort(20f);
        restored.resolve(p);
        restored.tick(false);
        restored.resolve(p);
        assertEquals(1, p.setCalls);
        assertNull(restored.record());
    }

    @Test void loadedActiveIsConvertedOnFirstLiveTickBeforeNewCustomArming() {
        var s = new FlightSession();
        s.load(new FlightRecord(FlightRecord.Phase.ACTIVE, true, 20f));
        s.tick(true);
        assertEquals(new FlightRecord(FlightRecord.Phase.PENDING, true, 20f), s.record());
        assertTrue(s.blocksCustomStart(true));
        s.prepareStart(false, true, true, 1f);
        s.finishStart(true);
        assertEquals(new FlightRecord(FlightRecord.Phase.PENDING, true, 20f), s.record());
    }

    @Test void aliveCopyPreservesImmutableDebtWhileDeathCopyStartsCleanLife() {
        var old = pending(1f, true);
        var alive = new FlightSession();
        alive.copyFrom(old, true);
        assertEquals(old.record(), alive.record());
        var fresh = new FlightSession();
        fresh.copyFrom(old, false);
        assertNull(fresh.record());
        assertFalse(fresh.blocksCustomStart(false));
        assertEquals(new FlightRecord(FlightRecord.Phase.PENDING, true, 1f), old.record());
    }

    @Test void genuineDeathClearsOldLifeAndOnlyDeathCopyAllowsNewStart() {
        var old = pending(1f, true);
        old.genuineDeath();
        assertNull(old.record());
        assertTrue(old.blocksCustomStart(false));
        old.prepareStart(false, true, true, 20f);
        old.finishStart(true);
        assertNull(old.record());
        var fresh = new FlightSession();
        fresh.copyFrom(old, false);
        fresh.prepareStart(false, true, true, 20f);
        fresh.finishStart(true);
        assertEquals(new FlightRecord(FlightRecord.Phase.ACTIVE, true, 20f), fresh.record());
    }

    @Test void alreadyDeadConsumesDebtWithoutSetterOrSecondDeath() {
        var s = pending(1f, true);
        var p = new FakePort(0f);
        s.resolve(p);
        assertNull(s.record());
        assertEquals(0, p.setCalls);
        assertEquals(0, p.deathCalls);
        assertTrue(s.blocksCustomStart(false));
    }

    @Test void removedAlivePlayerRetainsDebtForLaterResume() {
        var s = pending(20f, true);
        var p = new FakePort(20f);
        p.removed = true;
        s.resolve(p);
        assertEquals(new FlightRecord(FlightRecord.Phase.PENDING, true, 20f), s.record());
        assertEquals(0, p.setCalls);
        assertTrue(s.blocksCustomStart(false));
    }

    @Test void validNonCustomPendingConsumesNoneWithoutHealthEffects() {
        var s = pending(1f, false);
        var p = new FakePort(20f);
        s.resolve(p);
        assertNull(s.record());
        assertEquals(0, p.setCalls);
        assertEquals(0, p.deathCalls);
    }

    @Test void halfHeartIncompatibilityRemainsPendingWithoutTickRetry() {
        var s = pending(20f, true);
        var p = new FakePort(20f);
        p.maxHp = 0.5f;
        s.resolve(p);
        s.tick(false);
        s.resolve(p);
        assertEquals(new FlightRecord(FlightRecord.Phase.PENDING, true, 20f), s.record());
        assertEquals(0.5f, p.hp());
        assertEquals(1, p.setCalls);
        assertTrue(s.blocksCustomStart(false));
        assertEquals(1, p.warnings);
        assertEquals("R10_HALF_HEART_NOT_ACKNOWLEDGED", p.lastWarning);
    }

    @Test void fatalSetterReentryCannotResolveOrStartTwice() {
        var s = pending(1f, true);
        var p = new FakePort(20f);
        p.onSet = () -> {
            assertTrue(s.blocksCustomStart(false));
            s.ended(true, false);
            s.resolve(p);
            s.prepareStart(false, true, true, 20f);
            s.finishStart(true);
        };
        s.resolve(p);
        s.tick(false);
        s.resolve(p);
        assertEquals(1, p.setCalls);
        assertEquals(1, p.deathCalls);
        assertNull(s.record());
    }

    @Test void genuineDeathDuringFatalSetterCannotInvokeSecondDieSequence() {
        var s = pending(1f, true);
        var p = new FakePort(20f);
        p.onSet = s::genuineDeath;
        s.resolve(p);
        s.tick(false);
        s.resolve(p);
        assertEquals(0f, p.hp());
        assertEquals(1, p.setCalls);
        assertEquals(0, p.deathCalls, "explicit genuineDeath already completed this old life");
        assertNull(s.record());
        assertTrue(s.blocksCustomStart(false));
    }

    @Test void callbackFailurePropagatesPrimaryAndBlocksPartialOutcomeWithoutRetry() {
        var s = pending(1f, true);
        var debt = s.record();
        var p = new FakePort(20f);
        var primary = new IllegalStateException("test callback");
        p.onSet = () -> { throw primary; };
        assertSame(primary, assertThrows(IllegalStateException.class, () -> s.resolve(p)));
        assertEquals(0f, p.hp(), "the numerical change happened before the callback failed");
        assertEquals(1, p.setCalls);
        assertEquals(0, p.deathCalls);
        assertEquals(debt, s.record());
        s.blockOutcomeFailure(debt);
        s.tick(false);
        s.resolve(p);
        assertEquals(debt, s.record());
        assertEquals(1, p.setCalls);
        assertTrue(s.blocksCustomStart(false));
        assertTrue(s.claimOutcomeFailureWarning());
        assertFalse(s.claimOutcomeFailureWarning());
    }

    @Test void halfHeartCallbackFailureDoesNotPretendOutcomeAcknowledged() {
        var s = pending(20f, true);
        var debt = s.record();
        var p = new FakePort(20f);
        var primary = new IllegalStateException("half-heart callback");
        p.onSet = () -> { throw primary; };
        assertSame(primary, assertThrows(IllegalStateException.class, () -> s.resolve(p)));
        assertEquals(1f, p.hp());
        assertEquals(debt, s.record());
        s.blockOutcomeFailure(debt);
        s.resolve(p);
        assertEquals(1, p.setCalls);
        assertEquals(debt, s.record());
    }

    @Test void explicitGenuineDeathThenCallbackFailureDoesNotResurrectDebt() {
        var s = pending(1f, true);
        var debt = s.record();
        var p = new FakePort(20f);
        var primary = new IllegalStateException("post-death callback");
        p.onSet = () -> { s.genuineDeath(); throw primary; };
        assertSame(primary, assertThrows(IllegalStateException.class, () -> s.resolve(p)));
        s.blockOutcomeFailure(debt);
        s.resolve(p);
        assertNull(s.record());
        assertEquals(1, p.setCalls);
        assertEquals(0, p.deathCalls);
        assertTrue(s.blocksCustomStart(false));
    }

    @Test void failureBlockRestoresPriorPendingDebtButNeverGuessesMissingCapture() {
        var s = new FlightSession();
        var activeDebt = new FlightRecord(FlightRecord.Phase.ACTIVE, true, 1f);
        s.blockOutcomeFailure(activeDebt);
        assertEquals(new FlightRecord(FlightRecord.Phase.PENDING, true, 1f), s.record());
        assertTrue(s.blocksCustomStart(false));
        var missing = new FlightSession();
        missing.blockOutcomeFailure(null);
        assertEquals(new FlightRecord(FlightRecord.Phase.LOCKED, false, 0f), missing.record());
        assertTrue(missing.blocksCustomStart(false));
    }

    @Test void normalLoadResetsOneSessionFailureWarningAndAttemptGuard() {
        var s = pending(20f, true);
        s.blockOutcomeFailure(s.record());
        assertTrue(s.claimOutcomeFailureWarning());
        assertFalse(s.claimOutcomeFailureWarning());
        s.load(new FlightRecord(FlightRecord.Phase.PENDING, true, 20f));
        assertTrue(s.claimOutcomeFailureWarning());
        var p = new FakePort(20f);
        s.resolve(p);
        assertEquals(1f, p.hp());
        assertEquals(1, p.setCalls);
        assertNull(s.record());
    }

    @Test void fatalErrorsRemainFatalRatherThanBeingContainedAsRuntimeFailures() {
        var s = pending(1f, true);
        var p = new FakePort(20f);
        var fatal = new AssertionError("fatal test callback");
        p.onSet = () -> { throw fatal; };
        assertSame(fatal, assertThrows(AssertionError.class, () -> s.resolve(p)));
    }
}
