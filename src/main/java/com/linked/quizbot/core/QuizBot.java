
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
import java.util.concurrent.TimeUnit;

import com.linked.quizbot.Constants;
import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.commands.list.EndCommand;
import com.linked.quizbot.commands.list.HelpCommand;
import com.linked.quizbot.commands.list.PreviousCommand;
import com.linked.quizbot.utils.Option;
import com.linked.quizbot.utils.Question;
import com.linked.quizbot.utils.QuestionList;
import com.linked.quizbot.utils.UserLists;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.TimeFormat;

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
 * @see UserLists
 */
public class QuizBot extends ListenerAdapter {
	public final Map<User, Double> userScoreApproxi = new HashMap<>();
    public final Map<User, Set<Option>> userAnswers = new HashMap<>();
    public final Map<Question, Map<User, Set<Option>>> awnsersByUserByQuestion = new HashMap<>();
	public Map<User, Double> userScoreExact = null;
    public QuestionList quizQuestions;
    public boolean quizActive;
    public int currentQuestionIndex;
    public Message quizMessage = null;
	public double pointsForCorrect = 1.00;
	public double pointsForIncorrect = -0.25;
    public int delaySec = 15;
    public MessageChannel channel;
	public static Random random = new Random();

    public QuizBot(MessageChannel channel){
        this.channel = channel;
        quizActive = false;
        this.quizQuestions = new QuestionList();
    }
    public QuizBot(MessageChannel channel, QuestionList c) {
        this(channel);
        this.quizQuestions = c;
    }
    public void setQuestionList(QuestionList c) {
        quizQuestions = c;
    }
    public Message getQuizMessage() { return quizMessage;}
    public void setChannel(MessageChannel channel) { this.channel = channel;}
    public MessageChannel getChannel(){ return channel;}

    public Boolean isActive() { return quizActive;}

	public void start() {
		BotCore.explicationRequestByChannel.put(getChannel().getId(), new HashSet<>());
		quizActive = true;
		currentQuestionIndex = 0;
		userScoreApproxi.clear();
		userAnswers.clear();
        awnsersByUserByQuestion.clear();
		sendNextQuestion();
	}

	public int getDelaySec(){
		return delaySec;
	}

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

	
    public int getCurrentQuestionIndex() { return currentQuestionIndex;}

    private void sendNextQuestion() {
        if (!isActive()) { return;}
        if (getCurrentQuestionIndex() >= quizQuestions.size()) {
			BotCore.explicationRequestByChannel.get(getChannel().getId()).remove(quizMessage.getId());
			Message message = getQuizMessage();
			User sender = null;
			String[] args = new String[0];
            BotCommand.getCommandByName(EndCommand.CMDNAME)
			.execute(sender, null, channel, args);
            return;
        }
		
        Question currentQuestion = getCurrQuestion();
        currentQuestion.rearrageOptions(random);
        quizQuestions.set(getCurrentQuestionIndex(), currentQuestion);
		
        if (!awnsersByUserByQuestion.containsKey(currentQuestion)) {
			awnsersByUserByQuestion.put(currentQuestion, new HashMap<>());
        }
		// Add reactions to the message for each answer option
		List<Emoji> emojis = new ArrayList<>();
		for (int i = 0; i < currentQuestion.size(); i++) {
			emojis.add(getReactionForAnswer(i + 1));
		}
		// Add White space for seperation between awnsers and navigation
		emojis.addAll(Arrays.asList(Constants.EMOJIWHITESQUARE,
		Constants.EMOJIPREVQUESTION,
		Constants.EMOJIMORETIME,
		Constants.EMOJINEXTQUESTION,
		Constants.EMOJIWHITESQUARE,
		Constants.EMOJIEXPLICATION
		));
        getChannel().sendMessage(formatQuestion(currentQuestion)).queue(message -> {
            quizMessage = message;
			addReactions(quizMessage, emojis.iterator());
			BotCore.explicationRequestByChannel.get(getChannel().getId()).add(quizMessage.getId());
            // Start the next question after delaySec seconds
            message.delete().queueAfter(delaySec, TimeUnit.SECONDS, tmp ->
            {
				// if we are still on the same message that means this thread is still the one serving the questions, 
				// if not that means this thread should end.
				if (quizMessage.equals(message)) {
					nextQuestion();
				}
            }, 
				failure -> {
					System.out.printf(" $> Error is being hanfled --+\n\t $> at com.linked.quizbot.core.QuizBot.lambda$0(QuizBot.java:182)");
					if(Constants.AREWETESTING) failure.printStackTrace();
					System.out.printf(" $> Error is being hanfled --+\n");
			}
			);
        });
    }
	private static void addReactions(Message message, Iterator<Emoji> iter) {
		if(iter.hasNext()){
			message.addReaction(iter.next()).queue( msg -> addReactions(message, iter));
        }
    }
    public void nextQuestion() {
		currentQuestionIndex++;
		BotCore.explicationRequestByChannel.get(getChannel().getId()).remove(quizMessage.getId());
        sendNextQuestion();
		userAnswers.clear();
    }
	
    public void prevQuestion(){
		if (getCurrentQuestionIndex() < 1) {
            BotCommand.getCommandByName(HelpCommand.CMDNAME)
            .execute(null, null, channel, new String[]{PreviousCommand.CMDNAME});
			return;
        }
		BotCore.explicationRequestByChannel.get(getChannel().getId()).remove(quizMessage.getId());
        currentQuestionIndex -=1;
        sendNextQuestion();
		userAnswers.clear();
    }
	
