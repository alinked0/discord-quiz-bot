import com.linked.quizbot.Constants;
import com.linked.quizbot.utils.Option;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
public class TestOption {
    
    @Test
    @DisplayName("Test Option constructor with text and correctness")
    void testOptionConstructor_TextAndCorrectness() {
        Option option = new Option("Test Option 1", true);
        assertNotNull(option);
        assertEquals("Test Option 1", option.getText());
        assertTrue(option.isCorrect());
        assertNull(option.getExplication()); // Should be null by default
    }

    @Test
    @DisplayName("Test Option constructor with text, correctness, and explanation")
    void testOptionConstructor_Full() {
        Option option = new Option("Test Option 2", false, "This is an explanation.");
        assertNotNull(option);
        assertEquals("Test Option 2", option.getText());
        assertFalse(option.isCorrect());
        assertEquals("This is an explanation.", option.getExplication());
    }

    @Test
    @DisplayName("Test getText method")
    void testGetText() {
        Option option = new Option("Simple Text", true);
        assertEquals("Simple Text", option.getText());
    }

    @Test
    @DisplayName("Test isCorrect method - true")
    void testIsCorrect_True() {
        Option option = new Option("Correct Answer", true);
        assertTrue(option.isCorrect());
    }

    @Test
    @DisplayName("Test isCorrect method - false")
    void testIsCorrect_False() {
        Option option = new Option("Incorrect Answer", false);
        assertFalse(option.isCorrect());
    }

    @Test
    @DisplayName("Test getExplication method - with explanation")
    void testGetExplication_WithExplanation() {
        Option option = new Option("Option with Expl.", false, "Detail for option.");
        assertEquals("Detail for option.", option.getExplication());
    }

    @Test
    @DisplayName("Test getExplication method - without explanation")
    void testGetExplication_NoExplanation() {
        Option option = new Option("Option without Expl.", true);
        assertNull(option.getExplication());
    }

    @Test
    @DisplayName("Test getExplicationFriendly method - with explanation")
    void testGetExplicationFriendly_WithExplanation() {
        Option option = new Option("Option with Expl.", false, "Detail for option.");
        assertEquals("Detail for option.", option.getExplicationFriendly());
    }

    @Test
    @DisplayName("Test getExplicationFriendly method - without explanation (null)")
    void testGetExplicationFriendly_NoExplanationNull() {
        // Constants.NOEXPLICATION needs to be available for this test or mocked.
        // Assuming Constants.NOEXPLICATION is defined as "No explanation provided." or similar.
        // For the purpose of this test, we'll assert against the default behavior if Constants.java is not linked.
        // If Constants.java is compiled with the project, this test will pass based on its value.
        Option option = new Option("Option without Expl.", true);
        assertEquals(Constants.NOEXPLICATION, option.getExplicationFriendly());
    }

    @Test
    @DisplayName("Test getExplicationFriendly method - with 'null' string explanation")
    void testGetExplicationFriendly_NullStringExplanation() {
        Option option = new Option("Option with Null String Expl.", true, "null");
        assertEquals(Constants.NOEXPLICATION, option.getExplicationFriendly());
    }

    @Test
    @DisplayName("Test comparator - correct option comes before incorrect")
    void testComparator_CorrectBeforeIncorrect() {
        Option correctOption = new Option("Correct", true);
        Option incorrectOption = new Option("Incorrect", false);
        assertTrue(Option.comparator().compare(correctOption, incorrectOption) < 0);
    }

    @Test
    @DisplayName("Test comparator - incorrect option comes after correct")
    void testComparator_IncorrectAfterCorrect() {
        Option correctOption = new Option("Correct", true);
        Option incorrectOption = new Option("Incorrect", false);
        assertTrue(Option.comparator().compare(incorrectOption, correctOption) > 0);
    }

    @Test
    @DisplayName("Test comparator - two correct options are equal for sorting")
    void testComparator_TwoCorrectOptions() {
        Option correct1 = new Option("Correct 1", true);
        Option correct2 = new Option("Correct 2", true);
        assertEquals(0, Option.comparator().compare(correct1, correct2));
    }

    @Test
    @DisplayName("Test comparator - two incorrect options are equal for sorting")
    void testComparator_TwoIncorrectOptions() {
        Option incorrect1 = new Option("Incorrect 1", false);
        Option incorrect2 = new Option("Incorrect 2", false);
        assertEquals(0, Option.comparator().compare(incorrect1, incorrect2));
    }

