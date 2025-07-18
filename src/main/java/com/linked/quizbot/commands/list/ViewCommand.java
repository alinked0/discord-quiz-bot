package com.linked.quizbot.commands.list;

import java.util.List;

import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.commands.CommandOutput;
import com.linked.quizbot.core.Viewer;
import com.linked.quizbot.utils.QuestionList;
import com.linked.quizbot.utils.Users;

import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class ViewCommand extends BotCommand{
    public static final String CMDNAME = "view";
    private String cmdDesrciption = "showing the contents of a question list in a readable manner";
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
    public CommandOutput execute(String userId,  List<String> args){
		QuestionList l = args.size()>0?getSelectedQuestionList(args.get(0)): null;
        if (l==null){
            return BotCommand.getCommandByName(HelpCommand.CMDNAME).execute(userId, List.of(getName()));
        }
        Viewer viewer = new Viewer(l, Users.get(userId).useButtons(), false);
		return viewer.start();
    }

}
