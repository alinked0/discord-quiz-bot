import com.fasterxml.jackson.core.JsonProcessingException;
import com.linked.quizbot.Constants;
import com.linked.quizbot.utils.*; 
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static org.junit.jupiter.api.Assertions.*;
public class TestUser {


	@TempDir
	Path tempDir; // JUnit 5 provides a temporary directory for each test

	private Path tempListsPath;
	private Path tempUserDataPath;

	@BeforeEach
	void setUp() throws IOException {
		// Set up temporary directories for Constants
		tempListsPath = tempDir.resolve("temp_lists");
		tempUserDataPath = tempDir.resolve("temp_user_data");
		Files.createDirectories(tempListsPath);
		Files.createDirectories(tempUserDataPath);

		Constants.LISTSPATH = tempListsPath.toString();
		Constants.USERDATAPATH = tempUserDataPath.toString();
		Constants.SEPARATOR = File.separator;

		// Reset all static stubs for each test
		Users.clear();
		QuestionList.Hasher.clearGeneratedCodes();
	}

	@AfterEach
	void tearDown() throws IOException {
		// No explicit cleanup needed for @TempDir, JUnit handles it.
		// Ensure static fields are clear() for good measure, though @BeforeEach handles it too.
		Users.clear();
		QuestionList.Hasher.clearGeneratedCodes();
	}

	// Helper to create a dummy user data JSON file
	private void createUserDataFile(String userId, String prefix, double points, int games, Map<String, String> tags) throws IOException {
		Path userDir = tempUserDataPath.resolve(userId);
		Files.createDirectories(userDir);
		Path userFile = userDir.resolve("user-data.json");

		StringBuilder sb = new StringBuilder();
		sb.append("{\n");
		sb.append("\t"+Constants.MAPPER.writeValueAsString("userId")+":"+Constants.MAPPER.writeValueAsString(userId)+"\n");
		sb.append("\t"+Constants.MAPPER.writeValueAsString("tagEmojiPerTagName")+":{");
		Iterator<Entry<String, String>> iter = tags.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<String, String> entry = iter.next();
			
			sb.append(Constants.MAPPER.writeValueAsString(entry.getKey())+":"+Constants.MAPPER.writeValueAsString(entry.getValue()));
			if(iter.hasNext()){
				sb.append(", ");
			}
		}
		sb.append("},\n");
		sb.append("\t"+Constants.MAPPER.writeValueAsString("prefixe")+":"+Constants.MAPPER.writeValueAsString(prefix)+",\n");
		sb.append("\t"+Constants.MAPPER.writeValueAsString("totalPointsEverGained")+":"+Constants.MAPPER.writeValueAsString(points)+",\n");
		sb.append("\t"+Constants.MAPPER.writeValueAsString("numberOfGamesPlayed")+":"+Constants.MAPPER.writeValueAsString(games)+"\n");
		sb.append("}");

