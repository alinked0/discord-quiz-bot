package com.linked.quizbot.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.linked.quizbot.Constants;

/**
 * A stateful utility class that manages all data and preferences for a single bot user.
 * <p>
 * This class serves as the central repository for a user's quiz lists, game statistics,
 * command prefixes, and personal settings. It provides methods for creating and managing
 * quiz lists and tags, as well as handling data persistence by importing from and exporting
 * to local files. The class uses a Builder pattern for robust object creation and is
 * designed to be the primary interface for all user-specific data.
 * </p>
 *
 * @author alinked0
 * @version 1.0
 * @since 2025-02-01
 * @see QuestionList
 * @see QuestionList.Hasher
 * @see Users
 */
public class User implements Iterable<QuestionList>{
	private final String userId;
	private final List<QuestionList> listsSortedById= new ArrayList<>();
	private  final Map<String, String> tagEmojiPerTagName= new HashMap<>();
	private final Map<String, List<QuestionList>> questionListPerTags= new HashMap<>();
	private String prefixe;
	private int numberOfGamesPlayed;
	private double totalPointsEverGained;
	private boolean useButtons;
	private boolean useAutoNext;

	/**
	 * Inner class implementing the Builder pattern for creating and initializing {@link User} objects.
	 * <p>
	 * This allows for optional parameter setting and a more readable and robust construction process.
	 * </p>
	 */
	public static class Builder {
		private String userId = null;
		private String prefixe = null;
		protected List<QuestionList> list = new ArrayList<>();
		private boolean useButtons = true;	
		private boolean useAutoNext = false;
		private int numberOfGamesPlayed=0;
		private double totalPointsEverGained=0;
		private Map<String, String> tagEmojiPerTagName= new HashMap<>();
		private Map<String, List<QuestionList>> questionListPerTags= new HashMap<>();
		
		/**
		 * Sets the mandatory user ID.
		 * @param userId The unique identifier for the user.
		 * @return The current Builder instance for chaining.
		 * @throws NullPointerException if {@code userId} is null.
		 * @requires userId != null
		 * @ensures this.userId == userId
		 */
		public Builder id(String userId){ 
			if (userId == null){
				throw new NullPointerException();
			}
			this.userId = userId;
			return this;
		}  

		/**
		 * Sets the command prefix for the user.
		 * @param prefixe The custom command prefix.
		 * @return The current Builder instance for chaining.
		 * @ensures this.prefixe == prefixe
		 */
		public Builder prefixe(String prefixe){ 
			this.prefixe= prefixe;
			return this;
		}

		/**
		 * Sets the preference for using interactive buttons in the bot.
		 * @param b {@code true} to use buttons, {@code false} otherwise. Defaults to {@code true}.
		 * @return The current Builder instance for chaining.
		 * @ensures this.useButtons == b
		 */
		public Builder useButtons(boolean b){ 
			this.useButtons= b;
			return this;
		}

		/**
		 * Sets the preference for automatically advancing to the next question.
		 * @param b {@code true} to use auto-next, {@code false} otherwise. Defaults to {@code false}.
		 * @return The current Builder instance for chaining.
		 * @ensures this.useAutoNext == b
		 */
		public Builder useAutoNext(boolean b){ 
			this.useAutoNext= b;
			return this;
		}

		/**
		 * Sets the total number of games played by the user.
		 * @param n The number of games played. Defaults to 0.
		 * @return The current Builder instance for chaining.
		 * @requires n >= 0
		 * @ensures this.numberOfGamesPlayed == n
		 */
		public Builder numberOfGamesPlayed(int n){ 
			this.numberOfGamesPlayed=n;
			return this;
		}

		/**
		 * Sets the total points ever gained by the user.
		 * @param points The total points. Defaults to 0.0.
		 * @return The current Builder instance for chaining.
		 * @requires points >= 0.0
		 * @ensures this.totalPointsEverGained == points
		 */
		public Builder totalPointsEverGained(double points){ 
			this.totalPointsEverGained = points;
			return this;
		}

		/**
		 * Sets the map of tag names to their associated emojis.
		 * @param tagEmojiPerTagName A map of tag names (String) to emoji representations (String).
		 * @return The current Builder instance for chaining.
		 * @ensures this.tagEmojiPerTagName.equals(tagEmojiPerTagName)
		 */
		public Builder tagEmojiPerTagName(Map<String, String> tagEmojiPerTagName){
			this.tagEmojiPerTagName = tagEmojiPerTagName;
			return this;
		}

		/**
		 * Adds a single tag name and its emoji to the map.
		 * @param tagName The name of the tag.
		 * @param emoji The emoji associated with the tag.
		 * @return The current Builder instance for chaining.
		 * @requires tagName != null && emoji != null
		 * @ensures this.tagEmojiPerTagName.containsKey(tagName)
		 */
		public  Builder addTag(String tagName, String emoji){
			this.tagEmojiPerTagName.put(tagName, emoji);
			return this;
		}

