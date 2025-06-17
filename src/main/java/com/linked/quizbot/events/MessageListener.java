package com.linked.quizbot.events;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import com.linked.quizbot.Constants;
import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.commands.CommandCategory;
import com.linked.quizbot.commands.list.CreateListCommand;
import com.linked.quizbot.commands.list.HelpCommand;
import com.linked.quizbot.commands.list.AddListCommand;
import com.linked.quizbot.commands.list.ViewCommand;
import com.linked.quizbot.core.BotCore;
import com.linked.quizbot.utils.Users;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.ChannelType;
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
        String userPrefixe = Users.get(sender.getId()).getPrefix();
        if (content.length() < userPrefixe.length() && content.length() < Constants.CMDPREFIXE.length()){return ;}
        int cmdPrefixeLen;
        if (userPrefixe!=null){
            if (userPrefixe.length()<Constants.CMDPREFIXE.length() && content.substring(0, Constants.CMDPREFIXE.length()).equals(Constants.CMDPREFIXE)){
                prefixe = Constants.CMDPREFIXE;
            }else if (userPrefixe.length()<=content.length() && content.substring(0, userPrefixe.length()).equals(userPrefixe)){
                prefixe = userPrefixe;
            }
        }
        cmdPrefixeLen = prefixe.length();
        String h = content.substring(cmdPrefixeLen);
        if (content.length()<=cmdPrefixeLen) {
            userCmdName = HelpCommand.CMDNAME;
        } else if (prefixe.equalsIgnoreCase(content.substring(0,cmdPrefixeLen))) {
            String tmp = h.split(" ")[0];
            userCmdName = tmp;
        }
        
        // Vérifier si le message correspond a une commande existante, ex: "!embed"
        BotCommand cmd = BotCommand.getCommandByName(userCmdName);
        if(cmd == null) {
            if (!Constants.isBugFree())System.out.printf("  $> not found : %s;\n",userCmdName);
            return;
        }
        if(BotCore.isShutingDown()){
            CommandCategory category = cmd.getCategory();
            if(category.equals(CommandCategory.EDITING) || category.equals(CommandCategory.GAME)){
                channel.sendMessage(Constants.UPDATEEXPLANATION).queue();
                return;
            }
        }
        List<String> args=new ArrayList<>();
        String command = cmd.getName();
        String cmndLineArgs = "";
        if (h.length()>=userCmdName.length()+1) {
            cmndLineArgs = h.substring(userCmdName.length()+1);
            System.out.println("  $> "+userCmdName +"?="+command);
        }
        switch (command) {
            case CreateListCommand.CMDNAME, AddListCommand.CMDNAME -> {
                args.addAll(split(cmndLineArgs));
                String userId = sender.getId();
                for (Attachment att : message.getAttachments()){
                    String tmpStr = "";
                    try {
                        URL website = new URL(att.getUrl());
                        String path = Constants.LISTSPATH+Constants.SEPARATOR+userId+Constants.SEPARATOR+"tmp";
                        File f = new File(path);
                        ReadableByteChannel rbc = Channels.newChannel(website.openStream());
                        FileOutputStream fos = new FileOutputStream(f);
                        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                        if(!f.getParentFile().exists()) {
                            f.getParentFile().mkdirs();
                        }
                        BufferedReader fd = Files.newBufferedReader(f.toPath());

                        String k = "";
                        do {
                            tmpStr+= k;
                            k=fd.readLine();
                        }while(k!=null);
                        fd.close();
                        f.delete();
                        fos.close();
                        if (!tmpStr.isEmpty()){
                            args.addAll(split(tmpStr));
                        }
                    } catch (IOException e) {
                        System.err.println(" $> An error occurred while taking an attachment.");
                        e.printStackTrace();
                    }
                }
            }
            default->{
                args = List.of(cmndLineArgs.split(" "));
            }
        }
        if (!Constants.isBugFree()) {
            System.out.print("  $> "+content.replace("\n", "").replace("\t", ""));
            System.out.print(" ; args :");
            for (int i=0; i<args.size(); i++) { 
                System.out.print(args.get(i).replace("\n", "").replace("\t", "")+":");
            }
        }
        cmd.execute(sender, message, channel, args);
        if (!Constants.isBugFree()) System.out.printf("   $> time of "+cmd.getName()+" = `%.3f ms`\n", (System.nanoTime() - start) / 1000000.00);
    }
    private List<String> split(String argumment){
        int k = 0;
        int start = -1;
        List<String> res = new ArrayList<>();
        String[] l = argumment.split("");
        int i=0;
        for (i = 0; i<argumment.length(); i++){
            if (l[i].equals("{")){
                start = i;
                break;
            }
        }
        if (start==-1){return res;}
        for (; i<argumment.length(); i++){
            if (l[i].equals("{")){k+=1;}
            else if (l[i].equals("}")){k-=1;}
            if (k==0){
                res.add(argumment.substring(start, i+1));
                start = i+1;
                if (i+1<argumment.length()) res.addAll(split(argumment.substring(start)));
                return res;
            }
        }
        return res;
    }
}


