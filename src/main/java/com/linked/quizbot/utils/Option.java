package com.linked.quizbot.utils;

import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
		return explication;
	}
	public String getExplicationFriendly() {
		List<String> censored = List.of("null", "nope");
		if (getExplication()==null || censored.contains(getExplication())){
			return Constants.NOEXPLICATION;
		};
		return getExplication();
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
		return toJson().replace("\n\t\"","\"");
	}
	public String toJson() {
		ObjectMapper mapper = new ObjectMapper();
		try {
			String s = "{\n\t\"text\":"+mapper.writeValueAsString(getText());
			s+= ",\n\t\"isCorrect\":"+isCorrect();
			s+= ",\n\t\"explication\":";
			if(getExplication()==null || getExplication().equals("null") || getExplication().equals(Constants.NOEXPLICATION)){
				s+=null;
			}else {s += mapper.writeValueAsString(getExplication());}
			s+="\n}";
			return s;
		} catch (Exception e){
			System.err.println("[toJson() failed]"+e.getMessage());
		}
		String s = "{\n\t\"text\":\""+getText()+"\"";
		s+= ",\n\t\"isCorrect\":"+isCorrect();
		s+= ",\n\t\"explication\":";
		if(getExplication()==null || getExplication().equals("null") || getExplication().equals(Constants.NOEXPLICATION)){
			s+=null;
		}else {s += "\""+getExplication()+"\"";}
		s+="\n}";
		return s;
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