		/**
		 * Adds all entries from a map of tags to the current tag map.
		 * @param tags A map of tag names (String) to emoji representations (String).
		 * @return The current Builder instance for chaining.
		 * @requires tags != null
		 * @ensures this.tagEmojiPerTagName.keySet().containsAll(tags.keySet())
		 */
		public  Builder addTags(Map<String,String> tags){
			this.tagEmojiPerTagName.putAll(tags);
			return this;
		}

		/**
		 * Sets the map associating tag names with lists of questions lists that have that tag.
		 * @param questionListPerTags A map where the key is the tag name (String) and the value is a list of {@link QuestionList}.
		 * @return The current Builder instance for chaining.
		 * @ensures this.questionListPerTags.equals(questionListPerTags)
		 */
		public Builder questionListPerTags(Map<String, List<QuestionList>> questionListPerTags){
			this.questionListPerTags = questionListPerTags;
			return this;
		}

		/**
		 * Sets the initial list of quiz lists for the user.
		 * @param listsSortedById A list of {@link QuestionList} objects.
		 * @return The current Builder instance for chaining.
		 * @requires listsSortedById != null
		 * @ensures this.list.size() == listsSortedById.size()
		 */
		public Builder lists(List<QuestionList> listsSortedById){
			this.list = new ArrayList<>(listsSortedById);
			return this;
		}

		 /**
		 * Adds a single {@link QuestionList} to the user's collection.
		 * @param l The {@link QuestionList} to add.
		 * @return The current Builder instance for chaining.
		 * @requires l != null
		 * @ensures this.list.size() == \old(this.list.size()) + 1
		 */
		public Builder add(QuestionList l){
			this.list.add(l);
			return this;
		}

		/**
		 * Adds a collection of {@link QuestionList} objects to the user's collection.
		 * @param c The collection of {@link QuestionList} objects to add.
		 * @return The current Builder instance for chaining.
		 * @requires c != null
		 * @ensures this.list.containsAll(c)
		 */
		public Builder addAll(List<QuestionList> c){
			this.list.addAll(c);
			return this;
		}

		/**
		 * Constructs the final {@link User} object.
		 * <p>The constructor will automatically call {@link User#syncWithLocal()} to load and merge
		 * any existing user data and quiz lists from local files after object creation.</p>
		 * @return The newly constructed and synchronized {@link User} object.
		 * @requires this.userId != null
		 * @throws NullPointerException if the user ID was not set in the builder.
		 * @ensures \result != null && \result.getId().equals(this.userId)
		 */
		public User build(){
			return new User(this);
		}
	}

	/**
	 * Constructs a {@code User} object from a {@link Builder}.
	 * <p>
	 * Initializes all fields using the builder's values and immediately calls {@link #syncWithLocal()}
	 * to load and merge data from local storage.
	 * </p>
	 * @param builder The {@link User.Builder} containing the initial configuration.
	 * @throws NullPointerException if the builder's user ID is null.
	 * @requires builder.userId != null
	 * @ensures this.userId == builder.userId
	 * @ensures syncWithLocal() is called
	 */
	public User(User.Builder builder){
		if (builder.userId == null){
			throw new NullPointerException();
		}
		this.userId = builder.userId;
		this.prefixe = builder.prefixe;
		this.numberOfGamesPlayed = builder.numberOfGamesPlayed;
		this.totalPointsEverGained = builder.totalPointsEverGained;
		this.tagEmojiPerTagName.putAll(builder.tagEmojiPerTagName);
		this.questionListPerTags.putAll(builder.questionListPerTags);
		this.listsSortedById.addAll(builder.list);
		this.useButtons = builder.useButtons;
		this.useAutoNext = builder.useAutoNext;
		syncWithLocal();
	}

