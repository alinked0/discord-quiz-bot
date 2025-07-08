package com.linked.quizbot.commands.list;

import java.util.List;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.linked.quizbot.Constants;
import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.commands.BotCommand.CommandCategory;
import com.linked.quizbot.commands.CommandOutput;
import com.linked.quizbot.utils.User;
import com.linked.quizbot.utils.Users;

import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

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
        res.add(new OptionData(OptionType.STRING, "userid", "number identifing the user on discord", false).setRequiredLength(Constants.DISCORDIDLENMIN, Constants.DISCORDIDLENMAX));
        return res;
    }
	@Override
    public CommandOutput execute(String userId,  List<String> args){
        String res = "```js\n";
		User user = args.size()>0?new User(args.get(0)): null;
        if (user==null){
            user = new User(userId);
        }
		res += user.toString()+"\n```";
        
		return new CommandOutput.Builder()
				.addTextMessage(res)
				
				.build();
    }

}
