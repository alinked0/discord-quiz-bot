package com.linked.quizbot.commands.list;

import com.linked.quizbot.utils.Option;
import com.linked.quizbot.utils.Question;
import com.linked.quizbot.utils.QuestionList;
import com.linked.quizbot.utils.UserData;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.awt.Color;

import com.linked.quizbot.commands.BotCommand;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.utils.TimeFormat;

/**
 * The {@code EmbedCommand} class is a test command for creating and sending Discord embeds.
 * It retrieves a user's first question list and displays a formatted embed containing a question, 
 * its possible answers, and a countdown timer.
 *
 * <h2>Usage:</h2>
 * <ul>
 *     <li>When executed, the bot retrieves the first question from the user's saved lists.</li>
 *     <li>An embed is created with the question, its options, and a 30-second expiration time.</li>
 *     <li>The bot adds numbered emoji reactions (e.g., 1️⃣, 2️⃣, 3️⃣) for user interaction.</li>
 *     <li>The message auto-deletes after 30 seconds.</li>
 * </ul>
 *
 * <h2>Features:</h2>
 * <ul>
 *     <li>Uses {@link EmbedBuilder} to format the question and answers.</li>
 *     <li>Retrieves question data from {@link UserData} and {@link QuestionList}.</li>
 *     <li>Includes a timestamp and a themed description.</li>
 *     <li>Adds emoji reactions based on the number of available answer options.</li>
 *     <li>Deletes the message after 30 seconds to prevent clutter.</li>
 * </ul>
 *
 * <h2>Example:</h2>
 * <pre>
 * // Bot generates an embedded question
 * [Question: What is the capital of France?]
 * 1️⃣ Paris
 * 2️⃣ Berlin
 * 3️⃣ Madrid
 * 
 * // Users react with an emoji to select an answer.
 * // The embed disappears after 30 seconds.
 * </pre>
 *
 * @author alinked0
 * @version 1.0
 * @see BotCommand
 * @see EmbedBuilder
 * @see UserData
 * @see QuestionList
 * @see Question
 * @see Option
 */
public class EmbedCommand extends BotCommand {
    public static final String CMDNAME = "embed";
    private String cmdDesrciption = "old test for printing questions using discord embeds";
	private String[] abbrevs = new String[0];
    
	@Override
	public String[] getAbbreviations(){ return abbrevs;}
	@Override
    public String getName(){ return CMDNAME;}
	@Override
    public String getDescription(){ return cmdDesrciption;}
    @Override
    public void execute(User sender, Message message, MessageChannel channel, String[] args){
        //Liste de l'utilisateur
        UserData UserData = new UserData(sender.getId());
        QuestionList list = UserData.get(0);
        Question question = list.get(0);
        // Créer l'embed
        EmbedBuilder embed = new EmbedBuilder()
                    .setTitle(question.getQuestion())
                    //.setDescription(list.getTheme())
                    .setFooter("Java Bot Server Test")
                    .setAuthor("Java Bot Server Test")
                    .setTimestamp(java.time.Instant.now());

        int i = 1;
        List<Option> listOpts = question.getOptions();
        for(Option opt : listOpts) {
            embed.addField(""+i, opt.getText(), true);
            i++;
        }
        // Ajout d'un temps de 30 sec qui s'écoule
        embed.addField("time: ","" + TimeFormat.RELATIVE.after(30*1000) , false);
    
        embed.setColor(Color.RED);

        // Envoyer l'embed et ajouter une réaction ✨, "😀", "😅"
        channel.sendMessageEmbeds(embed.build())
        .queue(msg -> {
                for (int j=1; j<=question.size(); j++) {
                    msg.addReaction(Emoji.fromUnicode("U+3"+j+"U+fe0fU+20e3")).queue();
                }
                msg.delete().queueAfter(30, TimeUnit.SECONDS);
            }
        );
    }
}
