package com.linked.quizbot.commands.list;

import java.util.List;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

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
 * The {@code CollectionCommand} class retrieves and displays a user's collection of question lists,
 * categorized by tags. It extends {@link BotCommand} and allows users to view their own collections
 * or those of other users by specifying a user ID.
 * <p>
 * This command is part of a Discord bot that manages quiz questions and user interactions.
 * It provides functionality to list all question lists associated with a user, including an example list.
 * The command can be invoked with various abbreviations and includes options for specifying the user ID.
 * </p>
 * @author alinked0
 * @version 1.0
 * @since 2025-02-01
 * @see BotCommand
 * @see Users
 * @see QuestionList
 */
public class CollectionCommand extends BotCommand {
	public static final String CMDNAME = "collection";
    private String cmdDesrciption = "listing all questions";
	private List<String> abbrevs = List.of("c", "ls");
	
	@Override
	public String getName(){ return CMDNAME;}
	@Override
	public BotCommand.CommandCategory getCategory(){
		return BotCommand.CommandCategory.READING;
	}
	@Override
	public List<String> getAbbreviations(){ return abbrevs;}
	@Override
	public String getDescription(){ return cmdDesrciption;}
	@Override
    public List<OptionData> getOptionData(){
        List<OptionData> tmp = new ArrayList<>();
		tmp.add(new OptionData(OptionType.STRING, "user-id", "id of the user who's questions will be listed"));
        return tmp;
    }
	@Override
	public CommandOutput execute(String userId,  List<String> args){
		List<String> res = new ArrayList<>();
		String tmp = "Collection of ";
		tmp += String.format("<@%s>\n",userId);

		User user = Users.get(userId);
		List<QuestionList> list = user.getLists();
		QuestionList example = QuestionList.getExampleQuestionList();
		list.sort(QuestionList.comparatorByDate().reversed());
		list.add(example);
		int maxTags = 0;
		for (QuestionList l : list){
			maxTags = Math.max(maxTags, l.getTags().size());
		}
		Collection<Emoji> emojis;
		String emojiStr = "";
		for (QuestionList l : list){
			emojiStr = "";
			emojis = l.getTags().values();
			if (emojis.size()<maxTags){
				emojiStr += Constants.EMOJIWHITESQUARE.getAsReactionCode().repeat(maxTags-emojis.size());
			}
			for (Emoji e: emojis){
				emojiStr +=e.getAsReactionCode();
			}
			tmp += String.format("`%s`%s%s\n", l.getId(),emojiStr,l.getName());
			if (tmp.length()>Constants.CHARSENDLIM - 400) {
				res.add(tmp);
				tmp = "";
			}
		}
		if (tmp.length()>0) res.add(tmp);
		return new CommandOutput.Builder()
				.addAllTextMessage(res)
				.build();
    }
}
