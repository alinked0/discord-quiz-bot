package com.linked.quizbot.commands.list;

import java.util.ArrayList;
import java.util.List;

import com.linked.quizbot.Constants;
import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.commands.CommandCategory;
import com.linked.quizbot.utils.QuestionList;
import com.linked.quizbot.utils.Users;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;

public class TagListCommand extends BotCommand{
    public static final String CMDNAME = "taglist";
    private String cmdDesrciption = "add a tag to a list of questions";
	private String[] abbrevs = new String[]{"tag", "tl"};
    
	@Override
	public String[] getAbbreviations(){ return abbrevs;}
	@Override
	public CommandCategory getCategory(){ return CommandCategory.EDITING;}
    @Override
    public String getName(){ return CMDNAME;}
	@Override
    public String getDescription(){ return cmdDesrciption;}
	@Override
    public List<OptionData> getOptionData(){
        List<OptionData> res = new ArrayList<>();
        res.add(new OptionData(OptionType.STRING, "tag-name", "the name associated with your tag", true));
        res.add(new OptionData(OptionType.STRING, "list-id", "listid of the question list you wish to tag", true));
        return res;
    }
	@Override
    public void execute(User sender, Message message, MessageChannel channel, String[] args){
        if (args.length < getRequiredOptionData().size()){
            BotCommand.getCommandByName(HelpCommand.CMDNAME).execute(sender, message, channel, new String[]{getName()});
            return;
        }
        String tagNameInput=args[0];
        Users user = new Users(sender.getId());
        Emoji emoji = user.getEmojiFomTagName(tagNameInput);
        String res = "";
        String taggedStr = "Taged";
        String notOwnedStr = "You are not the owner of";
        int totalTagged = 0;
        int totalNotOwned = 0;
        QuestionList k;
        if (emoji!=null){
            for(int i=1; i<args.length; i++){
                k= user.getUserQuestionListByListId(args[i]);
                if (k==null){
                    notOwnedStr += " `"+args[i]+"`";
                    totalNotOwned +=1;
                } else {
                    Users.addTagToList( k.getListId(), tagNameInput);
                    taggedStr += " `"+k.getListId()+"`";
                    totalTagged += 1;
                }
            }
        } else {
            res = "Tag : '"+tagNameInput+"' doesn't exist. Firstly create it:`"+Constants.CMDPREFIXE+HelpCommand.CMDNAME +" "+CreateTagCommand.CMDNAME+"`\n";
        }
        if (totalNotOwned>0){res += notOwnedStr + "\n";}
        if (totalTagged>0){res += taggedStr;}
        MessageCreateAction send = channel.sendMessage(res);
        if (message != null){send.setMessageReference(message);}
        send.queue();
    }

}

