# Minecraft Mods & Toolkit

A versioned collection of custom Fabric mods, datapacks, commands, and modding code patterns.
Built for **Minecraft 1.21.x** on Fabric — targeting players who read bytecode for fun.

> *"I just wanted to make an Infiniminer clone."*
> — Markus "Notch" Persson, 2009. The understatement that launched 300 million sales.

---

## The Archaeology of Minecraft's Codebase

Most modding guides show you *what* to do. This section explains *why the APIs are shaped the way they are* — because every design decision in modern MC is a scar from a past mistake.

### Why Mixins Exist

Minecraft ships as an obfuscated JAR. Every release, Mojang runs ProGuard, which renames `net.minecraft.world.entity.player.Player` to something like `class_bxz`. This means you can't just subclass vanilla code — the names change every snapshot.

**Forge's solution (2011-2018):** Ship massive patch files that rewrite vanilla classes at load time. This worked until it didn't. Every MC update required Forge maintainers to manually re-create thousands of patches. The 1.13 update (the Flattening) took Forge *six months* to port because Mojang renamed every block ID.

**Fabric's solution (2018):** Use Mumfrey's Mixin framework. Instead of replacing classes, Mixins inject bytecode into specific methods at runtime using ASM transformations. A `@Inject(at = @At("HEAD"))` becomes a `GOTO` instruction inserted before the method's first opcode. The original method stays intact — you're just adding instructions around it.

**The key insight:** Mixins work on *intermediary mappings* — a stable set of names that Fabric maintains across versions. When Mojang renames `class_bxz.method_4521()` to `class_cyq.method_4890()`, the intermediary name `net/minecraft/class_1234/method_5678` stays the same. Your mixin targets the intermediary name and Fabric Loom remaps it at build time.

This is why Fabric mods update in hours, not months.

### How Structure Generation Actually Works

When you add a structure like our castles to Minecraft, three separate systems cooperate:

**1. Placement (structure_set JSON):** During world creation, MC divides the world into a grid of *regions* sized `spacing × spacing` chunks. Within each region, it picks one random chunk (with `separation` as minimum distance). This is `RandomSpreadStructurePlacement` — a salted hash of `(regionX, regionZ, seed, salt)` determines the exact chunk. The salt we use (`987654321`) guarantees our castles don't collide with villages (salt `10387312`).

**2. Generation (Structure.findGenerationPoint):** When a chunk in the grid starts generating, MC calls your Structure's `findGenerationPoint()`. This is where you check the heightmap, biome, and decide if the structure should actually spawn. We use `onTopOfChunkCenter()` which queries `Heightmap.WORLD_SURFACE_WG` — the *world-gen* heightmap that includes terrain but not trees or grass.

**3. Piece placement (StructurePiece.postProcess):** Structures span multiple chunks. MC doesn't build the whole structure at once — it calls `postProcess()` for *each chunk that overlaps the structure's bounding box*. The `placeBlock(level, state, localX, localY, localZ, chunkBounds)` method automatically clips to the current chunk's boundaries. This is why torches at chunk borders sometimes pop off — their support block might be in a neighboring chunk that hasn't generated yet.

**Fun fact:** Villages use a completely different system — *Jigsaw structures* — where pieces snap together like puzzle pieces along connection points. This was inspired by the Wave Function Collapse algorithm from procedural generation research. Our castles use the simpler *monolithic piece* approach (one StructurePiece builds everything) because castles have a fixed layout.

### The Flying Mob Problem

Making a mob fly in Minecraft is deceptively hard because the movement system was designed around walking:

**PathNavigation** assumes ground movement. It calculates A* paths on a 2D heightmap projection. `FlyingPathNavigation` extends this to 3D, but the pathfinding grid resolution is 1 block — meaning a flying mob navigating between trees computes *every possible 1-block step* in 3D space. This is why Ghasts have almost no pathfinding (they use random vectors instead).

**MoveControl** is the bridge between AI goals ("go to position X,Y,Z") and the physics system ("apply velocity V"). `FlyingMoveControl` differs from `MoveControl` in one critical way: it applies velocity *vertically* as well as horizontally. Regular MoveControl only sets `xxa` and `zza` (horizontal strafe/forward) — `FlyingMoveControl` also manipulates `yya`.

**Gravity:** Simply calling `setNoGravity(true)` isn't enough. The `travel()` method in `LivingEntity` applies drag, friction, and movement differently based on whether the entity is on ground, in water, or in air. The `travelFlying(Vec3, float)` helper method applies the correct air-friction formula: `velocity = (velocity + acceleration) * friction` where friction is typically 0.91. Without this, your flying mob either drifts forever or stops dead.

**Our approach** in Medieval Conquest: keep `Monster` as the base class (so we inherit hostile mob spawning rules and darkness checks), but override navigation to `FlyingPathNavigation`, set `FlyingMoveControl`, disable gravity, and use a custom 3-phase attack AI: **Circle** (parametric orbit at radius 12, altitude +15) → **Dive** (straight-line intercept at 1.5x speed) → **Climb** (vertical escape at +20 blocks). The transition timings (60/40/30 ticks) were tuned to feel menacing without being unfair.

### Entity Models: The UV Puzzle

Minecraft entity models are defined entirely in code — there's no 3D model file. Each body part is a `CubeListBuilder` with dimensions in pixels and a `texOffs(u, v)` pointing to its UV origin on the texture sheet.

The UV unwrap follows a specific pattern for each box:
```
         +---+
         | T |        T = Top face
     +---+---+---+
     | L | F | R |    F = Front, L = Left, R = Right
     +---+---+---+
         | B |        B = Bottom, Bk = Back
         +---+
         | Bk|
         +---+
```