    public void currQuestion(){
		if (getCurrentQuestionIndex() < 0) {
            return;
        }
		BotCore.explicationRequestByChannel.get(getChannel().getId()).remove(quizMessage.getId());
        sendNextQuestion();
    }
    private String formatQuestion (Question q) {
        String quiz = "";
        String questionText = "### "+q.getQuestion() + "\n";
        String options = "";
        for (int i = 0; i < q.size(); i++) {
			options += (i + 1)+". "+q.get(i).getText()+"\n";
        }
        options += TimeFormat.RELATIVE.after(delaySec*1000)+"\n";
        quiz = questionText + options;
        return quiz;
    }
    public void end() {
		quizActive = false;
    }
	
	private Set<Option> getUserSelOptions(User requester, Question q) {
		Map<User, Set<Option>> e = awnsersByUserByQuestion.getOrDefault(q, null);
		if(e != null) {
			Set<Option> opts = e.get(requester);
			return opts;
		}
		return null;
	}
	public List<String> leaderBoard(){
		userScoreExact = new HashMap<>();
		List<String> res = new ArrayList<>();
		double totalPoints = quizQuestions.size()*pointsForCorrect;
        String leaderboard = "Leaderboard:\n";
		double point;
		double score;
        Iterator<Map.Entry<Question, Map<User, Set<Option>>>> iter_AwnsersByUserByQuestion = awnsersByUserByQuestion.entrySet().iterator();
        while (iter_AwnsersByUserByQuestion.hasNext()) {
            Map.Entry<Question, Map<User, Set<Option>>> entry_AwnsersByUserByQuestion = iter_AwnsersByUserByQuestion.next();
            Question q = entry_AwnsersByUserByQuestion.getKey();
			int numberOfTrueOptions = q.getTrueOptions().size();
            Iterator<Map.Entry<User, Set<Option>>> iter_AwnsersByUser = entry_AwnsersByUserByQuestion.getValue().entrySet().iterator();
            while (iter_AwnsersByUser.hasNext()){
                Map.Entry<User, Set<Option>> awnsersByUser = iter_AwnsersByUser.next();
				User u = awnsersByUser.getKey();
				score = userScoreExact.getOrDefault( u, 0.00);
                for (Option opt : awnsersByUser.getValue()) {
                    System.out.printf("   $> lb %s, %s\n", opt.isCorrect(), opt.getText());
                    point = (opt.isCorrect()?pointsForCorrect/numberOfTrueOptions:pointsForIncorrect);
                    score += point;
                }
                userScoreExact.put(u, score);
            }
        }
        Iterator<Map.Entry<User, Double>> SortedScoreByUser = userScoreExact.entrySet().stream()
            .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).iterator();
		
		int i = 1;
        while(SortedScoreByUser.hasNext()) {
            Map.Entry<User, Double> entry = SortedScoreByUser.next();
            leaderboard += "#"+i+"."+entry.getKey().getEffectiveName();
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
	
	public List<String> explain(User requester){
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
		Set<Option> opts;
		String optsString;
		Double points;
		text += "## "+quizQuestions.getName() + " `"+ getUserScore(requester)+"/"+quizQuestions.size()+"`\n";
		for (Question q : main) {
			points = 0.00;
			opts = getUserSelOptions(requester, q);
			int numberOfTrueOptions = q.getTrueOptions().size();
			optsString = "";
			index = quizQuestions.indexOf(q)+1;
			text += "### "+(index)+". "+q.getQuestion();
			if (opts!=null) {
				for (Option opt : opts) {
					explication = opt.getExplicationFriendly();
					//System.out.println("   $> opt "+opt.isCorrect()+""+opt.getText() + "\n");
					points += opt.isCorrect()?pointsForCorrect/numberOfTrueOptions:pointsForIncorrect;
					optsString += "> "+(opt.isCorrect()?Constants.EMOJITRUE:Constants.EMOJIFALSE).getFormatted()+explication+"\n";
				}
			}
			text += " `"+points+"/1`\n";
			text += optsString;
			explication = q.getExplicationFriendly();
			text += "> \n> **"+explication+"**\n\n";

			if (text.length()>(Constants.CHARSENDLIM-1000)) {
				res.add(text);
				text="";
			}
		}
		res.add(text);
		return res;
	}

    public Emoji getReactionForAnswer(int index) {
        return Emoji.fromUnicode("U+3"+index+"U+fe0fU+20e3");
    }
    
    public Double getUserScore(User user) {
		double point, score = 0.00;
		List<Question> main;
		if(isActive()){
			main = getQuizQuestionList().subList(0, getCurrentQuestionIndex()+1);
		} else {
			main = new ArrayList<>(getQuizQuestionList());
		}
        for (Question q : main){
			int numberOfTrueOptions = q.getTrueOptions().size();
			Map<User, Set<Option>> awnsersByUser = awnsersByUserByQuestion.get(q);
			if(awnsersByUser!=null) {
				Set<Option> awnsers = awnsersByUser.get(user);
				if(awnsers != null) {
					for (Option opt : awnsers) {
						System.out.printf("   $> lb %s, %s\n", opt.isCorrect(), opt.getText());
						point = (opt.isCorrect()?pointsForCorrect/numberOfTrueOptions:pointsForIncorrect);
						score += point;
					}
				}
			}
        }
        return score;
    }
    public void setDelay(int sec) { this.delaySec = sec;}
}