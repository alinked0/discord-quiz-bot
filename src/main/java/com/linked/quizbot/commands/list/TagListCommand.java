package com.linked.quizbot.commands.list;

import java.util.List;
import java.util.ArrayList;


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
	private List<String> abbrevs = List.of("tag", "tl");
    
	@Override
	public List<String> getAbbreviations(){ return abbrevs;}
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
    public void execute(User sender, Message message, MessageChannel channel, List<String> args){
        if (args.size() < getRequiredOptionData().size()){
            BotCommand.getCommandByName(HelpCommand.CMDNAME).execute(sender, message, channel, List.of(getName()));
            return;
        }
        String tagNameInput=args.get(0);
        Users user = new Users(sender.getId());
        Emoji emoji = user.getEmojiFomTagName(tagNameInput);
        String res = "";
        String taggedStr = "Taged";
        String notOwnedStr = "You are not the owner of";
        int totalTagged = 0;
        int totalNotOwned = 0;
        QuestionList k;
        if (emoji!=null){
            for(int i=1; i<args.size(); i++){
                k= user.getUserQuestionListByListId(args.get(i));
                if (k==null){
                    notOwnedStr += " `"+args.get(i)+"`";
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

