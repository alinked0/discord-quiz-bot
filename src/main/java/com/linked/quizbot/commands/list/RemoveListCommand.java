package com.linked.quizbot.commands.list;

import java.util.List;
import java.util.ArrayList;

import com.linked.quizbot.Constants;
import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.commands.CommandOutput;
import com.linked.quizbot.core.BotCore;
import com.linked.quizbot.utils.QuestionList;
import com.linked.quizbot.utils.Users;

import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

/**
 * The {@code RemoveListCommand} class provides functionality to permanently remove a list of questions
 * without confirmation. It extends {@link BotCommand} and is part of a Discord bot that manages quiz games.
 * <p>
 * This command allows users to delete a specified question list from their collection, bypassing the usual
 * confirmation step for quicker removal.
 * </p>
 * <p>
 * For now this command is not listed in the help command, as it is intended for
 * administrative use or for specific scenarios where immediate deletion is required.
 * </p>
 * @author alinked0
 * @version 1.0
 * @since 2025-02-01
 * @see BotCommand
 */
public class RemoveListCommand extends BotCommand{
    public static final String CMDNAME = "removelist";
    @Override
    public String getName(){ return CMDNAME;}
    @Override
    public String getDescription(){ return "permenantly removing a list of questions with no confirmation";}
    @Override
    public List<OptionData> getOptionData(){
        List<OptionData> res = new ArrayList<OptionData>();
        res.add(new OptionData(OptionType.STRING, "message-id", "the message id associated with the ongoing viewer", true)
        .setRequiredLength(Constants.DISCORDIDLENMIN, Constants.DISCORDIDLENMAX));
        return res;
    }
    @Override
    public CommandOutput execute(String userId,  List<String> args){
        if (args.size()<getOptionData().size()){
            return BotCommand.getCommandByName(HelpCommand.CMDNAME).execute(userId, List.of(getName()));
        }
        CommandOutput.Builder output = new CommandOutput.Builder();
        String messageId = args.get(0);
        QuestionList l;
        if (BotCore.toBeDeleted.get(messageId)==null){
            l= Users.getById(messageId);
            if (l==null){
                output.add(String.format("Couldn't guess your intentions, nothing will change."));
            }
        } else {
            l= BotCore.toBeDeleted.get(messageId);
            String listId = l.getId();
            CommandOutput outRaw = BotCommand.getCommandByName(RawListCommand.CMDNAME).execute(userId, List.of(listId));
            output.addAllFile(outRaw.getFiles())
            .setMessage(BotCore.deletionMessages.get(messageId))
            .sendInOriginalMessage(true);
        }
        if (l!=null){
            if (Users.deleteList(l)){
                output.add(String.format("`%s`**%s** is deleted form your collection\n", l.getId(), l.getName()));
            } else {
                output.add(String.format("`%s`**%s** will be deleted form your collection\n", l.getId(), l.getName()));
            }
            BotCore.toBeDeleted.remove(messageId);
            BotCore.deletionMessages.remove(messageId);
        }
        return output.build();
    }
}
