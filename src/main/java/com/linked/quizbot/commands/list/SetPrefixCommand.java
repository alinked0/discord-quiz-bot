package com.linked.quizbot.commands.list;

import java.util.List;
import java.util.ArrayList;

import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.commands.CommandOutput;
import com.linked.quizbot.core.BotCore;
import com.linked.quizbot.utils.Users;

import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

/**
 * The {@code SetPrefixCommand} class allows users to set their preferred command prefix for the bot.
 * It extends {@link BotCommand} and provides functionality to change the prefix used for bot commands.
 * <p>
 * This command is part of a Discord bot that manages user interactions and preferences.
 * It allows users to customize their experience by setting a unique prefix for commands.
 * </p>
 * @author alinked0
 * @version 1.0
 * @since 2025-02-01
 * @see BotCommand
 * @see Users
 */
public class SetPrefixCommand extends BotCommand{
	public static final String CMDNAME = "setprefix";
	private String cmdDesrciption = "choose your perffere prefix";
	private List<String> abbrevs = List.of();
	
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
		res.add(new OptionData(OptionType.STRING, "prefix", "youtr perfered prefix", true));
		return res;
	}
	@Override
	public CommandOutput execute(String userId,  List<String> args){
		if (args.size() < getRequiredOptionData().size()){
			return BotCommand.getCommandByName(HelpCommand.CMDNAME).execute(userId, List.of(getName()));
		}
		String prefix=args.get(0);
		Users.get(userId).setPrefix(prefix);
		String res = BotCore.getEffectiveNameFromId(userId)+"'s prefix has been set to '"+prefix+"'";
		return new CommandOutput.Builder()
				.add(res)
				.build();
	}

}
