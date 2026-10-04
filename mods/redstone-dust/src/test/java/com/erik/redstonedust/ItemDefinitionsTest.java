package com.erik.redstonedust;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

class ItemDefinitionsTest {

	@Test
	void kitHasModernItemDefinition() throws IOException {
		JsonObject definition = readObject("assets/redstonedust/items/redstone_kit.json");
		JsonObject model = objectMember(definition, "model");
		assertEquals("minecraft:model", stringMember(model, "type"));
		assertEquals("redstonedust:item/redstone_kit", stringMember(model, "model"));
	}

	@Test
	void tutorialHasModernItemDefinition() throws IOException {
		JsonObject definition = readObject("assets/redstonedust/items/tutorial_book.json");
		JsonObject model = objectMember(definition, "model");
		assertEquals("minecraft:model", stringMember(model, "type"));
		assertEquals("redstonedust:item/tutorial_book", stringMember(model, "model"));
	}

	@Test
	void existingLegacyModelsKeepExpectedTextureReferences() throws IOException {
		JsonObject kit = readObject("assets/redstonedust/models/item/redstone_kit.json");
		assertEquals("minecraft:item/generated", stringMember(kit, "parent"));
		assertEquals("minecraft:item/redstone", stringMember(objectMember(kit, "textures"), "layer0"));

		JsonObject tutorial = readObject("assets/redstonedust/models/item/tutorial_book.json");
		assertEquals("minecraft:item/generated", stringMember(tutorial, "parent"));
		assertEquals("minecraft:item/enchanted_book", stringMember(objectMember(tutorial, "textures"), "layer0"));
	}

	private static JsonObject readObject(String path) throws IOException {
		InputStream stream = ItemDefinitionsTest.class.getClassLoader().getResourceAsStream(path);
		assertNotNull(stream, "Missing item resource: " + path);
		try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
			JsonElement value = JsonParser.parseReader(reader);
			assertTrue(value.isJsonObject(), "Expected JSON object: " + path);
			return value.getAsJsonObject();
		}
	}

	private static JsonObject objectMember(JsonObject object, String name) {
		JsonElement value = object.get(name);
		assertNotNull(value, "Missing JSON member: " + name);
		assertTrue(value.isJsonObject(), "Expected object member: " + name);
		return value.getAsJsonObject();
	}

	private static String stringMember(JsonObject object, String name) {
		JsonElement value = object.get(name);
		assertNotNull(value, "Missing JSON member: " + name);
		assertTrue(value.isJsonPrimitive(), "Expected string member: " + name);
		assertTrue(value.getAsJsonPrimitive().isString(), "Expected string member: " + name);
		return value.getAsString();
	}
}
