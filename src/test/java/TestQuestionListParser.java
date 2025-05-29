import com.linked.quizbot.Constants;
import com.linked.quizbot.utils.QuestionList;
import com.linked.quizbot.utils.UserLists;
import com.linked.quizbot.utils.Question;
import com.linked.quizbot.utils.Option;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for QuestionList import/export functionality.
 * Tests the serialization and deserialization of QuestionList objects to/from JSON.
 */
public class TestQuestionListParser {

    @TempDir
    Path tempDir;
    File tempFile = new File("src"+Constants.SEPARATOR+"test"+Constants.SEPARATOR+"java"+Constants.SEPARATOR+"tmp");

    private QuestionList originalList;
    private static final String AUTHOR_ID = "test_author";
    private static final String LIST_NAME = "Test Quiz";
    private static final String THEME = "Science";
    private File jsonFile;

    @BeforeEach
    void setUp() throws IOException {
        // Create a sample QuestionList with various test cases
        originalList = createSampleQuestionList();
        
        // Create temporary file for testing
        jsonFile = new File(tempDir.toFile(), "test_questions.json");
    }
    /**
     * Cleans up after each test by removing test files.
     */
    @AfterEach
    public void tearDown() {
        // Delete the test file
        File f = tempFile;
        if (f.exists()){
            f.delete();
        }
        f = jsonFile;
        if (f.exists()){
            f.delete();
        }
        f = tempDir.toFile();
        if (f.exists()){
            f.delete();
        }
        if (originalList != null){
            f = new File(originalList.getPathToList());
            if (f.exists()){
                f.delete();
            }
        }
    }

    /**
     * Creates a sample QuestionList with various test cases
     */
    private QuestionList createSampleQuestionList() {
        QuestionList list = new QuestionList(AUTHOR_ID, LIST_NAME, THEME);

        // Add a simple question with basic options
        Question q1 = new Question("What is H2O?", createOptions(
            new String[]{"Water", "Carbon Dioxide"},
            new boolean[]{true, false},
            new String[]{"Correct! H2O is the chemical formula for water", "Incorrect. CO2 is carbon dioxide"}
        ));
        list.add(q1);

        // Add a question with an image source
        Question q2 = new Question("Identify this element's atomic structure:", createOptions(
            new String[]{"Hydrogen", "Helium"},
            new boolean[]{false, true},
            new String[]{"Incorrect", "Correct!"}
        ));
        q2.setImageSrc("atomic_structure.png");
        q2.setExplication("This is the atomic structure of Helium");
        list.add(q2);

        // Add a question with multiple options
        Question q3 = new Question("Select all noble gases:", createOptions(
            new String[]{"Helium", "Oxygen", "Neon", "Nitrogen"},
            new boolean[]{true, false, true, false},
            new String[]{"Correct!", "Not a noble gas", "Correct!", "Not a noble gas"}
        ));
        list.add(q3);

        return list;
    }

    /**
     * Helper method to create options for questions
     */
    private LinkedList<Option> createOptions(String[] texts, boolean[] corrects, String[] explanations) {
        LinkedList<Option> options = new LinkedList<>();
        for (int i = 0; i < texts.length; i++) {
            options.add(new Option(texts[i], corrects[i], explanations[i]));
        }
        return options;
    }

    @Test
    void testExportAndImport() throws IOException {
        // Export the original list
        System.out.println(originalList);
        originalList.exportListQuestionAsJson(tempFile.getPath());

        // Import the list back
        QuestionList importedList = QuestionList.importListQuestionFromJson(tempFile.getPath());
        System.out.println(importedList);
        // Verify the metadata
        assertEquals(originalList.getAuthorId(), importedList.getAuthorId(), "Author ID should match");
        assertEquals(originalList.getName(), importedList.getName(), "List name should match");
        assertEquals(originalList.getTheme(), importedList.getTheme(), "Theme should match");

        // Verify the number of questions
        assertEquals(originalList.size(), importedList.size(), "Number of questions should match");

        // Verify each question's content
        for (int i = 0; i < originalList.size(); i++) {
            Question originalQ = originalList.get(i);
            Question importedQ = importedList.get(i);

            assertEquals(originalQ.getQuestion(), importedQ.getQuestion(), 
                "Question text should match for question " + (i + 1));
            assertEquals(originalQ.getImageSrc(), importedQ.getImageSrc(), 
                "Image source should match for question " + (i + 1));
            assertEquals(originalQ.getExplication(), importedQ.getExplication(), 
                "Explanation should match for question " + (i + 1));

            // Verify options
            assertEquals(originalQ.getOptions().size(), importedQ.getOptions().size(), 
                "Number of options should match for question " + (i + 1));

            for (Option originalOpt : originalQ.getOptions()) {
                Option importedOpt = importedQ.getOptions().get(importedQ.getOptions().indexOf(originalOpt));
                assertEquals(originalOpt.getText(), importedOpt.getText());
                assertEquals(originalOpt.isCorrect(), importedOpt.isCorrect());
                assertEquals(originalOpt.getExplication(), importedOpt.getExplication());
            }
        }
        tempFile.delete();
    }

    @Test
    void testImportNonexistentFile() {
        File nonexistentFile = new File(tempDir.toFile(), "nonexistent.json");
        QuestionList importedList = new QuestionList(nonexistentFile.getPath());
        
        // Verify that importing a non-existent file results in an empty list with null fields
        assertNotNull(importedList, "Should create an empty list even when file doesn't exist");
        assertEquals(0, importedList.size(), "List should be empty");
        assertNull(importedList.getAuthorId(), "Author ID should be null");
        assertNull(importedList.getName(), "Name should be null");
        assertNull(importedList.getTheme(), "Theme should be null");
    }

    @Test
    void testExportToInvalidPath() {
        QuestionList list = new QuestionList(AUTHOR_ID, LIST_NAME, THEME);
        File invalidPath = new File("/invalid/path/test.json");
        
        // No exception should be thrown, but the file should not be created
        list.exportListQuestionAsJson();
        assertFalse(invalidPath.exists(), "File should not be created at invalid path");
    }

    @Test
    void testImportInvalidJson(){
        // Create an invalid JSON file
        String invalidJson = "{ invalid json content }";
        File f = new File(tempDir.toFile(),"invalid");
        QuestionList.getExampleQuestionList().exportListQuestionAsJson(f.getAbsolutePath());
        try {
            BufferedWriter buff = Files.newBufferedWriter(Paths.get(f.getAbsolutePath()));
            buff.write(invalidJson);
            buff.close();
		} catch (IOException e) {
			System.err.println("$> An error occurred while exporting a List of questions.");
			e.printStackTrace();
		}
        // Attempting to import invalid JSON should result in an empty list
        QuestionList importedList = new QuestionList(jsonFile.getPath());
        
        assertEquals(0, importedList.size(), "Invalid JSON should result in empty list");
        assertNull(importedList.getAuthorId(), "Author ID should be null for invalid JSON");
        assertNull(importedList.getName(), "Name should be null for invalid JSON");
        assertNull(importedList.getTheme(), "Theme should be null for invalid JSON");
        f.delete();
        jsonFile.delete();
    }
}