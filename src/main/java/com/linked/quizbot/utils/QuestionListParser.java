package com.linked.quizbot.utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Date;


import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.linked.quizbot.Constants;

import java.util.Collection;

public class QuestionListParser {
    public static QuestionList jsonToQuestionList(String filePathToJson){
        long start = System.nanoTime();
		QuestionList result = new QuestionList();
		try {
			File f = new File(filePathToJson);
			if (!f.exists()){
				System.err.println("  $> File not found"+ f.getAbsoluteFile());
				return null;
			}
			JsonParser jp =  new JsonFactory().createParser(new File(filePathToJson));
			JsonToken tmp;
			/*Check if result file is a json */
			if (jp.nextToken() != JsonToken.START_OBJECT) {
				System.err.println("$> The file is not a json");
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
					case "authorId" -> result.setAuthorId(jp.getText());
					case "theme" -> result.setTheme(jp.getText());
					case "name" -> result.setName(jp.getText());
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
							if (expl!=null && expl.equals("null")){
								expl = null;
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
							if (optExpl!=null && optExpl.equals("null")){
								optExpl = null;
							}
						}
						case "isCorrect" -> isCorr = jp.getValueAsBoolean();//Text().equals("true")?true:false;
					}
				}while(true);
				//System.out.println("");
				result.add(new Question(q, opt));
				result.get(result.size()-1).setImageSrc(imgSrc);
				result.get(result.size()-1).setExplication(expl);
			}
            System.out.printf("   $> time importJson = `%.3f ms` n=%d name=%s\n",(System.nanoTime() - start) / 1000000.00 ,result.size(),result.getName());
			return result;
		} catch (IOException e) {
			System.err.println("Error: Parser creation failed");
			e.printStackTrace();
		} catch (NullPointerException e) {
			System.err.println("Error Json representation of ListQuestion is invalid");
			e.printStackTrace();
		}
        System.out.printf("   $> time importJson = `%.3f ms`\n",(System.nanoTime() - start) / 1000000.00);
		return null;
    }
	public static QuestionList stringToQuestionList(String arg){
		Date d = new Date();
        File f = new File(Constants.LISTSPATH+Constants.SEPARATOR+"tmp"+d.getTime());
		try {
			if(!f.getParentFile().exists()) {
				f.getParentFile().mkdirs();
			}
			BufferedWriter buff = Files.newBufferedWriter(f.toPath());
			buff.write(arg);
			buff.close();
		} catch (IOException e) {
			System.err.println("$> An error occurred while adding a List of questions.");
			e.printStackTrace();
			return null;
		}
		QuestionList res = new QuestionList(f.getAbsolutePath());
		f.delete();
		return res;
	}
}