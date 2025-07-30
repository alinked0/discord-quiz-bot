package com.linked.quizbot.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import net.dv8tion.jda.api.entities.emoji.Emoji;

public class UserDataParser {
    public static User.Builder fromJsonFile(String filePathToJson)throws IOException{
		File f = new File(filePathToJson);
		if (!f.exists()){
			throw new FileNotFoundException(filePathToJson);
		}
		JsonParser jp =  new JsonFactory().createParser(f);
		return parser(jp);
    }

	public static User.Builder fromString(String arg) throws IOException{
		JsonParser jp =  new JsonFactory().createParser(arg);
		return parser(jp);
	}

	public static User.Builder parser(JsonParser jp) throws IOException{
		if (jp.nextToken() != JsonToken.START_OBJECT){
			throw new IOException();
		}
		User.Builder userBuilder = new User.Builder();
		String fieldName;
		while (!jp.isClosed()){
			//System.out.println("("+jp.currentName() +":"+jp.getText()+") "+jp.currentToken());
			if (jp.currentToken() == JsonToken.FIELD_NAME) {
				fieldName = jp.currentName();
				jp.nextToken();
				switch (fieldName){
					case "userId" -> {
						userBuilder.id(jp.getText());
					}
					case "prefixe" -> {
						userBuilder.prefixe(jp.getText());
					}
					case "totalPointsEverGained" -> {
						userBuilder.totalPointsEverGained(jp.getDoubleValue());
					}
					case "numberOfGamesPlayed" -> {
						userBuilder.numberOfGamesPlayed(jp.getValueAsInt());
					}
					case "tagEmojiPerTagName" -> {
						userBuilder.tagEmojiPerTagName(parseEmojiPerTagName(jp));
					}
					case "useButtons" -> {
						userBuilder.useButtons(jp.getValueAsBoolean());
					}
					case "useAutoNext" -> {
						userBuilder.useAutoNext(jp.getValueAsBoolean());
					}
				}
			} else {
				jp.nextToken();
			}
		}
		return userBuilder;
	}
	public static Map<String, Emoji> parseEmojiPerTagName(JsonParser jp) throws IOException {
		String tagName;
		String emoji;
		Map<String, Emoji> m = new HashMap<>();
		if (jp.currentToken() == JsonToken.FIELD_NAME && "getTagEmojiPerTagName".equals(jp.currentName())) {
			jp.nextToken();
		}
		while (jp.nextToken() != JsonToken.END_OBJECT && !jp.isClosed()) {
			//System.out.print("("+jp.currentName() +":"+jp.getText()+") ");
			if (jp.currentToken() == JsonToken.FIELD_NAME) {
				tagName = jp.currentName();
				jp.nextToken();
				emoji = jp.getText();
				m.put(tagName, Emoji.fromFormatted(emoji));
				//System.out.print("("+jp.currentName() +":"+jp.getText()+") ");
			}
		}
		jp.nextToken();
		return m;
	}
}
