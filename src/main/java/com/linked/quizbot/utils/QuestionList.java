package com.linked.quizbot.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jetbrains.annotations.Nullable;

import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.UUID;

import javax.management.InvalidAttributeValueException;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.linked.quizbot.Constants;
import com.linked.quizbot.core.BotCore;

import net.dv8tion.jda.api.entities.emoji.Emoji;

/**
 * The QuestionList is a class designed to manage a collection
 * of {@link Question} objects. It includes additional metadata such as author ID, and name, and provides
 * utility methods for importing and exporting the list in JSON format.
 *
 * <p>This class is primarily intended for use in quiz or survey applications, where a structured collection
 * of questions needs to be maintained along with associated metadata.</p>
 *
 * <h2>Features:</h2>
 * <ul>
 *   <li>Supports creating instances with metadata (author ID and name) and an initial collection of questions.</li>
 *   <li>Facilitates JSON import/export of the question list.</li>
 *   <li>Automatically handles file naming and path generation based on metadata attributes.</li>
 *   <li>Overrides methods like {@code toString()}, {@code equals()}, and {@code hashCode()} for custom behavior.</li>
 * </ul>
 *
 * <h2>Examples:</h2>
 * <pre>
 * // Creating a new QuestionList with metadata and adding questions
 * QuestionList list = new QuestionList("001", "Trivia Night", "Science");
 * list.add(new Question("What is the chemical symbol for water?", 1, "H2O", "CO2"));
 *
 * // Exporting the list to a JSON file
 * list.exportListQuestionAsJson();
 *
 * // Importing a QuestionList from a JSON file
 * QuestionList importedList = new QuestionList("path/to/json/file.json");
 * </pre>
 *
 * @see Question
 * @see Option
 * @author alinked0
 * @version 1.0
 * @since 2025-02-01
 */
public class QuestionList implements Iterable<Question>{
	private String authorId;
	private Map<String,String> emojiPerTagName;
	private List<Question> questions;
	private String name;
	public static double pointsForCorrect = 1.00;
	public static double pointsForIncorrect = -0.25;
	private long timeCreatedMillis;
	private String id; //TODO the id should be made final
	
	static {
		getExampleQuestionList().exportListQuestionAsJson();
	}
	
	/**
	 * Inner class implementing the **Builder pattern** for creating and initializing {@link QuestionList} objects.
	 * <p>
	 * Allows for step-by-step construction of a quiz list by setting metadata (author, name, ID) and adding questions.
	 * </p>
	 */
	public static class Builder {
		public final List<Question> list= new ArrayList<>();
		public final Map<String,String> emojiPerTagName= new HashMap<>();
		public String authorId= null;
		public String name= null;
		public String id= null;
		public long timeCreatedMillis= 0L;
		
		/**
		 * Sets the ID of the author for the quiz list.
		 * @param authorId The ID of the author (e.g., a user ID).
		 * @return The current Builder instance for chaining.
		 * @requires authorId != null
		 * @ensures this.authorId == authorId
		 */
		public  Builder authorId(String authorId){
			this.authorId = authorId;
			return this;
		}
		
		/**
		 * Adds a single tag-emoji pair to the list's tag map.
		 * @param tagName The name of the tag (e.g., "Math", "Science").
		 * @param emoji The emoji string associated with the tag (e.g., "$\#$").
		 * @return The current Builder instance for chaining.
		 * @requires tagName != null && emoji != null
		 * @ensures this.emojiPerTagName.containsKey(tagName)
		 */
		public  Builder addTag(String tagName, String emoji){
			this.emojiPerTagName.put(tagName, emoji);
			return this;
		}
		
		
		/**
		 * Adds all tag-emoji pairs from a map to the list's tag map. Existing keys are overwritten.
		 * @param emojiPerTagName A map of tags and their corresponding emoji icons.
		 * @return The current Builder instance for chaining.
		 * @requires emojiPerTagName != null
		 * @ensures this.emojiPerTagName contains all entries from the input map.
		 */
		public  Builder addTags(Map<String,String> emojiPerTagName){
			this.emojiPerTagName.putAll(emojiPerTagName);
			return this;
		}
		
		
		/**
		 * Sets the display name for the quiz list.
		 * @param listName The name of the list.
		 * @return The current Builder instance for chaining.
		 * @requires listName != null
		 * @ensures this.name == listName
		 */
		public  Builder name(String listName){
			this.name = listName;
			return this;
		}
		
		
		/**
		 * Sets the unique identifier (ID) for the quiz list.
		 * @param id The unique ID for the list.
		 * @return The current Builder instance for chaining.
		 * @requires id != null
		 * @ensures this.id == id
		 */
		public  Builder id(String id){
			this.id = id;
			return this;
		}
		
		/**
		 * Sets the creation timestamp for the quiz list.
		 * @param timeCreatedMillis The time of creation in milliseconds.
		 * @return The current Builder instance for chaining.
		 * @requires timeCreatedMillis >= 0L
		 * @ensures this.timeCreatedMillis == timeCreatedMillis
		 */
		public  Builder timeCreatedMillis(long timeCreatedMillis){
			this.timeCreatedMillis = timeCreatedMillis;
			return this;
		}
		
