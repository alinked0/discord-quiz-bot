package com.linked.quizbot.utils;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import net.dv8tion.jda.api.entities.emoji.Emoji;

public class QuestionListParser {
    public static QuestionList fromJsonFile(String filePathToJson) throws IOException{
        long start = System.nanoTime();
		QuestionList outputBuilder = null;
		File f = new File(filePathToJson);
		if (!f.exists()){
			System.err.println("  $> File not found"+ f.getAbsoluteFile());
			return null;
		}
		JsonParser jp =  new JsonFactory().createParser(new File(filePathToJson));
		outputBuilder = parser(jp);
        //if (!Constants.isBugFree()) System.out.printf("   $> time fromJsonFile = `%.3f ms`\n",(System.nanoTime() - start) / 1000000.00);
		return outputBuilder;
    }

	public static QuestionList fromString(String arg)throws IOException{
		long start = System.nanoTime();
		QuestionList outputBuilder = null;
		JsonParser jp =  new JsonFactory().createParser(arg);
		outputBuilder = parser(jp);
        //if (!Constants.isBugFree()) System.out.printf("   $> time fromString = `%.3f ms`\n",(System.nanoTime() - start) / 1000000.00);
		return outputBuilder;
	}

	public static QuestionList parser(JsonParser jp) throws IOException{
		QuestionList.Builder outputBuilder = new QuestionList.Builder();
		String fieldName;
		/*Check if outputBuilder file is a json */
		if (jp.nextToken() != JsonToken.START_OBJECT) {
			System.err.println("$> The file is not a json");
		}
		/* first layer the ListQuestion 
		 * iterating over ListQuestion attributes
		 */
		do{
			//System.out.print("("+jp.currentName() +":"+jp.getText()+") "+ (jp.currentToken() == JsonToken.FIELD_NAME)+"\n");
			if (jp.currentToken() == JsonToken.FIELD_NAME) {
				fieldName = jp.currentName().toLowerCase();
				jp.nextToken();
				switch (fieldName){
					case "authorid" -> {
						outputBuilder.authorId(jp.getText());
					}
					case "tags" -> {
						outputBuilder.addTags(parseTags(jp));
					}
					case "name" -> {
						outputBuilder.name(jp.getText());
					}
					case "timecreatedmillis" -> {
						outputBuilder.timeCreatedMillis(jp.getValueAsLong());
					}
					case "listid" -> {
						outputBuilder.id(jp.getText());
					}
					case "questions" -> {
						outputBuilder.addAll(parseQuestionList(jp));
					}
				}
			} else {
				jp.nextToken();
			}
		}while (!jp.isClosed());
		QuestionList reuslt = outputBuilder.build();
		if (reuslt.getListId()==null || reuslt.getListId().isEmpty()|| reuslt.getListId().length()!=QuestionListHash.DEFAULT_LENGTH){
			reuslt.setListId(QuestionListHash.generate(reuslt.getAuthorId()+reuslt.getName(), reuslt.getTimeCreatedMillis()));
		} else {
			QuestionListHash.addGeneratedCode(reuslt.getListId());
		}
		if (reuslt.getTimeCreatedMillis()==0L){
			reuslt.setTimeCreatedMillis(System.currentTimeMillis());
		}
		return reuslt;
	}

	public static Map<String, Emoji> parseTags(JsonParser jp) throws IOException {
		Map<String, Emoji> m = new HashMap<>();
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
				Emoji unicode = Emoji.fromFormatted(emojiCode);
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
		Question outputBuilder = null;
		String q = null;
		String expl = null;
		String imgSrc= null, fieldName;
		List <Option>opts = null;
		if(jp.currentToken() != JsonToken.START_OBJECT) {
			return null;
		}
		 while(!jp.isClosed()){
			//System.out.println("("+jp.currentName() +":"+jp.getText()+") ");
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
		if (q==null){
			return null;
		}
		outputBuilder = new Question(q, opts);
		outputBuilder.setExplication(expl);
		outputBuilder.setImageSrc(imgSrc);
		//System.out.println("\n"+outputBuilder+"\n");
		return outputBuilder;
	}
	public static List<Option> parseOptionList(JsonParser jp) throws IOException {
		LinkedList<Option> opts = new LinkedList<>();
		if(jp.currentToken() != JsonToken.START_ARRAY){
			return null;
		}
		jp.nextToken();
		while(!jp.isClosed()){
			//System.out.println("("+jp.currentName() +":"+jp.getText()+") ");
			if (jp.currentToken() == JsonToken.START_OBJECT){
				opts.add(parseOption(jp));
				//System.out.print("("+jp.currentName() +":"+jp.getText()+") ");
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
		Option res=null;
		if(jp.currentToken() != JsonToken.START_OBJECT){
			return null;
		}
		while(!jp.isClosed()){
			jp.nextToken();
			//System.out.println("opt ("+jp.currentName() +":"+jp.getText()+") ");
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

	public static List<Question> parseQuestionList(JsonParser jp) throws IOException {
		List<Question> outputBuilder = new LinkedList<>();
		/* iterating over every Question attributes then Options*/
		if(jp.currentToken() != JsonToken.START_ARRAY) {
			return null;
		}
		while (!jp.isClosed()) {
			//System.out.print("("+jp.currentName() +":"+jp.getText()+") ");
			if(jp.currentToken() == JsonToken.START_OBJECT) {
				outputBuilder.add(parseQuestion(jp));
			} else if(jp.currentToken() == JsonToken.END_ARRAY) {
				break;
			}else {
				jp.nextToken();
			}
		}
		return outputBuilder;
	}
}