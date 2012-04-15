package com.ubmm;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

public class TransitActivity extends Activity {
	public Class<?> c = null;
	ImageView bgImage, playerPicture;
	TextView playerInfo;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.transit);

		Intent i = getIntent();			

		bgImage = (ImageView) findViewById(R.id.transit_background_image);

		playerPicture = (ImageView) findViewById(R.id.transit_player_picture);
		playerPicture.setImageBitmap(TestList.mProfile.urImage);

		String intent = i.getStringExtra("intent");
		displayBackgroundImage(intent);

		/** Set the name of the player. */
		String info = i.getStringExtra("playerInfo");
		playerInfo = (TextView) findViewById(R.id.transit_player_info);
		playerInfo.setText(info);
		
		String StringClassname = "com.ubmm."+i.getStringExtra("classname");

		if(StringClassname != null) {
			try {
				c = Class.forName(StringClassname);
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void displayBackgroundImage(String m) {
		if (m != null) {
			if (m.equals("guess")) {
				bgImage.setImageResource(R.drawable.tap_to_guess);				
			}
			else if (m.equals("watch")) {
				bgImage.setImageResource(R.drawable.tap_to_watch);				
			} 
			else if (m.equals("act")) {
				bgImage.setImageResource(R.drawable.tap_to_act);
			}
			else return;
		} else return;
	}

	@Override
	public boolean onTouchEvent(MotionEvent e) {
		// MotionEvent reports input details from the touch screen
		// and other input controls. In this case, you are only
		// interested in events where the touch position changed.

		switch (e.getAction()) {
		case MotionEvent.ACTION_UP:
			Intent intent = new Intent(getApplicationContext(), c);
			startActivity(intent);
			finish();
			break;
		}
		return true;
	}
}