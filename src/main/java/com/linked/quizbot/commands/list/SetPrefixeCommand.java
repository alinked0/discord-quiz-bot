package com.linked.quizbot.commands.list;

import java.util.List;

import java.util.ArrayList;
import java.util.List;

import com.linked.quizbot.Constants;
import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.commands.BotCommand.CommandCategory;
import com.linked.quizbot.commands.CommandOutput;
import com.linked.quizbot.core.BotCore;
import com.linked.quizbot.utils.QuestionList;
import com.linked.quizbot.utils.Users;

import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class SetPrefixeCommand extends BotCommand{
    public static final String CMDNAME = "setprefixe";
    private String cmdDesrciption = "choose your perffere prefixe";
	private List<String> abbrevs = List.of();
    
	@Override
	public List<String> getAbbreviations(){ return abbrevs;}
	@Override
	public BotCommand.CommandCategory getCategory(){ return BotCommand.CommandCategory.EDITING;}
    @Override
    public String getName(){ return CMDNAME;}
	@Override
    public String getDescription(){ return cmdDesrciption;}
	@Override
    public List<OptionData> getOptionData(){
        List<OptionData> res = new ArrayList<>();
        res.add(new OptionData(OptionType.STRING, "prefixe", "youtr perfered prefixe", true));
        return res;
    }
	@Override
    public CommandOutput execute(String userId,  List<String> args){
        if (args.size() < getRequiredOptionData().size()){
            return BotCommand.getCommandByName(HelpCommand.CMDNAME).execute(userId, List.of(getName()));
        }
        String prefixe=args.get(0);
        Users.get(userId).setPrefix(prefixe);
        String res = BotCore.getEffectiveNameFromId(userId)+"'s prefixe has been set to '"+prefixe+"'";
		return new CommandOutput.Builder()
				.addTextMessage(res)
				.build();
    }

}

