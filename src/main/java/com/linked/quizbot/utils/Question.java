package com.linked.quizbot.utils;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.ArrayList;
import java.util.Collection;

import com.linked.quizbot.Constants;
import com.linked.quizbot.core.BotCore;

/**
 * The Question class represents a question in a quiz, along with its possible answer options.
 * A question includes the text of the question, a number of correct options,
 * an optional explanation, and a list of possible answers, called options.
 */
public class Question extends LinkedList<Option>{
	private final String question;
	private String explication;

	private String imageSrc = null;//pour l'intant pas encore implementer
	
	/**
	 * Constructor for a true-or-false question.
	 *
	 * @param question
	 * @requires question !=null
	 * @ensures getNumberTrue() == 1
	 * @ensures getQuestion().equals(question)
	 * @ensures getOptions().get(0).equals("True")
	 * @ensures getOptions().get(1).equals("False")
	 */
	public Question(String question){
        super();
        this.question = question;
        explication = null;

        add(new Option("True", true));
        add(new Option("False", false));
	}
	/**
	 * Create a multiple choice question from a collection of answers.
	 *
	 * @param question a relevant and well-asked question.
	 * @param optionsForAnswer all possible answer options, there should be at least one true option.
	 * @requires question !=null
	 * @requires optionsForAnswer!=null
	 * @ensures getQuestion().equals(question)
	 */
	public Question(String question, Collection<? extends Option> optionsForAnswer) {
        super(optionsForAnswer);
        this.question = question;
	}

	/**
	 * Constructs a multiple-choice question with the specified number of correct options.
	 *
	 * @param question the text of the question
	 * @param numberTrue the number of correct options
	 * @param optionsForAnswer the array of possible answer options
	 * @requires question != null
	 * @requires optionsForAnswer != null
	 * @requires numberTrue > 0
	 * @ensures getNumberTrue() == numberTrue
	 * @ensures getQuestion().equals(question)
	 * @ensures (\forall int k; k >= 0 && k < optionsForAnswer.length;
	 *          optionsForAnswer[k].equals(getOptions().get(k)))
	 */
	public Question(String question, int numberTrue, String... optionsForAnswer) {
		int i = 1;
		for (String s : optionsForAnswer){
			add(new Option(s, ((i++)<=numberTrue)));
		}
		this.question = question;
	}
	public Question(String question, Option... optionsForAnswer) {
		this.question = question;
		for (Option option : optionsForAnswer){
			add(option);
		}
	}
	/**
	 * Returns the number of correct options.
	 *
	 * @return the number of correct options
	 */
	public int getNumberTrue() {return getTrueOptions().size();}

	/**
	 * Returns the explanation for the question, if any.
	 *
	 * @return the explanation, or null if none is defined
	 */
	public String getExplication() { return explication;}
	public String getExplicationFriendly() {
		List<String> censored = List.of("null", "nope");
		if (getExplication()==null || censored.contains(getExplication())){
			return Constants.NOEXPLICATION;
		};
		return getExplication();
	}
	public void setExplication(String explication) { this.explication = explication;}
	
	/**
	 * Returns a list of all options for the question, sorted in natural order.
	 *
	 * @return a list of options in natural order
	 */
	public List<Option> getOptions() {
		List<Option> res = new LinkedList<>(this);
		return res;
	}

	/**
	 * Returns a randomly sorted list of all options for the question.
	 *
	 * @return a randomly sorted list of options
	 */
	public List<Option> getOptionsRearraged() {
		Random r = BotCore.getRandom();
		List<Option> res = getOptions();
		res.sort((a,b)->r.nextBoolean()?-1:1);
		return res;
	}
	public void rearrageOptions(Random random){
		sort((a,b)->random.nextBoolean()?-1:1);
	}
	public String getImageSrc (){ return imageSrc;}
	public void setImageSrc(String imageSrc){ this.imageSrc = imageSrc;}
	/**
	 * Returns the text of the question.
	 *
	 * @return the question text
	 */
	public String getQuestion() { return question;}

	/**
	 * Returns the option at the specified index.
	 *
	 * @param index the index of the option
	 * @return the option at the specified index, or null if the index is out of bounds
	 */
	@Override
	public Option get(int index) {
		if(index<0 || index>= size()) {
			return null;
		}
		return getOptions().get(index);
	}

	/**
	 * Returns the list of correct options.
	 *
	 * @return the list of correct options
	 */
	public List<Option> getTrueOptions() {
		List<Option> res = new ArrayList<>();
		for (Option opt:getOptions()) {
			if (opt.isCorrect()) {
				res.add(opt);
			}
		}
		return res;
	}

	/**
	 * Returns the list of incorrect options.
	 *
	 * @return the list of incorrect options
	 */
	public List<Option> getFalseOptions() {
		List<Option> res = new ArrayList<>();
		for (Option opt:getOptions()) {
			if (!opt.isCorrect()) {
				res.add(opt);
			}
		}
		return res;
	}

	/**
	 * Checks if a given option is one of the correct options.
	 *
	 * @param option the option to check
	 * @return true if the option is correct, false otherwise
	 */
	public boolean isCorrect(Option option){
		return option.isCorrect();
	}

	/**
	 * Returns a string representation of this Question in JSON format.
	 *
	 * @return the JSON representation of this Question
	 */
	@Override
	public String toString() {
		String s = "{\n\t\"question\":\""+getQuestion()+"\"";
		s+= ",\n\t\"explication\":";
		if(getExplication()==null || getExplication().equals("null") || getExplication().equals(Constants.NOEXPLICATION)){
			s+=null;
		}else {s+="\""+getExplication()+"\"";}
		s+=",";
		s+= "\n\t\"imageSrc\":"+(getImageSrc()==null?null:"\""+getImageSrc()+"\"")+",";
		s+="\n\t\"options\": [";
		List<Option> opts = getOptions(); opts.sort((a,b)->(a.isCorrect()?-1:1));
		for (Iterator<Option> iter = opts.iterator(); iter.hasNext(); ) {
			s+="\n\t\t"+iter.next().toString();
			if(iter.hasNext()) {
				s+= ",";
			}
		}
		s+= "\n\t]\n}";
		return s;
	}

	/**
	 * Compares this Question to another object for equality.
	 *
	 * @param o the object to compare
	 * @return true if the objects are equal, false otherwise
	 */
	@Override
	public boolean equals(Object o){
			if (o == this){
					return true;
			}
			if (!(o instanceof Question)) {
					return false;
			}
			Question q = (Question) o;
			return getQuestion().equals(q.getQuestion())
					&& getOptions().equals(q.getOptions());
	}
	public static Question getExampleQuestion(){
		Question q = new Question("What is H₂O?", Option.getExampleOption());
		return q;
	}
}

