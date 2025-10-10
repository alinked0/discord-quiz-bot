import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linked.quizbot.Constants;
import com.linked.quizbot.utils.User;
import com.linked.quizbot.utils.UserDataParser;

import net.dv8tion.jda.api.entities.emoji.Emoji;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class TestUserDataParser {
	// Mock Emoji object that will be returned by Emoji.fromFormatted
	private String emoji;
	private String tagName;


	@BeforeEach
	void setUp() {
		// Initialize a  Emoji object before each test
		emoji = "<:mocked_emoji:123456789>";
	   tagName = "mocked_emoji";
	}

	@Test
	@DisplayName("Test fromJsonFile with a valid file")
	void testFromJsonFile_ValidFile() throws IOException, JsonProcessingException{
		// Create a temporary JSON file for testing
		File tempFile = File.createTempFile("userData", ".json");
		tempFile.deleteOnExit(); // Ensure the file is deleted after the test

		String jsonContent = "{" +
							 Constants.MAPPER.writeValueAsString("prefixe")+": "+Constants.MAPPER.writeValueAsString("!test")+", " +
							 Constants.MAPPER.writeValueAsString("totalPointsEverGained")+": 100.5, " +
							 Constants.MAPPER.writeValueAsString("numberOfGamesPlayed")+": 5, " +
							 Constants.MAPPER.writeValueAsString("tagEmojiPerTagName")+": {" +
							 Constants.MAPPER.writeValueAsString("tagA")+": "+Constants.MAPPER.writeValueAsString("<:emojiA:12345>")+", " +
							 Constants.MAPPER.writeValueAsString("tagB")+": "+Constants.MAPPER.writeValueAsString("<:emojiB:67890>")+ 
							 "}" +
							 "}";

		try (FileWriter writer = new FileWriter(tempFile)) {
			writer.write(jsonContent);
		}
		
		User user = UserDataParser.fromJsonFile(tempFile.getAbsolutePath()).id("testUser").build();

		assertNotNull(user);
		assertEquals("!test", user.getPrefix());
		assertEquals(100.5, user.getTotalPointsEverGained());
		assertEquals(5, user.getNumberOfGamesPlayed());
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
		assertThrows(FileNotFoundException.class, () -> UserDataParser.fromJsonFile(nonExistentPath));
	}

	@Test
	@DisplayName("Test fromString with a valid JSON string")
	void testFromString_ValidString() throws IOException {
		String jsonContent = "{" +
							 Constants.MAPPER.writeValueAsString("prefixe")+": "+Constants.MAPPER.writeValueAsString("@quiz")+", " +
							 Constants.MAPPER.writeValueAsString("totalPointsEverGained")+": 250.75, " +
							 Constants.MAPPER.writeValueAsString("numberOfGamesPlayed")+": 15, " +
							 Constants.MAPPER.writeValueAsString("tagEmojiPerTagName")+": {" +
							 Constants.MAPPER.writeValueAsString("quiz")+": "+Constants.MAPPER.writeValueAsString("<:quiz_emoji:111222>")+ 
							 "}" +
							 "}";
		User user = UserDataParser.fromString(jsonContent).id("testUser").build();

		assertNotNull(user);
		assertEquals("@quiz", user.getPrefix());
		assertEquals(250.75, user.getTotalPointsEverGained());
		assertEquals(15, user.getNumberOfGamesPlayed());
		assertNotNull(user.getEmojiPerTagName());
		assertEquals(1, user.getEmojiPerTagName().size());
		assertTrue(user.getEmojiPerTagName().containsKey("quiz"));
		assertEquals("<:quiz_emoji:111222>", user.getEmojiPerTagName().get("quiz"));
	}

	@Test
	@DisplayName("Test fromString with an empty JSON object")
	void testFromString_EmptyObject() throws IOException {
		String jsonContent = "{}";

		User user = UserDataParser.fromString(jsonContent).id("testUser").build();

		assertNotNull(user);
		assertNull(user.getPrefix()); // Default value for String
		assertEquals(0.0, user.getTotalPointsEverGained()); // Default value for double
		assertEquals(0, user.getNumberOfGamesPlayed()); // Default value for int
		assertNotNull(user.getEmojiPerTagName());
		assertTrue(user.getEmojiPerTagName().isEmpty());
	}

	@Test
	@DisplayName("Test fromString with missing fields")
	void testFromString_MissingFields() throws IOException {
		String jsonContent = "{"+Constants.MAPPER.writeValueAsString("totalPointsEverGained")+": 50.0}";
			User user = UserDataParser.fromString(jsonContent).id("testUser").build();

		assertNotNull(user);
		assertNull(user.getPrefix());
		assertEquals(50.0, user.getTotalPointsEverGained());
		assertEquals(0, user.getNumberOfGamesPlayed()); // Default value
		assertNotNull(user.getEmojiPerTagName());
		assertTrue(user.getEmojiPerTagName().isEmpty());
	}

	@Test
	@DisplayName("Test parser with valid JsonParser input")
	void testParser_ValidInput() throws IOException {
		String jsonContent = "{" +
							 Constants.MAPPER.writeValueAsString("prefixe")+": "+Constants.MAPPER.writeValueAsString("#")+", " +
							 Constants.MAPPER.writeValueAsString("totalPointsEverGained")+": 75.25, " +
							 Constants.MAPPER.writeValueAsString("numberOfGamesPlayed")+": 8, " +
							 Constants.MAPPER.writeValueAsString("tagEmojiPerTagName")+": {" +
							 Constants.MAPPER.writeValueAsString("dev")+": "+Constants.MAPPER.writeValueAsString("<:dev_emoji:333444>")+", " +
							 Constants.MAPPER.writeValueAsString("testing")+": "+Constants.MAPPER.writeValueAsString("<:test_emoji:555666>")+ 
							 "}" +
							 "}";
		JsonParser jp = new JsonFactory().createParser(jsonContent);
		User user = UserDataParser.parser(jp).id("testUser").build();

		assertNotNull(user);
		assertEquals("#", user.getPrefix());
		assertEquals(75.25, user.getTotalPointsEverGained());
		assertEquals(8, user.getNumberOfGamesPlayed());
		assertNotNull(user.getEmojiPerTagName());
		assertEquals(2, user.getEmojiPerTagName().size());
		assertEquals("<:dev_emoji:333444>", user.getEmojiPerTagName().get("dev"));
		assertEquals("<:test_emoji:555666>", user.getEmojiPerTagName().get("testing"));
		jp.close();
	}

	@Test
	@DisplayName("Test parser with incorrect starting token (not START_OBJECT)")
	void testParser_IncorrectStartToken() throws IOException {
		// Simulate a JSON array or a string instead of an object
		String invalidJson = "["+Constants.MAPPER.writeValueAsString("not an object")+"]";
		JsonParser jp = new JsonFactory().createParser(invalidJson);

		assertThrows(IOException.class, () -> UserDataParser.parser(jp));
		jp.close();
	}

	@Test
	@DisplayName("Test parseEmojiPerTagName with valid emoji map")
	void testParseEmojiPerTagName_ValidMap() throws IOException {
		String jsonContent = "{"+Constants.MAPPER.writeValueAsString("tagEmojiPerTagName")+": {" +
								Constants.MAPPER.writeValueAsString("funny")+": "+Constants.MAPPER.writeValueAsString("<:laugh:123>")+", " +
								Constants.MAPPER.writeValueAsString("serious")+": "+Constants.MAPPER.writeValueAsString("<:think:456>")+ 
								"}}";
		JsonParser jp = new JsonFactory().createParser(jsonContent);
		// Navigate to the 'tagEmojiPerTagName' field
		jp.nextToken(); // START_OBJECT
		jp.nextToken(); // FIELD_NAME: "tagEmojiPerTagName"
		jp.nextToken(); // START_OBJECT of the map

		Map<String, String> result = UserDataParser.parseEmojiPerTagName(jp);

		assertNotNull(result);
		assertEquals(2, result.size());
		assertEquals("<:laugh:123>", result.get("funny"));
		assertEquals("<:think:456>", result.get("serious"));
		jp.close();
	}

	@Test
	@DisplayName("Test parseEmojiPerTagName with empty emoji map")
	void testParseEmojiPerTagName_EmptyMap() throws IOException {
		String jsonContent = "{"+Constants.MAPPER.writeValueAsString("tagEmojiPerTagName")+": {}}";
		JsonParser jp = new JsonFactory().createParser(jsonContent);
		// Navigate to the 'tagEmojiPerTagName' field
		jp.nextToken(); // START_OBJECT
		jp.nextToken(); // FIELD_NAME: "tagEmojiPerTagName"
		jp.nextToken(); // START_OBJECT of the map

		Map<String, String> result = UserDataParser.parseEmojiPerTagName(jp);

		assertNotNull(result);
		assertTrue(result.isEmpty());
		jp.close();
	}

	@Test
	@DisplayName("Test parseEmojiPerTagName when the field name is not 'tagEmojiPerTagName'")
	void testParseEmojiPerTagName_WrongFieldName() throws IOException {
		// Simulate a scenario where `parseEmojiPerTagName` is called but the current token
		// is inside another object or array, or points to a different field name,
		// but still within a structure that leads to an object.
		// This test ensures it handles moving past the potential FIELD_NAME check.
		String jsonContent = "{"+Constants.MAPPER.writeValueAsString("someOtherField")+": {"+Constants.MAPPER.writeValueAsString("key")+":"+Constants.MAPPER.writeValueAsString("value")+"}, "+Constants.MAPPER.writeValueAsString("tagEmojiPerTagName")+": {}}";
		JsonParser jp = new JsonFactory().createParser(jsonContent);
		// Simulate the parser being at the "someOtherField" key's value (which is an object)
		jp.nextToken(); // START_OBJECT
		jp.nextToken(); // FIELD_NAME "someOtherField"
		jp.nextToken(); // START_OBJECT of "someOtherField"
		jp.nextToken(); // FIELD_NAME "key"
		jp.nextToken(); // VALUE "value"
		jp.nextToken(); // END_OBJECT of "someOtherField"
		jp.nextToken(); // FIELD_NAME "tagEmojiPerTagName"
		jp.nextToken(); // START_OBJECT of "tagEmojiPerTagName"

		Map<String, String> result = UserDataParser.parseEmojiPerTagName(jp);

		assertNotNull(result);
		assertTrue(result.isEmpty());
		jp.close();
	}
}
