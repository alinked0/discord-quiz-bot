import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.linked.quizbot.utils.Option;
import com.linked.quizbot.utils.Question;
import com.linked.quizbot.utils.QuestionList;
import com.linked.quizbot.utils.QuestionListParser;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.UnicodeEmoji;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class TestQuestionListParser {

    @TempDir
    File tempDir;

    // --- 1. Test fromJsonFile() ---
    @Test
    void testFromJsonFile_validJson() throws IOException {
        QuestionList original = QuestionList.getExampleQuestionList();
        File file = new File(tempDir, "list.json");
        original.exportListQuestionAsJson(file.getAbsolutePath());

        QuestionList parsed = QuestionListParser.fromJsonFile(file.getAbsolutePath());

        assertNotNull(parsed);
        assertEquals(original.size(), parsed.size());
        assertEquals(original.getAuthorId(), parsed.getAuthorId());
    }

    @Test
    void testFromJsonFile_missingFile() {
        File missing = new File(tempDir, "does_not_exist.json");
        QuestionList parsed = QuestionListParser.fromJsonFile(missing.getAbsolutePath());
        assertNull(parsed);
    }

    // --- 2. Test fromString() ---
    @Test
    void testFromString_validJson() {
        QuestionList list = QuestionList.getExampleQuestionList();
        QuestionList parsed = QuestionListParser.fromString(list.toJson());

        assertNotNull(parsed);
        assertEquals(list.size(), parsed.size());
    }

    @Test
    void testFromString_invalidJson() {
        String invalidJson = "{ invalid json }";
        QuestionList parsed = QuestionListParser.fromString(invalidJson);
        assertNull(parsed);
    }

    // --- 3. Test parser(JsonParser) ---
    @Test
    void testParser_directly() throws IOException {
        QuestionList original = QuestionList.getExampleQuestionList();
        JsonParser parser = new JsonFactory().createParser(original.toJson());
        QuestionList parsed = QuestionListParser.parser(parser);

        assertNotNull(parsed);
        assertEquals(original.getAuthorId(), parsed.getAuthorId());
    }

    // --- 4. Test parseTags() ---
    @Test
    void testParseTags_validTags() throws IOException {
        String json = "{ \"Science\": \"U+2697\", \"Math\": \"U+1F4C8\" }";
        JsonParser parser = new JsonFactory().createParser(json);
        parser.nextToken(); // move to START_OBJECT

        Map<String, UnicodeEmoji> tags = QuestionListParser.parseTags(parser);
        assertNotNull(tags);
        assertEquals(2, tags.size());
        assertTrue(tags.containsKey("Science"));
    }

    // --- 5. Test parseOption() ---
    @Test
    void testParseOption_valid() throws IOException {
        String json = "{ \"text\": \"42\", \"isCorrect\": true, \"explication\": \"The answer to everything\" }";
        JsonParser parser = new JsonFactory().createParser(json);
        parser.nextToken(); // move to START_OBJECT

        Option opt = QuestionListParser.parseOption(parser);
        assertNotNull(opt);
        assertTrue(opt.isCorrect());
        assertEquals("42", opt.getText());
    }

    // --- 6. Test parseOptionList() ---
    @Test
    void testParseOptionList_valid() throws IOException {
        String json = "[ "+new Option("Yes", true)+", "+new Option("No", false)+" ]";
        
        JsonParser parser = new JsonFactory().createParser(json);
        parser.nextToken(); // move to START_ARRAY

        List<Option> options = QuestionListParser.parseOptionList(parser);
        assertNotNull(options);
        assertEquals(2, options.size());
    }

    // --- 7. Test parseQuestion() ---
    @Test
    void testParseQuestion_valid() throws IOException {
        Question q = new Question("What is Java?", new Option("Language", true));
        q.setImageSrc("java.png");
        q.setExplication("A programming language");
        String json = q.toString();
        JsonParser parser = new JsonFactory().createParser(json);
        parser.nextToken(); // move to START_OBJECT

        Question question = QuestionListParser.parseQuestion(parser);
        assertNotNull(question);
        assertEquals("What is Java?", question.getQuestion());
        assertEquals(1, question.getOptions().size());

        String validQ = "{ \"question\": \"What is Java?\", \"options\": [ { \"text\": \"Lang\", \"isCorrect\": true } ], \"explication\": \"A language\", \"img_src\": \"img.png\" }";
        parser = new JsonFactory().createParser(validQ);
        parser.nextToken(); // move to START_OBJECT
        q = QuestionListParser.parseQuestion(parser);
        assertNotNull(q);
        assertEquals("What is Java?", q.getQuestion());
    }

    // --- 8. Test parseQuestionList() ---
    @Test
    void testParseQuestionList_valid() throws IOException {
        String json = """
        [
          {
            "question": "Q1?",
            "explication": "Exp1",
            "img_src": "img1.png",
            "options": [{"text":"T1", "isCorrect":true, "explication":"E1"}]
          },
          {
            "question": "Q2?",
            "explication": "Exp2",
            "img_src": "img2.png",
            "options": [{"text":"T2", "isCorrect":false, "explication":"E2"}]
          }
        ]
        """;
        JsonParser parser = new JsonFactory().createParser(json);
        parser.nextToken(); // move to START_ARRAY

        List<Question> questions = QuestionListParser.parseQuestionList(parser);
        assertNotNull(questions);
        assertEquals(2, questions.size());

        String valid = "[ { \"question\": \"Q1?\", \"options\": [ { \"text\": \"A\", \"isCorrect\": true } ] }, { \"question\": \"Q2?\", \"options\": [ { \"text\": \"B\", \"isCorrect\": false } ] } ]";
        parser = new JsonFactory().createParser(valid);
        parser.nextToken(); // move to START_ARRAY
        questions = QuestionListParser.parseQuestionList(parser);
        assertNotNull(questions);
        assertEquals(2, questions.size());
    }
}
