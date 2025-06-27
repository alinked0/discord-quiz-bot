package com.linked.quizbot.commands.list;

import java.util.List;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.linked.quizbot.Constants;
import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.commands.BotCommand.CommandCategory;
import com.linked.quizbot.commands.CommandOutput;
import com.linked.quizbot.utils.QuestionList;
import com.linked.quizbot.utils.QuestionListHash;
import com.linked.quizbot.utils.QuestionListParser;
import com.linked.quizbot.utils.Users;

import net.dv8tion.jda.api.interactions.commands.build.OptionData;

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
    public CommandOutput execute(String userId, String channelId, List<String> args, boolean reply){
        int n = args.size();
        List<String> res = new ArrayList<>();
        if (n<=0) {
            return BotCommand.getCommandByName(HelpCommand.CMDNAME).execute(userId,  channelId, List.of(getName()), reply);
        }
        for (int i = 0; i<n; i++) {
            try {
                QuestionList l = QuestionListParser.fromString(args.get(i));
                if (l!=null) {
                    if (l.getAuthorId()==null) {
                        l.setAuthorId(userId);
                    }
                    res.add(addListAndReturnMessage(l));
                }
            }catch (IOException e){
                res.add("Failed to import ```js\n"+args.get(i)+"```\n");
            }
        }
		return new CommandOutput.Builder()
				.addAllTextMessage(res)
				.reply(reply)
				.build();
    }
    @Override
	public List<String> parseArguments(String cmndLineArgs){
		List<String> res = new ArrayList<>();
        res.addAll(splitJson(cmndLineArgs));
		return res;
	}
    private String addListAndReturnMessage(QuestionList l) {
        String res = "";
        res = "Failed, list of name : \""+l.getName()+"\" doesn't exists.";
        if (QuestionList.getExampleQuestionList().getName().equals(l.getName())) {
            res = "The example list cannot be modified.\n";
            return res;
        }
        QuestionList k;
        if (l.getListId()!=null && l.getListId().length()==QuestionListHash.DEFAULT_LENGTH) {
            k = Users.getQuestionListByListId(l.getListId());
            if(k != null) {
                Users.addListToUser(l.getAuthorId(), l);
                String index = l.getListId();
                res = "Success, list has been added, use `"+Constants.CMDPREFIXE+ViewCommand.CMDNAME+" "+index+"` command to verife.\n";
                return res;
            }
        }
        if (l.getName()!=null /*&& l.getTheme()!=null*/) {
            k = Users.getQuestionListByName(l.getName());
            if(k != null) {
                Users.addListToUser(l.getAuthorId(), l);
                String index = k.getListId();
                res = "Success, list has been added, use `"+Constants.CMDPREFIXE+ViewCommand.CMDNAME+" "+index+"` command to verife.\n";
                return res;
            }
        }
        return res;
    }
}
