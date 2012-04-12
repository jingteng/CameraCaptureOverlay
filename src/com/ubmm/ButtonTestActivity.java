package com.ubmm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;

import com.ubmm.TestList.FriendListAdapter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TableRow.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class ButtonTestActivity extends Activity implements SurfaceHolder.Callback, OnPreparedListener {

	/** Activity constants */
	private String answerKey;	
	private static final int NUM_INPUT_BUTTONS_PER_ROW = 7;
	private static final int MAX_NUM_LETTERS_PER_ROW = 8;	
	private static final int NUM_ROWS = 2;	
	private static final int ANSWER_WRONG = 0;
	private static final int ANSWER_IDLE = 1;
	private static final int ANSWER_RIGHT = 2;
	private static final String TAG = "ButtonTest";
	private static final String BLANK_CHARACTER = " ";

	int answerLength = 0, currentAnswerButtonIndex = 0;
	int answerState;	
	LinearLayout mainLayout;
	TableLayout answerTable, inputTable;
	TableRow[] inputRows;
	TableRow answerRow;
	
	/** TextViews are used as input buttons and answer buttons. */
	TextView[] answerButtons, inputButtons;	
	
	/** result = { "correct!", "guess again!" }  */
	TextView result;
	
	/** Controller buttons */
	ImageButton removeAll, shuffle, pass;

	/** Used to keep track of the answerButtons and inputButtons: <sourceID, destinationID> */
	Hashtable<Integer, Integer> inputSources;
	
	/** Used to store all the letters left in the input panel */
	ArrayList<String> letterList;
	
	
	/* for background media view */
	MediaPlayer mediaPlayer;
	SurfaceView surfaceView;
	SurfaceHolder surfaceHolder;
	boolean playing = false;
	
	String filePath;
	/* end of background media view */
	
	@Override
	public void onCreate(Bundle savedInstanceState) {		 
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.guess);		

		answerKey = TestList.mProfile.getGameWord();
		filePath = TestList.mProfile.getGameVideo();
		
		Log.d(TAG,"The video is:"+filePath+",the word is : "+answerKey);
		
		// TODO: Get the actual length of the answer from the server.
		answerLength = answerKey.length();
		
		/** 
		 * The initial state of the user's answer is idle. 
		 * When the user has input [answerLength] letters, answerState changes to either
		 * ANSWER_WRONG or ANSWER_RIGHT.
		 */
		answerState = ANSWER_IDLE;
				
		letterList = new ArrayList<String>();
		
		addButtons();
		
		/** Add pass button */
		pass = (ImageButton) findViewById(R.id.pass_button);
		pass.setBackgroundColor(Color.TRANSPARENT);
		pass.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	TestList.mProfile.reset();
            	//TODO:
            	// goto another activity that show you you failed
            	gotoAction();
            }});
		
		/** Remove all letters from the answer table. */
		removeAll = (ImageButton) findViewById(R.id.remove_all);
		removeAll.setBackgroundColor(Color.TRANSPARENT);
		removeAll.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!inputSources.isEmpty()) {
					for (int i = 0; i < answerLength; i++) removeLetterFromAnswerTable(i);											
				}
			}
		});		
		
		/** Create the shuffle button for shuffling input letters. */
		shuffle = (ImageButton) findViewById(R.id.shuffle);
		shuffle.setBackgroundColor(Color.TRANSPARENT);
		shuffle.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				shuffleLetters();
			}
		});				
		
		/** result = { "correct!", "guess again!" } */
		result = (TextView) findViewById(R.id.answer);
		
		surfaceView = (SurfaceView) findViewById(R.id.guessing_view);
		surfaceHolder = surfaceView.getHolder();
		surfaceHolder.addCallback(this);
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		
		TextView dumb1 = (TextView) findViewById(R.id.turn_count);
		dumb1.setText(Integer.toString(TestList.mProfile.getGame().round));
		TextView dumb2 = (TextView) findViewById(R.id.player_info);
		dumb2.setText(
				"  You are guessing\n  "+TestList.mProfile.getPlayerName()+"'s\n  act.");
		ImageView dumb3 = (ImageView)findViewById(R.id.player_picture); 
		dumb3.setImageBitmap(TestList.mProfile.urImage);
	}

	private void removeLetterFromAnswerTable(int currentButtonId) {
		TextView currentButton = answerButtons[currentButtonId];
		if (!currentButton.getText().toString().equals(" ")) {	
			int sourceID = inputSources.get(currentButtonId);	
			inputSources.remove(currentButtonId);
			
			inputButtons[sourceID].setText(currentButton.getText().toString());
			inputButtons[sourceID].setVisibility(View.VISIBLE);
			Log.d(TAG, "source ID = " + sourceID);
			if (answerState == ANSWER_WRONG) {

				/** 
				 * The user got the answer wrong and is trying to remove the letters. 
				 * The button background color has to be reset to blue as well as the answerRow.
				 */
				for (TextView tv : answerButtons) tv.setBackgroundResource(R.drawable.blue);
						answerRow.setBackgroundResource(R.color.blue_background);
						result.setVisibility(View.INVISIBLE); 

						/** Reset the answer state back to normal. */
						answerState = ANSWER_IDLE;					
						Log.d(TAG, "State reset");
			} 						 			
			letterList.add(currentButton.getText().toString());
			currentButton.setBackgroundResource(R.drawable.white);
			currentButton.setText(BLANK_CHARACTER);
		}
	}
	
	private void addButtons() {
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		//size = display.getWidth();
		int screenWidth = display.getWidth();

		/** Calculate the dimensions of a button. */
		int buttonWidth = screenWidth / (NUM_INPUT_BUTTONS_PER_ROW + (MAX_NUM_LETTERS_PER_ROW - NUM_INPUT_BUTTONS_PER_ROW));
		int buttonHeight = buttonWidth;

		inputTable = (TableLayout) this.findViewById(R.id.input_table);
		answerTable = (TableLayout) this.findViewById(R.id.answer_table);

		Log.d(TAG, "button width = " + buttonWidth);

		answerRow = new TableRow(this);				
		TableLayout.LayoutParams tableRowParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT,TableLayout.LayoutParams.WRAP_CONTENT);
		tableRowParams.setMargins(2, 2, 2, 2);
		answerRow.setLayoutParams(tableRowParams);
		answerRow.setBackgroundResource(R.color.blue_background);		
		answerRow.setGravity(Gravity.CENTER);			

		answerButtons = new TextView[answerLength];

		/** Generate NUM_ROWS rows for inputButtons */
		inputRows = new TableRow[NUM_ROWS];
		
		/** Generate inputButtons */
		inputButtons = new TextView[NUM_INPUT_BUTTONS_PER_ROW * NUM_ROWS];
		
		inputSources = new Hashtable<Integer, Integer>();

		for (int i = 0; i < answerLength; i++) {			
			answerButtons[i] = new Button(this);			
			final TextView currentButton = answerButtons[i];
			final int currentButtonID = i;

			currentButton.setText(BLANK_CHARACTER);
			currentButton.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD), Typeface.BOLD);

			/** Set button layout properties. */
			LayoutParams buttonLayoutParams = new LayoutParams(buttonWidth, buttonHeight);			
			buttonLayoutParams.setMargins(4, 4, 4, 4);
			currentButton.setLayoutParams(buttonLayoutParams);

			currentButton.setBackgroundResource(R.drawable.white);					
			currentButton.setOnClickListener(new OnClickListener() {
				@Override 
				public void onClick(View v) {
					if (!currentButton.getText().toString().equals(BLANK_CHARACTER)) {	
						removeLetterFromAnswerTable(currentButtonID);
					}
				}
			});
			answerRow.addView(answerButtons[i]);
		} 
		answerTable.addView(answerRow, new TableLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

		for (int i = 0; i < NUM_ROWS; i++) {
			inputRows[i] = new TableRow(this);			
			inputRows[i].setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		}

		char[] letters=WordUtility.getRandomLetters(NUM_ROWS * NUM_INPUT_BUTTONS_PER_ROW, answerKey);
		/**
		 * Create and display input buttons.
		 */
		for (int i = 0; i < NUM_ROWS * NUM_INPUT_BUTTONS_PER_ROW; i++) {
			final int currentIndex = i;
			
			// TODO Replace dummy letters with the ones obtained from the server.
			char temp = letters[i]; //(char)('A' + i);
			final String currentText = Character.toString(temp);
			
			letterList.add(currentText);
			inputButtons[currentIndex] = new Button(this);
			
			/** Obtain a final reference to the current input button (for OnClickListener). */
			final TextView currentInputButton = inputButtons[currentIndex];
			currentInputButton.setText(currentText);				
			currentInputButton.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD), Typeface.BOLD);
			
			/** Set its layout and text formatting. */
			LayoutParams buttonLayoutParams = new LayoutParams(buttonWidth, buttonHeight);
			buttonLayoutParams.setMargins(4, 4, 4, 4);
			currentInputButton.setLayoutParams(buttonLayoutParams);
			currentInputButton.setBackgroundResource(R.drawable.blue);
			currentInputButton.setTextSize(20);
			currentInputButton.setTextColor(Color.WHITE);

			/** Define input button's behavior. */
			currentInputButton.setOnClickListener(new OnClickListener() {
				@Override 
				public void onClick(View v) {
					/** Get the next available answer button. */					
					int tempIndex = getNextAvailableAnswerButtonIndex();
					if (tempIndex < answerLength && tempIndex != -1) {						
						currentAnswerButtonIndex = tempIndex;
						Log.d(TAG, "Next available answer button index = " + currentAnswerButtonIndex);
						
						/** Change the answer button's character to the input character. */
						answerButtons[currentAnswerButtonIndex].setText(currentInputButton.getText().toString());
						answerButtons[currentAnswerButtonIndex].setBackgroundResource(R.drawable.blue);						
						answerButtons[currentAnswerButtonIndex].setTextSize(20);						
						answerButtons[currentAnswerButtonIndex].setTextColor(Color.WHITE);					

						inputSources.put(currentAnswerButtonIndex, currentIndex);
						inputButtons[currentIndex].setVisibility(View.INVISIBLE);						
						letterList.remove(inputButtons[currentIndex].getText());						
						Log.d(TAG, "Current input button letter = " + inputButtons[currentIndex].getText().toString());
						
						inputButtons[currentIndex].setText(BLANK_CHARACTER);
						
						/**
						 * If the number of characters the user input == the length of the word,
						 * check to see if the answer is correct.
						 */
						if (getNextAvailableAnswerButtonIndex() == -1) {							
							if (getAnswer().equalsIgnoreCase(answerKey)) {
								/** The user input is correct. */
								answerState = ANSWER_RIGHT;
								answerRow.setBackgroundResource(R.color.green_background);								
								result.setBackgroundResource(R.drawable.answer_textview_right);								
								result.setText("correct!");
								for (TextView tv : answerButtons) tv.setBackgroundResource(R.drawable.green);
								Handler myHandler = new Handler();
								myHandler.postDelayed(new Runnable() {
									@Override
									public void run() {
										// Change state here
										TestList.mProfile.evolve();
										gotoAction();
									}
								}, 500);
								
							} else {
								/** The user input is wrong. */
								answerState = ANSWER_WRONG;
								Log.d(TAG, "Wrong answer.");
								answerRow.setBackgroundResource(R.color.red_background);
								result.setBackgroundResource(R.drawable.answer_textview_wrong);								
								result.setText("guess again!");								 
								for (TextView tv : answerButtons) tv.setBackgroundResource(R.drawable.red);
							}
							result.setVisibility(View.VISIBLE);
						}

					}
				}
			});
			inputRows[i / NUM_INPUT_BUTTONS_PER_ROW].addView(inputButtons[currentIndex]);
		}

		for (int i = 0; i < NUM_ROWS; i++)
			inputTable.addView(inputRows[i], new TableLayout.LayoutParams(LayoutParams.FILL_PARENT,	LayoutParams.WRAP_CONTENT));		
	}

	/**
	 * Scan through the answerButtons[] array for the index of the first empty answerButton.
	 * @return the index of the first empty answerButton
	 */
	private int getNextAvailableAnswerButtonIndex() {
		for (int i = 0; i < answerLength; i++) {
			if (answerButtons[i].getText().equals(BLANK_CHARACTER)) {
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * Assemble the answer string from answerButtons[] array.
	 * @return answer string
	 */
	private String getAnswer() {
		String answer = "";
		for (TextView tv : answerButtons) answer += tv.getText().toString();
		return answer; 
	}
	
	/**
	 * Shuffle all the letters in the input panel. 
	 */
	private void shuffleLetters() {
		Collections.shuffle(letterList);
		int letterIndex = 0;
		for (int i = 0; i < NUM_INPUT_BUTTONS_PER_ROW * NUM_ROWS; i++) {
			if (!inputButtons[i].getText().toString().equals(BLANK_CHARACTER)) {
				inputButtons[i].setText(letterList.get(letterIndex)); 
				letterIndex++; 
			}
		}
	}
	
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		Log.d(TAG, "surface changed");
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.d(TAG, "surface created");
		playVideo();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.d(TAG, "surface destroyed");
		if (playing) {
			playing = false;
			if (mediaPlayer!=null) {
			mediaPlayer.stop();
			mediaPlayer=null;
			}
		}
	}

	public void onPrepared(MediaPlayer mp) {
        Log.d(TAG, "onPrepared called");
        playing = true;
        mediaPlayer.start();
        mediaPlayer.setLooping(true);
        Log.d(TAG, "mediaPlayer started");
    }
	
	public void onBackPressed() {
		// Enables power-saving
		Log.d(TAG, "onBackPressed called");
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		//gotoAction(); 
		//super.onBackPressed();
	}
	
	private void gotoAction() {
		if (playing) {
			playing=false;
			mediaPlayer.stop();
		}
		releaseMediaPlayer();
		
		Log.d(TAG, "calling guess intent");
		Intent i = new Intent(getApplicationContext(), CameraActivity.class); // start this game
    	startActivity(i);
		
    	finish();
	}
	
	public void playVideo() {
		// Obtain a media player instance
		mediaPlayer = new MediaPlayer();
		mediaPlayer.setOnPreparedListener(this);
		
		mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer arg0) {
				Log.d(TAG, "on completion");
				//gotoAction();
			}
		});
		
		mediaPlayer.setDisplay(surfaceHolder);
		mediaPlayer.reset();
		try {
			mediaPlayer.setDataSource(filePath);
			mediaPlayer.prepare();
		} catch (IllegalArgumentException e) {
			Log.d(TAG, "Illegal Argument Exceptioin");
			e.printStackTrace();
		} catch (IllegalStateException e) {
			Log.d(TAG, "Illegal State Exceptioin");
			e.printStackTrace();
		} catch (IOException e) {
			Log.d(TAG, "IO Exception");
			e.printStackTrace();
		}
	}
	
    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaPlayer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseMediaPlayer();
    }

    private void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
	protected void onResume() {
		// Disables power-saving
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		Log.d(TAG, "onResume called");
		super.onResume();
	}

}