	/**
	 * Synchronizes the in-memory user data with local files.
	 * <p>
	 * This method:
	 * <ol>
	 * <li>Sorts the current list of quizzes by ID.</li>
	 * <li>Loads general user data (prefix, stats, preferences, and tags) from the local JSON file, overriding in-memory defaults.</li>
	 * <li>Imports all {@link QuestionList} objects from their respective local files, merging with or adding to existing ones.</li>
	 * <li>Updates the internal tag-to-quiz map based on the tags present in all loaded quiz lists.</li>
	 * </ol>
	 * </p>
	 * @ensures this.listsSortedById is sorted by ID.
	 * @ensures this.prefixe, stats, and preferences are updated from local JSON if the file exists.
	 * @ensures all local quiz lists are loaded and merged into {@code this.listsSortedById}.
	 */
	public void syncWithLocal() {
		Map<String, String> tags;
		listsSortedById.sort(QuestionList.comparatorById());
		for(QuestionList l: listsSortedById){
			QuestionList.Hasher.addGeneratedCode(l.getId());
		}
		if (new File(getPathToUserData()).exists()){ 
			try {
				User.Builder builder = UserDataParser.fromJsonFile(getPathToUserData());
				this.prefixe = builder.prefixe;
				this.numberOfGamesPlayed = builder.numberOfGamesPlayed;
				this.totalPointsEverGained = builder.totalPointsEverGained;
				this.tagEmojiPerTagName.putAll(builder.tagEmojiPerTagName);
				this.questionListPerTags.putAll(builder.questionListPerTags);
				this.useButtons = builder.useButtons;
				this.useAutoNext = builder.useAutoNext;
			} catch (IOException e){
				System.err.println(String.format("[ERROR] %s %s", getPathToUserData(), e.getMessage()));
			}
		}
		for(QuestionList l: importLists().values()){
			addList(l);
		}
		for(QuestionList l: listsSortedById){
			tags = l.getEmojiPerTagName();
			tagEmojiPerTagName.putAll(tags);
			for (String tagName : tags.keySet()){
				if (questionListPerTags.get(tagName)==null){
					questionListPerTags.put(tagName, new ArrayList<QuestionList>());
				}
				questionListPerTags.get(tagName).add(l);
			}
		}
	}

	/**
	 * Constructs a {@code User} object with only the required user ID.
	 * <p>
	 * Uses a default {@link Builder} to create the object. The constructor will automatically
	 * call {@link #syncWithLocal()} to load existing data for this user, if any.
	 * </p>
	 * @param userId The unique identifier for the user.
	 * @requires userId != null
	 * @ensures this.userId == userId
	 * @ensures syncWithLocal() is called
	 */
	public User(@NotNull String userId){
		this(new User.Builder().id(userId));
	}

	/**
	 * Checks if the user has enabled the use of buttons for interaction.
	 * @return {@code true} if buttons are enabled, {@code false} otherwise.
	 * @ensures \result == useButtons
	 */
	public boolean useButtons(){return useButtons;}

	/**
	 * Sets the preference for using interactive buttons and does not automatically export data.
	 * @param b {@code true} to enable buttons.
	 * @ensures this.useButtons == b
	 */
	public void useButtons(boolean b){ useButtons = b;}

	/**
	 * Checks if the user has enabled the auto-next feature.
	 * @return {@code true} if auto-next is enabled, {@code false} otherwise.
	 * @ensures \result == useAutoNext
	 */
	public boolean useAutoNext(){return useAutoNext;}

	/**
	 * Sets the preference for automatically advancing to the next question and does not automatically export data.
	 * @param b {@code true} to enable auto-next.
	 * @ensures this.useAutoNext == b
	 */
	public void useAutoNext(boolean b){ useAutoNext = b;}

	/**
	 * Gets the custom command prefix for the user.
	 * @return The command prefix string.
	 * @ensures \result == prefixe
	 * @pure
	 */
	public String getPrefix(){return prefixe;}

	/**
	 * Sets a new custom command prefix for the user and immediately exports the user data.
	 * @param prefixe The new command prefix.
	 * @ensures this.prefixe == prefixe
	 * @ensures exportUserData() is called
	 */
	public void setPrefix(String prefixe){
		this.prefixe= prefixe;
		exportUserData();
	}

	/**
	 * Gets a copy of the user's sorted list of quiz lists.
	 * @return A new {@code List<QuestionList>} containing all of the user's quizzes, sorted by ID.
	 * @ensures \result is a new list instance
	 * @ensures \result.size() == listsSortedById.size()
	 * @pure
	 */
	public List<QuestionList> getLists() {
		List<QuestionList> res = new ArrayList<>(listsSortedById);
		return res;
	}

	 /**
	 * Gets the unique ID of the user.
	 * @return The user ID string.
	 * @ensures \result == userId
	 */
	public String getId(){ return userId;}

	/**
	 * Gets the absolute file path to the user's data JSON file.
	 * @return The file path string.
	 * @ensures \result.endsWith("user-data.json")
	 * @ensures \result.contains(getId())
	 */
	public String getPathToUserData(){
		return Constants.USERDATAPATH+Constants.SEPARATOR+getId()+Constants.SEPARATOR+"user-data.json";
	}

	/**
	 * Gets a {@link QuestionList} by its index in the internal, ID-sorted list.
	 * @param index The index of the quiz list.
	 * @return The {@link QuestionList} at the specified index.
	 * @requires 0 <= index < listsSortedById.size()
	 * @ensures \result == listsSortedById.get(index)
	 */
	public QuestionList get(int index) {
		return listsSortedById.get(index);
	}

	public double getTotalPointsEverGained(){
		return totalPointsEverGained;
	}
	public int getNumberOfGamesPlayed(){
		return numberOfGamesPlayed;
	}
	public void incrTotalPointsEverGained(double numberOfPointsGained){
		totalPointsEverGained+=numberOfPointsGained;
		this.exportUserData();
	}
	public void incrNumberOfGamesPlayed(){
		numberOfGamesPlayed+=1;
		this.exportUserData();
	}