    @Test
    @DisplayName("Test toString method - with explanation")
    void testToString_WithExplanation() {
        Option option = new Option("Test Option", true, "Detailed explanation here.");
        String expectedJson = "{\n\t\"text\":\"Test Option\",\n\t\"isCorrect\":true,\n\t\"explication\":\"Detailed explanation here.\"\n}".replace("\n\t\"","\"");
        assertEquals(expectedJson, option.toString());
    }

    @Test
    @DisplayName("Test toString method - without explanation (null)")
    void testToString_NoExplanationNull() {
        Option option = new Option("Test Option No Expl", false);
        String expectedJson = "{\n\t\"text\":\"Test Option No Expl\",\n\t\"isCorrect\":false,\n\t\"explication\":null\n}".replace("\n\t\"","\"");
        assertEquals(expectedJson, option.toString());
    }

    @Test
    @DisplayName("Test toString method - with empty string explanation")
    void testToString_EmptyStringExplanation() {
        Option option = new Option("Test Option Empty Expl", true, "");
        String expectedJson = "{\n\t\"text\":\"Test Option Empty Expl\",\n\t\"isCorrect\":true,\n\t\"explication\":\"\"\n}".replace("\n\t\"","\"");
        assertEquals(expectedJson, option.toString());
    }

    @Test
    @DisplayName("Test toString method - with 'null' string explanation")
    void testToString_NullStringExplanation() {
        Option option = new Option("Test Option 'null' Expl", true, "null");
        String expectedJson = "{\n\t\"text\":\"Test Option 'null' Expl\",\n\t\"isCorrect\":true,\n\t\"explication\":null\n}".replace("\n\t\"","\"");
        assertEquals(expectedJson, option.toString());
    }

    @Test
    @DisplayName("Test toString method - with Constants.NOEXPLICATION explanation")
    void testToString_ConstantsNoExplanation() {
        Option option = new Option("Test Option Constants.NOEXPLICATION", true, Constants.NOEXPLICATION);
        String expectedJson = "{\n\t\"text\":\"Test Option Constants.NOEXPLICATION\",\n\t\"isCorrect\":true,\n\t\"explication\":null\n}".replace("\n\t\"","\"");
        assertEquals(expectedJson, option.toString());
    }

    @Test
    @DisplayName("Test equals method - same object")
    void testEquals_SameObject() {
        Option option = new Option("Same", true);
        assertTrue(option.equals(option));
    }

    @Test
    @DisplayName("Test equals method - equal options (same text)")
    void testEquals_EqualOptions() {
        Option option1 = new Option("Equal Text", true, "Expl 1");
        Option option2 = new Option("Equal Text", false, "Expl 2"); // Correctness and explanation don't matter for equals
        assertTrue(option1.equals(option2));
    }

    @Test
    @DisplayName("Test equals method - different options (different text)")
    void testEquals_DifferentOptions() {
        Option option1 = new Option("Text A", true);
        Option option2 = new Option("Text B", false);
        assertFalse(option1.equals(option2));
    }

    @Test
    @DisplayName("Test equals method - compare with String (equal)")
    void testEquals_CompareWithString_Equal() {
        Option option = new Option("String Match", true);
        assertTrue(option.equals("String Match"));
    }

    @Test
    @DisplayName("Test equals method - compare with String (not equal)")
    void testEquals_CompareWithString_NotEqual() {
        Option option = new Option("String Match", true);
        assertFalse(option.equals("No Match"));
    }

    @Test
    @DisplayName("Test equals method - compare with null")
    void testEquals_CompareWithNull() {
        Option option = new Option("Test", true);
        assertFalse(option.equals(null));
    }

    @Test
    @DisplayName("Test equals method - compare with different class object")
    void testEquals_CompareWithDifferentClass() {
        Option option = new Option("Test", true);
        assertFalse(option.equals(new Object()));
    }

    @Test
    @DisplayName("Test getExampleOption method")
    void testGetExampleOption() {
        List<Option> exampleOptions = Option.getExampleOption();
        assertNotNull(exampleOptions);
        assertEquals(2, exampleOptions.size());

        Option opt1 = exampleOptions.get(0);
        assertEquals("Water", opt1.getText());
        assertTrue(opt1.isCorrect());
        assertEquals("Correct! H2O is the chemical formula for water", opt1.getExplication());

        Option opt2 = exampleOptions.get(1);
        assertEquals("Carbon Dioxide", opt2.getText());
        assertFalse(opt2.isCorrect());
        assertEquals("Incorrect. CO2 is carbon dioxide", opt2.getExplication());
    }
}
