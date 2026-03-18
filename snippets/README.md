# Fabric Modding Snippets

Reusable patterns for **Fabric 1.21.x** mod development.
Each snippet is a self-contained example with imports and context.

---

## Entity Registration

### Custom hostile mob with attributes
```java
// In your registry class
public static final EntityType<MyMob> MY_MOB =
    EntityType.Builder.of(MyMob::new, MobCategory.MONSTER)
        .sized(1.0f, 2.0f)          // hitbox: width x height
        .clientTrackingRange(10)     // render distance (chunks)
        .build(ResourceKey.create(Registries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(MOD_ID, "my_mob")));

// Register in your ModEntities.register():
Registry.register(BuiltInRegistries.ENTITY_TYPE,
    Identifier.fromNamespaceAndPath(MOD_ID, "my_mob"), MY_MOB);
FabricDefaultAttributeRegistry.register(MY_MOB, MyMob.createAttributes());

// In your entity class:
public static AttributeSupplier.Builder createAttributes() {
    return Monster.createMonsterAttributes()
        .add(Attributes.MAX_HEALTH, 40.0)
        .add(Attributes.ATTACK_DAMAGE, 6.0)
        .add(Attributes.MOVEMENT_SPEED, 0.3)
        .add(Attributes.FOLLOW_RANGE, 32.0)
        .add(Attributes.ARMOR, 4.0);
}
```

### Flying mob (keep Monster base, add flight)
```java
// Constructor — swap move control and navigation, disable gravity
public MyFlyingMob(EntityType<? extends Monster> type, Level level) {
    super(type, level);
    this.moveControl = new FlyingMoveControl(this, 20, true);
    this.setNoGravity(true);
}

@Override
protected PathNavigation createNavigation(Level level) {
    FlyingPathNavigation nav = new FlyingPathNavigation(this, level);
    nav.setCanOpenDoors(false);
    nav.setCanFloat(true);
    return nav;
}

@Override
public void travel(Vec3 travelVector) {
    if (this.isInWater()) {
        this.moveRelative(0.02f, travelVector);
        this.move(MoverType.SELF, this.getDeltaMovement());
        this.setDeltaMovement(this.getDeltaMovement().scale(0.8));
    } else {
        this.travelFlying(travelVector, 0.91f);
    }
    this.calculateEntityAnimation(false);
}
```

### Client-side renderer + model registration
```java
// In ClientModInitializer.onInitializeClient():
EntityRendererRegistry.register(ModEntities.MY_MOB, MyMobRenderer::new);
EntityModelLayerRegistry.registerModelLayer(MyMobModel.LAYER_LOCATION,
    MyMobModel::createBodyLayer);
```

---

## Custom Structures (Programmatic)

### Structure type + piece registration
```java
// StructureType — tells MC how to decode your structure from JSON
public static final StructureType<MyStructure> MY_TYPE = () -> MyStructure.CODEC;

// PieceType — tells MC how to load your piece from NBT (world saves)
public static final StructurePieceType MY_PIECE = MyPiece::new;

// Register both:
Registry.register(BuiltInRegistries.STRUCTURE_TYPE, id("my_structure"), MY_TYPE);
Registry.register(BuiltInRegistries.STRUCTURE_PIECE, id("my_piece"), MY_PIECE);
```

### Structure class
```java
public class MyStructure extends Structure {
    public static final MapCodec<MyStructure> CODEC = simpleCodec(MyStructure::new);

    public MyStructure(StructureSettings settings) { super(settings); }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext ctx) {
        return onTopOfChunkCenter(ctx, Heightmap.Types.WORLD_SURFACE_WG, builder -> {
            int x = ctx.chunkPos().getMiddleBlockX();
            int z = ctx.chunkPos().getMiddleBlockZ();
            int y = ctx.chunkGenerator().getFirstOccupiedHeight(
                x, z, Heightmap.Types.WORLD_SURFACE_WG,
                ctx.heightAccessor(), ctx.randomState());
            builder.addPiece(new MyPiece(x, y, z));
        });
    }

    @Override
    public StructureType<?> type() { return ModStructures.MY_TYPE; }
}
```

### Data-driven placement (JSON)
```
data/<modid>/worldgen/structure/<name>.json          — what & where (biomes)
data/<modid>/worldgen/structure_set/<name>.json       — spacing & separation
data/<modid>/tags/worldgen/biome/has_structure/<name>.json — biome list
```

---

## Event Handling

### Block break interception
```java
PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, entity) -> {
    if (state.is(BlockTags.LOGS) && !player.isCreative()) {
        player.displayClientMessage(
            Component.literal("Use a tool to break this!"), true);
        return false; // Cancel the break
    }
    return true; // Allow
});
```

### Item use on block (right-click)
```java
UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
    ItemStack stack = player.getItemInHand(hand);
    if (stack.is(ModItems.MY_TOOL) && world instanceof ServerLevel serverLevel) {
        BlockPos pos = hitResult.getBlockPos();
        // ... do something
        stack.hurtAndBreak(1, serverLevel, player instanceof ServerPlayer sp ? sp : null,
            item -> {});
        return InteractionResult.SUCCESS;
    }
    return InteractionResult.PASS;
});
```

---

## Mixin Patterns

### Inject at method head (cancel)
```java
@Mixin(SomeVanillaClass.class)
public class MyMixin {
    @Inject(method = "targetMethod", at = @At("HEAD"), cancellable = true)
    private void onTargetMethod(CallbackInfo ci) {
        if (someCondition) {
            ci.cancel(); // Prevent original method from running
        }
    }
}
```

### Modify return value
```java
@Inject(method = "getDestroyProgress", at = @At("HEAD"), cancellable = true)
private void modifyBreakSpeed(Player player, BlockGetter world, BlockPos pos,
                              CallbackInfoReturnable<Float> cir) {
    if (shouldModify) {
        cir.setReturnValue(0.0f); // Override return value
    }
}
```

---

## World Generation

### Biome-based mob spawning
```java
BiomeModifications.addSpawn(
    BiomeSelectors.includeByKey(Biomes.PLAINS, Biomes.FOREST),
    MobCategory.MONSTER,
    ModEntities.MY_MOB,
    5,    // weight (zombie=95, enderman=10)
    1,    // min group size
    2     // max group size
);
```

### Ore generation (quick reference)
```
data/<modid>/worldgen/configured_feature/my_ore.json
data/<modid>/worldgen/placed_feature/my_ore.json
data/<modid>/tags/worldgen/biome/has_feature/my_ore.json  (optional)
```

---

## Tips

- **Mappings**: Use `javap` on the remapped jar in `~/.gradle/caches/fabric-loom/minecraftMaven/` to find exact method names for your MC version
- **Debugging**: `./gradlew runClient` launches with hot-reload — change code, rebuild, `/reload` in-game
- **Textures**: 16x16 for items/blocks, entity textures match model UV layout
- **JSON hot-reload**: Datapacks (`/reload`) vs Java code (restart required)
- **Performance**: Use `placeBlock()` in StructurePiece (chunk-clipped) instead of `level.setBlock()` for structures