	/**
	 * Gets a copy of the map linking tag names to their associated emojis.
	 * @return A new {@code Map<String, String>} containing all user tags and emojis.
	 * @ensures \result.size() == tagEmojiPerTagName.size()
	 */
	public Map<String, String> getEmojiPerTagName() {
		return new HashMap<>(tagEmojiPerTagName);
	}

	/**
	 * Gets a copy of the map linking tag names to the quiz lists that have that tag.
	 * @return A new {@code Map<String, List<QuestionList>>} mapping tags to lists.
	 * @ensures \result.size() == questionListPerTags.size()
	 */
	public Map<String, List<QuestionList>> getQuestionListPerTags() {
		return new HashMap<>(questionListPerTags);
	}

	/**
	 * Retrieves all {@link QuestionList} objects associated with a given tag name.
	 * @param tagName The name of the tag to search for.
	 * @return A new {@code List<QuestionList>} of quizzes with the specified tag, or an empty list if the tag doesn't exist.
	 * @ensures \result is a new list instance
	 * @ensures \result.size() <= listsSortedById.size()
	 */
	public List<QuestionList> getListsByTag(String tagName) {
		List<QuestionList> res = questionListPerTags.get(tagName);
		if (res == null) {
			return new ArrayList<>();
		}
		return new ArrayList<>(res);
	}

	/**
	 * Gets a set of all tag names defined by the user.
	 * @return A {@code Set<String>} of all tag names.
	 * @ensures \result.equals(tagEmojiPerTagName.keySet())
	 */
	public Set<String> getTagNames() {
		return tagEmojiPerTagName.keySet();
	}

	/**
	 * Retrieves all {@link QuestionList} objects associated with a given tag name.
	 * <p>This is an alias for {@link #getListsByTag(String)}.</p>
	 * @param tagName The name of the tag to search for.
	 * @return A new {@code List<QuestionList>} of quizzes with the specified tag, or an empty list if the tag doesn't exist.
	 * @ensures \result is a new list instance
	 * @ensures \result.size() <= listsSortedById.size()
	 */
	public List<QuestionList> getQuestionListsFromTagName(String tagName){
		List<QuestionList> res = questionListPerTags.get(tagName);
		if (res == null){
			return new ArrayList<>();
		}
		return new ArrayList<>(res);
	}

	/**
	 * Retrieves a {@link QuestionList} by its unique ID.
	 * <p>Performs a binary search on the internally ID-sorted list. Checks for a static example list first.</p>
	 * @param id The unique ID of the quiz list.
	 * @return The matching {@link QuestionList}, or {@code null} if not found.
	 * @ensures (\result == null) || (\result.getId().equals(id))
	 */
	public QuestionList getById(String id) {
		if (QuestionList.getExampleQuestionList().getId().equals(id)){
			return QuestionList.getExampleQuestionList();
		} else {
			QuestionList searched = new QuestionList.Builder().id(id).build();
			int i=-1;
			List<QuestionList> l=getLists();
			i = Users.myBinarySearchIndexOf(l, searched, QuestionList.comparatorById());
			if (i>=0){
				return l.get(i);
			}
		}
		return null;
	}

	/**
	 * Retrieves a {@link QuestionList} by its name.
	 * <p>Temporarily sorts a copy of the list by name to perform a binary search.</p>
	 * @param listName The name of the quiz list.
	 * @return The matching {@link QuestionList}, or {@code null} if not found.
	 * @ensures (\result == null) || (\result.getName().equals(listName))
	 */
	public QuestionList getByName(String listName){
		List<QuestionList> listsSortedByName = new ArrayList<>(listsSortedById);
		listsSortedByName.sort(QuestionList.comparatorByName());
		int index = QuestionList.myBinarySearchIndexOf(listsSortedByName, listName);
		if (index<0) return null;
		return listsSortedByName.get(index);
	}

	/**
	 * Retrieves the emoji associated with a given tag name for this user.
	 * @param tagName The name of the tag.
	 * @return The emoji string, or {@code null} if the tag is not defined.
	 * @ensures \result == tagEmojiPerTagName.getOrDefault(tagName, null)
	 */
	public String getEmojiFomTagName(String tagName){
		return tagEmojiPerTagName.getOrDefault(tagName, null);
	}
	
