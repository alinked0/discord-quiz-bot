package com.linked.quizbot.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import com.linked.quizbot.Constants;
import com.linked.quizbot.core.BotCore;

import kotlin.Pair;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.utils.TimeFormat;

/**
 * The QuestionList class is a specialized extension of the ArrayList class, designed to manage a collection
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
 * @see ArrayList
 * @author alinked0
 * @version 1.0
 */
public class QuestionList extends ArrayList<Question> {
	private String authorId;
	private Map<String,Emoji> tags;
	private String name;
	public static double pointsForCorrect = 1.00;
	public static double pointsForIncorrect = -0.25;
	private long timeCreatedMillis;
	private String id;

	static {
		getExampleQuestionList().exportListQuestionAsJson();
	}

	public static class Builder {
		private final List<Question> list= new ArrayList<>();
		private final Map<String,Emoji> tags= new HashMap<>();
		private String authorId= null;
		private String name= null;
		private String id= null;
		private long timeCreatedMillis= 0L;

		public  Builder authorId(String authorId){
			this.authorId = authorId;
			return this;
		}
		public  Builder addTag(String tagName, Emoji emoji){
			this.tags.put(tagName, emoji);
			return this;
		}
		public  Builder addTags(Map<String,Emoji> tags){
			this.tags.putAll(tags);
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
		public Builder addAll(List<Question> c){
			list.addAll(c);
			return this;
		}
		public QuestionList build(){
			return new QuestionList(this);
		}
	}
	/**
	 * Default constructor for QuestionList.
	 * Initializes the list with default values for authorId, name, and tags.
	 */
	public QuestionList(Builder builder) {
		super();
		this.authorId = builder.authorId;
		this.name = builder.name;
		this.tags = builder.tags;
		this.id = builder.id;
		this.timeCreatedMillis = builder.timeCreatedMillis;
		this.addAll(builder.list);
	}

	/**
	 * Constructs a QuestionList with the specified authorId, name, and a collection of questions.
	 *
	 * @param authorId the ID of the author of this question list
	 * @param name the name of this question list
	 * @param c the initial collection of questions to populate the list
	 */
	public QuestionList(String authorId, String name, Collection<? extends Question> c) {
		super(c);
		this.authorId = authorId;
		this.name = name;
		this.tags = new HashMap<>();
		this.timeCreatedMillis = System.currentTimeMillis();
		this.id = QuestionListHash.generate(authorId+name, timeCreatedMillis);
	}

	/**
	 * Constructs a QuestionList with the specified authorId, and name.
	 *
	 * @param authorId the ID of the author of this question list
	 * @param name the name of this question list
	 */
	public QuestionList(String authorId, String name){
		this.authorId = authorId;
		this.name = name;
		this.tags = new HashMap<>();
		this.timeCreatedMillis = System.currentTimeMillis();
		this.id = QuestionListHash.generate(authorId+name, timeCreatedMillis);
	}

	/**
	 * Constructs a QuestionList from a JSON file located at the specified file path.
	 *
	 * @param filePath the file path of the JSON file to import the list from
	 */
	public QuestionList(String filePath) throws IOException {
		QuestionList res = QuestionListParser.fromJsonFile(filePath);
		if (res!=null && res.getName()!=null) {
			this.addAll(res);
			this.name = res.getName();
			this.authorId = res.getAuthorId();
			this.tags = res.getTags();
			this.timeCreatedMillis = res.getTimeCreatedMillis();
			this.id = res.getId();
		}
	}

