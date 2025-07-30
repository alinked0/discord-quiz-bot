package com.linked.quizbot.commands.list;

import java.util.ArrayList;
import java.util.List;

import com.linked.quizbot.Constants;
import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.commands.CommandOutput;
import com.linked.quizbot.core.BotCore;
import com.linked.quizbot.core.viewers.QuizBot;
import com.linked.quizbot.utils.User;
import com.linked.quizbot.utils.Users;

import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

/**
 * The {@code UseAutoNextCommand} class adds a few more secs on the awnser time.
 *
 * @author alinked0
 * @version 1.0
 * @see BotCommand
 * @see QuizBot
 * @see BotCore
 */
public class UseAutoNextCommand extends BotCommand {
	public static final String CMDNAME = "useautonext";
	private String cmdDesrciption = "going to the next question as soon as an awnser is registered";
	private List<String> abbrevs = List.of("un");
	
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
        res.add(new OptionData(OptionType.STRING, "yes-or-no", "choose if the game should automaticly go to the next question", true));
        return res;
    }
	@Override
	public CommandOutput execute(String userId,  List<String> args){
		if (args.size() < getRequiredOptionData().size()){
			return BotCommand.getCommandByName(HelpCommand.CMDNAME).execute(userId, List.of(getName()));
		}
		boolean b;
		User user = Users.get(userId);
		String s = args.get(0).toLowerCase();
		switch (s) {
			case "false", "f", "no", "n", "off":
				b = false;
				break;
			default:
				b = true;
				break;
		}
		user.useAutoNext(b);
		Users.update(user);
		return new CommandOutput.Builder()
				.addTextMessage(String.format("AutoNext has been set to %s", b))
				.build();
	}
}
