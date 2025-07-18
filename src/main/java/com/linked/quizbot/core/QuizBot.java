package com.linked.quizbot.core;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import com.linked.quizbot.Constants;
import com.linked.quizbot.utils.Option;
import com.linked.quizbot.utils.Question;
import com.linked.quizbot.utils.QuestionList;
import com.linked.quizbot.utils.Users;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.utils.TimeFormat;
import net.dv8tion.jda.api.utils.Timestamp;

/**
 * The {@code QuizBot} class manages quiz sessions in a Discord channel.
 * It handles quiz progression, question presentation, user interactions,
 * scoring, explanations, and leaderboard calculations.
 * 
 * <h2>Features:</h2>
 * <ul>
 *     <li>Manages a list of quiz getQuestionList().</li>
 *     <li>Handles user answers and scoring.</li>
 *     <li>Provides explanations for previous and current getQuestionList().</li>
 *     <li>Supports automatic question cycling and leaderboard generation.</li>
 * </ul>
 * 
 * <h2>Usage:</h2>
 * <ul>
 *     <li>Instantiate with a Discord {@link MessageChannel}.</li>
 *     <li>Call {@code start()} to begin a quiz session.</li>
 *     <li>Automatically handles user interactions and progresses through getQuestionList().</li>
 * </ul>
 * 
 * <h2>Example:</h2>
 * <pre>
 * MessageChannel channel = event.getChannel();
 * List<Question> questions = getQuestions();
 * QuizBot quiz = new QuizBot(channel, questions);
 * quiz.start();
 * </pre>
 * 
 * <h2>Command Behavior:</h2>
 * <ul>
 *     <li>{@code start()} - Starts the quiz and sends the first question.</li>
 *     <li>{@code sendNextQuestion()} - Sends the next question and sets up reactions.</li>
 *     <li>{@code explain(User requester)} - Provides explanations for past getQuestionList().</li>
 *     <li>{@code leaderBoard()} - Generates and sends the leaderboard after the quiz.</li>
 *     <li>{@code end()} - Ends the quiz and shows results.</li>
 * </ul>
 * 
 * <h2>Scoring System:</h2>
 * <ul>
 *     <li>Correct answer: {@code pointsForCorrect} (default: 1.00)</li>
 *     <li>Incorrect answer: {@code pointsForIncorrect} (default: -0.25)</li>
 * </ul>
 * 
 * @author alinked0
 * @version 1.0
 * @see Question
 * @see Option
 * @see BotCore
 * @see Users
 */
public class QuizBot extends Viewer {
	public final Map<String, Double> userScoreApproxi = new HashMap<>();
    public final Map<String, Set<Option>> userAnswersForCurrQuestion = new HashMap<>();
    public final List<Map<String, Set<Option>>> awnsersByUserIdByQuestionIndex = new ArrayList<>();
    public final Set<String> players = new HashSet<>();
	public Map<String, Double> userScoreExact = null;
    public boolean isExplaining = false;
    public boolean autoNext = true;
    public final int delaySec = 5;
	public Timestamp timeLimit;

