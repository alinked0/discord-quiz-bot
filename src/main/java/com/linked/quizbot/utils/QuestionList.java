package com.linked.quizbot.utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.linked.quizbot.Constants;

import java.util.Collection;

/**
 * The ListQuestion class is a specialized extension of the LinkedList class, designed to manage a collection
 * of {@link Question} objects. It includes additional metadata such as author ID, theme, and name, and provides
 * utility methods for importing and exporting the list in JSON format.
 *
 * <p>This class is primarily intended for use in quiz or survey applications, where a structured collection
 * of questions needs to be maintained along with associated metadata.</p>
 *
 * <h2>Features:</h2>
 * <ul>
 *   <li>Supports creating instances with metadata (author ID, theme, and name) and an initial collection of questions.</li>
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
	private String theme;
	private String name;
	/**
	 * Default constructor for ListQuestion.
	 * Initializes the list with default values for authorId, name, and theme.
	 */
	public QuestionList() {
		super();
		this.authorId = null;
		this.name = null;
		this.theme = null;
	}

	/**
	 * Constructs a ListQuestion with the specified authorId, name, theme, and a collection of questions.
	 *
	 * @param authorId the ID of the author of this question list
	 * @param name the name of this question list
	 * @param theme the theme of this question list
	 * @param c the initial collection of questions to populate the list
	 */
	public QuestionList(String authorId, String name, String theme, Collection<? extends Question> c) {
		super(c);
		this.authorId = authorId;
		this.name = name;
		this.theme = theme;
	}

	/**
	 * Constructs a ListQuestion with the specified authorId, name, and theme.
	 *
	 * @param authorId the ID of the author of this question list
	 * @param name the name of this question list
	 * @param theme the theme of this question list
	 */
	public QuestionList(String authorId, String name, String theme){
		this.authorId = authorId;
		this.name = name;
		this.theme = theme;
	}

	/**
	 * Constructs a ListQuestion from a JSON file located at the specified file path.
	 *
	 * @param filePathToJson the file path of the JSON file to import the list from
	 */
	public QuestionList(String filePathToJson) {
        long start = System.nanoTime();
		QuestionList res = new QuestionList();
		try {
			File f = new File(filePathToJson);
			if (!f.exists()){
				System.err.println("  $> File not found"+ f.getAbsoluteFile());
				return;
			}
			JsonParser jp =  new JsonFactory().createParser(new File(filePathToJson));
			JsonToken tmp;
			/*Check if this file is a json */
			if (jp.nextToken() != JsonToken.START_OBJECT) {
				System.err.println("The file is not a json");
			}
			/* first layer the ListQuestion 
			 * iterating over ListQuestion attributes
			*/
			do{
				jp.nextToken();
				//System.out.print("("+jp.currentName() +":"+jp.getText()+") ");
				if (jp.currentToken() == JsonToken.START_ARRAY){
					jp.nextToken();
					break;
				}
				switch (jp.currentName()){
					case "authorId" -> res.setAuthorId(jp.getText());
					case "theme" -> res.setTheme(jp.getText());
					case "name" -> res.setName(jp.getText());
				}
			}while (true);
			//System.out.println("");
			/* iterating over evry Question attributes then Options*/
			boolean keepGoing = true;
			while (keepGoing) {
				String q = "";
				//int num = 1;
				String expl = "";
				String imgSrc= "";
				do {
					jp.nextToken();
					if(jp.currentToken() == JsonToken.START_ARRAY) {
						jp.nextToken();
						break;
					}
					//System.out.print("("+jp.currentName() +":"+jp.getText()+") ");
					switch (jp.currentName()){
						case "question" -> {q = jp.getText();}
						//case "numberTrue" -> num = jp.getValueAsInt();
						case "explication" -> {
							expl = jp.getText();
							if (expl==null || expl.equals("null")){
								expl = "No explanation found.";
							}
						}
						case "img_src","imageSrc" -> {
							imgSrc = jp.getText().equals("null")?null:jp.getText();
						}
					}
				}while(true);
				//System.out.println("");

				String optTxt = "";
				String optExpl = null;
				Boolean isCorr = false;
				LinkedList<Option> opt = new LinkedList<>();
				do {
					tmp = jp.nextToken();
					jp.nextToken();
					//System.out.print("("+jp.currentName() +":"+jp.getText()+") ");
					if (tmp ==JsonToken.END_OBJECT) {
						opt.add(new Option(optTxt, isCorr, optExpl));
						if (jp.currentToken()==JsonToken.END_ARRAY){
							jp.nextToken();jp.nextToken();
							if (jp.currentToken()==JsonToken.END_ARRAY) {
								keepGoing = false;
							}
							break;
						}
						continue;
					}
					switch(jp.currentName()){
						case "text" -> optTxt = jp.getText();
						case "explication" -> {
							optExpl = jp.getText();
							if (optExpl==null || optExpl.equals("null")){
								optExpl = "No explanation found.";
							}
						}
						case "isCorrect" -> isCorr = jp.getValueAsBoolean();//Text().equals("true")?true:false;
					}
				}while(true);
				//System.out.println("");
				res.add(new Question(q, opt));
				res.get(res.size()-1).setImageSrc(imgSrc);
				res.get(res.size()-1).setExplication(expl);
			}
		} catch (IOException e) {
			System.err.println("Error: Parser creation failed");
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (NullPointerException e) {
			System.err.println("Error Json representation of ListQuestion is invalid");
			e.printStackTrace();
		}
		this.addAll(res);
		this.name = res.getName();
		this.theme = res.getTheme();
		this.authorId = res.getAuthorId();
		System.out.printf("   $> time importJson = `%.3f ms` n=%d name=%s\n",(System.nanoTime() - start) / 1000000.00 ,this.size(),this.name);
	}

	/**
	 * Imports a ListQuestion from a JSON file.
	 *
	 * @param filePathToJson the file path of the JSON file to import the list from
	 * @return a new ListQuestion instance populated with data from the JSON file
	 */
	public static QuestionList importListQuestionFromJson(String filePathToJson) {
		return new QuestionList(filePathToJson);
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
	 * Sets the theme for this ListQuestion.
	 * Renames the associated file if necessary.
	 *
	 * @param theme the new theme to set
	 */
	public void setTheme(String theme) {
		//File file = new File(getPathToList());
		this.theme= theme;
		// if(file.exists()) {
		// 	if(!file.renameTo(new File(getPathToList()))){
		// 		System.out.println("Error: renaming of questions file failed");
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
	 * Gets the theme of this ListQuestion.
	 *
	 * @return the theme
	 */
	public String getTheme() {
		return theme;
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
	 * Returns the path to the list file for this ListQuestion.
	 *
	 * @return the file path as a string
	 */
	public String getPathToList(){
		String p = Constants.LISTSPATH+Constants.SEPARATOR+getAuthorId()+Constants.SEPARATOR;
		p+=(getTheme()!=null?getTheme().replace(" ", "_"):getTheme())+"-";
		p += getName()!=null?getName().replace(" ", "_"):getName();
		p+= ".json";
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
		try {
			File myJson = new File(getPathToList());
			if(!myJson.getParentFile().exists()) {
				myJson.getParentFile().mkdirs();
			}
			//Writer fw = new FileWriter(getPathToList(), false);
			BufferedWriter buff = Files.newBufferedWriter(Paths.get(getPathToList()));
			
			//FileWriter myWriter = new FileWriter(myJson.getPath());
			buff.write(this.toString());
			//myWriter.write(this.toString());
			buff.close();
		} catch (IOException e) {
			System.err.println("An error occurred while exporting a List of questions.");
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
		QuestionList res = new QuestionList(e.getAuthorId(),e.getName(), e.getTheme());
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
		String res="", tab="";
		res += "{\n";
		tab = "\t";
		res += tab+"\"authorId\":\""+getAuthorId()+"\",\n";
		res += tab + "\"theme\":\""+getTheme()+"\",\n";
		res += tab + "\"name\":\""+getName()+"\",\n";
		res += tab + "\"questions\": \n";
		res += "\t[\n";
		Iterator<Question> iterQuestion = this.iterator();
		while (iterQuestion.hasNext()){
			Question q = iterQuestion.next();
			tab = "\t\t";
			res += "\t{\n" + tab + "\"question\":\""+q.getQuestion()+"\",\n";
			res += tab + "\"explication\":";
			if(q.getExplication()==null || q.getExplication().equals("null") || q.getExplication().equals(Constants.NOEXPLICATION)){
				res +=null;
			}else {
				res += "\""+q.getExplication()+"\"";
			}
			res += ",\n";
			res +=tab + "\"imageSrc\":"+(q.getImageSrc()==null?null:"\""+q.getImageSrc()+"\"")+",\n";
			res +=tab + "\"options\": [\n";
			List<Option> opts = q.getOptions(); opts.sort((a,b)->(a.isCorrect()?-1:1));
			Iterator<Option> iterOpt = opts.iterator();
			while (iterOpt.hasNext()){
				Option opt = iterOpt.next();
				res += "\t\t{\n";
				tab = "\t\t\t";
				res += tab+"\"text\":\""+opt.getText()+"\",\n";
				res += tab+"\"isCorrect\":"+opt.isCorrect()+",\n";
				res += tab+"\"explication\":";
				if(opt.getExplication()==null || opt.getExplication().equals("null") || opt.getExplication().equals(Constants.NOEXPLICATION)){
					res +=null+"\n";
				}else {
					res += "\""+opt.getExplication()+"\"\n";
				}
				res += "\t\t}";
				if (iterOpt.hasNext()){
					res += ",\n";
				}
			}
			if(iterQuestion.hasNext()) {
				res+= ",\n";
			}
		}
		res += "\n\t]\n";
		res += "\t}\n";
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
		return getAuthorId().hashCode()*7 + getTheme().hashCode()*5+ getName().hashCode();
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
				&& getTheme().equals(l.getTheme())
				&& getName().equals(l.getName());
	}

	public static QuestionList getExampleQuestionList(){
		QuestionList l = new QuestionList();
		l.add(Question.getExampleQuestion());
		l.setName("Example QuestionList");
		l.setTheme("Example QuestionList");
		return l;
	}
}
