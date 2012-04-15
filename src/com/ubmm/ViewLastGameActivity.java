package com.ubmm;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

public class ViewLastGameActivity extends Activity implements SurfaceHolder.Callback,
		OnClickListener, OnPreparedListener {
	
	final String TAG = "ViewLastGame";

	MediaPlayer mediaPlayer;
	SurfaceView surfaceView;
	SurfaceHolder surfaceHolder;

	String filePath;

	boolean playing = false;

	ImageButton skipButton;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.lastgame_layout);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		//Intent sender = getIntent();
		//filePath = sender.getExtras().getString("videofile");
		filePath = TestList.mProfile.getGameVideo();
		
		Log.d(TAG, "get video file path = " + filePath);

		skipButton = (ImageButton) findViewById(R.id.skip_lastgame_button);
		skipButton.setOnClickListener(this);
		
		surfaceView = (SurfaceView) findViewById(R.id.lastgame_surfaceview);
		surfaceHolder = surfaceView.getHolder();
		surfaceHolder.addCallback(this);
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

	}
	
	public void onPrepared(MediaPlayer mp) {
        Log.d(TAG, "onPrepared called");
        playing = true;
        mediaPlayer.start();
        Log.d(TAG, "mediaPlayer started");
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

	@Override
	public void onClick(View v) {
		int buttonId = v.getId();

		switch (buttonId) {
		case R.id.skip_lastgame_button:
			break;
		}
		gotoGuess();
	}
	
	public void onBackPressed() {
		// Enables power-saving
		Log.d(TAG, "onBackPressed called");
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		gotoGuess(); 
		//super.onBackPressed();
	}
	
	private void gotoGuess() {
		if (playing) {
			playing=false;
			mediaPlayer.stop();
		}
		releaseMediaPlayer();
		
		Log.d(TAG, "calling guess intent");

//		Intent i = new Intent(getApplicationContext(), ButtonTestActivity.class); // start this game
//		startActivity(i);
		Intent i = new Intent(getApplicationContext(), TransitActivity.class);
		/** Tap to guess */
		i.putExtra("intent", "guess");
		i.putExtra("playerInfo", "You are guessing \n" + TestList.mProfile.getPlayerName() + "'s act.");
		i.putExtra("classname", "ButtonTestActivity");
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
				gotoGuess();
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

