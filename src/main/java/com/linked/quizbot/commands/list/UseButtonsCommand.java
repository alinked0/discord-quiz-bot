package com.linked.quizbot.commands.list;

import java.util.List;

import java.util.ArrayList;

import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.commands.CommandOutput;
import com.linked.quizbot.utils.User;
import com.linked.quizbot.utils.Users;

import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

/**
 * The {@code UseButtonsCommand} class allows users to choose whether they want to use buttons or reactions
 * for interactions in the bot. It extends {@link BotCommand} and provides functionality to toggle this preference.
 * <p>
 * This command is part of a Discord bot that manages quiz questions and user interactions.
 * It enables users to switch between using buttons or reactions for their interactions with the bot.
 * </p>
 * @author alinked0
 * @version 1.0
 * @since 2025-02-01
 * @see BotCommand
 * @see Users
 */
public class UseButtonsCommand extends BotCommand{
	public static final String CMDNAME = "usebuttons";
	private String cmdDesrciption = "choose your perfered buttons";
	private List<String> abbrevs = List.of("ub");
	
	@Override
	public List<String> getAbbreviations(){ return abbrevs;}
	@Override
	public BotCommand.CommandCategory getCategory(){ return BotCommand.CommandCategory.OTHER;}
	@Override
	public String getName(){ return CMDNAME;}
	@Override
	public String getDescription(){ return cmdDesrciption;}
	@Override
	public List<OptionData> getOptionData(){
		List<OptionData> res = new ArrayList<>();
		res.add(new OptionData(OptionType.STRING, "yes-or-no", "if you want to use buttons", true));
		return res;
	}
	@Override
	public CommandOutput execute(String userId,  List<String> args){
		if (args.size() < getRequiredOptionData().size()){
			return BotCommand.getCommandByName(HelpCommand.CMDNAME).execute(userId, List.of(getName()));
		}
		User user = Users.get(userId);
		String s = args.get(0).toLowerCase();
		boolean b;
		switch (s) {
			case "false", "f", "no", "n", "off":
				b = false;
				break;
			default:
				b = true;
				break;
		}
		user.useButtons(b);
		Users.update(user);
		return new CommandOutput.Builder()
				.add(String.format("Your now using %s", b?"Buttons":"Reactions"))
				.build();
	}

}
