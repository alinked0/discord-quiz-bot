package com.linked.quizbot.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.entities.MessageEmbed.Footer;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.utils.AttachedFile;
import okio.Buffer;

import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.function.Consumer;

import com.linked.quizbot.Constants;
import com.linked.quizbot.events.ReactionListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;

public class CommandOutput {
	private final List<String> textMessages;
	private final List<MessageEmbed> embeds;
	private final List<Consumer<Message>> postSendActions;
	private final List<File> attachedFiles;
	private final List<Emoji> reactions;
	private final boolean sendInOriginalMessage;
	private final boolean replyToSender;
	private final boolean sendInThread;
	private final boolean ephemeral;
	private final boolean clearReactions;
	private final long delayMillis;
	private boolean useButtons = true;	
	private String userId;
	private Message message;
	
	public static class Builder {
		private final List<String> textMessages = new ArrayList<>();
		private final List<MessageEmbed> embeds = new ArrayList<>();
        private final List<Consumer<Message>> postSendActions = new ArrayList<>();
		private final List<File> attachedFiles= new ArrayList<>();
		private final List<Emoji> reactions= new ArrayList<>();
		private boolean useButtons = true;
		private boolean sendInOriginalMessage = false;
		private boolean replyToSender = true;
		private boolean sendInThread= false;
		private boolean clearReactions= false;
		private String userId = null;
		private boolean ephemeral = false;
		private long delayMillis = 0;
		private Message message=null;
		public Builder addTextMessage(String message){
			if (message !=null && !message.isEmpty()){
				textMessages.add(message);
			}
			return this;
		}
		public Builder useButtons(boolean b){ 
            this.useButtons= b;
            return this;
        }
		public Builder setMessage(Message msg){ message = msg; return this;}
		public Builder addAllTextMessage(List<String> c){
			for (String s : c){
				this.addTextMessage(s);
			}
			return this;
		}
		public Builder addEmbed(EmbedBuilder embedBuilder){
			if (embedBuilder != null){
				embeds.add(embedBuilder.build());
			}
			return this;
		}
		public Builder addEmbed(MessageEmbed embed){
			if (embed != null){
				embeds.add(embed);
			}
			return this;
		}
		public Builder addAllEmbed(List<MessageEmbed> c){
			for (MessageEmbed k : c){
				this.addEmbed(k);
			}
			return this;
		}
		public Builder reply(boolean replyToSender){
			this.replyToSender = replyToSender;
			return this;
		}
		public Builder sendAsPrivateMessage(String userId){
			if (userId != null && !userId.isEmpty() && userId.length()>= Constants.DISCORDIDLENMIN){
				this.sendInOriginalMessage = false;
				this.userId = userId;
			}
			return this;
		}
		public Builder addFile(File file){
			this.attachedFiles.add(file);
			return this;
		}
		public Builder addFile(String filePath){
			this.attachedFiles.add(new File(filePath));
			return this;
		}
		public Builder addAllFile(List<File> files){
			this.attachedFiles.addAll(files);
			return this;
		}
		public Builder addFile(List<String> filePaths){
			for (String s : filePaths){
				addFile(s);
			}
			return this;
		}
		public Builder ephemeral(boolean ephemeral){
			this.ephemeral = ephemeral;
			return this;
		}
		public Builder clearReactions(boolean b){
			this.clearReactions = b;
			return this;
		}
		public Builder addReaction(Emoji e){
			this.reactions.add(e);
			return this;
		}
		public Builder addReactions(List<Emoji> e){
			this.reactions.addAll(e);
			return this;
		}
		public Builder sendInThread(boolean b){
			this.sendInThread = b;
			return this;
		}
		public Builder sendInOriginalMessage(boolean b){
			this.sendInOriginalMessage = b;
			return this;
		}
		public Builder addPostSendAction(Consumer<Message> action) {
			if (action != null) {
				this.postSendActions.add(action);
			}
			return this;
		}
		public Builder setPostSendAction(List<Consumer<Message>> action) {
			this.postSendActions.clear();
			addAllPostSendAction(action);
			return this;
		}
		public Builder addAllPostSendAction(List<Consumer<Message>> action) {
			if (action != null) {
				this.postSendActions.addAll(action);
			}
			return this;
		}
		public Builder delayMillis(long t){
			delayMillis = t;
			return this;
		}
		public Builder add(CommandOutput t) {
			addAllTextMessage(t.textMessages);
			addAllEmbed(t.embeds);
			this.replyToSender = t.replyToSender;
			this.ephemeral = t.ephemeral;
			this.delayMillis = t.delayMillis;
			this.postSendActions.addAll(t.postSendActions);
			this.attachedFiles.addAll(t.attachedFiles);
			this.sendInOriginalMessage = t.sendInOriginalMessage;
			this.sendInThread = t.sendInThread;
			this.userId = t.userId;
			this.message = t.message;
			this.clearReactions = t.clearReactions;
			this.reactions.addAll(t.reactions);
			this.useButtons = t.useButtons;
			return this;
		}
		public CommandOutput build(){
			return new CommandOutput(this);
		}
	}
	private CommandOutput(Builder builder){
		this.textMessages = Collections.unmodifiableList(builder.textMessages);
		this.embeds = Collections.unmodifiableList(builder.embeds);
		this.replyToSender = builder.replyToSender;
		this.ephemeral = builder.ephemeral;
		this.delayMillis = builder.delayMillis;
		this.postSendActions = builder.postSendActions;
		this.attachedFiles = builder.attachedFiles;
		this.sendInOriginalMessage = builder.sendInOriginalMessage;
		this.sendInThread = builder.sendInThread;
		this.userId = builder.userId;
		this.message = builder.message;
		this.clearReactions = builder.clearReactions;
		this.reactions = builder.reactions;
		this.useButtons = builder.useButtons;
	}
	public List<File> getFiles(){
		return attachedFiles;
	}
	public List<Emoji> getReactions(){
		return reactions;
	}
	public boolean useButtons(){ return useButtons;}
	public List<ActionRow> getActionRows(){
		int nbOptions = 0;
		List<Emoji> l = getReactions();
		List<Button> row= new ArrayList<>();
		List<ActionRow> newActionRows = new ArrayList<>();
		int i=0;
		Emoji e;
		for (; nbOptions<l.size(); ++nbOptions){
			e = Emoji.fromUnicode("U+3"+(nbOptions+1)+"U+fe0fU+20e3");
			if (l.get(nbOptions).equals(e)){
				row.add(Button.of(ButtonStyle.PRIMARY, String.format("%s", (nbOptions+1)), e.getFormatted())); //ReactionListener.getCommandFromEmoji(e).getName()
				if(nbOptions+1==5){
					newActionRows.add(ActionRow.of(row));
					row = new ArrayList<>();
				}
			} else {
				break;
			}
		}
		if (!row.isEmpty())newActionRows.add(ActionRow.of(row));
		row = new ArrayList<>();
		for (i=nbOptions; i<l.size(); ++i){
			e = l.get(i);
			BotCommand cmd = ReactionListener.getCommandFromEmoji(e);
			if (cmd!=null){
				String id = cmd.getName();
				row.add(Button.of(ButtonStyle.PRIMARY, id, e.getFormatted()));
				if(row.size()==5){
					newActionRows.add(ActionRow.of(row));
					row = new ArrayList<>();
				}
			}
		}
		if (!row.isEmpty())newActionRows.add(ActionRow.of(row));
		return newActionRows;
	}
	public Message getMessage(){
		return message;
	}
	public boolean clearReactions(){
		return clearReactions;
	}
	public boolean sendInThread(){
		return sendInThread;
	}
	public boolean sendInOriginalMessage(){
		return sendInOriginalMessage;
	}
	public List<String> getTextMessages(){
		return textMessages;
	}
	public String getRequesterId(){
		return userId;
	}
	public List<String> getAsText(){
		List<String> res = new ArrayList<>();
		res.addAll(getTextMessages());
		String s = "";
		Footer footer;
		for (MessageEmbed embed : getEmbeds()) {
			s =String.format("# %s\n## %s\n", embed.getTitle(), embed.getDescription());
			for (Field f : embed.getFields()){
				s+=String.format("- %s\n> %s\n", f.getName(),f.getValue());
			}
			footer = embed.getFooter();
			if (footer!=null)s+= String.format("%s\n", footer.getText());
			res.add(s);
		}
		s = "";
		for (File f : getFiles()){
			try{
				Scanner sc = new Scanner(f);
				while(sc.hasNext()){
					s += sc.nextLine();
				}
				res.add(s);
				s="";
				sc.close();
			} catch(FileNotFoundException e){
				e.printStackTrace();
			}
		}
		return res;
	}
	public List<MessageEmbed> getEmbeds(){
		return embeds;
	}
	public boolean sendAsPrivateMessage(){
		return userId!=null;
	}
	public boolean shouldReplyToSender(){
		return replyToSender;
	}
	public boolean isEphemeral(){
		return ephemeral;
	}
	public long getDelayMillis(){
		return delayMillis;
	}
	public List<Consumer<Message>> getPostSendActions() {
        return postSendActions;
    }
	public String toString(){
		return 
		this.textMessages.toString() +
		this.embeds.toString() +
		this.replyToSender +
		this.ephemeral+
		this.delayMillis+
		this.postSendActions.toString() +
		this.attachedFiles.toString() +
		this.sendInOriginalMessage +
		this.sendInThread +
		(this.userId!=null?this.userId.toString():null) +
		(this.message!=null?this.message.toString():null) +
		this.clearReactions+
		this.reactions.toString();
	}
}
