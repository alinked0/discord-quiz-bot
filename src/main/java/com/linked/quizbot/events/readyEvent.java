package com.linked.quizbot.events;

import java.util.ArrayList;
import java.util.List;

import com.linked.quizbot.Constants;
import com.linked.quizbot.commands.BotCommand;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class readyEvent extends ListenerAdapter {
    @Override
	public void onGenericEvent(GenericEvent event){
		if (Constants.AREWETESTING){
			System.out.println(" $> - "+event);
		}
	}

	// @Override
	// public void onReadyEvent(ReadyEvent event){
	// 	List<SlashCommandData> commandData = BotCommand.getSlashCommandDataList();
	// 	event.getJDA().updateCommands().addCommands(commandData).queue();
	// }
	
	@Override
	public void onGuildReady(GuildReadyEvent event){
		if (!Constants.canIRunThisHere(event.getGuild().getId())){
			return;
		}
        if(Constants.AREWETESTING && Constants.DEBUGGUILDID.equals(event.getGuild().getId())){
            TextChannel c = event.getJDA().getTextChannelById(Constants.DEBUGCHANNELID);
            if (c!=null) c.sendMessage("Bot is Ready for testing.").queue();
        }
		List<SlashCommandData> commandData = BotCommand.getSlashCommandDataList();
		event.getGuild().updateCommands().addCommands(commandData).queue();
	}
}