		/**
		 * Adds a single {@link Question} to the list of questions.
		 * @param q The question to add.
		 * @return The current Builder instance for chaining.
		 * @requires q != null
		 * @ensures this.list.contains(q)
		 * @ensures this.list.size() == \old(this.list.size()) + 1
		 */
		public Builder add(Question q){
			if (!list.contains(q))	list.add(q);
			return this;
		}
		
		/**
		 * Merges content from an existing {@link QuestionList} into this builder.
		 * <p>Questions are deep-cloned. Tags are merged. Metadata (ID, name, author) is only set if not already present in the builder.
		 * The earliest creation timestamp is retained.</p>
		 * @param questions The existing QuestionList to merge from.
		 * @return The current Builder instance for chaining.
		 * @requires questions != null
		 * @ensures this.list contains clones of all questions from the input list.
		 */
		public Builder add(QuestionList questions){
			for (Question q : questions){
				this.add(q);
			}
			this.addTags(questions.getEmojiPerTagName());
			if (id == null)this.id(questions.getId());
			if (name==null)this.name(questions.getName());
			if (authorId==null)this.authorId(questions.getAuthorId());
			if (timeCreatedMillis==0L || questions.getTimeCreatedMillis()<timeCreatedMillis) this.timeCreatedMillis(questions.getTimeCreatedMillis());
			return this;
		}
		
		/**
		 * Adds a collection of {@link Question} objects to the list.
		 * @param c The list or collection of questions to add.
		 * @return The current Builder instance for chaining.
		 * @requires c != null
		 * @ensures this.list contains all elements from c.
		 */
		public Builder addAll(List<? extends Question> c){
			for (Question q : c)	if (!list.contains(q))	list.add(q);
			return this;
		}
		/**
		 * Constructs the final immutable {@link QuestionList} object.
		 * @return The newly constructed {@link QuestionList} object.
		 * @requires this.name != null
		 * @ensures \result != null
		 * @ensures \result.getQuestions().equals(this.list)
		 */
		public QuestionList build(){
			return new QuestionList(this);
		}
	}
	
	/**
	 * QuestionList.Hasher is a utility class for generating unique, short, alphanumeric codes
	 * based on an input string and a timestamp. It uses SHA-256 hashing and Base-36 encoding
	 * to create a code that is guaranteed to be unique for the same input and timestamp.
	 * <p>
	 * Most of this code was written by chatgpt.
	 * </p>
	 */
	public static class Hasher {
		
		public static final char[] BASE36_ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyz".toCharArray();
		public static final int DEFAULT_LENGTH = 7;
		public static final Set<String> generatedCodes = new HashSet<>();
		
		/**
		 * Adds a newly generated code to the set of codes currently in use.
		 * @param code The code to add.
		 * @requires code != null
		 * @ensures generatedCodes.contains(code)
		 */
		public static void addGeneratedCode(String code){
			Hasher.generatedCodes.add(code);
		}
		
		/**
		 * Clears the set of generated codes, allowing codes to be reused in a new session.
		 * @ensures generatedCodes.isEmpty()
		 */
		public static void clearGeneratedCodes(){
			generatedCodes.clear();
		}
		
		
		/**
		 * Generates a unique, short hash code based on a string input and a timestamp.
		 * <p>The method ensures the code is not already in {@link #generatedCodes} using random components and retries.</p>
		 * @param input The base string input for hashing (e.g., list name).
		 * @param timestamp The creation time or other timestamp to ensure entropy.
		 * @return A unique alphanumeric hash code of {@link #DEFAULT_LENGTH}.
		 * @throws RuntimeException if too many hash collisions occur (10,000 attempts).
		 * @requires input != null
		 * @ensures \result != null && \result.length() == DEFAULT_LENGTH
		 * @ensures generatedCodes.contains(\result)
		 */
		public static String generate(String input, long timestamp) {
			String code;
			int attempts = 0;
			
			do {
				String randomComponent = UUID.randomUUID().toString();
				String combined = input + "|" + timestamp + "|" + randomComponent;
				code = createCode(combined);
				attempts++;
				
				// Just in case something goes wrong
				if (attempts > 10_000) {
					throw new RuntimeException("Too many hash collisions. Aborting.");
				}
			} while (generatedCodes.contains(code));
			
			Hasher.addGeneratedCode(code);
			return code;
		}
		
		/**
		 * Generates a unique, short hash code for a given {@link QuestionList} object.
		 * <p>The hash is primarily based on the list's author ID and name, combined with the creation timestamp.</p>
		 * @param l The QuestionList object for which to generate the hash.
		 * @return A unique alphanumeric hash code of {@link #DEFAULT_LENGTH}.
		 * @throws RuntimeException if too many hash collisions occur (10,000 attempts).
		 * @requires l != null
		 * @ensures \result != null && \result.length() == DEFAULT_LENGTH
		 * @ensures generatedCodes.contains(\result)
		 */
		public static String generate(QuestionList l) {
			String code, input = l.getAuthorId() + l.getName();
			long timestamp = l.getTimeCreatedMillis();
			int attempts = 0;
			
			do {
				String randomComponent = UUID.randomUUID().toString();
				String combined = input + "|" + timestamp + "|" + randomComponent;
				code = createCode(combined);
				attempts++;
				
				// Just in case something goes wrong
				if (attempts > 10_000) {
					throw new RuntimeException("Too many hash collisions. Aborting.");
				}
			} while (generatedCodes.contains(code));
			
			Hasher.addGeneratedCode(code);
			return code;
		}
		
