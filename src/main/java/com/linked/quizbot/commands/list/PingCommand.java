package com.linked.quizbot.commands.list;

import java.util.List;

import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.commands.CommandOutput;
import com.linked.quizbot.core.BotCore;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

public class PingCommand extends BotCommand {
	public static final String CMDNAME = "ping";
	private String cmdDesrciption = "getting comfirmation that the bot is online.";
	private List<String> abbrevs = List.of("p");
	
	public List<String> getAbbreviations(){ return abbrevs;}
	public String getName(){ return CMDNAME;}
	public String getDescription(){ return cmdDesrciption;}
	public CommandOutput execute(String userId, String channelId, List<String> args, boolean reply){
		long start = System.nanoTime();

		// Get the JDA instance from the channel to access gateway ping
		JDA jda = BotCore.getJDA();
		long gatewayPing = 0L;
		if (jda!=null) gatewayPing = jda.getGatewayPing(); // Get the WebSocket latency to Discord

		// Calculate the internal processing time.
		double internalProcessingTimeMs = (System.nanoTime() - start) / 1000000.00;
		// Combine both internal processing time and gateway ping into the response
		String res = String.format("Pong! Internal processing: %.2fms | Gateway Ping: %dms", 
									internalProcessingTimeMs, gatewayPing);
		return new CommandOutput.Builder()
				.addTextMessage(res)
				.reply(reply)
				.build();
	}
}
