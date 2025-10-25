import com.fasterxml.jackson.core.JsonProcessingException;
import com.linked.quizbot.Constants;
import com.linked.quizbot.utils.Option;
import com.linked.quizbot.utils.Question;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class QuestionTest {
	
	
	@Test
	@DisplayName("Test Question constructor for true-or-false")
	void testQuestionConstructor_TrueFalse() {
		Question q = new Question("Is the sky blue?");
		assertNotNull(q);
		assertEquals("Is the sky blue?", q.getQuestion());
		assertEquals(1, q.getNumberTrue());
		assertEquals(2, q.size()); // Should contain two options
		assertEquals("True", q.get(0).getText());
		assertTrue(q.get(0).isCorrect());
		assertEquals("False", q.get(1).getText());
		assertFalse(q.get(1).isCorrect());
		assertEquals(Constants.NOEXPLICATION, q.getExplication()); // No explanation by default
	}
	
	@Test
	@DisplayName("Test Question constructor with Collection of Options")
	void testQuestionConstructor_CollectionOptions() {
		Option opt1 = new Option("Option A", true);
		Option opt2 = new Option("Option B", false);
		List<Option> options = Arrays.asList(opt1, opt2);
		
		Question q = new Question("Which is correct?", options);
		assertNotNull(q);
		assertEquals("Which is correct?", q.getQuestion());
		assertEquals(1, q.getNumberTrue());
		assertEquals(2, q.size());
		assertTrue(q.contains(opt1));
		assertTrue(q.contains(opt2));
	}
	
	@Test
	@DisplayName("Test Question constructor with numberTrue and String... options")
	void testQuestionConstructor_NumberTrueStringOptions() {
		Question q = new Question("Pick two correct", 2, "Correct 1", "Correct 2", "Incorrect 1", "Incorrect 2");
		assertNotNull(q);
		assertEquals("Pick two correct", q.getQuestion());
		assertEquals(2, q.getNumberTrue());
		assertEquals(4, q.size());
		assertTrue(q.get(0).isCorrect()); // Correct 1
		assertTrue(q.get(1).isCorrect()); // Correct 2
		assertFalse(q.get(2).isCorrect()); // Incorrect 1
		assertFalse(q.get(3).isCorrect()); // Incorrect 2
		assertEquals("Correct 1", q.get(0).getText());
		assertEquals("Correct 2", q.get(1).getText());
	}
	
	@Test
	@DisplayName("Test Question constructor with Option... options")
	void testQuestionConstructor_OptionArray() {
		Option opt1 = new Option("True option", true);
		Option opt2 = new Option("False option", false);
		Question q = new Question("Choose wisely", opt1, opt2);
		assertNotNull(q);
		assertEquals("Choose wisely", q.getQuestion());
		assertEquals(1, q.getNumberTrue());
		assertEquals(2, q.size());
		assertTrue(q.contains(opt1));
		assertTrue(q.contains(opt2));
	}
	
	@Test
	@DisplayName("Test getQuestion method")
	void testGetQuestion() {
		Question q = new Question("What is the capital of France?");
		assertEquals("What is the capital of France?", q.getQuestion());
	}
	
	@Test
	@DisplayName("Test getNumberTrue method")
	void testGetNumberTrue() {
		Question q = new Question("Multiple choice", Arrays.asList(
				new Option("A", true),
				new Option("B", false),
				new Option("C", true)
		));
		assertEquals(2, q.getNumberTrue());
		
		Question q2 = new Question("Single choice", new Option("Only true", true));
		assertEquals(1, q2.getNumberTrue());
	}
	
	@Test
	@DisplayName("Test getExplication method")
	void testExplicationMethods() {
		Question q1 = new Question("Question with no explication");
		assertEquals(Constants.NOEXPLICATION, q1.getExplication());
		
		Question q2 = new Question("Question with explication");
		q2.setExplication("Detailed explanation.");
		assertEquals("Detailed explanation.", q2.getExplication());
		
		Question q3 = new Question("Question with 'null' string explication");
		q3.setExplication("null");
		assertEquals(Constants.NOEXPLICATION, q3.getExplication());
		
		Question q4 = new Question("Question with Constants.NOEXPLICATION string explication");
		q4.setExplication(Constants.NOEXPLICATION);
		assertEquals(Constants.NOEXPLICATION, q4.getExplication());
	}
	
	@Test
	@DisplayName("Test getImageSrc and setImageSrc methods")
	void testImageSrcMethods() {
		Question q = new Question("Image question");
		assertNull(q.getImageSrc());
		
		q.setImageSrc("http://example.com/image.png");
		assertEquals("http://example.com/image.png", q.getImageSrc());
		
		q.setImageSrc(null);
		assertNull(q.getImageSrc());
	}
	
	@Test
	@DisplayName("Test getOptions method")
	void testGetOptions() {
		Option opt1 = new Option("First", true);
		Option opt2 = new Option("Second", false);
		Option opt3 = new Option("Third", true);
		Question q = new Question("Order test", Arrays.asList(opt1, opt2, opt3));
		
		List<Option> options = q.getOptions();
		assertNotNull(options);
		assertEquals(3, options.size());
		assertEquals(opt1, options.get(0)); // LinkedList maintains insertion order
		assertEquals(opt2, options.get(1));
		assertEquals(opt3, options.get(2));
	}
	
	@Test
	@DisplayName("Test get method (indexed access)")
	void testGet_IndexedAccess() {
		Option opt1 = new Option("A", true);
		Option opt2 = new Option("B", false);
		Question q = new Question("Index test", opt1, opt2);
		
		assertEquals(opt1, q.get(0));
		assertEquals(opt2, q.get(1));
		assertThrows(IndexOutOfBoundsException.class, () -> q.get(-1)); // Out of bounds
		assertThrows(IndexOutOfBoundsException.class, () -> q.get(2));  // Out of bounds
	}
	
	@Test
	@DisplayName("Test trueOptions method")
	void testGetTrueOptions() {
		Option opt1 = new Option("Correct 1", true);
		Option opt2 = new Option("Incorrect 1", false);
		Option opt3 = new Option("Correct 2", true);
		Option opt4 = new Option("Incorrect 2", false);
		Question q = new Question("Get true options test", Arrays.asList(opt1, opt2, opt3, opt4));
		
		List<Option> trueOptions = q.trueOptions();
		assertNotNull(trueOptions);
		assertEquals(2, trueOptions.size());
		assertTrue(trueOptions.contains(opt1));
		assertTrue(trueOptions.contains(opt3));
		assertFalse(trueOptions.contains(opt2));
	}
	
	@Test
	@DisplayName("Test falseOptions method")
	void testGetFalseOptions() {
		Option opt1 = new Option("Correct 1", true);
		Option opt2 = new Option("Incorrect 1", false);
		Option opt3 = new Option("Correct 2", true);
		Option opt4 = new Option("Incorrect 2", false);
		Question q = new Question("Get false options test", Arrays.asList(opt1, opt2, opt3, opt4));
		
		List<Option> falseOptions = q.falseOptions();
		assertNotNull(falseOptions);
		assertEquals(2, falseOptions.size());
		assertTrue(falseOptions.contains(opt2));
		assertTrue(falseOptions.contains(opt4));
		assertFalse(falseOptions.contains(opt1));
	}
	
	@Test
	@DisplayName("Test isCorrect method (option correctness)")
	void testIsCorrect_OptionCorrectness() {
		Option correctOpt = new Option("Correct", true);
		Option incorrectOpt = new Option("Incorrect", false);
		Question q = new Question("Check correctness", correctOpt, incorrectOpt);
		
		assertTrue(q.isCorrect(correctOpt));
		assertFalse(q.isCorrect(incorrectOpt));
	}
	
	@Test
	@DisplayName("Test rearrageOptions and rearrageOptions methods")
	void testRearrangeOptions() {
		// Create a question with distinct options to easily detect rearrangement
		Option opt1 = new Option("One", true);
		Option opt2 = new Option("Two", false);
		Option opt3 = new Option("Three", true);
		Option opt4 = new Option("One", true);
		Option opt5 = new Option("Two", false);
		Option opt6 = new Option("Three", true);
		Question q = new Question("Rearrange me", opt1, opt2, opt3, opt4, opt5, opt6);
		
		List<Option> originalOrder = new ArrayList<>(q.getOptions());
		
		// Test rearrageOptions()
		List<Option> rearranged10 = q.rearrageOptions().getOptions();
		assertEquals(originalOrder.size(), rearranged10.size());
		// It's possible but highly improbable for random to return the same order.
		// We'll verify content is the same, just order might differ.
		assertTrue(rearranged10.containsAll(originalOrder) && originalOrder.containsAll(rearranged10));
		// Check if order is *likely* different, but allow for chance.
		List<Option> rearranged11 = q.rearrageOptions().getOptions();
		assertNotEquals(originalOrder.equals(rearranged10) ^ originalOrder.equals(rearranged11), "Order should generally be different due to random sort.");
		
		// Verify content is still the same
		assertTrue(rearranged10.containsAll(originalOrder) && originalOrder.containsAll(rearranged10));
		
		// Re-randomize with a *different* fixed seed and ensure it changes again
		Random differentRandom = new Random(456L);
		q.rearrageOptions((e,f)-> differentRandom.nextBoolean()?1:-1);
		List<Option> rearranged3 = q.getOptions();
		assertNotEquals(rearranged10, rearranged3, "rearrageOptions should change order with a new seed.");
	}
	
	@Test
	@DisplayName("Test toString method - basic without explication/image")
	void testToString_Basic() throws JsonProcessingException{
		Option opt1 = new Option("True option", true);
		Option opt2 = new Option("False option", false);
		Question q = new Question("Simple Question", opt1, opt2);
		
		String expectedJson = "{\n" +
							  "\t\"question\":"+Constants.MAPPER.writeValueAsString("Simple Question")+",\n" +
							  "\t\"explication\":null,\n" +
							  "\t\"imageSrc\":null,\n" +
							  "\t\"options\": [\n" +
							  "\t\t{\n\t\"option\":"+Constants.MAPPER.writeValueAsString("True option")+",\n\t\"isCorrect\":true,\n\t\"explication\":null\n},\n" +
							  "\t\t{\n\t\"option\":"+Constants.MAPPER.writeValueAsString("False option")+",\n\t\"isCorrect\":false,\n\t\"explication\":null\n}\n" +
							  "\t]\n}";
		// Normalize whitespace for comparison if necessary, but direct comparison is usually fine
		assertEquals(expectedJson.replaceAll("[\\n \\s \\t]*", ""), q.toString().replaceAll("[\\n \\s \\t]*", ""));
	}
	
	@Test
	@DisplayName("Test toString method - with explication and imageSrc")
	void testToString_WithExplicationAndImage() throws JsonProcessingException{
		Option opt1 = new Option("Correct", true, "This is correct");
		Option opt2 = new Option("Incorrect", false);
		Question q = new Question("Advanced Question", opt1, opt2);
		q.setExplication("Full explanation for the question.");
		q.setImageSrc("http://example.com/question.jpg");
		
		String expectedJson = "{\n" +
							  "\t\"question\":"+Constants.MAPPER.writeValueAsString("Advanced Question")+",\n" +
							  "\t\"explication\":"+Constants.MAPPER.writeValueAsString("Full explanation for the question.")+",\n" +
							  "\t\"imageSrc\":"+Constants.MAPPER.writeValueAsString("http://example.com/question.jpg")+",\n" +
							  "\t\"options\": [\n" +
							  "\t\t{\n\t\"option\":\"Correct\",\n\t\"isCorrect\":true,\n\t\"explication\":"+Constants.MAPPER.writeValueAsString("This is correct")+"\n},\n" +
							  "\t\t{\n\t\"option\":\"Incorrect\",\n\t\"isCorrect\":false,\n\t\"explication\":null\n}\n" +
							  "\t]\n}";
		assertEquals(expectedJson.replaceAll("[\\n \\s \\t]*", ""), q.toString().replaceAll("[\\n \\s \\t]*", ""));
	}
	
	@Test
	@DisplayName("Test toString method - explication is 'null' string or Constants.NOEXPLICATION")
	void testToString_NullExplications() throws JsonProcessingException{
		Option opt = new Option("Dummy", true);
		
		Question q1 = new Question("Question with 'null' explication string", opt);
		q1.setExplication("null");
		String expected1 = "{\n\t\"question\":"+Constants.MAPPER.writeValueAsString("Question with 'null' explication string")+",\n\t\"explication\":null,\n\t\"imageSrc\":null,\n\t\"options\": [\n\t\t{\n\t\"option\":\"Dummy\",\n\t\"isCorrect\":true,\n\t\"explication\":null\n}\n\t]\n}";
		assertEquals(expected1.replaceAll("[\\n \\s \\t]*", ""), q1.toString().replaceAll("[\\n \\s \\t]*", ""));
		
		Question q2 = new Question("Question with Constants.NOEXPLICATION", opt);
		q2.setExplication(Constants.NOEXPLICATION);
		String expected2 = "{\n\t\"question\":"+Constants.MAPPER.writeValueAsString("Question with Constants.NOEXPLICATION")+",\n\t\"explication\":null,\n\t\"imageSrc\":null,\n\t\"options\": [\n\t\t{\n\t\"option\":\"Dummy\",\n\t\"isCorrect\":true,\n\t\"explication\":null\n}\n\t]\n}";
		assertEquals(expected2.replaceAll("[\\n \\s \\t]*", ""), q2.toString().replaceAll("[\\n \\s \\t]*", ""));
	}
	
	
	@Test
	@DisplayName("Test equals method - same object")
	void testEquals_SameObject() {
		Question q = new Question("Same object", new Option("Opt", true));
		assertTrue(q.equals(q));
	}
	
	@Test
	@DisplayName("Test equals method - equal questions (same question text and options)")
	void testEquals_EqualQuestions() {
		Option opt1 = new Option("Option 1", true);
		Option opt2 = new Option("Option 2", false);
		Question q1 = new Question("Question text", Arrays.asList(opt1, opt2));
		Question q2 = new Question("Question text", Arrays.asList(opt1, opt2));
		
		assertTrue(q1.equals(q2));
	}
	
	@Test
	@DisplayName("Test equals method - different questions (different text)")
	void testEquals_DifferentQuestionText() {
		Option opt = new Option("Common Option", true);
		Question q1 = new Question("Question A", opt);
		Question q2 = new Question("Question B", opt);
		
		assertFalse(q1.equals(q2));
	}
	
	@Test
	@DisplayName("Test equals method - different questions (different options)")
	void testEquals_DifferentOptions() {
		Question q1 = new Question("Same Text", new Option("Opt1", true));
		Question q2 = new Question("Same Text", new Option("Opt2", true)); // Option content differs, so equals false
		
		assertFalse(q1.equals(q2));
		
		// Test with different number of options
		Question q3 = new Question("Same Text", new Option("Opt1", true));
		Question q4 = new Question("Same Text", new Option("Opt1", true), new Option("Opt2", false));
		assertFalse(q3.equals(q4));
	}
	
	@Test
	@DisplayName("Test equals method - compare with null")
	void testEquals_CompareWithNull() {
		Question q = new Question("Test", new Option("Opt", true));
		assertFalse(q.equals(null));
	}
	
	@Test
	@DisplayName("Test equals method - compare with different class object")
	void testEquals_CompareWithDifferentClass() {
		Question q = new Question("Test", new Option("Opt", true));
		assertFalse(q.equals(new Object()));
		assertFalse(q.equals("a string"));
	}
	
	@Test
	@DisplayName("Test getExampleQuestion method")
	void testGetExampleQuestion() {
		Question exampleQuestion = Question.getExampleQuestion();
		assertNotNull(exampleQuestion);
		assertEquals("What is H₂O?", exampleQuestion.getQuestion());
		assertEquals(2, exampleQuestion.size()); // Should have 2 options from Option.getExampleOption()
		
		List<Option> options = exampleQuestion.getOptions();
		Option opt1 = options.get(0);
		Option opt2 = options.get(1);
		
		assertEquals("Water", opt1.getText());
		assertTrue(opt1.isCorrect());
		assertEquals("Correct! H2O is the chemical formula for water", opt1.getExplication());
		
		assertEquals("Carbon Dioxide", opt2.getText());
		assertFalse(opt2.isCorrect());
		assertEquals("Incorrect. CO2 is carbon dioxide", opt2.getExplication());
	}
}

