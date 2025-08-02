package com.linked.quizbot.commands.list;

import java.util.List;

import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.commands.CommandOutput;
import com.linked.quizbot.core.viewers.Viewer;
import com.linked.quizbot.utils.QuestionList;
import com.linked.quizbot.utils.Users;

import net.dv8tion.jda.api.interactions.commands.build.OptionData;

/**
 * Represents the `view` command for the quiz bot, which allows users to display the contents
 * of a {@link com.linked.quizbot.utils.QuestionList}.
 * <p>
 * This command is designed to present the questions within a list in a human-readable format,
 * making it easy for users to review the content of a quiz before or after taking it.
 * The output is rendered by a {@link com.linked.quizbot.core.viewers.Viewer}, which
 * handles the display logic based on user preferences (e.g., using buttons).
 * </p>
 * @author alinked0
 * @version 1.0
 * @since 2025-02-01
 */
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
        System.out.println(l.toJson());
        Viewer viewer = new Viewer(l, Users.get(userId).useButtons());
		return viewer.start();
    }

}
