package com.linked.quizbot.utils;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.linked.quizbot.Constants;

public final class Awnser {
	public final Long duration;
	public final Set<Option> response;
	
	public static class Parser {
		public static Awnser parse(JsonParser jp, String original) throws IOException{
			if (jp.currentToken() != JsonToken.START_OBJECT && jp.nextToken() != JsonToken.START_OBJECT){
				throw new IOException(String.format(Constants.ERROR+Constants.RED+"Awnser.Parser.parse, input is not a json: (%s, %s, %s) (%s, %s, %s) %s"+Constants.RESET,  jp.currentToken(), jp.currentName(), jp.getText(), jp.nextValue(), jp.nextFieldName(), jp.nextTextValue(), original));
			}
			Awnser awnser = null;
			Long duration = null;
			Set<Option> response = null;
			String fieldName;
			while (!jp.isClosed()){
				if (jp.currentToken() == JsonToken.FIELD_NAME) {
					fieldName = jp.currentName();
					jp.nextToken();
					/* parsing System.out.print("Awnser.Parser.parse("+jp.currentToken()+", "+jp.currentName()+") "); */
					switch (fieldName){
						case "duration" -> {
							duration = jp.getLongValue();
						}
						case "response" -> {
							response = new HashSet<>(Option.Parser.parseList(jp, original));
						}
						default -> {
							jp.skipChildren();
						}
					}
				} else if (jp.currentToken() == JsonToken.END_OBJECT){
					jp.nextToken();
					/* parsing System.out.println("Awnser.Parser.parse("+jp.currentToken()+", "+jp.currentName()+") "); */
					break;
				} else {
					jp.nextToken();
					/* parsing System.out.print("Awnser.Parser.parse("+jp.currentToken()+", "+jp.currentName()+") "); */
				}
			}
			if (duration!=null && response!=null){
				awnser = new Awnser(duration, response);
			}
			return awnser;
		}
	}
	
	public static Awnser of(final Long duration, Set<Option> response) {
		return new Awnser(duration, response);
	}
	
	public Awnser(final Long duration, Set<Option> response) {
		if (duration == null || response == null) throw new NullPointerException();
		this.duration = duration;
		this.response = response;
	}
	
	public Long getDuration() {
		return duration;
	}
	
	public Set<Option> getResponse() {
		return response;
	}
	public boolean equals(Object o){
		if (this == o)	return true;
		if(!(o instanceof Awnser)) {
			return false;
		}
		Awnser p = (Awnser) o;
		return getDuration().equals(p.getDuration()) && getResponse().equals(p.getResponse());
	}
	public String toString(){
		return toJson();
	}
	public String toJson(){
		String s = "[";
		Iterator<Option> iter  =getResponse().iterator();
		while (iter.hasNext()){
			s+= iter.next().toJson();
			if (iter.hasNext()) s+= ",";
		}
		s+= "]";
		return String.format("{\"duration\":%d, \"response\":%s}", getDuration(), s);
	}
}
