package com.linked.quizbot.utils;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.io.IOException;
import java.util.ArrayList;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linked.quizbot.Constants;

/**
 * The Option class represents an option that can be displayed in a multiple-choice questionnaire.
 * An option consists of a text value and an optional explanation.
 *
 * The toString will return a Json representation of this Option.
 *
 * The option will not contain its own validity.
 */
public class Option {
	private String txt;
	private String explication;
	private boolean isCorrect;
	
	public static class Parser {
		
		public static Option parse(JsonParser jp, String original) throws IOException{
			String optTxt = null;
			String optExpl = null, fieldName;
			Boolean isCorr = null;
			Option res=null;
			if(jp.currentToken() != JsonToken.START_OBJECT && jp.nextToken() != JsonToken.START_OBJECT){
				throw new IOException(String.format(Constants.ERROR+Constants.RED+"Option.Parser.parse, input is not a json: (%s, %s, %s) (%s, %s, %s) %s"+Constants.RESET,  jp.currentToken(), jp.currentName(), jp.getText(), jp.nextValue(), jp.nextFieldName(), jp.nextTextValue(), original));
			}
			while(!jp.isClosed()){
				if (jp.currentToken() == JsonToken.FIELD_NAME) {
					fieldName = jp.currentName().toLowerCase();
					jp.nextToken();
					/* parsing System.out.print("Option.Parser.parse("+jp.currentToken()+", "+jp.currentName()+") "); */
					switch(fieldName){
						case "option", "text" -> optTxt = jp.getValueAsString();
						case "explication" -> {
							optExpl = jp.getText();
							if (optExpl!=null && optExpl.equals("null")){
								optExpl = null;
							}
						}
						case "iscorrect", "correct" -> {
							isCorr = jp.getBooleanValue();
						}
						default -> {
							jp.skipChildren();
						}
					}
				} else if(jp.currentToken() == JsonToken.END_OBJECT){
					jp.nextToken();
					/* parsing System.out.println("Option.Parser.parse("+jp.currentToken()+", "+jp.currentName()+") "); */
					break;
				} else {
					jp.nextToken();
					/* parsing System.out.print("Option.Parser.parse("+jp.currentToken()+", "+jp.currentName()+") "); */
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
		public static List<Option> parseList(JsonParser jp, String original) throws IOException{
			LinkedList<Option> opts = new LinkedList<>();
			Option opt;
			if(jp.currentToken() != JsonToken.START_ARRAY && jp.nextToken() != JsonToken.START_ARRAY){
				throw new IOException(String.format(Constants.ERROR+Constants.RED+"Option.Parser.parseList, input is not a json: (%s, %s, %s) (%s, %s, %s) %s"+Constants.RESET,  jp.currentToken(), jp.currentName(), jp.getText(), jp.nextValue(), jp.nextFieldName(), jp.nextTextValue(), original));
			}
			while(!jp.isClosed()){
				if (jp.currentToken() == JsonToken.START_OBJECT){
					opt = Option.Parser.parse(jp, original);
					if(opt!=null && opt.getText()!=null)	opts.add(opt);
				} else if (jp.currentToken() == JsonToken.END_ARRAY){
					jp.nextToken();
					/* parsing System.out.println("Option.Parser.parseList("+jp.currentToken()+", "+jp.currentName()+") "); */
					break;
				} else {
					jp.nextToken();
					/* parsing System.out.print("Option.Parser.parseList("+jp.currentToken()+", "+jp.currentName()+") "); */
				}
			};
			return opts;
		}
	}
	/**
	 * Constructs an Option with only text.
	 *
	 * @param text the text of the option
	 * @requires text != null
	 * @ensures getText().equals(text)
	 */
	public Option(@NotNull String text, boolean isCorrect){
		txt = text;
		explication = null;
		this.isCorrect = isCorrect;
	}
	
	/**
	 * Constructs an Option with text and an explanation.
	 *
	 * @param text the text of the option
	 * @param explication the explanation for the option
	 * @requires text != null && explication != null
	 * @ensures getText().equals(text) && getExplication().equals(explication)
	 */
	public Option(@Nullable String text, boolean isCorrect, @Nullable String explication){
		this(text, isCorrect);
		this.explication = explication;
	}
	
	/**
	 * Gets the text of the option.
	 *
	 * @return the text of the option
	 */
	public String getText() {
		return txt;
	}
	public boolean isCorrect() {
		return isCorrect;
	}
	
	/**
	 * Retrieves the explanation of the option.
	 *
	 * @return the explanation of the option, or null if none is defined
	 */
	public String getExplication() {
		List<String> censored = List.of("null", "nope");
		if (explication==null || censored.contains(explication)){
			return Constants.NOEXPLICATION;
		};
		return explication;
	}
	
	public static Comparator<? super Option> comparator() {
		return (e, f)->((e.isCorrect()^f.isCorrect())?(e.isCorrect()?-1:1):0);
	}
	
	/**
	 * Returns a string representation of this Option in JSON format.
	 *
	 * @return the JSON representation of this Option
	 */
	@Override
	public String toString() {
		return toJson().replace("\n\t\"" ,"\"");
	}
	public String toJson() {
		
		try {
			String s = "{\n\t\"option\":"+Constants.MAPPER.writeValueAsString(getText());
			s+= ",\n\t\"isCorrect\":"+isCorrect();
			s+= ",\n\t\"explication\":";
			if(getExplication()==null || getExplication().equals("null") || getExplication().equals(Constants.NOEXPLICATION)){
				s+=null;
			}else {s += Constants.MAPPER.writeValueAsString(getExplication());}
			s+="\n}";
			return s;
		} catch (Exception e){
			System.err.println(String.format(Constants.ERROR + "[%s.toJson() failed]%s", getClass(), e.getMessage()));
		}
		return null;
	}
	/**
	 * Compares this Option to another object for equality.
	 * An Option is considered equal to another Option if their text values match.
	 * It is also considered equal to a String if the String matches its text value.
	 *
	 * @param o the object to compare
	 * @return true if the objects are equal, false otherwise
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o)	return true;
		if(!(o instanceof Option)) {
			if ((o instanceof String)) {
				String s = (String) o;
				return getText().equals(s);
			}
			return false;
		}
		Option p = (Option) o;
		if (getText() == p.getText()){
			return true;
		}
		if (getText()==null || null==p.getText()){
			return false;
		}
		return getText().equals(p.getText());
	}
	public static List<Option> getExampleOption(){
		List<Option> opts = new ArrayList<>();
		opts.add(new Option("Water", true, "Correct! H2O is the chemical formula for water"));
		opts.add(new Option("Carbon Dioxide", false, "Incorrect. CO2 is carbon dioxide"));
		return opts;
	}
}
