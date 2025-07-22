package com.linked.quizbot.commands.list;

import java.util.List;

import java.io.File;
import java.io.IOException;

import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.commands.CommandOutput;
import com.linked.quizbot.utils.QuestionList;

import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class RawListCommand extends BotCommand{
    public static final String CMDNAME = "rawlist";
    private String cmdDesrciption = "sending the raw list of questions in a json format";
	private List<String> abbrevs = List.of("raw", "rl");
    
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
        if (args.size()<getOptionData().size()){
            return BotCommand.getCommandByName(HelpCommand.CMDNAME).execute(userId, List.of(getName()));
        }
		QuestionList l = args.size()>0?getSelectedQuestionList(args.get(0)): null;
        if (l==null){
            return BotCommand.getCommandByName(HelpCommand.CMDNAME).execute(userId, List.of(getName()));
        }
        CommandOutput.Builder outputBuilder = new CommandOutput.Builder();
        try{
            outputBuilder.addFile(new File(l.getPathToList()));
        } catch(IOException e){
            e.printStackTrace();
        }
		return outputBuilder.build();
    }

}
