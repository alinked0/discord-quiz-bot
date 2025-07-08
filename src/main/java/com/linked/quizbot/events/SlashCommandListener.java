package com.linked.quizbot.events;

import java.util.ArrayList;
import java.util.List;

import com.linked.quizbot.Constants;
import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.commands.BotCommand.CommandCategory;
import com.linked.quizbot.commands.CommandOutput;
import com.linked.quizbot.core.BotCore;
import com.linked.quizbot.core.MessageSender;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

/**
 * The class SlashCommandListener will serve as the first layer to any slash command
 * that means message commands like /help
 */
public class SlashCommandListener extends ListenerAdapter {
	
	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
		User sender = event.getUser();
		// Ignorer les messages des bots
		if (sender.isBot()) return;
        
        if (event.isFromGuild()){
            if (!Constants.canIRunThisHere(event.getGuild().getId())){
                return;
            }
        }
		String userId = sender.getId();
		MessageChannel channel = event.getInteraction().getChannel();
        String channelId = channel.getId();
		
		// log User
		BotCore.addUser(sender);
		
		List<String> args = new ArrayList<>();
		String k;
		BotCommand cmd  = BotCommand.getCommandByName(event.getName());
		if (cmd==null){
			event.reply(String.format("Command %s could not be found.", event.getName())).queue();
			return;
		}

		if(BotCore.isShutingDown()){
			BotCommand.CommandCategory category = cmd.getCategory();
			if(category.equals(BotCommand.CommandCategory.EDITING) || category.equals(BotCommand.CommandCategory.GAME)){
				event.reply(Constants.UPDATEEXPLANATION).queue();
				return;
			}
		}
		for (OptionData d : cmd.getOptionData()){
			OptionMapping tmp = event.getOption(d.getName());
			if(tmp !=null) {
				if (tmp.getType().equals(OptionType.USER)){
					args.add(tmp.getAsUser().getId());
				}
				else if (tmp.getType().equals(OptionType.ATTACHMENT)){
					args.addAll(BotCommand.getArgFromAttachments(userId, tmp.getAsAttachment()));
				} else {
					args.add(tmp.getAsString());
				}
			}
		}
		event.reply(cmd.getDescription()).queue();
		MessageSender.sendCommandOutput(
			cmd.execute(userId, args),
			channel,
			null 
		);
	}
}
