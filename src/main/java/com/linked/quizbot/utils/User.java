package com.linked.quizbot.utils;

import java.io.BufferedWriter;
import java.io.File;
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
import java.util.Set;

import org.jetbrains.annotations.NotNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linked.quizbot.Constants;

import net.dv8tion.jda.api.entities.emoji.Emoji;

public class User implements Iterable<QuestionList>{
	private final String userId;
	private final List<QuestionList> listsSortedById= new ArrayList<>();
	private  final Map<String, Emoji> tagEmojiPerTagName= new HashMap<>();
	private final Map<String, List<QuestionList>> questionListPerTags= new HashMap<>();
	private String prefixe;
	private int numberOfGamesPlayed;
	private double totalPointsEverGained;
	private boolean useButtons;
	private boolean useAutoNext;

	public static class Builder {
        private String userId = null;
        private String prefixe = null;
        protected List<QuestionList> list = new ArrayList<>();
		private boolean useButtons = true;	
		private boolean useAutoNext = false;
        private int numberOfGamesPlayed=0;
        private double totalPointsEverGained=0;
        private Map<String, Emoji> tagEmojiPerTagName= new HashMap<>();
        private Map<String, List<QuestionList>> questionListPerTags= new HashMap<>();
		public Builder id(String userId){ 
			if (userId == null){
				throw new NullPointerException();
			}
            this.userId = userId;
            return this;
        }  
		public Builder prefixe(String prefixe){ 
            this.prefixe= prefixe;
            return this;
        }
		public Builder useButtons(boolean b){ 
            this.useButtons= b;
            return this;
        }
		public Builder useAutoNext(boolean b){ 
            this.useAutoNext= b;
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
            this.list = new ArrayList<>(listsSortedById);
            return this;
        }
		public Builder add(QuestionList l){
			this.list.add(l);
            return this;
        }
		public Builder addAll(List<QuestionList> c){
			this.list.addAll(c);
            return this;
        }
		public User build(){
			return new User(this);
		}
	}

	public User(User.Builder builder){
		if (builder.userId == null){
			throw new NullPointerException();
		}
		this.userId = builder.userId;
        this.prefixe = builder.prefixe;
		this.numberOfGamesPlayed = builder.numberOfGamesPlayed;
		this.totalPointsEverGained = builder.totalPointsEverGained;
		this.tagEmojiPerTagName.putAll(builder.tagEmojiPerTagName);
		this.questionListPerTags.putAll(builder.questionListPerTags);
		this.listsSortedById.addAll(builder.list);
		this.useButtons = builder.useButtons;
		this.useAutoNext = builder.useAutoNext;
	}
	public void syncWithLocal() {
		Map<String, Emoji> tags;
		for(QuestionList l: listsSortedById){
			QuestionListHash.addGeneratedCode(l.getId());
		}
		listsSortedById.sort(QuestionList.comparatorById());
		if (new File(getPathToUserData()).exists()){
			try {
				User.Builder builder = UserDataParser.fromJsonFile(getPathToUserData());
				this.prefixe = builder.prefixe;
				this.numberOfGamesPlayed = builder.numberOfGamesPlayed;
				this.totalPointsEverGained = builder.totalPointsEverGained;
				this.tagEmojiPerTagName.putAll(builder.tagEmojiPerTagName);
				this.questionListPerTags.putAll(builder.questionListPerTags);
				this.useButtons = builder.useButtons;
				this.useAutoNext = builder.useAutoNext;
			} catch (IOException e){
				e.printStackTrace();
			}
		}
		for(QuestionList l: listsSortedById){
			tags = l.getTags();
			tagEmojiPerTagName.putAll(tags);
			for (String tagName : tags.keySet()){
				if (questionListPerTags.get(tagName)==null){
					questionListPerTags.put(tagName, new ArrayList<QuestionList>());
				}
				questionListPerTags.get(tagName).add(l);
			}
		}
	}
	public User(@NotNull String userId){
		this(new User.Builder().id(userId));
		listsSortedById.addAll(importUserLists());
		syncWithLocal();
	}
	public boolean useButtons(){return useButtons;}
	public void useButtons(boolean b){ useButtons = b;}
	public boolean useAutoNext(){return useAutoNext;}
	public void useAutoNext(boolean b){ useAutoNext = b;}
	public String getPrefix(){
		return prefixe;
	}
	public String getPreferredPrefix(){
		return getPrefix();
	}
	public void setPrefix(String prefixe){
		this.prefixe= prefixe;
		exportUserData();
	}
	public List<QuestionList> getLists() {
		List<QuestionList> res = new ArrayList<>(listsSortedById);
		return res;
	}
	public String getId(){ return userId;}

