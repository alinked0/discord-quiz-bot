package com.linked.quizbot.commands.list;

import java.util.List;

import java.util.ArrayList;

import com.linked.quizbot.core.QuizBot;
import com.linked.quizbot.utils.QuestionList;
import com.linked.quizbot.utils.QuestionListHash;
import com.linked.quizbot.utils.User;
import com.linked.quizbot.utils.Users;
import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.commands.CommandOutput;
import com.linked.quizbot.utils.Question;

import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

/**
 * The {@code StartCommand} class is responsible for initiating a quiz based on a selected question list.
 * Users can start their own quiz or a quiz from another user's collection.
 *
 * <h2>Usage:</h2>
 * <ul>
 *     <li>Allows users to start a quiz using the stored question lists.</li>
 *     <li>Users can specify a theme ID and a question list ID, or let the bot select randomly.</li>
 *     <li>Supports both targeted and randomized quiz selection.</li>
 * </ul>
 *
 * <h2>Features:</h2>
 * <ul>
 *     <li>Retrieves questions from {@link Users} and {@link QuestionList}.</li>
 *     <li>Randomizes questions if no specific list is provided.</li>
 *     <li>Initiates a new {@link QuizBot} instance to handle quiz interactions.</li>
 * </ul>
 *
 * <h2>Command Parameters:</h2>
 * <ul>
 *     <li>{@code themeIndex} (Optional) - The index of the theme in the user's question collection.</li>
 *     <li>{@code questionid} (Optional) - The index of a specific question list within the chosen theme.</li>
 * </ul>
 *
 * <h2>Example:</h2>
 * <pre>
 * // Start a quiz with a specific theme and question list
 * !start 2 3
 *
 * // Start a quiz with a random question list from the first theme
 * !start
 * </pre>
 *
 * @author alinked0
 * @version 1.0
 * @see BotCommand
 * @see QuizBot
 * @see Users
 * @see QuestionList
 * @see Question
 */
public class StartCommand extends BotCommand{
    public static final String CMDNAME = "start";
    private String cmdDesrciption = "starting a given quiz, whether its your own or another users";
	private List<String> abbrevs = List.of("s", "play");

	@Override
	public List<String> getAbbreviations(){ return abbrevs;}
	@Override
	public BotCommand.CommandCategory getCategory(){
        return BotCommand.CommandCategory.GAME;
	}
    @Override
    public String getName(){ return CMDNAME;}
    @Override
    public String getDescription(){ return cmdDesrciption;}
	@Override
    public List<OptionData> getOptionData(){
        List<OptionData> res = new ArrayList<OptionData>();
        res.add(new OptionData(OptionType.STRING, "listid", "listid given by "+CollectionCommand.CMDNAME, true)
        .setRequiredLength(QuestionListHash.DEFAULT_LENGTH, QuestionListHash.DEFAULT_LENGTH));
        return res;
    }
    @Override
    public CommandOutput execute(String userId,  List<String> args){
		QuestionList questions = args.size()>0?getSelectedQuestionList(args.get(0)): null;
        if (questions==null){
			return BotCommand.getCommandByName(HelpCommand.CMDNAME).execute(userId, List.of(getName()));
        }
        questions = new QuestionList.Builder().add(questions).build().rearrageOptions();
		User user = Users.get(userId);
        QuizBot quizBot = new QuizBot(questions, user.useButtons(), false);
        return quizBot.start();
    }
}