	/**
	 * Adds a new {@link QuestionList} to the user's collection or updates an existing one.
	 * <p>If a list with the same ID or name exists, the questions from the new list are **merged** into the existing one.
	 * The list is sorted by ID, its options are rearranged, and the list is exported to its local JSON file.
	 * </p>
	 * @param l The {@link QuestionList} to add or merge.
	 * @return {@code true} upon successful addition/update.
	 * @requires l != null
	 * @ensures l is merged into or added to listsSortedById, maintaining sort order.
	 * @ensures l.exportListQuestionAsJson() is called.
	 * @ensures Users.update(this) is called.
	 */
	public boolean addList(@NotNull QuestionList l){
		int index;
		QuestionList k = getById(l.getId());
		
		if (k==null){
			k = getByName(l.getId());
			if (k==null){
				k = l;
			} 
		}
		
		k.addAll(l);
		k.rearrageOptions((e, f) -> e.isCorrect()?-1:1);
		
		index = myBinarySearchIndexOf(listsSortedById, k, QuestionList.comparatorById());
		if (index>=0) {
			listsSortedById.set(index, k);
		} else{
			listsSortedById.add(index*-1 -1,k);
		}
		k.exportListQuestionAsJson();
		Users.update(this);
		return true;
	}

		/**
	 * Creates a new tag with an associated emoji and exports the user data.
	 * @param tagName The name of the new tag.
	 * @param emoji The emoji to associate with the tag.
	 * @return {@code true} if the tag was created, {@code false} if a tag with that name already exists.
	 * @requires tagName != null && emoji != null
	 * @ensures \result == !tagEmojiPerTagName.containsKey(tagName)
	 * @ensures (\result == true) ==> tagEmojiPerTagName.containsKey(tagName) && questionListPerTags.containsKey(tagName) && exportUserData() is called.
	 */
	public boolean createTag(@NotNull String tagName, @NotNull String emoji) {
		if (tagEmojiPerTagName.containsKey(tagName)) {
			return false; // Tag already exists
		}
		tagEmojiPerTagName.put(tagName, emoji);
		questionListPerTags.put(tagName, new ArrayList<>());
		exportUserData();
		return true;
	}

	/**
	 * Adds an existing user-level tag (name and emoji) to a specific {@link QuestionList} by its ID.
	 * @param tagName The name of the tag to add.
	 * @param emoji The emoji associated with the tag (used to pass to QuestionList).
	 * @param id The ID of the {@link QuestionList} to tag.
	 * @return {@code true} if the tag was successfully added to the list, {@code false} if the tag has not been created at the user level.
	 * @requires id != null && tagName != null && emoji != null
	 * @ensures \result == tagEmojiPerTagName.containsKey(tagName)
	 * @ensures (\result == true) ==> getById(id).getTagNames().contains(tagName) && exportUserData() is called.
	 */
	public boolean addTagToQuestionList(String tagName, String emoji, String id) {
		if (!tagEmojiPerTagName.containsKey(tagName)) {
			return false; // Tag hasnt been created
		}
		getById(id).addTag(tagName, emoji);
		exportUserData();
		Users.update(this);
		return true;
	}

	/**
	 * Deletes a tag from the user's master list, removing it from the {@code tagEmojiPerTagName} and {@code questionListPerTags} maps.
	 * <p>It is important to note this method does *not* remove the tag from individual {@link QuestionList} objects.</p>
	 * @param tagName The name of the tag to delete.
	 * @return {@code true} if the tag was deleted, {@code false} if the tag did not exist.
	 * @requires tagName != null
	 * @ensures \result == tagEmojiPerTagName.containsKey(tagName)
	 * @ensures (\result == true) ==> !tagEmojiPerTagName.containsKey(tagName) && !questionListPerTags.containsKey(tagName) && exportUserData() is called.
	 */
	public boolean deleteTag(String tagName) {
		if (!tagEmojiPerTagName.containsKey(tagName)) {
			return false; // Tag does not exist
		}
		tagEmojiPerTagName.remove(tagName);
		questionListPerTags.remove(tagName);
		exportUserData();
		return true;
	}

	/**
	 * Renames a {@link QuestionList} and re-adds it to the user's collection to maintain correct sorting and data integrity.
	 * @param l The {@link QuestionList} to rename.
	 * @param newName The new name for the quiz list.
	 * @return {@code true} if the list was successfully renamed and updated, {@code false} if a list with the {@code newName} already exists.
	 * @requires l != null && newName != null
	 * @ensures \result == (getByName(newName) == null)
	 * @ensures (\result == true) ==> l.getName().equals(newName) && addList(l) is called.
	 */
	public boolean renameList(QuestionList l, String newName){
		QuestionList k = this.getByName(newName);
		if (k!=null){
			return false;
		}
		int oldIndex = myBinarySearchIndexOf(getLists(), l, QuestionList.comparatorById());
		listsSortedById.remove(oldIndex);
		l.setName(newName);
		return addList(l);
	}

