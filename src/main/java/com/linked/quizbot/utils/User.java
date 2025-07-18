package com.linked.quizbot.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.function.Consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linked.quizbot.Constants;
import com.linked.quizbot.commands.CommandOutput;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.utils.AttachedFile;

public class User implements Iterable<QuestionList>{
	private String userId;
	private String perferedPrefix;
	protected List<QuestionList> listsSortedById;
	private int numberOfGamesPlayed;
	private double totalPointsEverGained;
	private boolean useButtons;
	private Map<String, Emoji> tagEmojiPerTagName;
	private Map<String, List<QuestionList>> questionListPerTags;

	public static class Builder {
        private String userId=null;
        private String perferedPrefix = null;
        protected List<QuestionList> listsSortedById = new ArrayList<>();
		private boolean useButtons = true;	
        private int numberOfGamesPlayed=0;
        private double totalPointsEverGained=0;
        private Map<String, Emoji> tagEmojiPerTagName= new HashMap<>();
        private Map<String, List<QuestionList>> questionListPerTags= new HashMap<>();
		public Builder userId(String userId){ 
            this.userId = userId;
            return this;
        }  
		public Builder perferedPrefixe(String prefixe){ 
            this.perferedPrefix= prefixe;
            return this;
        }
		public Builder useButtons(boolean b){ 
            this.useButtons= b;
            return this;
        }
		public Builder numberOfGamesPlayed(int n){ 
			this.numberOfGamesPlayed=n;
            return this;
        }
		public Builder totalPointsEverGained(double points){ 
            this.totalPointsEverGained = points;
            return this;
        }
		public Builder tagEmojiPerTagName(Map<String, Emoji> tagEmojiPerTagName){
            this.tagEmojiPerTagName = tagEmojiPerTagName;
            return this;
        }
		public  Builder addTag(String tagName, Emoji emoji){
			this.tagEmojiPerTagName.put(tagName, emoji);
			return this;
		}
		public  Builder addTags(Map<String,Emoji> tags){
			this.tagEmojiPerTagName.putAll(tags);
			return this;
		}
		public Builder questionListPerTags(Map<String, List<QuestionList>> questionListPerTags){
            this.questionListPerTags = questionListPerTags;
            return this;
        }
		public Builder listsSortedById(List<QuestionList> listsSortedById){
            this.listsSortedById = new ArrayList<>(listsSortedById);
            return this;
        }
		public Builder add(QuestionList l){
			this.listsSortedById.add(l);
            return this;
        }
		public Builder addAll(List<QuestionList> c){
			this.listsSortedById.addAll(c);
            return this;
        }
		public User build(){
			return new User(this);
		}
	}

