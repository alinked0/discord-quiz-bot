
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.linked.quizbot.utils.Attempt;
import com.linked.quizbot.utils.Awnser;
import com.linked.quizbot.utils.Option;
import com.linked.quizbot.utils.Question;
import com.linked.quizbot.utils.QuestionList;

public class AttemptTest {
	
	private static final String USER_ID = "testUser123";
	private QuestionList smallQuestionList;
	private Long fixedStartTime;
	private Long fixedEndTime;
	private Map<Integer, Awnser> awnsers;
	
	private Set<Option> createOptionSet(boolean correctOption, boolean incorrectOption) {
		Set<Option> options = new HashSet<>();
		if (correctOption) {
			options.add(new Option("Correct", true));
		}
		if (incorrectOption) {
			options.add(new Option("Incorrect", false));
		}
		return options;
	}
	
	@BeforeEach
	void setUp() {
		Option optTrue = new Option("True option", true);
		Option optFalse = new Option("False option", false);
		Question q = new Question("Choose wisely A", optTrue, optTrue, optFalse);
		smallQuestionList = new QuestionList.Builder().ownerId("Test00000").name("mock questions").add(q).build();
		q = new Question("Choose wisely B", optTrue, optFalse);
		smallQuestionList.add(q);
		q = new Question("Choose wisely C", optTrue, optTrue, optFalse);
		smallQuestionList.add(q);
		fixedStartTime = 1635338400000L;
		fixedEndTime = 1635338460000L;
		
		awnsers = new HashMap<>();
		Set<Option> awnserQ0 = new HashSet<>();
		awnserQ0.add(optTrue);
		awnserQ0.add(optFalse);
		awnsers.put(0, new Awnser(1000L, awnserQ0));
		Set<Option> awnserQ1 = new HashSet<>();
		awnserQ1.add(optTrue);
		awnsers.put(1, new Awnser(2000L, awnserQ1));
	}
	
	
	@Test
	void testConstructor_Full() {
		Attempt attempt = new Attempt(USER_ID, smallQuestionList, fixedStartTime, fixedEndTime, awnsers);
		
		assertNotNull(attempt);
		assertEquals(USER_ID, attempt.getUserId());
		assertEquals(smallQuestionList, attempt.getQuestionList());
		assertEquals(fixedStartTime, attempt.getStart());
		assertEquals(fixedEndTime, attempt.getEnd());
		assertEquals(awnsers, attempt.getAwnsers());
	}
	
	@Test
	void testConstructor_Minimal() {
		long before = System.currentTimeMillis();
		Attempt attempt = new Attempt(USER_ID, smallQuestionList);
		long after = System.currentTimeMillis();
		
		assertNotNull(attempt);
		assertEquals(USER_ID, attempt.getUserId());
		assertEquals(smallQuestionList, attempt.getQuestionList());
		assertTrue(attempt.getStart() >= before && attempt.getStart() <= after);
		assertEquals(0L, attempt.getEnd());
		assertNotNull(attempt.getAwnsers());
		assertTrue(attempt.getAwnsers().isEmpty());
	}
	
	
	@Test
	void testGetQuestionList() {
		Attempt attempt = new Attempt(USER_ID, smallQuestionList, fixedStartTime, fixedEndTime, new HashMap<>());
		assertEquals(smallQuestionList, attempt.getQuestionList());
	}
	
	@Test
	void testGetUserId() {
		Attempt attempt = new Attempt(USER_ID, smallQuestionList, fixedStartTime, fixedEndTime, new HashMap<>());
		assertEquals(USER_ID, attempt.getUserId());
	}
	
	@Test
	void testGetStart() {
		Attempt attempt = new Attempt(USER_ID, smallQuestionList, fixedStartTime, fixedEndTime, new HashMap<>());
		assertEquals(fixedStartTime, attempt.getStart());
	}
	
