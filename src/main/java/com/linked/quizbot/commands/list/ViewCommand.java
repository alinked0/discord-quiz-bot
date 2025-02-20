package com.linked.quizbot.commands.list;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.linked.quizbot.Constants;
import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.commands.CommandCategory;
import com.linked.quizbot.core.BotCore;
import com.linked.quizbot.core.QuizBot;
import com.linked.quizbot.utils.QuestionList;
import com.linked.quizbot.utils.UserLists;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.utils. AttachedFile;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;

public class ViewCommand extends BotCommand{
    public static final String CMDNAME = "view";
    private String cmdDesrciption = "show the list of questions as in a json format";
	private String[] abbrevs = new String[]{"v"};
    
	@Override
	public String[] getAbbreviations(){ return abbrevs;}
	@Override
	public CommandCategory getCategory(){
		return CommandCategory.READING;
	}
	@Override
    public String getName(){ return CMDNAME;}
	@Override
    public String getDescription(){ return cmdDesrciption;}
	@Override
    public List<OptionData> getOptionData(){
        List<OptionData> res = new ArrayList<>();
        res.add(new OptionData(OptionType.INTEGER, "index-of-theme", "the theme index given by !c"));
        res.add(new OptionData(OptionType.INTEGER, "index-of-list", "the list index given by !c"));
        return res;
    }
	@Override
    public void execute(User sender, Message message, MessageChannel channel, String[] args){
        String res = "```js\n";
        QuestionList l = getSelectedQuestionList(sender.getId(), channel.getJDA(), args);
        if (l==null){
            BotCommand.getCommandByName(HelpCommand.CMDNAME).execute(sender, message, channel, new String[]{getName()});
            return;
        }
		res += l.toString()+"\n```";
        
        MessageCreateAction send;
        int n = res.length();
        if(n>Constants.CHARSENDLIM) {
            send = channel.sendFiles(AttachedFile.fromData(new File(l.getPathToList())));
        }else {
            send = channel.sendMessage(res);
        }
        if (message != null){send.setMessageReference(message);}
        send.queue(msg -> msg.delete().queueAfter(Constants.READTIMELONGMIN, TimeUnit.MINUTES));
    }

}
