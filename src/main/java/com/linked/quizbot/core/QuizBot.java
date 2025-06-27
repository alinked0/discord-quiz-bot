
package com.linked.quizbot.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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
import net.dv8tion.jda.api.entities.User;
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
 * List<Question> questions = getQuizQuestions();
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
    public QuestionList quizQuestions;
    public boolean quizActive;
    public int currentQuestionIndex;
    public int previousQuestionIndex;
    public Message quizMessage = null;
    public String quizMessageId = null;
	public double pointsForCorrect = 1.00;
	public double pointsForIncorrect = -0.25;
    public int delaySec = 0;
    public String channelId;
	public Timestamp timeLimit;
	public static Random random = BotCore.getRandom();
    public QuizBot(String channelId){
        this.channelId = channelId;
        quizActive = false;
        this.quizQuestions = new QuestionList.Builder().build();
    }
    public QuizBot(String channelId, QuestionList c) {
        this(channelId);
        this.quizQuestions = c;
    }
    public void setQuestionList(QuestionList c) {
        quizQuestions = c;
    }
    public void setQuizMessage(Message m) {
        quizMessage = m;
		quizMessageId = quizMessage.getId();
    }
    public Message getQuizMessage() { return quizMessage;}
    public String getQuizMessageId() { return quizMessageId;}
    public void setChannel(String channelId) { this.channelId = channelId;}
    public String getChannelId(){ return channelId;}
	public Timestamp getLastTimestamp(){
		return timeLimit;
	}
    public Boolean isActive() { return quizActive;}

	public CommandOutput start() {
		BotCore.explicationRequestByChannel.put(getChannelId(), new HashSet<>());
		quizActive = true;
		previousQuestionIndex = -1;
		currentQuestionIndex = 0;
		userScoreApproxi.clear();
		userAnswersForCurrQuestion.clear();
        awnsersByUserByQuestion.clear();
		return sendNextQuestion();
	}

	public int getDelaySec(){ return this.delaySec;}
	
	public void setDelay(int sec) { this.delaySec = sec;}

    public Question getCurrQuestion() {
        if (!isActive() || getCurrentQuestionIndex()>=quizQuestions.size()) { return null;}
        return quizQuestions.get(getCurrentQuestionIndex());
    }

    public Question getPrevQuestion() {
        if (!isActive()) { return null;}
        return quizQuestions.get(getCurrentQuestionIndex()-1);
    }

	public QuestionList getQuizQuestionList(){
        return quizQuestions;
    }
	public Set<String> getPlayers(){
		return players;
	}
	public void addPlayer(String player){
		players.add(player);
	}
	
    public int getCurrentQuestionIndex() { return currentQuestionIndex;}

    private CommandOutput sendNextQuestion() {
		CommandOutput.Builder outputBuilder = new CommandOutput.Builder();
        if (!isActive()) { return outputBuilder.build();}
        if (getCurrentQuestionIndex() >= quizQuestions.size()) {
			BotCore.explicationRequestByChannel.get(getChannelId()).remove(quizMessage.getId());
			List<String> args = List.of();
            return BotCommand.getCommandByName(EndCommand.CMDNAME).execute(null, channelId, args, false);
        }
		
        Question currentQuestion = getCurrQuestion();
        currentQuestion.rearrageOptions(random);
        quizQuestions.set(getCurrentQuestionIndex(), currentQuestion);
		
        if (!awnsersByUserByQuestion.containsKey(currentQuestion)) {
			awnsersByUserByQuestion.put(currentQuestion, new HashMap<>());
        }
		return outputBuilder
			.addTextMessage(formatQuestion(currentQuestion))
			.addPostSendAction(message -> {
				BotCore.getQuizBot(channelId).setQuizMessage(message);
				addReactions(quizMessage, getButtons().iterator());
				BotCore.explicationRequestByChannel.get(getChannelId()).add(quizMessage.getId());
			}).build();
    }
	public List<Emoji> getButtons(){
		List<Emoji> emojis = new ArrayList<>();
		for (int i = 0; i < getCurrQuestion().size(); i++) {
			emojis.add(getReactionForAnswer(i + 1));
		}
		if (delaySec == 0|| awnsersByUserByQuestion.get(getCurrQuestion()).isEmpty()){
			emojis.addAll(Arrays.asList(
				Constants.EMOJIWHITESQUARE,
				Constants.EMOJIPREVQUESTION,
				Constants.EMOJINEXTQUESTION,
				Constants.EMOJIWHITESQUARE,
				Constants.EMOJIEXPLICATION
			));
		}else{
			emojis.addAll(Arrays.asList(
				Constants.EMOJIWHITESQUARE,
				Constants.EMOJIPREVQUESTION,
				Constants.EMOJIMORETIME,
				Constants.EMOJINEXTQUESTION,
				Constants.EMOJIWHITESQUARE,
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
    public CommandOutput nextQuestion() {
		previousQuestionIndex =currentQuestionIndex;
		currentQuestionIndex++;
		BotCore.explicationRequestByChannel.get(getChannelId()).remove(quizMessage.getId());
		userAnswersForCurrQuestion.clear();
        return sendNextQuestion();
    }
	
    public CommandOutput prevQuestion(){
		if (getCurrentQuestionIndex() < 1) {
            return BotCommand.getCommandByName(HelpCommand.CMDNAME).execute(null, channelId, List.of(PreviousCommand.CMDNAME), false);
        }
		BotCore.explicationRequestByChannel.get(getChannelId()).remove(quizMessage.getId());
		previousQuestionIndex =currentQuestionIndex;
        currentQuestionIndex -=1;
		userAnswersForCurrQuestion.clear();
        return sendNextQuestion();
    }
	
    public CommandOutput currQuestion(){
		if (getCurrentQuestionIndex() < 0) {
            return new CommandOutput.Builder().build();
        }
		previousQuestionIndex =currentQuestionIndex;
		BotCore.explicationRequestByChannel.get(getChannelId()).remove(quizMessage.getId());

		CommandOutput.Builder outputBuilder = new CommandOutput.Builder();
        if (!isActive()) { return outputBuilder.build();}
        if (getCurrentQuestionIndex() >= quizQuestions.size()) {
			BotCore.explicationRequestByChannel.get(getChannelId()).remove(quizMessage.getId());
			List<String> args = List.of();
            return BotCommand.getCommandByName(EndCommand.CMDNAME).execute(null, channelId, args, false);
        }
		
        Question currentQuestion = getCurrQuestion();
		
        if (!awnsersByUserByQuestion.containsKey(currentQuestion)) {
			awnsersByUserByQuestion.put(currentQuestion, new HashMap<>());
        }
		return outputBuilder
			.addTextMessage(formatQuestion(currentQuestion))
			.addPostSendAction(message -> {
				BotCore.getQuizBot(channelId).setQuizMessage(message);
				addReactions(quizMessage, getButtons().iterator());
				BotCore.explicationRequestByChannel.get(getChannelId()).add(quizMessage.getId());
			}).build();
    }
    private String formatQuestion (Question q) {
        String quiz = "", options = "";
		if (delaySec>0&& awnsersByUserByQuestion.get(getCurrQuestion()).size()>=1){
			this.timeLimit = TimeFormat.RELATIVE.after(delaySec*1000);
		} else if (delaySec==0){
			this.timeLimit = TimeFormat.RELATIVE.after(delaySec*1000);
		}
		int index = quizQuestions.indexOf(q);
        String questionText = "### "+(index+1)+"/"+quizQuestions.size()+" "+q.getQuestion()+"\n";
        for (int i = 0; i < q.size(); i++) {
			options += (i + 1)+". "+q.get(i).getText()+"\n";
        }
        quiz = questionText + options+getLastTimestamp()+"\n";
        return quiz;
    }
    public void end() {
		quizActive = false;
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
	public List<String> leaderBoard(){
		List<String> res = new ArrayList<>();
		double totalPoints = quizQuestions.size()*pointsForCorrect;
        String leaderboard = "Leaderboard:\n";

        Iterator<Map.Entry<String, Double>> SortedScoreByUser = userScoreExact.entrySet().stream()
            .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).iterator();
		
		int i = 1;
        while(SortedScoreByUser.hasNext()) {
            Map.Entry<String, Double> entry = SortedScoreByUser.next();
            leaderboard += "#"+i+"."+BotCore.getEffectiveNameFromId(entry.getKey());
            leaderboard += ": `"+entry.getValue()+"`\n";
            i++;
			if (leaderboard.length()>(Constants.CHARSENDLIM-1000)) {
				res.add(leaderboard);
				leaderboard="";
			}
        }
		leaderboard += "Max points :`"+totalPoints+"`\n";
		res.add(leaderboard);
		return res;
	}
	
	public List<String> explain(String userId){
		List<Question> main = new ArrayList<>();
		String text = "";
		List<String> res = new ArrayList<>();
		String explication;
		int index;
		if (isActive()) {
			main.add(getCurrQuestion());
		} else {
			main.addAll(quizQuestions);
		}
		Option opt;
		Set<Option> optsUser;
		String optsString;
		Double points;
		text += "For "+BotCore.getEffectiveNameFromId(userId)+"\n";
		text += "## "+quizQuestions.getName() + " `"+ getUserScore(userId)+"/"+quizQuestions.size()+"`\n";
		for (Question q : main) {
			points = 0.00;
			optsUser = getUserSelOptions(userId, q);
			int numberOfTrueOptions = q.getTrueOptions().size();
			optsString = "";
			index = quizQuestions.indexOf(q)+1;
			for (int i = 0; i < q.size(); i++) {
				opt = q.get(i);
				explication = opt.getExplicationFriendly();
				optsString += "> "+(i + 1)+". "+opt.getText()+"\n";
				if (optsUser!=null && optsUser.contains(opt)){
					points += opt.isCorrect()?pointsForCorrect/numberOfTrueOptions:pointsForIncorrect;
					optsString += "> "+(opt.isCorrect()?Constants.EMOJITRUE:Constants.EMOJIFALSE).getFormatted()+explication+"\n";
				}else {
					optsString += "> "+(opt.isCorrect()?Constants.EMOJICORRECT:Constants.EMOJIINCORRECT).getFormatted()+explication+"\n";
				}
			}

			text += "### "+(index)+". "+q.getQuestion();
			text += " `"+points+"/1`\n";
			text += optsString;
			explication = q.getExplicationFriendly();
			text += "> \n> **"+explication+"**\n\n";

			if (text.length()>(Constants.CHARSENDLIM-1000)) {
				res.add(text);
				text="";
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
			main = getQuizQuestionList().subList(0, getCurrentQuestionIndex()+1);
		} else {
			main = new ArrayList<>(getQuizQuestionList());
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
}