	@Test
	void testGetEnd() {
		Attempt attempt = new Attempt(USER_ID, smallQuestionList, fixedStartTime, fixedEndTime, new HashMap<>());
		assertEquals(fixedEndTime, attempt.getEnd());
	}
	
	@Test
	void testGetDuration() {
		Attempt attempt = new Attempt(USER_ID, smallQuestionList, fixedStartTime, fixedEndTime, new HashMap<>());
		Long expectedDuration = fixedEndTime - fixedStartTime;
		assertEquals(expectedDuration, attempt.getDuration());
	}
	
	@Test
	void testGetAwnsers() {
		Attempt attempt = new Attempt(USER_ID, smallQuestionList, fixedStartTime, fixedEndTime, awnsers);
		assertEquals(awnsers, attempt.getAwnsers());
		assertSame(awnsers, attempt.getAwnsers());
	}
	
	
	@Test
	void testAddAwnser_NewEntry() {
		Attempt attempt = new Attempt(USER_ID, smallQuestionList, fixedStartTime, fixedEndTime, new HashMap<>());
		int questionIndex = 5;
		Set<Option> response = createOptionSet(true, false);
		Long duration = 1500L;
		
		Attempt result = attempt.addAwnser(questionIndex, response.iterator().next(), duration);
		
		assertSame(attempt, result);
		assertEquals(1, attempt.getAwnsers().size());
		Awnser addedAwnser = attempt.getAwnsers().get(questionIndex);
		assertNotNull(addedAwnser);
		assertEquals(response, addedAwnser.getResponses());
	}
	
	@Test
	void testAddAwnser_OverwriteEntry() {
		Map<Integer, Awnser> initialAwnsers = new HashMap<>();
		initialAwnsers.put(0, new Awnser(1L, createOptionSet(false, false)));
		Attempt attempt = new Attempt(USER_ID, smallQuestionList, fixedStartTime, fixedEndTime, initialAwnsers);
		
		int questionIndex = 0;
		Set<Option> newResponse = createOptionSet(true, true);
		Long newDuration = 5000L;
		
		newResponse.stream().forEach(r -> attempt.addAwnser(questionIndex, r, newDuration));
		
		assertEquals(1, attempt.getAwnsers().size());
		Awnser updatedAwnser = attempt.getAwnsers().get(questionIndex);
		assertNotNull(updatedAwnser);
		assertEquals(newResponse, updatedAwnser.getResponses());
	}
	
	@Test
	void testEnd() {
		Attempt attempt = new Attempt(USER_ID, smallQuestionList, fixedStartTime, 0L, new HashMap<>());
		
		long before = System.currentTimeMillis();
		Attempt result = attempt.end();
		long after = System.currentTimeMillis();
		
		assertSame(attempt, result);
		assertTrue(attempt.getEnd() >= before && attempt.getEnd() <= after);
		assertTrue(attempt.getDuration() > 0);
	}
	
	
	@Test
	void testGetScore_FullCalculation() {
		Attempt attempt = new Attempt(USER_ID, smallQuestionList, fixedStartTime, fixedEndTime, awnsers);
		
		Double expectedScore = 1.25;
		assertEquals(expectedScore, attempt.getScore(), 0.0001);
	}
	
	@Test
	void testGetScore_NoAwnsers() {
		Attempt attempt = new Attempt(USER_ID, smallQuestionList, fixedStartTime, fixedEndTime, new HashMap<>());
		assertEquals(0.0, attempt.getScore(), 0.0001);
	}
	
	@Test
	void testGetScore_AwnserIsNull() {
		Map<Integer, Awnser> mapWithNull = new HashMap<>();
		mapWithNull.put(0, null);
		
		Attempt attempt = new Attempt(USER_ID, smallQuestionList, fixedStartTime, fixedEndTime, mapWithNull);
		assertEquals(0.0, attempt.getScore(), 0.0001);
	}
	
