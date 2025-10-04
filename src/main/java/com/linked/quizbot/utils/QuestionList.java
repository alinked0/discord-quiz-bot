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
import java.util.Collections;
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
import java.util.UUID;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linked.quizbot.Constants;
import com.linked.quizbot.core.BotCore;

import kotlin.Pair;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.utils.TimeFormat;

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

	public static class Builder {
		public final List<Question> list= new ArrayList<>();
		public final Map<String,String> emojiPerTagName= new HashMap<>();
		public String authorId= null;
		public String name= null;
		public String id= null;
		public long timeCreatedMillis= 0L;

		public  Builder authorId(String authorId){
			this.authorId = authorId;
			return this;
		}
		public  Builder addTag(String tagName, String emoji){
			this.emojiPerTagName.put(tagName, emoji);
			return this;
		}
		public  Builder addTags(Map<String,String> emojiPerTagName){
			this.emojiPerTagName.putAll(emojiPerTagName);
			return this;
		}
		public  Builder name(String listName){
			this.name = listName;
			return this;
		}
		public  Builder id(String id){
			this.id = id;
			return this;
		}
		public  Builder timeCreatedMillis(long timeCreatedMillis){
			this.timeCreatedMillis = timeCreatedMillis;
			return this;
		}
		public Builder add(Question q){
			list.add(q);
			return this;
		}
		public Builder add(QuestionList questions){
			for (Question q : questions){
				this.add(q.clone());
			}
			this.addTags(questions.getEmojiPerTagName());
			if (id == null)this.id(questions.getId());
			if (name==null)this.name(questions.getName());
			if (authorId==null)this.authorId(questions.getAuthorId());
			if (timeCreatedMillis==0L || questions.getTimeCreatedMillis()<timeCreatedMillis) this.timeCreatedMillis(questions.getTimeCreatedMillis());
			return this;
		}
		public Builder addAll(List<? extends Question> c){
			list.addAll(c);
			return this;
		}
		public QuestionList build(){
			return new QuestionList(this);
		}
	}
	
	/**
	 * QuestionList.Hasher is a utility class for generating unique, short, alphanumeric codes
	 * based on an input string and a timestamp. It uses SHA-256 hashing and Base-36 encoding
	 * to create a code that is guaranteed to be unique for the same input and timestamp.
	 * 
	 * Most of this code was written by chatgpt
	 */
	public static class Hasher {

		public static final char[] BASE36_ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyz".toCharArray();
		public static final int DEFAULT_LENGTH = 7;
		public static final Set<String> generatedCodes = new HashSet<>();

		public static void addGeneratedCode(String code){
			Hasher.generatedCodes.add(code);
		}
		public static void clearGeneratedCodes(){
			generatedCodes.clear();
		}
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
		private static String padToLength(String input, int length) {
			StringBuilder sb = new StringBuilder(input);
			while (sb.length() < length) {
				sb.append('a');
			}
			return sb.toString();
		}
		public static boolean isAlreadyInUse(String id){
			return Hasher.generatedCodes.contains(id);
		}
	}

	public static class Parser {
		public static QuestionList.Builder fromJsonFile(String filePathToJson) throws IOException{
			File f = new File(filePathToJson);
			if (!f.exists()){
				System.err.println("[ERROR] File not found"+ f.getAbsoluteFile());
				return null;
			}
			JsonParser jp =  new JsonFactory().createParser(new File(filePathToJson));
			return parser(jp, filePathToJson);
		}

		public static QuestionList.Builder fromString(String arg)throws IOException{
			JsonParser jp =  new JsonFactory().createParser(arg);
			return parser(jp, arg);
		}

		public static QuestionList.Builder parser(JsonParser jp, String arg) throws IOException{
			QuestionList.Builder outputBuilder = new QuestionList.Builder();
			String fieldName;
			/*Check if outputBuilder file is a json */
			if (jp.nextToken() != JsonToken.START_OBJECT) {
				throw new IOException(String.format("Error QuestionList.Parser.parser, input is not a json: \n\t%s\n", arg));
			}
			/* first layer the ListQuestion 
			* iterating over ListQuestion attributes
			*/
			do{
				if (jp.currentToken() == JsonToken.FIELD_NAME) {
					fieldName = jp.currentName().toLowerCase();
					jp.nextToken();
					switch (fieldName){
						case "authorid","userid","user" -> {
							outputBuilder.authorId(jp.getText());
						}
						case "emojipertagname" -> {
							outputBuilder.addTags(parseEmojiPerTagName(jp, arg));
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
							outputBuilder.addAll(parseQuestionList(jp, arg));
						}
					}
				} else {
					jp.nextToken();
				}
			}while (!jp.isClosed());

			return outputBuilder;
		}

		public static Map<String, String> parseEmojiPerTagName(JsonParser jp, String arg) throws IOException {
			Map<String, String> m = new HashMap<>();
			String tagName, emojiCode;
			if(jp.currentToken() != JsonToken.START_OBJECT){
				throw new IOException(String.format("Error QuestionList.Parser.parseEmojiPerTagName, input is not a json: \n\t%s\n", arg));
			}
			while (!jp.isClosed()) {
				jp.nextToken();
				if (jp.currentToken() == JsonToken.FIELD_NAME) {
					tagName = jp.currentName();
					jp.nextToken();
					emojiCode = jp.getText();
					m.put(tagName, emojiCode);
				}
				if(jp.currentToken() == JsonToken.END_OBJECT) {
					jp.nextToken();
					break;
				}
			}
			return m;
		}

		public static Question parseQuestion(JsonParser jp, String arg) throws IOException {
			Question outputBuilder = null;
			String q = null;
			String expl = null;
			String imgSrc= null, fieldName;
			List <Option>opts = null;
			if(jp.currentToken() != JsonToken.START_OBJECT) {
				throw new IOException(String.format("[ERROR] QuestionList.Parser.parseEmojiPerTagName, input is not a json: \n\t%s\n", arg));
			}
			while(!jp.isClosed()){
				if(jp.currentToken() == JsonToken.FIELD_NAME) {
					fieldName = jp.currentName().toLowerCase();
					jp.nextToken();
					switch (fieldName){
						case "question" -> {q = jp.getText();}
						case "explication" -> {
							expl = jp.getText();
							if (expl!=null && expl.equals("null")){
								expl = null;
							}
						}
						case "img_src","imagesrc" -> {
							imgSrc = jp.getText().equals("null")?null:jp.getText();
						}
						case "options" -> opts = parseOptionList(jp, arg);
					}
				} else if (jp.currentToken() == JsonToken.END_OBJECT) {
					jp.nextToken();
					break;
				} else {
					jp.nextToken();
				}
			};
			if (q==null){
				return null;
			}
			outputBuilder = new Question(q, opts);
			outputBuilder.setExplication(expl);
			outputBuilder.setImageSrc(imgSrc);
			return outputBuilder;
		}
		public static List<Option> parseOptionList(JsonParser jp, String arg) throws IOException {
			LinkedList<Option> opts = new LinkedList<>();
			if(jp.currentToken() != JsonToken.START_ARRAY){
				throw new IOException(String.format("Error QuestionList.Parser.parseOptionList, input is not a json: \n\t%s\n", arg));
			}
			jp.nextToken();
			while(!jp.isClosed()){
				if (jp.currentToken() == JsonToken.START_OBJECT){
					opts.add(parseOption(jp, arg));
				} else if (jp.currentToken() == JsonToken.END_ARRAY){
					jp.nextToken();
					break;
				} else {
					jp.nextToken();
				}
			};
			return opts;
		}
		public static Option parseOption(JsonParser jp, String arg) throws IOException {
			String optTxt = null;
			String optExpl = null, fieldName;
			Boolean isCorr = null;
			Option res=null;
			if(jp.currentToken() != JsonToken.START_OBJECT){
				throw new IOException(String.format("Error QuestionList.Parser.parseOption, input is not a json: \n\t%s\n", arg));
			}
			while(!jp.isClosed()){
				jp.nextToken();
				if (jp.currentToken() == JsonToken.FIELD_NAME) {
					fieldName = jp.currentName().toLowerCase();
					jp.nextToken();
					switch(fieldName){
						case "text" -> optTxt = jp.getValueAsString();
						case "explication" -> {
							optExpl = jp.getText();
							if (optExpl!=null && optExpl.equals("null")){
								optExpl = null;
							}
						}
						case "iscorrect" -> {
							isCorr = jp.getBooleanValue();
						}
					}
				} else if(jp.currentToken() != JsonToken.END_ARRAY){
					jp.nextToken();
					break;
				}
			};
			if (optTxt!=null && isCorr!= null){
				if (optExpl!=null){
					res = new Option(optTxt, isCorr, optExpl);
				} else {
					res = new Option(optTxt, isCorr);
				}
			}
			return res;
		}

		public static List<Question> parseQuestionList(JsonParser jp, String arg) throws IOException {
			List<Question> outputBuilder = new LinkedList<>();
			/* iterating over every Question attributes then Options*/
			if(jp.currentToken() != JsonToken.START_ARRAY) {
				return null;
			}
			while (!jp.isClosed()) {
				if(jp.currentToken() == JsonToken.START_OBJECT) {
					outputBuilder.add(parseQuestion(jp, arg));
				} else if(jp.currentToken() == JsonToken.END_ARRAY) {
					break;
				}else {
					jp.nextToken();
				}
			}
			return outputBuilder;
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
		if (!(builder.id!=null && !builder.id.isEmpty()&& builder.id.length()>=QuestionList.Hasher.DEFAULT_LENGTH)){
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
	public QuestionList(String filePath) throws IOException {
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
	
	/** TODO */
	public Question get(int index) {
		return questions.get(index);
	}
	
	/** TODO */
	public boolean addAll(List<? extends Question> c) {
		for (Question e : c) {
			if(!contains(e)) {
				add(e);
			}
		}
		return true;
	}
	
	/** TODO */
	public Iterator<Question> iterator(){
		return questions.iterator();
	}
	
	/** TODO */
	public boolean addAll(QuestionList q) {
		return addAll(q.getQuestions());
	}
	
	/** TODO */
	public List<Question> getQuestions(){
		List<Question> res = new LinkedList<>(questions);
		return res;
	}
	
	/** TODO */
	public boolean add(Question e) {
		if (contains(e)) {
			return true;
		}
		questions.add(e);

		return true;
	}
	
	/** TODO */
	public void add(int index, Question element) {
		if (contains(element)) {
			return ;
		}
		questions.add(index, element);
	}
	
	/** TODO */
	public boolean contains(Object o) {
		return questions.contains(o);
	}
	
	/** TODO */
	public boolean isEmpty(){
		return questions.isEmpty();
	}

	/** TODO */
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

	public Set<String> getTagNames() {
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
	
	/** TODO */
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
		return questions.getPathToList();
	}

	/**
	 * Returns the default path to the file for this QuestionList.
	 *
	 * @return the default file path as a string
	 */
	public String getPathToList(){
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
		exportListQuestionAsJson(getPathToList());
	}
	
	/** TODO */
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
		} catch (IOException e) {
			System.err.println("[ERROR] An error occurred while exporting a List of questions.");
			e.printStackTrace();
		}
	}
	
	
	/** TODO */
	public static Comparator<? super QuestionList> comparatorByDate() {
        return (e, f)->(Long.compare(e.getTimeCreatedMillis(),f.getTimeCreatedMillis()));
    }
	
	/** TODO */
	public static Comparator<? super QuestionList> comparatorByName() {
        return (e, f)->(e.getName().compareTo(f.getName()));
    }
	
	/** TODO */
	public static Comparator<? super QuestionList> comparatorById() {
        return (e, f)->(e.getId().compareTo(f.getId()));
    }
	
	/**
	 * Returns a string representation of this QuestionList in JSON format.
	 *
	 * @return the JSON representation of this QuestionList
	 */
	@Override
	public String toString() {
		return toJson().replace("\n", "").replace("\t", "");
	}
	
	/** TODO */
	public String toJsonUsingMapper() throws JsonProcessingException{
		String res="", 
			tab="",
			spc1 = "  ",
			spc2 = spc1+spc1,
			spc3 = spc2+spc1,
		seperatorParamOpt = "\n";



		res += "{\n";
		tab = "\t";
		res += tab+Constants.MAPPER.writeValueAsString("authorId")+":"+Constants.MAPPER.writeValueAsString(""+getAuthorId()+"")+",\n";
		res += tab + Constants.MAPPER.writeValueAsString("name")+":"+Constants.MAPPER.writeValueAsString(""+getName()+"")+",\n";
		res += tab + Constants.MAPPER.writeValueAsString("id")+":"+Constants.MAPPER.writeValueAsString(""+getId()+"")+",\n";
		res += tab + Constants.MAPPER.writeValueAsString("timeCreatedMillis")+":"+getTimeCreatedMillis()+",\n";

		res += tab + Constants.MAPPER.writeValueAsString("emojiPerTagName")+":{";
		Iterator<Entry<String, String>> iter = emojiPerTagName.entrySet().iterator();
		Entry<String, String> entry;
		while (iter.hasNext()) {
			entry = iter.next();
			res += ""+Constants.MAPPER.writeValueAsString(""+entry.getKey()+"")+" : "+Constants.MAPPER.writeValueAsString(""+entry.getValue()+"")+"";
			if(iter.hasNext()){
				res += ", ";
			}
		}
		res += "},\n";

		res += tab + Constants.MAPPER.writeValueAsString("questions")+": \n";
		res += "\t[\n";
		Iterator<Question> iterQuestion = this.iterator();
		while (iterQuestion.hasNext()){
			Question q = iterQuestion.next();
			tab = "\t\t";
			res += "\t{\n" + spc2 + Constants.MAPPER.writeValueAsString("question")+":"+Constants.MAPPER.writeValueAsString(q.getQuestion())+",\n";
			res += spc2 + Constants.MAPPER.writeValueAsString("explication")+":";
			if(q.getExplication()==null || q.getExplication().equals("null") || q.getExplication().equals(Constants.NOEXPLICATION)){
				res +=null;
			}else {
				res += Constants.MAPPER.writeValueAsString(q.getExplication());
			}
			res += ",\n";
			res +=spc2 + Constants.MAPPER.writeValueAsString("imageSrc")+":"+(q.getImageSrc()==null?null:Constants.MAPPER.writeValueAsString(q.getImageSrc()))+",\n";
			res +=spc2 + Constants.MAPPER.writeValueAsString("options")+": [\n";
			List<Option> opts = q.getOptions(); opts.sort((a,b)->(a.isCorrect()?-1:1));
			Iterator<Option> iterOpt = opts.iterator();
			while (iterOpt.hasNext()){
				Option opt = iterOpt.next();
				res += spc2+"{\n";
				res += spc3+Constants.MAPPER.writeValueAsString("text")+":"+Constants.MAPPER.writeValueAsString(opt.getText())+","+seperatorParamOpt;
				res += spc3+Constants.MAPPER.writeValueAsString("isCorrect")+":"+opt.isCorrect()+","+seperatorParamOpt;
				res += spc3+Constants.MAPPER.writeValueAsString("explication")+":";
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
	}
	
	/** TODO */
	public String toJson(){
		String res=null;
		try {
			res = toJsonUsingMapper();
		} catch (Exception e){
			System.err.println("[ERROR] [toJsonUsingMapper() failed]"+e.getMessage());
		}
		return res;
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
	
	/** TODO */
	public static boolean areStringsEqual(String s1, String s2) {
		if (s1 == s2){ 
			return true;
		}
		if (s1 == null || s2 == null){
			return false;
		}
		return s1.equals(s2);
	}
	
	/** TODO */
	public QuestionList rearrageQuestions(){
		Random r = BotCore.getRandom();
		questions.sort((a,b)->r.nextBoolean()?-1:1);
		return this;
	}
	
	/** TODO */
	public QuestionList rearrageOptions(){
		Random r = BotCore.getRandom();
		return rearrageOptions((a,b)->r.nextBoolean()?-1:1);
	}
	
	/** TODO */
	public QuestionList rearrageOptions(Comparator<? super Option> comp){
		Question q;
		for (int i=0; i<size(); ++i){
			q = get(i).rearrageOptions(comp);
			questions.set(i, q);
		}
		return this;
	}
	
	/** TODO */
	public String header(){
		String res = String.format("`%s` `%d` **%s**\n",getId(), size(), getName());
		return res;
	}
	
	/** TODO */
	public String getFormated(int index, @Nullable Boolean correct, @Nullable Map<String, Set<Option>> respondents, @Nullable Set<String> players) {
		Question q = get(index);
		double points = 0.00;
		String options = "",box="", users="", emoji, pointStr = "", correctionStr, expl;
		Iterator<Set<Option>> iter;
		Set<Option>awnsers;
		Option opt;
		for (int i = 0; i < q.size(); i++) {
			opt = q.get(i);
			correctionStr="";
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
					points += opt.isCorrect() ? pointsForCorrect / q.getTrueOptions().size() : pointsForIncorrect;
					emoji = (opt.isCorrect() ? Constants.EMOJITRUE : Constants.EMOJIFALSE);
				} else {
					emoji = (opt.isCorrect() ? Constants.EMOJICORRECT : Constants.EMOJIINCORRECT);
				}
				expl = opt.getExplication();
				if (expl ==null){
					expl = Constants.NOEXPLICATION;
				}
				correctionStr = String.format("> %s%s\n",emoji,expl);
			}
			options += String.format("%s%d. %s\n%s", box, i + 1, q.get(i).getText(), correctionStr);
		}
		if (correct != null && correct){
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
		return String.format("%s%s%s%s", header(), questionText, users, options).replace("\\$([^\\$]*)\\$", "`$1`");
	}	

	/** TODO */
	public String getFormatedCorrection (int index, Collection<Option> opts) {
		Map<String, Set<Option>> m = null;
		if (opts!=null){
			m=new HashMap<>();
			m.put("unkown", new HashSet<>(opts));
		}
		return getFormated (index,true, m, null);
	}
	
	/** TODO */
	public String getFormatedCorrection (int index) {
		return getFormated(index, true, null, null);
	}
	
	/** TODO */
	public static QuestionList getExampleQuestionList(){
		return new QuestionList.Builder()
		.authorId("Examplary Author")
		.name("Example of a QuestionList")
		.id("abcdefg")
		.add(Question.getExampleQuestion())
		.addTag("Science", Emoji.fromUnicode("U+1F52D").getFormatted())
		.build();
	}
	
	/** TODO */
	public static int myBinarySearchIndexOf(List<QuestionList> tab, int start, int end, String listName){
		QuestionList searched = new QuestionList.Builder().name(listName).build();
		return Users.myBinarySearchIndexOf(tab, 0, end, searched, QuestionList.comparatorByName());
	}
	
	/** TODO */
	public static int myBinarySearchIndexOf(List<QuestionList> tab, String listName){
		return myBinarySearchIndexOf(tab, 0, tab.size()-1, listName);
	}
	
	/** TODO */
	public static QuestionList getById(String id) {
		return Users.getById(id);
	}
	
	/** TODO */
	public static QuestionList getByName(String listName) {
		return Users.getByName(listName);
	}
}
