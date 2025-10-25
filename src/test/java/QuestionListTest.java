
import com.linked.quizbot.utils.QuestionList;
import com.linked.quizbot.utils.Question;
import com.linked.quizbot.utils.Option;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for QuestionList equality and contains operations.
 * Focuses on testing the equality implementation and the contains behavior 
 * for collections of QuestionList objects.
 */
public class QuestionListTest {
	
	private QuestionList questionList1;
	private QuestionList questionList2;
	private QuestionList questionList3;
	private List<QuestionList> questionListCollection;
	
	@BeforeEach
	public void setUp() {
		// Create the first QuestionList with specific attributes and questions
		questionList1 = new QuestionList("author1", "Test Questions");
		questionList1.add(new Question("What is H2O?", createSampleOptions("Water", "Carbon Dioxide")));
		questionList1.add(new Question("What is the capital of France?", createSampleOptions("Paris", "London")));
		
		// Create a second QuestionList with the same attributes and questions (should be equal)
		questionList2 = new QuestionList.Builder().add(questionList1).build();
		
		// Create a third QuestionList with different attributes (should not be equal)
		questionList3 = new QuestionList("author2", "Different Questions");
		questionList3.add(new Question("Who was the first U.S. President?", createSampleOptions("George Washington", "Thomas Jefferson")));
		
		// Initialize the collection of QuestionLists
		questionListCollection = new ArrayList<>();
		questionListCollection.add(questionList1);
	}
	
	/**
	 * Helper method to create sample options for questions
	 */
	private LinkedList<Option> createSampleOptions(String correctAnswer, String incorrectAnswer) {
		LinkedList<Option> options = new LinkedList<>();
		options.add(new Option(correctAnswer, true, "Correct explanation"));
		options.add(new Option(incorrectAnswer, false, "Incorrect explanation"));
		return options;
	}
	
	@Test
	public void testQuestionListEquality() {
		// Test equality between two QuestionLists with same attributes and questions
		assertEquals(questionList1, questionList2, "Two QuestionLists with the same attributes and questions should be equal");
		assertEquals(questionList1.size(), questionList2.size(), "Two QuestionLists with the same attributes and questions should be equal");
		
		// Test equality with itself
		assertEquals(questionList1, questionList1, "A QuestionList should be equal to itself");
		
		// Test inequality between two QuestionLists with different attributes
		assertNotEquals(questionList1, questionList3, "Two QuestionLists with different attributes should not be equal");
		
		// Test inequality with null
		assertNotEquals(null, questionList1, "A QuestionList should not be equal to null");
		
		// Test inequality with a different type
		assertNotEquals(questionList1, "Not a QuestionList", "A QuestionList should not be equal to an object of a different type");
	}
	
	@Test
	public void testQuestionListContains() {
		// Test that the collection contains a QuestionList that is equal to questionList1
		assertTrue(questionListCollection.contains(questionList1), "Collection should contain the exact same QuestionList object that was added");
		
		// Test that the collection contains a QuestionList that is equal to questionList2
		// This tests the equals method behavior in contains operation
		assertTrue(questionListCollection.contains(questionList2), "Collection should contain a QuestionList that equals the one that was added");
		
		// Test that the collection does not contain a QuestionList that is not equal to any in the collection
		assertFalse(questionListCollection.contains(questionList3), "Collection should not contain a QuestionList that wasn't added");
	}
	
	@Test
	public void testQuestionListHashCode() {
		// Test that equal QuestionLists have the same hash code
		assertEquals(questionList1, questionList1);
		// Test that different QuestionLists have different hash codes (not guaranteed, but likely)
		assertNotEquals(questionList1.hashCode(), questionList2.hashCode(),
			"QuestionLists with diffrent headers have diffrent hash code");
		assertNotEquals(questionList1.hashCode(), questionList3.hashCode(),
			"Different QuestionLists are likely to have different hash codes");
	}
	
	@Test
	public void testQuestionListInCollection() {
		// Create a new collection
		List<QuestionList> newCollection = new ArrayList<>();
		
		// Add questionList2 (which is equal to questionList1 but not the same object)
		newCollection.add(questionList2);
		
		// Test that the collection contains questionList1 (via equality)
		assertTrue(newCollection.contains(questionList1),
			"Collection should contain a QuestionList that equals the one we're checking for");
		
		// Create a copy of questionList1 with the same content 
		QuestionList questionList1Copy = new QuestionList.Builder().add(questionList1).build();
		
		// Test that the collection contains the copy (via equality)
		assertTrue(newCollection.contains(questionList1Copy),
			"Collection should contain a QuestionList that equals a copy with the same content");
	}
	
	@Test
	public void testQuestionListModification() {
		// Create a copy of questionList1
		QuestionList modifiedList = new QuestionList.Builder().add(questionList1).build();
		
		// At this point, modifiedList should equal questionList1
		assertTrue(questionListCollection.contains(modifiedList),
			"Before modification, collection should contain a QuestionList that equals the copy");
		
		// Modify the list by adding a new question
		modifiedList.add(new Question("What is the speed of light?",
			createSampleOptions("299,792,458 m/s", "150,000,000 m/s")));
		// After modification, the list should no longer be equal
		assertFalse(questionListCollection.contains(modifiedList),
			"After modification, collection should not contain the modified QuestionList");
	}
}
