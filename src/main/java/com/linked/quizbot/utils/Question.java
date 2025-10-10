package com.linked.quizbot.utils;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linked.quizbot.Constants;
import com.linked.quizbot.core.BotCore;

/**
 * The Question class represents a question in a quiz, along with its possible answer options.
 * A question includes the text of the question, a number of correct options,
 * an optional explanation, and a list of possible answers, called options.
 */
public class Question{
	private final String question;
	private String explication;
	private List<Option> options = new LinkedList<>();


	private String imageSrc = null;// TODO pour l'intant pas encore implementer
	public static class Builder {
		private final List<Option> list= new LinkedList<>();
		private  String question = null;
		private String explication= null;
		private String imageSrc = null;

		public  Builder question(String question){
			this.question = question;
			return this;
		}
		public  Builder explication(String explication){
			this.explication = explication;
			return this;
		}
		public  Builder imageSrc(String imageSrc){
			this.imageSrc = imageSrc;
			return this;
		}
		public Builder add(Option opt){
			if (opt!=null){
				list.add(opt);
			}
			return this;
		}
		public Builder add(Question question){
			return this.question(question.getQuestion()).explication(question.getExplication()).imageSrc(question.getImageSrc()).addAll(question.getOptions());
		}
		public Builder addAll(List<Option> c){
			list.addAll(c);
			return this;
		}
		public Question build(){
			this.list.sort((e, f) -> e.isCorrect()?-1:1);
			return new Question(this);
		}
	}
	public Question(Builder builder){
        this.options = builder.list;
        this.question = builder.question;
        this.explication = builder.explication;
		this.imageSrc = builder.imageSrc;
	}
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
        this.options.addAll(optionsForAnswer);
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
			get(0);
		}
		this.question = question;
	}
	public Question(String question, Option... optionsForAnswer) {
		this.question = question;
		for (Option option : optionsForAnswer){
			add(option);
		}
	}
	public boolean add(Option opt) {
		options.add(opt);
		return true;
	}
	public boolean addAll(Collection<? extends Option> opt) {
		options.addAll(opt);
		return true;
	}
	public boolean contains(Object o) {
		return options.contains(o);
	}
	public int size(){
		return options.size();
	}

	/**
	 * Returns the option at the specified index.
	 *
	 * @param index the index of the option
	 * @return the option at the specified index
	 * @throws IndexOutOfBoundsException - if the index is out of range (index < 0 || index >= size())
	 */
	public Option get(int index){
		return options.get(index);
	}

	/**
	 * Returns the number of correct options.
	 *
	 * @return the number of correct options
	 */
	public int getNumberTrue() {
		int t = 0;
		for (Option opt:getOptions()) {
			if (opt.isCorrect()) {
				++t;
			}
		}
		return t;
	}

	/**
	 * Returns the explanation for the question, if any.
	 *
	 * @return the explanation, or null if none is defined
	 */
	public String getExplication() { 
		List<String> censored = List.of("null", "nope");
		if (explication==null || censored.contains(explication)){
			return Constants.NOEXPLICATION;
		};
		return explication;
	}
	
	public void setExplication(String explication) { this.explication = explication;}
	
	/**
	 * Returns a list of all options for the question, sorted in natural order.
	 *
	 * @return a list of options in natural order
	 */
	public List<Option> getOptions() {
		List<Option> res = new LinkedList<>(options);
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
	public Question rearrageOptions(){
		Random r = BotCore.getRandom();
		return rearrageOptions((a,b)->r.nextBoolean()?-1:1);
	}
	public Question rearrageOptions(Comparator<? super Option> comp){
		options.sort(comp);
		return this;
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
		try {
			String s = "{\n\t"+Constants.MAPPER.writeValueAsString("question")+":"+Constants.MAPPER.writeValueAsString(""+getQuestion()+"")+"";
			s+= ",\n\t"+Constants.MAPPER.writeValueAsString("explication")+":";
			if(getExplication()==null || getExplication().equals("null") || getExplication().equals(Constants.NOEXPLICATION)){
				s+=null;
			}else {s+=""+Constants.MAPPER.writeValueAsString(""+getExplication()+"")+"";}
			s+=",";
			s+= "\n\t"+Constants.MAPPER.writeValueAsString("imageSrc")+":"+(getImageSrc()==null?null:""+Constants.MAPPER.writeValueAsString(""+getImageSrc()+"")+"")+",";
			s+="\n\t"+Constants.MAPPER.writeValueAsString("options")+": [";
			List<Option> opts = getOptions(); opts.sort((a,b)->(a.isCorrect()?-1:1));
			for (Iterator<Option> iter = opts.iterator(); iter.hasNext(); ) {
				s+="\n\t\t"+iter.next().toString();
				if(iter.hasNext()) {
					s+= ",";
				}
			}
			s+= "\n\t]\n}";
			return s;
		}catch(JsonProcessingException e){e.printStackTrace();}
		return null;
	}
	public Question clone(){
		return new Question.Builder().add(this).build();
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
					&& getOptions().containsAll(q.getOptions());
	}
	
	public static Question getExampleQuestion(){
		Question q = new Question("What is H₂O?", Option.getExampleOption());
		return q;
	}
}