		/**
		 * Internal method to create a Base-36 encoded, truncated code from a combined string input.
		 * <p>Uses SHA-256 hashing on the input, converts the hash to Base-36, prepends 'a' if necessary, and truncates/pads to {@link #DEFAULT_LENGTH}.</p>
		 * @param combinedInput The input string (including random elements) to be hashed.
		 * @return A Base-36 encoded string based on the hash.
		 * @throws RuntimeException if SHA-256 algorithm is not available.
		 * @requires combinedInput != null
		 */
		private static String createCode(String combinedInput) {
			try {
				// SHA-256 hashing
				MessageDigest digest = MessageDigest.getInstance("SHA-256");
				byte[] hashBytes = digest.digest(combinedInput.getBytes(StandardCharsets.UTF_8));
				BigInteger hashInt = new BigInteger(1, hashBytes);
				
				// Base-36 encoding
				String base36 = toBase36(hashInt);
				
				// Ensure it starts with a letter
				if (base36.length() > 0 && Character.isDigit(base36.charAt(0))) {
					base36 = "a" + base36;
				}
				
				// Truncate or pad the code
				return base36.length() >= DEFAULT_LENGTH ? base36.substring(0, DEFAULT_LENGTH) : padToLength(base36, DEFAULT_LENGTH);
			
			} catch (NoSuchAlgorithmException e) {
				throw new RuntimeException("SHA-256 not available", e);
			}
		}
		
		/**
		 * Internal method to convert a {@link BigInteger} into a Base-36 encoded string.
		 * @param value The BigInteger value to encode.
		 * @return The Base-36 string representation.
		 * @requires value.compareTo(BigInteger.ZERO) > 0
		 */
		private static String toBase36(BigInteger value) {
			StringBuilder sb = new StringBuilder();
			BigInteger base = BigInteger.valueOf(36);
			
			while (value.compareTo(BigInteger.ZERO) > 0) {
				BigInteger[] divmod = value.divideAndRemainder(base);
				sb.insert(0, BASE36_ALPHABET[divmod[1].intValue()]);
				value = divmod[0];
			}
			
			return sb.toString();
		}
		
		/**
		 * Internal method to pad an input string with the character 'a' until it reaches the specified length.
		 * @param input The string to pad.
		 * @param length The target length.
		 * @return The padded string.
		 * @requires input != null
		 * @requires length >= input.length()
		 * @ensures \result.length() == length
		 */
		private static String padToLength(String input, int length) {
			StringBuilder sb = new StringBuilder(input);
			while (sb.length() < length) {
				sb.append('a');
			}
			return sb.toString();
		}
		
		/**
		 * Checks if the given ID is currently tracked in the set of generated codes.
		 * @param id The ID to check.
		 * @return {@code true} if the ID is already in use, {@code false} otherwise.
		 * @requires id != null
		 * @ensures \result == generatedCodes.contains(id)
		 */
		public static boolean isAlreadyInUse(String id){
			return Hasher.generatedCodes.contains(id);
		}
	}
	
	/**
	 * QuestionList.Parser is a utility class dedicated to parsing {@link QuestionList} objects
	 * from JSON input, supporting reading from files or direct strings.
	 * <p>It uses Jackson's low-level {@link JsonParser} for efficient processing.</p>
	 */
	public static class Parser {
		
		/**
		 * Parses a {@link QuestionList} from a JSON file specified by the file path.
		 * @param filePathToJson The absolute or relative path to the JSON file.
		 * @return A {@link QuestionList.Builder} pre-populated with data from the file, or {@code null} if the file is not found.
		 * @throws IOExceptionif an I/O error occurs during file reading or JSON parsing.
		 * @requires filePathToJson != null
		 * @ensures \result == parse(new JsonFactory().createParser(f), filePathToJson) if file exists.
		 */
		public static QuestionList.Builder fromJsonFile(String filePathToJson) throws IOException{
			File f = new File(filePathToJson);
			if (!f.exists()){
				System.err.println(Constants.ERROR + "File not found"+ f.getAbsoluteFile());
				return null;
			}
			JsonParser jp =  new JsonFactory().createParser(new File(filePathToJson));
			return parse(jp, filePathToJson);
		}
		
		/**
		 * Parses a {@link QuestionList} from a raw JSON string.
		 * @param arg The JSON string content.
		 * @return A {@link QuestionList.Builder} pre-populated with data from the string.
		 * @throws IOExceptionif an error occurs during JSON parsing.
		 * @requires arg != null
		 * @ensures \result == parse(new JsonFactory().createParser(arg), arg)
		 */
		public static QuestionList.Builder fromString(String arg)throws IOException{
			JsonParser jp =  new JsonFactory().createParser(arg);
			return parse(jp, arg);
		}
		
