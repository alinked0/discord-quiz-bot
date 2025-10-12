package com.linked.quizbot.commands.list;

import java.util.List;

import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.commands.CommandOutput;
import com.linked.quizbot.core.BotCore;

import net.dv8tion.jda.api.JDA;

/**
 * The {@code PingCommand} class provides functionality to check the bot's responsiveness
 * by measuring the internal processing time and the WebSocket latency to Discord.
 * It extends {@link BotCommand} and is part of a Discord bot that manages quiz games.
 * <p>
 * This command allows users to verify that the bot is online and responsive, 
 * providing feedback on both internal processing time and gateway ping.
 * </p>
 * @author alinked0
 * @version 1.0
 * @since 2025-02-01
 * @see BotCommand
 */
public class PingCommand extends BotCommand {
	public static final String CMDNAME = "ping";
	private String cmdDesrciption = "getting comfirmation that the bot is online.";
	private List<String> abbrevs = List.of("p");
	
	public List<String> getAbbreviations(){ return abbrevs;}
	public String getName(){ return CMDNAME;}
	public String getDescription(){ return cmdDesrciption;}
	public CommandOutput execute(String userId,  List<String> args){
		long start = System.nanoTime();

		// Get the JDA instance from the channel to access gateway ping
		JDA jda = BotCore.getJDA();
		long gatewayPing = 0L;
		if (jda!=null) gatewayPing = jda.getGatewayPing(); // Get the WebSocket latency to Discord

		// Calculate the internal processing time.
		double internalProcessingTimeMs = (System.nanoTime() - start) / 1000000.00;
		// Combine both internal processing time and gateway ping into the response
		String res = String.format("Pong! Internal processing: %.3fms | Gateway Ping: %dms", 
									internalProcessingTimeMs, gatewayPing);
		return new CommandOutput.Builder()
				.add(res)
				
				.build();
	}
}
