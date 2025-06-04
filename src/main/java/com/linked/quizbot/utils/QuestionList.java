package com.linked.quizbot.utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.linked.quizbot.Constants;

import net.dv8tion.jda.api.entities.emoji.Emoji;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;

/**
 * The ListQuestion class is a specialized extension of the LinkedList class, designed to manage a collection
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
 * // Creating a new ListQuestion with metadata and adding questions
 * ListQuestion list = new ListQuestion("001", "Trivia Night", "Science");
 * list.add(new Question("What is the chemical symbol for water?", 1, "H2O", "CO2"));
 *
 * // Exporting the list to a JSON file
 * list.exportListQuestionAsJson();
 *
 * // Importing a ListQuestion from a JSON file
 * ListQuestion importedList = new ListQuestion("path/to/json/file.json");
 * </pre>
 *
 * @see Question
 * @see Option
 * @see LinkedList
 * @author alinked0
 * @version 1.0
 */
public class QuestionList extends LinkedList<Question> {
	private String authorId;
	private HashMap<String, Emoji> tags;
	private String name;
	private long timeCreatedMillis;
	private String listId;

	/**
	 * Default constructor for ListQuestion.
	 * Initializes the list with default values for authorId, name, and tags.
	 */
	public QuestionList() {
		super();
		this.authorId = null;
		this.name = null;
		this.tags = null;
		this.listId = "";
		this.timeCreatedMillis = 0L;
	}

	/**
	 * Constructs a ListQuestion with the specified authorId, name, and a collection of questions.
	 *
	 * @param authorId the ID of the author of this question list
	 * @param name the name of this question list
	 * @param c the initial collection of questions to populate the list
	 */
	public QuestionList(String authorId, String name, Collection<? extends Question> c) {
		super(c);
		this.authorId = authorId;
		this.name = name;
		this.timeCreatedMillis = System.currentTimeMillis();
		this.listId = new QuestionListHash().generate(authorId+name, timeCreatedMillis);;
	}

	/**
	 * Constructs a ListQuestion with the specified authorId, and name.
	 *
	 * @param authorId the ID of the author of this question list
	 * @param name the name of this question list
	 */
	public QuestionList(String authorId, String name){
		this.authorId = authorId;
		this.name = name;
		this.timeCreatedMillis = System.currentTimeMillis();
		this.listId = new QuestionListHash().generate(authorId+name, timeCreatedMillis);
	}

	/**
	 * Constructs a ListQuestion from a JSON file located at the specified file path.
	 *
	 * @param filePath the file path of the JSON file to import the list from
	 */
	public QuestionList(String filePath) {
		this();
		QuestionList res = QuestionListParser.jsonToQuestionList(filePath);
		if (res!=null) {
			this.addAll(res);
			this.name = res.getName();
			this.authorId = res.getAuthorId();
			this.timeCreatedMillis = res.getTimeCreatedMillis();
			this.listId = res.getListId();
		}
	}

	/**
	 * Imports a ListQuestion from a JSON file.
	 *
	 * @param filePath the file path of the JSON file to import the list from
	 * @return a new ListQuestion instance populated with data from the JSON file
	 */
	public static QuestionList importListQuestionFromJson(String filePath) {
		return new QuestionList(filePath);
	}

	/**
	 * Sets the author ID for this ListQuestion.
	 * Renames the associated directory if necessary.
	 *
	 * @param authorId the new author ID to set
	 */
	public void setAuthorId(String authorId) {
		//File dir = new File(getPathToList());
		this.authorId= authorId;
		// if(dir.getParentFile().exists()) {
		// 	if(!dir.renameTo(new File(getPathToList()).getParentFile())){
		// 		System.out.println("Error: Change of author folder failed");
		// 	}
		// }
	}

	/**
	 * Sets the name for this ListQuestion.
	 * Renames the associated file if necessary.
	 *
	 * @param name the new name to set
	 */
	public void setName(String name) {
		//File file = new File(getPathToList());
		this.name= name;
		// if(file.exists()) {
		// 	if(!file.renameTo(new File(getPathToList()))){
		// 		System.out.println("Error: renaming of questions file failed");
		// 	}
		// }
	}

