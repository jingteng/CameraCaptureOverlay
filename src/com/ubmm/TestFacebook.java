package com.ubmm;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class TestFacebook extends Activity {

	private static final String FACEBOOK_APPID = "291467504260989";
	
    //Facebook facebook;
    
	private static final String FACEBOOK_PERMISSION = "publish_stream";
	private static final String TAG = "FacebookSample";
	private static final String MSG = "Message from FacebookSample";

	private final Handler mFacebookHandler = new Handler();
	private TextView loginStatus;

    final Runnable mUpdateFacebookNotification = new Runnable() {
        public void run() {
        	Toast.makeText(getBaseContext(), "Facebook updated !", Toast.LENGTH_LONG).show();
        }
    };

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.facebooktest);
 
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		//this.facebookConnector.getFacebook().authorizeCallback(requestCode, resultCode, data);
	}


	@Override
	protected void onResume() {
		super.onResume();
		//updateLoginStatus();
	}

}
