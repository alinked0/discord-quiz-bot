package com.linked.quizbot.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.linked.quizbot.Constants;

import net.dv8tion.jda.api.utils.TimeFormat;
import net.dv8tion.jda.api.utils.Timestamp;

public class Attempt {
	private QuestionList questionList;
	private String userId;
	private Long timeStartedMillis;
	private Long timeEndedMillis;
	private Map<Integer, Awnser> awnsersByQuestion;
	private Double score = null;
	
	public static class Parser {
		public static List<Attempt> parseList(JsonParser jp, String original) throws IOException{
			List<Attempt> attempts = new ArrayList<>();
			if(jp.currentToken() != JsonToken.START_ARRAY && jp.nextToken() != JsonToken.START_ARRAY){
				throw new IOException(String.format(Constants.ERROR + "User.Parser.parseAttemptList, input is not a json: (%s, %s, %s) (%s, %s, %s) \n%s\n"+Constants.RESET,  jp.currentToken(), jp.currentName(), jp.getText(), jp.nextValue(), jp.nextFieldName(), jp.nextTextValue(), original));
			}
			while(!jp.isClosed()){
				if (jp.currentToken() == JsonToken.START_OBJECT){
					attempts.add(Attempt.Parser.parse(jp, original));
				} else if (jp.currentToken() == JsonToken.END_ARRAY){
					jp.nextToken();
					/* parsing System.out.println("Attempt.Parser.parseList("+jp.currentToken()+", "+jp.currentName()+") "); */
					break;
				} else {
					jp.nextToken();
					/* parsing System.out.print("Attempt.Parser.parseList("+jp.currentToken()+", "+jp.currentName()+") "); */
				}
			};
			return attempts;
		}
		public static Attempt parse(JsonParser jp, String original) throws IOException{
			if (jp.currentToken()!= JsonToken.START_OBJECT && jp.nextToken() != JsonToken.START_OBJECT){
				throw new IOException(String.format(Constants.ERROR+Constants.RED+"Attempt.parse, input is not a json: (%s, %s, %s) (%s, %s, %s) \n%s\n"+Constants.RESET,  jp.currentToken(), jp.currentName(), jp.getText(), jp.nextValue(), jp.nextFieldName(), jp.nextTextValue(), original));
			}
			QuestionList questionList = null;
			String userId= null;
			Long timeStartedMillis= null;
			Long timeEndedMillis= null;
			Map<Integer, Awnser> awnsersByQuestion= null;
			String fieldName;
			while (!jp.isClosed()){
				if (jp.currentToken() == JsonToken.FIELD_NAME) {
					fieldName = jp.currentName();
					jp.nextToken();
					/* parsing System.out.print("Attempt.Parser.parse("+jp.currentToken()+", "+jp.currentName()+") "); */
					switch (fieldName){
						case "questionList", "list" -> {
							questionList = QuestionList.Parser.parse(jp, original).build();
						}
						case "userId" -> {
							userId = jp.getText();
						}
						case "timeStartedMillis", "start" -> {
							timeStartedMillis = jp.getLongValue();
						}
						case "timeEndedMillis", "end" -> {
							timeEndedMillis =jp.getLongValue();
						}
						case "awnsersByQuestion", "response", "awnsers" -> {
							awnsersByQuestion = parseAwnsersByQuestion(jp, original);
						}
						default -> {
							jp.skipChildren();
						}
					}
				} else if (jp.currentToken() == JsonToken.END_OBJECT){
					jp.nextToken();
					/* parsing System.out.println("Attempt.Parser.parse("+jp.currentToken()+", "+jp.currentName()+") "); */
					break;
				} else {
					jp.nextToken();
					/* parsing System.out.print("Attempt.Parser.parse("+jp.currentToken()+", "+jp.currentName()+") "); */
				}
			}
			Attempt att = null;
			if (questionList!=null && userId!=null && timeStartedMillis!=null && timeEndedMillis!=null && awnsersByQuestion!=null){
				att = new Attempt(userId, questionList, timeStartedMillis, timeEndedMillis, awnsersByQuestion);
			}
			return att;
		}
		public static Map<Integer, Awnser> parseAwnsersByQuestion(JsonParser jp, String original) throws IOException{
			Map<Integer, Awnser> awnsersByQuestion= new HashMap<>();
			String question;
			Awnser awnser;
			if (jp.currentToken() != JsonToken.START_OBJECT && jp.nextToken() != JsonToken.START_OBJECT ) {
				throw new IOException(String.format(Constants.ERROR+Constants.RED+"Attempt.Parser.parseAwnsersByQuestion, input is not a json: (%s, %s, %s) (%s, %s, %s) \n%s\n"+Constants.RESET,  jp.currentToken(), jp.currentName(), jp.getText(), jp.nextValue(), jp.nextFieldName(), jp.nextTextValue(), original));
			}
			while (!jp.isClosed()) {
				/* parsing System.out.print("Attempt.Parser.parseAwnsersByQuestion("+jp.currentToken()+", "+jp.currentName()+") "); */
				if (jp.currentToken() == JsonToken.FIELD_NAME) {
					question = jp.currentName();
					jp.nextToken();
					/* parsing System.out.print("Attempt.Parser.parseAwnsersByQuestion("+jp.currentToken()+", "+jp.currentName()+") "); */
					awnser = Awnser.Parser.parse(jp, original);
					awnsersByQuestion.put(Integer.valueOf(question), awnser);
				} else if (jp.currentToken() == JsonToken.END_OBJECT){
					jp.nextToken();
					/* parsing System.out.println("Attempt.Parser.parseAwnsersByQuestion("+jp.currentToken()+", "+jp.currentName()+") "); */
					break;
				} else {
					jp.nextToken();
					/* parsing System.out.print("Attempt.Parser.parseAwnsersByQuestion("+jp.currentToken()+", "+jp.currentName()+") "); */
				}
			}
			return awnsersByQuestion;
		}
	}
	
