package com.linked.quizbot.utils;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.linked.quizbot.Constants;
import com.linked.quizbot.core.BotCore;

/**
 * The Question class represents a question in a quiz, along with its possible answer options.
 * A question includes the text of the question, a number of correct options,
 * an optional explanation, and a list of possible answers, called options.
 * 
 * @author alinked0
 * @version 1.0
 * @since 2025-02-01
 * @see Option
 * @see Question.Builder
 */
public class Question{
	private final String question;
	private String explication;
	private List<Option> options = new LinkedList<>();


	private String imageSrc = null;// TODO impl a way to attach images
	
	/**
	 * Inner class implementing the **Builder pattern** for creating and initializing {@link Question} objects.
	 * <p>
	 * This allows for step-by-step construction of complex questions with options, explanations, and image sources.
	 * </p>
	 */
	public static class Builder {
		private final List<Option> list= new LinkedList<>();
		private  String question = null;
		private String explication= null;
		private String imageSrc = null;

		/**
		 * Sets the text of the question.
		 * @param question The main text of the question.
		 * @return The current Builder instance for chaining.
		 * @requires question != null
		 * @ensures this.question.equals(question)
		 */
		public  Builder question(String question){
			this.question = question;
			return this;
		}

		/**
		 * Sets the optional explanation or reasoning for the answer.
		 * @param explication The explanation text.
		 * @return The current Builder instance for chaining.
		 * @ensures this.explication.equals(explication)
		 */
		public  Builder explication(String explication){
			this.explication = explication;
			return this;
		}
		
		/**
		 * Sets the optional URL or path to an image associated with the question.
		 * @param imageSrc The image source string.
		 * @return The current Builder instance for chaining.
		 * @ensures this.imageSrc.equals(imageSrc)
		 */
		public  Builder imageSrc(String imageSrc){
			this.imageSrc = imageSrc;
			return this;
		}
		
		/**
		 * Adds a single {@link Option} to the list of possible answers.
		 * @param opt The answer option to add. It can be correct or incorrect.
		 * @return The current Builder instance for chaining.
		 * @ensures this.list.size() == \old(this.list.size()) + (opt != null ? 1 : 0)
		 */
		public Builder add(Option opt){
			if (opt!=null){
				list.add(opt);
			}
			return this;
		}

		/**
		 * Copies the content (question text, explanation, image source, and all options) from an existing {@link Question}.
		 * @param question The existing question to copy from.
		 * @return The current Builder instance for chaining.
		 * @requires question != null
		 * @ensures this.question.equals(question.getQuestion())
		 * @ensures this.list.containsAll(question.getOptions())
		 */
		public Builder add(Question question){
			return this.question(question.getQuestion()).explication(question.getExplication()).imageSrc(question.getImageSrc()).addAll(question.getOptions());
		}


		/**
		 * Adds a collection of {@link Option} objects to the list of possible answers.
		 * @param c The collection of options to add.
		 * @return The current Builder instance for chaining.
		 * @requires c != null
		 * @ensures this.list.containsAll(c)
		 */
		public Builder addAll(List<Option> c){
			list.addAll(c);
			return this;
		}

		/**
		 * Constructs the final {@link Question} object.
		 * <p>Before constructing, the options are sorted with correct answers preceding incorrect answers.</p>
		 * @return The newly constructed {@link Question} object.
		 * @requires this.question != null
		 * @ensures \result != null
		 * @ensures \result.getOptions() is sorted by correctness (true options first).
		 */
		public Question build(){
			this.list.sort((e, f) -> e.isCorrect()?-1:1);
			return new Question(this);
		}
	}


	/**
	 * Constructs a {@code Question} object from a {@link Builder}.
	 * @param builder The {@link Question.Builder} containing the question data.
	 * @requires builder != null
	 * @ensures this.question == builder.question
	 * @ensures this.options.containsAll(builder.list)
	 */
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

	/**
	 * Constructs a multiple-choice question directly from an array of pre-built {@link Option} objects.
	 *
	 * @param question The text of the question.
	 * @param optionsForAnswer The array of possible answer options (must contain at least one correct option).
	 * @requires question != null
	 * @requires optionsForAnswer != null
	 * @requires optionsForAnswer.length > 0
	 * @ensures getQuestion().equals(question)
	 * @ensures getOptions().containsAll(Arrays.asList(optionsForAnswer))
	 */
	public Question(String question, Option... optionsForAnswer) {
		this.question = question;
		for (Option option : optionsForAnswer){
			add(option);
		}
	}

	/**
	 * Adds a single {@link Option} to the question's list of possible answers.
	 * @param opt The answer option to add.
	 * @return {@code true} (as per {@link List#add(Object)} specification).
	 * @requires opt != null
	 * @ensures options.contains(opt)
	 * @ensures options.size() == \old(options.size()) + 1
	 */
	public boolean add(Option opt) {
		options.add(opt);
		return true;
	}

	/**
	 * Adds all {@link Option} objects from a collection to the question's list of possible answers.
	 * @param opt The collection of options to add.
	 * @return {@code true} if this list changed as a result of the call.
	 * @requires opt != null
	 * @ensures options.containsAll(opt)
	 */
	public boolean addAll(Collection<? extends Option> opt) {
		options.addAll(opt);
		return true;
	}

	/**
	 * Checks if this question's options list contains the specified object.
	 * @param o The object to be checked for containment.
	 * @return {@code true} if the options list contains the object.
	 * @ensures \result == options.contains(o)
	 */
	public boolean contains(Object o) {
		return options.contains(o);
	}


