package ParserTest;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.linked.quizbot.utils.Attempt;
import com.linked.quizbot.utils.Awnser;
import com.linked.quizbot.utils.Attempt.Parser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration Test for Attempt.Parser.
 */
public class AttemptParserTest {
	
	private JsonFactory factory;
	private static final String ORIGINAL_JSON = "Test JSON string for context";
	
	// Minimal valid JSON for dependencies, assumed to work with their respective parsers.
	private static final String VALID_AWNSER_JSON = 
		"{\"duration\": 500, \"response\": []}"; // Assumes Option[] is a valid response structure
	private static final String VALID_QUESTIONLIST_JSON = 
		"{\"questions\": []}"; // Assumes this is enough to produce a QuestionList object
	
	@BeforeEach
	void setUp() {
		factory = new JsonFactory();
	}
	
	// --- Helper to create a JsonParser from a JSON string ---
	private JsonParser createParser(String json) throws IOException{
		JsonParser jp = factory.createParser(new StringReader(json));
		// Advance the parser to the first meaningful token
		if (jp.nextToken() == null) {
			jp = factory.createParser(new StringReader(json)); // Reset if empty
			jp.nextToken();
		}
		return jp;
	}
	
	
	@Test
	void testParseAwnsersByQuestion_Success() throws IOException{
		String json = "{\"1\": " + VALID_AWNSER_JSON + ", \"5\": " + VALID_AWNSER_JSON + "}";
		
		JsonParser jp = createParser(json);
		
		// This relies on Awnser.Parser.parse() working correctly
		Map<Integer, Awnser> result = Parser.parseAwnsersByQuestion(jp, ORIGINAL_JSON);
		
		assertNotNull(result);
		assertEquals(2, result.size());
		assertTrue(result.containsKey(1));
		assertTrue(result.containsKey(5));
		
		// Ensure Awnser objects were actually created (non-null)
		assertNotNull(result.get(1));
		assertNotNull(result.get(5));
	}
	
	@Test
	void testParseAwnsersByQuestion_ThrowsExceptionOnInvalidInput() {
		String json = "\"not_an_object\""; 
		
		assertThrows(IOException.class, () -> {
			JsonParser jp = createParser(json);
			Parser.parseAwnsersByQuestion(jp, ORIGINAL_JSON);
		});
	}
	
	
	@Test
	void testParse_Success_FullIntegration() throws IOException{
		long startTime = 1000L;
		long endTime = 2000L;
		String userId = "U12345";
		
		// Construct a complete, valid Attempt JSON string using dependency JSON.
		String json = String.format("""
			{
				"questionList": %s, 
				"userId": "%s", 
				"timeStartedMillis": %d, 
				"timeEndedMillis": %d, 
				"awnsersByQuestion": {"0": %s}
			}
			""", VALID_QUESTIONLIST_JSON, userId, startTime, endTime, VALID_AWNSER_JSON);
		
		JsonParser jp = createParser(json);
		
		// This tests the full path: parse -> QuestionList.Parser.parse -> parseAwnsersByQuestion -> Awnser.Parser.parse
		Attempt result = Parser.parse(jp, ORIGINAL_JSON);
		
		assertNotNull(result);
		assertEquals(userId, result.getUserId());
		assertEquals(startTime, result.getStart());
		assertEquals(endTime, result.getEnd());
		assertNotNull(result.getQuestionList(), "QuestionList should be parsed successfully.");
		assertEquals(1, result.getAwnsers().size(), "Should have 1 awnser parsed.");
	}
	
	@Test
	void testParse_HandlesAliasFields() throws IOException{
		String json = String.format("""
			{
				"list": %s, 
				"userId": "U54321", 
				"start": 100, 
				"end": 200, 
				"awnsers": {}
			}
			""", VALID_QUESTIONLIST_JSON);
		
		JsonParser jp = createParser(json);
		Attempt result = Parser.parse(jp, ORIGINAL_JSON);
		
		assertNotNull(result);
		assertEquals("U54321", result.getUserId(), "Should handle 'userId'");
		assertEquals(100L, result.getStart(), "Should handle 'start' alias for timeStartedMillis");
		assertEquals(200L, result.getEnd(), "Should handle 'end' alias for timeEndedMillis");
		assertTrue(result.getAwnsers().isEmpty(), "Should handle 'awnsers' alias for awnsersByQuestion");
		assertNotNull(result.getQuestionList(), "Should handle 'list' alias for questionList");
	}
	
	@Test
	void testParse_ReturnsNullOnMissingField() throws IOException{
		// Missing "timeEndedMillis" (and "end") field
		String json = String.format("""
			{
				"questionList": %s, 
				"userId": "12345", 
				"timeStartedMillis": 1000, 
				"awnsersByQuestion": {}
			}
			""", VALID_QUESTIONLIST_JSON);
		
		JsonParser jp = createParser(json);
		Attempt result = Parser.parse(jp, ORIGINAL_JSON);
		
		assertNull(result, "Attempt should be null if any required field is missing.");
	}
	
	
	@Test
	void testParseList_Success() throws IOException{
		// Two valid attempts in an array
		String singleAttempt = String.format(
			"{\"userId\":\"u1\", \"questionList\":%s, \"timeStartedMillis\":10, \"timeEndedMillis\":20, \"awnsersByQuestion\":{}}", 
			VALID_QUESTIONLIST_JSON);
			
		String json = "[" + singleAttempt + ", " + singleAttempt.replace("u1", "u2") + "]";
		
		JsonParser jp = createParser(json);
		
		List<Attempt> result = Parser.parseList(jp, ORIGINAL_JSON);
		
		assertNotNull(result);
		assertEquals(2, result.size());
		assertEquals("u1", result.get(0).getUserId());
		assertEquals("u2", result.get(1).getUserId());
	}
	
	@Test
	void testParseList_EmptyArray() throws IOException{
		String json = "[]";
		
		JsonParser jp = createParser(json);
		List<Attempt> result = Parser.parseList(jp, ORIGINAL_JSON);
		
		assertNotNull(result);
		assertTrue(result.isEmpty());
	}
	
	@Test
	void testParseList_ThrowsExceptionOnInvalidInput() {
		String json = "\"not_an_array\""; 
		
		assertThrows(IOException.class, () -> {
			JsonParser jp = createParser(json);
			Parser.parseList(jp, ORIGINAL_JSON);
		});
	}
}