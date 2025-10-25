package ParserTest;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.linked.quizbot.Constants;
import com.linked.quizbot.utils.Option;
import com.linked.quizbot.utils.Question;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.StringReader;

public class QuestionParserTest {
	
	private JsonFactory factory;
	
	@BeforeEach
	void setUp() {
		// Initialize Jackson's JsonFactory
		factory = new JsonFactory();
	}
	
	// A helper method to create a JsonParser from a String
	private JsonParser createJsonParser(String json) throws IOException{
		return factory.createParser(new StringReader(json));
	}
	
	// ---
	
	
	@Test
	void testParse_FullQuestion() throws IOException{
		// GIVEN a complete JSON string with all fields
		String json = """
			{
				"question": "What is the capital of France?",
				"explication": "It's Paris!",
				"imageSrc": "http://example.com/paris.jpg",
				"options": [
					{"option":"Correct Answer", "correct": true}, 
					{"option":"Wrong Answer", "correct": false}
				]
			}
			""";
		
		// WHEN parsing the JSON
		JsonParser jp = createJsonParser(json);
		Question result = Question.Parser.parse(jp, json);
		
		// THEN the Question object should be correctly built
		assertNotNull(result, "The parsed question should not be null."+json);
		assertEquals("What is the capital of France?", result.getQuestion(), "Question text mismatch.");
		assertEquals("It's Paris!", result.getExplication(), "Explication mismatch.");
		assertEquals("http://example.com/paris.jpg", result.getImageSrc(), "Image source mismatch.");
		
		List<Option> expectedOptions = List.of(
			new Option("Correct Answer", true),
			new Option("Wrong Answer", false)
		);
		
		// Note: The Question constructor sorts correct options first, 
		// which the mock Option.Parser.parseList also emulates.
		assertEquals(expectedOptions.size(), result.getOptions().size(), "Option count mismatch.");
		assertTrue(result.getOptions().containsAll(expectedOptions), "Options content mismatch.");
	}
	
	// ---
	
	
	@Test
	void testParse_MinimalQuestion() throws IOException{
		// GIVEN a minimal JSON string (question and options with a correct one)
		String json = """
			{
				"question": "Minimal Question",
				"options": [
					{"option":"Correct Answer", "correct": true}, 
					{"option":"Wrong Answer", "correct": false}
				]
			}
			""";
		
		// WHEN parsing the JSON
		JsonParser jp = createJsonParser(json);
		Question result = Question.Parser.parse(jp, json);
		
		// THEN the Question object should be correctly built with default/null optional fields
		assertNotNull(result, "The parsed question should not be null.");
		assertEquals("Minimal Question", result.getQuestion(), "Question text mismatch.");
		assertEquals(Constants.NOEXPLICATION, result.getExplication(), "Explication should be default.");
		assertNull(result.getImageSrc(), "Image source should be null.");
	}
	
	// ---
	
	
	@Test
	void testParse_NullAndEmptyOptionals() throws IOException{
		// GIVEN a JSON string with null/empty values for optional fields
		String json = """
			{
				"question": "Test Null Fields",
				"explication": "null",
				"img_src": "null",
				"options": [
					{"option":"Correct Answer", "correct": true}, 
					{"option":"Wrong Answer", "correct": false}
				]
			}
			""";
		
		// WHEN parsing the JSON
		JsonParser jp = new JsonFactory().createParser(json);
		Question result = Question.Parser.parse(jp, json);
		
		// THEN the Question object should handle null/empty strings correctly
		assertNotNull(result);
		assertEquals(Constants.NOEXPLICATION, result.getExplication(), "Explication 'null' string should result in NOEXPLICATION.");
		assertNull(result.getImageSrc(), "Image source 'null' string should result in null.");
		
		// GIVEN a JSON string with the alternative field name
		String jsonAltImg = """
			{
				"question": "Test Alternative ImageSrc",
				"imageSrc": "test.png",
				"options": [
					{"option":"Correct Answer", "correct": true}, 
					{"option":"Wrong Answer", "correct": false}
				]
			}
			""";
		
		// WHEN parsing the JSON
		jp = createJsonParser(jsonAltImg);
		result = Question.Parser.parse(jp, jsonAltImg);
		
		// THEN the 'imageSrc' field should be correctly parsed (case "imagesrc")
		assertNotNull(result);
		assertEquals("test.png", result.getImageSrc(), "Alternative imageSrc field name was not parsed.");
	}
	
	// ---

	
	@Test
	void testParse_MissingQuestionField() throws IOException{
		// GIVEN a JSON missing the mandatory 'question' field
		String json = """
			{
				"explication": "None",
				"options": [
					{"option":"Correct Answer", "correct": true}
				]
			}
			""";
		
		// WHEN parsing the JSON
		JsonParser jp = createJsonParser(json);
		Question result = Question.Parser.parse(jp, json);
		
		// THEN parsing should return null because `q==null` condition is met
		assertNull(result, "Parsing should return null when the 'question' field is missing.");
	}
	
	@Test
	void testParse_NoOptions() throws IOException{
		// GIVEN a JSON with no options (opts==null or opts.isEmpty())
		String json = """
			{
				"question": "Test No Options",
				"options": []
			}
			"""; // Original string used for Option.Parser mock lookup is "Test No Options"
		
		// WHEN parsing the JSON
		JsonParser jp = createJsonParser(json);
		Question result = Question.Parser.parse(jp, "no-options"); // Use the key for the mock
		
		// THEN parsing should return null
		assertNull(result, "Parsing should return null when 'options' list is empty.");
	}
	
	@Test
	void testParse_NoCorrectOptions() throws IOException{
		// GIVEN a JSON where all options are incorrect
		String json = """
			{
				"question": "Test No Correct Options",
				"options": [
					{"option":"Wrong 1", "correct": false},
					{"option":"Wrong 2", "correct": false}
				]
			}
			""";
		
		// WHEN parsing the JSON
		JsonParser jp = createJsonParser(json);
		Question result = Question.Parser.parse(jp, "no-correct"); // Use the key for the mock
		
		// THEN parsing should return null because `opts.stream().filter(o -> o.isCorrect()).count()==0`
		assertNull(result, "Parsing should return null when there are no correct options.");
	}
	
	@Test
	void testParse_InputIsNotJson() throws IOException{
		// GIVEN an input string that is not a JSON object
		String notJson = "This is not a JSON string";
		
		// WHEN attempting to parse
		JsonParser jp = createJsonParser(notJson);
		
		// THEN an IOException should be thrown due to the initial token check
		assertThrows(IOException.class, () -> {
			Question.Parser.parse(jp, notJson);
		}, "An IOException should be thrown for non-JSON input.");
	}
}