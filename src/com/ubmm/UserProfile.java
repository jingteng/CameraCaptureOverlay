package com.ubmm;

import java.util.Vector;

import android.graphics.Bitmap;
import android.os.Handler;

public class UserProfile {
	HTTPUtility h;
	ProfileEventListener listener;
	Handler mHandler;
	
	public static final int MY_PROFILE_DOWNLOADED_EVENT = 0;
	public static final int MY_PROFILE_UPDATED_EVENT = 1;
	public static final int MY_PROFILE_REFRESHED_EVENT = 2;
	public static final int MY_PROFILE_GAME_ADDED_EVENT=3;
	
	private String profile="";
	private String proFilename; // on remote server
	private String UID;
	
	public Vector<Games> gameList;
	private int currGameId;
	private Games currGame;
	
	private String urName;
	public Bitmap urImage;
	
	UserProfile(String uid, ProfileEventListener l) {
		listener = l;
		h = new HTTPUtility();
		mHandler = new Handler();
		UID = uid;
		proFilename = uid + ".dsthusr";
		gameList = new Vector<Games>();
		downloadProfile();
	}
	
	UserProfile(String uid) {
		h = new HTTPUtility();
		mHandler = new Handler();
		UID = uid;
		proFilename = uid + ".dsthusr";
		gameList = new Vector<Games>();	
		downloadProfileSync();
	}
	
	private void downloadProfile() {
		mHandler.post(new Runnable() {
            @Override
            public void run() {
            	profile = h.downloadText(proFilename);
            	if (profile==null) {
        			profile=""; //create a new profile
        		} else
        			getGameList();
            	listener.onProfileEvent(MY_PROFILE_DOWNLOADED_EVENT);
            }
        });
	}
	
	private void downloadProfileSync() {
		profile = h.downloadText(proFilename);
		if (profile == null) {
			profile = ""; // create a new profile
		} else
			getGameList();
	}
	
	private void getGameList() {
		String[] lines = profile.split(System.getProperty("line.separator"));
		for (int i=0; i<lines.length; i++) {
			if (lines[i].equals("")) continue;
			gameList.add(new Games(UID, lines[i]));
		}
	}
	
	private int searchGame(String uid) {
		int i, k=gameList.size();
		for (i=0; i<k; i++)
			if (gameList.get(i).urUID.equals(uid)) return i;
		return -1;
	}
	
	public void playWith(String urUID, String fbname, Bitmap thumbnail) {
		urImage = thumbnail;
		urName = fbname;
		currGameId = searchGame(urUID);
		if (currGameId<0) {
			currGameId = newGame(urUID);
			listener.onProfileEvent(MY_PROFILE_GAME_ADDED_EVENT);
		}
		currGame = gameList.get(currGameId);
	}
	
	public String getPlayerName() {
		return urName;
	}
	
	// new game happens when the user initiates a game with a friend
	public int newGame(String playerUID) {
		gameList.add(new Games(UID,playerUID, 0));
		return gameList.size()-1;
	}
	
	public Games getGame(int gameId) {
		return gameList.get(gameId);
	}
	
	public Games getGame() {
		return currGame;
	}
	
	public boolean isMyTurn() {
		return currGame.isMyTurn();
	}
	
	public boolean hasLastGame() {
		return currGame.hasLastGame();
	}
	
	public boolean hasThisGame() {
		return currGame.hasThisGame();
	}
	
	public String getLastGameVideo() {
		return currGame.myVideoURL;
	}
	
	public String getLastGameWord() {
		return currGame.myWord;
	}
	
	public String getGameVideo() {
		return currGame.urVideoURL;
	}
	
	public String getGameWord() {
		return currGame.urWord;
	}
	
	public void evolve() {
		currGame.evolve();
	}
	
	public void reset() {
		currGame.reset();
	}
	
	public void uploadProfileSync() {
		updateProfile();
		h.uploadText(profile, proFilename);
	}
	
	public void update(final String newword, final String videoFile) {
		currGame.update(newword);
		updateProfile(); // gameList -> profile
		
		mHandler.post(new Runnable() {
            @Override
            public void run() {
            	//uploadFile(videoFile);
				uploadProfileSync();
				// update the other party's profile
            	UserProfile urProfile = new UserProfile(currGame.urUID);
            	urProfile.playWith(UID,"",null);
            	urProfile.getGame().updateU(newword);
            	urProfile.uploadProfileSync();
            	h.uploadFile(videoFile, currGame.myVideoURL);
            	listener.onProfileEvent(MY_PROFILE_UPDATED_EVENT);
            }
        });
		
	}
	
	private void updateProfile() {
		int k=gameList.size();
		Games temp;
		profile="";
		for (int i=0; i<k; i++) {
			temp = gameList.get(i);
			profile=profile+temp.urUID+" "+Integer.toString(temp.round)+" "
					+((temp.isMyTurn())?"game":"wait")+" "+temp.myWord+" "+temp.urWord+"\n";
		}
	}
	
	public void refreshUsrProfile() {
		gameList.clear();
		downloadProfile();
	}

}