	/**
	 * Imports a QuestionList from a JSON file.
	 *
	 * @param filePath the file path of the JSON file to import the list from
	 * @return a new QuestionList instance populated with data from the JSON file
	 */
	public static QuestionList importListQuestionFromJson(String filePath) throws IOException{
		return new QuestionList(filePath);
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
	 * Sets the creation time for this QuestionList.
	 * @param timeMillis the new time created in milliseconds
	 */
	public void setTimeCreatedMillis(long timeMillis){
		this.timeCreatedMillis = timeMillis;
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
	 * Gets the tags of this QuestionList.
	 *
	 * @return an instance of tags
	 */
	public HashMap<String,Emoji> getTags() {
		HashMap<String, Emoji> res = new HashMap<>(tags);
		return res;
	}

	public Emoji getEmojiByTag(String tagName) {
		return getTags().getOrDefault(tagName, null);
	}

	public void addTag(String tagName, Emoji emoji) {
		tags.put(tagName, emoji);
	}
	public void setTags(Map<? extends String,? extends Emoji> m) {
		tags = new HashMap<>(m);
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

	public void exportListQuestionAsJson(String destFilePath){
		try {
			File myJson = new File(destFilePath);
			File folder = myJson.getParentFile();
			if(folder != null && !myJson.getParentFile().exists()) {
				folder.mkdirs();
			}
			BufferedWriter buff = Files.newBufferedWriter(Paths.get(destFilePath));
			buff.write(this.toString());
			buff.close();
		} catch (IOException e) {
			System.err.println("$> An error occurred while exporting a List of questions.");
			e.printStackTrace();
		}
	}
	public void removeTag(String tagName) {
		if (tags.containsKey(tagName)) {
			tags.remove(tagName);
		}
	}
	public static QuestionList mergeQuestionLists(QuestionList e, QuestionList f) {
		if (e==null || f == null) {
			return null;
		}
		if (!e.equals(f)) {
			return null;
		}
		QuestionList res = new QuestionList(e.getAuthorId(),e.getName());
		res.addAll(e);
		res.addAll(f);
		return res;
	}
	@Override
	public boolean addAll(Collection<? extends Question> c) {
		for (Question e : c) {
			if(!contains(e)) {
				add(e);
			}
		}
		return true;
	}
	@Override
	public boolean add(Question e) {
		if (contains(e)) {
			return true;
		}
		super.add(e);

		return true;
	}
	@Override
	public void add(int index, Question element) {
		if (contains(element)) {
			return ;
		}
		super.add(index, element);
	}

	public static Comparator<? super QuestionList> comparatorByDate() {
        return (e, f)->(Long.compare(e.getTimeCreatedMillis(),f.getTimeCreatedMillis()));
    }

	public static Comparator<? super QuestionList> comparatorByName() {
        return (e, f)->(e.getName().compareTo(f.getName()));
    }

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
		return toJson();
	}
	public String toJson() {
		String res="", 
			tab="",
			tab1 = "\t",
			tab2 = "\t\t",
			tab3 = "\t\t\t",
		seperatorParamOpt = "\n";

		res += "{\n";
		tab = "\t";
		res += tab+"\"authorId\":\""+getAuthorId()+"\",\n";
		res += tab + "\"name\":\""+getName()+"\",\n";
		res += tab + "\"id\":\""+getId()+"\",\n";
		res += tab + "\"timeCreatedMillis\":"+getTimeCreatedMillis()+",\n";

		res += tab + "\"tags\":{";
		Iterator<Entry<String, Emoji>> iter = tags.entrySet().iterator();
		Entry<String, Emoji> entry;
		while (iter.hasNext()) {
			entry = iter.next();
			res += "\""+entry.getKey()+"\" : \""+entry.getValue().getFormatted()+"\"";
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
			res += "\t{\n" + tab2 + "\"question\":\""+q.getQuestion()+"\",\n";
			res += tab2 + "\"explication\":";
			if(q.getExplication()==null || q.getExplication().equals("null") || q.getExplication().equals(Constants.NOEXPLICATION)){
				res +=null;
			}else {
				res += "\""+q.getExplication()+"\"";
			}
			res += ",\n";
			res +=tab2 + "\"imageSrc\":"+(q.getImageSrc()==null?null:"\""+q.getImageSrc()+"\"")+",\n";
			res +=tab2 + "\"options\": [\n";
			List<Option> opts = q.getOptions(); opts.sort((a,b)->(a.isCorrect()?-1:1));
			Iterator<Option> iterOpt = opts.iterator();
			while (iterOpt.hasNext()){
				Option opt = iterOpt.next();
				res += tab2+"{\n";
				res += tab3+"\"text\":\""+opt.getText()+"\","+seperatorParamOpt;
				res += tab3+"\"isCorrect\":"+opt.isCorrect()+","+seperatorParamOpt;
				res += tab3+"\"explication\":";
				if(opt.getExplication()==null || opt.getExplication().equals("null") || opt.getExplication().equals(Constants.NOEXPLICATION)){
					res +=null+seperatorParamOpt;
				}else {
					res += "\""+opt.getExplication()+"\""+seperatorParamOpt;
				}
				res += tab2+"}";
				if (iterOpt.hasNext()){
					res += ",";
				}
				res += "\n";
			}
			res += tab2+"]\n";
			res += tab1+"}";
			if(iterQuestion.hasNext()) {
				res+= ",";
			}
			res += "\n";
		}
		res += tab1+"]\n";
		res +="}";
		res = res.replace("\\", "\\\\");
		res = res.replace("\\", "\\\\");
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
		if (!(o instanceof QuestionList l)) return false;
		if (!super.equals(l)) return false;
		
		return areStringsEqual(getAuthorId(), l.getAuthorId()) && areStringsEqual(getName(),l.getName());
	}
	private boolean areStringsEqual(String s1, String s2) {
		if (s1 == s2){ 
			return true;
		}
		if (s1 == null || s2 == null){
			return false;
		}
		return s1.equals(s2);
	}
	public QuestionList rearrageQuestions(){
		Random r = BotCore.getRandom();
		sort((a,b)->r.nextBoolean()?-1:1);
		return this;
	}
	public QuestionList rearrageOptions(){
		Random r = BotCore.getRandom();
		for (Question q : this){
			q.sort((a,b)->r.nextBoolean()?-1:1);
		}
		return this;
	}
	public String header(){
		String res = String.format("**Name:** **%s**\n**Author:** <@%s>\n**nb of questions:** `%d`\n**Date created:** %s\n", 
			getName(), getAuthorId(),size(), TimeFormat.DATE_TIME_LONG.atTimestamp(getTimeCreatedMillis()));
		return res;
	}
	public String getFormated (int index) {
		Question q = get(index);
		String questionText = "### "+(index+1)+"/"+size()+" "+q.getQuestion()+"\n";
		String options = "";
		for (int i = 0; i < q.size(); i++) {
			options += (i + 1)+". "+q.get(i).getText()+"\n";
		}
		return questionText + options;
	}
	public Pair<Double, String> getFormatedCorrection (int index, Collection<Option> opts, double pointsForCorrect, double pointsForIncorrect) {
		double points = 0.00;
		Question q = get(index);
		int numberOfTrueOptions = q.getTrueOptions().size();
		String optsString = "";
		Option opt;
		Emoji emoji;
		for (int i = 0; i < q.size(); i++){
			opt = q.get(i);
			optsString += String.format("> %d. %s\n", i + 1, opt.getText());
			if (opts != null && opts.contains(opt)) {
				points += opt.isCorrect() ? pointsForCorrect / numberOfTrueOptions : pointsForIncorrect;
				emoji = (opt.isCorrect() ? Constants.EMOJITRUE : Constants.EMOJIFALSE);
			} else {
				emoji = (opt.isCorrect() ? Constants.EMOJICORRECT : Constants.EMOJIINCORRECT);
			}
			optsString += String.format("> %s*%s*\n",emoji.getFormatted(),opt.getExplicationFriendly());
		}
		String text = String.format("`%s` `%d` **%s**\n",getId(), size(), getName());
		text += String.format("### %d. %s", index+1, q.getQuestion());
		if (opts!=null && pointsForCorrect != 0.0){
			text += String.format(" `%s/%s`", points,pointsForCorrect);
		}
		text += String.format("\n%s> \n> **%s**\n", optsString, q.getExplicationFriendly());
		return new Pair<Double, String>(points, text);
	}
	public Pair<Double, String> getFormatedCorrection (int index, Collection<Option> opts) {
		return getFormatedCorrection (index, opts, pointsForCorrect, pointsForIncorrect);
	}
	public String getFormatedCorrection (int index) {
		return getFormatedCorrection (index, null, 0.0, 0.0).getSecond();
	}
	public static QuestionList getExampleQuestionList(){
		return new QuestionList.Builder()
		.authorId("Examplary Author")
		.name("Example of a QuestionList")
		.id("abcdefg")
		.add(Question.getExampleQuestion())
		.addTag("Science", Emoji.fromUnicode("U+1F52D"))
		.build();
	}

	public static int myBinarySearchIndexOf(List<QuestionList> tab, int start, int end, String listName){
		QuestionList searched = new QuestionList.Builder().name(listName).build();
		return Users.myBinarySearchIndexOf(tab, 0, end, searched, QuestionList.comparatorByName());
	}
	public static int myBinarySearchIndexOf(List<QuestionList> tab, String listName){
		return myBinarySearchIndexOf(tab, 0, tab.size()-1, listName);
	}
	public static QuestionList getQuestionListById(String id) {
		return Users.getQuestionListById(id);
	}
	public static QuestionList getQuestionListByName(String listName) {
		return Users.getQuestionListByName(listName);
	}
}
