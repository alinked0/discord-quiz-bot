import com.linked.quizbot.utils.QuestionList;
import com.linked.quizbot.utils.Question;
import com.linked.quizbot.utils.UserLists;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.linked.quizbot.Constants;

/**
 * Test class for UserLists functionality.
 * Tests the creation, modification, and management of user question lists.
 */
public class TestUserLists {
    private static final String TEST_USER_ID = "tmp123456789";
    private UserLists userLists;
    private QuestionList sampleList1;
    private QuestionList sampleList2;
    private QuestionList sampleList3;
    
    /**
     * Sets up the test environment before each test.
     * Creates sample QuestionList objects and a UserLists instance.
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
        sampleList1 = new QuestionList(TEST_USER_ID, "Science Quiz", "Science");
        Question q1 = new Question("What is H2O?", 1, "Water", "Carbon Dioxide");
        Question q2 = new Question("What is the closest planet to the Sun?", 1, "Mercury", "Venus", "Earth");
        sampleList1.add(q1);
        sampleList1.add(q2);
        
        sampleList2 = new QuestionList(TEST_USER_ID, "History Quiz", "History");
        Question q3 = new Question("Who was the first president of the United States?", 1, "George Washington", "Thomas Jefferson");
        sampleList2.add(q3);
        
        sampleList3 = new QuestionList(TEST_USER_ID, "Math Quiz", "Science");
        Question q4 = new Question("What is 2+2?", 1, "4", "5");
        sampleList3.add(q4);
        
        // Ensure the test directory exists
        new File(Constants.LISTSPATH + Constants.SEPARATOR + TEST_USER_ID).mkdirs();
        
        // Export the lists to JSON
        sampleList1.exportListQuestionAsJson();
        sampleList2.exportListQuestionAsJson();
        
        // Clear any existing UserLists for this test user
        UserLists.allUserLists.removeIf(ul -> ul.getUserId().equals(TEST_USER_ID));
        
        // Create a UserLists instance for testing
        userLists = new UserLists(TEST_USER_ID);
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
        
        // Clear any UserLists for this test user
        UserLists.allUserLists.removeIf(ul -> ul.getUserId().equals(TEST_USER_ID));
    }
    
    /**
     * Tests the construction of UserLists and initialization of attributes.
     */
    @Test
    public void testUserListsConstruction() {
        // Check user ID is set correctly
        assertEquals(TEST_USER_ID, userLists.getUserId());
        
        // Check lists are loaded correctly
        List<QuestionList> allLists = userLists.getAllLists();
        assertNotNull(allLists);
        assertEquals(2, allLists.size());
        
        // Check all lists have the user ID set correctly
        for (QuestionList list : allLists) {
            assertEquals(TEST_USER_ID, list.getAuthorId());
        }
    }
    
    /**
     * Tests the collection constructor for UserLists.
     */
    @Test
    public void testUserListsCollectionConstructor() {
        List<QuestionList> testLists = new ArrayList<>();
        testLists.add(sampleList1);
        testLists.add(sampleList2);
        
        UserLists collectionUserLists = new UserLists(TEST_USER_ID, testLists);
        
        assertEquals(TEST_USER_ID, collectionUserLists.getUserId());
        assertEquals(2, collectionUserLists.getAllLists().size());
        assertTrue(collectionUserLists.getAllLists().contains(sampleList1));
        assertTrue(collectionUserLists.getAllLists().contains(sampleList2));
    }
    
    /**
     * Tests the theme initialization and organization of lists by theme.
     */
    @Test
    public void testThemeInitialization() {
        // Check themes are extracted correctly
        List<String> themes = userLists.getAllThemes();
        assertEquals(2, themes.size());
        assertTrue(themes.contains("Science"));
        assertTrue(themes.contains("History"));
        
        // Check lists are organized by theme correctly
        Map<String, List<QuestionList>> listsByTheme = userLists.getListsByTheme();
        assertEquals(2, listsByTheme.size());
        assertEquals(1, listsByTheme.get("Science").size());
        assertEquals(1, listsByTheme.get("History").size());
        assertEquals("Science Quiz", listsByTheme.get("Science").get(0).getName());
        assertEquals("History Quiz", listsByTheme.get("History").get(0).getName());
    }
    