    public QuizBot(QuestionList c) {
		this(c, true, false);
    }
	public QuizBot(QuestionList c, boolean useButtons, boolean replyToSender) {
		super(c, useButtons, replyToSender);
    }
	public Timestamp getLastTimestamp(){return timeLimit;}
    public Boolean isExplaining() { return isExplaining;}
    public void isExplaining(boolean b) { isExplaining=b;}
	public int getDelaySec(){ return this.delaySec;}
	public boolean getAutoNext(){ return this.autoNext;}
	public Set<String> getPlayers(){return players;}
	public void addPlayer(String player){players.add(player);}
	public void autoNext(boolean b){ autoNext=b;}
    public List<Emoji> getReactionsForOptions(){
        List<Emoji> emojis = new ArrayList<>();
        for (int i = 0; i < getCurrQuestion().size(); i++) {
            emojis.add(getReactionForAnswer(i + 1));
        }
        return emojis;
    }
    public void addReaction(String userId, Emoji emoji){
		Question currQuestion = this.getCurrQuestion();
		if (!this.userAnswersForCurrQuestion.containsKey(userId)) {
			if (!this.getPlayers().contains(userId)){
				Users.getUser(userId).incrNumberOfGamesPlayed();
				this.addPlayer(userId);
			}
			this.userAnswersForCurrQuestion.put(userId, new HashSet<>());
		}
		Option userAwnser = null;
		// Record user's answer (reaction)
		for (int i = 1; i<=currQuestion.size(); i++) {
			if (emoji.equals(getReactionForAnswer(i))) {
				userAwnser = currQuestion.get(i-1);
				if (userAwnser != null) {
					//System.out.printf("  $> awnser %s, %s ;\n", userAwnser.isCorrect(), userAwnser.getText());
					this.userAnswersForCurrQuestion.get(userId).add(userAwnser);
				}
				break;
			}
		}
		Map<String, Set<Option>> tmpUserAnswers = new HashMap<>(this.userAnswersForCurrQuestion);
		this.awnsersByUserIdByQuestionIndex.set(getCurrentIndex(), tmpUserAnswers);
		// If it's the correct answer, increase their score
		if (userAwnser != null) {
			double point = (userAwnser.isCorrect()?QuestionList.pointsForCorrect:QuestionList.pointsForIncorrect)/currQuestion.getTrueOptions().size();
			this.userScoreApproxi.put(userId, this.getUserScore(userId) +point);
		}
	};
    public void removeReaction(String userId, Emoji emoji){};
    @Override
    public void inBetweenProccessorStart(){
		userScoreApproxi.clear();
		userAnswersForCurrQuestion.clear();
        awnsersByUserIdByQuestionIndex.clear();
		int n = getQuestionList().size();
		this.timeLimit = TimeFormat.RELATIVE.now();
		isExplaining(false);
		for (int i=0; i<n; ++i) 
			awnsersByUserIdByQuestionIndex.add(new HashMap<>());
	}
	@Override
	public void inBetweenProccessorCurrent(){
		if (getMessage() != null) {
			BotCore.explicationRequest.remove(getMessageId());
		}
		userAnswersForCurrQuestion.clear();
		isExplaining(false);
		this.timeLimit = TimeFormat.RELATIVE.now();
	}
    @Override
    public Consumer<Message> postSendActionCurrent(){
        return msg ->{
                BotCore.viewerByMessageId.put(msg.getId(), this);
                BotCore.explicationRequest.add(getMessageId());
            };
    }
    @Override
	public List<Emoji> getReactions(){
		List<Emoji> emojis = new ArrayList<>();
		if (-1<getCurrentIndex()) emojis.addAll(getReactionsForOptions());
		emojis.addAll(super.getReactions());
		if (-1<getCurrentIndex()) emojis.add(Constants.EMOJIEXPLICATION);
		return emojis;
	}
    @Override
    public String getFormatedQuestion () {
		if (getAutoNext() && awnsersByUserIdByQuestionIndex.get(getCurrentIndex()).size()>1){
			this.timeLimit = TimeFormat.RELATIVE.after(delaySec*1000);
		}
        return getQuestionList().getFormated(getCurrentIndex())+getLastTimestamp()+"\n";
    }
    @Override
    public void end() {
		super.end();
		getExactUserScore();
		for (String user : getPlayers()){
			Users.getUser(user).incrTotalPointsEverGained(userScoreExact.get(user));
		}
    }
	private Map<String, Double> getExactUserScore(){
		userScoreExact = new HashMap<>();
		double point;
		double score;
		for (int i =0; i<awnsersByUserIdByQuestionIndex.size(); ++i){
			Map<String, Set<Option>> entry_AwnsersByUserByQuestion = awnsersByUserIdByQuestionIndex.get(i);
			int numberOfTrueOptions = getQuestionList().get(i).getTrueOptions().size();
			Iterator<Map.Entry<String, Set<Option>>> iter_AwnsersByUser = entry_AwnsersByUserByQuestion.entrySet().iterator();
			while (iter_AwnsersByUser.hasNext()){
				Map.Entry<String, Set<Option>> awnsersByUserId = iter_AwnsersByUser.next();
				String u = awnsersByUserId.getKey();
				score = userScoreExact.getOrDefault( u, 0.00);
				for (Option opt : awnsersByUserId.getValue()) {
					if (!Constants.isBugFree()) System.out.printf("   $> lb %s, %s\n", opt.isCorrect(), opt.getText());
					point = (opt.isCorrect()?QuestionList.pointsForCorrect/numberOfTrueOptions:QuestionList.pointsForIncorrect);
					score += point;
				}
				userScoreExact.put(u, score);
			}
		}
		return userScoreExact;
	}
	private Set<Option> getUserSelOptions(String requester, int index) {
		if (awnsersByUserIdByQuestionIndex.size()>index){
			Map<String, Set<Option>> e = awnsersByUserIdByQuestionIndex.get(index);
			if(e != null) {
				Set<Option> opts = e.get(requester);
				return opts;
			}
		}
		return null;
	}
	public List<String> leaderBoard() {
		List<String> res = new ArrayList<>();
		double totalPoints = getQuestionList().size() * QuestionList.pointsForCorrect;
		String leaderboard = "Leaderboard:\n";

		Iterator<Map.Entry<String, Double>> SortedScoreByUser = this.userScoreExact.entrySet().stream()
			.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).iterator();
		int i = 1;
		while (SortedScoreByUser.hasNext()) {
			Map.Entry<String, Double> entry = SortedScoreByUser.next();
			leaderboard += String.format("#%d. %s: `%s`\n", i, BotCore.getEffectiveNameFromId(entry.getKey()), entry.getValue());
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
    public static Emoji getReactionForAnswer(int index) {
        return Emoji.fromUnicode("U+3"+index+"U+fe0fU+20e3");
    }
	public List<Set<Option>> getAwsersByQuestion(String userId){
		List<Set<Option>> awsers= new ArrayList<>();
		for (int i=0; i<=getCurrentIndex(); ++i){
			awsers.add(getUserSelOptions(userId, i));
		}
		return awsers;
	}
    public Double getUserScore(String userId) {
		double point, score = 0.00;
        for (int i = 0; i<=getCurrentIndex(); ++i){
			int numberOfTrueOptions = getQuestionList().get(i).getTrueOptions().size();
			Map<String, Set<Option>> awnsersByUserId = awnsersByUserIdByQuestionIndex.get(i);
			if(awnsersByUserId!=null) {
				Set<Option> awnsers = awnsersByUserId.get(userId);
				if(awnsers != null) {
					for (Option opt : awnsers) {
						point = (opt.isCorrect()?QuestionList.pointsForCorrect/numberOfTrueOptions:QuestionList.pointsForIncorrect);
						score += point;
					}
				}
			}
        }
        return score;
    }
}
