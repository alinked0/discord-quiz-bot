package com.linked.quizbot.events;

import com.linked.quizbot.Constants;
import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.commands.CommandCategory;
import com.linked.quizbot.commands.list.CreateListCommand;
import com.linked.quizbot.commands.list.AddListCommand;
import com.linked.quizbot.commands.list.ViewCommand;
import com.linked.quizbot.core.BotCore;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

/**
 * The class MessageReceivedListener will serve as the first layer to any text command
 * that means message commands like !help
 */
public class MessageListener extends ListenerAdapter {
    
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        long start = System.nanoTime();
        // Ignorer les messages des bots
        User sender = event.getAuthor();
        if (sender.isBot()) return;
        
        if (event.isFromGuild() || event.isFromThread()){
            if (!Constants.canIRunThisHere(event.getGuild().getId())){
                return;
            }
        } else if (event.isFromType(ChannelType.PRIVATE)){
            if (Constants.AREWETESTING && !Constants.AUTHORID.equals(sender.getId())){
                return;
            }
        }
        MessageChannel channel = event.getChannel();
        // log User
        BotCore.addUser(sender);
        
        //vefifier si le message contient notre prefixe
        Message message = event.getMessage();
        String content = message.getContentRaw();
        String userCmdName= "";
        String prefixe = BotCore.getPrefixe();
        int cmdPrefixeLen = prefixe.length();
        if (content.length()<=cmdPrefixeLen) {
            if (content.equalsIgnoreCase(prefixe)) {
                userCmdName = "help";
            } else {
                prefixe = Constants.CMDPREFIXE;
                cmdPrefixeLen = prefixe.length();
                if (content.length()<=cmdPrefixeLen) {
                    if (content.equalsIgnoreCase(Constants.CMDPREFIXE)) {
                        userCmdName = "help";
                    } else {
                        return;
                    }
                }
            }
        }
        String h = content.substring(cmdPrefixeLen);
        if (prefixe.equalsIgnoreCase(content.substring(0,cmdPrefixeLen))) {
            String tmp = h.split(" ")[0];
            userCmdName = tmp.length()>=1?tmp:userCmdName;
        } 
        
        // Vérifier si le message correspond a une commande existante, ex: "!embed"
        BotCommand cmd = BotCommand.getCommandByName(userCmdName);
        if(BotCore.isShutingDown()){
            CommandCategory category = cmd.getCategory();
            if(category.equals(CommandCategory.EDITING) || category.equals(CommandCategory.GAME)){
                channel.sendMessage(Constants.UPDATEEXPLANATION).queue();
                return;
            }
        }
        String [] args=null;
        if(cmd == null) {
            System.out.printf("  $> not found : %s;\n",userCmdName);
        } else {
            String command = cmd.getName();
            if (h.length()>userCmdName.length()+1) {
                String argumments = h.substring(userCmdName.length()+1);
                System.out.println("  $> "+userCmdName +"?="+command);
                if (command.equalsIgnoreCase( CreateListCommand.CMDNAME)){ 
                    args = new String[]{argumments};
                }
                if (command.equalsIgnoreCase(AddListCommand.CMDNAME)){
                    args = new String[]{argumments};
                }
                if (command.equalsIgnoreCase(ViewCommand.CMDNAME)){
                    String[] tmp = argumments.split(" ");
                    if (tmp.length>=2) {
                        args = tmp;
                    }
                }
                if (args==null) {
                    args = argumments.split(" ");
                }
            }else { 
                args = new String[0];
            }
            System.out.print("  $> "+content.replace("\n", "").replace("\t", ""));
            System.out.print(" ; args :");
            for (int i=0; i<args.length; i++) { 
                System.out.print(args[i].replace("\n", "").replace("\t", "")+":");
            }
            System.out.println("");
            cmd.execute(sender, message, channel, args);
            System.out.printf("   $> time of "+cmd.getName()+" = `%.3f ms`\n", (System.nanoTime() - start) / 1000000.00);
        }
    }
}


