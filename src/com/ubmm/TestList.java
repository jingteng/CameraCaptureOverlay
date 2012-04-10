package com.ubmm;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.android.AsyncFacebookRunner;

import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;
import com.ubmm.SessionEvents.AuthListener;
import com.ubmm.SessionEvents.LogoutListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class TestList extends Activity implements OnItemClickListener {
	private static final String TAG = "TestList";
	
	final static int LOGIN_STAGE = 0;
	final static int LOGGED_STAGE = 1;
	final static int USERINFO_DOWNLOADED_STAGE = 2;
	final static int FRIEND_LIST_DOWNLOADED_STAGE = 3;
	
	int stage = LOGIN_STAGE;
	
	protected ListView friendsList, gamesList;
    protected static JSONArray jsonArray;
    
	private LoginButton mLoginButton;
	public String usrname="Loading";
	public Bitmap[] pics;
	public Bitmap newGamePic;
	public int picNum;
	public Preview mPreview; 
	
	private static final String FACEBOOK_APPID = "291467504260989";
	
	final static int AUTHORIZE_ACTIVITY_RESULT_CODE = 0;
    final static int PICK_EXISTING_PHOTO_RESULT_CODE = 1;
    
	private Handler mHandler;
	String[] main_items = { "Update Status", "App Requests", "Get Friends", "Upload Photo",
            "Place Check-in", "Run FQL Query", "Graph API Explorer", "Token Refresh" };
    String[] permissions = { "offline_access", "publish_stream", "user_photos", "publish_checkins",
            "photo_upload" };

	private TextView mText;
	private ImageView mUserPic;
    public ImageButton currentGame, createGame;
    public boolean viewingCurrent=true;
    
	public ImageView upbanner, botbanner;
	
	public void shrinkList(ListView v) {
		LinearLayout.LayoutParams mParam = new LinearLayout.LayoutParams((int)(407),(int)(0));
		mParam.gravity = Gravity.CENTER;
        v.setLayoutParams(mParam);
		//v.setVisibility(View.INVISIBLE);
	}
	
	public void collapseList(ListView v) {
		//v.setVisibility(View.VISIBLE);
		//TODO: minimum height
		//int fullHeight = v.getCount() * 
		LinearLayout.LayoutParams mParam = new LinearLayout.LayoutParams((int)(407),(int)(400));
		mParam.gravity = Gravity.CENTER;
		
        v.setLayoutParams(mParam);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		// Init Facebook
		mHandler = new Handler();
        // Create the Facebook Object using the app id.
        Utility.mFacebook = new Facebook(FACEBOOK_APPID);
        // Instantiate the asynrunner object for asynchronous api calls.
        Utility.mAsyncRunner = new AsyncFacebookRunner(Utility.mFacebook);

        // restore session if one exists
        SessionStore.restore(Utility.mFacebook, this);
        SessionEvents.addAuthListener(new FbAPIsAuthListener());
        SessionEvents.addLogoutListener(new FbAPIsLogoutListener());
        
        setContentView(R.layout.gamelist);
        //FrameLayout preview = (FrameLayout) findViewById(R.id.main_capture);
        mLoginButton = (LoginButton) findViewById(R.id.login);
        
        mText = (TextView) findViewById(R.id.txt);
        mUserPic = (ImageView) findViewById(R.id.user_pic);

        mLoginButton.init(this, AUTHORIZE_ACTIVITY_RESULT_CODE, Utility.mFacebook, permissions);
        gamesList = (ListView) findViewById(R.id.games_list);
        friendsList = (ListView)findViewById(R.id.friends_list);
        
        upbanner = (ImageView) findViewById(R.id.current_gamelist);
        botbanner = (ImageView) findViewById(R.id.current_gamelistend);
        //upbanner.setVisibility(View.INVISIBLE);
        //botbanner.setVisibility(View.INVISIBLE);
        
        shrinkList(friendsList);
        shrinkList(gamesList);
        
        createGame = (ImageButton) findViewById(R.id.create_gamelist);
        createGame.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
            	if (viewingCurrent) {
            		viewingCurrent = false;
            		shrinkList(gamesList);
            		collapseList(friendsList);
            	} else {
            		viewingCurrent = true;
            		shrinkList(friendsList);
            		collapseList(gamesList);
            	}
            }
        });
        
        currentGame = (ImageButton) findViewById(R.id.current_gamelist);
        currentGame.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
            	if (viewingCurrent) {
            		viewingCurrent = false;
            		shrinkList(gamesList);
            		collapseList(friendsList);
            	} else {
            		viewingCurrent = true;
            		shrinkList(friendsList);
            		collapseList(gamesList);
            	}
            }
        });
        if (Utility.mFacebook.isSessionValid())
        	requestUserData();
	}
	
	protected void onResume() {
		// Disables power-saving
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		Log.d(TAG, "onResume called");
		super.onResume();
	}

	public void onBackPressed() {
		// Enables power-saving
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		Log.d(TAG, "onBackPressed called");
		super.onBackPressed();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}
	
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        /*
         * if this is the activity result from authorization flow, do a call
         * back to authorizeCallback Source Tag: login_tag
         */
            case AUTHORIZE_ACTIVITY_RESULT_CODE: {
                Utility.mFacebook.authorizeCallback(requestCode, resultCode, data);
                break;
            }
            /*
             * if this is the result for a photo picker from the gallery, upload
             * the image after scaling it. You can use the Utility.scaleImage()
             * function for scaling
             */
            case PICK_EXISTING_PHOTO_RESULT_CODE: {
                if (resultCode == Activity.RESULT_OK) {
                    Uri photoUri = data.getData();
                    if (photoUri != null) {
                        Bundle params = new Bundle();
                        try {
                            params.putByteArray("photo",
                                    Utility.scaleImage(getApplicationContext(), photoUri));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
//                        params.putString("caption", "FbAPIs Sample App photo upload");
//                        Utility.mAsyncRunner.request("me/photos", params, "POST",
//                                new PhotoUploadListener(), null);
                    } else {
                        Toast.makeText(getApplicationContext(),
                                "Error selecting image from the gallery.", Toast.LENGTH_SHORT)
                                .show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "No image selected for upload.",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }
    
    
	/******************
	 * 
	 * Facebook
	 *
	 */
    
    /*
     * Clicking on a friend should popup a dialog for user to post on friend's
     * wall.
     */
    @Override
    public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
        try {
            final long friendId;
            friendId = jsonArray.getJSONObject(position).getLong("id");
            String name = jsonArray.getJSONObject(position).getString("name");
            Log.d(TAG,"clicked name = "+name);
        } catch (JSONException e) {
        	Log.d(TAG,"Error json");
            showToast("Error: " + e.getMessage());
        }
    }

    /*
     * Callback after the message has been posted on friend's wall.
     */
    public class PostDialogListener extends BaseDialogListener {
        @Override
        public void onComplete(Bundle values) {
            final String postId = values.getString("post_id");
            if (postId != null) {
                showToast("Message posted on the wall.");
            } else {
                showToast("No message posted on the wall.");
            }
        }
    }

    public void showToast(final String msg) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(TestList.this, msg, Toast.LENGTH_LONG);
                toast.show();
            }
        });
    }

    /**
     * Definition of the list adapter
     */
    public class FriendListAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        TestList friendsList;

        public FriendListAdapter(TestList friendsList) {
            this.friendsList = friendsList;
            if (Utility.model == null) {
                Utility.model = new FriendsGetProfilePics();
            }
            Utility.model.setListener(this);
            mInflater = LayoutInflater.from(friendsList.getBaseContext());
        }

        @Override
        public int getCount() {
            return jsonArray.length();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            JSONObject jsonObject = null;
            try {
                jsonObject = jsonArray.getJSONObject(position);
            } catch (JSONException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            View hView = convertView;
            if (convertView == null) {
                hView = mInflater.inflate(R.layout.friend_item, null);
                ViewHolder holder = new ViewHolder();
                holder.profile_pic = (ImageView) hView.findViewById(R.id.profile_pic);
                holder.name = (TextView) hView.findViewById(R.id.name);
                holder.info = (TextView) hView.findViewById(R.id.info);
                hView.setTag(holder);
            }

            ViewHolder holder = (ViewHolder) hView.getTag();
            try {
                
                    holder.profile_pic.setImageBitmap(Utility.model.getImage(
                            jsonObject.getString("id"), jsonObject.getString("picture")));
                
            } catch (JSONException e) {
                holder.name.setText("");
            }
            try {
                holder.name.setText(jsonObject.getString("name"));
            } catch (JSONException e) {
                holder.name.setText("");
            }
            try {
               
                    holder.info.setText(jsonObject.getJSONObject("location").getString("name"));
               
            } catch (JSONException e) {
                holder.info.setText("");
            }
            return hView;
        }

    }

    class ViewHolder {
        ImageView profile_pic;
        TextView name;
        TextView info;
    }
    
    /*
     * callback after friends are fetched via me/friends or fql query.
     */
    public class FriendsRequestListener extends BaseRequestListener {

        @Override
        public void onComplete(final String response, final Object state) {
            //dialog.dismiss();
        	try {
                
                jsonArray = new JSONObject(response).getJSONArray("data");
                
            } catch (JSONException e) {
                //showToast("Error: " + e.getMessage());
            	Log.d(TAG,"json error, message="+response);
                return;
            }
        	runOnUiThread(new Runnable() {
        	     public void run() {
        	    	 
					// stuff that updates ui
					friendsList.setOnItemClickListener(TestList.this);
					friendsList.setAdapter(new FriendListAdapter(TestList.this));
					
					//upbanner.setVisibility(View.VISIBLE);
			        //botbanner.setVisibility(View.VISIBLE);
					collapseList(gamesList);
        	    }
        	});
        	
        }

        public void onFacebookError(FacebookError error) {
            //dialog.dismiss();
            Toast.makeText(getApplicationContext(), "Facebook Error: " + error.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }
    
    /*
     * Callback for fetching current user's name, picture, uid.
     */
    public class UserRequestListener extends BaseRequestListener {

        @Override
        public void onComplete(final String response, final Object state) {
            JSONObject jsonObject;
            try {
                jsonObject = new JSONObject(response);

                final String picURL = jsonObject.getString("picture");
                final String name = jsonObject.getString("name");
                Utility.userUID = jsonObject.getString("id");
                Log.d(TAG,"Json resolved, name="+name);
                
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mText.setText(name);
                        mUserPic.setImageBitmap(Utility.getBitmap(picURL));
                        
                        // start to extract friends data
                        String graph_or_fql = "graph";
                        Bundle params = new Bundle();
                        params.putString("fields", "name, picture, location");
                        Utility.mAsyncRunner.request("me/friends", params,
                                new FriendsRequestListener());
                    }
                });           
            } catch (JSONException e) {
                // TODO Auto-generated catch block
            	Log.d(TAG,"Json exception, message="+response);
                e.printStackTrace();
            }
        }

    }
    
	/*
     * Request user name, and picture to show on the main screen.
     */
    public void requestUserData() {
        //mText.setText("Fetching user name, profile pic...");
        Bundle params = new Bundle();
        params.putString("fields", "name, picture");
        Utility.mAsyncRunner.request("me", params, new UserRequestListener());
    }
    
	  /*
     * The Callback for notifying the application when authorization succeeds or
     * fails.
     */

    public class FbAPIsAuthListener implements AuthListener {

        @Override
        public void onAuthSucceed() {
            requestUserData();
        }

        @Override
        public void onAuthFail(String error) {
            //mText.setText("Login Failed: " + error);
        }
    }

    /*
     * The Callback for notifying the application when log out starts and
     * finishes.
     */
    public class FbAPIsLogoutListener implements LogoutListener {
        @Override
        public void onLogoutBegin() {
            //mText.setText("Logging out...");
        }

        @Override
        public void onLogoutFinish() {
            //mText.setText("You have logged out! ");
            //mUserPic.setImageBitmap(null);
        }
    }
    // End of facebook
}