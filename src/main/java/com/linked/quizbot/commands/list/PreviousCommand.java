package com.linked.quizbot.commands.list;

import java.util.ArrayList;
import java.util.List;

import com.linked.quizbot.Constants;
import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.commands.CommandOutput;
import com.linked.quizbot.core.BotCore;
import com.linked.quizbot.core.viewers.QuizBot;
import com.linked.quizbot.core.viewers.Viewer;
import com.linked.quizbot.utils.User;
import com.linked.quizbot.utils.Users;

import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

/**
 * The {@code PreviousCommand} class the previous question to get sent.
 *
 * @author alinked0
 * @version 1.0
 * @see BotCommand
 * @see QuizBot
 * @see BotCore
 */
public class PreviousCommand extends BotCommand {
    public static final String CMDNAME = "previous";
    private String cmdDesrciption = "triggering the previous question to get sent if a quiz is ongoing";
	private List<String> abbrevs = List.of("prev");

	public List<String> getAbbreviations(){ return abbrevs;}
	@Override
	public BotCommand.CommandCategory getCategory(){
		return BotCommand.CommandCategory.NAVIGATION;
	}
	@Override
    public String getName(){ return CMDNAME;}
	@Override
    public String getDescription(){ return cmdDesrciption;}
	@Override
    public List<OptionData> getOptionData(){
        List<OptionData> res = new ArrayList<OptionData>();
        res.add(new OptionData(OptionType.STRING, "message-id", "the message id associated with the ongoing viewer", true)
        .setRequiredLength(Constants.DISCORDIDLENMIN, Constants.DISCORDIDLENMAX));
        return res;
    }
	@Override
    public CommandOutput execute(String userId, List<String> args){
		if (args.size() < getRequiredOptionData().size()){
			return BotCommand.getCommandByName(HelpCommand.CMDNAME).execute(userId, List.of(getName()));
		}
        Viewer q = BotCore.getViewer(args.get(0));
        if (q != null && q.isActive()){
			return q.previous();
        }
        return BotCommand.getCommandByName(HelpCommand.CMDNAME).execute(userId, List.of(getName()));
    }
}