    /**
     * Tests adding a new list to a user.
     */
    @Test
    public void testAddList() {
        // Add a new list
        userLists.addList(sampleList3);
        
        // Check list was added
        List<QuestionList> allLists = userLists.getAllLists();
        assertEquals(3, allLists.size());
        assertTrue(allLists.contains(sampleList3));
        
        // Check theme organization was updated
        Map<String, List<QuestionList>> listsByTheme = userLists.getListsByTheme();
        assertEquals(2, listsByTheme.get(sampleList3.getTheme()).size());
    }
    
    /**
     * Tests adding a list with the same name but different content.
     */
    @Test
    public void testAddListWithSameName() {
        // Create a list with the same name but different content
        QuestionList sameNameList = new QuestionList(TEST_USER_ID, "Science Quiz", "Science");
        Question q5 = new Question("What is the speed of light?", 1, "299,792,458 m/s", "150,000 m/s");
        sameNameList.add(q5);
        
        // Add the list
        userLists.addList(sameNameList);
        
        // Check the list was merged with the existing one
        List<QuestionList> allLists = userLists.getAllLists();
        assertEquals(2, allLists.size());
        
        // Find the science quiz
        QuestionList mergedList = null;
        for (QuestionList list : allLists) {
            if (list.getName().equals("Science Quiz")) {
                mergedList = list;
                break;
            }
        }
        
        assertNotNull(mergedList);
        assertEquals(3, mergedList.size());  // Should have 3 questions now
    }
    
