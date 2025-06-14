package com.linked.quizbot.commands.list;

import java.io.File;
import java.util.List;

import com.linked.quizbot.Constants;
import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.commands.CommandCategory;
import com.linked.quizbot.utils.QuestionList;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.utils. AttachedFile;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;

public class ViewCommand extends BotCommand{
    public static final String CMDNAME = "view";
    private String cmdDesrciption = "showing a list of questions in a json format";
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
        List<OptionData> res = BotCommand.getCommandByName(StartCommand.CMDNAME).getOptionData();
        return res;
    }
	@Override
    public void execute(User sender, Message message, MessageChannel channel, String[] args){
        String res = "```js\n";
		QuestionList l = args.length>0?getSelectedQuestionList(args[0]): null;
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
        send.queue();
    }

}
