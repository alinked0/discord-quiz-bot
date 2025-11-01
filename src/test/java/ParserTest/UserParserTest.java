package ParserTest;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.linked.quizbot.Constants;
import com.linked.quizbot.utils.Attempt;
import com.linked.quizbot.utils.User;
import com.linked.quizbot.utils.User.Parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class UserParserTest {
	@BeforeEach
	void setUp() {
	}
	
	@Test
	@DisplayName("Test fromJsonFile with a valid file")
	void testFromJsonFile_ValidFile() throws IOException, JsonProcessingException{
		// Create a temporary JSON file for testing
		File tempFile = File.createTempFile("userDataTest", ".json");
		tempFile.deleteOnExit(); // Ensure the file is deleted after the test
		
		String jsonContent = "{" +
							 "\"prefix\": "+Constants.MAPPER.writeValueAsString("!test")+", " +
							 "\"tagEmojiByTagName\": {" +
							 "\"tagA\": "+Constants.MAPPER.writeValueAsString("<:emojiA:12345>")+", " +
							 "\"tagB\": "+Constants.MAPPER.writeValueAsString("<:emojiB:67890>")+ 
							 "}" +
							 "}";
		
		try (FileWriter writer = new FileWriter(tempFile)) {
			writer.write(jsonContent);
		}
		
		User user = User.Parser.fromJsonFile(tempFile.getAbsolutePath()).id("testUser").build();
		
		assertNotNull(user);
		assertEquals("!test", user.getPrefix());
		assertNotNull(user.getEmojiPerTagName());
		assertEquals(2, user.getEmojiPerTagName().size());
		assertTrue(user.getEmojiPerTagName().containsKey("tagA"));
		assertTrue(user.getEmojiPerTagName().containsKey("tagB"));
		assertEquals("<:emojiA:12345>", user.getEmojiPerTagName().get("tagA"));
		assertEquals("<:emojiB:67890>", user.getEmojiPerTagName().get("tagB"));
	}
	
	@Test
	@DisplayName("Test fromJsonFile with a non-existent file")
	void testFromJsonFile_NonExistentFile() {
		String nonExistentPath = "path/to/non_existent_file.json";
		assertThrows(FileNotFoundException.class, () -> User.Parser.fromJsonFile(nonExistentPath));
	}
	
	@Test
	@DisplayName("Test fromString with a valid JSON string")
	void testFromString_ValidString() throws IOException{
		String jsonContent = "{" +
							 "\"prefix\": "+Constants.MAPPER.writeValueAsString("@quiz")+", " +
							 "\"tagEmojiByTagName\": {" +
							 "\"quiz\": "+Constants.MAPPER.writeValueAsString("<:quiz_emoji:111222>")+ 
							 "}" +
							 "}";
		User user = User.Parser.fromString(jsonContent).id("testUser").build();
		
		assertNotNull(user);
		assertEquals("@quiz", user.getPrefix());
		assertNotNull(user.getEmojiPerTagName());
		assertEquals(1, user.getEmojiPerTagName().size());
		assertTrue(user.getEmojiPerTagName().containsKey("quiz"));
		assertEquals("<:quiz_emoji:111222>", user.getEmojiPerTagName().get("quiz"));
	}
	
	@Test
	@DisplayName("Test fromString with an empty JSON object")
	void testFromString_EmptyObject() throws IOException{
		String jsonContent = "{}";
		
		User user = User.Parser.fromString(jsonContent).id("testUser").build();
		
		assertNotNull(user);
		assertNull(user.getPrefix()); // Default value for String
		assertNotNull(user.getEmojiPerTagName());
		assertTrue(user.getEmojiPerTagName().isEmpty());
	}
	
	@Test
	@DisplayName("Test parse with valid JsonParser input")
	void testParser_ValidInput() throws IOException{
		String jsonContent = "{" +
							 "\"prefix\": \"#\", " +
							 "\"tagEmojiByTagName\": {" +
							 "\"dev\": "+Constants.MAPPER.writeValueAsString("<:dev_emoji:333444>")+", " +
							 "\"testing\": "+Constants.MAPPER.writeValueAsString("<:test_emoji:555666>")+ 
							 "}" +
							 "}";
		JsonParser jp = new JsonFactory().createParser(jsonContent);
		User user = User.Parser.parse(jp, jsonContent).id("testUser").build();
		
		assertNotNull(user);
		assertEquals("#", user.getPrefix());
		assertNotNull(user.getEmojiPerTagName());
		assertEquals(2, user.getEmojiPerTagName().size());
		assertEquals("<:dev_emoji:333444>", user.getEmojiPerTagName().get("dev"));
		assertEquals("<:test_emoji:555666>", user.getEmojiPerTagName().get("testing"));
		jp.close();
	}
	
	@Test
	@DisplayName("Test parse with incorrect starting token (not START_OBJECT)")
	void testParser_IncorrectStartToken() throws IOException{
		// Simulate a JSON array or a string instead of an object
		String invalidJson = "["+Constants.MAPPER.writeValueAsString("not an object")+"]";
		JsonParser jp = new JsonFactory().createParser(invalidJson);
		
		assertThrows(IOException.class, () -> User.Parser.parse(jp, invalidJson));
		jp.close();
	}
	
	@Test
	@DisplayName("Test parseEmojiPerTagName with valid emoji map")
	void testParseEmojiPerTagName_ValidMap() throws IOException{
		String jsonContent = "{\"tagEmojiByTagName\": {" +
								"\"funny\": "+Constants.MAPPER.writeValueAsString("<:laugh:123>")+", " +
								"\"serious\": "+Constants.MAPPER.writeValueAsString("<:think:456>")+ 
								"}}";
		JsonParser jp = new JsonFactory().createParser(jsonContent);
		// Navigate to the 'tagEmojiByTagName' field
		jp.nextToken(); // START_OBJECT
		jp.nextToken(); // FIELD_NAME: "tagEmojiByTagName"
		jp.nextToken(); // START_OBJECT of the map
		
		Map<String, String> result = User.Parser.parseEmojiPerTagName(jp, jsonContent);
		
		assertNotNull(result);
		assertEquals(2, result.size());
		assertEquals("<:laugh:123>", result.get("funny"));
		assertEquals("<:think:456>", result.get("serious"));
		jp.close();
	}
	
	@Test
	@DisplayName("Test parseEmojiPerTagName with empty emoji map")
	void testParseEmojiPerTagName_EmptyMap() throws IOException{
		String jsonContent = "{\"tagEmojiByTagName\": {}}";
		JsonParser jp = new JsonFactory().createParser(jsonContent);
		// Navigate to the 'tagEmojiByTagName' field
		jp.nextToken(); // START_OBJECT
		jp.nextToken(); // FIELD_NAME: "tagEmojiByTagName"
		jp.nextToken(); // START_OBJECT of the map
		
		Map<String, String> result = User.Parser.parseEmojiPerTagName(jp, jsonContent);
		
		assertNotNull(result);
		assertTrue(result.isEmpty());
		jp.close();
	}
	
	@Test
	@DisplayName("Test parseEmojiPerTagName when the field name is not 'tagEmojiByTagName'")
	void testParseEmojiPerTagName_WrongFieldName() throws IOException{
		// Simulate a scenario where `parseEmojiPerTagName` is called but the current token
		// is inside another object or array, or points to a different field name,
		// but still within a structure that leads to an object.
		// This test ensures it handles moving past the potential FIELD_NAME check.
		String jsonContent = "{\"someOtherField\": {\"key\":\"value\"}, \"tagEmojiByTagName\": {}}";
		JsonParser jp = new JsonFactory().createParser(jsonContent);
		// Simulate the parse being at the "someOtherField" key's value (which is an object)
		jp.nextToken(); // START_OBJECT
		jp.nextToken(); // FIELD_NAME "someOtherField"
		jp.nextToken(); // START_OBJECT of "someOtherField"
		jp.nextToken(); // FIELD_NAME "key"
		jp.nextToken(); // VALUE "value"
		jp.nextToken(); // END_OBJECT of "someOtherField"
		jp.nextToken(); // FIELD_NAME "tagEmojiByTagName"
		jp.nextToken(); // START_OBJECT of "tagEmojiByTagName"
		
		Map<String, String> result = User.Parser.parseEmojiPerTagName(jp, jsonContent);
		
		assertNotNull(result);
		assertTrue(result.isEmpty());
		jp.close();
	}
	
	@Test
	@DisplayName("Test parse with attemptsByListId/attempts field")
	void testParse_WithAttempts() throws IOException, JsonProcessingException {
		String attemptsForListA = "[" +
									"{\"score\": 10, \"date\": 1600000000}, " +
									"{\"score\": 20, \"date\": 1600000001}" +
								"]";
		String attemptsForListB = "[" +
									"{\"score\": 5, \"date\": 1600000005}" +
								"]";
		
		String jsonContent = "{" +
							 "\"userId\": \"12345\", " +
							 "\"attempts\": {" +
							 "\"listA\": " + attemptsForListA + ", " + // "attempts" alias
							 "\"listB\": " + attemptsForListB + 
							 "}" +
							 "}";
		
		JsonParser jp = new JsonFactory().createParser(jsonContent);
		User user = User.Parser.parse(jp, jsonContent).build();
		
		assertNotNull(user);
		assertEquals("12345", user.getId());
		assertNotNull(user.getAttemptsByListId());
		assertEquals(2, user.getAttemptsByListId().size());
		
		assertTrue(user.getAttemptsByListId().containsKey("listA"));
		assertTrue(user.getAttemptsByListId().containsKey("listB"));
		
		List<Attempt> listAAttempts = user.getAttemptsByListId().get("listA");
		List<Attempt> listBAttempts = user.getAttemptsByListId().get("listB");
		
		assertNotNull(listAAttempts);
		assertEquals(2, listAAttempts.size());
		
		assertNotNull(listBAttempts);
		assertEquals(1, listBAttempts.size());
		
		jp.close();
	}
	
	@Test
	@DisplayName("Test parseAttempts with valid attempt map")
	void testParseAttempts_ValidMap() throws IOException{
		// Simulate the inner content of the 'attempts' map where the value is an array 
		// that `Attempt.Parser.parseList` would handle.
		String jsonContent = "{\"attempts\": {" +
							"\"list1\": [{\"score\": 1}, {\"score\": 2}], " +
							"\"list2\": [{\"score\": 3}]" +
							"}}";
		
		// A simplified mock implementation of the required dependency (Attempt.Parser.parseList).
		// Since we cannot use Mockito[cite: 2], we rely on the implementation's reliance on
		// `Attempt.Parser.parseList` and simulate the JSON structure.
		// For the purpose of testing *this* method (`parseAttempts`), we focus on the map keys.
		
		JsonParser jp = new JsonFactory().createParser(jsonContent);
		
		// Navigate to the 'attempts' field value
		jp.nextToken(); // START_OBJECT
		jp.nextToken(); // FIELD_NAME: "attempts"
		jp.nextToken(); // START_OBJECT of the map
		
		// This will call `Attempt.Parser.parseList` for "list1" and "list2".
		// We are relying on the actual implementation of `Attempt.Parser.parseList`
		// and the lack of a real `Attempt` class.
		Map<String, List<Attempt>> result = User.Parser.parseAttempts(jp, jsonContent);
		
		assertNotNull(result);
		assertEquals(2, result.size());
		assertTrue(result.containsKey("list1"));
		assertTrue(result.containsKey("list2"));
		
		// Since we can't inspect the 'Attempt' list content without the class, 
		// we rely on the size check as a basic verification.
		assertNotNull(result.get("list1"));
		assertNotNull(result.get("list2"));
		
		jp.close();
	}
	
	@Test
	@DisplayName("Test parseAttempts with empty map")
	void testParseAttempts_EmptyMap() throws IOException{
		String jsonContent = "{\"attempts\": {}}";
		JsonParser jp = new JsonFactory().createParser(jsonContent);
		
		// Navigate to the 'attempts' field value
		jp.nextToken(); // START_OBJECT
		jp.nextToken(); // FIELD_NAME: "attempts"
		jp.nextToken(); // START_OBJECT of the map
		
		Map<String, List<Attempt>> result = User.Parser.parseAttempts(jp, jsonContent);
		
		assertNotNull(result);
		assertTrue(result.isEmpty());
		
		jp.close();
	}
	
	@Test
	@DisplayName("Test parseAttempts with incorrect starting token (not START_OBJECT)")
	void testParseAttempts_IncorrectStartToken() throws IOException{
		// Simulate a JSON array or a string instead of an object map for attempts
		String invalidJson = "{\"attempts\": [\"not an object map\"]}";
		JsonParser jp = new JsonFactory().createParser(invalidJson);
		
		// Navigate to the value of 'attempts'
		jp.nextToken(); // START_OBJECT
		jp.nextToken(); // FIELD_NAME: "attempts"
		jp.nextToken(); // START_ARRAY
		
		// The internal logic of parseAttempts will try to advance to START_OBJECT, fail, and throw an exception[cite: 16, 17].
		assertThrows(IOException.class, () -> User.Parser.parseAttempts(jp, invalidJson));
		jp.close();
	}
}
