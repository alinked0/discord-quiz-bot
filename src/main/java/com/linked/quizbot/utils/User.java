package com.linked.quizbot.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
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
	private final Map<String, QuestionList> lists= new HashMap<>();
	private final Map<String, String> tagEmojiByTagName= new HashMap<>();
	private final Map<String, List<String>> questionListPerTags= new HashMap<>();
	private final Map<String, List<Attempt>> attemptsByListId= new HashMap<>();
	private String prefix;
	private Boolean useButtons;
	private Boolean useAutoNext;
	
	/**
	 * Inner class implementing the Builder pattern for creating and initializing {@link User} objects.
	 * <p>
	 * This allows for optional parameter setting and a more readable and robust construction process.
	 * </p>
	 */
	public static class Builder {
		private String userId = null;
		public String prefix = null;
		private List<QuestionList> list = new ArrayList<>();
		private Boolean useButtons = true;
		private Boolean useAutoNext = false;
		private Map<String, String> tagEmojiByTagName= new HashMap<>();
		private Map<String, List<String>> questionListPerTags= new HashMap<>();
		private Map<String, List<Attempt>> attemptsByListId= new HashMap<>();
		
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
		 * @param prefix The custom command prefix.
		 * @return The current Builder instance for chaining.
		 * @ensures this.prefix == prefix
		 */
		public Builder prefix(String prefix){
			this.prefix= prefix;
			return this;
		}
		
		/**
		 * Sets the preference for using interactive buttons in the bot.
		 * @param b {@code true} to use buttons, {@code false} otherwise. Defaults to {@code true}.
		 * @return The current Builder instance for chaining.
		 * @ensures this.useButtons == b
		 */
		public Builder useButtons(Boolean b){
			this.useButtons= b;
			return this;
		}
		
		/**
		 * Sets the preference for automatically advancing to the next question.
		 * @param b {@code true} to use auto-next, {@code false} otherwise. Defaults to {@code false}.
		 * @return The current Builder instance for chaining.
		 * @ensures this.useAutoNext == b
		 */
		public Builder useAutoNext(Boolean b){
			this.useAutoNext= b;
			return this;
		}
		
		/**
		 * Sets the map of tag names to their associated emojis.
		 * @param tagEmojiByTagName A map of tag names (String) to emoji representations (String).
		 * @return The current Builder instance for chaining.
		 * @ensures this.tagEmojiByTagName.equals(tagEmojiByTagName)
		 */
		public Builder tagEmojiByTagName(Map<String, String> tagEmojiByTagName){
			this.tagEmojiByTagName = tagEmojiByTagName;
			return this;
		}
		public Builder attemptsByListId(Map<String, List<Attempt>>attemptsByListId){
			this.attemptsByListId = attemptsByListId;
			return this;
		}
		
		/**
		 * Adds a single tag name and its emoji to the map.
		 * @param tagName The name of the tag.
		 * @param emoji The emoji associated with the tag.
		 * @return The current Builder instance for chaining.
		 * @requires tagName != null && emoji != null
		 * @ensures this.tagEmojiByTagName.containsKey(tagName)
		 */
		public  Builder addTag(String tagName, String emoji){
			this.tagEmojiByTagName.put(tagName, emoji);
			return this;
		}
		
		/**
		 * Adds all entries from a map of tags to the current tag map.
		 * @param tags A map of tag names (String) to emoji representations (String).
		 * @return The current Builder instance for chaining.
		 * @requires tags != null
		 * @ensures this.tagEmojiByTagName.keySet().containsAll(tags.keySet())
		 */
		public  Builder addTags(Map<String,String> tags){
			this.tagEmojiByTagName.putAll(tags);
			return this;
		}
		
		/**
		 * Sets the map associating tag names with lists of questions lists that have that tag.
		 * @param questionListPerTags A map where the key is the tag name (String) and the value is a list of ids of {@link QuestionList}.
		 * @return The current Builder instance for chaining.
		 * @ensures this.questionListPerTags.equals(questionListPerTags)
		 */
		public Builder questionListPerTags(Map<String, List<String>> questionListPerTags){
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
	
	public static class Parser {
		public static User.Builder fromJsonFile(String filePathToJson)throws IOException{
			File f = new File(filePathToJson);
			if (!f.exists()){
				throw new FileNotFoundException(filePathToJson);
			}
			JsonParser jp =  new JsonFactory().createParser(f);
			return parse(jp, filePathToJson);
		}
		
		public static User.Builder fromString(String arg) throws IOException{
			JsonParser jp =  new JsonFactory().createParser(arg);
			return parse(jp, arg);
		}
		
		public static User.Builder parse(JsonParser jp, String original) throws IOException{
			if (jp.currentToken() != JsonToken.START_OBJECT && jp.nextToken() != JsonToken.START_OBJECT){
				throw new IOException(String.format(Constants.ERROR+Constants.RED+"User.Parser.parse, input is not a json: (%s, %s, %s) (%s, %s, %s) \n%s\n"+Constants.RESET,  jp.currentToken(), jp.currentName(), jp.getText(), jp.nextValue(), jp.nextFieldName(), jp.nextTextValue(), original));
			}
			User.Builder userBuilder = new User.Builder();
			String fieldName;
			while (!jp.isClosed()){
				if (jp.currentToken() == JsonToken.FIELD_NAME) {
					fieldName = jp.currentName();
					jp.nextToken();
					/* parsing System.out.print("User.Parser.Parser.parse("+jp.currentToken()+", "+jp.currentName()+") "); */
					switch (fieldName){
						case "userId", "id" -> {
							userBuilder.id(jp.getText());
						}
						case "prefix", "prefixe" -> {
							userBuilder.prefix(jp.getText());
						}
						case "tagEmojiByTagName" -> {
							userBuilder.tagEmojiByTagName(parseEmojiPerTagName(jp, original));
						}
						case "attemptsByListId", "attempts" -> {
							userBuilder.attemptsByListId(parseAttempts(jp, original));
						}
						case "useButtons" -> {
							userBuilder.useButtons(jp.getValueAsBoolean());
						}
						case "useAutoNext" -> {
							userBuilder.useAutoNext(jp.getValueAsBoolean());
						}
						default -> {
							jp.skipChildren();
						}
					}
				} else if (jp.currentToken() == JsonToken.END_OBJECT){
					jp.nextToken();
					/* parsing System.out.println("User.Parser.Parser.parse("+jp.currentToken()+", "+jp.currentName()+") "); */
					break;
				} else {
					jp.nextToken();
					/* parsing System.out.print("User.Parser.Parser.parse("+jp.currentToken()+", "+jp.currentName()+") "); */
				}
			}
			return userBuilder;
		}
		public static Map<String, String> parseEmojiPerTagName(JsonParser jp, String original) throws IOException{
			String tagName;
			String emoji;
			Map<String, String> m = new HashMap<>();
			if (jp.currentToken() != JsonToken.START_OBJECT && jp.nextToken() != JsonToken.START_OBJECT) {
				throw new IOException(String.format(Constants.ERROR+Constants.RED+"User.Parser.parseEmojiPerTagName, input is not a json: (%s, %s, %s) (%s, %s, %s) \n%s\n"+Constants.RESET,  jp.currentToken(), jp.currentName(), jp.getText(), jp.nextValue(), jp.nextFieldName(), jp.nextTextValue(), original));
			}
			while (!jp.isClosed()) {
				/* parsing System.out.print("User.Parser.Parser.parseEmojiPerTagName("+jp.currentToken()+", "+jp.currentName()+") "); */
				if (jp.currentToken() == JsonToken.FIELD_NAME) {
					tagName = jp.currentName();
					jp.nextToken();
					/* parsing System.out.print("User.Parser.Parser.parseEmojiPerTagName("+jp.currentToken()+", "+jp.currentName()+") "); */
					emoji = jp.getText();
					m.put(tagName, emoji);
				} else if(jp.currentToken() == JsonToken.END_OBJECT){
					jp.nextToken();
					/* parsing System.out.println("User.Parser.Parser.parseEmojiPerTagName("+jp.currentToken()+", "+jp.currentName()+") "); */
					break;
				} else{
					jp.nextToken();
					/* parsing System.out.print("User.Parser.Parser.parseEmojiPerTagName("+jp.currentToken()+", "+jp.currentName()+") "); */
				}
			}
			return m;
		}
		public static Map<String, List<Attempt>> parseAttempts(JsonParser jp, String original) throws IOException{
			Map<String, List<Attempt>> attemptsByListId= new HashMap<>();
			String listId;
			List<Attempt> att;
			if (jp.currentToken() != JsonToken.START_OBJECT && jp.nextToken() != JsonToken.START_OBJECT) {
				throw new IOException(String.format(Constants.ERROR+Constants.RED+"User.Parser.parseAttempts, input is not a json: (%s, %s, %s) (%s, %s, %s) %s"+Constants.RESET,  jp.currentToken(), jp.currentName(), jp.getText(), jp.nextValue(), jp.nextFieldName(), jp.nextTextValue(), original));
			}
			
			while (!jp.isClosed()) {
				/* parsing System.out.print("User.Parser.Parser.parseAttempts("+jp.currentToken()+", "+jp.currentName()+") "); */
				if (jp.currentToken() == JsonToken.FIELD_NAME) {
					listId = jp.currentName();
					jp.nextToken();
					/* parsing System.out.print("User.Parser.Parser.parseAttempts("+jp.currentToken()+", "+jp.currentName()+") "); */
					att = Attempt.Parser.parseList(jp, original);
					attemptsByListId.put(listId, att);
				}else if(jp.currentToken() == JsonToken.END_OBJECT){
					jp.nextToken();
					/* parsing System.out.println("User.Parser.Parser.parseAttempts("+jp.currentToken()+", "+jp.currentName()+") "); */
					break;
				}  else{
					jp.nextToken();
					/* parsing System.out.print("User.Parser.Parser.parseAttempts("+jp.currentToken()+", "+jp.currentName()+") "); */
				}
			}
			return attemptsByListId;
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
		if (new File(getPathToUserData()).exists()){
			try {
				User.Builder builder0 = User.Parser.fromJsonFile(getPathToUserData());
				this.prefix = builder0.prefix;
				this.tagEmojiByTagName.putAll(builder0.tagEmojiByTagName);
				this.questionListPerTags.putAll(builder0.questionListPerTags);
				this.attemptsByListId.putAll(builder0.attemptsByListId);
				this.useButtons = builder0.useButtons;
				this.useAutoNext = builder0.useAutoNext;
			} catch (IOException e){
				System.err.printf(Constants.ERROR + Constants.RED+"Import failed: %s", getPathToUserData());
				e.printStackTrace();
				System.err.print(Constants.RESET);
			}
		}
		if (builder.prefix!=null)this.prefix = builder.prefix;
		this.tagEmojiByTagName.putAll(builder.tagEmojiByTagName);
		this.questionListPerTags.putAll(builder.questionListPerTags);
		this.attemptsByListId.putAll(builder.attemptsByListId);
		for (QuestionList q : builder.list){
			this.lists.put(q.getId(), q);
		}
		this.useButtons = builder.useButtons;
		this.useAutoNext = builder.useAutoNext;
		Map<String, String> tags;
		for(String l: lists.keySet()){
			QuestionList.Hasher.addGeneratedCode(l);
		}
		for(QuestionList l: importLists().values()){
			addList(l);
		}
		for(QuestionList l: lists.values()){
			tags = l.getEmojiPerTagName();
			tagEmojiByTagName.putAll(tags);
			for (String tagName : tags.keySet()){
				if (questionListPerTags.get(tagName)==null){
					questionListPerTags.put(tagName, new ArrayList<>());
				}
				questionListPerTags.get(tagName).add(l.getId());
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
		this.userId = userId;
		String pathToUserData = Constants.USERDATAPATH+Constants.SEPARATOR+userId+Constants.SEPARATOR+"user-data.json";
		if (new File(pathToUserData).exists()){
			try {
				User.Builder builder0 = User.Parser.fromJsonFile(pathToUserData);
				this.prefix = builder0.prefix;
				this.tagEmojiByTagName.putAll(builder0.tagEmojiByTagName);
				this.questionListPerTags.putAll(builder0.questionListPerTags);
				this.attemptsByListId.putAll(builder0.attemptsByListId);
				this.useButtons = builder0.useButtons;
				this.useAutoNext = builder0.useAutoNext;
				for (QuestionList q : builder0.list){
					this.lists.put(q.getId(), q);
				}
			} catch (IOException e){
				System.err.printf(Constants.ERROR + Constants.RED+"Import failed: %s", getPathToUserData());
				e.printStackTrace();
				System.err.print(Constants.RESET);
			}
		}
		Map<String, String> tags;
		for(String l: lists.keySet()){
			QuestionList.Hasher.addGeneratedCode(l);
		}
		for(QuestionList l: importLists().values()){
			addList(l);
		}
		for(QuestionList l: lists.values()){
			tags = l.getEmojiPerTagName();
			tagEmojiByTagName.putAll(tags);
			for (String tagName : tags.keySet()){
				if (questionListPerTags.get(tagName)==null){
					questionListPerTags.put(tagName, new ArrayList<>());
				}
				questionListPerTags.get(tagName).add(l.getId());
			}
		}
	}
	
	/**
	 * Checks if the user has enabled the use of buttons for interaction.
	 * @return {@code true} if buttons are enabled, {@code false} otherwise.
	 * @ensures \result == useButtons
	 */
	public Boolean useButtons(){return useButtons;}
	public Boolean getUseButtons(){return useButtons;}
	
	/**
	 * Sets the preference for using interactive buttons and does not automatically export data.
	 * @param b {@code true} to enable buttons.
	 * @ensures this.useButtons == b
	 */
	public void useButtons(Boolean b){ useButtons = b;}
	
	/**
	 * Checks if the user has enabled the auto-next feature.
	 * @return {@code true} if auto-next is enabled, {@code false} otherwise.
	 * @ensures \result == useAutoNext
	 */
	public Boolean useAutoNext(){return useAutoNext;}
	public Boolean getUseAutoNext(){return useAutoNext;}
	
	/**
	 * Sets the preference for automatically advancing to the next question and does not automatically export data.
	 * @param b {@code true} to enable auto-next.
	 * @ensures this.useAutoNext == b
	 */
	public void useAutoNext(Boolean b){ useAutoNext = b;}
	
	/**
	 * Gets the custom command prefix for the user.
	 * @return The command prefix string.
	 * @ensures \result == prefix
	 * @pure
	 */
	public String getPrefix(){return prefix;}
	
	/**
	 * Sets a new custom command prefix for the user and immediately exports the user data.
	 * @param prefix The new command prefix.
	 * @ensures this.prefix == prefix
	 * @ensures exportUserData() is called
	 */
	public void setPrefix(String prefix){
		this.prefix= prefix;
		exportUserData();
	}
	
	/**
	 * Gets a copy of the user's sorted list of quiz lists.
	 * @return A new {@code List<QuestionList>} containing all of the user's quizzes, sorted by ID.
	 * @ensures \result is a new list instance
	 * @ensures \result.size() == listsSortedById.size()
	 * @pure
	 */
	public Map<String, QuestionList> getLists() {
		Map<String, QuestionList> res = new HashMap<>(lists);
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
	 * Gets a copy of the map linking tag names to their associated emojis.
	 * @return A new {@code Map<String, String>} containing all user tags and emojis.
	 * @ensures \result.size() == tagEmojiByTagName.size()
	 */
	public Map<String, String> getEmojiPerTagName() {
		return new HashMap<>(tagEmojiByTagName);
	}
	
	/**
	 * Gets a copy of the map linking tag names to the quiz list ids that have that tag.
	 * @return A new {@code Map<String, List<String>>} mapping tags to listIds.
	 * @ensures \result.size() == questionListPerTags.size()
	 */
	public Map<String, List<String>> getQuestionListPerTags() {
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
		if (!questionListPerTags.containsKey(tagName)) return new ArrayList<>();
		return questionListPerTags.get(tagName).stream().map(id -> getById(id)).toList();
	}
	
	/**
	 * Gets a set of all tag names defined by the user.
	 * @return A {@code Set<String>} of all tag names.
	 * @ensures \result.equals(tagEmojiByTagName.keySet())
	 */
	public Set<String> tagNames() {
		return tagEmojiByTagName.keySet();
	}
	
	/**
	 * Retrieves a {@link QuestionList} by its unique ID.
	 * <p>Performs a binary search on the internally ID-sorted list. Checks for a static example list first.</p>
	 * @param id The unique ID of the quiz list.
	 * @return The matching {@link QuestionList}, or {@code null} if not found.
	 * @ensures (\result == null) || (\result.getId().equals(id))
	 */
	public QuestionList getById(String listId) {
		if (QuestionList.getExampleQuestionList().getId().equals(listId)) return QuestionList.getExampleQuestionList();
		return lists.get(listId);
	}
	
	/**
	 * Retrieves a {@link QuestionList} by its name.
	 * <p>Temporarily sorts a copy of the list by name to perform a binary search.</p>
	 * @param listName The name of the quiz list.
	 * @return The matching {@link QuestionList}, or {@code null} if not found.
	 * @ensures (\result == null) || (\result.getName().equals(listName))
	 */
	public QuestionList getByName(String listName){
		List<QuestionList> listsSortedByName = new ArrayList<>(lists.values());
		listsSortedByName.sort(QuestionList.comparatorByName());
		int index = QuestionList.myBinarySearchIndexOf(listsSortedByName, listName);
		if (index<0) return null;
		return listsSortedByName.get(index);
	}
	
	/**
	 * Retrieves the emoji associated with a given tag name for this user.
	 * @param tagName The name of the tag.
	 * @return The emoji string, or {@code null} if the tag is not defined.
	 * @ensures \result == tagEmojiByTagName.get(tagName)
	 */
	public String getEmojiFomTagName(String tagName){
		return tagEmojiByTagName.get(tagName);
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
	public Boolean addList(@NotNull QuestionList l){
		QuestionList k = getById(l.getId());
		
		if (k==null){
			k = getByName(l.getName());
			if (k==null){
				k = l;
			}
		}
		
		k.addAll(l);
		k.rearrageOptions((e, f) -> e.isCorrect()?-1:1);
		k.setOwnerId(userId);
		lists.put(k.getId(), k);
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
	 * @ensures \result == !tagEmojiByTagName.containsKey(tagName)
	 * @ensures (\result == true) ==> tagEmojiByTagName.containsKey(tagName) && questionListPerTags.containsKey(tagName) && exportUserData() is called.
	 */
	public Boolean createTag(@NotNull String tagName, @NotNull String emoji) {
		if (tagEmojiByTagName.containsKey(tagName)) {
			return false; // Tag already exists
		}
		tagEmojiByTagName.put(tagName, emoji);
		questionListPerTags.put(tagName, new ArrayList<>());
		exportUserData();
		Users.update(this);
		return true;
	}
	
	/**
	 * Adds an existing user-level tag (name and emoji) to a specific {@link QuestionList} by its ID.
	 * @param tagName The name of the tag to add.
	 * @param emoji The emoji associated with the tag (used to pass to QuestionList).
	 * @param id The ID of the {@link QuestionList} to tag.
	 * @return {@code true} if the tag was successfully added to the list, {@code false} if the tag has not been created at the user level.
	 * @requires id != null && tagName != null && emoji != null
	 * @ensures \result == tagEmojiByTagName.containsKey(tagName)
	 * @ensures (\result == true) ==> getById(id).tagNames().contains(tagName) && exportUserData() is called.
	 */
	public Boolean addTagToQuestionList(String tagName, String emoji, String id) {
		if (!tagEmojiByTagName.containsKey(tagName)) {
			return false; // Tag hasnt been created
		}
		getById(id).addTag(tagName, emoji);
		exportUserData();
		Users.update(this);
		return true;
	}
	
	/**
	 * Deletes a tag from the user's master list, removing it from the {@code tagEmojiByTagName} and {@code questionListPerTags} maps.
	 * <p>It is important to note this method does *not* remove the tag from individual {@link QuestionList} objects.</p>
	 * @param tagName The name of the tag to delete.
	 * @return {@code true} if the tag was deleted, {@code false} if the tag did not exist.
	 * @requires tagName != null
	 * @ensures \result == tagEmojiByTagName.containsKey(tagName)
	 * @ensures (\result == true) ==> !tagEmojiByTagName.containsKey(tagName) && !questionListPerTags.containsKey(tagName) && exportUserData() is called.
	 */
	public Boolean deleteTag(String tagName) {
		if (!tagEmojiByTagName.containsKey(tagName)) {
			return false; // Tag does not exist
		}
		tagEmojiByTagName.remove(tagName);
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
	public Boolean renameList(QuestionList l, String newName){
		QuestionList k = this.getByName(newName);
		if (k!=null){
			return false;
		}
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
	 * @ensures \result == (getById(l.getId()) != null && tagEmojiByTagName.containsKey(tagName))
	 * @ensures (\result == true) ==> l.tagNames().contains(tagName) && exportUserData() is called.
	 */
	public Boolean addTagToList(QuestionList l, String tagName) {
		String emoji;
		List<String> listsTagged;
		if (tagNames().contains(tagName)) {
			emoji = tagEmojiByTagName.get(tagName);
			l.addTag(tagName, emoji);
			listsTagged = questionListPerTags.get(tagName);
			if (listsTagged==null){
				listsTagged = new ArrayList<>();
			}
			listsTagged.add(l.getId());
			questionListPerTags.put(tagName, listsTagged);
			lists.put(l.getId(), l);
			l.exportListQuestionAsJson();
			exportUserData();
			Users.update(this);
			return true;
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
	 * @ensures \result == l.tagNames().contains(tagName)
	 * @ensures (\result == true) ==> !l.tagNames().contains(tagName) && tagEmojiByTagName.remove(tagName) is called.
	 */
	public Boolean removeTagFromList(QuestionList l, String tagName) {
		if (!l.tagNames().contains(tagName)) {
			return false; // Tag does not exist in the list
		}
		l.removeTag(tagName);
		l.exportListQuestionAsJson();
		tagEmojiByTagName.remove(tagName);
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
	 * @ensures (\result == true) ==> !listsSortedById.contains(l) && the file at l.pathToList() is deleted.
	 * @ensures tag and list maps are rebuilt.
	 */
	public Boolean deleteList(QuestionList l){ // TODO one deletion really shouldnt be O(n^2) operation
		this.lists.remove(l.getId());
		tagEmojiByTagName.clear();
		questionListPerTags.clear();
		for(QuestionList l1: lists.values()){
			Map<String, String> tags = l1.getEmojiPerTagName();
			tagEmojiByTagName.putAll(l1.getEmojiPerTagName());
			for (String tagName : tags.keySet()){
				if (questionListPerTags.get(tagName)==null){
					questionListPerTags.put(tagName, new ArrayList<>());
				}
				questionListPerTags.get(tagName).add(l1.getId());
			}
		}
		File f = new File(l.pathToList());
		File dest = new File(f.getParentFile().getAbsolutePath()+Constants.SEPARATOR+"tmp"+Constants.SEPARATOR+f.getName());
		
		dest.mkdirs();
		f.renameTo(dest);
		int t = 0;
		while(!f.delete()){if (++t>20){return false;}};
		return true;
	}
	
	/*TODO add docs */
	public void addAttempt(String listId, Attempt att){
		List<Attempt> last = attemptsByListId.getOrDefault(listId, new ArrayList<>());
		last.addFirst(att);
		attemptsByListId.put(listId, last);
		
		Users.update(this);
	}
	
	/*TODO add docs */
	public List<Attempt> getAttempts(String listId){
		return attemptsByListId.getOrDefault(listId, List.of());
	}
	
	/*TODO add docs */
	public Map<String, List<Attempt>> getAttemptsByListId(){
		return attemptsByListId;
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
		return getLists().values().iterator();
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
		for (QuestionList l : getLists().values()) {
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
	 * Converts the user's data into a pretty-printed, multi-line JSON string.
	 * <p>A convenience method that calls {@code toJsonUsingMapper(false)}.</p>
	 * @return The user data as a JSON string, or {@code null} if a serialization error occurred.
	 * @ensures \result is a pretty-printed JSON string of user data.
	 */
	public String toJson(){
		/*try {
			return Constants.MAPPER.writer(new DefaultPrettyPrinter()).writeValueAsString(this);
		}catch(JsonProcessingException e){System.err.print(Constants.ERROR);e.printStackTrace();}*/
		return toJsonUsingMapper(false);
	}
	
	/**
	 * Converts the user's data into a JSON string format, with options for single-line or pretty-printed output.
	 * @param oneLine {@code true} for a compact, single-line JSON string; {@code false} for a pretty-printed, multi-line format.
	 * @return The user data as a JSON string.
	 * @throws JsonProcessingException if an error occurs during JSON serialization.
	 * @requires Constants.MAPPER is initialized.
	 * @ensures \result is a valid JSON string representation of the user data (ID, tags, prefix, stats, preferences).
	 */
	private String toJsonUsingMapper(Boolean oneLine) {
		String nextLine = oneLine?"":"\n";
		String res="", spc = oneLine?" ":"  ";
		try {
		res += "{"+nextLine;
		res += String.format(spc+"\"%s\":%s%s", "id", Constants.MAPPER.writeValueAsString(getId()), ","+nextLine);
		res += String.format(spc+"\"%s\":%s%s", "tagEmojiByTagName",Constants.MAPPER.writeValueAsString(tagEmojiByTagName), ","+nextLine);
		if (getPrefix()!=null) res += String.format(spc+"\"%s\":%s%s", "prefix", Constants.MAPPER.writeValueAsString(getPrefix()), ","+nextLine);
		res += String.format(spc+"\"%s\":%s%s", "useButtons", useButtons(), ","+nextLine);
		res += String.format(spc+"\"%s\":%s%s", "useAutoNext", useAutoNext(), ","+nextLine);
		res += String.format(spc+"\"%s\":%s%s", "attemptsByListId", Constants.MAPPER.writeValueAsString(attemptsByListId), nextLine);
		res +="}";
		return res;
		}catch(JsonProcessingException e){System.err.print(Constants.ERROR);e.printStackTrace();}
		return null;
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
			System.err.println(Constants.ERROR + "An error occurred while exporting UserData."+destFilePath);
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
			res = toJson();//Constants.MAPPER.writeValueAsString(this);
		} catch (Exception e){
			System.err.println(Constants.ERROR + "[toJsonUsingMapper() failed]"+e.getMessage());
		}
		return res;
	}
}
