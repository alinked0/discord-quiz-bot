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

import com.linked.quizbot.Constants;

import net.dv8tion.jda.api.entities.emoji.Emoji;

public class Users {
	public static List<User> allUsers = new ArrayList<>();

	public static User get(String userId) {
		return Users.getUser(userId);
	}
	public static void reset() {
        allUsers.clear();
        QuestionListHash.clearGeneratedCodes();
    }
	public static void addUser(User user){
		int index = myBinarySearchIndexOf(Users.allUsers, user, User.comparatorByUserId());
		if (index < 0){
			index = -index - 1;
			Users.allUsers.add(index, user);
		}else {
			Users.allUsers.set(index, user);
		}
	}
	public static String getCodeForQuestionListId(QuestionList l){
		String listId = l.getListId();
		if (listId==null || listId.length()<Constants.DISCORDIDLENMIN){
			listId = QuestionListHash.generate(l);
		}
		return listId;
	}
	public static User getUser(String userId){
		int index = User.myBinarySearchUserId(Users.allUsers, userId);
		if (index >=0){
			return Users.allUsers.get(index);
		}
		addUser(new User(userId));
		index = User.myBinarySearchUserId(Users.allUsers, userId);
		if (index >=0){
			return Users.allUsers.get(index);
		}
		return null;
	}
	public static QuestionList getQuestionListByListId(String listId) {
		QuestionList l=null;
		for (User u : Users.allUsers){
			l = u.getUserQuestionListByListId(listId);
			if (l!=null){
				return l;
			}
		}
		return l;
	}
	public static QuestionList getQuestionListByName(String listName){
		QuestionList res = null;
		for (User u : Users.allUsers){
			res = u.getUserQuestionListByName(listName);
			if (res != null){
				return res;
			}
		}
		return res;
	}
	public static void addListToUser(String userId, QuestionList l) {
		getUser(userId).addList(l);
	}
	public static boolean createTag(String userId, String tagName, Emoji emoji) {
		User user = new User(userId);
		return user.createTag(tagName, emoji);
	}
	public static boolean deleteTag(String userId, String tagName) {
		User user = new User(userId);
		return user.deleteTag(tagName);
	}
	public static boolean addTagToList(String listId, String tagName) {
		QuestionList tmp = new QuestionList.Builder().id(listId).build();
		for (User u : Users.allUsers) {
			if (u.addTagToList(tmp, tagName)) {
				return true;
			}   
		}
		return false;
	}
	public static boolean removeTagFromList(String listId, String tagName) {
		QuestionList l = Users.getQuestionListByListId(listId);
		User u = new User(l.getAuthorId());
		return u.removeTagFromList(l, tagName);
	}
	public static void deleteList(QuestionList l){
		User user = new User(l.getAuthorId());
		user.listsSortedByListId.remove(l);
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
	
	public Iterator<User> iterator(){
		return Users.allUsers.iterator();
	}
	public static List<QuestionList> getUserListQuestions(String userId) {
		List<QuestionList> res;
		for (User u : Users.allUsers) {
			if (u.equals(userId)) {
				res = u.getLists();
				return res;
			} 
		}
		
		res = new ArrayList<>();
		
		File folder = new File(Constants.LISTSPATH+Constants.SEPARATOR+ userId);
		File[] listOfFiles = folder.listFiles();
		
		if(listOfFiles != null) {
			for (int i = 0; i < listOfFiles.length; i++) {
				if (List.of("user-data.json", "tmp").contains(listOfFiles[i].getName())) continue;
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
	public static void exportAllUserLists() {
		User user;
		for (int i=0; i<Users.allUsers.size(); i++) {
			user = Users.allUsers.get(i);
			user.exportUserLists();
			System.out.println("  $> exported UserLists ("+(i+1)+"/"+Users.allUsers.size()+"); ");
		}
	}
	public static void loadAllUsers(){
		File folder = new File(Constants.USERDATAPATH);
		String name;
		if (folder.exists()){
			for (File f : folder.listFiles()){
				if (f.isDirectory()){
					name = f.getName();
					if (name.length()>=Constants.DISCORDIDLENMIN){
						new User(name);
					}
				}
			}

		}
	}
	public static void exportAllUserData(){
		User user;
		for (int i=0; i<Users.allUsers.size(); i++) {
			user = Users.allUsers.get(i);
			user.exportUserData();
			System.out.println("  $> exported UserData ("+(i+1)+"/"+Users.allUsers.size()+"); ");
		}
		for(User u : Users.allUsers){
			u.exportUserData();
		}
	}
}
