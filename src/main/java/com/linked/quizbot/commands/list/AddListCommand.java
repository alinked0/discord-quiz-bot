package com.linked.quizbot.commands.list;

import java.util.List;

import java.io.IOException;
import java.util.ArrayList;

import com.linked.quizbot.Constants;
import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.commands.CommandOutput;
import com.linked.quizbot.utils.QuestionList;
import com.linked.quizbot.utils.QuestionListHash;
import com.linked.quizbot.utils.QuestionListParser;
import com.linked.quizbot.utils.Users;

import net.dv8tion.jda.api.interactions.commands.build.OptionData;

/**
 * The {@code AddListCommand} class allows users to add a list of questions to their personal lists.
 * It extends {@link BotCommand} and processes multiple lists provided as arguments.
 * @author alinked0
 * @version 1.0
 * @since 2025-02-01
 * @see BotCommand
 * @see QuestionList
 * @see Users
 */
public class AddListCommand extends BotCommand{
    public static final String CMDNAME = "addlist";
    private String cmdDesrciption = "adding a list of questions to a user's lists";
	private List<String> abbrevs = List.of("add", "al");
    
	@Override
	public List<String> getAbbreviations(){ return abbrevs;}
	@Override
	public BotCommand.CommandCategory getCategory(){
        return BotCommand.CommandCategory.EDITING;
	}
    @Override
    public String getName(){ return CMDNAME;}
	@Override
    public String getDescription(){ return cmdDesrciption;}
    @Override
    public String getDetailedExamples(){ 
        String s = "```"+Constants.CMDPREFIXE+getName()+" "+QuestionList.getExampleQuestionList()+"\n```";
        return s;
    }
	@Override
    public List<OptionData> getOptionData(){
        List<OptionData> res = BotCommand.getCommandByName(CreateListCommand.CMDNAME).getOptionData();
        return res;
    }
	@Override
    public CommandOutput execute(String userId,  List<String> args){
		if (args.size()<getRequiredOptionData().size()){
			return BotCommand.getCommandByName(HelpCommand.CMDNAME).execute(userId, List.of(getName()));
		}
        int n = args.size();
        List<String> res = new ArrayList<>();
        CommandOutput.Builder output = new CommandOutput.Builder();
        for (int i = 0; i<n; i++) {
            try {
                QuestionList l = QuestionListParser.fromString(args.get(i));
                if (l!=null) {
                    if (l.getAuthorId()==null) {
                        l.setAuthorId(userId);
                    }
                    output.addTextMessage(addListAndReturnMessage(l));
                }
            }catch (IOException e){
                res.add(String.format("**Failed to import** ```js\n%s```\n",args.get(i) ));
                res.add(String.format("**Reasons:** %s\n", e.getMessage()));
                e.printStackTrace();
            }
        }
		return output.build();
    }
    @Override
	public List<String> parseArguments(String cmndLineArgs){
		return BotCommand.getCommandByName(CreateListCommand.CMDNAME).parseArguments(cmndLineArgs);
	}
    private String addListAndReturnMessage(QuestionList l) {
        String res;
        res = "Failed, list of name : \""+l.getName()+"\" doesn't exists.";
        if (QuestionList.getExampleQuestionList().getName().equals(l.getName())) {
            res = "The example list cannot be modified.\n";
            return res;
        }
        QuestionList k;
        if (l.getId()!=null && l.getId().length()==QuestionListHash.DEFAULT_LENGTH) {
            k = Users.getQuestionListById(l.getId());
            if(k != null) {
                Users.addListToUser(l.getAuthorId(), l);
                String index = l.getId();
                res = "Success, list has been added, use `"+Constants.CMDPREFIXE+ViewCommand.CMDNAME+" "+index+"` command to verife.\n";
                return res;
            }
        }
        if (l.getName()!=null) {
            k = Users.getQuestionListByName(l.getName());
            if(k != null) {
                Users.addListToUser(l.getAuthorId(), l);
                String index = k.getId();
                res = "Success, list has been added, check with `"+Constants.CMDPREFIXE+ViewCommand.CMDNAME+" "+index+"` .\n";
                return res;
            }
        }
        return res;
    }
}
