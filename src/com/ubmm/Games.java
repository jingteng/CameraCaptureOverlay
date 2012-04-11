package com.ubmm;

import android.util.Log;

public class Games {
	public String myUID;
	public String urUID;
	public String myVideoURL;
	public String urVideoURL;
	public String recordURL;
	public int round;
	private boolean myTurn;
	public String myWord;
	public String urWord;
	
	private String TAG="GAMES";
	private final String urlhead = HTTPUtility.DEFAULT_SERVER_DOWNLOAD_FOLDER;
	
	private void generateURLs() {
		myVideoURL = urlhead + myUID+"_"+urUID+".mp4";
		urVideoURL = urlhead + urUID+"_"+myUID+".mp4";
		recordURL = urlhead + myUID+"_"+urUID+".dsthrec";
	}
	
	// init with a new game
	Games(String mUID, String uUID, int dumb) {
		myUID = mUID;
		urUID = uUID;
		generateURLs();
		myWord = "-";
		urWord = "-";
		
		reset();
	}
	
	// load an existing game
	Games(String mUID, String line) {
		myUID = mUID;
		
		if (line.length()<3) {
			Log.d(TAG, "Invalid game info line in profile");
			return;
		}
		// parse the line
		String[] x = line.split("\\s+");
		int l = x.length;
		urUID = x[0];
		round = l>0?Integer.valueOf(x[1]):1;
		myTurn = (l>1)?(x[2].equals("wait")?false:true):true;
		myWord= l>2?x[3]:"-";
		urWord= l>3?x[4]:"-";
		
		generateURLs();
		Log.d(TAG, "new game resolved: player "+ myUID +" vs "+urUID
				+"\nRound "+Integer.toString(round)+(myTurn?" game":" wait")
				+"\n My word="+myWord+" Your word="+urWord);
	}
	
	public void reset() {
		round = 0;
		myTurn = true;
	}
	
	public boolean isMyTurn() {
		return myTurn;
	}
	
	public boolean hasLastGame() {
		return !(myWord.equals("-"));
	}
	
	public boolean hasThisGame() {
		return !(urWord.equals("-"));
	}
	
	public void evolve() {
		round++;
	}
	
	public void update(String newword) {
		myTurn = false;
		myWord = newword;
		//generateURLs();
	}
	
	public void updateU(String newword) {
		myTurn = true;
		round++;
		urWord = newword;
	}
}
