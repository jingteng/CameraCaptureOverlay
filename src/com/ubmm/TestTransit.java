package com.ubmm;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.Window;
import android.widget.ImageView;

public class TestTransit extends Activity {
	ImageView bgImage, playerPicture;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.transit);

		/** Message received from the parent activity */
		String message = "act";
		
		bgImage = (ImageView) findViewById(R.id.transit_background_image);
		
		// TODO Change the player picture to the one retrieved from the server.
		playerPicture = (ImageView) findViewById(R.id.transit_player_picture);
		
		displayBackgroundImage(message);
	}
	
	private void displayBackgroundImage(String m) {
		if (m.equals("guess"))
			bgImage.setImageResource(R.drawable.tap_to_guess);			
		else if (m.equals("watch"))
			bgImage.setImageResource(R.drawable.tap_to_watch);
		else if (m.equals("act"))
			bgImage.setImageResource(R.drawable.tap_to_act);			
		else return;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent e) {

		switch (e.getAction()) {
		case MotionEvent.ACTION_UP:
//			Intent intent = new Intent(getApplicationContext(), c);
//	        startActivity(intent);
	        finish();
			break;
		}
		return true;
	}
}
