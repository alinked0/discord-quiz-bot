import com.linked.quizbot.Constants;
import com.linked.quizbot.utils.*; 
import net.dv8tion.jda.api.entities.emoji.Emoji;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
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
		Users.reset();
		QuestionListHash.clearGeneratedCodes();
	}

	@AfterEach
	void tearDown() throws IOException {
		// No explicit cleanup needed for @TempDir, JUnit handles it.
		// Ensure static fields are reset for good measure, though @BeforeEach handles it too.
		Users.reset();
		QuestionListHash.clearGeneratedCodes();
	}

	// Helper to create a dummy user data JSON file
	private void createUserDataFile(String userId, String prefix, double points, int games, Map<String, Emoji> tags) throws IOException {
		Path userDir = tempUserDataPath.resolve(userId);
		Files.createDirectories(userDir);
		Path userFile = userDir.resolve("user-data.json");

		StringBuilder sb = new StringBuilder();
		sb.append("{\n");
		sb.append("\t\"tagEmojiPerTagName\":{");
		Iterator<Entry<String, Emoji>> iter = tags.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<String, Emoji> entry = iter.next();
			sb.append("\"").append(entry.getKey()).append("\":\"").append(entry.getValue().getFormatted()).append("\"");
			if(iter.hasNext()){
				sb.append(", ");
			}
		}
		sb.append("},\n");
		sb.append("\t\"prefixe\":\"").append(prefix).append("\",\n");
		sb.append("\t\"totalPointsEverGained\":").append(points).append(",\n");
		sb.append("\t\"numberOfGamesPlayed\":").append(games).append("\n");
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
		Map<String, Emoji> tags = new HashMap<>();
		tags.put("fun", Emoji.fromFormatted("😊"));
		List<QuestionList> preloadedLists = new ArrayList<>();
		QuestionList l = new QuestionList("builderUser", "ListA");
		l.setId("idA");
		l.setTimeCreatedMillis(1L);
		preloadedLists.add(l);

		User user = new User.Builder()
				.userId("testBuilderUser")
				.perferedPrefixe("!")
				.numberOfGamesPlayed(10)
				.totalPointsEverGained(100.5)
				.tagEmojiPerTagName(tags)
				.listsSortedById(preloadedLists)
				.build();

		assertNotNull(user);
		assertEquals("testBuilderUser", user.getUserId());
		assertEquals("!", user.getPrefix());
		assertEquals(10, user.getNumberOfGamesPlayed());
		assertEquals(100.5, user.getTotalPointsEverGained());
		assertEquals(tags, user.getTagEmojiPerTagName());
		assertEquals(1, user.getLists().size());
		assertEquals("idA", user.getLists().get(0).getId());
	}

	@Test
	@DisplayName("Test User(String userId) constructor - existing user data and lists")
	void testUserConstructor_ExistingDataAndLists() throws IOException {
		String userId = "user1";
		String expectedPrefix = "$";
		double expectedPoints = 200.0;
		int expectedGames = 20;
		Map<String, Emoji> expectedTags = new HashMap<>();
		expectedTags.put("science", Emoji.fromFormatted("🧪"));
		expectedTags.put("history", Emoji.fromFormatted("📜"));

		// Pre-create user-data.json
		createUserDataFile(userId, expectedPrefix, expectedPoints, expectedGames, expectedTags);

		// Pre-create some question lists for the user
		String id1 = QuestionListHash.generate("My Math Quiz"+userId, 1000L);
		QuestionList list1 = new QuestionList.Builder()
			.authorId(userId)
			.name("My Math Quiz")
			.id(id1)
			.timeCreatedMillis(1000L)
			.addTag("science", Emoji.fromFormatted("🧪"))
			.build();
		list1.exportListQuestionAsJson(); // Writes to tempListsPath
		String id2 = QuestionListHash.generate("World History"+userId, 2000L);
		QuestionList list2 = new QuestionList.Builder()
			.authorId(userId)
			.name("World History")
			.id(id2)
			.timeCreatedMillis(2000L)
			.addTag("history", Emoji.fromFormatted("📜"))
			.build();
		list2.exportListQuestionAsJson(); // Writes to tempListsPath

		// Manually add to Users.allUsers and UserDataParser.predefinedUsers for stub lookup
		User predefinedUserStub = new User.Builder()
				.userId(userId)
				.perferedPrefixe(expectedPrefix)
				.totalPointsEverGained(expectedPoints)
				.numberOfGamesPlayed(expectedGames)
				.tagEmojiPerTagName(expectedTags)
				.build();
		predefinedUserStub.exportUserData();
		// Instantiate User, which should load data
		Users.reset();
		User user = new User(userId);

		assertNotNull(user);
		assertEquals(userId, user.getUserId());
		assertEquals(expectedPrefix, user.getPrefix());
		assertEquals(expectedPoints, user.getTotalPointsEverGained());
		assertEquals(expectedGames, user.getNumberOfGamesPlayed());
		assertEquals(expectedTags.size(), user.getTagEmojiPerTagName().size());
		assertEquals(expectedTags.get("science").getFormatted(), user.getTagEmojiPerTagName().get("science").getFormatted());

		// Verify loaded lists
		List<QuestionList> userLists = user.getLists();
		assertEquals(2, userLists.size());
		int i = 1, j=0;
		if (id1.compareTo(id2)<=0){
			i=0;j=1;
		}
		// Sorted by list ID
		List<String> ids = new ArrayList<>(List.of(id1, id2));
		ids.sort((e, f)-> e.compareTo(f));
		for (i=0; i<ids.size(); ++i){
			assertEquals(ids.get(i), userLists.get(i).getId());
		}
		
		// Verify questionListPerTags map is correctly populated
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
		
		User user = new User(userId); // No pre-existing files or stub data for this user

		assertNotNull(user);
		assertEquals(userId, user.getUserId());
		assertNull(user.getPrefix()); // Default prefix if no user-data.json
		assertEquals(0.0, user.getTotalPointsEverGained());
		assertEquals(0, user.getNumberOfGamesPlayed());
		assertTrue(user.getTagEmojiPerTagName().isEmpty());
		assertTrue(user.getLists().isEmpty());
		assertTrue(user.getQuestionListPerTags().isEmpty());
	}

	@Test
	@DisplayName("Test setPrefix and exportUserData")
	void testSetPrefixAndExport() throws IOException {
		String userId = "prefixUser";
		User user = new User.Builder().userId(userId).perferedPrefixe("old!").build();
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
		assertTrue(fileContent.contains("\"prefixe\":\"new!\""));
	}

	@Test
	@DisplayName("Test statistics incrementors and exportUserData")
	void testStatsIncrementors() throws IOException {
		String userId = "statsUser";
		User user = new User.Builder().userId(userId).totalPointsEverGained(50.0).numberOfGamesPlayed(5).build();
		Users.addUser(user);

		user.incrTotalPointsEverGained(25.0);
		user.incrNumberOfGamesPlayed();

		assertEquals(75.0, user.getTotalPointsEverGained());
		assertEquals(6, user.getNumberOfGamesPlayed());

		// Verify data is exported
		Path userFile = tempUserDataPath.resolve(userId).resolve("user-data.json");
		String fileContent = readFileContent(userFile);
		assertTrue(fileContent.contains("\"totalPointsEverGained\":75.0"));
		assertTrue(fileContent.contains("\"numberOfGamesPlayed\":6"));
	}

	@Test
	@DisplayName("Test getLists and iterator")
	void testGetListsAndIterator() {
		String userId = "listUser";
		QuestionList list1 = new QuestionList.Builder().authorId(userId).name("L1").id("id1").timeCreatedMillis(1L).build();
		QuestionList list2 = new QuestionList.Builder().authorId(userId).name("L2").id("id2").timeCreatedMillis(2L).build();
		User user = new User.Builder()
				.userId(userId)
				.listsSortedById(new ArrayList<>(Arrays.asList(list1, list2)))
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
		User user = new User.Builder().userId(userId).build();
		Users.addUser(user);

		Emoji emoji = Emoji.fromFormatted("✅");
		assertTrue(user.createTag("verified", emoji));

		// Verify internal state
		assertTrue(user.getTagEmojiPerTagName().containsKey("verified"));
		assertEquals(emoji.getFormatted(), user.getTagEmojiPerTagName().get("verified").getFormatted());
		assertTrue(user.getQuestionListPerTags().containsKey("verified"));
		assertTrue(user.getQuestionListPerTags().get("verified").isEmpty());

		// Verify exported data
		Path userFile = tempUserDataPath.resolve(userId).resolve("user-data.json");
		String fileContent = readFileContent(userFile);
		assertTrue(fileContent.contains("\"verified\":\"✅\""));
	}

	@Test
	@DisplayName("Test createTag method - tag already exists")
	void testCreateTag_AlreadyExists() throws IOException {
		String userId = "tagUser2";
		User user = new User.Builder().userId(userId).build();
		user.createTag("duplicate", Emoji.fromFormatted("❌")); // Initial creation
		Users.addUser(user);

		assertFalse(user.createTag("duplicate", Emoji.fromFormatted("✅"))); // Attempt to re-create
		// Ensure emoji hasn't changed
		assertEquals("❌", user.getTagEmojiPerTagName().get("duplicate").getFormatted());
	}

	@Test
	@DisplayName("Test deleteTag method - successful deletion")
	void testDeleteTag_Success() throws IOException {
		String userId = "tagUser3";
		User user = new User.Builder().userId(userId).build();
		user.createTag("toDelete", Emoji.fromFormatted("🗑️"));
		user.createTag("keep", Emoji.fromFormatted("👍"));
		Users.addUser(user);

		assertTrue(user.deleteTag("toDelete"));

		// Verify internal state
		assertFalse(user.getTagEmojiPerTagName().containsKey("toDelete"));
		assertFalse(user.getQuestionListPerTags().containsKey("toDelete"));
		assertTrue(user.getTagEmojiPerTagName().containsKey("keep"));

		// Verify exported data
		Path userFile = tempUserDataPath.resolve(userId).resolve("user-data.json");
		String fileContent = readFileContent(userFile);
		assertFalse(fileContent.contains("\"toDelete\""));
		assertTrue(fileContent.contains("\"keep\""));
	}

	@Test
	@DisplayName("Test deleteTag method - tag does not exist")
	void testDeleteTag_DoesNotExist() {
		String userId = "tagUser4";
		User user = new User.Builder().userId(userId).build();
		Users.addUser(user);

		assertFalse(user.deleteTag("nonExistent"));
	}

	@Test
	@DisplayName("Test addList method - new list")
	void testAddList_NewList() throws IOException {
		String userId = "addListUser";
		User user = new User.Builder().userId(userId).build();
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
		assertTrue(fileContent.contains("\"name\":\"New List\""));
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
			.userId(userId)
			.listsSortedById(new ArrayList<>(Collections.singletonList(existingList)))
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
		assertTrue(retrieved.stream().anyMatch(q -> q.getQuestion().equals("Old Q")));
		assertTrue(retrieved.stream().anyMatch(q -> q.getQuestion().equals("New Q")));

		// Verify the file was updated
		Path listFile = tempListsPath.resolve(userId).resolve(existingList.getId() + ".json");
		String fileContent = readFileContent(listFile);
		assertTrue(fileContent.contains("\"question\":\"New Q\""));
		assertTrue(fileContent.contains("\"question\":\"Old Q\""));
	}
	
	@Test
	@DisplayName("Test getUserQuestionListById - found")
	void testGetUserQuestionListById_Found() throws IOException {
		String userId = "110110110110110110";
		String id = QuestionListHash.generate(userId+"Test Quiz", 1L);
		QuestionList list1 = new QuestionList.Builder()
			.authorId(userId)
			.name("Test Quiz")
			.id(id)
			.timeCreatedMillis(1L)
			.build();
		list1.exportListQuestionAsJson(); // Make sure file exists for Users.getUserListQuestions
		
		Users.reset();
		User user = new User(userId); // Constructor will load list1

		QuestionList foundList = user.getById(id);
		assertNotNull(foundList);
		assertEquals(list1, foundList);
	}

	@Test
	@DisplayName("Test getUserQuestionListById - not found")
	void testGetUserQuestionListById_NotFound() throws IOException {
		String userId = "searchUser2";
		User user = new User(userId); // No lists for this user
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
			.id("uq1")
			.timeCreatedMillis(1L)
			.build();
		list1.exportListQuestionAsJson();

		User user = new User.Builder().userId(userId).add(list1).build();

		QuestionList foundList = user.getByName("Unique Name Quiz");
		assertNotNull(foundList);
		assertEquals(list1, foundList);
	}

	@Test
	@DisplayName("Test getUserQuestionListByName - not found")
	void testGetUserQuestionListByName_NotFound() throws IOException {
		String userId = "searchUser4";
		User user = new User(userId);
		QuestionList foundList = user.getByName("Non Existent Name");
		assertNull(foundList);
	}

	@Test
	@DisplayName("Test getEmojiFomTagName methods")
	void testGetEmojiFomTagName() throws IOException {
		String userId = "emojiUser";
		Map<String, Emoji> tags = new HashMap<>();
		tags.put("mood", Emoji.fromFormatted("😊"));
		tags.put("food", Emoji.fromFormatted("🍕"));
		
		User user = new User.Builder().userId(userId).tagEmojiPerTagName(tags).build();
		Users.addUser(user);

		// Test instance method
		Emoji moodEmoji = user.getEmojiFomTagName("mood");
		assertNotNull(moodEmoji);
		assertEquals("😊", moodEmoji.getFormatted());

		// Test static method
		Emoji foodEmoji = User.getEmojiFomTagName(userId, "food");
		assertNotNull(foodEmoji);
		assertEquals("🍕", foodEmoji.getFormatted());

		// Test non-existent tag
		assertNull(user.getEmojiFomTagName("nonexistent"));
		assertNull(User.getEmojiFomTagName(userId, "nonexistent"));
	}

	@Test
	@DisplayName("Test myBinarySearchIndexOf for lists")
	void testMyBinarySearchIndexOf_QuestionList() {
		List<QuestionList> lists = new ArrayList<>();
		lists.add(new QuestionList.Builder().authorId("a").name("A").id("idA").timeCreatedMillis(1L).build());
		lists.add(new QuestionList.Builder().authorId("b").name("B").id("idB").timeCreatedMillis(2L).build());
		lists.add(new QuestionList.Builder().authorId("c").name("C").id("idC").timeCreatedMillis(3L).build());

		// Test found
		QuestionList searchB = new QuestionList.Builder().id("idB").build();
		assertEquals(1, User.myBinarySearchIndexOf(lists, searchB, QuestionList.comparatorById()));

		// Test not found (insertion point)
		QuestionList searchX = new QuestionList.Builder().id("idx").build();
		assertEquals(-4, User.myBinarySearchIndexOf(lists, searchX, QuestionList.comparatorById()));

		QuestionList searchBeforeA = new QuestionList.Builder().id("id0").build();
		assertEquals(-1, User.myBinarySearchIndexOf(lists, searchBeforeA, QuestionList.comparatorById()));
	}

	@Test
	@DisplayName("Test myBinarySearchUserId for Users")
	void testMyBinarySearchUserId() {
		List<User> users = new ArrayList<>();
		users.add(new User.Builder().userId("user1").build());
		users.add(new User.Builder().userId("user3").build());
		users.add(new User.Builder().userId("user5").build());
		
		// Ensure the list is sorted for binary search
		users.sort(User.comparatorByUserId());

		// Test found
		assertEquals(0, User.myBinarySearchUserId(users, "user1"));
		assertEquals(1, User.myBinarySearchUserId(users, "user3"));
		assertEquals(2, User.myBinarySearchUserId(users, "user5"));

		// Test not found (insertion point)
		assertEquals(-1, User.myBinarySearchUserId(users, "user0"));
		assertEquals(-2, User.myBinarySearchUserId(users, "user2"));
		assertEquals(-3, User.myBinarySearchUserId(users, "user4"));
		assertEquals(-4, User.myBinarySearchUserId(users, "user6"));
	}

	@Test
	@DisplayName("Test userDataToString method")
	void testUserDataToString() {
		String userId = "stringUser";
		String prefix = ">>";
		double points = 300.75;
		int games = 30;
		Map<String, Emoji> tags = new HashMap<>();
		tags.put("sport", Emoji.fromFormatted("⚽"));
		tags.put("art", Emoji.fromFormatted("🎨"));

		User user = new User.Builder()
				.userId(userId)
				.perferedPrefixe(prefix)
				.totalPointsEverGained(points)
				.numberOfGamesPlayed(games)
				.tagEmojiPerTagName(tags)
				.build();
		
		String expectedJsonPart1 = "{\n\t\"tagEmojiPerTagName\":{";
		// Order of tags in map can vary, so check for both permutations
		String expectedTag1 = "\"sport\":\"⚽\", \"art\":\"🎨\"";
		String expectedTag2 = "\"art\":\"🎨\", \"sport\":\"⚽\"";

		String expectedJsonPart2 = "},\n" +
								   "\t\"prefixe\":\">>\",\n" +
								   "\t\"totalPointsEverGained\":300.75,\n" +
								   "\t\"numberOfGamesPlayed\":30\n" +
								   "}";
		
		String result = user.userDataToString();
		assertTrue(result.startsWith(expectedJsonPart1));
		assertTrue(result.endsWith(expectedJsonPart2));
		assertTrue(result.contains(expectedTag1) || result.contains(expectedTag2));
	}

	@Test
	@DisplayName("Test equals method for User objects")
	void testEquals_UserObjects() {
		User user1 = new User.Builder().userId("u1").build();
		User user2 = new User.Builder().userId("u1").build(); // Same ID, different instance
		User user3 = new User.Builder().userId("u2").build(); // Different ID

		assertTrue(user1.equals(user1)); // Self equality
		assertTrue(user1.equals(user2)); // Same userId
		assertFalse(user1.equals(user3)); // Different userId
		assertFalse(user1.equals(null)); // Null
		assertFalse(user1.equals(new Object())); // Different class
	}

	@Test
	@DisplayName("Test equals method for User and String")
	void testEquals_UserAndString() {
		User user = new User.Builder().userId("u123").build();

		assertTrue(user.equals("u123"));

		User userNumericId = new User.Builder().userId("12345").build();
		assertTrue(userNumericId.equals("12345"));

		// Test with original logic: if userId is "alpha123"
		User userAlphaNumericId = new User.Builder().userId("alpha123").build();
		assertTrue(userAlphaNumericId.equals("alpha123"));
	}
}