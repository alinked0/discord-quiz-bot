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

import org.jetbrains.annotations.NotNull;

import java.util.Set;

import com.linked.quizbot.Constants;

import net.dv8tion.jda.api.entities.emoji.Emoji;

public class UserData implements Iterable<QuestionList>{
	private String userId;
	protected List<QuestionList> listsSortedByListId;
	public static List<UserData> allUserLists = new ArrayList<>();
	private int numberOfGamesPlayed;
	private int totalPointsEverGained;
	private Map<String, Emoji> tagEmojiPerTagName;
	protected Map<String, List<QuestionList>> questionListPerTags;
	
	public UserData(){
		this.userId = "";
		this.listsSortedByListId = new ArrayList<>();
		this.numberOfGamesPlayed = 0;
		this.totalPointsEverGained = 0;
		this.tagEmojiPerTagName = new HashMap<>();
		this.questionListPerTags = new HashMap<>();
	}
	public UserData(String userId){
		this.userId = userId;
		listsSortedByListId = getUserListQuestions(userId);
		initStats();
		initTags();
	}
	
	public UserData(String userId, Collection<? extends QuestionList> c){
		this.userId = userId;
		listsSortedByListId = new ArrayList<>(c);
		initStats();
		initTags();
	}
	
	private void initTags(){
		Map<String, Emoji> tags;
		listsSortedByListId.sort(QuestionList.comparatorByListId());
		if (tagEmojiPerTagName == null) {
			tagEmojiPerTagName = new HashMap<>();
			for(QuestionList l: listsSortedByListId){
				tags = l.getTags();
				tagEmojiPerTagName.putAll(tags);
			}
		}
		questionListPerTags = new HashMap<>();
		for(QuestionList l: listsSortedByListId){
			tags = l.getTags();
			for (String tagName : tags.keySet()){
				if (questionListPerTags.getOrDefault(tagName, null)==null){
					questionListPerTags.put(tagName, new ArrayList<QuestionList>());
				}
				questionListPerTags.get(tagName).add(l);
			}
		}
		int index = myBinarySearchIndexOf(allUserLists, this, comparator());
		if (index < 0){
			index = -index - 1;
			allUserLists.add(index, this);
		}else {
			allUserLists.set(index, this);
		}
	}
	public void initStats(){
		try{
			UserData tmp = UserDataParser.fromJsonFile(getPathToUserData());
			numberOfGamesPlayed = tmp.getNumberOfGamesPlayed();
			totalPointsEverGained = tmp.getTotalPointsEverGained();
			tagEmojiPerTagName = tmp.getTagEmojiPerTagName();
		} catch (IOException e) {
			numberOfGamesPlayed = 0;
			totalPointsEverGained = 0;
			tagEmojiPerTagName = new HashMap<>();
		}
	}
	public List<QuestionList> getLists() {
		List<QuestionList> res = new ArrayList<>(listsSortedByListId);
		return res;
	}
	public String getUserId(){ return userId;}