	public Attempt(String userId, QuestionList list){
		this(userId, list, System.currentTimeMillis(), 0L, new HashMap<>());
	}
	
	public Attempt(String userId, QuestionList questionList, Long timeStartedMillis, Long timeEndedMillis, Map<Integer, Awnser> awnsersByQuestion){
		this.questionList = questionList;
		this.userId = userId;
		this.timeStartedMillis =timeStartedMillis;
		this.timeEndedMillis =timeEndedMillis;
		this.awnsersByQuestion = awnsersByQuestion;
	}
	
	public QuestionList getQuestionList(){return questionList;}
	
	public String getUserId(){return userId;}
	
	public Long getDuration(){return getEnd()-getStart();}
	
	public Long getStart(){return timeStartedMillis;}
	
	public Long getEnd(){return timeEndedMillis;}
	
	public Map<Integer, Awnser> getAwnsers(){
		return awnsersByQuestion;
	}
	
	public Attempt setAwnser(int questionIndex, Set<Option> responses, Long duration){
		awnsersByQuestion.put(questionIndex, new Awnser(duration, responses));
		return this;
	}
	public Attempt addAwnser(int questionIndex, Option response, Long duration){
		Set<Option> responses = awnsersByQuestion.getOrDefault(questionIndex, new Awnser(0L, new HashSet<>())).getResponses();
		responses.add(response);
		awnsersByQuestion.put(questionIndex, new Awnser(duration, responses));
		return this;
	}
	public Attempt removeAwnser(int questionIndex, Option response, Long duration){
		Set<Option> responses = awnsersByQuestion.getOrDefault(questionIndex, new Awnser(0L, new HashSet<>())).getResponses();
		responses.remove(response);
		awnsersByQuestion.put(questionIndex, new Awnser(duration, responses));
		return this;
	}
	
	public Attempt end(){
		timeEndedMillis = System.currentTimeMillis();
		score = getScore();
		return this;
	}
	
	/**
	 * Calculates and returns the exact final score for this user.
	 * @return the final score of this user
	 */
	public Double getScore(){
		//if (this.score!=null && this.getEnd()!=null){return this.score;}
		Double score=0.0;
		int numberOfTrueOptions;
		Awnser  awnser;
		Set<Option> response;
		for (int i =0; i<questionList.size(); ++i){
			awnser = awnsersByQuestion.get(i);
			if (awnser!=null){
				response = awnser.getResponses();
				if (response!=null){
					numberOfTrueOptions = getQuestionList().get(i).trueOptions().size();
					for (Option opt : response) {
						score += (opt.isCorrect()?QuestionList.pointsForCorrect/numberOfTrueOptions:QuestionList.pointsForIncorrect);
					}
				}
			}
		}
		return score;
	}
	
	@Override
	public int hashCode(){
		return getQuestionList().hashCode() + userId.hashCode() + getStart().hashCode() + getEnd().hashCode() + getAwnsers().hashCode();
	}
	
	@Override
	public boolean equals(Object o){
		if (this == o) {return true;}
		if(!(o instanceof  Attempt)) {return false;}
		Attempt a = (Attempt) o;
		return getStart().equals(a.getStart()) && getEnd().equals(a.getEnd())
			&& getUserId().equals(a.getUserId()) && getQuestionList().equals(a.getQuestionList()) 
			&& getAwnsers().equals(a.getAwnsers());
	}

	public String getTextPoints(){
		String time = String.format("`%3d s`",Long.divideUnsigned(getDuration(), 1000));
		if (getDuration()> 99*1000){
			time = String.format("`%3dm`",Long.divideUnsigned(getDuration(), 60000));
		}

		Timestamp startTime = TimeFormat.RELATIVE.atTimestamp(getStart());
		double score= getQuestionList().size()!=0?getScore()*100/getQuestionList().size():0.00;
		return String.format("`%3.0f%%` %s %s", score, time, startTime);
	}

	public String toJson(){
		/*try {
			return Constants.MAPPER.writer(new DefaultPrettyPrinter()).writeValueAsString(this);
		}catch(JsonProcessingException e){System.err.print(Constants.ERROR);e.printStackTrace();}*/
		String res = "{", s;
		Integer key;
		res += String.format("\"%s\":%s,", "userId", getQuestionList().toJson());
		res += String.format("\"%s\":%d,", "timeStartedMillis", getStart());
		res += String.format("\"%s\":%d,", "timeEndedMillis", getEnd());
		res += String.format("\"%s\":%s,", "questionList", getQuestionList().toJson());
		s="{";
		Iterator<Integer> iter =  awnsersByQuestion.keySet().iterator();
		while (iter.hasNext()){
			key = iter.next();
			s += String.format("\"%d\":%s", key, awnsersByQuestion.get(key).toJson());
			if (iter.hasNext()){s+= ", ";}
		}
		s+= "}";
		res += String.format("\"%s\":%s", "awnsersByQuestion", s);
		res += "}";
		return res;
	}
	
	public static Comparator<Attempt> comparatorEnd(){
		return (e, f)-> Long.compare(e.getEnd(), f.getEnd());
	}
	
	public static Comparator<Attempt> comparatorStart(){
		return (e, f)-> Long.compare(e.getStart(), f.getStart());
	}
	
	public static Comparator<Attempt> comparatorScore(){
		return (e, f)-> Double.compare(e.getScore(), f.getScore());
	}
}