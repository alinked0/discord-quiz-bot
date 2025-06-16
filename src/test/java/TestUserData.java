import com.linked.quizbot.utils.QuestionList;
import com.linked.quizbot.utils.Question;
import com.linked.quizbot.utils.Users;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.linked.quizbot.Constants;

/**
 * Test class for Users functionality.
 * Tests the creation, modification, and management of user question lists.
 */
public class TestUserData {
    private static final String TEST_USER_ID = "tmp123456789";
    private Users userData;
    private QuestionList sampleList1;
    private QuestionList sampleList2;
    private QuestionList sampleList3;
    
    /**
     * Sets up the test environment before each test.
     * Creates sample QuestionList objects and a Users instance.
     */
    @BeforeEach
    public void setUp() {
        File f = new File(Constants.LISTSPATH + Constants.SEPARATOR + TEST_USER_ID);
        if (f.exists()){
            File[] files = f.listFiles();
            for(int i=0; i<files.length; i++){
                files[i].delete();
            }
            f.delete();
        }
        // Create sample question lists
        sampleList1 = new QuestionList(TEST_USER_ID, "Science Quiz");
        Question q1 = new Question("What is H2O?", 1, "Water", "Carbon Dioxide");
        Question q2 = new Question("What is the closest planet to the Sun?", 1, "Mercury", "Venus", "Earth");
        sampleList1.add(q1);
        sampleList1.add(q2);
        
        sampleList2 = new QuestionList(TEST_USER_ID, "History Quiz");
        Question q3 = new Question("Who was the first president of the United States?", 1, "George Washington", "Thomas Jefferson");
        sampleList2.add(q3);
        
        sampleList3 = new QuestionList(TEST_USER_ID, "Math Quiz");
        Question q4 = new Question("What is 2+2?", 1, "4", "5");
        sampleList3.add(q4);
        
        // Ensure the test directory exists
        new File(Constants.LISTSPATH + Constants.SEPARATOR + TEST_USER_ID).mkdirs();
        
        // Export the lists to JSON
        sampleList1.exportListQuestionAsJson();
        sampleList2.exportListQuestionAsJson();
        
        // Clear any existing Users for this test user
        Users.allUsers.removeIf(ul -> ul.getUserId().equals(TEST_USER_ID));
        
        // Create a Users instance for testing
        userData = new Users(TEST_USER_ID);
    }
    
    /**
     * Cleans up after each test by removing test files.
     */
    @AfterEach
    public void tearDown() {
        // Delete the test files
        File f = new File(Constants.LISTSPATH + Constants.SEPARATOR + TEST_USER_ID);
        if (f.exists()){
            File[] files = f.listFiles();
            for(int i=0; i<files.length; i++){
                files[i].delete();
            }
            f.delete();
        }
        
        // Clear any Users for this test user
        Users.allUsers.removeIf(ul -> ul.getUserId().equals(TEST_USER_ID));
    }
    
    /**
     * Tests the construction of Users and initialization of attributes.
     */
    @Test
    public void testUserListsConstruction() {
        // Check user ID is set correctly
        assertEquals(TEST_USER_ID, userData.getUserId());
        
        // Check lists are loaded correctly
        List<QuestionList> allLists = userData.getLists();
        assertNotNull(allLists);
        assertEquals(2, allLists.size());
        
        // Check all lists have the user ID set correctly
        for (QuestionList list : allLists) {
            assertEquals(TEST_USER_ID, list.getAuthorId());
        }
    }
    
    /**
     * Tests the collection constructor for Users.
     */
    @Test
    public void testUserListsCollectionConstructor() {
        List<QuestionList> testLists = new ArrayList<>();
        testLists.add(sampleList1);
        testLists.add(sampleList2);
        
        Users collectionUserLists = new Users(TEST_USER_ID, testLists);
        
        assertEquals(TEST_USER_ID, collectionUserLists.getUserId());
        assertEquals(2, collectionUserLists.getLists().size());
        assertTrue(collectionUserLists.getLists().contains(sampleList1));
        assertTrue(collectionUserLists.getLists().contains(sampleList2));
    }
    
    /**
     * Tests adding a new list to a user.
     */
    @Test
    public void testAddList() {
        // Add a new list
        userData.addList(sampleList3);
        
        // Check list was added
        List<QuestionList> allLists = userData.getLists();
        assertEquals(3, allLists.size());
        assertTrue(allLists.contains(sampleList3));
    }
    
    /**
     * Tests adding a list with the same name but different content.
     */
    @Test
    public void testAddListWithSameName() {
        // Create a list with the same name but different content
        QuestionList sameNameList = new QuestionList(TEST_USER_ID, "Science Quiz");
        Question q5 = new Question("What is the speed of light?", 1, "299,792,458 m/s", "150,000 m/s");
        sameNameList.add(q5);
        int sizeBeforeLists = userData.getLists().size();
        int sizeBeforeQuestionList = userData.getQuestionListByName("Science Quiz").size();
        
        // Add the list
        userData.addList(sameNameList);

        // Check the list was merged with the existing one
        List<QuestionList> allLists = userData.getLists();
        assertEquals(sizeBeforeLists, allLists.size());
        
        // Find the science quiz
        QuestionList mergedList = userData.getQuestionListByName("Science Quiz");
        
        assertNotNull(mergedList);
        assertEquals(sizeBeforeQuestionList+1, mergedList.size());  // Should have 3 questions now
    }
    