For a box of dimensions `W×H×D`, the UV rectangle requires `(2W + 2D) × (H + D)` pixels. Our dragon uses a 256x128 sheet with 16 separate parts — each needs careful offset calculation to avoid overlap. The `.mirror()` method flips the UV horizontally, so left/right pairs share the same texture space.

**Animation** works by modifying `xRot`, `yRot`, `zRot` on each `ModelPart` every frame. Since parts are hierarchical (wing_outer is a child of wing_inner), rotations compound — rotating wing_inner by 30 degrees also rotates wing_outer by 30 degrees, then wing_outer's own rotation adds on top. This is how you get the multi-joint wing flap: inner wing flaps with `cos(age * 0.2)`, outer wing flaps with `cos(age * 0.2 - 0.5)` for a phase delay that simulates membrane flex.

---

## What's in This Repo

### Mods

| Mod | Version | Description |
|-----|---------|-------------|
| [Medieval Conquest v1](mods/medieval-conquest-v1/) | 0.1.0 | Ground-walking dragon, `/castle` command, territory claims, tree mechanics overhaul. The original release. |
| [Medieval Conquest v2](mods/medieval-conquest/) | 0.2.0 | **Flying dragons** with swoop-attack AI. **Castles spawn naturally** (like villages). **Brazier is now a small axe.** Doubled dragon spawn rate. |

```bash
cd mods/medieval-conquest
./gradlew build        # Build the JAR
./gradlew runClient    # Launch dev client
```

In-game verification:
```
/locate structure medievalconquest:castle
/summon medievalconquest:overworld_dragon ~ ~10 ~
/castle
```

### [Commands](commands/)

Battle-tested command-block recipes for 1.21.x:
- Custom boss bars linked to mob health via `execute store`
- Scoreboard-based progression systems (ranks, timers, kill counters)
- Proximity triggers with cooldown logic
- Structure saving/cloning with structure blocks
- Particle effect patterns (spirals, area indicators)

### [Datapacks](datapacks/)

Vanilla-compatible enhancements. No mods needed — drop in, `/reload`, done.

### [Snippets](snippets/)

Copy-paste-ready Fabric patterns with full context:
- Entity registration (hostile, passive, flying)
- Programmatic structures (Structure → Piece → data-driven JSON)
- Event hooks (block break, item use, player join)
- Mixin injection (`@Inject`, `@Redirect`, `@ModifyReturnValue`)
- World gen (biome spawning, ore placement, structure sets)

---

## Deep Lore: Things Most Players Don't Know

**Minecraft's tick rate (20 TPS) was chosen because of Java's `Thread.sleep()` granularity.** Notch needed a game loop that wouldn't drift on Windows, where sleep precision is ~15ms. At 20 TPS, each tick is 50ms — safely above the sleep threshold. This is why MC doesn't run at 30 or 60 TPS like most games: it was a JVM limitation from 2009 that became permanent.

**The world height limit (384 blocks, from -64 to 320) isn't arbitrary.** Each chunk section is 16x16x16 blocks stored as a palette-compressed array. The old limit (256) was 16 sections. The 1.18 expansion to 384 blocks added 8 more sections, which required changing the section index from a nibble (4 bits, max 16) to a byte. This single data type change cascaded through *hundreds* of methods in the codebase.

**Mojang's official mappings** (released in 2019) were a legal earthquake. For a decade, the modding community reverse-engineered class names through MCP (Mod Coder Pack) and later Yarn. When Mojang released their internal names, it revealed that some community-chosen names were wrong for years — what MCP called `EntityLivingBase` was internally `LivingEntity`; what Yarn called `Identifier` was `ResourceLocation`. This naming schism still causes confusion when reading mod tutorials written for different mapping sets.

**The Nether's ceiling at y=128 exists because the Nether was originally just the overworld with a different palette.** Notch reused the same chunk format but halved the height to save memory (2010 laptops had 2GB RAM). When the overworld grew to 256 (1.2) and then 384 (1.18), the Nether stayed at 128 because too many farms and builds relied on the bedrock ceiling.

**Chunk loading is the single most expensive operation in Minecraft.** Loading one chunk requires: deserializing NBT, decompressing palette data, reconstructing heightmaps, running light engine propagation, and triggering all pending block updates. On a server, chunk loading runs on the main thread (Mojang has tried — and failed — to move it off-thread three times). This is why elytra flying causes lag: you're forcing the server to load 20+ chunks per second.

**The seed `0` produces a world with a stronghold directly under spawn.** This was discovered in 2011 and never patched — it's a quirk of the modular arithmetic in `RandomSource` when initialized with zero.

---

## Project Structure

```
minecraft-mods/
├── mods/
│   ├── medieval-conquest-v1/   # v1: ground dragon, /castle command
│   └── medieval-conquest/      # v2: flying dragons, worldgen castles, axe brazier
├── commands/                   # Command-block recipes & advanced execute chains
├── datapacks/                  # Vanilla-compatible datapacks
└── snippets/                   # Reusable Fabric modding patterns (1.21.x)
```

## Requirements

| Component | Version |
|-----------|---------|
| Java | 21+ |
| Minecraft | 1.21.x |
| Fabric Loader | 0.18.2+ |
| Fabric API | 0.139.4+ |

Gradle wrapper is included in each mod — no global Gradle install needed.

## Contributing

Found a bug? Have a useful command? Want to add a mod? Open an issue or PR.
Each mod directory has its own build instructions and testing steps.

## License

MIT — use freely, modify freely, credit appreciated.

---

*Built with Mixins, math, and more test worlds than we'd like to admit.*
