package com.erik.medievalconquest.policy;

/** Pure wall-clock expiry policy for temporary and permanent barriers. */
public final class ExpiryPolicy {
	private static final long LIFETIME_MILLIS = 1_800_000L;

	private ExpiryPolicy() {
	}

	public static long deadlineMillis(long placedAtMillis, boolean permanent) {
		if (placedAtMillis < 0L) {
			throw new IllegalArgumentException("Placement timestamp must be non-negative");
		}
		if (permanent) {
			return Long.MAX_VALUE;
		}
		try {
			return Math.addExact(placedAtMillis, LIFETIME_MILLIS);
		} catch (ArithmeticException overflow) {
			throw new IllegalArgumentException("Deadline exceeds the timestamp range", overflow);
		}
	}

	public static boolean expired(long deadlineMillis, boolean permanent,
			long nowMillis, long maxSeenMillis) {
		if (deadlineMillis < 0L || nowMillis < 0L || maxSeenMillis < 0L) {
			throw new IllegalArgumentException("Timestamps must be non-negative");
		}
		return !permanent && Math.max(nowMillis, maxSeenMillis) >= deadlineMillis;
	}
}