    /**
     * Tests the static method for adding a list to a user.
     */
    @Test
    public void testStaticAddListToUser() {
        // Add a list using the static method
        userData.addListToUser(TEST_USER_ID, sampleList3);
        
        // Create a new Users instance to verify the list was added
        Users reloadedUserLists = new Users(TEST_USER_ID);
        
        // Check list was added
        List<QuestionList> allLists = reloadedUserLists.getLists();
        assertEquals(3, allLists.size());
        
        // Check for the specific list
        boolean foundList = false;
        for (QuestionList list : allLists) {
            if (list.getName().equals("Math Quiz")) {
                foundList = true;
                break;
            }
        }
        assertTrue(foundList);
    }
    
    /**
     * Tests the binary search method for strings.
     */
    @Test
    public void testBinarySearchForStrings() {
        List<String> sortedList = Arrays.asList("Apple", "Banana", "Cherry", "Date", "Elderberry");
        
        // Test finding existing elements
        assertEquals(0, Users.myBinarySearchIndexOf(sortedList, 0, sortedList.size() - 1, "Apple", (e,f)-> e.compareTo(f)));
        assertEquals(2, Users.myBinarySearchIndexOf(sortedList, 0, sortedList.size() - 1, "Cherry", (e,f)-> e.compareTo(f)));
        assertEquals(4, Users.myBinarySearchIndexOf(sortedList, 0, sortedList.size() - 1, "Elderberry", (e,f)-> e.compareTo(f)));
        
        // Test for element that doesn't exist but should be at a specific position
        int expectedPosition = -3; // -position-1 where position is 2
        assertEquals(expectedPosition, Users.myBinarySearchIndexOf(sortedList, 0, sortedList.size() - 1, "Carrot", (e,f)-> e.compareTo(f)));
    }
    
    /**
     * Tests the binary search method for QuestionList objects.
     */
    @Test
    public void testBinarySearchForQuestionLists() {
        List<QuestionList> sortedLists = new ArrayList<>();
        sortedLists.add(new QuestionList(TEST_USER_ID, "A Quiz"));
        sortedLists.add(new QuestionList(TEST_USER_ID, "B Quiz"));
        sortedLists.add(new QuestionList(TEST_USER_ID, "C Quiz"));
        
        // Test finding existing elements
        QuestionList testList = new QuestionList(TEST_USER_ID, "B Quiz");
        assertEquals(1, Users.myBinarySearchIndexOf(sortedLists, 0, sortedLists.size() - 1, testList, QuestionList.comparatorByName()));
        
        // Test for element that doesn't exist
        QuestionList missingList = new QuestionList(TEST_USER_ID, "D Quiz");
        int result = Users.myBinarySearchIndexOf(sortedLists, 0, sortedLists.size() - 1, missingList, QuestionList.comparatorByName());
        assertTrue(result < 0); // Should be negative for not found
    }
    
    /**
     * Tests the deletion of a list.
     */
    @Test
    public void testDeleteList() {
        // Delete a list
        Users.deleteList(sampleList1);
        
        // Create a new Users instance to verify the list was deleted
        Users reloadedUserLists = new Users(TEST_USER_ID);
        
        // Check list was deleted
        List<QuestionList> allLists = reloadedUserLists.getLists();
        assertEquals(1, allLists.size());
        assertFalse(allLists.contains(sampleList1));
    }
    
    /**
     * Tests exporting all user lists.
     */
    @Test
    public void testExportAllUserLists() {
        // Add a new list without exporting it
        QuestionList newList = new QuestionList(TEST_USER_ID, "New Quiz");
        Question q5 = new Question("What is the capital of France?", 1, "Paris", "London");
        newList.add(q5);
        
        // Add the list without exporting
        userData.addList(newList);
        
        // Get the path to where the file should be
        String path = newList.getPathToList();
        File file = new File(path);
        
        // Verify the file exists (it should have been exported when added)
        assertTrue(file.exists());
        
        // Delete the file to simulate it not being exported
        file.delete();
        assertFalse(file.exists());
        
        // Export all user lists
        Users.exportAllUserLists();
        
        // Verify the file exists again
        assertTrue(file.exists());
    }
    
    /**
     * Tests the iterator functionality.
     */
    @Test
    public void testIterator() {
        int count = 0;
        for (QuestionList list : userData) {
            assertNotNull(list);
            count++;
        }
        
        assertEquals(2, count);
    }
    
    /**
     * Tests the equals and hashCode methods.
     */
    @Test
    public void testEqualsAndHashCode() {
        // Create another Users instance with the same user ID
        Users sameUserLists = new Users(TEST_USER_ID);
        
        // Check equals
        assertEquals(userData, sameUserLists);
        assertEquals(userData, TEST_USER_ID);
        
        // Check hashCode
        assertEquals(userData.hashCode(), sameUserLists.hashCode());
        
        // Create a Users with a different user ID
        Users differentUserLists = new Users("987654321");
        
        // Check not equals
        assertNotEquals(userData, differentUserLists);
        assertNotEquals(userData, "987654321");
        assertNotEquals(userData, new Object());
    }
}