package com.linked.quizbot.commands.list;

import java.util.List;
import java.util.ArrayList;

import com.linked.quizbot.Constants;
import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.commands.CommandOutput;
import com.linked.quizbot.utils.User;
import com.linked.quizbot.utils.Users;

import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

/**
 * The {@code UserInfoCommand} class allows users to retrieve and display their user information in a JSON format.
 * It extends {@link BotCommand} and provides functionality to show user details based on their Discord ID.
 * <p>
 * This command is part of a Discord bot that manages user interactions and provides information about users.
 * It can be used to view user details such as username, ID, and other relevant information in a structured format.
 * </p>
 * @author alinked0
 * @version 1.0
 * @since 2025-02-01
 * @see BotCommand
 * @see Users
 */
public class UserInfoCommand extends BotCommand{
    public static final String CMDNAME = "userinfo";
    private String cmdDesrciption = "showing user information in a json format";
	private List<String> abbrevs = List.of("ui");
    
	@Override
	public List<String> getAbbreviations(){ return abbrevs;}
	@Override
	public BotCommand.CommandCategory getCategory(){
		return BotCommand.CommandCategory.READING;
	}
	@Override
    public String getName(){ return CMDNAME;}
	@Override
    public String getDescription(){ return cmdDesrciption;}
	@Override
    public List<OptionData> getOptionData(){
        List<OptionData> res = new ArrayList<OptionData>();
        res.add(new OptionData(OptionType.STRING, "userid", "number identifing the user on discord", false)
		.setRequiredLength(Constants.DISCORDIDLENMIN, Constants.DISCORDIDLENMAX));
        return res;
    }
	@Override
    public CommandOutput execute(String userId,  List<String> args){
		User user = args.size()>0?Users.get(args.get(0)): Users.get(userId);
		return new CommandOutput.Builder()
				.add(String.format("```js\n%s\n```", user.toString()))
				.build();
    }

}
