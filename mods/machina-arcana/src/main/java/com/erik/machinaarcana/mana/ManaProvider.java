package com.erik.machinaarcana.mana;

/**
 * ★ JAVA 21 FEATURE: Interface Segregation (part 2 of 2) ★
 *
 * The complement to {@link ManaReceiver}. Where ManaReceiver is for blocks that
 * consume mana, ManaProvider is for blocks that PRODUCE or STORE it.
 *
 * A Mana Conduit implements BOTH — it acts as a relay:
 *   1. It receives mana from adjacent providers (ManaReceiver side)
 *   2. It exposes stored mana to the network (ManaProvider side)
 *
 * This two-interface split is classic ISP: each interface has one clear purpose.
 */
public interface ManaProvider {

    /**
     * Attempt to extract {@code amount} mana of {@code type} from this provider.
     *
     * <p>Contract:
     * <ul>
     *   <li>Returns the amount actually extracted (may be less if running low).</li>
     *   <li>Returning 0 means nothing was available or the type doesn't match.</li>
     *   <li>Must not return a negative number.</li>
     * </ul>
     *
     * @param type   the type of mana requested
     * @param amount the desired extraction amount
     * @return actual amount extracted
     */
    int extractMana(ManaType type, int amount);

    /**
     * Query how much mana of the given type is currently stored.
     * Does NOT modify state — safe to call repeatedly.
     *
     * @param type the mana type to query
     * @return amount stored (0 if none or wrong type)
     */
    int getStoredMana(ManaType type);

    /**
     * Whether this provider currently has any mana of the given type available.
     * Fast check — should be O(1).
     *
     * @param type mana type to check
     * @return true if at least 1 unit of this type is available
     */
    boolean canProvide(ManaType type);
}
