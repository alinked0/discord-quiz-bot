package com.linked.quizbot.commands.list;

import java.util.List;
import java.util.ArrayList;

import com.linked.quizbot.Constants;
import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.commands.CommandOutput;
import com.linked.quizbot.utils.User;
import com.linked.quizbot.utils.Users;

import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

/**
 * The {@code RenameTagCommand} class allows users to change the name of a tag and it's shorthand.
 * It extends {@link BotCommand}.
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
public class RenameTagCommand extends BotCommand{
	public static final String CMDNAME = "renametag";
	private String cmdDesrciption =
        "change the name of tag and it's shorthand, to make it easily identifiable";
	private List<String> abbrevs = List.of("mvt");
	
	@Override
	public List<String> getAbbreviations(){
        return abbrevs;
    }
	@Override
	public BotCommand.CommandCategory getCategory(){
        return BotCommand.CommandCategory.EDITING;
    }
	@Override
	public String getName(){
        return CMDNAME;
    }
	@Override
	public String getDescription(){
        return cmdDesrciption;
    }
	@Override
	public List<OptionData> getOptionData(){
		List<OptionData> res = new ArrayList<>();
		res.add(new OptionData(OptionType.STRING, "old-tag-name", "the name associated with your old tag", true));
		res.add(new OptionData(OptionType.STRING, "new-tag-name", "the name associated with your new tag", true));
		res.add(new OptionData(OptionType.STRING, "new-shorthand", "a shortened new tag name", false));
		return res;
	}
	@Override
	public CommandOutput execute(String userId,  List<String> args){
		if (args.size() < getRequiredOptionData().size()){
			return BotCommand.getCommandByName(HelpCommand.CMDNAME).execute(userId, List.of(getName()));
		}
		String oldTagName, newTagName, res;
		User user;

		user = Users.get(userId);

		oldTagName=args.get(0);
        if (user.getEmojiFomTagName(oldTagName)==null) {
            res = "Tag : `"+oldTagName+"` doesn't exist. Firstly create it:`"
                +Constants.CMDPREFIXE+HelpCommand.CMDNAME +" "
                +CreateTagCommand.CMDNAME+"`\n";
            return new CommandOutput.Builder().add(res).build();
        }

		newTagName=args.get(1);
        if (user.getEmojiFomTagName(newTagName)==null) {
            user.createTag(newTagName, (args.size()>2)?args.get(2):user.getEmojiFomTagName(oldTagName));
        }

        res = String.format(
            "renamed `%s` `%s` -> `%s` `%s`",
            oldTagName, user.getEmojiFomTagName(oldTagName),
            newTagName, user.getEmojiFomTagName(newTagName)
        );

        user.renameTag(oldTagName, newTagName);
		return new CommandOutput.Builder().add(res).build();
	}
}
