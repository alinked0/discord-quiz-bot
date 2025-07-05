package com.linked.quizbot.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;

import com.linked.quizbot.Constants;
import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.commands.CommandOutput;
import com.linked.quizbot.commands.list.EndCommand;
import com.linked.quizbot.commands.list.HelpCommand;
import com.linked.quizbot.commands.list.NextCommand;
import com.linked.quizbot.commands.list.PreviousCommand;
import com.linked.quizbot.utils.Option;
import com.linked.quizbot.utils.Question;
import com.linked.quizbot.utils.QuestionList;
import com.linked.quizbot.utils.Users;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.TimeFormat;
import net.dv8tion.jda.api.utils.Timestamp;

/**
 * The {@code QuizBot} class manages quiz sessions in a Discord channel.
 * It handles quiz progression, question presentation, user interactions,
 * scoring, explanations, and leaderboard calculations.
 * 
 * <h2>Features:</h2>
 * <ul>
 *     <li>Manages a list of quiz questions.</li>
 *     <li>Handles user answers and scoring.</li>
 *     <li>Provides explanations for previous and current questions.</li>
 *     <li>Supports automatic question cycling and leaderboard generation.</li>
 * </ul>
 * 
 * <h2>Usage:</h2>
 * <ul>
 *     <li>Instantiate with a Discord {@link MessageChannel}.</li>
 *     <li>Call {@code start()} to begin a quiz session.</li>
 *     <li>Automatically handles user interactions and progresses through questions.</li>
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
 *     <li>{@code explain(User requester)} - Provides explanations for past questions.</li>
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
public class QuizBot extends ListenerAdapter {
	public final Map<String, Double> userScoreApproxi = new HashMap<>();
    public final Map<String, Set<Option>> userAnswersForCurrQuestion = new HashMap<>();
    public final Map<Question, Map<String, Set<Option>>> awnsersByUserByQuestion = new HashMap<>();
    public final Set<String> players = new HashSet<>();
	public Map<String, Double> userScoreExact = null;
    public QuestionList questions;
    public boolean active;
    public boolean explainWasTrigerred = false;
    public int currIndex;
    public int lastIndex;
    public Message message = null;
	public double pointsForCorrect = 1.00;
	public double pointsForIncorrect = -0.25;
    public int delaySec = 0;
    public String channelId;
	public Timestamp timeLimit;
	public static Random random = BotCore.getRandom();

    public QuizBot(String channelId){
        this.channelId = channelId;
        active = false;
        this.questions = new QuestionList.Builder().build();
    }
    public QuizBot(String channelId, QuestionList c) {
        this(channelId);
        this.questions = c;
    }
    public void setQuestionList(QuestionList c) {
        questions = c;
    }
    public void setMessage(Message m) {
        message = m;
    }
    public Message getMessage() { return message;}
    public String getMessageId() { return message!=null?message.getId():null;}
    public void setChannel(String channelId) { this.channelId = channelId;}
    public String getChannelId(){ return channelId;}
	public Timestamp getLastTimestamp(){
		return timeLimit;
	}
    public Boolean isActive() { return active;}
    public Boolean explainWasTrigerred() { return explainWasTrigerred;}
    public void setExplainTriger(boolean b) { explainWasTrigerred=b;}

	public CommandOutput start() {
		BotCore.explicationRequestByChannel.put(getChannelId(), new HashSet<>());
		active = true;
		lastIndex = -1;
		currIndex = 0;
		userScoreApproxi.clear();
		userAnswersForCurrQuestion.clear();
        awnsersByUserByQuestion.clear();
		return sendNextQuestion();
	}

	public int getDelaySec(){ return this.delaySec;}
	
	public void setDelay(int sec) { this.delaySec = sec;}

    public Question getCurrQuestion() {
        if (!isActive() || getCurrentIndex()>=questions.size()) { return null;}
        return questions.get(getCurrentIndex());
    }

	public QuestionList getQuestionList(){
        return questions;
    }
	public Set<String> getPlayers(){
		return players;
	}
	public void addPlayer(String player){
		players.add(player);
	}
	
    public int getCurrentIndex() { return currIndex;}

    private CommandOutput sendNextQuestion() {
		CommandOutput.Builder outputBuilder = new CommandOutput.Builder();
        if (!isActive()) { return outputBuilder.build();}
        if (getCurrentIndex() >= questions.size()) {
			if (message != null) {
				BotCore.explicationRequestByChannel.get(getChannelId()).remove(message.getId());
			}
			List<String> args = List.of();
            return BotCommand.getCommandByName(EndCommand.CMDNAME).execute(null, channelId, args, false);
        }
		
        Question currentQuestion = getCurrQuestion();
        currentQuestion.rearrageOptions(random);
        questions.set(getCurrentIndex(), currentQuestion);
		
        if (!awnsersByUserByQuestion.containsKey(currentQuestion)) {
			awnsersByUserByQuestion.put(currentQuestion, new HashMap<>());
        }
		return outputBuilder
			.addTextMessage(formatQuestion(currIndex))
			.addPostSendAction(message2 -> {
				BotCore.getQuizBot(channelId).setMessage(message2);
				addReactions(message, getButtons().iterator());
				if (message != null) {
					BotCore.explicationRequestByChannel.get(getChannelId()).add(message.getId());
				}
			}).build();
    }
	public List<Emoji> getButtonsForOptions(){
		List<Emoji> emojis = new ArrayList<>();
		for (int i = 0; i < getCurrQuestion().size(); i++) {
			emojis.add(getReactionForAnswer(i + 1));
		}
		return emojis;
	}
	public List<Emoji> getButtons(){
		List<Emoji> emojis = new ArrayList<>();
		emojis.addAll(getButtonsForOptions());
		if (delaySec == 0|| awnsersByUserByQuestion.get(getCurrQuestion()).isEmpty()){
			emojis.addAll(Arrays.asList(
				Constants.EMOJIWHITESQUARE,
				Constants.EMOJIPREVQUESTION,
				Constants.EMOJINEXTQUESTION,
				Constants.EMOJIEXPLICATION
			));
		}
		// TODO : find a use of the moreTime button, its currently inaccessible
		else{
			emojis.addAll(Arrays.asList(
				Constants.EMOJIWHITESQUARE,
				Constants.EMOJIPREVQUESTION,
				Constants.EMOJIMORETIME,
				Constants.EMOJINEXTQUESTION,
				Constants.EMOJIEXPLICATION
			));
		}
		return emojis;
	}
	private static void addReactions(Message message, Iterator<Emoji> iter) {
		if(iter.hasNext()){
			message.addReaction(iter.next()).queue( msg -> addReactions(message, iter));
        }
    }
    public CommandOutput next() {
		lastIndex =currIndex;
		currIndex++;
		if (message != null) {
			BotCore.explicationRequestByChannel.get(getChannelId()).remove(message.getId());
		}
		userAnswersForCurrQuestion.clear();
        return sendNextQuestion();
    }
	
    public CommandOutput previous(){
		if (getCurrentIndex() < 1) {
            return BotCommand.getCommandByName(HelpCommand.CMDNAME).execute(null, channelId, List.of(PreviousCommand.CMDNAME), false);
        }
		if (message != null) {
			BotCore.explicationRequestByChannel.get(getChannelId()).remove(message.getId());
		}
		lastIndex =currIndex;
        currIndex -=1;
		userAnswersForCurrQuestion.clear();
        return sendNextQuestion();
    }
	
    public CommandOutput current(){
		if (getCurrentIndex() < 0 || getCurrentIndex() >= questions.size()) {
            throw new NoSuchElementException();
        }
		lastIndex =currIndex;
		if (message != null) {
			BotCore.explicationRequestByChannel.get(getChannelId()).remove(message.getId());
		}

		CommandOutput.Builder outputBuilder = new CommandOutput.Builder();
        if (!isActive()) { return outputBuilder.build();}
        if (getCurrentIndex() >= questions.size()) {
			List<String> args = List.of();
            return BotCommand.getCommandByName(EndCommand.CMDNAME).execute(null, channelId, args, false);
        }
		
        Question currentQuestion = getCurrQuestion();
		
        if (!awnsersByUserByQuestion.containsKey(currentQuestion)) {
			awnsersByUserByQuestion.put(currentQuestion, new HashMap<>());
        }
		return outputBuilder
			.addTextMessage(formatQuestion(currIndex))
			.addPostSendAction(message2 -> {
				BotCore.getQuizBot(channelId).setMessage(message2);
				addReactions(message, getButtons().iterator());
				BotCore.explicationRequestByChannel.get(getChannelId()).remove(message.getId());
			}).build();
    }
    private String formatQuestion (int index) {
		if (delaySec>0&& awnsersByUserByQuestion.get(getCurrQuestion()).size()>=1){
			this.timeLimit = TimeFormat.RELATIVE.after(delaySec*1000);
		} else if (delaySec==0){
			this.timeLimit = TimeFormat.RELATIVE.after(delaySec*1000);
		}
        return questions.getFormated(index)+getLastTimestamp()+"\n";
    }
    public void end() {
		active = false;
		getExactUserScore();
    }
	private Map<String, Double> getExactUserScore(){
		userScoreExact = new HashMap<>();
		double point;
		double score;
		Iterator<Map.Entry<Question, Map<String, Set<Option>>>> iter_AwnsersByUserByQuestion = awnsersByUserByQuestion.entrySet().iterator();
		while (iter_AwnsersByUserByQuestion.hasNext()) {
			Map.Entry<Question, Map<String, Set<Option>>> entry_AwnsersByUserByQuestion = iter_AwnsersByUserByQuestion.next();
			Question q = entry_AwnsersByUserByQuestion.getKey();
			int numberOfTrueOptions = q.getTrueOptions().size();
			Iterator<Map.Entry<String, Set<Option>>> iter_AwnsersByUser = entry_AwnsersByUserByQuestion.getValue().entrySet().iterator();
			while (iter_AwnsersByUser.hasNext()){
				Map.Entry<String, Set<Option>> awnsersByUserId = iter_AwnsersByUser.next();
				String u = awnsersByUserId.getKey();
				score = userScoreExact.getOrDefault( u, 0.00);
				for (Option opt : awnsersByUserId.getValue()) {
					if (!Constants.isBugFree()) System.out.printf("   $> lb %s, %s\n", opt.isCorrect(), opt.getText());
					point = (opt.isCorrect()?pointsForCorrect/numberOfTrueOptions:pointsForIncorrect);
					score += point;
				}
				userScoreExact.put(u, score);
			}
		}
		return userScoreExact;
	}
	private Set<Option> getUserSelOptions(String requester, Question q) {
		Map<String, Set<Option>> e = awnsersByUserByQuestion.getOrDefault(q, null);
		if(e != null) {
			Set<Option> opts = e.get(requester);
			return opts;
		}
		return null;
	}
	public List<String> leaderBoard() {
		List<String> res = new ArrayList<>();
		double totalPoints = questions.size() * pointsForCorrect;
		String leaderboard = "Leaderboard:\n";

		Iterator<Map.Entry<String, Double>> SortedScoreByUser = userScoreExact.entrySet().stream()
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
	public List<String> explain(String userId) {
		List<Question> main = new ArrayList<>();
		String text = "";
		List<String> res = new ArrayList<>();
		String explication;
		int index;

		if (isActive()) {
			main.add(getCurrQuestion());
		} else {
			main.addAll(questions);
		}

		Option opt;
		Set<Option> optsUser;
		String optsString;
		Double points;

		text += String.format("## %s `%s/%d`\n", questions.getName(), getUserScore(userId), questions.size());
		text += String.format("For %s\n", BotCore.getEffectiveNameFromId(userId));
		Question q;
		Iterator<Question> iterQuestion = main.iterator();
		while (iterQuestion.hasNext()) {
			q = iterQuestion.next();
			points = 0.00;
			optsUser = getUserSelOptions(userId, q);
			int numberOfTrueOptions = q.getTrueOptions().size();
			optsString = "";
			index = questions.indexOf(q) + 1;

			for (int i = 0; i < q.size(); i++) {
				opt = q.get(i);
				explication = opt.getExplicationFriendly();
				optsString += String.format("> %d. %s\n", i + 1, opt.getText());

				if (optsUser != null && optsUser.contains(opt)) {
					points += opt.isCorrect() ? pointsForCorrect / numberOfTrueOptions : pointsForIncorrect;
					optsString += String.format("> %s%s\n",
						(opt.isCorrect() ? Constants.EMOJITRUE : Constants.EMOJIFALSE).getFormatted(),
						explication);
				} else {
					optsString += String.format("> %s%s\n",
						(opt.isCorrect() ? Constants.EMOJICORRECT : Constants.EMOJIINCORRECT).getFormatted(),
						explication);
				}
			}

			text += String.format("### %d. %s `%s/1`\n%s", index, q.getQuestion(), points, optsString);
			text += String.format("> \n> **%s**\n%s", q.getExplicationFriendly(), (iterQuestion.hasNext()?"\n":""));

			if (text.length() > (Constants.CHARSENDLIM - 500)) {
				res.add(text);
				text = "";
			}
		}
		if (!text.isEmpty()) res.add(text);
		return res;
	}


    public Emoji getReactionForAnswer(int index) {
        return Emoji.fromUnicode("U+3"+index+"U+fe0fU+20e3");
    }
    
    public Double getUserScore(String userId) {
		double point, score = 0.00;
		List<Question> main;
		if(isActive()){
			main = getQuestionList().subList(0, getCurrentIndex()+1);
		} else {
			main = new ArrayList<>(getQuestionList());
		}
        for (Question q : main){
			int numberOfTrueOptions = q.getTrueOptions().size();
			Map<String, Set<Option>> awnsersByUserId = awnsersByUserByQuestion.get(q);
			if(awnsersByUserId!=null) {
				Set<Option> awnsers = awnsersByUserId.get(userId);
				if(awnsers != null) {
					for (Option opt : awnsers) {
						if (!Constants.isBugFree()) System.out.printf("   $> lb %s, %s\n", opt.isCorrect(), opt.getText());
						point = (opt.isCorrect()?pointsForCorrect/numberOfTrueOptions:pointsForIncorrect);
						score += point;
					}
				}
			}
        }
        return score;
    }

	public void updateUserScoreAddReaction(String userId, Emoji reaction) {
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
			if (reaction.equals(this.getReactionForAnswer(i))) {
				userAwnser = currQuestion.get(i-1);
				if (userAwnser != null) {
					//System.out.printf("  $> awnser %s, %s ;\n", userAwnser.isCorrect(), userAwnser.getText());
					this.userAnswersForCurrQuestion.get(userId).add(userAwnser);
				}
				break;
			}
		}
		Map<String, Set<Option>> tmpUserAnswers = new HashMap<>(this.userAnswersForCurrQuestion);
		this.awnsersByUserByQuestion.put(currQuestion, tmpUserAnswers);
		// If it's the correct answer, increase their score
		if (userAwnser != null) {
			double point = (userAwnser.isCorrect()?this.pointsForCorrect:this.pointsForIncorrect)/currQuestion.getTrueOptions().size();
			this.userScoreApproxi.put(userId, this.getUserScore(userId) +point);
		}
	}
}
