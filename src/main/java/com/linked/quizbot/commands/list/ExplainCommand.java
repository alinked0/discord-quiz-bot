package com.linked.quizbot.commands.list;

import java.util.List;
import java.util.Set;
import java.nio.channels.Channel;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.linked.quizbot.Constants;
import com.linked.quizbot.commands.BotCommand.CommandCategory;
import com.linked.quizbot.commands.CommandOutput;
import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.core.BotCore;
import com.linked.quizbot.core.QuizBot;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.TimeFormat;

/**
 * The {@code ExplainCommand} class handles the explanation of the current or previous quiz question.
 * If a quiz is currently running in the channel, it provides an explanation for the current question.
 * If no active quiz is found, it attempts to retrieve the previous quiz for explanation.
 *
 * <h2>Usage:</h2>
 * <ul>
 *     <li>Retrieves the explanation for the current question in an active quiz session.</li>
 *     <li>If no active quiz is found, checks for the previous quiz session in the channel.</li>
 *     <li>If no quiz data is available, redirects the user to the help command.</li>
 * </ul>
 *
 * <h2>Features:</h2>
 * <ul>
 *     <li>Fetches quiz data from {@link BotCore}.</li>
 *     <li>Calls the {@code explain} method in {@link QuizBot} to provide explanations.</li>
 *     <li>Handles cases where no quiz data is available.</li>
 * </ul>
 *
 * <h2>Command Behavior:</h2>
 * <ul>
 *     <li>If a quiz is active, calls {@code q.explain(sender)} to provide an explanation.</li>
 *     <li>If no quiz is active, retrieves the previous quiz using {@code BotCore.getPrevQuizBot(channelId)}.</li>
 *     <li>If no previous quiz exists, invokes the help command.</li>
 * </ul>
 *
 * <h2>Example:</h2>
 * <pre>
 * // Get an explanation for the current quiz question
 * !explain
 * </pre>
 *
 * @author alinked0
 * @version 1.0
 * @see BotCommand
 * @see QuizBot
 * @see BotCore
 */
public class ExplainCommand extends BotCommand {
    public static final String CMDNAME = "explain";
    private String cmdDesrciption = "explaining the scoring for, the current question if a game is ongoing, or all question if not.";
	private List<String> abbrevs = List.of("expl");
    
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
		List<OptionData> res = new ArrayList<>();
        res.add (new OptionData(OptionType.STRING, "messageid", "id of the message to explain", false));
        return res;
    }
	@Override
    public CommandOutput execute(String userId, String channelId, List<String> args, boolean reply){
        QuizBot q = BotCore.getCurrQuizBot(channelId);
        if (q == null){
            q = BotCore.getPrevQuizBot(channelId);
        } 
        if (q == null) {
            return BotCommand.getCommandByName(HelpCommand.CMDNAME).execute(userId,  channelId, List.of(getName()), reply);
        }
        List<String> expl = q.explain(userId);
        CommandOutput.Builder outputBuild = new CommandOutput.Builder().addAllTextMessage(expl);
        int delay = q.getDelaySec();
        if (q.isActive()) {
            args = List.of(""+Constants.READTIMEMIN*60);
            outputBuild.editResponseIntoOriginalMessage(true)
            .addTextMessage(q.getLastTimestamp().toString());
        }
        q.setDelay(delay);
		return outputBuild.build();
    }
}
