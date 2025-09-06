package com.linked.quizbot.commands.list;

import java.util.List;
import java.util.ArrayList;


import com.linked.quizbot.Constants;
import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.commands.BotCommand.CommandCategory;
import com.linked.quizbot.commands.CommandOutput;
import com.linked.quizbot.utils.QuestionList;
import com.linked.quizbot.utils.User;
import com.linked.quizbot.utils.Users;

import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

/**
 * The {@code TagListCommand} class allows users to tag a list of questions with a specific tag.
 * It extends {@link BotCommand} and provides functionality to associate a tag with multiple question lists.
 * <p>
 * This command is part of a Discord bot that manages quiz questions and user interactions.
 * It enables users to organize their question lists by tagging them, making it easier to find and manage related content.
 * </p>
 * @author alinked0
 * @version 1.0
 * @since 2025-02-01
 * @see BotCommand
 * @see Users
 */
public class TagListCommand extends BotCommand{
    public static final String CMDNAME = "taglist";
    private String cmdDesrciption = "add a tag to a list of questions";
	private List<String> abbrevs = List.of("tag", "tl");
    
	@Override
	public List<String> getAbbreviations(){ return abbrevs;}
	@Override
	public BotCommand.CommandCategory getCategory(){ return BotCommand.CommandCategory.EDITING;}
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
    public CommandOutput execute(String userId,  List<String> args){
        if (args.size() < getRequiredOptionData().size()){
            return BotCommand.getCommandByName(HelpCommand.CMDNAME).execute(userId, List.of(getName()));
        }
        String tagNameInput=args.get(0);
        User user = Users.get(userId);
        String emoji = user.getEmojiFomTagName(tagNameInput);
        String res = "";
        String taggedStr = "Taged";
        String notOwnedStr = "You are not the owner of";
        int totalTagged = 0;
        int totalNotOwned = 0;
        QuestionList k;
        if (emoji!=null){
            for(int i=1; i<args.size(); i++){
                k= user.getById(args.get(i));
                if (k==null){
                    notOwnedStr += " `"+args.get(i)+"`";
                    totalNotOwned +=1;
                } else {
                    Users.addTagToList( k.getId(), tagNameInput);
                    taggedStr += " `"+k.getId()+"`";
                    totalTagged += 1;
                }
            }
        } else {
            res = "Tag : '"+tagNameInput+"' doesn't exist. Firstly create it:`"+Constants.CMDPREFIXE+HelpCommand.CMDNAME +" "+CreateTagCommand.CMDNAME+"`\n";
        }
        if (totalNotOwned>0){res += notOwnedStr + "\n";}
        if (totalTagged>0){res += taggedStr;}
        
		return new CommandOutput.Builder()
				.addTextMessage(res)
				.build();
    }

}

