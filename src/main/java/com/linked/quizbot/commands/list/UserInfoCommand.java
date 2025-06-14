package com.linked.quizbot.commands.list;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.linked.quizbot.Constants;
import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.commands.CommandCategory;
import com.linked.quizbot.utils.QuestionList;
import com.linked.quizbot.utils.QuestionListHash;
import com.linked.quizbot.utils.UserData;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.utils. AttachedFile;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;

public class UserInfoCommand extends BotCommand{
    public static final String CMDNAME = "userinfo";
    private String cmdDesrciption = "showing user information in a json format";
	private String[] abbrevs = new String[]{"ui"};
    
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
        List<OptionData> res = new ArrayList<OptionData>();
        res.add(new OptionData(OptionType.STRING, "userid", "number identifing the user on discord", false).setRequiredLength(17, 18));
        return res;
    }
	@Override
    public void execute(User sender, Message message, MessageChannel channel, String[] args){
        String res = "```js\n";
		UserData user = args.length>0?new UserData(args[0]): null;
        if (user==null){
            user = new UserData(sender.getId());
        }
		res += user.toString()+"\n```";
        
        MessageCreateAction send;
        int n = res.length();
        if(n>Constants.CHARSENDLIM) {
            send = channel.sendFiles(AttachedFile.fromData(new File(user.getPathToUserData())));
        }else {
            send = channel.sendMessage(res);
        }
        if (message != null){send.setMessageReference(message);}
        send.queue();
    }

}
