package com.linked.quizbot.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.linked.quizbot.Constants;

import net.dv8tion.jda.api.entities.emoji.Emoji;

/**
 * A central registry and utility class for managing all {@link User} objects in the application.
 * <p>
 * This class serves as a static access point to user data, providing methods for
 * retrieving, adding, and updating users. It is responsible for loading all user data
 * from local files on startup and exporting it to ensure data persistence.
 * This class also contains utility methods for performing efficient binary searches
 * on sorted lists of users and quiz lists.
 * </p>
 * **This class is tested using JUnit 5.**
 * * @author alinked0
 * @version 1.0
 * @since 2025-02-01
 * @see User
 * @see QuestionList
 */
public class Users {
	public static final List<User> allUsers = new ArrayList<>();

	/*static {
		loadAllUsers();
	}*/

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
	public static void update(User user){
		addUser(user);
	}
	public static String getCodeForQuestionListId(QuestionList l){
		String id = l.getId();
		if (id==null || id.length()<Constants.DISCORDIDLENMIN){
			id = QuestionListHash.generate(l);
		}
		return id;
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
	public static QuestionList getQuestionListById(String id) {
		QuestionList l=null;
		for (User u : Users.allUsers){
			l = u.getById(id);
			if (l!=null){
				return l;
			}
		}
		return l;
	}
	public static QuestionList getQuestionListByName(String listName){
		QuestionList res = null;
		for (User u : Users.allUsers){
			res = u.getByName(listName);
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
		User user = Users.get(userId);
		return user.createTag(tagName, emoji);
	}
	public static boolean deleteTag(String userId, String tagName) {
		User user = Users.get(userId);
		return user.deleteTag(tagName);
	}
	public static boolean addTagToList(String id, String tagName) {
		QuestionList tmp = new QuestionList.Builder().id(id).build();
		for (User u : Users.allUsers) {
			if (u.addTagToList(tmp, tagName)) {
				return true;
			}   
		}
		return false;
	}
	public static boolean removeTagFromList(String id, String tagName) {
		QuestionList l = Users.getQuestionListById(id);
		User u = Users.get(l.getAuthorId());
		return u.removeTagFromList(l, tagName);
	}
	public static void deleteList(QuestionList l){
		User user = Users.get(l.getAuthorId());
		user.deleteList(l);
		Users.update(user);
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
	public static List<QuestionList> importUserLists(String userId) {
		return new User.Builder().id(userId).build().importUserLists();
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
		String userId;
		if (folder.exists()){
			for (File f : folder.listFiles()){
				if (f.isDirectory()){
					userId = f.getName();
					if (userId.length()>=Constants.DISCORDIDLENMIN){
						addUser(new User(userId));
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
	}
}
