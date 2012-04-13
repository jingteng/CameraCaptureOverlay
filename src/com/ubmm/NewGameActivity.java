package com.ubmm;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View.OnClickListener;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

public class NewGameActivity extends Activity implements OnClickListener {
	private String[] s;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.newgame_page);
		
		TextView v1 = (TextView) findViewById(R.id.newgame_word1);
		TextView v2 = (TextView) findViewById(R.id.newgame_word2);
		TextView v3 = (TextView) findViewById(R.id.newgame_word3);

		s = WordUtility.getRandomThreeWords();
		v1.setText(s[0]);
		v2.setText(s[1]);
		v3.setText(s[2]);
		
		v1.setClickable(true);
		v2.setClickable(true);
		v3.setClickable(true);
		
		v1.setOnClickListener(this);
		v2.setOnClickListener(this);
		v3.setOnClickListener(this);
	}
	
	@Override
	public void onClick(View v) {
		int buttonId = v.getId();
		String selected="";
		switch (buttonId) {
		case R.id.newgame_word1:
			selected = s[0];
			break;
		case R.id.newgame_word2:
			selected = s[1];
			break;
		case R.id.newgame_word3:
			selected = s[2];
			break;
		default:
			selected = s[0];			
		}
		
		Intent i = new Intent(getApplicationContext(), CameraActivity.class);
		i.putExtra("word", selected);
		startActivity(i);
		
		finish();
	}
	
}