	public User(User.Builder builder){
		this.userId = builder.userId;
        this.perferedPrefix = builder.perferedPrefix;
		this.numberOfGamesPlayed = builder.numberOfGamesPlayed;
		this.totalPointsEverGained = builder.totalPointsEverGained;
		this.tagEmojiPerTagName = builder.tagEmojiPerTagName;
		this.questionListPerTags = builder.questionListPerTags;
		this.listsSortedById = builder.listsSortedById;
		this.useButtons = builder.useButtons;
		File folder = new File(Constants.LISTSPATH+Constants.SEPARATOR+ userId);
		File[] listOfFiles = folder.listFiles();
		if(listOfFiles != null) {
			for (int i = 0; i < listOfFiles.length; i++) {
				if (List.of("user-data.json", "tmp").contains(listOfFiles[i].getName())) continue;
				try{
					QuestionList l = QuestionList.importListQuestionFromJson(listOfFiles[i].getAbsolutePath());
                    if (!listsSortedById.contains(l)){
						listsSortedById.add(l);
                    }
				}catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		listsSortedById.sort(QuestionList.comparatorById());;
	}
	public User(String userId){
		this(new User.Builder());
		this.userId = userId;
		listsSortedById = getUserListQuestions(userId);
		initStats();
		initTags();
	}
	private void initTags(){
		Map<String, Emoji> tags;
		listsSortedById.sort(QuestionList.comparatorById());
		for(QuestionList l: listsSortedById){
			QuestionListHash.addGeneratedCode(l.getId());
			tags = l.getTags();
			tagEmojiPerTagName.putAll(tags);
			for (String tagName : tags.keySet()){
				if (questionListPerTags.getOrDefault(tagName, null)==null){
					questionListPerTags.put(tagName, new ArrayList<QuestionList>());
				}
				questionListPerTags.get(tagName).add(l);
			}
		}
        Users.addUser(this);
	}
	public void initStats(){
		try{
			User tmp = UserDataParser.fromJsonFile(getPathToUserData());
			perferedPrefix = tmp.getPrefix();
			numberOfGamesPlayed = tmp.getNumberOfGamesPlayed();
			totalPointsEverGained = tmp.getTotalPointsEverGained();
			tagEmojiPerTagName = tmp.getTagEmojiPerTagName();
		} catch (IOException e) {
			numberOfGamesPlayed = 0;
			totalPointsEverGained = 0;
			tagEmojiPerTagName = new HashMap<>();
		}
	}
	public boolean useButtons(){
		return useButtons;
	}
	public void useButtons(boolean b){ useButtons = b;}
	public String getPrefix(){
		return perferedPrefix;
	}
	public String getPreferredPrefix(){
		return getPrefix();
	}
	public void setPrefix(String perferedPrefix){
		this.perferedPrefix= perferedPrefix;
		exportUserData();
	}
	public List<QuestionList> getLists() {
		List<QuestionList> res = new ArrayList<>(listsSortedById);
		return res;
	}
	public String getUserId(){ return userId;}

	public String getPathToUserData(){
		return Constants.USERDATAPATH+Constants.SEPARATOR+getUserId()+Constants.SEPARATOR+"user-data.json";
	}
	public QuestionList get(int index) {
		return listsSortedById.get(index);
	}
	public static String getCodeForQuestionListId(QuestionList l){
		String id = l.getId();
		if (id==null || id.length()<Constants.DISCORDIDLENMIN){
			id = QuestionListHash.generate(l);
		}
		return id;
	}
	public double getTotalPointsEverGained(){
		return totalPointsEverGained;
	}
	public int getNumberOfGamesPlayed(){
		return numberOfGamesPlayed;
	}
	public void incrTotalPointsEverGained(double numberOfPointsGained){
		totalPointsEverGained+=numberOfPointsGained;
		this.exportUserData();
	}
	public void incrNumberOfGamesPlayed(){
		numberOfGamesPlayed+=1;
		this.exportUserData();
	}
	public Map<String, Emoji> getTagEmojiPerTagName() {
		return new HashMap<>(tagEmojiPerTagName);
	}
	public Map<String, List<QuestionList>> getQuestionListPerTags() {
		return new HashMap<>(questionListPerTags);
	}
	public List<QuestionList> getListsByTag(String tagName) {
		List<QuestionList> res = questionListPerTags.get(tagName);
		if (res == null) {
			return new ArrayList<>();
		}
		return new ArrayList<>(res);
	}
	public Set<String> getTags() {
		return new HashSet<>(tagEmojiPerTagName.keySet());
	}

	public List<QuestionList> getQuestionListsByTag(String tagName){
		List<QuestionList> res = questionListPerTags.get(tagName);
		if (res == null){
			return new ArrayList<>();
		}
		return new ArrayList<>(res);
	}
	public QuestionList getById(String id) {
		QuestionList res= null;
		if (QuestionList.getExampleQuestionList().getId().equals(id)){
			res =  QuestionList.getExampleQuestionList();
		} else {
			QuestionList searched = new QuestionList.Builder().id(id).build();
			int i=-1;
			List<QuestionList> l=getLists();
			i = Users.myBinarySearchIndexOf(l, searched, QuestionList.comparatorById());
			if (i>=0){
				res = l.get(i);
			}
		}
		return res;
	}
	public QuestionList getByName(String listName){
		List<QuestionList> listsSortedByName = new ArrayList<>(listsSortedById);
		listsSortedByName.sort(QuestionList.comparatorByName());
		int index = QuestionList.myBinarySearchIndexOf(listsSortedByName, listName);
		if (index<0) return null;
		return listsSortedByName.get(index);
	}
	public static QuestionList getQuestionListByName(String listName){
		return Users.getQuestionListByName(listName);
	}
	public Emoji getEmojiFomTagName(String tagName){
		return tagEmojiPerTagName.getOrDefault(tagName, null);
	}
	public static Emoji getEmojiFomTagName(String userId, String tagName){
		return Users.getUser(userId).getEmojiFomTagName(tagName);
	}
	public void setNumberOfGamesPlayed(int number) {
		this.numberOfGamesPlayed = number;
	}
	public void setTotalPointsEverGained(double points) {
		this.totalPointsEverGained = points;
	}
	public void setTagEmojiPerTagName(Map<String, Emoji> m) {
		this.tagEmojiPerTagName = m;
	}
	public static void addListToUser(String userId, QuestionList l) {
		Users.addListToUser(userId, l);
	}
	public boolean addList(@NotNull QuestionList l){
		int index;
		QuestionList k = getByName(l.getName());
		if (k==null){
			k = l;
		} else {
			k.addAll(l);
		}
		index = myBinarySearchIndexOf(listsSortedById, k, QuestionList.comparatorById());
		if (index>=0) {
			listsSortedById.set(index, k);
		} else{
			listsSortedById.add(index*-1 -1,k);
		}
		k.exportListQuestionAsJson();
		Users.update(this);
		return true;
	}
	public boolean createTag(@NotNull String tagName, @NotNull Emoji emoji) {
		if (tagEmojiPerTagName.containsKey(tagName)) {
			return false; // Tag already exists
		}
		tagEmojiPerTagName.put(tagName, emoji);
		questionListPerTags.put(tagName, new ArrayList<>());
		exportUserData();
		return true;
	}
	public boolean addTagToQuestionList(String tagName, Emoji emoji, String id) {
		if (!tagEmojiPerTagName.containsKey(tagName)) {
			return false; // Tag hasnt been created
		}
		getById(id).addTag(tagName, emoji);
		exportUserData();
		Users.update(this);
		return true;
	}
	public static boolean createTag(String userId, String tagName, Emoji emoji) {
		return Users.createTag(userId, tagName, emoji);
	}
	public boolean deleteTag(String tagName) {
		if (!tagEmojiPerTagName.containsKey(tagName)) {
			return false; // Tag does not exist
		}
		tagEmojiPerTagName.remove(tagName);
		questionListPerTags.remove(tagName);
		exportUserData();
		return true;
	}
	public static boolean deleteTag(String userId, String tagName) {
		return Users.deleteTag(userId, tagName);
	}
	public boolean renameList(QuestionList l, String newName){
		QuestionList k = this.getByName(newName);
		if (k!=null){
			return false;
		}
		int oldIndex = myBinarySearchIndexOf(getLists(), l, QuestionList.comparatorById());
		listsSortedById.remove(oldIndex);
		l.setName(newName);
		return addList(l);
	}
	public boolean addTagToList(QuestionList l, String tagName) {
		int index = myBinarySearchIndexOf(getLists(), l, QuestionList.comparatorById());
		Emoji emoji;
		List<QuestionList> listsTagged;
		if (index >= 0) {
			emoji = tagEmojiPerTagName.getOrDefault(tagName,null);
			if (emoji != null) {
				l = get(index);
				l.addTag(tagName, emoji);
				l.exportListQuestionAsJson();
				listsTagged = questionListPerTags.get(tagName);
				if (listsTagged==null){
					listsTagged = new ArrayList<>();
				}
				listsTagged.add(l);
				questionListPerTags.put(tagName, listsTagged);
				listsSortedById.set(index, l);
				exportUserData();
				return true;
			}
		}
		return false;
	}
	public static boolean addTagToList(String id, String tagName) {
		return Users.addTagToList(id, tagName);
	}
	
	public boolean removeTagFromList(QuestionList l, String tagName) {
		if (!l.getTags().containsKey(tagName)) {
			return false; // Tag does not exist in the list
		}
		l.removeTag(tagName);
		l.exportListQuestionAsJson();
		tagEmojiPerTagName.remove(tagName);
		questionListPerTags.remove(tagName);
		exportUserData();
		return true;
	}
	public static boolean removeTagFromList(String id, String tagName) {
		return Users.removeTagFromList(id, tagName);
	}
	public static void deleteList(QuestionList l){
		User user = new User(l.getAuthorId());
		user.listsSortedById.remove(l);
		File f = new File(l.getPathToList());
		f.delete();
		Users.allUsers.remove(user);
		Users.allUsers.add(user);
	}
	public static <T> int myBinarySearchIndexOf(List<T> tab, int start, int end, T q, Comparator<? super T> compare){
		if (start > end){
			return -1*start-1;
		}
		int m = (start+end)/2;
		int comp = compare.compare(tab.get(m), q);
		if(comp == 0){
			return m;
		}
		if (comp >0){
			return myBinarySearchIndexOf(tab, start, m-1, q, compare);
		}
		return myBinarySearchIndexOf(tab, m+1, end, q, compare);
	}
	public static <T> int myBinarySearchIndexOf(List<T> tab, T q, Comparator<? super T> compare){
		return myBinarySearchIndexOf(tab, 0, tab.size()-1, q, compare);
	}
	public static int myBinarySearchUserId(List<User> tab, int start, int end, String userId){
		if (start > end){
			return -1*start-1;
		}
		int m = (start+end)/2;
		int comp = tab.get(m).getUserId().compareTo(userId);
		if(comp == 0){
			return m;
		}
		if (comp >0){
			return myBinarySearchUserId(tab, start, m-1, userId);
		}
		return myBinarySearchUserId(tab, m+1, end, userId);
	}
	public static int myBinarySearchUserId(List<User> tab, String userId){
		return myBinarySearchUserId(tab, 0, tab.size()-1, userId);
	}
	@Override
	public Iterator<QuestionList> iterator(){
		return getLists().iterator();
	}
	public static Comparator<? super User> comparatorByUserId() {
		return (e, f)->(e.getUserId().compareTo(f.getUserId()));
	}
	public static List<QuestionList> getUserListQuestions(String userId) {
		return Users.getUserListQuestions(userId);
	}
	public void exportUserLists() {
		List<QuestionList> listsSortedById = getLists();
		for (QuestionList l : listsSortedById) {
			l.exportListQuestionAsJson();
		}
	}
	@Override
	public int hashCode(){
		return getUserId().hashCode();
	}
	@Override
	public boolean equals(Object o){
		if (this == o) {return true;}
		if(o instanceof  User) {
			User u = (User) o;
			return getUserId().equals(u.getUserId());
		}
		if(o instanceof  String) {
			String u = (String) o;
			return getUserId().equals(u);
		}
		return false;
	}
	public String toJsonUsingMapper() throws JsonProcessingException{
		String res="", 
			tab1 = "\t";
			res += "{\n";
		ObjectMapper mapper = new ObjectMapper();
		res +=tab1+"\"tagEmojiPerTagName\":{";
		Iterator<Entry<String, Emoji>> iter = tagEmojiPerTagName.entrySet().iterator();
		Entry<String, Emoji> entry2;
		while (iter.hasNext()) {
			entry2 = iter.next();
			res += mapper.writeValueAsString(entry2.getKey())+":"+mapper.writeValueAsString(entry2.getValue().getFormatted());
			if(iter.hasNext()){
				res += ", ";
			}
		}
		res += "},\n";
		res += tab1+"\"prefixe\":"+mapper.writeValueAsString(getPrefix())+",\n";
		res += tab1+"\"useButtons\":"+useButtons()+",\n";
		res += tab1+"\"totalPointsEverGained\":"+getTotalPointsEverGained()+",\n";
		res += tab1+"\"numberOfGamesPlayed\":"+getNumberOfGamesPlayed()+"\n";
		res +="}";
		return res;
	}
	public String toJson(){
		String res="";
		try {
			res = toJsonUsingMapper();
			return res;
		} catch (Exception e){
			System.err.println("[toJsonUsingMapper() failed]"+e.getMessage());
		}
		String tab1 = "\t";
		res = "{\n";
		res +=tab1+"\"tagEmojiPerTagName\":{";
		Iterator<Entry<String, Emoji>> iter = tagEmojiPerTagName.entrySet().iterator();
		Entry<String, Emoji> entry2;
		while (iter.hasNext()) {
			entry2 = iter.next();
			res += "\""+entry2.getKey()+"\":\""+entry2.getValue().getFormatted()+"\"";
			if(iter.hasNext()){
				res += ", ";
			}
		}
		res += "},\n";
		res += tab1+"\"prefixe\":\""+getPrefix()+"\",\n";
		res += tab1+"\"useButtons\":"+useButtons()+",\n";
		res += tab1+"\"totalPointsEverGained\":"+getTotalPointsEverGained()+",\n";
		res += tab1+"\"numberOfGamesPlayed\":"+getNumberOfGamesPlayed()+"\n";
		res +="}";
		return res;
	}
	public void exportUserData(){
		String destFilePath = getPathToUserData();
		try {
			File myJson = new File(destFilePath);
			File folder = myJson.getParentFile();
			if(folder != null && !myJson.getParentFile().exists()) {
				folder.mkdirs();
			}
			BufferedWriter buff = Files.newBufferedWriter(Paths.get(destFilePath));
			buff.write(this.toString());
			buff.close();
		} catch (IOException e) {
			System.err.println("$> An error occurred while exporting UserData."+destFilePath);
			e.printStackTrace();
		}
	}
	@Override
	public String toString() {
		String res = toJson();
		return res;
	}
}
