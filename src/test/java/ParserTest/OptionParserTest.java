package ParserTest;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linked.quizbot.Constants; // Assuming this provides ObjectMapper and error strings
import com.linked.quizbot.utils.Option;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;

public class OptionParserTest {
	
	// Assuming Constants.MAPPER is a public static ObjectMapper instance, 
	// and Constants.ERROR is a public static String.
	private final ObjectMapper mapper = Constants.MAPPER;
	
	// Helper method to create a JsonParser from a JSON string
	private JsonParser createParser(String json) throws IOException{
		// We call nextToken() here as the parser needs to be advanced to the first token 
		// (START_OBJECT or START_ARRAY) for the Option.Parser methods to start correctly.
		// The Option.Parser.parse/parseList methods call jp.nextToken() at the start if currentToken is not the expected one.
		JsonParser jp = mapper.getFactory().createParser(json);
		jp.nextToken(); 
		return jp;
	}
	
	// --- Tests for parse(JsonParser jp, String original) ---
	
	@Test
	@DisplayName("Test parse - Full valid Option (text, isCorrect, explication)")
	void testParse_FullValidOption() throws IOException{
		String json = "{\"option\":\"Test Text\", \"isCorrect\":true, \"explication\":\"Test Explanation\"}";
		JsonParser jp = createParser(json);
		
		Option option = Option.Parser.parse(jp, json);
		
		assertNotNull(option);
		assertEquals("Test Text", option.getText());
		assertTrue(option.isCorrect());
		assertEquals("Test Explanation", option.getExplication());
		assertTrue(jp.isClosed() || jp.currentToken() == null, "Parser should have consumed the object");
	}
	
	@Test
	@DisplayName("Test parse - Option with 'correct' alias for isCorrect")
	void testParse_CorrectAlias() throws IOException{
		String json = "{\"option\":\"Alias Test\", \"correct\":false, \"explication\":\"Alias Explanation\"}";
		JsonParser jp = createParser(json);
		
		Option option = Option.Parser.parse(jp, json);
		
		assertNotNull(option);
		assertFalse(option.isCorrect());
	}
	
	@Test
	@DisplayName("Test parse - Option without explication")
	void testParse_NoExplication() throws IOException{
		String json = "{\"option\":\"No Expl Text\", \"isCorrect\":false}";
		JsonParser jp = createParser(json);
		
		Option option = Option.Parser.parse(jp, json);
		
		assertNotNull(option);
		assertEquals("No Expl Text", option.getText());
		assertFalse(option.isCorrect());
		assertEquals(Constants.NOEXPLICATION, option.getExplication()); // getExplication() returns NOEXPLICATION for null explication
	}
	
	@Test
	@DisplayName("Test parse - Option with null explication string")
	void testParse_NullStringExplication() throws IOException{
		String json = "{\"option\":\"Null Expl Text\", \"isCorrect\":true, \"explication\":\"null\"}";
		JsonParser jp = createParser(json);
		
		Option option = Option.Parser.parse(jp, json);
		
		assertNotNull(option);
		assertEquals(Constants.NOEXPLICATION, option.getExplication());
	}
	
	@Test
	@DisplayName("Test parse - Option with null JSON value for explication")
	void testParse_NullJsonValueExplication() throws IOException{
		String json = "{\"option\":\"Null Json Expl Text\", \"isCorrect\":true, \"explication\":null}";
		JsonParser jp = createParser(json);
		
		Option option = Option.Parser.parse(jp, json);
		
		assertNotNull(option);
		assertEquals(Constants.NOEXPLICATION, option.getExplication());
	}
	
	@Test
	@DisplayName("Test parse - Missing 'isCorrect' field")
	void testParse_MissingIsCorrect() throws IOException{
		String json = "{\"option\":\"Missing Correct\"}";
		JsonParser jp = createParser(json);
		
		Option option = Option.Parser.parse(jp, json);
		
		assertNull(option); // parse returns null if optTxt or isCorr is null
	}
	
	@Test
	@DisplayName("Test parse - Missing 'text' field")
	void testParse_MissingText() throws IOException{
		String json = "{\"isCorrect\":true}";
		JsonParser jp = createParser(json);
		
		Option option = Option.Parser.parse(jp, json);
		
		assertNull(option); // parse returns null if optTxt or isCorr is null
	}
	
	@Test
	@DisplayName("Test parse - Invalid JSON format (not an object)")
	void testParse_InvalidJsonFormat() throws IOException{
		String json = "\"Not a json object\"";
		
		// Need to create parser and not call nextToken() if the first token is not START_OBJECT for the initial check to pass
		JsonParser jp = mapper.getFactory().createParser(json);
		
		IOException thrown = assertThrows(IOException.class, () -> {
			Option.Parser.parse(jp, json);
		});
		
		assertTrue(thrown.getMessage().contains("Option.Parser.parse, input is not a json"), 
				   "Exception message should indicate invalid JSON format");
	}
	
	// --- Tests for parseList(JsonParser jp, String original) ---
	
	@Test
	@DisplayName("Test parseList - Valid list of Options")
	void testParseList_ValidList() throws IOException{
		String json = "[" + 
					  "{\"option\":\"Opt 1\", \"isCorrect\":true, \"explication\":\"Expl 1\"}," +
					  "{\"option\":\"Opt 2\", \"isCorrect\":false}" +
					  "]";
		JsonParser jp = createParser(json);
		
		List<Option> options = Option.Parser.parseList(jp, json);
		
		assertNotNull(options);
		assertEquals(2, options.size());
		assertEquals("Opt 1", options.get(0).getText());
		assertTrue(options.get(0).isCorrect());
		assertEquals("Opt 2", options.get(1).getText());
		assertFalse(options.get(1).isCorrect());
		assertTrue(jp.isClosed() || jp.currentToken() == null, "Parser should have consumed the array");
	}
	
	@Test
	@DisplayName("Test parseList - List with an invalid Option (missing field)")
	void testParseList_InvalidOptionInList() throws IOException{
		String json = "[" + 
					  "{\"option\":\"Valid Opt\", \"isCorrect\":true}," +
					  "{\"option\":\"Missing Correct\"}," + // This one will return null from parse()
					  "{\"option\":\"Another Valid\", \"isCorrect\":false}" +
					  "]";
		JsonParser jp = createParser(json);
		
		List<Option> options = Option.Parser.parseList(jp, json);
		
		assertNotNull(options);
		assertEquals(2, options.size()); // The invalid option is skipped
		assertEquals("Valid Opt", options.get(0).getText());
		assertEquals("Another Valid", options.get(1).getText());
	}
	
	@Test
	@DisplayName("Test parseList - Empty list")
	void testParseList_EmptyList() throws IOException{
		String json = "[]";
		JsonParser jp = createParser(json);
		
		List<Option> options = Option.Parser.parseList(jp, json);
		
		assertNotNull(options);
		assertTrue(options.isEmpty());
	}
	
	@Test
	@DisplayName("Test parseList - Invalid JSON format (not an array)")
	void testParseList_InvalidJsonFormat() throws IOException{
		String json = "{\"key\":\"Not a json array\"}";
		JsonParser jp = mapper.getFactory().createParser(json);
		
		IOException thrown = assertThrows(IOException.class, () -> {
			Option.Parser.parseList(jp, json);
		});
		
		assertTrue(thrown.getMessage().contains("Option.Parser.parseList, input is not a json"), 
				   "Exception message should indicate invalid JSON array format");
	}
}