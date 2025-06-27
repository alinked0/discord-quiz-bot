package com.linked.quizbot.commands.list;

import java.util.List;

import java.io.File;
import java.util.List;

import com.linked.quizbot.Constants;
import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.commands.BotCommand.CommandCategory;
import com.linked.quizbot.commands.CommandOutput;
import com.linked.quizbot.utils.QuestionList;

import net.dv8tion.jda.api.utils. AttachedFile;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class ViewCommand extends BotCommand{
    public static final String CMDNAME = "view";
    private String cmdDesrciption = "showing a list of questions in a json format";
	private List<String> abbrevs = List.of("v");
    
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
        List<OptionData> res = BotCommand.getCommandByName(StartCommand.CMDNAME).getOptionData();
        return res;
    }
	@Override
    public CommandOutput execute(String userId, String channelId, List<String> args, boolean reply){
		QuestionList l = args.size()>0?getSelectedQuestionList(args.get(0)): null;
        if (l==null){
            return BotCommand.getCommandByName(HelpCommand.CMDNAME).execute(userId,  channelId, List.of(getName()), reply);
        }
        CommandOutput.Builder outputBuilder = new CommandOutput.Builder().reply(reply);
        outputBuilder.addFile(new File(l.getPathToList()));
		return outputBuilder.build();
    }

}
