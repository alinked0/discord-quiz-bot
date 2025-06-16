
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.linked.quizbot.utils.Users;
import com.linked.quizbot.utils.UserDataParser;

import net.dv8tion.jda.api.entities.emoji.Emoji;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class TestUserDataParser {

    @TempDir
    File tempDir;

    private Users sampleUserData;
    private String sampleJson;

    @BeforeEach
    void setUp() {
        sampleUserData = new Users("test_user_id");
        sampleUserData.setTotalPointsEverGained(100);
        sampleUserData.setNumberOfGamesPlayed(5);
        sampleUserData.createTag("tagA", Emoji.fromFormatted("<:tagA:123456789012345678>"));
        sampleUserData.createTag("tagB", Emoji.fromUnicode("👍"));

        // Manually construct a sample JSON string that matches Users's userDataToString()
        // for direct string parsing tests.
        sampleJson = sampleUserData.toString();
    }

    // --- 1. Test fromJsonFile() ---
    @Test
    void testFromJsonFile_validJson() throws IOException {
        File jsonFile = new File(tempDir, "user_data.json");
        try (FileWriter writer = new FileWriter(jsonFile)) {
            writer.write(sampleJson);
        }
        Users parsedUserData = UserDataParser.fromJsonFile(jsonFile.getAbsolutePath());
        assertNotNull(parsedUserData);
        assertEquals(sampleUserData.getTotalPointsEverGained(), parsedUserData.getTotalPointsEverGained());
        assertEquals(sampleUserData.getNumberOfGamesPlayed(), parsedUserData.getNumberOfGamesPlayed());
        assertEquals(sampleUserData.getTagEmojiPerTagName().size(), parsedUserData.getTagEmojiPerTagName().size());
        assertTrue(parsedUserData.getTagEmojiPerTagName().containsKey("tagA"));
        assertTrue(parsedUserData.getTagEmojiPerTagName().containsKey("tagB"));
        assertEquals(sampleUserData.getTagEmojiPerTagName().get("tagA"), parsedUserData.getTagEmojiPerTagName().get("tagA"));
        assertEquals(sampleUserData.getTagEmojiPerTagName().get("tagB"), parsedUserData.getTagEmojiPerTagName().get("tagB"));
    }

    @Test
    void testFromJsonFile_missingFile() {
        File missingFile = new File(tempDir, "non_existent_user_data.json");
        Users result = new Users("another_user123");
         Users parsedUserData =null;
		try{
            parsedUserData = UserDataParser.fromJsonFile(missingFile.getAbsolutePath());
		} catch (IOException e) {
		}
        assertNull(parsedUserData); // Should return null if file not found
    }

    // --- 2. Test fromString() ---
    @Test
    void testFromString_validJson()  throws IOException {
        Users parsedUserData = UserDataParser.fromString(sampleJson);
        assertNotNull(parsedUserData);
        assertEquals(sampleUserData.getTotalPointsEverGained(), parsedUserData.getTotalPointsEverGained());
        assertEquals(sampleUserData.getNumberOfGamesPlayed(), parsedUserData.getNumberOfGamesPlayed());
        assertEquals(sampleUserData.getTagEmojiPerTagName().size(), parsedUserData.getTagEmojiPerTagName().size());
        assertTrue(parsedUserData.getTagEmojiPerTagName().containsKey("tagA"));
        assertTrue(parsedUserData.getTagEmojiPerTagName().containsKey("tagB"));
        assertEquals(sampleUserData.getTagEmojiPerTagName().get("tagA"), parsedUserData.getTagEmojiPerTagName().get("tagA"));
        assertEquals(sampleUserData.getTagEmojiPerTagName().get("tagB"), parsedUserData.getTagEmojiPerTagName().get("tagB"));
    }
    @Test
    void testFromString_emptyJson()  throws IOException {
        String emptyJson = "{}";
        Users parsedUserData = UserDataParser.fromString(emptyJson);
        assertNotNull(parsedUserData);
        assertEquals(0, parsedUserData.getTotalPointsEverGained());
        assertEquals(0, parsedUserData.getNumberOfGamesPlayed());
        assertTrue(parsedUserData.getTagEmojiPerTagName().isEmpty());
    }

    // --- 3. Test parser(JsonParser, Users) ---
    @Test
    void testParser_directly() throws IOException {
        JsonParser jp = new JsonFactory().createParser(sampleJson);
        Users parsedUserData = UserDataParser.parser(jp);

        assertNotNull(parsedUserData);
        assertEquals(sampleUserData.getTotalPointsEverGained(), parsedUserData.getTotalPointsEverGained());
        assertEquals(sampleUserData.getNumberOfGamesPlayed(), parsedUserData.getNumberOfGamesPlayed());
        assertEquals(sampleUserData.getTagEmojiPerTagName().size(), parsedUserData.getTagEmojiPerTagName().size());
        assertTrue(parsedUserData.getTagEmojiPerTagName().containsKey("tagA"));
        assertTrue(parsedUserData.getTagEmojiPerTagName().containsKey("tagB"));
    }

    // --- 4. Test parseEmojiPerTagName() ---
    @Test
    void testParseEmojiPerTagName_validJson() throws IOException {
        String emojiMapJson = "{\"tag1\":\"<:emoji1:123456789012345678>\", \"tag2\":\"😂\"}";
        JsonParser jp = new JsonFactory().createParser(emojiMapJson);
        jp.nextToken(); // Move to START_OBJECT

        Map<String, Emoji> parsedMap = UserDataParser.parseEmojiPerTagName(jp);

        assertNotNull(parsedMap);
        assertEquals(2, parsedMap.size());
        assertTrue(parsedMap.containsKey("tag1"));
        assertTrue(parsedMap.containsKey("tag2"));
        assertEquals(Emoji.fromFormatted("<:emoji1:123456789012345678>"), parsedMap.get("tag1"));
        assertEquals(Emoji.fromUnicode("😂"), parsedMap.get("tag2"));
    }

    @Test
    void testParseEmojiPerTagName_emptyJson() throws IOException {
        String emptyMapJson = "{}";
        JsonParser jp = new JsonFactory().createParser(emptyMapJson);
        jp.nextToken(); // Move to START_OBJECT

        Map<String, Emoji> parsedMap = UserDataParser.parseEmojiPerTagName(jp);
        assertNotNull(parsedMap);
        assertTrue(parsedMap.isEmpty());
    }

    @Test
    void testParseEmojiPerTagName_nonObjectJson() throws IOException {
        String nonObjectJson = "[]"; // An array instead of an object
        JsonParser jp = new JsonFactory().createParser(nonObjectJson);
        jp.nextToken(); // Move to START_ARRAY

        Map<String, Emoji> parsedMap = UserDataParser.parseEmojiPerTagName(jp);
        assertNotNull(parsedMap);
        assertTrue(parsedMap.isEmpty()); // Should return null or handle error
    }

    @Test
    void testParseEmojiPerTagName_invalidEmojiFormat() throws IOException {
        String invalidEmojiJson = "{\"tag\":\"invalid_emoji_format\"}";
        JsonParser jp = new JsonFactory().createParser(invalidEmojiJson);
        jp.nextToken(); // Move to START_OBJECT

        Map<String, Emoji> parsedMap = UserDataParser.parseEmojiPerTagName(jp);
        assertNotNull(parsedMap);
        assertTrue(parsedMap.containsKey("tag"));
        // Depending on JDA's Emoji.fromFormatted, this might be null or throw an exception.
        // As per the current implementation, it seems to create an Emoji object even with malformed input,
        // so we check if it's not null.
        assertNotNull(parsedMap.get("tag"));
    }
}