		public static QuestionList.Builder parse(JsonParser jp, String original) throws IOException{
			QuestionList.Builder outputBuilder = new QuestionList.Builder();
			String fieldName;
			/*Check if outputBuilder file is a json */
			if (jp.currentToken() != JsonToken.START_OBJECT && jp.nextToken() != JsonToken.START_OBJECT) {
				throw new IOException(String.format(Constants.ERROR+Constants.RED+"QuestionList.Parser.parse, input is not a json: (%s, %s, %s) (%s, %s, %s) %s"+Constants.RESET,  jp.currentToken(), jp.currentName(), jp.getText(), jp.nextValue(), jp.nextFieldName(), jp.nextTextValue(), original));
			}
			/* first layer the ListQuestion 
			* iterating over ListQuestion attributes
			*/
			while (!jp.isClosed()){
				if (jp.currentToken() == JsonToken.FIELD_NAME) {
					fieldName = jp.currentName().toLowerCase();
					jp.nextToken();
					/* parsing System.out.print("QuestionList("+jp.currentToken()+", "+jp.currentName()+") "); */
					switch (fieldName){
						case "authorid","userid","user" -> {
							outputBuilder.authorId(jp.getText());
						}
						case "emojipertagname" -> {
							outputBuilder.addTags(parseEmojiPerTagName(jp, original));
						}
						case "name" -> {
							outputBuilder.name(jp.getText());
						}
						case "timecreatedmillis" -> {
							outputBuilder.timeCreatedMillis(jp.getValueAsLong());
						}
						case "id","listid" -> {
							outputBuilder.id(jp.getText());
						}
						case "questions" -> {
							outputBuilder.addAll(parseList(jp, original));
						}
						default -> {
							jp.skipChildren();
						}
					} 
				} else if(jp.currentToken() == JsonToken.END_OBJECT) {
						jp.nextToken();
						/* parsing System.out.print("QuestionList("+jp.currentToken()+", "+jp.currentName()+") "); */
						break;
				} else {
					jp.nextToken();
					/* parsing System.out.print("QuestionList("+jp.currentToken()+", "+jp.currentName()+") "); */
				}
			};
			
			return outputBuilder;
		}
		
		public static Map<String, String> parseEmojiPerTagName(JsonParser jp, String original) throws IOException {
			Map<String, String> m = new HashMap<>();
			String tagName, emojiCode;
			if(jp.currentToken() != JsonToken.START_OBJECT && jp.nextToken() != JsonToken.START_OBJECT){
				throw new IOException(String.format(Constants.ERROR+Constants.RED+"QuestionList.Parser.parseEmojiPerTagName, input is not a json: (%s, %s, %s) (%s, %s, %s) %s"+Constants.RESET,  jp.currentToken(), jp.currentName(), jp.getText(), jp.nextValue(), jp.nextFieldName(), jp.nextTextValue(), original));
			}
			while (!jp.isClosed()) {
				if (jp.currentToken() == JsonToken.FIELD_NAME) {
					tagName = jp.currentName();
					jp.nextToken();
					emojiCode = jp.getText();
					m.put(tagName, emojiCode);
				}else if(jp.currentToken() == JsonToken.END_OBJECT) {
					jp.nextToken();
					break;
				} else {
					jp.nextToken();
				}
			}
			return m;
		}
				
		public static List<Question> parseList(JsonParser jp, String original) throws IOException {
			List<Question> questions = new LinkedList<>();
			/* iterating over every Question attributes then Options*/
			if(jp.currentToken() != JsonToken.START_ARRAY && jp.nextToken() != JsonToken.START_ARRAY) {
				throw new IOException(String.format(Constants.ERROR+Constants.RED+"QuestionList.Parser.parseList, input is not a json: (%s, %s, %s) (%s, %s, %s) %s"+Constants.RESET,  jp.currentToken(), jp.currentName(), jp.getText(), jp.nextValue(), jp.nextFieldName(), jp.nextTextValue(), original));
			}
			Question q;
			while (!jp.isClosed()) {
				if(jp.currentToken() == JsonToken.START_OBJECT) {
					q = Question.Parser.parse(jp, original);
					if (q!=null)	questions.add(q);
				} else if(jp.currentToken() == JsonToken.END_ARRAY) {
					jp.nextToken();
					///* parsing System.out.print("QuestionList.Parser.parseList("+jp.currentToken()+", "+jp.currentName()+") "); */
					break;
				}else {
					jp.nextToken();
					///* parsing System.out.print("QuestionList.Parser.parseList("+jp.currentToken()+", "+jp.currentName()+") "); */
				}
			}
			return questions;
		}
	}
	
