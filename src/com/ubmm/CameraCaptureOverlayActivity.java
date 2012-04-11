package com.ubmm;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class CameraCaptureOverlayActivity extends Activity {
	private String TAG="UBMMTEST";
	
	public String HerUID= "54321";
	public UserProfile pro;
	
	Button startButton, frontButton, FacebookButton;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        String MyUID = "12345";
        
        pro = new UserProfile(MyUID, new ProfileEventListener() {
        	public void onProfileEvent(int event) {
        		switch (event) {
        		case UserProfile.MY_PROFILE_DOWNLOADED_EVENT:
        			Log.d(TAG,"profile downloaded");
        			startButton.setEnabled(true);
        			pro.playWith(HerUID);
        			break;
        		case UserProfile.MY_PROFILE_UPDATED_EVENT:
        			Log.d(TAG,"Profile updated");
        			break;
        		}
        	}
        });
        
        startButton = (Button) this.findViewById(R.id.startRecorder);
        startButton.setText("PLAY");
        startButton.setEnabled(false);
        startButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
//            	Intent i = new Intent(getApplicationContext(), CameraActivity.class);
//            	startActivity(i);
            	
            	
				if (pro.isMyTurn()) {
					if (pro.hasThisGame()) {
						if (pro.hasLastGame()) {
							Log.d(TAG, "Review last round's guess");
							Log.d(TAG, "word:" + pro.getLastGameWord());
							Log.d(TAG, "videourl:" + pro.getLastGameVideo());
						}
						Log.d(TAG, "watch and guess");
						Log.d(TAG, "word:" + pro.getGameWord());
						Log.d(TAG, "videourl:" + pro.getGameVideo());
						pro.evolve();
					} else {
						Log.d(TAG, "NEW GAME");
						pro.evolve();
					}
					pro.update("goodtry", "dumblocation");
				} else {
					Log.d(TAG, "NOT MY TURN");
				}
			}
        });
        
        frontButton = (Button) this.findViewById(R.id.front_page_button);
        frontButton.setText("Load game");
        frontButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
//            	Intent i = new Intent(getApplicationContext(), TestDrawable.class);
//            	startActivity(i);
            }
        });
        
        FacebookButton = (Button) this.findViewById(R.id.facebook_button);
        FacebookButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
                // Perform action on click
//            	UserProfile u = new UserProfile("123456");
//            	u.listener = new ProfileEventListener() {
//            		public void onProfileDownloadDone() {
//            			Log.d(TAG,"ProfileDownload complete");
//            		}
//            		public void onProfileUpdatedDone() {
//            			
//            		}
//            	};
            }
        });
    }
}