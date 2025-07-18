package com.linked.quizbot.commands.list;

import java.util.List;

import java.io.File;

import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.commands.CommandOutput;
import com.linked.quizbot.utils.QuestionList;

import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class RawCommand extends BotCommand{
    public static final String CMDNAME = "raw";
    private String cmdDesrciption = "sending the raw list of questions in a json format";
	private List<String> abbrevs = List.of();
    
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
    public CommandOutput execute(String userId,  List<String> args){
		QuestionList l = args.size()>0?getSelectedQuestionList(args.get(0)): null;
        if (l==null){
            return BotCommand.getCommandByName(HelpCommand.CMDNAME).execute(userId, List.of(getName()));
        }
        CommandOutput.Builder outputBuilder = new CommandOutput.Builder();
        outputBuilder.addFile(new File(l.getPathToList()));
		return outputBuilder.build();
    }

}
