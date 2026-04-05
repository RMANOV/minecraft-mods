package com.erik.bestiary.signal;

import com.erik.bestiary.entity.ai.Signal;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * ★ JAVA CONCEPT: Observer Pattern with Functional Interfaces ★
 *
 * SignalPropagator implements the Observer (publish/subscribe) pattern.
 * Wolves register as listeners (observers). When a signal is broadcast,
 * all listeners receive it — but only if it's still alive after propagation.
 *
 * ─── Observer Pattern Structure ──────────────────────────────────────────────
 * Subject (SignalPropagator) maintains a list of Observer callbacks.
 * When an event occurs (broadcast), all observers are notified.
 *
 * ─── Why Consumer<Signal> Instead of a Named Interface? ─────────────────────
 * In Java 21, we prefer functional interfaces from java.util.function.
 * Consumer<Signal> is exactly "something that takes a Signal and does something."
 * This is lambda-compatible — any lambda (signal -> doSomething(signal)) works.
 *
 * Comparison:
 *   Old style: interface SignalListener { void onSignal(Signal s); }
 *   Modern:    Consumer<Signal>  (from java.util.function)
 *
 * The modern approach integrates with method references:
 *   propagator.addListener(wolf::onSignal);  // no boilerplate!
 *
 * ─── Thread Safety Note ──────────────────────────────────────────────────────
 * This runs on the Minecraft server tick thread, so no synchronization needed.
 * If used across threads, wrap listeners in CopyOnWriteArrayList.
 */
public class SignalPropagator {

    /**
     * List of registered signal listeners.
     * Each entry is a Consumer<Signal> — a functional callback.
     * Using ArrayList for O(1) add and fast iteration in broadcast.
     */
    private final List<Consumer<Signal>> listeners = new ArrayList<>();

    /**
     * The world position this propagator is anchored to (usually the alpha's position).
     * Signals calculate attenuation relative to this position.
     */
    private Vec3 anchorPosition;

    public SignalPropagator(Vec3 initialAnchor) {
        this.anchorPosition = initialAnchor;
    }

    // ─── Observer Management ─────────────────────────────────────────────────

    /**
     * ★ JAVA CONCEPT: Functional Interface as Parameter ★
     *
     * Any lambda or method reference that matches (Signal) -> void works here:
     *   propagator.addListener(wolf::onSignalReceived);
     *   propagator.addListener(s -> System.out.println("Got: " + s));
     *
     * @param listener the callback to invoke when a signal is broadcast
     */
    public void addListener(Consumer<Signal> listener) {
        listeners.add(listener);
    }

    /**
     * Remove a listener (when a wolf leaves the pack or dies).
     *
     * @param listener the callback to remove
     */
    public void removeListener(Consumer<Signal> listener) {
        listeners.remove(listener);
    }

    /**
     * Clear all listeners (used when pack dissolves).
     */
    public void clearListeners() {
        listeners.clear();
    }

    /**
     * @return how many wolves are currently listening to this propagator
     */
    public int listenerCount() {
        return listeners.size();
    }

    // ─── Signal Broadcasting ─────────────────────────────────────────────────

    /**
     * ★ JAVA CONCEPT: Functional Composition with forEach ★
     *
     * Broadcasts a signal to all registered listeners.
     * The signal is first propagated through the anchor position (attenuated),
     * then only delivered if still alive.
     *
     * Using listeners.forEach() with a lambda demonstrates:
     * 1. Internal iteration (the list controls the loop, not the caller)
     * 2. Method reference compatible: listeners.forEach(l -> l.accept(signal))
     * 3. Laziness: we check isAlive() ONCE before iterating all listeners
     *
     * ─── Diminishing Urgency Design ──────────────────────────────────────────
     * Each wolf in the chain re-broadcasts a weaker version of the signal.
     * The SignalPropagator represents ONE HOP in this chain — from the origin
     * wolf to all directly connected pack members.
     *
     * @param signal the signal to broadcast (from a wolf or the system)
     */
    public void broadcast(Signal signal) {
        // Propagate signal through the anchor position
        Signal propagated = signal.propagate(anchorPosition);

        // Only deliver if still alive (urgency > 0.1, hops < 5)
        if (propagated.isAlive()) {
            // Deliver to ALL listeners — pack coordination is simultaneous
            // forEach with lambda: clean, no index tracking needed
            listeners.forEach(listener -> listener.accept(propagated));
        }
    }

    /**
     * Broadcasts to only those listeners at positions within range.
     *
     * ★ JAVA CONCEPT: Stream API for Filtering ★
     * Demonstrates filtering a collection with lambda predicates.
     *
     * In a full implementation, listeners would carry their position.
     * Simplified here: broadcasts to all, but attenuates by distance.
     *
     * @param signal the signal to broadcast
     * @param receiverPositions positions of the intended receivers
     */
    public void broadcastToPositions(Signal signal, List<Vec3> receiverPositions) {
        // For each receiver position, check if the propagated signal is alive
        // then deliver to the corresponding listener
        for (int i = 0; i < Math.min(listeners.size(), receiverPositions.size()); i++) {
            Signal propagated = signal.propagate(receiverPositions.get(i));
            if (propagated.isAlive()) {
                listeners.get(i).accept(propagated);
            }
        }
    }

    /**
     * Update the anchor position (called when the alpha wolf moves).
     */
    public void updateAnchor(Vec3 newAnchor) {
        this.anchorPosition = newAnchor;
    }

    public Vec3 getAnchorPosition() {
        return anchorPosition;
    }
}
