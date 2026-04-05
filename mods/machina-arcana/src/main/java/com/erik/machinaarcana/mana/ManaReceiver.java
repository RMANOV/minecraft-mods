package com.erik.machinaarcana.mana;

/**
 * ★ JAVA 21 FEATURE: Interface Segregation Principle ★
 *
 * The Interface Segregation Principle (ISP) says: "Clients should not be forced
 * to depend on interfaces they do not use."
 *
 * Instead of one huge "ManaCapable" interface with 10 methods, we split into:
 *   - ManaReceiver  — for things that CONSUME mana
 *   - ManaProvider  — for things that PRODUCE/STORE mana
 *
 * A block entity can implement BOTH if it both stores and distributes mana
 * (like a Mana Conduit), or just one if it only receives (like the Assembler).
 *
 * This interface is used as a type bound: {@code <T extends ManaReceiver>}
 * means T must implement this interface, giving compile-time guarantees.
 */
public interface ManaReceiver {

    /**
     * Attempt to push {@code amount} mana of {@code type} into this receiver.
     *
     * <p>Contract:
     * <ul>
     *   <li>Returns the amount actually received (may be less if nearly full).</li>
     *   <li>Returning 0 means the push was rejected entirely.</li>
     *   <li>Must not return a negative number.</li>
     * </ul>
     *
     * @param type   the type of mana being pushed
     * @param amount the amount to push (must be positive)
     * @return actual amount received
     */
    int receiveMana(ManaType type, int amount);

    /**
     * The maximum amount of mana this receiver can accept per tick from the network.
     * Acts as a "bandwidth" limiter to prevent one receiver from draining the whole network.
     *
     * @return max mana per tick
     */
    int getMaxReceive();

    /**
     * Whether this receiver can currently accept mana of the given type.
     * Fast check before calling the more expensive {@link #receiveMana}.
     *
     * @param type mana type to check
     * @return true if this receiver will accept this mana type
     */
    boolean canReceive(ManaType type);
}