	/**
	 * Returns the number of options (correct and incorrect) associated with this question.
	 * @return The number of options.
	 * @ensures \result == options.size()
	 */
	public int size(){
		return options.size();
	}

	/**
	 * Returns the option at the specified index.
	 *
	 * @param index The index of the option.
	 * @return The option at the specified index.
	 * @throws IndexOutOfBoundsException if the index is out of range (index < 0 || index >= size()).
	 * @requires 0 <= index < size()
	 * @ensures \result == options.get(index)
	 */
	public Option get(int index){
		return options.get(index);
	}

	/**
	 * Returns the number of correct options.
	 *
	 * @return The number of correct options.
	 * @ensures \result >= 0
	 * @ensures (\forall Option opt; getOptions().contains(opt); opt.isCorrect() ==> \result++)
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
	 * @return the explanation,  or {@link Constants#NOEXPLICATION} if none is defined.
	 */
	public String getExplication() { 
		List<String> censored = List.of("null", "nope");
		if (explication==null || censored.contains(explication)){
			return Constants.NOEXPLICATION;
		};
		return explication;
	}
	
	/**
	 * Sets a new explanation for the question.
	 * @param explication The new explanation text.
	 * @ensures this.explication == explication
	 */
	public void setExplication(String explication) { this.explication = explication;}
	
	/**
	 * Returns a new {@code List} containing all options for the question.
	 * <p>The order of options reflects the internal storage order (by default, correct options come first).</p>
	 *
	 * @return A new list of options.
	 * @ensures \result is a new List instance
	 * @ensures \result.size() == size()
	 */
	public List<Option> getOptions() {
		List<Option> res = new LinkedList<>(options);
		return res;
	}

	/**
	 * Returns a new, randomly sorted list of all options for the question.
	 * <p>Uses the internal {@link BotCore#getRandom()} instance for shuffling.</p>
	 * @return A randomly sorted list of options.
	 * @ensures \result is a new List instance
	 * @ensures \result.size() == size()
	 */
	public List<Option> getOptionsRearraged() {
		Random r = BotCore.getRandom();
		List<Option> res = getOptions();
		res.sort((a,b)->r.nextBoolean()?-1:1);
		return res;
	}

	/**
	 * Rearranges the internal list of options using a random comparator.
	 * @return The current Question object, with options shuffled.
	 * @ensures this.options is randomly shuffled.
	 */
	public Question rearrageOptions(){
		Random r = BotCore.getRandom();
		return rearrageOptions((a,b)->r.nextBoolean()?-1:1);
	}

	/**
	 * Rearranges the internal list of options using the specified {@link Comparator}.
	 * @param comp The comparator to determine the new order of options.
	 * @return The current Question object, with options sorted according to {@code comp}.
	 * @requires comp != null
	 * @ensures this.options is sorted according to comp.
	 */
	public Question rearrageOptions(Comparator<? super Option> comp){
		options.sort(comp);
		return this;
	}

	/**
	 * Returns the optional image source URL or path associated with the question.
	 * @return The image source string, or {@code null} if not set.
	 * @ensures \result == imageSrc
	 */
	public String getImageSrc (){ return imageSrc;}


	/**
	 * Sets the optional image source URL or path associated with the question.
	 * @param imageSrc The image source string.
	 * @ensures this.imageSrc == imageSrc
	 */
	public void setImageSrc(String imageSrc){ this.imageSrc = imageSrc;}

	/**
	 * Returns the text of the question.
	 * @return The question text.
	 * @ensures \result == question
	 */
	public String getQuestion() { return question;}

	/**
	 * Returns a new list containing only the correct options.
	 * @return The list of correct options.
	 * @ensures (\forall Option opt; \result.contains(opt); opt.isCorrect())
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
	 * Returns a new list containing only the incorrect options.
	 * @return The list of incorrect options.
	 * @ensures (\forall Option opt; \result.contains(opt); !opt.isCorrect())
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
	 * <p>This method simply delegates to {@link Option#isCorrect()}.</p>
	 * @param option The option to check.
	 * @return {@code true} if the option is correct, {@code false} otherwise.
	 * @requires option != null
	 * @ensures \result == option.isCorrect()
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


	/**
	 * Creates and returns a deep clone of this {@code Question} object.
	 * @return A new {@code Question} object with identical content and options.
	 * @ensures \result != this
	 * @ensures \result.equals(this)
	 */
	public Question clone(){
		return new Question.Builder().add(this).build();
	}
	
	/**
	 * Compares this Question to another object for equality.
	 * <p>Two questions are considered equal if their question text is identical and they contain the same set of options.</p>
	 *
	 * @param o The object to compare.
	 * @return {@code true} if the objects are equal, {@code false} otherwise.
	 * @ensures \result == (o instanceof Question && getQuestion().equals(((Question) o).getQuestion()) && getOptions().containsAll(((Question) o).getOptions()))
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
	
	/**
	 * Static factory method to retrieve an example {@code Question} object.
	 * <p>The example question is: "What is H₂O?" with the default example option.</p>
	 * @return An example {@link Question}.
	 * @ensures \result != null
	 * @ensures \result.getQuestion().equals("What is H₂O?")
	 */
	public static Question getExampleQuestion(){
		Question q = new Question("What is H₂O?", Option.getExampleOption());
		return q;
	}
}

