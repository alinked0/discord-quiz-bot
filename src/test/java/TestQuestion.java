import com.linked.quizbot.utils.Option;
import com.linked.quizbot.utils.Question;

import java.util.Arrays;
import java.util.List;

public class TestQuestion {
	public static void test(String[] args) {
		// Test 1: True/False question
		Question tfQuestion = new Question("Is the sky blue?");
		System.out.println("Test 1: True/False Question");
		System.out.println("Question: " + tfQuestion.getQuestion());
		System.out.println("Options: " + tfQuestion.getOptions());
		System.out.println("True Options: " + tfQuestion.getTrueOptions());
		System.out.println("False Options: " + tfQuestion.getFalseOptions());
		System.out.println("Is 'True' correct? " + tfQuestion.isCorrect(tfQuestion.get(0)));
		System.out.println("Is 'False' correct? " + tfQuestion.isCorrect(tfQuestion.get(1)));
		System.out.println("JSON Representation: " + tfQuestion);
		System.out.println();

		// Test 2: Multiple choice question
		List<Option> options = Arrays.asList(
				new Option("Option 1", true), //true),
				new Option("Option 2", true), //true),
				new Option("Option 3", false), //false),
				new Option("Option 4", false) //false)
		);
		Question mcQuestion = new Question("Which options are true?", options);
		System.out.println("Test 2: Multiple Choice Question");
		System.out.println("Question: " + mcQuestion.getQuestion());
		System.out.println("Options: " + mcQuestion.getOptions());
		System.out.println("True Options: " + mcQuestion.getTrueOptions());
		System.out.println("False Options: " + mcQuestion.getFalseOptions());
		System.out.println("Is 'Option 1' correct? " + mcQuestion.isCorrect(mcQuestion.get(0)));
		System.out.println("Is 'Option 3' correct? " + mcQuestion.isCorrect(mcQuestion.get(2)));
		System.out.println("JSON Representation: " + mcQuestion);
		System.out.println();

		// Test 3: Invalid index
		System.out.println("Test 3: Invalid Index");
		System.out.println("Option at index 5: " + mcQuestion.get(5));
		System.out.println();

		// Test 4: Equality Check
		Question duplicateMcQuestion = new Question("Which options are true?", options);
		System.out.println("Test 4: Equality Check");
		System.out.println("Is mcQuestion equal to duplicateMcQuestion? " + mcQuestion.equals(duplicateMcQuestion));
		System.out.println();

		// Test 5: Invalid inputs
		System.out.println("Test 5: Invalid Inputs");
		try {
			Question invalidQuestion = new Question(null);
		} catch (Exception e) {
			System.out.println("Caught exception for null question: " + e.getMessage());
		}
	}
}
