import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.linked.quizbot.Constants;
import com.linked.quizbot.utils.Option;
import com.linked.quizbot.utils.Question;
import com.linked.quizbot.utils.QuestionList;
import com.linked.quizbot.utils.QuestionListHash;
import com.linked.quizbot.utils.QuestionListParser;

import net.dv8tion.jda.api.entities.emoji.Emoji;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class TestQuestionListParser{
    // Example JSON content provided by the user in the initial prompt
    private static final String SAMPLE_JSON = "{" +
            "\"authorId\":\"468026374557270017\", " +
            "\"name\":\"Agent Welcome aéroportuaire-Aéroportuaire\", " +
            "\"id\":\"a4we1i5\", " +
            "\"timeCreatedMillis\":1748459681435, " +
            "\"tags\":{\"trivia\" : \"📎\"}, " +
            "\"questions\": [" +
            "{" +
            "\"question\":\"Quel est le rôle principal d'un Agent Welcome dans un aéroport?\"," +
            "\"explication\":\"L'Agent Welcome a des responsabilités précises concernant la sécurité et l'accueil des passagers.\"," +
            "\"imageSrc\":null," +
            "\"options\": [" +
            "{" +
            "\"text\":\"Assurer la sécurité des passagers, des équipages et des infrastructures aéroportuaires\"," +
            "\"isCorrect\":true," +
            "\"explication\":\"L'Agent Welcome joue un rôle crucial dans la sécurité des passagers, des équipages, et des infrastructures aéroportuaires tout en assurant l'accueil.\"" +
            "}," +
            "{" +
            "\"text\":\"S'occuper uniquement de l'enregistrement des bagages\"," +
            "\"isCorrect\":false," +
            "\"explication\":\"L'Agent Welcome a un rôle plus large que simplement l'enregistrement des bagages.\"" +
            "}," +
            "{" +
            "\"text\":\"Effectuer le contrôle des passeports\"," +
            "\"isCorrect\":false," +
            "\"explication\":\"Le contrôle des passeports relève généralement de la police aux frontières, pas de l'Agent Welcome.\"" +
            "}," +
            "{" +
            "\"text\":\"Piloter les avions en cas d'urgence\"," +
            "\"isCorrect\":false," +
            "\"explication\":\"L'Agent Welcome n'a aucune fonction liée au pilotage des aéronefs.\"" +
            "}" +
            "]" +
            "}," +
            "{" + // Second question for testing
            "\"question\":\"What is the capital of France?\"," +
            "\"explication\":\"A common geography question.\"," +
            "\"imageSrc\":\"http://example.com/paris.jpg\"," +
            "\"options\": [" +
            "{" +
            "\"text\":\"Paris\"," +
            "\"isCorrect\":true," +
            "\"explication\":\"Paris is the capital of France.\"" +
            "}," +
            "{" +
            "\"text\":\"Berlin\"," +
            "\"isCorrect\":false," +
            "\"explication\":\"Berlin is the capital of Germany.\"" +
            "}" +
            "]" +
            "}" +
            "]" +
            "}";

    @Test
    @DisplayName("Test fromJsonFile with a valid file")
    void testFromJsonFile_ValidFile() throws IOException {
        File tempFile = File.createTempFile("questionList", ".json");
        tempFile.deleteOnExit();

        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write(SAMPLE_JSON);
        }

        QuestionList questionList = QuestionListParser.fromJsonFile(tempFile.getAbsolutePath());

        assertNotNull(questionList);
        assertEquals("468026374557270017", questionList.getAuthorId());
        assertEquals("Agent Welcome aéroportuaire-Aéroportuaire", questionList.getName());
        assertEquals("a4we1i5", questionList.getId());
        assertEquals(1748459681435L, questionList.getTimeCreatedMillis());

        // Test tags
        assertNotNull(questionList.getTags());
        assertEquals(1, questionList.getTags().size());
        assertTrue(questionList.getTags().containsKey("trivia"));
        assertEquals("📎", questionList.getTags().get("trivia").getFormatted());

        // Test questions
        assertEquals(2, questionList.size()); // Inherited from LinkedList<Question>

        // Verify first question
        Question q1 = questionList.get(0);
        assertEquals("Quel est le rôle principal d'un Agent Welcome dans un aéroport?", q1.getQuestion());
        assertEquals("L'Agent Welcome a des responsabilités précises concernant la sécurité et l'accueil des passagers.", q1.getExplication());
        assertNull(q1.getImageSrc());
        assertEquals(4, q1.size()); // Number of options
        assertEquals("Assurer la sécurité des passagers, des équipages et des infrastructures aéroportuaires", q1.get(0).getText());
        assertTrue(q1.get(0).isCorrect());

        // Verify second question
        Question q2 = questionList.get(1);
        assertEquals("What is the capital of France?", q2.getQuestion());
        assertEquals("A common geography question.", q2.getExplication());
        assertEquals("http://example.com/paris.jpg", q2.getImageSrc());
        assertEquals(2, q2.size()); // Number of options
        assertEquals("Paris", q2.get(0).getText());
        assertTrue(q2.get(0).isCorrect());
    }

    @Test
    @DisplayName("Test fromJsonFile with a non-existent file")
    void testFromJsonFile_NonExistentFile() throws IOException {
        String nonExistentPath = "path/to/non_existent_file_qlist.json";
        assertNull(QuestionListParser.fromJsonFile(nonExistentPath)); // Should return null as per current implementation
    }

    @Test
    @DisplayName("Test fromString with a valid JSON string")
    void testFromString_ValidString() throws IOException {
        QuestionList questionList = QuestionListParser.fromString(SAMPLE_JSON);

        assertNotNull(questionList);
        assertEquals("468026374557270017", questionList.getAuthorId());
        assertEquals("Agent Welcome aéroportuaire-Aéroportuaire", questionList.getName());
        assertEquals("a4we1i5", questionList.getId());
        assertEquals(1748459681435L, questionList.getTimeCreatedMillis());

        // Test tags
        assertNotNull(questionList.getTags());
        assertEquals(1, questionList.getTags().size());
        assertTrue(questionList.getTags().containsKey("trivia"));
        assertEquals("📎", questionList.getTags().get("trivia").getFormatted());

        // Test questions
        assertEquals(2, questionList.size());
    }

    @Test
    @DisplayName("Test parser with valid JsonParser input")
    void testParser_ValidInput() throws IOException {
        JsonParser jp = new JsonFactory().createParser(SAMPLE_JSON);
        QuestionList questionList = QuestionListParser.parser(jp);

        assertNotNull(questionList);
        assertEquals("468026374557270017", questionList.getAuthorId());
        assertEquals("Agent Welcome aéroportuaire-Aéroportuaire", questionList.getName());
        assertEquals("a4we1i5", questionList.getId());
        assertEquals(1748459681435L, questionList.getTimeCreatedMillis());
        assertEquals(2, questionList.size());
        jp.close();
    }

    @Test
    @DisplayName("Test parser with malformed JSON (not starting with object)")
    void testParser_MalformedJson() throws IOException {
        String invalidJson = "[\"not a json object\"]";
        JsonParser jp = new JsonFactory().createParser(invalidJson);
        // The parser method itself might not throw an exception immediately for the root token,
        // but it will likely produce an empty or malformed QuestionList.
        // We'll assert specific behaviors if they are designed to handle this.
        // Based on the code, it just prints an error and proceeds.
        QuestionList questionList = QuestionListParser.parser(jp);
        assertNotNull(questionList); // It still returns a new QuestionList instance
        assertNull(questionList.getAuthorId()); // Fields should be null/default
        assertTrue(questionList.isEmpty()); // No questions parsed
        jp.close();
    }

    @Test
    @DisplayName("Test parseTags method with valid tags")
    void testParseTags_ValidTags() throws IOException {
        String jsonWithTags = "{\"tags\":{\"tagA\":\"<:emojiA:123>\",\"tagB\":\"<:emojiB:456>\"}}";
        JsonParser jp = new JsonFactory().createParser(jsonWithTags);

        jp.nextToken(); // START_OBJECT
        jp.nextToken(); // FIELD_NAME "tags"
        jp.nextToken(); // START_OBJECT of tags map

        Map<String, Emoji> tags = QuestionListParser.parseTags(jp);
        assertNotNull(tags);
        assertEquals(2, tags.size());
        assertEquals("<:emojiA:123>", tags.get("tagA").getFormatted());
        assertEquals("<:emojiB:456>", tags.get("tagB").getFormatted());
        jp.close();
    }

    @Test
    @DisplayName("Test parseTags method with empty tags")
    void testParseTags_EmptyTags() throws IOException {
        String jsonWithEmptyTags = "{\"tags\":{}}";
        JsonParser jp = new JsonFactory().createParser(jsonWithEmptyTags);

        jp.nextToken(); // START_OBJECT
        jp.nextToken(); // FIELD_NAME "tags"
        jp.nextToken(); // START_OBJECT of tags map

        Map<String, Emoji> tags = QuestionListParser.parseTags(jp);
        assertNotNull(tags);
        assertTrue(tags.isEmpty());
        jp.close();
    }

    @Test
    @DisplayName("Test parseTags method when tags field is missing or not an object")
    void testParseTags_MissingOrInvalidTagsField() throws IOException {
        // Simulate parser at a non-START_OBJECT token when parseTags is called
        String jsonInvalidTags = "{\"name\":\"test\", \"tags\":\"invalid\"}"; // Tags is a string, not object
        JsonParser jp = new JsonFactory().createParser(jsonInvalidTags);
        jp.nextToken(); // START_OBJECT
        jp.nextToken(); // FIELD_NAME "name"
        jp.nextToken(); // VALUE "test"
        jp.nextToken(); // FIELD_NAME "tags"
        jp.nextToken(); // VALUE "invalid" (This is where parseTags would be called after jp.nextToken())

        // The current implementation of parseTags will return null if currentToken is not START_OBJECT
        Map<String, Emoji> tags = QuestionListParser.parseTags(jp);
        assertNull(tags);
        jp.close();
    }

    @Test
    @DisplayName("Test parseQuestion method with full details")
    void testParseQuestion_FullDetails() throws IOException {
        String jsonQuestion = "{" +
                              "\"question\":\"Sample Q\"," +
                              "\"explication\":\"Sample E\"," +
                              "\"imageSrc\":\"http://img.com/a.png\"," +
                              "\"options\":[" +
                              "{\"text\":\"Opt1\",\"isCorrect\":true,\"explication\":\"Opt1 Expl\"}," +
                              "{\"text\":\"Opt2\",\"isCorrect\":false,\"explication\":\"null\"}" + // expl is "null" string
                              "]" +
                              "}";
        JsonParser jp = new JsonFactory().createParser(jsonQuestion);
        jp.nextToken(); // START_OBJECT

        Question question = QuestionListParser.parseQuestion(jp);
        assertNotNull(question);
        assertEquals("Sample Q", question.getQuestion());
        assertEquals("Sample E", question.getExplication());
        assertEquals("http://img.com/a.png", question.getImageSrc());
        assertEquals(2, question.size());
        assertEquals("Opt1", question.get(0).getText());
        assertTrue(question.get(0).isCorrect());
        assertEquals("Opt1 Expl", question.get(0).getExplication());
        assertEquals("Opt2", question.get(1).getText());
        assertFalse(question.get(1).isCorrect());
        assertNull(question.get(1).getExplication()); // "null" string should be parsed as actual null
        jp.close();
    }

    @Test
    @DisplayName("Test parseQuestion method with missing explication and imageSrc")
    void testParseQuestion_MissingFields() throws IOException {
        String jsonQuestion = "{" +
                              "\"question\":\"Simple Q\"," +
                              "\"options\":[" +
                              "{\"text\":\"Opt1\",\"isCorrect\":true}" +
                              "]" +
                              "}";
        JsonParser jp = new JsonFactory().createParser(jsonQuestion);
        jp.nextToken(); // START_OBJECT

        Question question = QuestionListParser.parseQuestion(jp);
        assertNotNull(question);
        assertEquals("Simple Q", question.getQuestion());
        assertNull(question.getExplication());
        assertNull(question.getImageSrc());
        assertEquals(1, question.size());
        assertEquals("Opt1", question.get(0).getText());
        assertTrue(question.get(0).isCorrect());
        assertNull(question.get(0).getExplication());
        jp.close();
    }

    @Test
    @DisplayName("Test parseOptionList method")
    void testParseOptionList() throws IOException {
        String jsonOptionsArray = "[" +
                                  "{\"text\":\"A\",\"isCorrect\":true,\"explication\":\"ExpA\"}," +
                                  "{\"text\":\"B\",\"isCorrect\":false}" +
                                  "]";
        JsonParser jp = new JsonFactory().createParser(jsonOptionsArray);
        jp.nextToken(); // START_ARRAY

        List<Option> options = QuestionListParser.parseOptionList(jp);
        assertNotNull(options);
        assertEquals(2, options.size());
        assertEquals("A", options.get(0).getText());
        assertTrue(options.get(0).isCorrect());
        assertEquals("ExpA", options.get(0).getExplication());
        assertEquals("B", options.get(1).getText());
        assertFalse(options.get(1).isCorrect());
        assertNull(options.get(1).getExplication());
        jp.close();
    }

    @Test
    @DisplayName("Test parseOption method")
    void testParseOption() throws IOException {
        String jsonOption = "{\"text\":\"The Answer\",\"isCorrect\":true,\"explication\":\"This is the right one\"}";
        JsonParser jp = new JsonFactory().createParser(jsonOption);
        jp.nextToken(); // START_OBJECT

        Option option = QuestionListParser.parseOption(jp);
        assertNotNull(option);
        assertEquals("The Answer", option.getText());
        assertTrue(option.isCorrect());
        assertEquals("This is the right one", option.getExplication());
        jp.close();
    }

    @Test
    @DisplayName("Test parseOption method with missing fields")
    void testParseOption_MissingFields() throws IOException {
        String jsonOption = "{\"text\":\"Only Text\"}"; // isCorrect and explication missing
        JsonParser jp = new JsonFactory().createParser(jsonOption);
        jp.nextToken(); // START_OBJECT

        Option option = QuestionListParser.parseOption(jp);
        assertNull(option);
        jp.close();
    }

    @Test
    @DisplayName("Test parseOption method with 'null' string for explication")
    void testParseOption_NullStringExplication() throws IOException {
        String jsonOption = "{\"text\":\"Test\",\"isCorrect\":true,\"explication\":\"null\"}";
        JsonParser jp = new JsonFactory().createParser(jsonOption);
        jp.nextToken(); // START_OBJECT

        Option option = QuestionListParser.parseOption(jp);
        assertNotNull(option);
        assertNull(option.getExplication());
        jp.close();
    }

    @Test
    @DisplayName("Test parseQuestionList method")
    void testParseQuestionList() throws IOException {
        String jsonQuestionsArray = "[" +
                                    "{\"question\":\"Q1\",\"options\":[{\"text\":\"A\",\"isCorrect\":true}]}," +
                                    "{\"question\":\"Q2\",\"options\":[{\"text\":\"B\",\"isCorrect\":false}]}" +
                                    "]";
        JsonParser jp = new JsonFactory().createParser(jsonQuestionsArray);
        jp.nextToken(); // START_ARRAY

        List<Question> questions = QuestionListParser.parseQuestionList(jp);
        assertNotNull(questions);
        assertEquals(2, questions.size());
        assertEquals("Q1", questions.get(0).getQuestion());
        assertEquals("Q2", questions.get(1).getQuestion());
        jp.close();
    }

    @Test
    @DisplayName("Test parsing a QuestionList with an empty 'questions' array")
    void testParseEmptyQuestionsArray() throws IOException {
        String jsonContent = "{" +
                             "\"authorId\":\"user123\", " +
                             "\"name\":\"Empty Quiz\"," +
                             "\"id\":\"empty123\"," +
                             "\"timeCreatedMillis\":123456789," +
                             "\"tags\":{}," +
                             "\"questions\": []" +
                             "}";
        QuestionList questionList = QuestionListParser.fromString(jsonContent);

        assertNotNull(questionList);
        assertEquals("user123", questionList.getAuthorId());
        assertEquals("Empty Quiz", questionList.getName());
        assertTrue(questionList.isEmpty());
    }

    @Test
    @DisplayName("Test parsing QuestionList with missing 'id' (should generate one)")
    void testParseMissingListId() throws IOException {
        // Remove id from the sample JSON
        String jsonContent = "{" +
                             "\"authorId\":\"authorMissingId\", " +
                             "\"name\":\"Quiz with no id\", " +
                             "\"timeCreatedMillis\":1000000000000," +
                             "\"tags\":{}," +
                             "\"questions\": []" +
                             "}";
        QuestionList questionList = QuestionListParser.fromString(jsonContent);

        assertNotNull(questionList);
        assertNotNull(questionList.getId());
        assertTrue(questionList.getId().length()== QuestionListHash.DEFAULT_LENGTH);
    }

    @Test
    @DisplayName("Test parsing QuestionList with missing 'timeCreatedMillis' (should default to current time)")
    void testParseMissingTimeCreatedMillis() throws IOException {
        // Remove timeCreatedMillis from the sample JSON
        String jsonContent = "{" +
                             "\"authorId\":\"authorMissingTime\", " +
                             "\"name\":\"Quiz with no time\"," +
                             "\"id\":\"fixedid\"," +
                             "\"tags\":{}," +
                             "\"questions\": []" +
                             "}";
        long beforeParse = System.currentTimeMillis();
        QuestionList questionList = QuestionListParser.fromString(jsonContent);
        long afterParse = System.currentTimeMillis();

        assertNotNull(questionList);
        assertTrue(questionList.getTimeCreatedMillis() >= beforeParse);
        assertTrue(questionList.getTimeCreatedMillis() <= afterParse);
    }
}