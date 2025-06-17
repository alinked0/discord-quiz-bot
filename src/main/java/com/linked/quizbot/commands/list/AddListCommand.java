package com.linked.quizbot.commands.list;

import java.util.List;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.linked.quizbot.Constants;
import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.commands.CommandCategory;
import com.linked.quizbot.utils.QuestionList;
import com.linked.quizbot.utils.QuestionListHash;
import com.linked.quizbot.utils.QuestionListParser;
import com.linked.quizbot.utils.Users;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;

public class AddListCommand extends BotCommand{
    public static final String CMDNAME = "addlist";
    private String cmdDesrciption = "adding a list of questions to a user's lists";
	private List<String> abbrevs = List.of("add", "al");
    
	@Override
	public List<String> getAbbreviations(){ return abbrevs;}
	@Override
	public CommandCategory getCategory(){
        return CommandCategory.EDITING;
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
    public void execute(User sender, Message message, MessageChannel channel, List<String> args){
        int n = args.size();
        List<String> res = new ArrayList<>();
        String userId = sender.getId().replace("[a-zA-Z]", "");
        if (n<=0) {
            BotCommand.getCommandByName(HelpCommand.CMDNAME).execute(sender, message, channel, List.of(getName()));
            return;
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
        BotCommand.recursive_send(res.iterator(), message, channel);
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
