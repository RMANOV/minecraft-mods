package com.erik.medievalconquest.policy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExpiryPolicyTest {

	@Test
	void normalDeadlineIsThirtyMinutesAfterEpochZero() {
		assertEquals(1_800_000L, ExpiryPolicy.deadlineMillis(0L, false));
	}

	@Test
	void normalDeadlineIncludesItsPlacementTimestamp() {
		assertEquals(1_700_001_800_123L,
				ExpiryPolicy.deadlineMillis(1_700_000_000_123L, false));
	}

	@Test
	void normalBarrierRemainsActiveOneMillisecondBeforeDeadline() {
		assertFalse(ExpiryPolicy.expired(1_800_000L, false, 1_799_999L, 0L));
	}

	@Test
	void normalBarrierExpiresAtItsExactDeadline() {
		assertTrue(ExpiryPolicy.expired(1_800_000L, false, 1_800_000L, 0L));
	}

	@Test
	void normalBarrierRemainsExpiredAfterItsDeadline() {
		assertTrue(ExpiryPolicy.expired(1_800_000L, false, 1_800_001L, 0L));
	}

	@Test
	void maximumCurrentTimestampExpiresANormalBarrier() {
		assertTrue(ExpiryPolicy.expired(1_800_000L, false, Long.MAX_VALUE, 0L));
	}

	@Test
	void observedExpiryIsNotUndoneByClockRollback() {
		assertTrue(ExpiryPolicy.expired(1_800_000L, false, 0L, 1_800_000L));
	}

	@Test
	void observedTimeBeforeDeadlineDoesNotExpireBarrierEarly() {
		assertFalse(ExpiryPolicy.expired(1_800_000L, false, 0L, 1_799_999L));
	}

	@Test
	void currentTimeCanAdvancePastAnOlderMaximumObservation() {
		assertTrue(ExpiryPolicy.expired(1_800_000L, false, 1_800_001L, 1_799_999L));
	}

	@Test
	void permanentBarrierNeverExpiresForValidTimestamps() {
		assertAll(
				() -> assertFalse(ExpiryPolicy.expired(0L, true, 0L, 0L)),
				() -> assertFalse(ExpiryPolicy.expired(1_800_000L, true, 1_800_000L, 0L)),
				() -> assertFalse(ExpiryPolicy.expired(
						Long.MAX_VALUE, true, Long.MAX_VALUE, Long.MAX_VALUE)));
	}

	@Test
	void permanentDeadlineUsesMaximumTimestamp() {
		assertEquals(Long.MAX_VALUE, ExpiryPolicy.deadlineMillis(0L, true));
	}

	@Test
	void permanentPlacementDoesNotPerformNormalDeadlineAddition() {
		assertEquals(Long.MAX_VALUE, ExpiryPolicy.deadlineMillis(Long.MAX_VALUE, true));
	}

	@Test
	void negativePlacementIsRejectedEvenForPermanentBarrier() {
		assertAll(
				() -> assertThrows(IllegalArgumentException.class,
						() -> ExpiryPolicy.deadlineMillis(-1L, false)),
				() -> assertThrows(IllegalArgumentException.class,
						() -> ExpiryPolicy.deadlineMillis(-1L, true)));
	}

	@Test
	void maximumNormalPlacementRejectsDeadlineOverflow() {
		assertThrows(IllegalArgumentException.class,
				() -> ExpiryPolicy.deadlineMillis(Long.MAX_VALUE, false));
	}

	@Test
	void latestRepresentableNormalDeadlineIsAccepted() {
		assertEquals(Long.MAX_VALUE,
				ExpiryPolicy.deadlineMillis(Long.MAX_VALUE - 1_800_000L, false));
	}

	@Test
	void firstUnrepresentableNormalDeadlineIsRejected() {
		assertThrows(IllegalArgumentException.class,
				() -> ExpiryPolicy.deadlineMillis(Long.MAX_VALUE - 1_799_999L, false));
	}

	@Test
	void negativeDeadlineIsRejectedBeforePermanentShortcut() {
		assertAll(
				() -> assertThrows(IllegalArgumentException.class,
						() -> ExpiryPolicy.expired(-1L, false, 0L, 0L)),
				() -> assertThrows(IllegalArgumentException.class,
						() -> ExpiryPolicy.expired(-1L, true, 0L, 0L)));
	}

	@Test
	void negativeCurrentTimeIsRejectedBeforePermanentShortcut() {
		assertAll(
				() -> assertThrows(IllegalArgumentException.class,
						() -> ExpiryPolicy.expired(0L, false, -1L, 0L)),
				() -> assertThrows(IllegalArgumentException.class,
						() -> ExpiryPolicy.expired(Long.MAX_VALUE, true, -1L, 0L)));
	}

	@Test
	void negativeMaximumObservationIsRejectedBeforePermanentShortcut() {
		assertAll(
				() -> assertThrows(IllegalArgumentException.class,
						() -> ExpiryPolicy.expired(0L, false, 0L, -1L)),
				() -> assertThrows(IllegalArgumentException.class,
						() -> ExpiryPolicy.expired(Long.MAX_VALUE, true, 0L, -1L)));
	}

	@Test
	void aValidNormalDeadlineAtZeroIsAlreadyExpired() {
		assertTrue(ExpiryPolicy.expired(0L, false, 0L, 0L));
	}

	@Test
	void aStoredDeadlineDoesNotRestartWhenReadBackLater() {
		long storedDeadline = ExpiryPolicy.deadlineMillis(1_000L, false);
		long reloadedDeadline = Long.parseLong(Long.toString(storedDeadline));

		assertEquals(1_801_000L, reloadedDeadline);
		assertFalse(ExpiryPolicy.expired(reloadedDeadline, false, 1_800_999L, 0L));
		assertTrue(ExpiryPolicy.expired(reloadedDeadline, false, 1_900_000L, 0L));
	}

	@Test
	void permanentQueriesDoNotLeakIntoLaterNormalQueries() {
		assertFalse(ExpiryPolicy.expired(1_800_000L, true, 1_800_000L, 0L));
		assertTrue(ExpiryPolicy.expired(1_800_000L, false, 1_800_000L, 0L));
		assertEquals(1_800_000L, ExpiryPolicy.deadlineMillis(0L, false));
	}

	@Test
	void latestRepresentableDeadlineExpiresAtMaximumObservation() {
		assertAll(
				() -> assertFalse(ExpiryPolicy.expired(
						Long.MAX_VALUE, false, Long.MAX_VALUE - 1L, 0L)),
				() -> assertTrue(ExpiryPolicy.expired(
						Long.MAX_VALUE, false, 0L, Long.MAX_VALUE)));
	}
}
