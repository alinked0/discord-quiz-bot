package com.linked.quizbot.commands.list;

import java.util.ArrayList;
import java.util.Iterator;
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

/**
 * The {@code CollectionCommand} class retrieves and displays a user's collection of question lists,
 * categorized by theme. It extends {@link BotCommand} and allows users to view their own collections
 * or those of other users by specifying a user ID.
 *
 * <h2>Usage:</h2>
 * <ul>
 *     <li>Typing {@code /collection} lists all question themes and lists for the sender.</li>
 *     <li>Typing {@code /collection [user-id]} lists the question themes and lists of the specified user.</li>
 * </ul>
 *
 * <h2>Features:</h2>
 * <ul>
 *     <li>Fetches and categorizes question lists based on themes.</li>
 *     <li>Supports both the sender’s collection and another user’s collection.</li>
 *     <li>Handles message length constraints by sending multiple messages when needed.</li>
 *     <li>Two execution methods provided:
 *         <ul>
 *             <li>{@code exec1} - Optimized execution (default).</li>
 *             <li>{@code exec2} - Slower, alternative execution method.</li>
 *         </ul>
 *     </li>
 *     <li>Messages are automatically deleted after a set duration to keep channels clean.</li>
 * </ul>
 *
 * <h2>Example:</h2>
 * <pre>
 * // Listing the sender's collection
 * /collection
 *
 * // Listing another user's collection
 * /collection 123456789012345678
 * </pre>
 *
 * @author alinked0
 * @version 1.0
 * @see BotCommand
 * @see Users
 * @see QuestionList
 */
public class CollectionCommand extends BotCommand {
	public static final String CMDNAME = "collection";
    private String cmdDesrciption = "listing all questions";
	private String[] abbrevs = new String[]{"c", "ls"};
	
	@Override
	public String getName(){ return CMDNAME;}
	@Override
	public CommandCategory getCategory(){
		return CommandCategory.READING;
	}
	@Override
	public String[] getAbbreviations(){ return abbrevs;}
	@Override
	public String getDescription(){ return cmdDesrciption;}
	@Override
    public List<OptionData> getOptionData(){
        List<OptionData> res = new ArrayList<>();
		res.add(new OptionData(OptionType.STRING, "user-id", "id of the user who's questions will be listed"));
        return res;
    }
	@Override
	public void execute(User sender, Message message, MessageChannel channel, String[] args){
		List<String> col = exec1(sender, message, channel, args);
		Iterator<String> iter = col.iterator();
		recursive_send(iter, message, channel);
    }

	//execution 1 is two times faster than exec 2
	private List<String> exec1 (User sender, Message message, MessageChannel channel, String[] args){
		List<String> result = new ArrayList<>();
		String userId = (args.length>0)?args[0]:sender.getId();
		String res = "Collection of ";
		res += "<@"+userId+">\n";

		Users Users = new Users(userId);
		List<QuestionList> list = Users.getLists();
		list.sort(QuestionList.comparatorByDate().reversed());
		for (QuestionList l : list){
			String emojiStr = "";
			for (Emoji e: l.getTags().values()){
				emojiStr +=e.getAsReactionCode();
			}
			res += "`"+l.getListId()+"` "+l.getName()+" "+emojiStr+"\n";
			if (res.length()>Constants.CHARSENDLIM - 400) {
				result.add(res);
				res = "";
			}
		}
        QuestionList example = QuestionList.getExampleQuestionList();
		res += "`"+example.getListId()+"` "+example.getName()+"\n";
		result.add(res);
		return result;
	}	
}