		try (BufferedWriter writer = Files.newBufferedWriter(userFile)) {
			writer.write(sb.toString());
		}
	}

	// Helper to read content from a file
	private String readFileContent(Path filePath) throws IOException {
		StringBuilder content = new StringBuilder();
		try (BufferedReader reader = Files.newBufferedReader(filePath)) {
			String line;
			while ((line = reader.readLine()) != null) {
				content.append(line);
			}
		}
		return content.toString();
	}

	@Test
	@DisplayName("Test User.Builder and constructor with Builder")
	void testUserBuilderAndConstructor() {
		Map<String, String> tags = new HashMap<>();
		tags.put("fun", "😊");
		List<QuestionList> preloadedLists = new ArrayList<>();
		QuestionList l = new QuestionList("builderUser", "ListA");
		preloadedLists.add(l);

		User user = new User.Builder()
				.id("testBuilderUser")
				.prefixe("!")
				.numberOfGamesPlayed(10)
				.totalPointsEverGained(100.5)
				.tagEmojiPerTagName(tags)
				.lists(preloadedLists)
				.build();

		assertNotNull(user);
		assertEquals("testBuilderUser", user.getId());
		assertEquals("!", user.getPrefix());
		assertEquals(10, user.getNumberOfGamesPlayed());
		assertEquals(100.5, user.getTotalPointsEverGained());
		assertEquals(tags, user.getEmojiPerTagName());
		assertEquals(1, user.getLists().size());
		assertEquals(l.getId(), user.getLists().get(0).getId());
	}

	@Test
	@DisplayName("Test User(String userId) constructor - existing user data and lists")
	void testUserConstructor_ExistingDataAndLists() throws IOException {
		String userId = "user100";
		String expectedPrefix = "$";
		double expectedPoints = 200.0;
		int expectedGames = 20;
		Map<String, String> expectedTags = new HashMap<>();
		expectedTags.put("science", "🧪");
		expectedTags.put("history", "📜");

		// Pre-create user-data.json
		createUserDataFile(userId, expectedPrefix, expectedPoints, expectedGames, expectedTags);

		// Pre-create some question lists for the user
		String id1 = QuestionList.Hasher.generate("My Math Quiz"+userId, 1000L);
		QuestionList list1 = new QuestionList.Builder()
			.authorId(userId)
			.name("My Math Quiz")
			.id(id1)
			.timeCreatedMillis(1000L)
			.addTag("science", "🧪")
			.build();
		list1.exportListQuestionAsJson(); // Writes to tempListsPath
		String id2 = QuestionList.Hasher.generate("World History"+userId, 2000L);
		QuestionList list2 = new QuestionList.Builder()
			.authorId(userId)
			.name("World History")
			.id(id2)
			.timeCreatedMillis(2000L)
			.addTag("history", "📜")
			.build();
		list2.exportListQuestionAsJson(); // Writes to tempListsPath

		// Manually add to Users.allUsers and UserDataParser.predefinedUsers for stub lookup
		User predefinedUserStub = new User.Builder()
				.id(userId)
				.prefixe(expectedPrefix)
				.totalPointsEverGained(expectedPoints)
				.numberOfGamesPlayed(expectedGames)
				.tagEmojiPerTagName(expectedTags)
				.build();
		predefinedUserStub.exportUserData();
		// Instantiate User, which should load data
		Users.clear();
		User user = new User(userId);
		Users.addUser(user);

		assertNotNull(user);
		assertEquals(userId, user.getId());
		assertEquals(expectedPrefix, user.getPrefix());
		assertEquals(expectedPoints, user.getTotalPointsEverGained());
		assertEquals(expectedGames, user.getNumberOfGamesPlayed());
		assertEquals(expectedTags.size(), user.getEmojiPerTagName().size());
		assertEquals(expectedTags.get("science"), user.getEmojiPerTagName().get("science"));

		// Verify loaded lists
		List<QuestionList> userLists = user.getLists();
		assertEquals(2, userLists.size());
		int i = 1;
		if (id1.compareTo(id2)<=0){
			i=0;
		}
		// Sorted by list ID
		List<String> ids = new ArrayList<>(List.of(id1, id2));
		ids.sort((e, f)-> e.compareTo(f));
		for (i=0; i<ids.size(); ++i){
			assertEquals(ids.get(i), userLists.get(i).getId());
		}
		
		// Verify questionListPerTags map is correctly populated
		assertEquals(2, user.getEmojiPerTagName().size());
		assertEquals(2, user.getQuestionListPerTags().size());
		assertTrue(user.getQuestionListPerTags().containsKey("science"));
		assertTrue(user.getQuestionListPerTags().containsKey("history"));
		assertEquals(1, user.getQuestionListPerTags().get("science").size());
		assertEquals(id1, user.getQuestionListPerTags().get("science").get(0).getId());
	}

	@Test
	@DisplayName("Test User(String userId) constructor - new user (no data/lists)")
	void testUserConstructor_NewUser() {
		String userId = "newUser";
		
		User user = Users.get(userId); // No pre-existing files or stub data for this user

		assertNotNull(user);
		assertEquals(userId, user.getId());
		assertNull(user.getPrefix()); // Default prefix if no user-data.json
		assertEquals(0.0, user.getTotalPointsEverGained());
		assertEquals(0, user.getNumberOfGamesPlayed());
		assertTrue(user.getEmojiPerTagName().isEmpty());
		assertTrue(user.getLists().isEmpty());
		assertTrue(user.getQuestionListPerTags().isEmpty());
	}

	@Test
	@DisplayName("Test setPrefix and exportUserData")
	void testSetPrefixAndExport() throws IOException {
		String userId = "prefixUser";
		User user = new User.Builder().id(userId).prefixe("old!").build();
		Users.addUser(user); // Add user to Users' global map

		// Initially, user-data.json might not exist or have old!
		// We'll test that setPrefix updates the file correctly.
		user.setPrefix("new!");

		// Verify the user object
		assertEquals("new!", user.getPrefix());

		// Verify the content of the exported file
		Path userFile = tempUserDataPath.resolve(userId).resolve("user-data.json");
		assertTrue(Files.exists(userFile));
		String fileContent = readFileContent(userFile);
		assertTrue(fileContent.contains(""+Constants.MAPPER.writeValueAsString("prefixe")+":"+Constants.MAPPER.writeValueAsString("new!")+""));
	}

	@Test
	@DisplayName("Test statistics incrementors and exportUserData")
	void testStatsIncrementors() throws IOException {
		String userId = "statsUser";
		User user = new User.Builder().id(userId).totalPointsEverGained(50.0).numberOfGamesPlayed(5).build();
		Users.addUser(user);

		user.incrTotalPointsEverGained(25.0);
		user.incrNumberOfGamesPlayed();

		assertEquals(75.0, user.getTotalPointsEverGained());
		assertEquals(6, user.getNumberOfGamesPlayed());

		// Verify data is exported
		Path userFile = tempUserDataPath.resolve(userId).resolve("user-data.json");
		String fileContent = readFileContent(userFile);
		assertTrue(fileContent.contains(""+Constants.MAPPER.writeValueAsString("totalPointsEverGained")+":75.0"));
		assertTrue(fileContent.contains(""+Constants.MAPPER.writeValueAsString("numberOfGamesPlayed")+":6"));
	}

	@Test
	@DisplayName("Test getLists and iterator")
	void testGetListsAndIterator() {
		String userId = "listUser";
		QuestionList list1 = new QuestionList.Builder().authorId(userId).name("L1").id("id10000").timeCreatedMillis(1L).build();
		QuestionList list2 = new QuestionList.Builder().authorId(userId).name("L2").id("id20000").timeCreatedMillis(2L).build();
		User user = new User.Builder()
				.id(userId)
				.lists(new ArrayList<>(Arrays.asList(list1, list2)))
				.build();
		Users.addUser(user);

		List<QuestionList> retrievedLists = user.getLists();
		assertEquals(2, retrievedLists.size());
		assertEquals(list1, retrievedLists.get(0));
		assertEquals(list2, retrievedLists.get(1));

		// Test iterator
		List<QuestionList> iteratedLists = new ArrayList<>();
		for (QuestionList ql : user) {
			iteratedLists.add(ql);
		}
		assertEquals(retrievedLists, iteratedLists);
	}

	@Test
	@DisplayName("Test createTag method - successful creation")
	void testCreateTag_Success() throws IOException {
		String userId = "tagUser";
		User user = new User.Builder().id(userId).build();
		Users.addUser(user);

		String emoji ="✅";
		assertTrue(user.createTag("verified", emoji));

		// Verify internal state
		assertTrue(user.getEmojiPerTagName().containsKey("verified"));
		assertEquals(emoji, user.getEmojiPerTagName().get("verified"));
		assertTrue(user.getQuestionListPerTags().containsKey("verified"));
		assertTrue(user.getQuestionListPerTags().get("verified").isEmpty());

		// Verify exported data
		Path userFile = tempUserDataPath.resolve(userId).resolve("user-data.json");
		String fileContent = readFileContent(userFile);
		assertTrue(fileContent.contains(""+Constants.MAPPER.writeValueAsString("verified")+":"+Constants.MAPPER.writeValueAsString("✅")+""));
	}

	@Test
	@DisplayName("Test createTag method - tag already exists")
	void testCreateTag_AlreadyExists() throws IOException {
		String userId = "tagUser2";
		User user = new User.Builder().id(userId).build();
		user.createTag("duplicate", "❌"); // Initial creation
		Users.addUser(user);

		assertFalse(user.createTag("duplicate", "✅")); // Attempt to re-create
		// Ensure emoji hasn't changed
		assertEquals("❌", user.getEmojiPerTagName().get("duplicate"));
	}

	@Test
	@DisplayName("Test deleteTag method - successful deletion")
	void testDeleteTag_Success() throws IOException {
		String userId = "tagUser3";
		User user = new User.Builder().id(userId).build();
		user.createTag("toDelete", "🗑️");
		user.createTag("keep", "👍");
		Users.addUser(user);

		assertTrue(user.deleteTag("toDelete"));

		// Verify internal state
		assertFalse(user.getEmojiPerTagName().containsKey("toDelete"));
		assertFalse(user.getQuestionListPerTags().containsKey("toDelete"));
		assertTrue(user.getEmojiPerTagName().containsKey("keep"));

		// Verify exported data
		Path userFile = tempUserDataPath.resolve(userId).resolve("user-data.json");
		String fileContent = readFileContent(userFile);
		assertFalse(fileContent.contains(""+Constants.MAPPER.writeValueAsString("toDelete")+""));
		assertTrue(fileContent.contains(""+Constants.MAPPER.writeValueAsString("keep")+""));
	}

	@Test
	@DisplayName("Test deleteTag method - tag does not exist")
	void testDeleteTag_DoesNotExist() {
		String userId = "tagUser4";
		User user = new User.Builder().id(userId).build();
		Users.addUser(user);

		assertFalse(user.deleteTag("nonExistent"));
	}

	@Test
	@DisplayName("Test addList method - new list")
	void testAddList_NewList() throws IOException {
		String userId = "addListUser";
		User user = new User.Builder().id(userId).build();
		Users.addUser(user);

		QuestionList newList = new QuestionList.Builder()
			.authorId(userId).name("New List")
			.id("newId123")
			.timeCreatedMillis(System.currentTimeMillis())
			.add(new Question("Q1", new Option("A", true)))
			.build();

		user.addList(newList);

		assertEquals(1, user.getLists().size());
		assertEquals(newList, user.getLists().get(0));
		
		// Verify list was exported
		Path listFile = tempListsPath.resolve(userId).resolve(newList.getId() + ".json");
		assertTrue(Files.exists(listFile));
		String fileContent = readFileContent(listFile);
		
		assertTrue(fileContent.contains(""+Constants.MAPPER.writeValueAsString("name")+":"+Constants.MAPPER.writeValueAsString("New List")+""));
	}

	@Test
	@DisplayName("Test addList method - update existing list (same name)")
	void testAddList_UpdateExistingList() throws IOException {
		String userId = "updateListUser";
		QuestionList existingList = new QuestionList.Builder()
			.authorId(userId).name("Existing List")
			.id("existId456")
			.timeCreatedMillis(System.currentTimeMillis())
			.add(new Question("Old Q", new Option("X", true)))
			.build();
		existingList.exportListQuestionAsJson(); // Simulate pre-existing file

		User user = new User.Builder()
			.id(userId)
			.lists(new ArrayList<>(Collections.singletonList(existingList)))
			.build();
		Users.addUser(user);

		
		// A "new" list with the same name, but new content
		QuestionList updatedList = new QuestionList.Builder()
			.authorId(userId).name("Existing List")
			.id("existId456")
			.timeCreatedMillis(System.currentTimeMillis()+1000)
			.add(new Question("New Q", new Option("Y", true)))
		.build();
		user.addList(updatedList);
		
		assertEquals(1, user.getLists().size()); // Should still be one list, updated
		QuestionList retrieved = user.getLists().get(0);
		assertEquals("Existing List", retrieved.getName());
		assertEquals(2, retrieved.size()); // Should now have both old Q and new Q
		assertTrue(retrieved.getQuestions().stream().anyMatch(q -> q.getQuestion().equals("Old Q")));
		assertTrue(retrieved.getQuestions().stream().anyMatch(q -> q.getQuestion().equals("New Q")));

		// Verify the file was updated
		Path listFile = tempListsPath.resolve(userId).resolve(existingList.getId() + ".json");
		String fileContent = readFileContent(listFile);
		assertTrue(fileContent.contains(""+Constants.MAPPER.writeValueAsString("question")+":"+Constants.MAPPER.writeValueAsString("New Q")+""));
		assertTrue(fileContent.contains(""+Constants.MAPPER.writeValueAsString("question")+":"+Constants.MAPPER.writeValueAsString("Old Q")+""));
	}
	
	@Test
	@DisplayName("Test getUserQuestionListById - found")
	void testGetUserQuestionListById_Found() throws IOException {
		String userId = "110110110110110110";
		String id = QuestionList.Hasher.generate(userId+"Test Quiz", 1L);
		QuestionList list1 = new QuestionList.Builder()
			.authorId(userId)
			.name("Test Quiz")
			.id(id)
			.timeCreatedMillis(1L)
			.build();
		list1.exportListQuestionAsJson(); // Make sure file exists for Users.importUserLists
		
		Users.clear();
		User user = Users.get(userId); // Constructor will load list1

		QuestionList foundList = user.getById(id);
		assertNotNull(foundList);
		assertEquals(list1, foundList);
	}

	@Test
	@DisplayName("Test getUserQuestionListById - not found")
	void testGetUserQuestionListById_NotFound() throws IOException {
		String userId = "searchUser2";
		User user = Users.get(userId); // No lists for this user
		QuestionList foundList = user.getById("nonExistentId");
		assertNull(foundList);
	}

	@Test
	@DisplayName("Test getUserQuestionListByName - found")
	void testGetUserQuestionListByName_Found() throws IOException {
		String userId = "searchUser3";
		QuestionList list1 = new QuestionList.Builder()
			.authorId(userId)
			.name("Unique Name Quiz")
			.id("uq10000")
			.timeCreatedMillis(1L)
			.build();
		list1.exportListQuestionAsJson();

		User user = new User.Builder().id(userId).add(list1).build();

		QuestionList foundList = user.getByName("Unique Name Quiz");
		assertNotNull(foundList);
		assertEquals(list1, foundList);
	}

	@Test
	@DisplayName("Test getUserQuestionListByName - not found")
	void testGetUserQuestionListByName_NotFound() throws IOException {
		String userId = "searchUser4";
		User user = Users.get(userId);
		QuestionList foundList = user.getByName("Non Existent Name");
		assertNull(foundList);
	}

	@Test
	@DisplayName("Test getEmojiFomTagName methods")
	void testGetEmojiFomTagName() throws IOException {
		String userId = "emojiUser";
		Map<String, String> tags = new HashMap<>();
		tags.put("mood", "😊");
		tags.put("food", "🍕");
		
		User user = new User.Builder().id(userId).tagEmojiPerTagName(tags).build();
		Users.addUser(user);

		// Test instance method
		String moodEmoji = user.getEmojiFomTagName("mood");
		assertNotNull(moodEmoji);
		assertEquals("😊", moodEmoji);
		
		// Test non-existent tag
		assertNull(user.getEmojiFomTagName("nonexistent"));
	}

	@Test
	@DisplayName("Test myBinarySearchIndexOf for lists")
	void testMyBinarySearchIndexOf_QuestionList() {
		QuestionList listb = new QuestionList.Builder().authorId("b").name("B").id("idB0000").timeCreatedMillis(2L).build();
		List<QuestionList> lists = new ArrayList<>();
		lists.add(new QuestionList.Builder().authorId("a").name("A").id("idA0000").timeCreatedMillis(1L).build());
		lists.add(listb);
		lists.add(new QuestionList.Builder().authorId("c").name("C").id("idC0000").timeCreatedMillis(3L).build());

		// Test found
		QuestionList searchB = new QuestionList.Builder().add(listb).build();
		assertEquals(1, User.myBinarySearchIndexOf(lists, searchB, QuestionList.comparatorById()));

		// Test not found (insertion point)
		QuestionList searchX = new QuestionList.Builder().id("idx0000").build();
		assertEquals(-4, User.myBinarySearchIndexOf(lists, searchX, QuestionList.comparatorById()));

		QuestionList searchBeforeA = new QuestionList.Builder().id("id00000").build();
		assertEquals(-1, User.myBinarySearchIndexOf(lists, searchBeforeA, QuestionList.comparatorById()));
	}

	@Test
	@DisplayName("Test myBinarySearchUserId for Users")
	void testMyBinarySearchUserId() {
		List<User> users = new ArrayList<>();
		users.add(new User.Builder().id("user100").build());
		users.add(new User.Builder().id("user300").build());
		users.add(new User.Builder().id("user500").build());
		
		// Ensure the list is sorted for binary search
		users.sort(User.comparatorByUserId());

		// Test found
		assertEquals(0, User.myBinarySearchUserId(users, "user100"));
		assertEquals(1, User.myBinarySearchUserId(users, "user300"));
		assertEquals(2, User.myBinarySearchUserId(users, "user500"));

		// Test not found (insertion point)
		assertEquals(-1, User.myBinarySearchUserId(users, "user000"));
		assertEquals(-2, User.myBinarySearchUserId(users, "user200"));
		assertEquals(-3, User.myBinarySearchUserId(users, "user400"));
		assertEquals(-4, User.myBinarySearchUserId(users, "user600"));
	}

	@Test
	@DisplayName("Test toJson method")
	void testToJson() throws JsonProcessingException{
		String userId = "stringUser";
		String prefix = ">>";
		double points = 300.75;
		int games = 30;
		Map<String, String> tags = new HashMap<>();
		tags.put("sport", "⚽");
		tags.put("art", "🎨");

		User user = new User.Builder()
				.id(userId)
				.prefixe(prefix)
				.totalPointsEverGained(points)
				.numberOfGamesPlayed(games)
				.tagEmojiPerTagName(tags)
				.build();
		
		String expectedJsonPart1 = "{\n  "+Constants.MAPPER.writeValueAsString("userId")+":"+Constants.MAPPER.writeValueAsString("stringUser")+"";
		// Order of tags in map can vary, so check for both permutations
		String expectedTag1 = ""+Constants.MAPPER.writeValueAsString("sport")+":"+Constants.MAPPER.writeValueAsString("⚽")+", \n    "+Constants.MAPPER.writeValueAsString("art")+":"+Constants.MAPPER.writeValueAsString("🎨")+"";
		String expectedTag2 = ""+Constants.MAPPER.writeValueAsString("art")+":"+Constants.MAPPER.writeValueAsString("🎨")+", \n    "+Constants.MAPPER.writeValueAsString("sport")+":"+Constants.MAPPER.writeValueAsString("⚽")+"";

		String expectedJsonPart2 = "},\n" +
								   "  "+Constants.MAPPER.writeValueAsString("prefixe")+":"+Constants.MAPPER.writeValueAsString(">>")+",\n" +
								   "  "+Constants.MAPPER.writeValueAsString("useButtons")+":true,\n" +
								   "  "+Constants.MAPPER.writeValueAsString("useAutoNext")+":false,\n" +
								   "  "+Constants.MAPPER.writeValueAsString("totalPointsEverGained")+":300.75,\n" +
								   "  "+Constants.MAPPER.writeValueAsString("numberOfGamesPlayed")+":30\n" +
								   "}";
		
		String result = user.toJson();
		assertTrue(result.startsWith(expectedJsonPart1));
		assertTrue(result.endsWith(expectedJsonPart2));
		assertTrue(result.contains(expectedTag1) || result.contains(expectedTag2));
	}

	@Test
	@DisplayName("Test equals method for User objects")
	void testEquals_UserObjects() {
		User user100 = new User.Builder().id("u100000").build();
		User user200 = new User.Builder().id("u100000").build(); // Same ID, different instance
		User user300 = new User.Builder().id("u200000").build(); // Different ID

		assertTrue(user100.equals(user100)); // Self equality
		assertTrue(user100.equals(user200)); // Same userId
		assertFalse(user100.equals(user300)); // Different userId
		assertFalse(user100.equals(null)); // Null
		assertFalse(user100.equals(new Object())); // Different class
	}

	@Test
	@DisplayName("Test equals method for User and String")
	void testEquals_UserAndString() {
		User user = new User.Builder().id("u123456").build();

		assertTrue(user.equals("u123456"));

		User userNumericId = new User.Builder().id("1234567").build();
		assertTrue(userNumericId.equals("1234567"));

		// Test with original logic: if userId is "alpha123"
		User userAlphaNumericId = new User.Builder().id("alpha123").build();
		assertTrue(userAlphaNumericId.equals("alpha123"));
	}
}