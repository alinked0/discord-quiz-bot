package ParserTest;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import com.linked.quizbot.utils.Awnser;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit Test suite for Awnser.Parser.parse.
 * This test uses a real ObjectMapper to create a JsonParser instance,
 * honoring the constraint of not mocking dependencies and allowing
 * dependency failures (like Option.Parser.parseList) to be reflected.
 */
public class AwnserParserTest {
	
	// Helper to create a real JsonParser from a JSON string
	private JsonParser createParser(String json) throws IOException{
		// We use ObjectMapper as a convenient way to get a configured JsonFactory and Parser
		ObjectMapper mapper = new ObjectMapper();
		// The parser is created and is pointing at NO_TOKEN, so Awnser.Parser.parse
		// will call jp.nextToken() to find the START_OBJECT
		return mapper.getFactory().createParser(json);
	}
	
	/**
	 * Test case for parsing a valid JSON object with both fields present.
	 */
	@Test
	void testParse_ValidCompleteJson() throws IOException{
		// The structure of the Option response must match what the Option.Parser.parseList stub expects.
		// The stub consumes objects within the array.
		String json = """
			{
				"duration": 5000,
				"response": [
					{"explication":"Expl A","option":"A","correct":false},
					{"explication":null,"option":"B","correct":false}
				]
			}
			""";
		
		JsonParser jp = createParser(json);
		
		// Since the Option stub just consumes the objects and inserts placeholders,
		// we check if a non-null Awnser object is returned.
		Awnser result = Awnser.Parser.parse(jp, json);
		
		assertNotNull(result, "Awnser object should not be null for valid input.");
		assertEquals(5000L, result.getDuration(), "Duration should be parsed correctly.");
		assertFalse(result.getResponses().isEmpty(), "Response set should not be empty.");
		
		// The stub Option.Parser.parseList adds 2 options
		assertEquals(2, result.getResponses().size(), "Response set should contain 2 Option stubs.");
	}
	
	/**
	 * Test case for when the 'duration' field is missing.
	 * The parser logic should return null if either required field is missing.
	 */
	@Test
	void testParse_MissingDuration() throws IOException{
		String json = """
			{
				"response": [
					{"explication":"Expl A","option":"A","correct":false}
				]
			}
			""";
		
		JsonParser jp = createParser(json);
		Awnser result = Awnser.Parser.parse(jp, json);
		
		assertNull(result, "Awnser should be null when 'duration' is missing.");
	}
	
	/**
	 * Test case for when the 'response' field is missing.
	 * The parser logic should return null if either required field is missing.
	 */
	@Test
	void testParse_MissingResponse() throws IOException{
		String json = """
			{
				"duration": 15000
			}
			""";
		
		JsonParser jp = createParser(json);
		Awnser result = Awnser.Parser.parse(jp, json);
		
		assertNull(result, "Awnser should be null when 'response' is missing.");
	}
	
	/**
	 * Test case for an empty object. Both fields are missing.
	 */
	@Test
	void testParse_EmptyObject() throws IOException{
		String json = "{}";
		
		JsonParser jp = createParser(json);
		Awnser result = Awnser.Parser.parse(jp, json);
		
		assertNull(result, "Awnser should be null for an empty JSON object.");
	}
	
	/**
	 * Test case for invalid starting token (not START_OBJECT).
	 * This directly tests the initial exception handling in Awnser.Parser.
	 */
	@Test
	void testParse_InvalidStartToken() throws IOException{
		// A simple JSON array, which is not an object
		String json = "[\"duration\": 5000]";
		
		JsonParser jp = createParser(json);
		
		// We expect the IOException to be thrown immediately because the first token is START_ARRAY
		assertThrows(IOException.class, () -> Awnser.Parser.parse(jp, json),
			"Should throw IOException when input does not start with a JSON object.");
	}
	
	/**
	 * Test case for an object with extra, unhandled fields.
	 * The parser should ignore unknown fields and successfully parse the known ones.
	 */
	@Test
	void testParse_WithExtraFields() throws IOException{
		String json = """
			{
				"duration": 2500,
				"extraField1": "ignore me",
				"response": [
					{"explication":"Expl A","option":"A","correct":false}
				],
				"extraField2": 123
			}
			""";
		
		JsonParser jp = createParser(json);
		Awnser result = Awnser.Parser.parse(jp, json);
		
		assertNotNull(result, "Awnser object should not be null despite extra fields.");
		assertEquals(2500L, result.getDuration(), "Duration should be parsed correctly.");
		assertEquals(1, result.getResponses().size(), "Response set should contain 1 Option stub.");
	}
}