	/**
	 * Sets the creation time for this ListQuestion.
	 * @param timeMillis the new time created in milliseconds
	 */
	public void setTimeCreatedMillis(long timeMillis){
		this.timeCreatedMillis = timeMillis;
	}

	/**
	 * Sets the listId for this ListQuestion.
	 * @param listId the new listId
	 */
	public void setListId(String listId){
		this.listId = listId;
	}

	/**
	 * Gets the time this ListQuestion was created in milliseconds.
	 *
	 * @return the time created in milliseconds
	 */
	public long getTimeCreatedMillis() {
		return timeCreatedMillis;
	}
	
	/**
	 * Gets the unique identifier for this ListQuestion.
	 *
	 * @return the list ID
	 */
	public String getListId() {
		return listId;
	}

	/**
	 * Gets the author ID for this ListQuestion.
	 *
	 * @return the author ID
	 */
	public String getAuthorId() {
		return authorId;
	}

	/**
	 * Gets the name of this ListQuestion.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the path to the list file for a given ListQuestion.
	 *
	 * @param questions the ListQuestion to get the path for
	 * @return the file path as a string
	 */
	public static String pathToList(QuestionList questions) {
		return questions.getPathToList();
	}

	/**
	 * Returns the default path to the file for this ListQuestion.
	 *
	 * @return the default file path as a string
	 */
	public String getPathToList(){
		String p = Constants.LISTSPATH+Constants.SEPARATOR+getAuthorId()+Constants.SEPARATOR;
		p+=getListId()+".json";
		return p;
	}

	/**
	 * static method to export a ListQuestion as a JSON file.
	 * @param c the ListQuestion to export.
	 */
	public static void exportListQuestionAsJson(QuestionList c){
		c.exportListQuestionAsJson();
	}

	/**
	 * Exports this ListQuestion as a JSON file.
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

	public static Comparator<? super QuestionList> comparatorByListId() {
        return (e, f)->(e.getListId().compareTo(f.getListId()));
    }
	
	/**
	 * Returns a string representation of this ListQuestion in JSON format.
	 *
	 * @return the JSON representation of this ListQuestion
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
		//res += tab + "\"tags\":\""+getTags()+"\",\n";
		res += tab + "\"name\":\""+getName()+"\",\n";
		res += tab + "\"listId\":\""+getListId()+"\",\n";
		res += tab + "\"timeCreatedMillis\":"+getTimeCreatedMillis()+",\n";
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
				}else{
					res += "\n"+tab2+"]";
				}
				res += "\n";
			}
			res += tab1+"}";
			if(iterQuestion.hasNext()) {
				res+= ",";
			}else{
				res += "\n"+tab1+"]";
			}
			res += "\n";
		}
		res +="}";
		res = res.replace("\\", "\\\\");
		res = res.replace("\\", "\\\\");
		return res;
	}
	/**
	 * Returns the hash code for this ListQuestion.
	 *
	 * @return the hash code
	 */
	@Override
	public int hashCode() {
		return getAuthorId().hashCode()*7 + getName().hashCode()
				+ super.hashCode() + (int) (getTimeCreatedMillis() % Integer.MAX_VALUE)
				+ getListId().hashCode();
	}
	
	/**
	 * Compares this ListQuestion to another object for equality.
	 *
	 * @param o the object to compare
	 * @return true if the objects are equal, false otherwise
	 */
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof QuestionList l)) return false;
		if (!super.equals(l)) return false;
		return getAuthorId().equals(l.getAuthorId())
				&& getName().equals(l.getName());
	}

	public static QuestionList getExampleQuestionList(){
		QuestionList l = new QuestionList();
		l.setAuthorId("ExampleAuthor");
		l.setName("Example QuestionList");
		l.setListId("abcdefg");
		l.add(Question.getExampleQuestion());
		return l;
	}
}
