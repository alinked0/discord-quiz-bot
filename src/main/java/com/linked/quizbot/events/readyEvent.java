package com.linked.quizbot.events;

import java.util.List;

import com.linked.quizbot.Constants;
import com.linked.quizbot.commands.BotCommand;

import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class readyEvent extends ListenerAdapter {
    @Override
	public void onGenericEvent(GenericEvent event){
		if (Constants.AREWETESTING){
			System.out.println(" $> - "+event);
		}
	}

	@Override
	public void onReady(ReadyEvent event){
		List<SlashCommandData> commandData = BotCommand.getSlashCommandDataList();

		// Clear all global commands
		event.getJDA().retrieveCommands().queue(commands -> {
			for (Command cmd : commands){
				event.getJDA().deleteCommandById(cmd.getId()).queue();
			}
		});

		// Re-register updated global commands
		event.getJDA().updateCommands().addCommands(commandData).queue();
	}

	
	@Override
	public void onGuildReady(GuildReadyEvent event){
		List<SlashCommandData> commandData = BotCommand.getSlashCommandDataList();

		// Clear guild-specific commands
		event.getGuild().retrieveCommands().queue(commands -> {
			for (Command cmd : commands){
				event.getGuild().deleteCommandById(cmd.getId()).queue();
			}
		});

		for (SlashCommandData cmd : commandData){
			event.getJDA().upsertCommand(cmd).queue();
		}
		event.getGuild().updateCommands().addCommands(commandData).queue();
		if (!Constants.isBugFree()){
			if (event.getGuild().getId().equals(Constants.DEBUGGUILDID)){
				event.getGuild().getTextChannelById(Constants.DEBUGCHANNELID).sendMessage("Bot is ready for testing.").queue();
			}
		}
	}

}
