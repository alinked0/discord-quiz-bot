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
    public static User fromJsonFile(String filePathToJson)throws IOException{
		File f = new File(filePathToJson);
		if (!f.exists()){
			throw new FileNotFoundException(filePathToJson);
		}
		JsonParser jp =  new JsonFactory().createParser(f);
		return parser(jp);
    }

	public static User fromString(String arg) throws IOException{
		JsonParser jp =  new JsonFactory().createParser(arg);
		return parser(jp);
	}

	public static User parser(JsonParser jp) throws IOException{
		if (jp.nextToken() != JsonToken.START_OBJECT){
			throw new IOException();
		}
		User.Builder userBuilder = new User.Builder();
		double totalPointsEverGained=0;
		int numberOfGamesPlayed = 0;
		String prefixe = null;
		boolean useButtons = true;
		String fieldName;
		Map<String, Emoji> m = new HashMap<>();
		while (!jp.isClosed()){
			//System.out.println("("+jp.currentName() +":"+jp.getText()+") "+jp.currentToken());
			if (jp.currentToken() == JsonToken.FIELD_NAME) {
				fieldName = jp.currentName();
				jp.nextToken();
				switch (fieldName){
					case "prefixe" -> {
						prefixe=jp.getText();
					}
					case "totalPointsEverGained" -> {
						totalPointsEverGained=jp.getDoubleValue();
					}
					case "numberOfGamesPlayed" -> {
						numberOfGamesPlayed=jp.getValueAsInt();
					}
					case "tagEmojiPerTagName" -> {
						m=parseEmojiPerTagName(jp);
					}
					case "useButtons" -> {
						useButtons=jp.getValueAsBoolean();
					}
				}
			} else {
				jp.nextToken();
			}
		}
		userBuilder.totalPointsEverGained(totalPointsEverGained);
		userBuilder.numberOfGamesPlayed(numberOfGamesPlayed);
		userBuilder.tagEmojiPerTagName(m);
		userBuilder.perferedPrefixe(prefixe);
		userBuilder.useButtons(useButtons);
		return userBuilder.build();
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
