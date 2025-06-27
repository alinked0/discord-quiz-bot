package com.linked.quizbot.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.entities.MessageEmbed.Footer;
import net.dv8tion.jda.api.utils.AttachedFile;
import okio.Buffer;

import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.function.Consumer;

import com.linked.quizbot.Constants;

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
	private final boolean editResponseIntoOriginalMessage;
	private final boolean replyToSender;
	private boolean ephemeral;
	private boolean sendAsPrivateMessage;
	private final long delayMillis;

	public static class Builder {
		private final List<String> textMessages = new ArrayList<>();
		private final List<MessageEmbed> embeds = new ArrayList<>();
        private final List<Consumer<Message>> postSendActions = new ArrayList<>();
		private final List<File> attachedFiles= new ArrayList<>();
		private boolean editResponseIntoOriginalMessage = false;
		private boolean replyToSender = true;
		private boolean sendAsPrivateMessage = false;
		private boolean ephemeral = false;
		private long delayMillis = 0;

		public Builder addTextMessage(String message){
			if (message !=null && !message.isEmpty()){
				textMessages.add(message);
			}
			return this;
		}
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
		public Builder sendAsPrivateMessage(boolean sendAsPrivateMessage){
			this.sendAsPrivateMessage = sendAsPrivateMessage;
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
		public Builder editResponseIntoOriginalMessage(boolean b){
			this.editResponseIntoOriginalMessage = b;
			return this;
		}
		public Builder addPostSendAction(Consumer<Message> action) {
			if (action != null) {
				this.postSendActions.add(action);
			}
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
		public Builder addCommandOutput(CommandOutput t) {
			addAllTextMessage(t.textMessages);
			addAllEmbed(t.embeds);
			ephemeral(t.ephemeral);
			addAllPostSendAction(t.postSendActions);
			addAllFile(t.attachedFiles);
			this.delayMillis = t.delayMillis;
			this.sendAsPrivateMessage = t.sendAsPrivateMessage;
			this.editResponseIntoOriginalMessage = t.editResponseIntoOriginalMessage;
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
		this.sendAsPrivateMessage = builder.sendAsPrivateMessage;
		this.editResponseIntoOriginalMessage = builder.editResponseIntoOriginalMessage;
	}
	public List<File> getFiles(){
		return attachedFiles;
	}
	public boolean editOriginalMessage(){
		return editResponseIntoOriginalMessage;
	}
	public List<String> getTextMessages(){
		return textMessages;
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
	public boolean shouldSendAsPrivateMessage(){
		return sendAsPrivateMessage;
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
}