	public String getPathToUserData(){
		return Constants.USERDATAPATH+Constants.SEPARATOR+getUserId()+Constants.SEPARATOR+"user-data.json";
	}
	public QuestionList get(int index) {
		return listsSortedByListId.get(index);
	}
	public static String getCodeForIndexQuestionList(QuestionList l){
		return l.getListId();
	}
	public int getTotalPointsEverGained(){
		return totalPointsEverGained;
	}
	public int getNumberOfGamesPlayed(){
		return numberOfGamesPlayed;
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
	public static QuestionList getQuestionList(String listId) {
		QuestionList res= null;
		if (QuestionList.getExampleQuestionList().getListId().equals(listId)){
			res =  QuestionList.getExampleQuestionList();
		} else {
			QuestionList searched = new QuestionList(); 
			searched.setListId(listId);
			int i=-1, k;
			List<QuestionList> l=null;
			for (UserData u : UserData.allUserLists){
				l = u.getLists();
				k = UserData.myBinarySearchIndexOf(l, 0, l.size()-1, searched, QuestionList.comparatorByListId());
				if (k!=-1){
					i=k;
					break;
				}
			}
			if (i>=0){
				res = l.get(i);
			}
		}
		return res;
	}
	public void setNumberOfGamesPlayed(int number) {
		this.numberOfGamesPlayed = number;
	}
	public void setTotalPointsEverGained(int points) {
		this.totalPointsEverGained = points;
	}
	public void setTagEmojiPerTagName(Map<String, Emoji> m) {
		this.tagEmojiPerTagName = m;
	}
	public static void addListToUser(String userId, QuestionList l) {
		for (UserData u : allUserLists) {
			if (u.getUserId().equals(userId)) {
				u.addList(l);
				return;
			}
		}
	}
	public QuestionList getQuestionListByName(String listName){
		List<QuestionList> listsSortedByName = new ArrayList<>(listsSortedByListId);
		listsSortedByName.sort(QuestionList.comparatorByName());
		int index = QuestionList.myBinarySearchIndexOf(listsSortedByName, listName, (e,f)->e.compareTo(f));
		if (index<0) return null;
		return listsSortedByName.get(index);
	}
	public void addList(@NotNull QuestionList l){
		int index;
		QuestionList k = getQuestionListByName(l.getName());
		if (k==null){
			k = l;
		} else {
			k.addAll(l);
		}
		index = myBinarySearchIndexOf(listsSortedByListId, k, QuestionList.comparatorByListId());
		if (index>=0) {
			listsSortedByListId.set(index, k);
		} else{
			listsSortedByListId.add(index*-1 -1,k);
		}
		k.exportListQuestionAsJson();
		index = myBinarySearchIndexOf(allUserLists, this, comparator());
		if (index < 0){
			index = -index - 1;
			allUserLists.add(index, this);
		}else {
			allUserLists.set(index, this);
		}
	}
	public boolean addTag(String tagName, Emoji emoji) {
		if (tagEmojiPerTagName.containsKey(tagName)) {
			return false; // Tag already exists
		}
		tagEmojiPerTagName.put(tagName, emoji);
		questionListPerTags.put(tagName, new ArrayList<>());
		exportUserData();
		return true;
	}
	public static boolean addTag(String userId, String tagName, Emoji emoji) {
		UserData user = new UserData(userId);
		return user.addTag(tagName, emoji);
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
		UserData user = new UserData(userId);
		return user.deleteTag(tagName);
	}
	public boolean addTagToList(QuestionList l, String tagName) {
		int index = myBinarySearchIndexOf(getLists(), l, QuestionList.comparatorByListId());
		Emoji emoji;
		if (index >= 0) {
			emoji = tagEmojiPerTagName.getOrDefault(tagName,null);
			if (emoji == null) {
				l = get(index);
				l.addTag(tagName, emoji);
				questionListPerTags.get(tagName).add(l);
				l.exportListQuestionAsJson();
				exportUserData();
				return true;
			}
			return true;
		}
		return false;
	}
	public static boolean addTagToList(String listId, String tagName) {
		QuestionList tmp = new QuestionList(); tmp.setListId(listId);
		for (UserData u : allUserLists) {
			if (u.addTagToList(tmp, tagName)) {
				return true;
			}   
		}
		return false;
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
	public static boolean removeTagFromList(String listId, String tagName) {
		QuestionList l,tmp = new QuestionList(); tmp.setListId(listId);
		int index;
		for (UserData u : allUserLists) {
			index = myBinarySearchIndexOf(u.getLists(), tmp, QuestionList.comparatorByListId());
			if (index >= 0) {
				l = u.get(index);
				if (!l.getTags().containsKey(tagName)) {
					return false; // Tag does not exist in the list
				}
				l.removeTag(tagName);
				l.exportListQuestionAsJson();
				u.tagEmojiPerTagName.remove(tagName);
				u.questionListPerTags.remove(tagName);
				u.exportUserData();
				return true;
			}
		}
		return false;
	}
	public static void deleteList(QuestionList l){
		UserData UserData = new UserData(l.getAuthorId());
		UserData.listsSortedByListId.remove(l);
		File f = new File(l.getPathToList());
		f.delete();
		allUserLists.remove(UserData);
		allUserLists.add(UserData);
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
	public static int myBinarySearchIndexOf(List<UserData> tab, int start, int end, String q, Comparator<? super String> compare){
		if (start > end){
			return -1*start-1;
		}
		int m = (start+end)/2;
		int comp = compare.compare(tab.get(m).getUserId(), q);
		if(comp == 0){
			return m;
		}
		if (comp >0){
			return myBinarySearchIndexOf(tab, start, m-1, q, compare);
		}
		return myBinarySearchIndexOf(tab, m+1, end, q, compare);
	}
	public static int myBinarySearchIndexOf(List<UserData> tab, String q, Comparator<? super String> compare){
		return myBinarySearchIndexOf(tab, 0, tab.size()-1, q, compare);
	}

	@Override
	public Iterator<QuestionList> iterator(){
		return getLists().iterator();
	}

	public Comparator<? super UserData> comparator() {
		return (e, f)->(e.getUserId().compareTo(f.getUserId()));
	}
	public static List<QuestionList> getUserListQuestions(String userId) {
		userId = userId.replace("[a-zA-Z]", "");
		long start = System.nanoTime();
		List<QuestionList> res;
		for (UserData u : allUserLists) {
			if (u.equals(userId)) {
				res = u.getLists();
				//if (!Constants.isBugFree()) System.out.printf("   $> time getUserListQuestions = `%.3f ms`\n", (System.nanoTime() - start) / 1000000.00);
				return res;
			} 
		}
		
		res = new ArrayList<>();
		
		File folder = new File(Constants.LISTSPATH+Constants.SEPARATOR+ userId);
		File[] listOfFiles = folder.listFiles();
		
		if(listOfFiles != null) {
			//Arrays.sort(listOfFiles, Comparator.comparingLong(File::lastModified).reversed());
			for (int i = 0; i < listOfFiles.length; i++) {
				//if (listOfFiles[i].isFile())
				if (List.of("user-data.json", "tmp").contains(listOfFiles[i].getName())) continue;
				QuestionList l = QuestionList.importListQuestionFromJson(listOfFiles[i].getAbsolutePath());
				res.add(l);
			}
		}
		return res;
	}
	public static void exportAllUserLists() {
		for (UserData UserData: UserData.allUserLists) {
			UserData.exportUserLists();
		}
	}
	public void exportUserLists() {
		List<QuestionList> listsSortedByListId = getLists();
		for (QuestionList l : listsSortedByListId) {
			l.exportListQuestionAsJson();
			System.out.println("  $> exported "+l.getPathToList()+"; ");
		}
	}
	@Override
	public int hashCode(){
		return getUserId().hashCode();
	}
	@Override
	public boolean equals(Object o){
		if (this == o) {return true;}
		if(o instanceof  UserData) {
			UserData u = (UserData) o;
			return getUserId().equals(u.getUserId());
		}
		if(o instanceof  String) {
			String u = (String) o;
			return getUserId().equals(u.replace("[a-zA-Z]", ""));
		}
		return false;
	}

	public String userDataToString(){
		String res="", 
			tab1 = "\t";
		res += "{\n";
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
			System.err.println("$> An error occurred while exporting a List of questions.");
			e.printStackTrace();
		}
	}
	@Override
	public String toString() {
		String res = userDataToString();
		return res;
	}
}