    /**
     * Tests the static method for adding a list to a user.
     */
    @Test
    public void testStaticAddListToUser() {
        // Add a list using the static method
        UserLists.addListToUser(TEST_USER_ID, sampleList3);
        
        // Create a new UserLists instance to verify the list was added
        UserLists reloadedUserLists = new UserLists(TEST_USER_ID);
        
        // Check list was added
        List<QuestionList> allLists = reloadedUserLists.getAllLists();
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
     * Tests getting the code for the index of a question list.
     */
    @Test
    public void testGetCodeForIndexQuestionList() {
        // Get the code for a list
        String code = userLists.getCodeForIndexQuestionList(sampleList1);
        
        // Check the code format (theme index + 1) + " " + (list index + 1)
        assertNotNull(code);
        
        // Split the code into parts
        String[] parts = code.split(" ");
        assertEquals(2, parts.length);
        
        // Convert to integers
        int themeIndex = Integer.parseInt(parts[0]);
        int listIndex = Integer.parseInt(parts[1]);
        
        // Check the indices are valid
        assertTrue(themeIndex > 0 && themeIndex <= userLists.getAllThemes().size());
        
        // Get the theme from the index
        String theme = userLists.getAllThemes().get(themeIndex - 1);
        List<QuestionList> listsInTheme = userLists.getListsByTheme(theme);
        
        assertTrue(listIndex > 0 && listIndex <= listsInTheme.size());
        
        // Check the list at the index is the one we asked for
        assertEquals(sampleList1, listsInTheme.get(listIndex - 1));
    }
    
    /**
     * Tests the static method for getting the code for the index of a question list.
     */
    @Test
    public void testStaticGetCodeForIndexQuestionList() {
        // Get the code using the static method
        String code = UserLists.getCodeForIndexQuestionList(sampleList1, TEST_USER_ID);
        
        // Check the code is not null
        assertNotNull(code);
        
        // Split the code into parts
        String[] parts = code.split(" ");
        assertEquals(2, parts.length);
    }
    
    /**
     * Tests the binary search method for strings.
     */
    @Test
    public void testBinarySearchForStrings() {
        List<String> sortedList = Arrays.asList("Apple", "Banana", "Cherry", "Date", "Elderberry");
        
        // Test finding existing elements
        assertEquals(0, UserLists.myBinarySearchIndexOf(sortedList, 0, sortedList.size() - 1, "Apple"));
        assertEquals(2, UserLists.myBinarySearchIndexOf(sortedList, 0, sortedList.size() - 1, "Cherry"));
        assertEquals(4, UserLists.myBinarySearchIndexOf(sortedList, 0, sortedList.size() - 1, "Elderberry"));
        
        // Test for element that doesn't exist but should be at a specific position
        int expectedPosition = -3; // -position-1 where position is 2
        assertEquals(expectedPosition, UserLists.myBinarySearchIndexOf(sortedList, 0, sortedList.size() - 1, "Carrot"));
    }
    
    /**
     * Tests the binary search method for QuestionList objects.
     */
    @Test
    public void testBinarySearchForQuestionLists() {
        List<QuestionList> sortedLists = new ArrayList<>();
        sortedLists.add(new QuestionList(TEST_USER_ID, "A Quiz", "Test"));
        sortedLists.add(new QuestionList(TEST_USER_ID, "B Quiz", "Test"));
        sortedLists.add(new QuestionList(TEST_USER_ID, "C Quiz", "Test"));
        
        // Test finding existing elements
        QuestionList testList = new QuestionList(TEST_USER_ID, "B Quiz", "Test");
        assertEquals(1, UserLists.myBinarySearchIndexOf(sortedLists, 0, sortedLists.size() - 1, testList));
        
        // Test for element that doesn't exist
        QuestionList missingList = new QuestionList(TEST_USER_ID, "D Quiz", "Test");
        int result = UserLists.myBinarySearchIndexOf(sortedLists, 0, sortedLists.size() - 1, missingList);
        assertTrue(result < 0); // Should be negative for not found
    }
    
    /**
     * Tests the deletion of a list.
     */
    @Test
    public void testDeleteList() {
        // Delete a list
        UserLists.deleteList(sampleList1);
        
        // Create a new UserLists instance to verify the list was deleted
        UserLists reloadedUserLists = new UserLists(TEST_USER_ID);
        
        // Check list was deleted
        List<QuestionList> allLists = reloadedUserLists.getAllLists();
        assertEquals(1, allLists.size());
        assertFalse(allLists.contains(sampleList1));
        
        // Check theme organization was updated if needed
        List<String> themes = reloadedUserLists.getAllThemes();
        assertEquals(1, themes.size());
        assertEquals("History", themes.get(0));
    }
    
    /**
     * Tests exporting all user lists.
     */
    @Test
    public void testExportAllUserLists() {
        // Add a new list without exporting it
        QuestionList newList = new QuestionList(TEST_USER_ID, "New Quiz", "New");
        Question q5 = new Question("What is the capital of France?", 1, "Paris", "London");
        newList.add(q5);
        
        // Add the list without exporting
        userLists.addList(newList);
        
        // Get the path to where the file should be
        String path = newList.getPathToList();
        File file = new File(path);
        
        // Verify the file exists (it should have been exported when added)
        assertTrue(file.exists());
        
        // Delete the file to simulate it not being exported
        file.delete();
        assertFalse(file.exists());
        
        // Export all user lists
        UserLists.exportAllUserLists();
        
        // Verify the file exists again
        assertTrue(file.exists());
    }
    
    /**
     * Tests the iterator functionality.
     */
    @Test
    public void testIterator() {
        int count = 0;
        for (QuestionList list : userLists) {
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
        // Create another UserLists instance with the same user ID
        UserLists sameUserLists = new UserLists(TEST_USER_ID);
        
        // Check equals
        assertEquals(userLists, sameUserLists);
        assertEquals(userLists, TEST_USER_ID);
        
        // Check hashCode
        assertEquals(userLists.hashCode(), sameUserLists.hashCode());
        
        // Create a UserLists with a different user ID
        UserLists differentUserLists = new UserLists("987654321");
        
        // Check not equals
        assertNotEquals(userLists, differentUserLists);
        assertNotEquals(userLists, "987654321");
        assertNotEquals(userLists, new Object());
    }
>>>>>>> parent of ad9647e (No modification)
}