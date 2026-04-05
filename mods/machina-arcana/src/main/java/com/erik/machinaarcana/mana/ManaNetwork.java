package com.erik.machinaarcana.mana;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/**
 * ★ JAVA 21 FEATURE: Generics with Multiple Type Bounds (Intersection Types) ★
 *
 * <pre>
 *   class ManaNetwork<T extends BlockEntity & ManaReceiver>
 * </pre>
 *
 * This is called an <b>intersection type bound</b>: {@code T} must satisfy BOTH
 * constraints simultaneously:
 *   1. {@code extends BlockEntity}  — T IS a BlockEntity (single class bound)
 *   2. {@code & ManaReceiver}       — T ALSO implements ManaReceiver (interface bound)
 *
 * The {@code &} operator chains bounds. You can chain multiple interfaces:
 *   {@code T extends BlockEntity & ManaReceiver & Tickable}
 *
 * Why is this useful here?
 *   - We can call {@code t.getBlockPos()} because T is a BlockEntity
 *   - We can call {@code t.receiveMana()} because T is a ManaReceiver
 *   - No casting needed — the compiler guarantees both APIs are available!
 *
 * Without generics, we'd need an ugly cast: {@code ((ManaReceiver) blockEntity).receiveMana(...)}
 * which could fail at runtime if the cast is wrong. Generics move this check to compile time.
 *
 * ★ ALSO: Functional pipelines with streams ★
 *
 * The {@link #distributeToReceivers} method uses Stream.forEach() and functional lambdas
 * to distribute mana without explicit iteration loops — more readable and composable.
 */
public class ManaNetwork<T extends BlockEntity & ManaReceiver> {

    // ── Constants ──────────────────────────────────────────────────────────

    /** Maximum BFS radius for network discovery (blocks). */
    private static final int MAX_NETWORK_RADIUS = 16;

    /** Amount of mana to push per receiver per tick. */
    private static final int DISTRIBUTION_AMOUNT = 10;

    // ── Fields ─────────────────────────────────────────────────────────────

    /** All receivers currently connected in this network. */
    private final List<T> receivers = new ArrayList<>();

    /** The level (world) this network lives in. */
    private final Level level;

    /** Center position — usually the ManaConduit that triggered the rebuild. */
    private final BlockPos origin;

    // ── Constructor ────────────────────────────────────────────────────────

    /**
     * Constructs a new network. Does NOT automatically discover receivers —
     * call {@link #rebuild(Class)} to populate.
     *
     * @param level  the world
     * @param origin starting position for BFS discovery
     */
    public ManaNetwork(Level level, BlockPos origin) {
        this.level = level;
        this.origin = origin;
    }

    // ── Network Discovery ──────────────────────────────────────────────────

    /**
     * Rebuilds the network by BFS-walking from origin, collecting all
     * adjacent block entities that are instances of {@code receiverClass}.
     *
     * ★ Generic method parameter: {@code receiverClass} is the runtime
     * Class<T> token needed because Java erases generics at runtime.
     * We can't write {@code instanceof T} (T is erased), so we pass the
     * class token and use {@code receiverClass.isInstance(be)}.
     *
     * @param receiverClass the Class object for type T (runtime type token)
     */
    @SuppressWarnings("unchecked")
    public void rebuild(Class<T> receiverClass) {
        receivers.clear();

        // BFS state
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new ArrayDeque<>();
        queue.add(origin);
        visited.add(origin);

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();

            // Check all 6 cardinal directions
            for (Direction dir : Direction.values()) {
                BlockPos neighbor = current.relative(dir);

                // Skip already-visited positions
                if (visited.contains(neighbor)) continue;
                visited.add(neighbor);

                // Don't wander too far
                if (neighbor.distSqr(origin) > MAX_NETWORK_RADIUS * MAX_NETWORK_RADIUS) continue;

                BlockEntity be = level.getBlockEntity(neighbor);
                if (be == null) continue;

                // ★ Using the class token to check type — safe at runtime ★
                if (receiverClass.isInstance(be)) {
                    // Safe cast: we just verified instanceof
                    receivers.add((T) be);
                    // Continue BFS through this node (conduits chain)
                    queue.add(neighbor);
                }
            }
        }
    }

    // ── Mana Distribution ─────────────────────────────────────────────────

    /**
     * Distributes mana of the given type to all connected receivers.
     *
     * ★ FUNCTIONAL PIPELINE with streams ★
     *
     * Instead of a verbose for-loop with if-statements, we build a pipeline:
     *   1. {@code stream()} — wrap list in a stream
     *   2. {@code filter()} — keep only receivers that canReceive this type
     *   3. {@code forEach()} — push mana to each remaining receiver
     *
     * This is declarative: we describe WHAT to do, not HOW to iterate.
     * The stream handles the iteration internally, making it parallelizable
     * later if needed ({@code parallelStream()}).
     *
     * @param provider the source providing mana
     * @param type     the mana type to distribute
     */
    public void distributeToReceivers(ManaProvider provider, ManaType type) {
        // ★ Stream pipeline: filter → action ★
        receivers.stream()
                // Only push to receivers that want this mana type
                .filter(receiver -> receiver.canReceive(type))
                // For each eligible receiver, extract from provider and push
                .forEach(receiver -> {
                    // How much can this receiver take?
                    int toSend = Math.min(DISTRIBUTION_AMOUNT, receiver.getMaxReceive());
                    // Try to extract that much from the provider
                    int extracted = provider.extractMana(type, toSend);
                    if (extracted > 0) {
                        // Push to receiver — uses T's ManaReceiver methods
                        // Note: because T extends BlockEntity & ManaReceiver,
                        // both receiver.getBlockPos() and receiver.receiveMana() compile!
                        receiver.receiveMana(type, extracted);
                    }
                });
    }

    // ── Accessors ──────────────────────────────────────────────────────────

    /**
     * @return immutable view of current receivers
     */
    public List<T> getReceivers() {
        return List.copyOf(receivers);
    }

    /**
     * @return number of connected receivers
     */
    public int size() {
        return receivers.size();
    }

    /**
     * @return true if no receivers are connected
     */
    public boolean isEmpty() {
        return receivers.isEmpty();
    }
}