	/**
	 * Default constructor for QuestionList.
	 * Initializes the list with default values for authorId, name, and emojiPerTagName.
	 */
	public QuestionList(Builder builder) {
		if (builder.timeCreatedMillis==0L){
			builder.timeCreatedMillis = System.currentTimeMillis();
		}
		if (builder.id==null || builder.id.isBlank() || builder.id.length()<QuestionList.Hasher.DEFAULT_LENGTH){
			builder.id = QuestionList.Hasher.generate(builder.authorId+builder.name, builder.timeCreatedMillis);
		}
		this.questions= builder.list;
		this.authorId = builder.authorId;
		this.name = builder.name;
		this.emojiPerTagName = builder.emojiPerTagName;
		this.id = builder.id;
		this.timeCreatedMillis = builder.timeCreatedMillis;
	}
	
	/**
	 * Constructs a QuestionList with the specified authorId, name, and a collection of questions.
	 *
	 * @param authorId the ID of the author of this question list
	 * @param name the name of this question list
	 * @param c the initial List of questions to populate the list
	 */
	public QuestionList(String authorId, String name, List<? extends Question> c) {
		this(new QuestionList.Builder().authorId(authorId).name(name).addAll(c));
	}
	
	/**
	 * Constructs a QuestionList with the specified authorId, and name.
	 *
	 * @param authorId the ID of the author of this question list
	 * @param name the name of this question list
	 */
	public QuestionList(String authorId, String name){
		this(new QuestionList.Builder().authorId(authorId).name(name));
	}
	
	/**
	 * Constructs a QuestionList from a JSON file located at the specified file path.
	 *
	 * @param filePath the file path of the JSON file to import the list from
	 */
	public QuestionList(String filePath) throws IOException{
		this(QuestionList.Parser.fromJsonFile(filePath));
	}
	
	/**
	 * Imports a QuestionList from a JSON file.
	 *
	 * @param filePath the file path of the JSON file to import the list from
	 * @return a new QuestionList instance populated with data from the JSON file
	 */
	public static QuestionList importListQuestionFromJson(String filePath) throws IOException{
		return QuestionList.Parser.fromJsonFile(filePath).build();
	}
	
	/** 
	 * Returns the Question at the specified position in this list.
	 * @param index index of the Question to return
	 * @return the Question at the specified position in this list
	 * @pure
	 */
	public Question get(int index) {
		return questions.get(index);
	}
	
	/** 
	 * Appends all of the Questions that aren't already in this QuestionList,
	 * @param c List containing Questions to be added to this list
	 * @return true
	 * @ensures this.containsAll(c)
	 */
	public boolean addAll(List<? extends Question> c) {
		for (Question e : c) {
			if(!contains(e)) {
				add(e);
			}
		}
		return true;
	}
	
	/** 
	 * @return an Iterator over the Questions in this list in insertion order
	 */
	public Iterator<Question> iterator(){
		return questions.iterator();
	}
	
	/** 
	 * Append all questions in q into this QuestionList
	 * @param q the QuestionList containing Questions to be added to this list
	 * @return true
	 * @ensures this.containsAll(q.getQuestions())
	 */
	public boolean addAll(QuestionList q) {
		return addAll(q.getQuestions());
	}
	
	/** 
	 * @return an instance of the list of Questions in insertion order
	 */
	public List<Question> getQuestions(){
		List<Question> res = new LinkedList<>(questions);
		return res;
	}
	
	/** 
	 * Appends the specified Question to the end of this list if not already present.
	 * @param e Question to be appended to this list
	 * @return true
	 */
	public boolean add(Question e) {
		if (contains(e)) {
			return true;
		}
		questions.add(e);
		
		return true;
	}
	