	public String getPathToUserData(){
		return Constants.USERDATAPATH+Constants.SEPARATOR+getId()+Constants.SEPARATOR+"user-data.json";
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
		QuestionList res;
		if (QuestionList.getExampleQuestionList().getId().equals(id)){
			res =  QuestionList.getExampleQuestionList();
			return res.clone();
		} else {
			QuestionList searched = new QuestionList.Builder().id(id).build();
			int i=-1;
			List<QuestionList> l=getLists();
			i = Users.myBinarySearchIndexOf(l, searched, QuestionList.comparatorById());
			if (i>=0){
				res = l.get(i);
				return res.clone();
			}
		}
		return null;
	}
	public QuestionList getByName(String listName){
		List<QuestionList> listsSortedByName = new ArrayList<>(listsSortedById);
		listsSortedByName.sort(QuestionList.comparatorByName());
		int index = QuestionList.myBinarySearchIndexOf(listsSortedByName, listName);
		if (index<0) return null;
		return listsSortedByName.get(index).clone();
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
	public static void addListToUser(String userId, QuestionList l) {
		Users.addListToUser(userId, l);
	}
	public boolean addList(@NotNull QuestionList l){
		int index;
		QuestionList k = getById(l.getId());
		if (k==null){
			k = getByName(l.getId());
			if (k==null){
				k = l;
			} 
		}
		k.addAll(l);
		k.rearrageOptions((e, f) -> e.isCorrect()?-1:1);
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
	public void deleteList(QuestionList l){
		if(l.getAuthorId()!= getId()){
			return;
		}
		this.listsSortedById.remove(l);
		File f = new File(l.getPathToList());
		f.delete();
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
		int comp = tab.get(m).getId().compareTo(userId);
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
		return (e, f)->(e.getId().compareTo(f.getId()));
	}
	public List<QuestionList> importUserLists() {
		List<QuestionList> res = new ArrayList<>();
		File folder = new File(Constants.LISTSPATH+Constants.SEPARATOR+ userId+Constants.SEPARATOR);
		File[] listOfFiles = folder.listFiles();
		if(listOfFiles != null) {
			for (int i = 0; i < listOfFiles.length; i++) {
				if (List.of("tmp").contains(listOfFiles[i].getName())) continue;
				try{
					QuestionList l = QuestionList.importListQuestionFromJson(listOfFiles[i].getAbsolutePath());
					res.add(l);
				}catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return res;
	}
	public void exportUserLists() {
		List<QuestionList> listsSortedById = getLists();
		for (QuestionList l : listsSortedById) {
			l.exportListQuestionAsJson();
		}
	}
	@Override
	public int hashCode(){
		return getId().hashCode();
	}
	@Override
	public boolean equals(Object o){
		if (this == o) {return true;}
		if(o instanceof  User) {
			User u = (User) o;
			return getId().equals(u.getId());
		}
		if(o instanceof  String) {
			String u = (String) o;
			return getId().equals(u);
		}
		return false;
	}
	public String toJsonUsingMapper() throws JsonProcessingException{
		String res="", 
			tab1 = "\t";
			res += "{\n";
		ObjectMapper mapper = new ObjectMapper();
		res += tab1+mapper.writeValueAsString("userId")+":"+mapper.writeValueAsString(getId())+",\n";
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
		res += tab1+mapper.writeValueAsString("prefixe")+":"+mapper.writeValueAsString(getPrefix())+",\n";
		res += tab1+mapper.writeValueAsString("useButtons")+":"+useButtons()+",\n";
		res += tab1+mapper.writeValueAsString("useAutoNext")+":"+useAutoNext()+",\n";
		res += tab1+mapper.writeValueAsString("totalPointsEverGained")+":"+getTotalPointsEverGained()+",\n";
		res += tab1+mapper.writeValueAsString("numberOfGamesPlayed")+":"+getNumberOfGamesPlayed()+"\n";
		res +="}";
		return res;
	}
	public String toJson(){
		String res=null;
		try {
			res = toJsonUsingMapper();
		} catch (Exception e){
			System.err.println("[toJsonUsingMapper() failed]"+e.getMessage());
		}
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
		String res = toJson().replace("\n", "").replace("\t", "");
		return res;
	}
}