	@Test
	void testHashCode_Equality() {
		Attempt attempt1 = new Attempt(USER_ID, smallQuestionList, fixedStartTime, fixedEndTime, awnsers);
		Attempt attempt2 = new Attempt(USER_ID, smallQuestionList, fixedStartTime, fixedEndTime, awnsers);
		
		assertEquals(attempt1.hashCode(), attempt2.hashCode());
	}
	
	@Test
	void testHashCode_Difference() {
		Attempt attempt1 = new Attempt(USER_ID, smallQuestionList, fixedStartTime, fixedEndTime, awnsers);
		Attempt attempt2 = new Attempt("otherUser", smallQuestionList, fixedStartTime, fixedEndTime, awnsers);
		
		assertNotEquals(attempt1.hashCode(), attempt2.hashCode());
	}
	
	@Test
	void testEquals_True() {
		Attempt attempt1 = new Attempt(USER_ID, smallQuestionList, fixedStartTime, fixedEndTime, awnsers);
		Attempt attempt2 = new Attempt(USER_ID, smallQuestionList, fixedStartTime, fixedEndTime, awnsers);
		assertTrue(attempt1.equals(attempt1));
		assertTrue(attempt1.equals(attempt2));
		assertTrue(attempt2.equals(attempt1));
	}
	
	@Test
	void testEquals_False() {
		Attempt attempt1 = new Attempt(USER_ID, smallQuestionList, fixedStartTime, fixedEndTime, awnsers);
		Attempt attempt2 = new Attempt(USER_ID, smallQuestionList, fixedStartTime + 1, fixedEndTime, awnsers);
		assertFalse(attempt1.equals(attempt2));
		Attempt attempt3 = new Attempt("DifferentUser", smallQuestionList, fixedStartTime, fixedEndTime, awnsers);
		assertFalse(attempt1.equals(attempt3));
		assertFalse(attempt1.equals(new Object()));
		assertFalse(attempt1.equals(null));
	}
	
	@Test
	void testGetTextPoints_TimeInSeconds() {
		assertEquals(smallQuestionList.size(), 3);
		Attempt attempt = new Attempt(USER_ID, smallQuestionList, fixedStartTime, fixedEndTime, awnsers);
		String result = attempt.getTextPoints();
		assertTrue(smallQuestionList.get(0).getOptions().containsAll(attempt.getAwnsers().get(0).getResponses()) );
		assertTrue(result.startsWith("` 42%` ` 60 s`"), "Should show score and seconds time"+result);
	}
	
	@Test
	void testGetTextPoints_TimeInMinutes() {
		Long longEndTime = fixedStartTime + 600000L;
		Attempt attempt = new Attempt(USER_ID, smallQuestionList, fixedStartTime, longEndTime, awnsers);
		
		String result = attempt.getTextPoints();
		assertTrue(result.startsWith("` 42%` ` 10m`"), "Should show score and minutes time"+result);
	}
	
	@Test
	void testToJson() {
		Attempt attempt = new Attempt(USER_ID, smallQuestionList, fixedStartTime, fixedEndTime, awnsers);
		
		String result = attempt.toJson();
		String expectedStart = String.format("{\"userId\":%s,\"timeStartedMillis\":%d,\"timeEndedMillis\":%d,\"questionList\":%s", 
			smallQuestionList.toJson(), 
			fixedStartTime, 
			fixedEndTime, 
			smallQuestionList.toJson());
		
		assertTrue(result.startsWith(expectedStart), "JSON should start with the correct fields.");
		assertTrue(result.contains("\"awnsersByQuestion\":"), "JSON should contain awnsersByQuestion field.");
		assertTrue(result.endsWith("}"), "JSON should end with a closing brace.");
		
		String awnsersPart = String.format("\"0\":%s, \"1\":%s", awnsers.get(0).toJson(), awnsers.get(1).toJson());
		assertTrue(result.contains(awnsersPart) || result.contains(awnsers.get(1).toJson() + ", " + awnsers.get(0).toJson()), 
			"awnsersByQuestion should contain serialized awnsers with integer keys.");
	}

}