package com.erik.machinaarcana.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * ★ JAVA 21 FEATURE: Builder Pattern with Fluent API ★
 *
 * The Builder Pattern separates object construction from the object itself.
 * Instead of a constructor with many parameters, you chain method calls:
 *
 * <pre>
 *   Optional<AssemblerBuilder> result = AssemblerBuilder.create()
 *       .inWorld(level)
 *       .withCore(corePos)
 *       .withFrames(framePositions)
 *       .validate()
 *       .build();
 * </pre>
 *
 * Key Builder characteristics demonstrated here:
 *
 * <h3>1. Private Constructor + Static Factory</h3>
 * {@code new AssemblerBuilder()} is forbidden — you must use {@code create()}.
 * This ensures the builder starts in a defined state.
 *
 * <h3>2. Fluent Returns (method chaining)</h3>
 * Every setter returns {@code this}, enabling the call chain above.
 * Each call returns the SAME builder instance — no copies are made.
 *
 * <h3>3. Optional Result</h3>
 * {@code build()} returns {@code Optional<AssemblerBuilder>} rather than
 * throwing exceptions. This forces callers to handle the failure case explicitly
 * (unlike exceptions, which can be silently ignored).
 *
 * <h3>4. Validation Step</h3>
 * {@code validate()} checks preconditions and sets an internal error message.
 * {@code build()} returns empty if validation failed.
 */
public class AssemblerBuilder {

    // ── Internal State ─────────────────────────────────────────────────────

    private Level level;
    private BlockPos corePos;
    private final List<BlockPos> framePositions = new ArrayList<>();
    private String validationError = null;
    private boolean validated = false;

    // The validator is a dependency — injected at construction for testability
    private final MultiBlockValidator validator;

    // ── Static Factory (replaces public constructor) ───────────────────────

    /**
     * ★ Static factory method ★
     *
     * Named factories are more expressive than constructors:
     *   - The name "create" makes intent clear
     *   - Can return subtypes (unlike constructors)
     *   - Can cache instances (unlike constructors)
     *
     * Constructor is private (see below) — this is the only way to create a builder.
     *
     * @return a new, unconfigured builder
     */
    public static AssemblerBuilder create() {
        return new AssemblerBuilder();
    }

    /**
     * Private constructor — enforces use of {@link #create()}.
     *
     * ★ Pattern: private constructor + static factory ★
     * This lets us control how builders are created (could add caching, etc.)
     */
    private AssemblerBuilder() {
        this.validator = new MultiBlockValidator();
    }

    // ── Fluent Setters (Builder steps) ─────────────────────────────────────

    /**
     * Sets the world to build in.
     *
     * ★ Fluent return: returns `this` for chaining ★
     *
     * @param level the game world
     * @return this builder (for chaining)
     */
    public AssemblerBuilder inWorld(Level level) {
        this.level = level;
        return this; // ← key to fluent API
    }

    /**
     * Sets the position of the central Assembler Core block.
     *
     * @param corePos center of the 3×3×3 structure
     * @return this builder (for chaining)
     */
    public AssemblerBuilder withCore(BlockPos corePos) {
        this.corePos = corePos;
        return this;
    }

    /**
     * Adds a list of expected frame positions.
     * You can also call {@link #withFrame} for individual positions.
     *
     * @param positions frame block positions
     * @return this builder (for chaining)
     */
    public AssemblerBuilder withFrames(List<BlockPos> positions) {
        this.framePositions.addAll(positions);
        return this;
    }

    /**
     * Adds a single frame position.
     *
     * @param pos one frame block position
     * @return this builder (for chaining)
     */
    public AssemblerBuilder withFrame(BlockPos pos) {
        this.framePositions.add(pos);
        return this;
    }

    /**
     * Validates the multi-block structure in the world.
     *
     * This is the "validate" step — it checks that:
     *   1. Level and corePos are set
     *   2. The world contains the correct blocks at all positions
     *
     * If validation fails, records the error; {@code build()} will return empty.
     *
     * @return this builder (for chaining — allows .validate().build())
     */
    public AssemblerBuilder validate() {
        this.validated = true;

        // Precondition checks
        if (level == null) {
            this.validationError = "Level not set (call inWorld() first)";
            return this;
        }
        if (corePos == null) {
            this.validationError = "Core position not set (call withCore() first)";
            return this;
        }

        // Delegate to the validator for the actual block checks
        MultiBlockValidator.ValidationResult result = validator.validate(level, corePos);
        if (!result.valid()) {
            this.validationError = result.failureReason();
        }

        return this;
    }

    // ── Terminal Operation ─────────────────────────────────────────────────

    /**
     * Finalizes the builder and returns the result.
     *
     * ★ JAVA FEATURE: Optional<T> for nullable results ★
     *
     * Instead of returning null (which causes NullPointerExceptions) or
     * throwing exceptions (which callers might ignore), we return Optional.
     * This makes the "might fail" contract explicit in the type system.
     *
     * Callers MUST handle both cases:
     * <pre>
     *   result.ifPresent(builder -> ...)        // act on success
     *   result.orElseThrow(...)                  // throw on failure
     *   result.orElse(defaultValue)              // use fallback
     * </pre>
     *
     * @return Optional containing this builder if valid, empty if not
     */
    public Optional<AssemblerBuilder> build() {
        if (!validated) {
            // Call validate() automatically if the caller forgot
            validate();
        }
        if (validationError != null) {
            return Optional.empty();
        }
        return Optional.of(this);
    }

    // ── Accessors (for use after successful build()) ───────────────────────

    /**
     * @return the level this assembler is in
     */
    public Level getLevel() {
        return level;
    }

    /**
     * @return the position of the core block
     */
    public BlockPos getCorePos() {
        return corePos;
    }

    /**
     * @return the frame block positions
     */
    public List<BlockPos> getFramePositions() {
        return List.copyOf(framePositions);
    }

    /**
     * @return the validation error message, or null if valid
     */
    public String getValidationError() {
        return validationError;
    }

    /**
     * @return true if the structure is valid and build() will succeed
     */
    public boolean isValid() {
        return validationError == null;
    }
}
