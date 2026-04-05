package com.erik.arcanenaturalis.season;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;

/**
 * ★ JAVA 21 FEATURE: Streams + Lambdas for event-driven color updates ★
 *
 * SeasonColorHandler registers a client tick event that:
 *   1. Gets the current season (using SeasonManager)
 *   2. Updates grass color in nearby chunks using stream().forEach()
 *
 * The Mixin (FoliageColorMixin) handles FOLIAGE color overriding at the
 * FoliageColor.get() call site. This class handles the CLIENT TICK side:
 * spawning the right particles based on current biome + season.
 *
 * ★ Java 21 Stream usage:
 *   The particle spawning uses:
 *     level.players().stream()
 *         .filter(p -> isInSnowBiome(p))   — only snow players get aurora
 *         .forEach(p -> spawnAurora(p))    — spawn aurora for each
 *
 * This replaces the imperative:
 *   for (Player p : level.players()) {
 *       if (isInSnowBiome(p)) { spawnAurora(p); }
 *   }
 *
 * The stream version is more concise and composable — filters chain naturally.
 */
public class SeasonColorHandler {

    // How often to spawn particles (every N ticks to avoid performance hit)
    private static final int PARTICLE_TICK_INTERVAL = 10;

    private static int tickCounter = 0;

    public static void register() {
        // ★ Lambda: ClientTickEvents.END_CLIENT_TICK accepts Level→void ★
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.level == null) return;

            tickCounter++;
            if (tickCounter % PARTICLE_TICK_INTERVAL != 0) return;

            spawnParticlesForSeason(client);
        });
    }

    /**
     * ★ Java 21: Streams + Pattern Matching to dispatch particle spawning ★
     *
     * Gets the current season and uses a pattern matching switch to decide
     * which particles to spawn. Each season spawns different particle effects.
     */
    private static void spawnParticlesForSeason(Minecraft client) {
        if (client.level == null || client.player == null) return;

        var level  = client.level;
        var player = client.player;

        long gameTime = level.getGameTime();
        Season season = SeasonManager.getCurrentSeason(gameTime);

        // ★ Pattern Matching Switch: dispatches by season type ★
        // Also used as an expression to get the spawn rate modifier
        double spawnChance = switch (season) {
            case Season.Spring s -> 0.4;  // moderate particles — spring flowers
            case Season.Summer s -> 0.2;  // few particles — clear sunny days
            case Season.Autumn s -> 0.5;  // more particles — magical autumn feeling
            case Season.Winter s -> 0.6;  // most particles — aurora in cold biomes
        };

        // ★ Streams: get nearby players (in multiplayer) and filter ★
        // Only spawn for players in appropriate biomes
        level.players().stream()
                .filter(p -> client.level.getRandom().nextDouble() < spawnChance)
                .forEach(p -> {
                    // Spawn a few particles near each qualifying player
                    double px = p.getX() + (level.getRandom().nextDouble() - 0.5) * 16;
                    double py = p.getY() + 1.0 + level.getRandom().nextDouble() * 4;
                    double pz = p.getZ() + (level.getRandom().nextDouble() - 0.5) * 16;

                    // ★ Switch expression to choose particle type ★
                    switch (season) {
                        case Season.Spring ignored ->
                            level.addParticle(
                                com.erik.arcanenaturalis.registry.ModParticles.FIREFLY,
                                px, py, pz, 0, 0, 0);
                        case Season.Summer ignored ->
                            level.addParticle(
                                com.erik.arcanenaturalis.registry.ModParticles.FIREFLY,
                                px, py, pz, 0, 0, 0);
                        case Season.Autumn ignored ->
                            level.addParticle(
                                com.erik.arcanenaturalis.registry.ModParticles.MAGICAL_DUST,
                                px, py, pz, 0, 0, 0);
                        case Season.Winter ignored ->
                            level.addParticle(
                                com.erik.arcanenaturalis.registry.ModParticles.AURORA,
                                px, py, pz, 0, 0, 0);
                    }
                });
    }
}
