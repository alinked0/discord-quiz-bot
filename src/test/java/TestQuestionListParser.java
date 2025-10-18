import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.linked.quizbot.Constants;
import com.linked.quizbot.utils.Option;
import com.linked.quizbot.utils.Question;
import com.linked.quizbot.utils.QuestionList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class TestQuestionListParser {

	private static String SAMPLE_JSON ;
			
	@BeforeEach
	void setUp() throws IOException {
		QuestionList.Hasher.clearGeneratedCodes();
		SAMPLE_JSON = "{" +
			Constants.MAPPER.writeValueAsString("authorId")+":"+Constants.MAPPER.writeValueAsString("468026374557270017")+", " +
			Constants.MAPPER.writeValueAsString("name")+":"+Constants.MAPPER.writeValueAsString("Agent Welcome aéroportuaire-Aéroportuaire")+", " +
			Constants.MAPPER.writeValueAsString("id")+":"+Constants.MAPPER.writeValueAsString("a4we1i5")+", " +
			Constants.MAPPER.writeValueAsString("timeCreatedMillis")+":1748459681435, " +
			Constants.MAPPER.writeValueAsString("emojiPerTagName")+":{"+Constants.MAPPER.writeValueAsString("trivia")+" : "+Constants.MAPPER.writeValueAsString("📎")+"}, " +
			Constants.MAPPER.writeValueAsString("questions")+": [" +
			"{" +
			Constants.MAPPER.writeValueAsString("question")+":"+Constants.MAPPER.writeValueAsString("Quel est le rôle principal d'un Agent Welcome dans un aéroport?")+"," +
			Constants.MAPPER.writeValueAsString("explication")+":"+Constants.MAPPER.writeValueAsString("L'Agent Welcome a des responsabilités précises concernant la sécurité et l'accueil des passagers.")+"," +
			Constants.MAPPER.writeValueAsString("imageSrc")+":null," +
			Constants.MAPPER.writeValueAsString("options")+": [" +
			"{" +
			Constants.MAPPER.writeValueAsString("text")+":"+Constants.MAPPER.writeValueAsString("Assurer la sécurité des passagers, des équipages et des infrastructures aéroportuaires")+"," +
			Constants.MAPPER.writeValueAsString("isCorrect")+":true," +
			Constants.MAPPER.writeValueAsString("explication")+":"+Constants.MAPPER.writeValueAsString("L'Agent Welcome joue un rôle crucial dans la sécurité des passagers, des équipages, et des infrastructures aéroportuaires tout en assurant l'accueil.")+ 
			"}," +
			"{" +
			Constants.MAPPER.writeValueAsString("text")+":"+Constants.MAPPER.writeValueAsString("S'occuper uniquement de l'enregistrement des bagages")+"," +
			Constants.MAPPER.writeValueAsString("isCorrect")+":false," +
			Constants.MAPPER.writeValueAsString("explication")+":"+Constants.MAPPER.writeValueAsString("L'Agent Welcome a un rôle plus large que simplement l'enregistrement des bagages.")+ 
			"}," +
			"{" +
			Constants.MAPPER.writeValueAsString("text")+":"+Constants.MAPPER.writeValueAsString("Effectuer le contrôle des passeports")+"," +
			Constants.MAPPER.writeValueAsString("isCorrect")+":false," +
			Constants.MAPPER.writeValueAsString("explication")+":"+Constants.MAPPER.writeValueAsString("Le contrôle des passeports relève généralement de la police aux frontières, pas de l'Agent Welcome.")+ 
			"}," +
			"{" +
			Constants.MAPPER.writeValueAsString("text")+":"+Constants.MAPPER.writeValueAsString("Piloter les avions en cas d'urgence")+"," +
			Constants.MAPPER.writeValueAsString("isCorrect")+":false," +
			Constants.MAPPER.writeValueAsString("explication")+":"+Constants.MAPPER.writeValueAsString("L'Agent Welcome n'a aucune fonction liée au pilotage des aéronefs.")+ 
			"}" +
			"]" +
			"}," +
			"{" + // Second question for testing
			""+Constants.MAPPER.writeValueAsString("question")+":"+Constants.MAPPER.writeValueAsString("What is the capital of France?")+"," +
			Constants.MAPPER.writeValueAsString("explication")+":"+Constants.MAPPER.writeValueAsString("A common geography question.")+"," +
			Constants.MAPPER.writeValueAsString("imageSrc")+":"+Constants.MAPPER.writeValueAsString("http://example.com/paris.jpg")+"," +
			Constants.MAPPER.writeValueAsString("options")+": [" +
			"{" +
			Constants.MAPPER.writeValueAsString("text")+":"+Constants.MAPPER.writeValueAsString("Paris")+"," +
			Constants.MAPPER.writeValueAsString("isCorrect")+":true," +
			Constants.MAPPER.writeValueAsString("explication")+":"+Constants.MAPPER.writeValueAsString("Paris is the capital of France.")+ 
			"}," +
			"{" +
			Constants.MAPPER.writeValueAsString("text")+":"+Constants.MAPPER.writeValueAsString("Berlin")+"," +
			Constants.MAPPER.writeValueAsString("isCorrect")+":false," +
			Constants.MAPPER.writeValueAsString("explication")+":"+Constants.MAPPER.writeValueAsString("Berlin is the capital of Germany.")+ 
			"}" +
			"]" +
			"}" +
			"]" +
			"}";
	}
	@AfterEach
	void tearDown() throws IOException {
		// Ensure static fields are clear() for good measure, though @BeforeEach handles it too.
		QuestionList.Hasher.clearGeneratedCodes();
	}
	// --- Start of the new test case (from previous response) ---
	@Test
	@DisplayName("Test fromString with questions having missing/null optional fields")
	void testFromString_QuestionsWithMissingAndNullFields() throws IOException {
		String jsonContent = "{" +
				Constants.MAPPER.writeValueAsString("authorId")+":"+Constants.MAPPER.writeValueAsString("user999")+", " +
				Constants.MAPPER.writeValueAsString("name")+":"+Constants.MAPPER.writeValueAsString("Quiz with partial questions")+"," +
				Constants.MAPPER.writeValueAsString("id")+":"+Constants.MAPPER.writeValueAsString("partialq")+"," +
				Constants.MAPPER.writeValueAsString("timeCreatedMillis")+":1678888888888," +
				Constants.MAPPER.writeValueAsString("emojiPerTagName")+":{}," +
				Constants.MAPPER.writeValueAsString("questions")+": [" +
				"{" +
				Constants.MAPPER.writeValueAsString("question")+":"+Constants.MAPPER.writeValueAsString("First question with only required fields?")+"," +
				Constants.MAPPER.writeValueAsString("options")+": [" +
				"{"+Constants.MAPPER.writeValueAsString("text")+":"+Constants.MAPPER.writeValueAsString("Option A")+","+Constants.MAPPER.writeValueAsString("isCorrect")+":true}" +
				"]" +
				"}," +
				"{" +
				Constants.MAPPER.writeValueAsString("question")+":"+Constants.MAPPER.writeValueAsString("Second question with null explanation and imageSrc as 'null' string?")+"," +
				Constants.MAPPER.writeValueAsString("explication")+":null," + // Explicit null
				""+Constants.MAPPER.writeValueAsString("imageSrc")+":"+Constants.MAPPER.writeValueAsString("null")+"," + // "null" as string
				""+Constants.MAPPER.writeValueAsString("options")+": [" +
				"{"+Constants.MAPPER.writeValueAsString("text")+":"+Constants.MAPPER.writeValueAsString("Option B")+","+Constants.MAPPER.writeValueAsString("isCorrect")+":false}," +
				"{"+Constants.MAPPER.writeValueAsString("text")+":"+Constants.MAPPER.writeValueAsString("Option C")+","+Constants.MAPPER.writeValueAsString("isCorrect")+":true,"+Constants.MAPPER.writeValueAsString("explication")+":"+Constants.MAPPER.writeValueAsString("This is correct")+"}" +
				"]" +
				"}," +
				"{" +
				Constants.MAPPER.writeValueAsString("question")+":"+Constants.MAPPER.writeValueAsString("Third question with missing explanation and explicit null imageSrc?")+"," +
				// "explication" field is entirely missing
				""+Constants.MAPPER.writeValueAsString("imageSrc")+":null," +
				Constants.MAPPER.writeValueAsString("options")+": [" +
				"{"+Constants.MAPPER.writeValueAsString("text")+":"+Constants.MAPPER.writeValueAsString("Option D")+","+Constants.MAPPER.writeValueAsString("isCorrect")+":true}" +
				"]" +
				"}" +
				"]" +
				"}";

		QuestionList questionList = QuestionList.Parser.fromString(jsonContent).build();

		assertNotNull(questionList, "QuestionList should not be null.");
		assertEquals("user999", questionList.getAuthorId());
		assertEquals("Quiz with partial questions", questionList.getName());
		assertEquals(3, questionList.size(), "Should have 3 questions.");

		// Verify first question (missing explication, imageSrc, and option explication)
		Question q1 = questionList.get(0);
		assertEquals("First question with only required fields?", q1.getQuestion());
		assertEquals( Constants.NOEXPLICATION,q1.getExplication(), "Q1 explication should be null (missing field).");
		assertNull(q1.getImageSrc(), "Q1 imageSrc should be null (missing field).");
		assertEquals(1, q1.size());
		assertEquals("Option A", q1.get(0).getText());
		assertTrue(q1.get(0).isCorrect());
		assertEquals( Constants.NOEXPLICATION,q1.get(0).getExplication(), "Q1 Option explication should be null (missing field).");

		// Verify second question (explicit null explication, "null" string imageSrc, and option with explication)
		Question q2 = questionList.get(1);
		assertEquals("Second question with null explanation and imageSrc as 'null' string?", q2.getQuestion());
		assertEquals( Constants.NOEXPLICATION,q2.getExplication(), "Q2 explication should be null (explicit null).");
		assertNull(q2.getImageSrc(), "Q2 imageSrc should be null ('null' string).");
		assertEquals(2, q2.size());
		assertEquals("Option B", q2.get(0).getText());
		assertFalse(q2.get(0).isCorrect());
		assertEquals( Constants.NOEXPLICATION,q2.get(0).getExplication(), "Q2 Option B explication should be null (missing field).");
		assertEquals("Option C", q2.get(1).getText());
		assertTrue(q2.get(1).isCorrect());
		assertEquals("This is correct", q2.get(1).getExplication());

		// Verify third question (missing explication, explicit null imageSrc)
		Question q3 = questionList.get(2);
		assertEquals("Third question with missing explanation and explicit null imageSrc?", q3.getQuestion());
		assertEquals( Constants.NOEXPLICATION,q3.getExplication(), "Q3 explication should be null (missing field).");
		assertNull(q3.getImageSrc(), "Q3 imageSrc should be null (explicit null).");
		assertEquals(1, q3.size());
		assertEquals("Option D", q3.get(0).getText());
		assertTrue(q3.get(0).isCorrect());
	}
	// --- End of the new test case ---

	@Test
	@DisplayName("Test fromJsonFile with a valid file")
	void testFromJsonFile_ValidFile() throws IOException {
		File tempFile = File.createTempFile("questionList", ".json");
		tempFile.deleteOnExit();

		try (FileWriter writer = new FileWriter(tempFile)) {
			writer.write(SAMPLE_JSON);
		}

		QuestionList questionList = QuestionList.Parser.fromJsonFile(tempFile.getAbsolutePath()).build();

		assertNotNull(questionList);
		assertEquals("468026374557270017", questionList.getAuthorId());
		assertEquals("Agent Welcome aéroportuaire-Aéroportuaire", questionList.getName());
		assertEquals("a4we1i5", questionList.getId());
		assertEquals(1748459681435L, questionList.getTimeCreatedMillis());

		// Test tags
		assertNotNull(questionList.getEmojiPerTagName());
		assertEquals(1, questionList.getEmojiPerTagName().size());
		assertTrue(questionList.getEmojiPerTagName().containsKey("trivia"));
		assertEquals("📎", questionList.getEmojiPerTagName().get("trivia"));

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
		assertNull(QuestionList.Parser.fromJsonFile(nonExistentPath)); // Should return null as per current implementation
	}

	@Test
	@DisplayName("Test fromString with a valid JSON string")
	void testFromString_ValidString() throws IOException {
		QuestionList questionList = QuestionList.Parser.fromString(SAMPLE_JSON).build();

		assertNotNull(questionList);
		assertEquals("468026374557270017", questionList.getAuthorId());
		assertEquals("Agent Welcome aéroportuaire-Aéroportuaire", questionList.getName());
		assertEquals("a4we1i5", questionList.getId());
		assertEquals(1748459681435L, questionList.getTimeCreatedMillis());

		// Test tags
		assertNotNull(questionList.getEmojiPerTagName());
		assertEquals(1, questionList.getTagNames().size());
		assertEquals(1, questionList.getEmojiPerTagName().size());
		assertTrue(questionList.getEmojiPerTagName().containsKey("trivia"));
		assertEquals("📎", questionList.getEmojiPerTagName().get("trivia"));

		// Test questions
		assertEquals(2, questionList.size());
	}

	@Test
	@DisplayName("Test parser with valid JsonParser input")
	void testParser_ValidInput() throws IOException {
		JsonParser jp = new JsonFactory().createParser(SAMPLE_JSON);
		QuestionList questionList = QuestionList.Parser.parser(jp, SAMPLE_JSON).build();

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
	void testParser_MalformedJson() throws JsonProcessingException{ // No IOException in signature because it's caught
		String invalidJson = "["+Constants.MAPPER.writeValueAsString("not a json object")+"]";
		JsonParser jp = null;
		try {
			JsonParser jps = new JsonFactory().createParser(invalidJson);
			// Assert that IOException is thrown for root token mismatch
			IOException thrown = assertThrows(IOException.class, () -> {
				QuestionList.Parser.parser(jps, invalidJson);
			}, "Expected IOException for JSON not starting with an object.");

			assertTrue(thrown.getMessage().contains("Error QuestionList.Parser.parser, input is not a json:"));
			assertTrue(thrown.getMessage().contains(invalidJson));
			jp = jps;
		} catch (IOException e) {
			// This catch block handles potential IOException from createParser
			fail("Failed to create JsonParser for malformed JSON: " + e.getMessage());
		} finally {
			if (jp != null) {
				try {
					jp.close();
				} catch (IOException e) {
					System.err.println(Constants.ERROR + "Error closing JsonParser: " + e.getMessage());
				}
			}
		}
	}


	@Test
	@DisplayName("Test parseEmojiPerTagName method with valid tags")
	void testParseTags_ValidTags() throws IOException {
		String jsonWithTags = "{"+Constants.MAPPER.writeValueAsString("emojiPerTagName")+":{"+Constants.MAPPER.writeValueAsString("tagA")+":"+Constants.MAPPER.writeValueAsString("<:emojiA:123>")+","+Constants.MAPPER.writeValueAsString("tagB")+":"+Constants.MAPPER.writeValueAsString("<:emojiB:456>")+"}}";
		JsonParser jp = new JsonFactory().createParser(jsonWithTags);

		jp.nextToken(); // START_OBJECT
		jp.nextToken(); // FIELD_NAME "emojiPerTagName"
		jp.nextToken(); // START_OBJECT of tags map (This is the token parseEmojiPerTagName expects)

		Map<String, String> tags = QuestionList.Parser.parseEmojiPerTagName(jp, jsonWithTags);
		assertNotNull(tags);
		assertEquals(2, tags.size());
		assertEquals("<:emojiA:123>", tags.get("tagA"));
		assertEquals("<:emojiB:456>", tags.get("tagB"));
		jp.close();
	}

	@Test
	@DisplayName("Test parseEmojiPerTagName method with empty tags")
	void testParseTags_EmptyTags() throws IOException {
		String jsonWithEmptyTags = "{"+Constants.MAPPER.writeValueAsString("emojiPerTagName")+":{}}";
		JsonParser jp = new JsonFactory().createParser(jsonWithEmptyTags);

		jp.nextToken(); // START_OBJECT
		jp.nextToken(); // FIELD_NAME "emojiPerTagName"
		jp.nextToken(); // START_OBJECT of tags map (This is the token parseEmojiPerTagName expects)

		Map<String, String> tags = QuestionList.Parser.parseEmojiPerTagName(jp, jsonWithEmptyTags);
		assertNotNull(tags);
		assertTrue(tags.isEmpty());
		jp.close();
	}

	@Test
	@DisplayName("Test parseEmojiPerTagName method when tags field is not an object (should throw IOException)")
	void testParseTags_NotAnObject_ThrowsIOException() throws IOException {
		String jsonInvalidTags = "{"+Constants.MAPPER.writeValueAsString("name")+":"+Constants.MAPPER.writeValueAsString("test")+", "+Constants.MAPPER.writeValueAsString("emojiPerTagName")+":"+Constants.MAPPER.writeValueAsString("invalid")+"}"; // Tags is a string, not object
		JsonParser jp = new JsonFactory().createParser(jsonInvalidTags);

		jp.nextToken(); // START_OBJECT
		jp.nextToken(); // FIELD_NAME "name"
		jp.nextToken(); // VALUE "test"
		jp.nextToken(); // FIELD_NAME "emojiPerTagName"
		jp.nextToken(); // VALUE_STRING "invalid" (This is the token parseEmojiPerTagName will see)

		IOException thrown = assertThrows(IOException.class, () -> {
			QuestionList.Parser.parseEmojiPerTagName(jp, jsonInvalidTags);
		}, "Expected IOException to be thrown when 'tags' field is not a START_OBJECT.");

		assertTrue(thrown.getMessage().contains("Error QuestionList.Parser.parseEmojiPerTagName, input is not a json:"));
		assertTrue(thrown.getMessage().contains(jsonInvalidTags));

		jp.close();
	}

	// Original test adapted to handle the main parser logic, where tags might be genuinely missing or null.
	// In such cases, parseEmojiPerTagName might not be called, or if called by a higher-level loop, it might lead to
	// an empty map, not necessarily an exception from parseEmojiPerTagName itself.
	@Test
	@DisplayName("Test QuestionList.Parser.parser when tags field is missing (should default to empty map)")
	void testQuestionListParser_MissingTagsField() throws IOException {
		String jsonWithoutTags = "{" +
				Constants.MAPPER.writeValueAsString("authorId")+":"+Constants.MAPPER.writeValueAsString("userNoTags")+", " +
				Constants.MAPPER.writeValueAsString("name")+":"+Constants.MAPPER.writeValueAsString("Quiz without tags")+"," +
				Constants.MAPPER.writeValueAsString("id")+":"+Constants.MAPPER.writeValueAsString("notags1")+"," +
				Constants.MAPPER.writeValueAsString("timeCreatedMillis")+":123456789," +
				Constants.MAPPER.writeValueAsString("questions")+": []" +
				"}";

		QuestionList questionList = QuestionList.Parser.fromString(jsonWithoutTags).build();
		assertNotNull(questionList);
		assertNotNull(questionList.getEmojiPerTagName());
		assertTrue(questionList.getEmojiPerTagName().isEmpty(), "Tags map should be empty if 'tags' field is missing.");
	}

	@Test
	@DisplayName("Test parseQuestion method with full details")
	void testParseQuestion_FullDetails() throws IOException {
		String jsonQuestion = "{" +
							  Constants.MAPPER.writeValueAsString("question")+":"+Constants.MAPPER.writeValueAsString("Sample Q")+"," +
							  Constants.MAPPER.writeValueAsString("explication")+":"+Constants.MAPPER.writeValueAsString("Sample E")+"," +
							  Constants.MAPPER.writeValueAsString("imageSrc")+":"+Constants.MAPPER.writeValueAsString("http://img.com/a.png")+"," +
							  Constants.MAPPER.writeValueAsString("options")+":[" +
							  "{"+Constants.MAPPER.writeValueAsString("text")+":"+Constants.MAPPER.writeValueAsString("Opt1")+","+Constants.MAPPER.writeValueAsString("isCorrect")+":true,"+Constants.MAPPER.writeValueAsString("explication")+":"+Constants.MAPPER.writeValueAsString("Opt1 Expl")+"}," +
							  "{"+Constants.MAPPER.writeValueAsString("text")+":"+Constants.MAPPER.writeValueAsString("Opt2")+","+Constants.MAPPER.writeValueAsString("isCorrect")+":false,"+Constants.MAPPER.writeValueAsString("explication")+":"+Constants.MAPPER.writeValueAsString("null")+"}" + // expl is "null" string
							  "]" +
							  "}";
		JsonParser jp = new JsonFactory().createParser(jsonQuestion);
		jp.nextToken(); // START_OBJECT (This is the token parseQuestion expects)

		Question question = QuestionList.Parser.parseQuestion(jp, jsonQuestion);
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
		assertEquals( Constants.NOEXPLICATION,question.get(1).getExplication()); // "null" string should be parsed as actual null
		jp.close();
	}

	@Test
	@DisplayName("Test parseQuestion method when input is not a START_OBJECT (should throw IOException)")
	void testParseQuestion_NotAnObject_ThrowsIOException() throws IOException {
		String invalidJson = "["+Constants.MAPPER.writeValueAsString("not a question object")+"]"; // An array, not an object
		JsonParser jp = new JsonFactory().createParser(invalidJson);
		jp.nextToken(); // Moves to START_ARRAY (This is the token parseQuestion will see)

		IOException thrown = assertThrows(IOException.class, () -> {
			QuestionList.Parser.parseQuestion(jp, invalidJson);
		}, "Expected IOException when input for parseQuestion is not START_OBJECT.");

		assertTrue(thrown.getMessage().contains(Constants.ERROR + "QuestionList.Parser.parseEmojiPerTagName")); // Note: The error message mistakenly says parseEmojiPerTagName
		assertTrue(thrown.getMessage().contains(invalidJson));

		jp.close();
	}

	@Test
	@DisplayName("Test parseQuestion method with missing explication and imageSrc")
	void testParseQuestion_MissingFields() throws IOException {
		String jsonQuestion = "{" +
							  Constants.MAPPER.writeValueAsString("question")+":"+Constants.MAPPER.writeValueAsString("Simple Q")+"," +
							  Constants.MAPPER.writeValueAsString("options")+":[" +
							  "{"+Constants.MAPPER.writeValueAsString("text")+":"+Constants.MAPPER.writeValueAsString("Opt1")+","+Constants.MAPPER.writeValueAsString("isCorrect")+":true}" +
							  "]" +
							  "}";
		JsonParser jp = new JsonFactory().createParser(jsonQuestion);
		jp.nextToken(); // START_OBJECT

		Question question = QuestionList.Parser.parseQuestion(jp, jsonQuestion);
		assertNotNull(question);
		assertEquals("Simple Q", question.getQuestion());
		assertEquals( Constants.NOEXPLICATION,question.getExplication());
		assertNull(question.getImageSrc());
		assertEquals(1, question.size());
		assertEquals("Opt1", question.get(0).getText());
		assertTrue(question.get(0).isCorrect());
		assertEquals( Constants.NOEXPLICATION,question.get(0).getExplication());
		jp.close();
	}

	@Test
	@DisplayName("Test parseOptionList method")
	void testParseOptionList() throws IOException {
		String jsonOptionsArray = "[" +
								  "{"+Constants.MAPPER.writeValueAsString("text")+":"+Constants.MAPPER.writeValueAsString("A")+","+Constants.MAPPER.writeValueAsString("isCorrect")+":true,"+Constants.MAPPER.writeValueAsString("explication")+":"+Constants.MAPPER.writeValueAsString("ExpA")+"}," +
								  "{"+Constants.MAPPER.writeValueAsString("text")+":"+Constants.MAPPER.writeValueAsString("B")+","+Constants.MAPPER.writeValueAsString("isCorrect")+":false}" +
								  "]";
		JsonParser jp = new JsonFactory().createParser(jsonOptionsArray);
		jp.nextToken(); // START_ARRAY (This is the token parseOptionList expects)

		List<Option> options = QuestionList.Parser.parseOptionList(jp, jsonOptionsArray);
		assertNotNull(options);
		assertEquals(2, options.size());
		assertEquals("A", options.get(0).getText());
		assertTrue(options.get(0).isCorrect());
		assertEquals("ExpA", options.get(0).getExplication());
		assertEquals("B", options.get(1).getText());
		assertFalse(options.get(1).isCorrect());
		assertEquals( Constants.NOEXPLICATION,options.get(1).getExplication());
		jp.close();
	}

	@Test
	@DisplayName("Test parseOptionList method when input is not a START_ARRAY (should throw IOException)")
	void testParseOptionList_NotAnArray_ThrowsIOException() throws IOException {
		String invalidJson = "{"+Constants.MAPPER.writeValueAsString("not_an_array")+":"+Constants.MAPPER.writeValueAsString("true")+"}"; // An object, not an array
		JsonParser jp = new JsonFactory().createParser(invalidJson);
		jp.nextToken(); // Moves to START_OBJECT (This is the token parseOptionList will see)

		IOException thrown = assertThrows(IOException.class, () -> {
			QuestionList.Parser.parseOptionList(jp, invalidJson);
		}, "Expected IOException when input for parseOptionList is not START_ARRAY.");

		assertTrue(thrown.getMessage().contains("Error QuestionList.Parser.parseOptionList, input is not a json:"));
		assertTrue(thrown.getMessage().contains(invalidJson));

		jp.close();
	}

	@Test
	@DisplayName("Test parseOption method")
	void testParseOption() throws IOException {
		String jsonOption = "{"+Constants.MAPPER.writeValueAsString("text")+":"+Constants.MAPPER.writeValueAsString("The Answer")+","+Constants.MAPPER.writeValueAsString("isCorrect")+":true,"+Constants.MAPPER.writeValueAsString("explication")+":"+Constants.MAPPER.writeValueAsString("This is the right one")+"}";
		JsonParser jp = new JsonFactory().createParser(jsonOption);
		jp.nextToken(); // START_OBJECT (This is the token parseOption expects)

		Option option = QuestionList.Parser.parseOption(jp, jsonOption);
		assertNotNull(option);
		assertEquals("The Answer", option.getText());
		assertTrue(option.isCorrect());
		assertEquals("This is the right one", option.getExplication());
		jp.close();
	}

	@Test
	@DisplayName("Test parseOption method when input is not a START_OBJECT (should throw IOException)")
	void testParseOption_NotAnObject_ThrowsIOException() throws IOException {
		String invalidJson = "["+Constants.MAPPER.writeValueAsString("not an option object")+"]"; // An array, not an object
		JsonParser jp = new JsonFactory().createParser(invalidJson);
		jp.nextToken(); // Moves to START_ARRAY (This is the token parseOption will see)

		IOException thrown = assertThrows(IOException.class, () -> {
			QuestionList.Parser.parseOption(jp, invalidJson);
		}, "Expected IOException when input for parseOption is not START_OBJECT.");

		assertTrue(thrown.getMessage().contains("Error QuestionList.Parser.parseOption, input is not a json:"));
		assertTrue(thrown.getMessage().contains(invalidJson));

		jp.close();
	}

	@Test
	@DisplayName("Test parseOption method with missing fields (should return null as per current logic)")
	void testParseOption_MissingFields_ReturnsNull() throws IOException {
		String jsonOption = "{"+Constants.MAPPER.writeValueAsString("text")+":"+Constants.MAPPER.writeValueAsString("Only Text")+"}"; // isCorrect is missing, which is a required field for `Option`
		JsonParser jp = new JsonFactory().createParser(jsonOption);
		jp.nextToken(); // START_OBJECT

		// The current implementation returns null if optTxt or isCorr is null
		Option option = QuestionList.Parser.parseOption(jp, jsonOption);
		assertNull(option, "Option should be null if 'isCorrect' field is missing.");
		jp.close();
	}

	@Test
	@DisplayName("Test parseOption method with 'null' string for explication")
	void testParseOption_NullStringExplication() throws IOException {
		String jsonOption = "{"+Constants.MAPPER.writeValueAsString("text")+":"+Constants.MAPPER.writeValueAsString("Test")+","+Constants.MAPPER.writeValueAsString("isCorrect")+":true,"+Constants.MAPPER.writeValueAsString("explication")+":"+Constants.MAPPER.writeValueAsString("null")+"}";
		JsonParser jp = new JsonFactory().createParser(jsonOption);
		jp.nextToken(); // START_OBJECT

		Option option = QuestionList.Parser.parseOption(jp, jsonOption);
		assertNotNull(option);
		assertEquals( Constants.NOEXPLICATION,option.getExplication()); // "null" string should be parsed as actual null
		jp.close();
	}

	@Test
	@DisplayName("Test parseQuestionList method")
	void testParseQuestionList() throws IOException {
		String jsonQuestionsArray = "[" +
									"{"+Constants.MAPPER.writeValueAsString("question")+":"+Constants.MAPPER.writeValueAsString("Q1")+","+Constants.MAPPER.writeValueAsString("options")+":[{"+Constants.MAPPER.writeValueAsString("text")+":"+Constants.MAPPER.writeValueAsString("A")+","+Constants.MAPPER.writeValueAsString("isCorrect")+":true}]}," +
									"{"+Constants.MAPPER.writeValueAsString("question")+":"+Constants.MAPPER.writeValueAsString("Q2")+","+Constants.MAPPER.writeValueAsString("options")+":[{"+Constants.MAPPER.writeValueAsString("text")+":"+Constants.MAPPER.writeValueAsString("B")+","+Constants.MAPPER.writeValueAsString("isCorrect")+":false}]}" +
									"]";
		JsonParser jp = new JsonFactory().createParser(jsonQuestionsArray);
		jp.nextToken(); // START_ARRAY (This is the token parseQuestionList expects)

		List<Question> questions = QuestionList.Parser.parseQuestionList(jp, jsonQuestionsArray);
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
							 Constants.MAPPER.writeValueAsString("authorId")+":"+Constants.MAPPER.writeValueAsString("user123")+", " +
							 Constants.MAPPER.writeValueAsString("name")+":"+Constants.MAPPER.writeValueAsString("Empty Quiz")+"," +
							 Constants.MAPPER.writeValueAsString("id")+":"+Constants.MAPPER.writeValueAsString("empty123")+"," +
							 Constants.MAPPER.writeValueAsString("timeCreatedMillis")+":123456789," + // Added 'L' for long literal
							 ""+Constants.MAPPER.writeValueAsString("emojiPerTagName")+":{}," +
							 Constants.MAPPER.writeValueAsString("questions")+": []" +
							 "}";
		QuestionList questionList = QuestionList.Parser.fromString(jsonContent).build();

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
							 Constants.MAPPER.writeValueAsString("authorId")+":"+Constants.MAPPER.writeValueAsString("authorMissingId")+", " +
							 Constants.MAPPER.writeValueAsString("name")+":"+Constants.MAPPER.writeValueAsString("Quiz with no id")+", " +
							 Constants.MAPPER.writeValueAsString("timeCreatedMillis")+":1000000000000," + // Added 'L' for long literal
							 ""+Constants.MAPPER.writeValueAsString("emojiPerTagName")+":{}," +
							 Constants.MAPPER.writeValueAsString("questions")+": []" +
							 "}";
		QuestionList questionList = QuestionList.Parser.fromString(jsonContent).build();

		assertNotNull(questionList);
		assertNotNull(questionList.getId());
		assertTrue(questionList.getId().length()== QuestionList.Hasher.DEFAULT_LENGTH);
	}

	@Test
	@DisplayName("Test parsing QuestionList with missing 'timeCreatedMillis' (should default to current time)")
	void testParseMissingTimeCreatedMillis() throws IOException {
		// Remove timeCreatedMillis from the sample JSON
		String jsonContent = "{" +
							 Constants.MAPPER.writeValueAsString("authorId")+":"+Constants.MAPPER.writeValueAsString("authorMissingTime")+", " +
							 Constants.MAPPER.writeValueAsString("name")+":"+Constants.MAPPER.writeValueAsString("Quiz with no time")+"," +
							 Constants.MAPPER.writeValueAsString("id")+":"+Constants.MAPPER.writeValueAsString("fixedid")+"," +
							 Constants.MAPPER.writeValueAsString("emojiPerTagName")+":{}," +
							 Constants.MAPPER.writeValueAsString("questions")+": []" +
							 "}";
		long beforeParse = System.currentTimeMillis();
		QuestionList questionList = QuestionList.Parser.fromString(jsonContent).build();
		long afterParse = System.currentTimeMillis();

		assertNotNull(questionList);
		assertTrue(questionList.getTimeCreatedMillis() >= beforeParse);
		assertTrue(questionList.getTimeCreatedMillis() <= afterParse);
	}
}