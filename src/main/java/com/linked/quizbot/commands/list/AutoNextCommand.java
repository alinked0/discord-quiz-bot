package com.linked.quizbot.commands.list;

import java.util.ArrayList;
import java.util.List;

import com.linked.quizbot.Constants;
import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.commands.CommandOutput;
import com.linked.quizbot.core.BotCore;
import com.linked.quizbot.core.QuizBot;

import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

/**
 * The {@code AutoNextCommand} class adds a few more secs on the awnser time.
 *
 * @author alinked0
 * @version 1.0
 * @see BotCommand
 * @see QuizBot
 * @see BotCore
 */
public class AutoNextCommand extends BotCommand {
	public static final String CMDNAME = "autonext";
	private String cmdDesrciption = "going to the next question as soon as an awnser is registered";
	private List<String> abbrevs = List.of("an");
	
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
        res.add(new OptionData(OptionType.STRING, "message-id", "the message id associated with the ongoing viewer", true)
        .setRequiredLength(Constants.DISCORDIDLENMIN, Constants.DISCORDIDLENMAX));
        res.add(new OptionData(OptionType.STRING, "yes-or-no", "choose if the game should automaticly go to the next question", true));
        return res;
    }
	@Override
	public CommandOutput execute(String userId,  List<String> args){
		if (args.size() < getRequiredOptionData().size()){
			return BotCommand.getCommandByName(HelpCommand.CMDNAME).execute(userId, List.of(getName()));
		}
		QuizBot q = (QuizBot) BotCore.getViewer(args.get(0));
		if (q == null){
			return BotCommand.getCommandByName(HelpCommand.CMDNAME).execute(userId, List.of(getName()));
		}
		String s = args.get(1).toLowerCase();
		boolean b;
		switch (s) {
			case "false", "f", "no", "n", "off":
				b = false;
				break;
			default:
				b = true;
				break;
		}
		q.autoNext(b);
		return new CommandOutput.Builder()
				.addTextMessage(String.format("AutoNext has been set to %s", b))
				.build();
	}
}
