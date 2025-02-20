package com.linked.quizbot.commands.list;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.linked.quizbot.Constants;
import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.utils.QuestionList;
import com.linked.quizbot.utils.UserLists;
import com.linked.quizbot.commands.CommandCategory;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;

public class AddListCommand extends BotCommand{
public static final String CMDNAME = "addlist";
    private String cmdDesrciption = "To add a list of questions to a user's lists";
	private String[] abbrevs = new String[]{"add"};
    
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
        String s = Constants.CMDPREFIXE+getName()+" "+QuestionList.getExampleQuestionList();
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
        String res = "Adding your list, ";
        String userId = sender.getId().replace("[a-zA-Z]", "");
        if (n<=0) {
            BotCommand.getCommandByName(HelpCommand.CMDNAME).execute(sender, message, channel, new String[]{getName()});
            return;
        }
        File f = new File(Constants.LISTSPATH+Constants.SEPARATOR+userId+Constants.SEPARATOR+"tmp");
        for (int i = 0; i<n; i++) {
            try {
                if(!f.getParentFile().exists()) {
                    f.getParentFile().mkdirs();
                }
                BufferedWriter buff = Files.newBufferedWriter(f.toPath());
                buff.write(args[i]);
                buff.close();
            } catch (IOException e) {
                System.err.println("An error occurred while adding a List of questions.");
                res += "failed\n";
                e.printStackTrace();
            }
            QuestionList l = new QuestionList(f.getAbsolutePath());
            if (l!=null) {
                if (l.getAuthorId()==null) {
                    res += "author set to "+userId+"\n";
                    l.setAuthorId(userId);
                }
                if (l.getName()!=null && l.getTheme()!=null) {
                    if(!UserLists.getUserListQuestions(userId).contains(l)) {
                        res = "Failed, list of name : \""+l.getName()+"\" and theme : \""+l.getTheme()+"\" doesn't exists.";
                    } else {
                        res = "Success, list has been added, use ```"+Constants.CMDPREFIXE+"collection "+l.getAuthorId()+"``` command to verife.\n" +res;
                        UserLists.addListToUser(l.getAuthorId(), l);
                    }
                }else {
                    res = "Failed, no \"name\" or \"theme\" found\n";
                }
            }
        }
        f.delete();
        MessageCreateAction send;
        send = channel.sendMessage(res);
        if(message!=null){send.setMessageReference(message);}
        send.queue(msg -> msg.delete().queueAfter(Constants.READTIMEMIN, TimeUnit.MINUTES));
    }
}
