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
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.prompt_page);
		
		Intent i = getIntent();
		
		TextView text = (TextView) findViewById(R.id.prompt_text);
		text.setText(i.getStringExtra("message"));
		
		ImageView bgImg = (ImageView) findViewById(R.id.back_image);
		bgImg.setBackgroundResource(i.getIntExtra("imageid",0));

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

