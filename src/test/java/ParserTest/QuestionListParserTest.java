package ParserTest;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.linked.quizbot.utils.Question;
import com.linked.quizbot.utils.QuestionList;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 test class for QuestionList.Parser, without using Mockito.
 * Uses minimal stub classes for dependencies like Question and Constants.
 * Assumes QuestionList.Hasher is correctly implemented for ID/time generation.
 */
public class QuestionListParserTest {
	@BeforeEach
	void setup() {
		// Ensure deterministic IDs for tests that rely on QuestionList.Hasher.generate()
		QuestionList.Hasher.clearGeneratedCodes();
	}
	
	/**
	 * Tests successful parsing of a JSON string containing all primary fields.
	 */
	@Test
	@DisplayName("Test successful parsing of a full QuestionList JSON string")
	void testParseFullJsonString() throws IOException {
		// A known timestamp for deterministic testing
		long fixedTime = 1672531200000L; // Jan 1, 2023
		String json = String.format("""
			{
				"ownerId": "user123",
				"name": "My Science Quiz",
				"id": "fixedid",
				"timeCreatedMillis": %d,
				"emojiPerTagName": {
					"Science": "\uD83E\uDDEA",
					"Math": "\uD83D\uDCDD"
				},
				"questions": [
					{ "question": "q1", "options": [{"explication":"Incorrect. CO2 is carbon dioxide","text":"Carbon Dioxide","correct":true}] },
					{ "question": "q2", "options": [{"explication":"Incorrect. CO2 is carbon dioxide","text":"Carbon Dioxide","correct":true}] }
				]
			}
			""", fixedTime);
		
		QuestionList list = QuestionList.Parser.fromString(json).build();
		
		assertNotNull(list);
		assertEquals("user123", list.getOwnerId());
		assertEquals("My Science Quiz", list.getName());
		assertEquals("fixedid", list.getId());
		assertEquals(fixedTime, list.getTimeCreatedMillis());
		assertEquals(2, list.size(), "Should have 2 questions parsed (due to Question stub).");
		
		Map<String, String> tags = list.getEmojiPerTagName();
		assertEquals(2, tags.size(), "Should have 2 tags parsed.");
		assertTrue(tags.containsKey("Science"));
		assertEquals("\uD83E\uDDEA", tags.get("Science"));
	}
	
	/**
	 * Tests that the parser correctly handles alternate and case-insensitive field names.
	 */
	@Test
	@DisplayName("Test parsing with alternate and case-insensitive field names")
	void testParseAlternateFieldNames() throws IOException {
		String json = """
			{
				"USERID": "alt_user",
				"listid": "alt_id00",
				"Name": "Alternate Test",
				"questions": []
			}
			""";
		
		QuestionList list = QuestionList.Parser.fromString(json).build();
		
		assertNotNull(list);
		assertEquals("alt_user", list.getOwnerId(), "Should map 'USERID' to ownerId.");
		assertEquals("Alternate Test", list.getName(), "Should be case-insensitive for 'Name'.");
		assertEquals("alt_id00", list.getId(), "Should map 'listid' to id.");
	}
	
	/**
	 * Tests that the internal `build()` method correctly generates `id` and `timeCreatedMillis` 
	 * if they are missing in the JSON input.
	 */
	@Test
	@DisplayName("Test parsing with missing non-required fields (id and timeCreatedMillis should be generated)")
	void testParseMissingOptionalFields() throws IOException {
		String json = """
			{
				"ownerId": "generator_test",
				"name": "Missing Fields Quiz",
				"emojiPerTagName": { "Tag": "Emoji" },
				"questions": []
			}
			""";
		
		long beforeParse = System.currentTimeMillis();
		QuestionList list = QuestionList.Parser.fromString(json).build();
		long afterParse = System.currentTimeMillis();
		
		assertNotNull(list);
		
		assertNotNull(list.getId());
		assertTrue(list.getId().length() >= QuestionList.Hasher.DEFAULT_LENGTH);
		
		assertTrue(list.getTimeCreatedMillis() >= beforeParse && list.getTimeCreatedMillis() <= afterParse, 
				   "TimeCreatedMillis should be set around the time of parsing.");
	}
	
	/**
	 * Tests the boundary condition where the JSON is structurally correct but contains no data.
	 */
	@Test
	@DisplayName("Test parsing of an empty question list (no questions, no tags)")
	void testParseEmptyList() throws IOException {
		String json = """
			{
				"ownerId": "empty",
				"name": "Empty Quiz",
				"emojiPerTagName": {},
				"questions": []
			}
			""";
		
		QuestionList list = QuestionList.Parser.fromString(json).build();
		
		assertNotNull(list);
		assertTrue(list.isEmpty());
		assertTrue(list.getEmojiPerTagName().isEmpty());
	}
	
	/**
	 * Tests that the main `parse` method throws an `IOException` when the input does not start with a JSON object.
	 */
	@Test
	@DisplayName("Test parsing failure when input is not a JSON object")
	void testParseInvalidJsonInput() {
		String invalidJson = "\"Just a string, not an object\"";
		
		Exception exception = assertThrows(IOException.class, () -> {
			QuestionList.Parser.fromString(invalidJson);
		});
		
		assertTrue(exception.getMessage().contains("QuestionList.Parser.parse, input is not a json:"));
	}
	
	/**
	 * Tests the lower-level `parseEmojiPerTagName` directly with an empty map token.
	 */
	@Test
	@DisplayName("Test parseEmojiPerTagName handles an empty map")
	void testParseEmptyEmojiMap() throws IOException {
		JsonParser jp = new JsonFactory().createParser("{}");
		
		Map<String, String> result = QuestionList.Parser.parseEmojiPerTagName(jp, "{}");
		
		assertNotNull(result);
		assertTrue(result.isEmpty());
	}
	
	/**
	 * Tests that the lower-level `parseList` handles an empty array token.
	 */
	@Test
	@DisplayName("Test parseList handles an empty array")
	void testParseEmptyQuestionArray() throws IOException {
		JsonParser jp = new JsonFactory().createParser("[]");
		
		List<Question> result = QuestionList.Parser.parseList(jp, "[]");
		
		assertNotNull(result);
		assertTrue(result.isEmpty());
	}
}