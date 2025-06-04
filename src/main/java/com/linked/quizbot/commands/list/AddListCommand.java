package com.linked.quizbot.commands.list;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.linked.quizbot.Constants;
import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.commands.CommandCategory;
import com.linked.quizbot.utils.QuestionList;
import com.linked.quizbot.utils.QuestionListParser;
import com.linked.quizbot.utils.UserLists;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;

public class AddListCommand extends BotCommand{
public static final String CMDNAME = "addlist";
    private String cmdDesrciption = "adding a list of questions to a user's lists";
	private String[] abbrevs = new String[]{"add", "al"};
    
	@Override
	public String[] getAbbreviations(){ return abbrevs;}
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
        String s = "```js \n"+Constants.CMDPREFIXE+getName()+" "+QuestionList.getExampleQuestionList()+"\n```";
        return s;
    }
	@Override
    public List<OptionData> getOptionData(){
        List<OptionData> res = BotCommand.getCommandByName(CreateListCommand.CMDNAME).getOptionData();
        return res;
    }
	@Override
    public void execute(User sender, Message message, MessageChannel channel, String[] args){
        int n = args.length;
        String res = "Failed, for yet to be known reasons.";
        String userId = sender.getId().replace("[a-zA-Z]", "");
        if (n<=0) {
            BotCommand.getCommandByName(HelpCommand.CMDNAME).execute(sender, message, channel, new String[]{getName()});
            return;
        }
        for (int i = 0; i<n; i++) {
            QuestionList l = QuestionListParser.stringToQuestionList(args[i]);
            if (l!=null) {
                if (l.getAuthorId()==null) {
                    l.setAuthorId(userId);
                }
                res = addListAndReturnMessage(l);
            }
        }
        MessageCreateAction send;
        send = channel.sendMessage(res);
        if(message!=null){send.setMessageReference(message);}
        send.queue(msg -> msg.delete().queueAfter(Constants.READTIMEMIN, TimeUnit.MINUTES));
    }
    private String addListAndReturnMessage(QuestionList l) {
        String res = "";
        if (QuestionList.getExampleQuestionList().getName().equals(l.getName())) {
            res += "The example list cannot be modified.\n";
            return res;
        }
        if (l.getName()!=null /*&& l.getTheme()!=null*/) {
            for (QuestionList k : UserLists.getUserListQuestions(l.getAuthorId())){
                if(k.getName().equals(l.getName())) {
                    UserLists.addListToUser(l.getAuthorId(), l);
                    String index = UserLists.getCodeForIndexQuestionList(l);
                    res += "Success, list has been added, use ```"+Constants.CMDPREFIXE+ViewCommand.CMDNAME+" "+l.getAuthorId()+" "+index+"``` command to verife.\n" +res;
                    return res;
                }
            }
        }
        res += "Failed, list of name : \""+l.getName()+"\" doesn't exists.";
        return res;
    }
}