	/**
	 * Adds an existing user-level tag to a specific {@link QuestionList}.
	 * <p>Updates the list, exports it, updates the internal {@code questionListPerTags} map, and exports the user data.</p>
	 * @param l The {@link QuestionList} to tag.
	 * @param tagName The name of the tag to add.
	 * @return {@code true} if the tag was successfully added to the list, {@code false} if the list is not in the user's collection or the tag is not defined at the user level.
	 * @requires l != null && tagName != null
	 * @ensures \result == (getById(l.getId()) != null && tagEmojiPerTagName.containsKey(tagName))
	 * @ensures (\result == true) ==> l.getTagNames().contains(tagName) && exportUserData() is called.
	 */
	public boolean addTagToList(QuestionList l, String tagName) {
		int index = myBinarySearchIndexOf(getLists(), l, QuestionList.comparatorById());
		String emoji;
		List<QuestionList> listsTagged;
		if (index >= 0) {
			emoji = tagEmojiPerTagName.getOrDefault(tagName,null);
			if (emoji != null) {
				l = get(index);
				l.addTag(tagName, emoji);
				l.exportListQuestionAsJson();
				listsTagged = questionListPerTags.get(tagName);
				if (listsTagged==null){
					listsTagged = new ArrayList<>();
				}
				listsTagged.add(l);
				questionListPerTags.put(tagName, listsTagged);
				listsSortedById.set(index, l);
				exportUserData();
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Removes a tag from a specific {@link QuestionList}.
	 * <p>Updates the list, exports it, and then attempts to delete the tag from the *user-level* master maps.
	 * This may lead to the unintended removal of the master tag definition even if other lists still use it.
	 * </p>
	 * @param l The {@link QuestionList} to remove the tag from.
	 * @param tagName The name of the tag to remove.
	 * @return {@code true} if the tag was successfully removed, {@code false} if the list did not have the tag.
	 * @requires l != null && tagName != null
	 * @ensures \result == l.getTagNames().contains(tagName)
	 * @ensures (\result == true) ==> !l.getTagNames().contains(tagName) && tagEmojiPerTagName.remove(tagName) is called.
	 */
	public boolean removeTagFromList(QuestionList l, String tagName) {
		if (!l.getTagNames().contains(tagName)) {
			return false; // Tag does not exist in the list
		}
		l.removeTag(tagName);
		l.exportListQuestionAsJson();
		tagEmojiPerTagName.remove(tagName);
		questionListPerTags.remove(tagName);
		exportUserData();
		return true;
	}

	/**
	 * Deletes a {@link QuestionList} from the user's collection and attempts to delete its local file.
	 * <p>
	 * The method rebuilds the internal tag association maps (O(n^2) complexity) after removal to ensure accuracy.
	 * </p>
	 * @param l The {@link QuestionList} to delete.
	 * @return {@code true} if the list was removed from the collection and its file was deleted, {@code false} otherwise.
	 * @requires l != null
	 * @ensures (\result == true) ==> !listsSortedById.contains(l) && the file at l.getPathToList() is deleted.
	 * @ensures tag and list maps are rebuilt.
	 */
	public boolean deleteList(QuestionList l){ // TODO one deletion really shouldnt be O(n^2) operation
		this.listsSortedById.remove(l);
		tagEmojiPerTagName.clear();
		questionListPerTags.clear();
		for(QuestionList l1: listsSortedById){
			Map<String, String> tags = l1.getEmojiPerTagName();
			tagEmojiPerTagName.putAll(l1.getEmojiPerTagName());
			for (String tagName : tags.keySet()){
				if (questionListPerTags.get(tagName)==null){
					questionListPerTags.put(tagName, new ArrayList<QuestionList>());
				}
				questionListPerTags.get(tagName).add(l1);
			}
		}
		File f = new File(l.getPathToList());
		File dest = new File(f.getParentFile().getAbsolutePath()+Constants.SEPARATOR+"tmp"+Constants.SEPARATOR+f.getName());
		
		dest.mkdirs();
		f.renameTo(dest);
		int t = 0;
		while(!f.delete()){if (++t>20){return false;}};
		return true;
	}

	/**
	 * **Static Utility Method.** Performs a custom recursive binary search on a sorted list to find the index of an element using a provided {@link Comparator}.
	 * @param <T> The type of elements in the list.
	 * @param tab The sorted list to search.
	 * @param start The starting index of the search range.
	 * @param end The ending index of the search range.
	 * @param q The element to search for.
	 * @param compare The comparator to use for comparison.
	 * @return The index of the element if found (non-negative), or a value less than zero that indicates the insertion point ($-1*start-1$).
	 * @requires tab is sorted according to compare.
	 * @ensures (\result >= 0) ==> tab.get(\result) is equal to q according to compare.
	 */
	public static <T> int myBinarySearchIndexOf(List<T> tab, int start, int end, T q, Comparator<? super T> compare){
		if (start > end){
			return -1*start-1;
		}
		int m = (start+end)/2;
		int comp = compare.compare(tab.get(m), q);
		if(comp == 0){
			return m;
		}
		if (comp >0){
			return myBinarySearchIndexOf(tab, start, m-1, q, compare);
		}
		return myBinarySearchIndexOf(tab, m+1, end, q, compare);
	}

	/**
	 * **Static Utility Method.** Performs a custom binary search on a sorted list to find the index of an element using a provided {@link Comparator}.
	 * <p>Searches the full range of the list ($0$ to $tab.size() - 1$).</p>
	 * @param <T> The type of elements in the list.
	 * @param tab The sorted list to search.
	 * @param q The element to search for.
	 * @param compare The comparator to use for comparison.
	 * @return The index of the element if found (non-negative), or a value less than zero that indicates the insertion point ($-1*start-1$).
	 * @requires tab is sorted according to compare.
	 * @ensures (\result >= 0) ==> tab.get(\result) is equal to q according to compare.
	 */
	public static <T> int myBinarySearchIndexOf(List<T> tab, T q, Comparator<? super T> compare){
		return myBinarySearchIndexOf(tab, 0, tab.size()-1, q, compare);
	}

	/**
	 * **Static Utility Method.** Performs a custom binary search on a list of {@link User} objects to find the index of a user by their ID.
	 * <p>Assumes the list is sorted by User ID and searches the specified range.</p>
	 * @param tab The sorted list of {@link User} objects.
	 * @param start The starting index of the search range.
	 * @param end The ending index of the search range.
	 * @param userId The user ID to search for.
	 * @return The index of the user if found (non-negative), or a value less than zero that indicates the insertion point ($-1*start-1$).
	 * @requires tab is sorted by User ID.
	 * @ensures (\result >= 0) ==> tab.get(\result).getId().equals(userId).
	 */
	public static int myBinarySearchUserId(List<User> tab, int start, int end, String userId){
		if (start > end){
			return -1*start-1;
		}
		int m = (start+end)/2;
		int comp = tab.get(m).getId().compareTo(userId);
		if(comp == 0){
			return m;
		}
		if (comp >0){
			return myBinarySearchUserId(tab, start, m-1, userId);
		}
		return myBinarySearchUserId(tab, m+1, end, userId);
	}

	/**
	 * **Static Utility Method.** Performs a custom binary search on a list of {@link User} objects to find the index of a user by their ID.
	 * <p>Assumes the list is sorted by User ID and searches the full range of the list ($0$ to $tab.size() - 1$).</p>
	 * @param tab The sorted list of {@link User} objects.
	 * @param userId The user ID to search for.
	 * @return The index of the user if found (non-negative), or a value less than zero that indicates the insertion point ($-1*start-1$).
	 * @requires tab is sorted by User ID.
	 * @ensures (\result >= 0) ==> tab.get(\result).getId().equals(userId).
	 */
	public static int myBinarySearchUserId(List<User> tab, String userId){
		return myBinarySearchUserId(tab, 0, tab.size()-1, userId);
	}

/**
	 * Returns an iterator over the user's {@link QuestionList} collection.
	 * @return An {@code Iterator<QuestionList>} over a copy of the list.
	 * @ensures \result is an iterator over a copy of listsSortedById.
	 */
	@Override
	public Iterator<QuestionList> iterator(){
		return getLists().iterator();
	}

	/**
	 * Provides a {@link Comparator} to sort {@link User} objects based on their user ID.
	 * @return A {@code Comparator<? super User>} that compares users by their ID string.
	 * @ensures \result.compare(e, f) == e.getId().compareTo(f.getId())
	 */
	public static Comparator<? super User> comparatorByUserId() {
		return (e, f)->(e.getId().compareTo(f.getId()));
	}

	/**
	 * Imports all local {@link QuestionList} files associated with this user ID.
	 * <p>Delegates the import logic to the {@link Users} utility class.</p>
	 * @return A map where the key is the list ID (String) and the value is the imported {@link QuestionList}.
	 */
	public Map<String, QuestionList> importLists() {
		return Users.importLists(getId());
	}

	/**
	 * Exports all of the user's {@link QuestionList} objects to their respective local JSON files.
	 * @ensures all QuestionList objects in listsSortedById have been exported to JSON.
	 */
	public void exportUserLists() {
		List<QuestionList> listsSortedById = getLists();
		for (QuestionList l : listsSortedById) {
			l.exportListQuestionAsJson();
		}
	}

	/**
	 * Generates a hash code for the {@code User} object based on the user ID.
	 * @return The hash code of the user ID.
	 * @ensures \result == getId().hashCode()
	 */
	@Override
	public int hashCode(){
		return getId().hashCode();
	}

	/**
	 * Compares this {@code User} object to another object for equality.
	 * <p>A user is equal to another user if their IDs match. A user is also equal to a {@link String} if the string matches the user ID.</p>
	 * @param o The object to compare with.
	 * @return {@code true} if the objects are equal, {@code false} otherwise.
	 * @ensures \result == (o instanceof User && getId().equals(((User)o).getId())) || (o instanceof String && getId().equals((String)o))
	 */
	@Override
	public boolean equals(Object o){
		if (this == o) {return true;}
		if(o instanceof  User) {
			User u = (User) o;
			return getId().equals(u.getId());
		}
		if(o instanceof  String) {
			String u = (String) o;
			return getId().equals(u);
		}
		return false;
	}

	/**
	 * Converts the user's data into a JSON string format, with options for single-line or pretty-printed output.
	 * @param oneLine {@code true} for a compact, single-line JSON string; {@code false} for a pretty-printed, multi-line format.
	 * @return The user data as a JSON string.
	 * @throws JsonProcessingException if an error occurs during JSON serialization.
	 * @requires Constants.MAPPER is initialized.
	 * @ensures \result is a valid JSON string representation of the user data (ID, tags, prefix, stats, preferences).
	 */
	private String toJsonUsingMapper(boolean oneLine) throws JsonProcessingException{
		String nextLine = oneLine?"":"\n";
		String res="", spc = oneLine?" ":"  ";
		res += "{"+nextLine;

		res += String.format("%s:%s%s", spc+Constants.MAPPER.writeValueAsString("userId"), Constants.MAPPER.writeValueAsString(getId()), ","+nextLine);
		res += String.format("%s:%s%s",spc+Constants.MAPPER.writeValueAsString("tagEmojiPerTagName"), "{", nextLine);
		Iterator<Entry<String, String>> iter = tagEmojiPerTagName.entrySet().iterator();
		Entry<String, String> entry2;
		while (iter.hasNext()) {
			entry2 = iter.next();
			res += String.format("%s:%s%s", spc+spc+Constants.MAPPER.writeValueAsString(entry2.getKey()), Constants.MAPPER.writeValueAsString(entry2.getValue()), (iter.hasNext()?", ":"")+nextLine);
		}
		res += spc+"},"+nextLine;
		res += String.format("%s:%s%s", spc+Constants.MAPPER.writeValueAsString("prefixe"), Constants.MAPPER.writeValueAsString(getPrefix()), ","+nextLine);
		res += String.format("%s:%s%s", spc+Constants.MAPPER.writeValueAsString("useButtons"), useButtons(), ","+nextLine);
		res += String.format("%s:%s%s", spc+Constants.MAPPER.writeValueAsString("useAutoNext"), useAutoNext(), ","+nextLine);
		res += String.format("%s:%s%s", spc+Constants.MAPPER.writeValueAsString("totalPointsEverGained"), getTotalPointsEverGained(), ","+nextLine);
		res += String.format("%s:%s%s", spc+Constants.MAPPER.writeValueAsString("numberOfGamesPlayed"), getNumberOfGamesPlayed(), nextLine);
		res +="}";
		return res;
	}

	/**
	 * Converts the user's data into a pretty-printed, multi-line JSON string.
	 * <p>A convenience method that calls {@code toJsonUsingMapper(false)}.</p>
	 * @return The user data as a JSON string, or {@code null} if a serialization error occurred.
	 * @ensures \result is a pretty-printed JSON string of user data.
	 */
	public String toJson(){
		String res=null;
		try {
			res = toJsonUsingMapper(false);
		} catch (Exception e){
			System.err.println("[ERROR] [toJsonUsingMapper() failed]"+e.getMessage());
		}
		return res;
	}

	/**
	 * Exports the user's data (prefix, stats, preferences, and tags) to the local JSON file specified by {@link #getPathToUserData()}.
	 * <p>Creates the directory structure if it does not exist.</p>
	 * @ensures the file at getPathToUserData() is created/updated with the current user data in JSON format.
	 */
	public void exportUserData(){
		String destFilePath = getPathToUserData();
		try {
			File myJson = new File(destFilePath);
			File folder = myJson.getParentFile();
			if(folder != null && !myJson.getParentFile().exists()) {
				folder.mkdirs();
			}
			BufferedWriter buff = Files.newBufferedWriter(Paths.get(destFilePath));
			buff.write(this.toJson());
			buff.close();
		} catch (IOException e) {
			System.err.println("[ERROR] An error occurred while exporting UserData."+destFilePath);
			e.printStackTrace();
		}
	}

	/**
	 * Returns a compact, single-line JSON string representation of the user's data.
	 * <p>A convenience method that calls {@code toJsonUsingMapper(true)}.</p>
	 * @return A single-line JSON string, or {@code null} if a serialization error occurred.
	 * @ensures \result is a single-line JSON string of user data.
	 */
	@Override
	public String toString() {
		String res=null;
		try {
			res = toJsonUsingMapper(true);
		} catch (Exception e){
			System.err.println("[ERROR] [toJsonUsingMapper() failed]"+e.getMessage());
		}
		return res;
	}
}