	/** 
	 * Inserts the specified Question at the specified position in this list if not already present.
	 * @param index index at which the specified Question is to be inserted
	 * @param element Question to be inserted
	 * @throws IndexOutOfBoundsException if the index is out of range (index < 0 || index > size())
	 */
	public void add(int index, Question element) {
		if (index < 0 || index > questions.size()) {
			throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + questions.size());
		}
		if (contains(element)) {
			return ;
		}
		questions.add(index, element);
	}
	
	/** 
	 * @param o Question
	 * @return true if o is in this list
	 */
	public boolean contains(Object o) {
		return questions.contains(o);
	}
	
	/** 
	 * @return true if this list contains nothing
	 * @pure
	 */
	public boolean isEmpty(){
		return questions.isEmpty();
	}
	
	/** 
	 * @return the number of Questions in this list
	 * @pure
	 */
	public int size(){
		return questions.size();
	}
	
	/**
	 * Sets the author ID for this QuestionList.
	 * Renames the associated directory if necessary.
	 *
	 * @param authorId the new author ID to set
	 */
	public void setAuthorId(String authorId) {
		this.authorId= authorId;
	}
	
	/**
	 * Sets the name for this QuestionList.
	 * Renames the associated file if necessary.
	 *
	 * @param name the new name to set
	 */
	public void setName(String name) {
		this.name= name;
	}
	
	/**
	 * Sets the id for this QuestionList.
	 * @param id the new id
	 */
	public void setId(String id){
		this.id = id;
	}
	
	/**
	 * Gets the time this QuestionList was created in milliseconds.
	 *
	 * @return the time created in milliseconds
	 */
	public long getTimeCreatedMillis() {
		return timeCreatedMillis;
	}
	
	/**
	 * Gets the unique identifier for this QuestionList.
	 *
	 * @return the list ID
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * Gets the author ID for this QuestionList.
	 *
	 * @return the author ID
	 */
	public String getAuthorId() {
		return authorId;
	}
	
	/**
	 * Gets the name of this QuestionList.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Gets the emojiPerTagName of this QuestionList.
	 *
	 * @return an instance of emojiPerTagName
	 */
	public HashMap<String,String> getEmojiPerTagName() {
		HashMap<String, String> res = new HashMap<>(emojiPerTagName);
		return res;
	}
	
	public Set<String> tagNames() {
		return emojiPerTagName.keySet();
	}
	
	public String getEmoji(String tagName) {
		return emojiPerTagName.getOrDefault(tagName, null);
	}
	
	public void addTag(String tagName, String emoji) {
		emojiPerTagName.put(tagName, emoji);
	}
	public void setTags(Map<? extends String,? extends String> m) {
		emojiPerTagName = new HashMap<>(m);
	}
	
	/** 
	 * Removes the tag with the specified name from this QuestionList.
	 * @param tagName the name of the tag to remove
	 * @ensures !this.tagNames().contains(tagName)
	 */
	public void removeTag(String tagName) {
		if (emojiPerTagName.containsKey(tagName)) {
			emojiPerTagName.remove(tagName);
		}
	}
	
	/**
	 * Returns the path to the list file for a given QuestionList.
	 *
	 * @param questions the QuestionList to get the path for
	 * @return the file path as a string
	 */
	public static String pathToList(QuestionList questions) {
		return questions.pathToList();
	}
	
	/**
	 * Returns the default path to the file for this QuestionList.
	 *
	 * @return the default file path as a string
	 */
	public String pathToList(){
		String p = Constants.LISTSPATH+Constants.SEPARATOR+getAuthorId()+Constants.SEPARATOR;
		p+=getId()+".json";
		return p;
	}
	
	/**
	 * static method to export a QuestionList as a JSON file.
	 * @param c the QuestionList to export.
	 */
	public static void exportListQuestionAsJson(QuestionList c){
		c.exportListQuestionAsJson();
	}
	
	/**
	 * Exports this QuestionList as a JSON file.
	 * Creates the file if it does not already exist.
	 */
	public void exportListQuestionAsJson(){
		exportListQuestionAsJson(pathToList());
	}
	
	/** 
	 * Exports this QuestionList as a JSON file to the specified destination file path.
	 */
	public void exportListQuestionAsJson(String destFilePath){
		try {
			File myJson = new File(destFilePath);
			File folder = myJson.getParentFile();
			if(folder != null && !myJson.getParentFile().exists()) {
				folder.mkdirs();
			}
			BufferedWriter buff = Files.newBufferedWriter(Paths.get(destFilePath));
			buff.write(this.toJson());
			buff.close();
		} catch (Exception e) {
			System.err.println(Constants.ERROR + "An error occurred while exporting a List of questions.");
			e.printStackTrace();
		}
	}
	
	
	/** 
	 * Comparator to compare two QuestionList instances by their creation date.
	 */
	public static Comparator<QuestionList> comparatorByDate() {
		return Comparator.comparingLong(QuestionList::getTimeCreatedMillis);
	}
	
	/** 
	 * Comparator to compare two QuestionList instances by their size.
	 */
	public static Comparator<QuestionList> comparatorBySize() {
		return Comparator.comparingInt(QuestionList::size);
	}
	
	/** 
	 * Comparator to compare two QuestionList instances by their name.
	 */
	public static Comparator<QuestionList> comparatorByName() {
		return Comparator.comparing(QuestionList::getName);
	}
	
	/** 
	 * Comparator to compare two QuestionList instances by their id.
	 */
	public static Comparator<QuestionList> comparatorById() {
		return Comparator.comparing(QuestionList::getId);
	}
	
	/**
	 * Returns a string representation of this QuestionList in JSON format.
	 *
	 * @return the JSON representation of this QuestionList
	 */
	@Override
	public String toString() {
		return toJson();
	}
	
	/** 
	 * Returns a string representation of this QuestionList in JSON format.
	 */
	public String toJson(){
		/*try {
			return Constants.MAPPER.writer(new DefaultPrettyPrinter()).writeValueAsString(this);
		}catch(JsonProcessingException e){System.err.print(Constants.ERROR);e.printStackTrace();}*/
		return toJsonUsingMapper();//Constants.MAPPER.writer(new DefaultPrettyPrinter()).writeValueAsString(this);
	}
	/** 
	 * Converts this QuestionList to a JSON string using the Jackson ObjectMapper.
	 */
	private String toJsonUsingMapper() {
		try {
			
			String res="",
			tab="",
			spc1 = "  ",
			spc2 = spc1+spc1,
			spc3 = spc2+spc1,
			seperatorParamOpt = "\n";


			
			res += "{\n";
			tab = "\t";
			res += tab+"\"authorId\":"+Constants.MAPPER.writeValueAsString(getAuthorId())+",\n";
			res += tab + "\"name\":"+Constants.MAPPER.writeValueAsString(getName())+",\n";
			res += tab + "\"id\":"+Constants.MAPPER.writeValueAsString(getId())+",\n";
			res += tab + "\"timeCreatedMillis\":"+getTimeCreatedMillis()+",\n";
			
			res += tab + "\"emojiPerTagName\":{";
			Iterator<Entry<String, String>> iter = emojiPerTagName.entrySet().iterator();
			Entry<String, String> entry;
			while (iter.hasNext()) {
				entry = iter.next();
				res += Constants.MAPPER.writeValueAsString(entry.getKey())+" : "+Constants.MAPPER.writeValueAsString(entry.getValue());
				if(iter.hasNext()){
					res += ", ";
				}
			}
			res += "},\n";
			
			res += tab + "\"questions\": \n";
			res += "\t[\n";
			Iterator<Question> iterQuestion = this.iterator();
			while (iterQuestion.hasNext()){
				Question q = iterQuestion.next();
				tab = "\t\t";
				res += "\t{\n" + spc2 + "\"question\":"+Constants.MAPPER.writeValueAsString(q.getQuestion())+",\n";
				res += spc2 + "\"explication\":";
				if(q.getExplication()==null || q.getExplication().equals("null") || q.getExplication().equals(Constants.NOEXPLICATION)){
					res +=null;
				}else {
					res += Constants.MAPPER.writeValueAsString(q.getExplication());
				}
				res += ",\n";
				res +=spc2 + "\"imageSrc\":"+(q.getImageSrc()==null?null:Constants.MAPPER.writeValueAsString(q.getImageSrc()))+",\n";
				res +=spc2 + "\"options\": [\n";
				List<Option> opts = q.getOptions(); opts.sort((a,b)->(a.isCorrect()?-1:1));
				Iterator<Option> iterOpt = opts.iterator();
				while (iterOpt.hasNext()){
					Option opt = iterOpt.next();
					res += spc2+"{\n";
					res += spc3+"\"option\":"+Constants.MAPPER.writeValueAsString(opt.getText())+","+seperatorParamOpt;
					res += spc3+"\"isCorrect\":"+opt.isCorrect()+","+seperatorParamOpt;
					res += spc3+"\"explication\":";
					if(opt.getExplication()==null || opt.getExplication().equals("null") || opt.getExplication().equals(Constants.NOEXPLICATION)){
						res +=null+seperatorParamOpt;
					}else {
						res += Constants.MAPPER.writeValueAsString(opt.getExplication())+seperatorParamOpt;
					}
					res += spc2+"}";
					if (iterOpt.hasNext()){
						res += ",";
					}
					res += "\n";
				}
				res += spc2+"]\n";
				res += spc1+"}";
				if(iterQuestion.hasNext()) {
					res+= ",";
				}
				res += "\n";
			}
			res += spc1+"]\n";
			res +="}";
			return res;
		} catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	
	/**
	 * Returns the hash code for this QuestionList.
	 *
	 * @return the hash code
	 */
	@Override
	public int hashCode() {
		return getAuthorId().hashCode()*7 + getName().hashCode()
				+ super.hashCode() + (int) (getTimeCreatedMillis() % Integer.MAX_VALUE)
				+ getId().hashCode();
	}
	
	/**
	 * Compares this QuestionList to another object for equality.
	 *
	 * @param o the object to compare
	 * @return true if the objects are equal, false otherwise
	 */
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof QuestionList)) return false;
		QuestionList l = (QuestionList) o;
		return this.getId().equals(l.getId()) && this.getTimeCreatedMillis() == l.getTimeCreatedMillis()
			&& areStringsEqual(getAuthorId(), l.getAuthorId()) && areStringsEqual(getName(),l.getName()) 
			&& Set.copyOf(this.getQuestions()).equals(Set.copyOf(l.getQuestions()));
	}
	
	/** 
	 * Utility method to compare two strings for equality, handling null values.
	 */
	private static boolean areStringsEqual(String s1, String s2) {
		if (s1 == s2){
			return true;
		}
		if (s1 == null || s2 == null){
			return false;
		}
		return s1.equals(s2);
	}
	
	/** 
	 * Rearranges the questions in this QuestionList randomly.
	 */
	public QuestionList rearrageQuestions(){
		Random r = BotCore.getRandom();
		questions.sort((a,b)->r.nextBoolean()?-1:1);
		return this;
	}
	
	/** 
	 * Rearranges the options of each question in this QuestionList randomly.
	 */
	public QuestionList rearrageOptions(){
		Random r = BotCore.getRandom();
		return rearrageOptions((a,b)->r.nextBoolean()?-1:1);
	}
	
	/** 
	 * Rearranges the options of each question in this QuestionList using the specified comparator.
	 */
	public QuestionList rearrageOptions(Comparator<? super Option> comp){
		Question q;
		for (int i=0; i<size(); ++i){
			q = get(i).rearrageOptions(comp);
			questions.set(i, q);
		}
		return this;
	}
	
	/** 
	 * @return a header string for this QuestionList
	 */
	public String header(){
		String res = String.format("`%s` `%d` **%s**\n",getId(), size(), getName());
		return res;
	}
	
	/** 
	 * Formats the question at the specified index for display.
	 * @param index the index of the question to format
	 * @param correct if true, includes the correct answers and explanations in the output
	 * @param respondents a map of user IDs to their selected options
	 * @param players a set of user IDs to display
	 * @return a formatted string representing the question and its options
	 */
	public String getFormated(int index, @Nullable Boolean correct, @Nullable Map<String, Set<Option>> respondents, @Nullable Set<String> players) {
		Question q = get(index);
		double points = 0.00;
		String options = "",box="", users="", emoji, pointStr = "", corrStr = "", optCorrStr, expl;
		Iterator<Set<Option>> iter;
		Set<Option>awnsers;
		Option opt;
		for (int i = 0; i < q.size(); i++) {
			opt = q.get(i);
			optCorrStr="";
			emoji="";
			if (players!=null && respondents!=null){
				box=">";
				if (!players.isEmpty()){
					box="";
					for (String u : players){
						awnsers = respondents.get(u);
						box += (awnsers==null||awnsers.isEmpty())?Constants.EMOJIBOX:(awnsers.contains(opt)?Constants.EMOJICHECKEDBOX:Constants.EMOJIBOX);
					}
				}
				box += " ";
			}
			if (correct != null && correct){
				if (respondents!=null){
					iter = respondents.values().iterator();
					awnsers = iter.hasNext()?iter.next():null;
				} else {
					awnsers = null;
				}
				if (awnsers != null && awnsers.contains(opt)) {
					points += opt.isCorrect() ? pointsForCorrect / q.trueOptions().size() : pointsForIncorrect;
					emoji = (opt.isCorrect() ? Constants.EMOJITRUE : Constants.EMOJIFALSE);
				} else {
					emoji = (opt.isCorrect() ? Constants.EMOJICORRECT : Constants.EMOJIINCORRECT);
				}
				expl = opt.getExplication();
				if (expl ==null){
					expl = Constants.NOEXPLICATION;
				}
				optCorrStr = String.format("> %s%s\n",emoji,expl);
			}
			options += String.format("%s%d. %s\n%s", box, i + 1, q.get(i).getText(), optCorrStr);
		}
		if (correct != null && correct){
			corrStr = String.format("> \n> %s\n",(q.getExplication()==null)?Constants.NOEXPLICATION:q.getExplication());
			pointStr = String.format("`%s/%s`", points, pointsForCorrect);
		}
		if (players!=null){
			Iterator<String> iterp = players.iterator();
			while (iterp.hasNext()){
				users += BotCore.getEffectiveNameFromId(iterp.next()).substring(0, 1);
				if (!iterp.hasNext())users += "\n";
			}
		}
		String questionText = String.format("### %d. %s %s\n", index+1, q.getQuestion(), pointStr);
		return String.format("%s%s%s%s%s", header(), questionText, users, options, corrStr).replace("\\$([^\\$]*)\\$", "`$1`");
	}	
	
	/** 
	 * @param index the index of the question to format
	 * @param opts a collection of options to mark as selected
	 * @return the correction of the question at index
	 */
	public String getFormatedCorrection (int index, Collection<Option> opts) {
		Map<String, Set<Option>> m = null;
		if (opts!=null && !opts.isEmpty()){
			m=new HashMap<>();
			m.put("unkown", new HashSet<>(opts));
		}
		return getFormated (index,true, m, null);
	}
	
	/** 
	 * @param index the index of the question to format
	 * @return the awsers of question at index
	 */
	public String getFormatedCorrection (int index) {
		return getFormated(index, true, null, null);
	}
	
	/** 
	 * @return an example of a QuestionList
	 */
	public static QuestionList getExampleQuestionList(){
		return new QuestionList.Builder()
		.authorId("Examplary Author")
		.name("Example of a QuestionList")
		.id("abcdefg")
		.add(Question.getExampleQuestion())
		.addTag("Science", Emoji.fromUnicode("U+1F52D").getFormatted())
		.timeCreatedMillis(1748458230337L)
		.build();
	}
	
	/** TODO docs */
	public static int myBinarySearchIndexOf(List<QuestionList> tab, int start, int end, String listName){
		QuestionList searched = new QuestionList.Builder().name(listName).build();
		return Users.myBinarySearchIndexOf(tab, 0, end, searched, QuestionList.comparatorByName());
	}
	
	/** TODO docs */
	public static int myBinarySearchIndexOf(List<QuestionList> tab, String listName){
		return myBinarySearchIndexOf(tab, 0, tab.size()-1, listName);
	}
	
	/** TODO docs */
	public static QuestionList getById(String id) {
		return Users.getById(id);
	}
	
	/** TODO docs */
	public static QuestionList getByName(String listName) {
		return Users.getByName(listName);
	}
}
