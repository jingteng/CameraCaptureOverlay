package com.ubmm;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

public class ConfirmVideo extends Activity implements SurfaceHolder.Callback,
		OnClickListener, OnPreparedListener {
	
	private static final int RESULT_USER_GOOD = 0;
	private static final int RESULT_USER_REDO = 1;

	final String TAG = "ConfirmVideo";

	MediaPlayer mediaPlayer;
	SurfaceView surfaceView;
	SurfaceHolder surfaceHolder;

	String filePath;

	boolean playing = false;

	ImageButton goodButton, badButton;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.confirm_layout);

		Intent sender = getIntent();
		filePath = sender.getExtras().getString("videofile");

		Log.d(TAG, "get video file path = " + filePath);

		goodButton = (ImageButton) findViewById(R.id.good_button);
		goodButton.setOnClickListener(this);
		badButton = (ImageButton) findViewById(R.id.bad_button);
		badButton.setOnClickListener(this);

		surfaceView = (SurfaceView) findViewById(R.id.surfaceview);
		surfaceHolder = surfaceView.getHolder();
		surfaceHolder.addCallback(this);
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

	}
	
	public void onPrepared(MediaPlayer mp) {
        Log.d(TAG, "onPrepared called");
        playing = true;
		mediaPlayer.setLooping(true);
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
			mediaPlayer.stop();
		}
	}

	@Override
	public void onClick(View v) {
		int buttonId = v.getId();
		int result = RESULT_USER_GOOD;
//		if (playing) {
//			playing = false;
//			mediaPlayer.stop();
//		}
		switch (buttonId) {
		case R.id.good_button:
			result = RESULT_USER_GOOD;
			break;
		case R.id.bad_button:
			result = RESULT_USER_REDO;
		}
		returnPrevious(result);
	}
	
	public void onBackPressed() {
		// Enables power-saving
		Log.d(TAG, "onBackPressed called");
		returnPrevious(RESULT_USER_REDO); 
		//super.onBackPressed();
	}
	
	private void returnPrevious(int result) {
		if (playing) {
			playing=false;
			mediaPlayer.stop();
		}
		Intent intent = new Intent();
		intent.putExtra("code", result);
		setResult(RESULT_OK, intent);
		Log.d(TAG, "send result "+Integer.toString(result));
		releaseMediaPlayer();
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
	
}
