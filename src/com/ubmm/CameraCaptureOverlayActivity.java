package com.ubmm;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class CameraCaptureOverlayActivity extends Activity {
	Button startButton, frontButton;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        startButton = (Button) this.findViewById(R.id.startRecorder);
        startButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
            	Intent i = new Intent(getApplicationContext(), CameraActivity.class);
            	startActivity(i);
            }
        });
        
        frontButton = (Button) this.findViewById(R.id.front_page_button);
        frontButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
            	Intent i = new Intent(getApplicationContext(), TestDrawable.class);
            	startActivity(i);
            }
        });
    }
}