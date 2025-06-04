package com.linked.quizbot.utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.linked.quizbot.Constants;

import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.UnicodeEmoji;

public class QuestionListParser {
    public static QuestionList fromJsonFile(String filePathToJson){
        long start = System.nanoTime();
		QuestionList result = null;
		try {
			File f = new File(filePathToJson);
			if (!f.exists()){
				System.err.println("  $> File not found"+ f.getAbsoluteFile());
				return null;
			}
			JsonParser jp =  new JsonFactory().createParser(new File(filePathToJson));
			result = parser(jp);
		} catch (IOException e) {
			System.err.println("Error: Parser creation failed");
			e.printStackTrace();
		} catch (NullPointerException e) {
			System.err.println("Error Json representation of ListQuestion is invalid");
			e.printStackTrace();
		}
        if (!Constants.isBugFree()) System.out.printf("   $> time fromJsonFile = `%.3f ms`\n",(System.nanoTime() - start) / 1000000.00);
		return result;
    }

	public static QuestionList fromString(String arg){
		long start = System.nanoTime();
		QuestionList result = null;
		try {
			JsonParser jp =  new JsonFactory().createParser(arg);
			result = parser(jp);
		} catch (IOException e) {
			System.err.println("Error: Parser creation failed");
			e.printStackTrace();
		} catch (NullPointerException e) {
			System.err.println("Error Json representation of ListQuestion is invalid");
			e.printStackTrace();
		}
        if (!Constants.isBugFree()) System.out.printf("   $> time fromString = `%.3f ms`\n",(System.nanoTime() - start) / 1000000.00);
		return result;
	}

	public static QuestionList parser(JsonParser jp) throws IOException{
		QuestionList result = new QuestionList();
		String fieldName;
		int i = 0;
		/*Check if result file is a json */
		if (jp.nextToken() != JsonToken.START_OBJECT) {
			System.err.println("$> The file is not a json");
		}
		i++;
		/* first layer the ListQuestion 
		 * iterating over ListQuestion attributes
		 */
		do{
			System.out.print("("+jp.currentName() +":"+jp.getText()+") "+ (jp.currentToken() == JsonToken.FIELD_NAME)+"\n");
			/*if (jp.currentToken() == JsonToken.END_OBJECT){
				i--;
			}
			if (jp.currentToken() == JsonToken.START_OBJECT){
				i++;
			}*/
			if (jp.currentToken() == JsonToken.FIELD_NAME) {
				fieldName = jp.currentName().toLowerCase();
				jp.nextToken();
				switch (fieldName){
					case "authorid" -> {
						result.setAuthorId(jp.getText());
					}
					case "tags" -> {
						result.setTags(parseTags(jp));
					}
					case "name" -> {
						result.setName(jp.getText());
					}
					case "timecreatedmillis" -> {
						result.setTimeCreatedMillis(jp.getValueAsLong());
						if (result.getTimeCreatedMillis()==0L){
							result.setTimeCreatedMillis(System.currentTimeMillis());
						}
					}
					case "listid" -> {
						result.setListId(jp.getText());
						if (result.getListId().isEmpty()){
							result.setListId(new QuestionListHash().generate(result.getAuthorId()+result.getName(), result.getTimeCreatedMillis()));
						} else {
							QuestionListHash.addGeneratedCode(result.getListId());
						}
					}
					case "questions" -> {
						result.addAll(parseQuestionList(jp));
					}
				}
			} else {
				jp.nextToken();
			}
		}while (!jp.isClosed());
		return result;
	}

	public static Map<String, UnicodeEmoji> parseTags(JsonParser jp) throws IOException {
		Map<String, UnicodeEmoji> m = new HashMap<>();
		String tagName, emojiCode;
		if(jp.currentToken() != JsonToken.START_OBJECT){
			return null;
		}
		while (!jp.isClosed()) {
			jp.nextToken();
			if (jp.currentToken() == JsonToken.FIELD_NAME) {
				tagName = jp.currentName();
				jp.nextToken();
				emojiCode = jp.getText();
				UnicodeEmoji unicode = Emoji.fromUnicode(emojiCode);
				m.put(tagName, unicode);
			}
			if(jp.currentToken() == JsonToken.END_OBJECT) {
				jp.nextToken();
				break;
			}
		}
		return m;
	}

	public static Question parseQuestion(JsonParser jp) throws IOException {
		Question result = null;
		String q = "";
		String expl = "";
		String imgSrc= "", fieldName;
		List <Option>opts = null;
		if(jp.currentToken() != JsonToken.START_OBJECT) {
			return null;
		}
		 while(!jp.isClosed()){
			System.out.println("("+jp.currentName() +":"+jp.getText()+") ");
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
					case "options" -> opts = parseOptionList(jp);
				}
			} else if (jp.currentToken() == JsonToken.END_OBJECT) {
				jp.nextToken();
				break;
			} else {
				jp.nextToken();
			}
		};
		result = new Question(q, opts);
		result.setExplication(expl);
		result.setImageSrc(imgSrc);
		System.out.println("\n"+result+"\n");
		return result;
	}
	public static List<Option> parseOptionList(JsonParser jp) throws IOException {
		LinkedList<Option> opts = new LinkedList<>();
		if(jp.currentToken() != JsonToken.START_ARRAY){
			return null;
		}
		jp.nextToken();
		while(!jp.isClosed()){
			System.out.println("("+jp.currentName() +":"+jp.getText()+") ");
			if (jp.currentToken() == JsonToken.START_OBJECT){
				opts.add(parseOption(jp));
				System.out.print("("+jp.currentName() +":"+jp.getText()+") ");
			} else if (jp.currentToken() == JsonToken.END_ARRAY){
				jp.nextToken();
				break;
			} else {
				jp.nextToken();
			}
		};
		return opts;
	}
	public static Option parseOption(JsonParser jp) throws IOException {
		String optTxt = null;
		String optExpl = null, fieldName;
		Boolean isCorr = null;
		Option res;
		if(jp.currentToken() != JsonToken.START_OBJECT){
			return null;
		}
		while(!jp.isClosed()){
			jp.nextToken();
			System.out.println("opt ("+jp.currentName() +":"+jp.getText()+") ");
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
		res = new Option(optTxt, isCorr, optExpl);
		return res;
	}

	public static List<Question> parseQuestionList(JsonParser jp) throws IOException {
		List<Question> result = new LinkedList<>();
		/* iterating over every Question attributes then Options*/
		if(jp.currentToken() != JsonToken.START_ARRAY) {
			return null;
		}
		while (!jp.isClosed()) {
			System.out.print("("+jp.currentName() +":"+jp.getText()+") ");
			if(jp.currentToken() == JsonToken.START_OBJECT) {
				result.add(parseQuestion(jp));
			} else if(jp.currentToken() == JsonToken.END_ARRAY) {
				break;
			}else {
				jp.nextToken();
			}
		}
		return result;
	}
}