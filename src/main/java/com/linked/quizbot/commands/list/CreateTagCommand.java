package com.linked.quizbot.commands.list;

import java.util.List;

import java.util.ArrayList;
import java.util.List;

import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.commands.CommandCategory;
import com.linked.quizbot.utils.Users;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;

public class CreateTagCommand extends BotCommand{
    public static final String CMDNAME = "createtag";
    private String cmdDesrciption = "creating a tag that can be use to sort question lists";
	private List<String> abbrevs = List.of("ct");
    
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
        res.add(new OptionData(OptionType.STRING, "emoji", "a visual aide", true));
        return res;
    }
	@Override
    public void execute(User sender, Message message, MessageChannel channel, List<String> args){
        if (args.size() < getRequiredOptionData().size()){
            BotCommand.getCommandByName(HelpCommand.CMDNAME).execute(sender, message, channel, List.of(getName()));
            return;
        }
        String tagNameInput=args.get(0), tagEmojiInput=args.get(1);
        Emoji emoji = getEmojiFromArg(tagEmojiInput);
        String res;
        
        
        if (Users.createTag(sender.getId(), tagNameInput, emoji)){
            res = "Tag has been created.";
        } else {
            res = "Tag already exists.";
        }
        MessageCreateAction send = channel.sendMessage(res);
        if (message != null){send.setMessageReference(message);}
        send.queue();
    }

}
