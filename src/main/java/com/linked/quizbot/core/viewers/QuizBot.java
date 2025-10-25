package com.linked.quizbot.core.viewers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.linked.quizbot.Constants;
import com.linked.quizbot.core.BotCore;
import com.linked.quizbot.utils.Attempt;
import com.linked.quizbot.utils.Awnser;
import com.linked.quizbot.utils.Option;
import com.linked.quizbot.utils.Question;
import com.linked.quizbot.utils.QuestionList;
import com.linked.quizbot.utils.Users;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.utils.TimeFormat;
import net.dv8tion.jda.api.utils.Timestamp;
import net.dv8tion.jda.internal.utils.tuple.ImmutablePair;
/**
 * An extension of {@link Viewer} that manages a live, interactive quiz session.
 * <p>
 * This class builds upon the base {@link Viewer} functionality by adding game-specific features,
 * such as tracking multiple players, recording their answers and scores, and managing a
 * leaderboard. It handles the state for a single quiz, including the current question,
 * player responses, and an optional time limit. It also integrates with the bot's core
 * functionality to handle Discord-specific interactions like reactions for answering
 * questions and auto-advancing the quiz.
 * <p>
 *
 * @author alinked0
 * @version 1.0
 * @since 2025-02-01
 * @see Question
 * @see Option
 * @see BotCore
 * @see Users
 */
public class QuizBot extends Viewer {
	private final Map<String, Set<Option>> userAnswersForCurrQuestion = new HashMap<>();
	private final Map<String, Attempt> attemptByPlayer = new HashMap<>();
	private Map<String, Double> userScoreExact = null;
	private boolean isExplaining = false;
	private final boolean autoNext;
	public final int delaySec = 5;
	private Timestamp timeLimit;
	private Long lastServedMillis=0L;
	
	/**
	 * Constructs a QuizBot instance with a question list, defaulting to buttons and no auto-next.
	 * @param c The question list for the quiz.
	 */
	public QuizBot(QuestionList c) {
		this(c, true, false);
	}
	
	/**
	 * Constructs a QuizBot instance with a question list and specified settings.
	 * @param c The question list for the quiz.
	 * @param useButtons If true, uses buttons for navigation; otherwise, uses reactions.
	 * @param autoNext If true, the quiz automatically advances after a delay.
	 */
	public QuizBot(QuestionList c, boolean useButtons, boolean autoNext) {
		super(c, useButtons);
		this.autoNext = autoNext;
	}
	
	/**
	 * Retrieves the timestamp for the current question's time limit.
	 * @return The time limit timestamp.
	 */
	public Timestamp getLastTimestamp(){return timeLimit;}
	
	/**
	 * Checks if the bot is in explanation mode.
	 * @return true if an explanation is being shown, false otherwise.
	 */
	public Boolean isExplaining() { return isExplaining;}
	
	/**
	 * Sets the explanation mode.
	 * @param b true to set to explanation mode, false to exit.
	 */
	public void isExplaining(boolean b) { isExplaining=b;}
	
	/**
	 * Retrieves the delay in seconds for auto-next.
	 * @return The auto-next delay.
	 */
	public int getDelaySec(){ return this.delaySec;}
	
	/**
	 * Checks if auto-next is enabled.
	 * @return true if auto-next is enabled, false otherwise.
	 */
	public boolean useAutoNext(){ return this.autoNext;}
	
	/**
	 * Gets the set of all players in the current quiz.
	 * @return A set of player IDs.
	 */
	public Set<String> getPlayers(){return attemptByPlayer.keySet();}
	
	/**
	 * Adds a new player to the quiz session.
	 * @param player The user ID of the new player.
	 */
	public void addPlayer(String player){
		attemptByPlayer.put(player, new Attempt(player, getQuestionList()));
	}
	
	/**
	 * Generates a list of emojis for each option of the current question.
	 * @return A list of option emojis.
	 */
	public List<Emoji> getReactionsForOptions(){
		List<Emoji> emojis = new ArrayList<>();
		for (int i = 0; i < getCurrQuestion().size(); i++) {
			emojis.add(getReactionForAnswer(i + 1));
		}
		return emojis;
	}
	
	@Override
	public void addReaction(String userId, Emoji emoji){
		Question currQuestion = this.getCurrQuestion();
		if (!this.getPlayers().contains(userId)){
			this.addPlayer(userId);
			this.userAnswersForCurrQuestion.put(userId, new HashSet<>());
		}
		Option userAwnser = null;
		// Record user's answer (reaction)
		for (int i = 1; i<=currQuestion.size(); i++) {
			if (emoji.equals(getReactionForAnswer(i))) {
				userAwnser = currQuestion.get(i-1);
				if (userAwnser != null) {
					this.userAnswersForCurrQuestion.get(userId).add(userAwnser);
				}
				return;
			}
		}
		if (!BotCore.isBugFree()){
			try {
				System.out.println(Constants.MAPPER.writeValueAsString(userAnswersForCurrQuestion));
			}catch(JsonProcessingException e){e.printStackTrace();}
		}
	}
	
	@Override
	public void removeReaction(String userId, Emoji emoji){/*TODO impl a response removal */};
	
	@Override
	public void inBetweenProccessorStart(){
		this.userAnswersForCurrQuestion.clear();
		this.timeLimit = TimeFormat.RELATIVE.now();
		isExplaining(false);
	}
	
