package com.erik.medievalconquest.world;

import com.erik.medievalconquest.MedievalConquestMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;

/**
 * Registers custom structure types and piece types for world generation.
 * The actual structure placement is data-driven (JSON in data pack).
 */
public class ModStructures {

	public static final StructureType<CastleStructure> CASTLE_TYPE = () -> CastleStructure.CODEC;

	public static final StructurePieceType CASTLE_PIECE = CastlePiece::new;

	public static void register() {
		Registry.register(BuiltInRegistries.STRUCTURE_TYPE,
				Identifier.fromNamespaceAndPath(MedievalConquestMod.MOD_ID, "castle"),
				CASTLE_TYPE);

		Registry.register(BuiltInRegistries.STRUCTURE_PIECE,
				Identifier.fromNamespaceAndPath(MedievalConquestMod.MOD_ID, "castle_piece"),
				CASTLE_PIECE);

		MedievalConquestMod.LOGGER.info("Structures registered!");
	}
}