	@Override
	public void inBetweenProccessorCurrent(){
		if (getMessage() != null) {
			BotCore.explicationRequest.remove(getMessageId());
		}
		if (getCurrentIndex()>=0){
			for (String u : getPlayers()){
				attemptByPlayer.get(u).addAwnser(getCurrentIndex(), userAnswersForCurrQuestion.getOrDefault(u, new HashSet<>()), System.currentTimeMillis()-lastServedMillis);
			}
		}
		this.userAnswersForCurrQuestion.clear();
		for (String userId : getPlayers()){
			this.userAnswersForCurrQuestion.put(userId, new HashSet<>());
		}
		isExplaining(false);
		this.timeLimit = TimeFormat.RELATIVE.now();
	}
	@Override
	public Consumer<Message> postSendActionCurrent(){
		return msg ->{
			String oldId = msg.getId();
			BotCore.viewerByMessageId.put(msg.getId(), this);
			BotCore.explicationRequest.remove(oldId);
			BotCore.explicationRequest.add(getMessageId());
		};
	}
	@Override
	public List<Emoji> getReactions(){
		List<Emoji> emojis = new ArrayList<>();
		if (-1<getCurrentIndex()) emojis.addAll(getReactionsForOptions());
		emojis.addAll(super.getReactions());
		if (-1<getCurrentIndex()) emojis.add(Emoji.fromFormatted(Constants.EMOJIEXPLICATION));
		return emojis;
	}
	@Override
	public String getHeader(){
		String res = super.getHeader();
		res += "---\n";
		res += String.format("**AutoNext:** `%s`\n", useAutoNext());
		return res;
	}
	
	@Override
	public String getFormatedQuestion() {
		/*if (useAutoNext() && awnsersByUserIdByQuestionIndex.get(getCurrentIndex()).size()>1){
			this.timeLimit = TimeFormat.RELATIVE.after(delaySec*1000);
		}*/
		return String.format("%s\n%s\n", getQuestionList().getFormated(getCurrentIndex(), false, userAnswersForCurrQuestion,  getPlayers()), getLastTimestamp());
	}
	
	@Override
	public void end() {
		super.end();
		getExactUserScore();
		for (String u : getPlayers()){
			Users.getUser(u).addAttempt(getQuestionList().getId(), attemptByPlayer.get(u).end());
		}
	}
	
	/**
	 * Calculates and returns the exact final score for all players.
	 * @return A map of user IDs to their final scores.
	 */
	private Map<String, Double> getExactUserScore(){
		userScoreExact = new HashMap<>();
		for (String u : getPlayers()){
				userScoreExact.put(u, attemptByPlayer.get(u).getScore());
		}
		return userScoreExact;
	}
	
	/**
	 * Generates a formatted leaderboard string for the quiz.
	 * @return A list of strings representing the leaderboard, possibly split into multiple messages.
	 */
	public List<String> leaderBoard() {
		List<String> res = new ArrayList<>();
		double totalPoints = getQuestionList().size() * QuestionList.pointsForCorrect;
		String leaderboard = "Leaderboard:\n";
		String uName;
		
		Iterator<Map.Entry<String, Double>> SortedScoreByUser = this.userScoreExact.entrySet().stream()
			.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).iterator();
		int i = 1;
		while (SortedScoreByUser.hasNext()) {
			Map.Entry<String, Double> entry = SortedScoreByUser.next();
			uName = BotCore.getEffectiveNameFromId(entry.getKey());
			if (uName == entry.getKey()){
				uName = String.format("<@%s>", uName);
			}
			leaderboard += String.format("#%d. %s: `%s`\n", i, uName, entry.getValue());
			i++;
			if (leaderboard.length() > (Constants.CHARSENDLIM - 1000)) {
				res.add(leaderboard);
				leaderboard = "";
			}
		}
		leaderboard += String.format("Max points :`%s`\n", totalPoints);
		res.add(leaderboard);
		return res;
	}
	
	/**
	 * Returns the emoji corresponding to a given answer index.
	 * @param index The 1-based index of the answer option.
	 * @return The Unicode emoji.
	 */
	public static Emoji getReactionForAnswer(int index) {
		return Emoji.fromUnicode("U+3"+index+"U+fe0fU+20e3");
	}
	
	/**
	 * Retrieves all answers submitted by a user up to the current question.
	 * @param userId The ID of the user.
	 * @return A list of sets, where each set contains the options selected for a question.
	 */
	public List<Set<Option>> getAwsersByQuestion(String userId){
		if (!getPlayers().contains(userId)) return List.of();
		Map<Integer, Awnser> awsers = attemptByPlayer.get(userId).getAwnsers();
		Awnser tmp;
		List<Set<Option>> res = new ArrayList<>();
		for (int i=0; i<=getCurrentIndex() &&i<getQuestionList().size(); ++i){
			tmp = awsers.get(i);
			if (tmp!=null)res.add(tmp.getResponse());
			else res.add(new HashSet<>());
		}
		if (getCurrentIndex()<getQuestionList().size() && 0<=getCurrentIndex()){
			for (String u : userAnswersForCurrQuestion.keySet()){
				res.set(getCurrentIndex(), userAnswersForCurrQuestion.getOrDefault(u, new HashSet<>()));
			}
		}
		return res;
	}
	
	/**
	 * Gets a user's current approximate score.
	 * @param userId The ID of the user.
	 * @return The user's score.
	 */
	public Double getUserScore(String userId) {
		if (!getPlayers().contains(userId)) return 0.00;
		return attemptByPlayer.get(userId).getScore();
	}
}
