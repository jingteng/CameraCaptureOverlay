package com.ubmm;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

public class CameraActivity extends Activity {

	private static final String TAG = "CameraActivity";
	private Camera mCamera;
	private CaptureWindow mPreview;
	private MediaRecorder mMediaRecorder;
	private String videoFileName;

	public int camId = 1;

	private boolean isRecording = false;
	Button captureButton;
	ImageButton switchButton;
	TextView countDown, timing;

	// Add a listener to the Capture button

	void setCaptureButtonText(String t) {
		captureButton.setText(t);
	}

	void setCountDownText(String t) {
		countDown.setText(t);
	}

	private boolean prepareVideoRecorder() {

		// mCamera = getCameraInstance();
		mMediaRecorder = new MediaRecorder();

		// Step 1: Unlock and set camera to MediaRecorder
		// mCamera.stopPreview();
		mCamera.unlock();
		mMediaRecorder.setCamera(mCamera);

		if (camId == 0)
			mMediaRecorder.setOrientationHint(90);
		else {
			int ot = this.getResources().getConfiguration().orientation;
			if (ot==Configuration.ORIENTATION_LANDSCAPE) {
				mMediaRecorder.setOrientationHint(180);
				Log.d(TAG,"hint set to 90 (landscape)");
			} else {
				mMediaRecorder.setOrientationHint(270);
				Log.d(TAG,"hint set to 180 (portrati)");
			}
		}
			

//        int ot = this.getResources().getConfiguration().orientation;
//        int ot2 = 0;
//		switch (ot) {
//
//		case Configuration.ORIENTATION_LANDSCAPE:
//			ot2=0;
//			Log.d("my orient", "ORIENTATION_LANDSCAPE");
//			break;
//		case Configuration.ORIENTATION_PORTRAIT:
//			ot2=90;
//			Log.d("my orient", "ORIENTATION_PORTRAIT");
//			break;
//		case Configuration.ORIENTATION_SQUARE:
//			ot2=180;
//			Log.d("my orient", "ORIENTATION_SQUARE");
//			break;
//		case Configuration.ORIENTATION_UNDEFINED:
//			ot2=270;
//			Log.d("my orient", "ORIENTATION_UNDEFINED");
//			break;
//		default:
//			Log.d("my orient", "default val");
//			break;
//		}
//		if (camId==1) ot2=360-ot2;
//		if (ot2<0) ot2=ot2+360;
//		if (ot2>=360) ot2=0;
//		mMediaRecorder.setOrientationHint(ot2);
		// Step 2: Set sources
		mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
		
		mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

		// Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
		// mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
		mMediaRecorder.setProfile(CamcorderProfile
				.get(CamcorderProfile.QUALITY_LOW));

		// mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
		//
		// mMediaRecorder.setVideoSize(400, 240);
		// mMediaRecorder.setVideoEncodingBitRate(100000);
		//
		// mMediaRecorder.setVideoFrameRate(24);
		// mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
		//
		// mMediaRecorder.setAudioEncoder(0);
		// mMediaRecorder.setMaxDuration(1000);

		// Step 4: Set output file
		videoFileName = getOutputMediaFile(MEDIA_TYPE_VIDEO).toString();
		mMediaRecorder.setOutputFile(videoFileName);

		// Step 5: Set the preview output
		mMediaRecorder.setPreviewDisplay(mPreview.getHolder().getSurface());

		// Step 6: Prepare configured MediaRecorder
		try {
			mMediaRecorder.prepare();
		} catch (IllegalStateException e) {
			Log.d(TAG,
					"IllegalStateException preparing MediaRecorder: "
							+ e.getMessage());
			releaseMediaRecorder();
			return false;
		} catch (IOException e) {
			Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
			releaseMediaRecorder();
			return false;
		}
		return true;
	}

	/** A safe way to get an instance of the Camera object. */
	public Camera getCameraInstance() {
		Camera c = null;
		try {
			c = Camera.open(camId); // attempt to get a Camera instance
		} catch (Exception e) {
			// Camera is not available (in use or does not exist)
			Log.d(TAG,"Camera is not available!");
		}
		Log.d(TAG,"Camera allocated");
		return c; // returns null if camera is unavailable
	}

	public void setupPreview() {
		mPreview = new CaptureWindow(this, mCamera);
		FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
		// preview.removeView(preview.get)
		preview.addView(mPreview);
	}

	public boolean startRecording() {
		if (prepareVideoRecorder()) {
			// Camera is available and unlocked, MediaRecorder is prepared,
			// now you can start recording
			mMediaRecorder.start();

			// inform the user that recording has started
			captureButton.setEnabled(true);
			// captureButton.setBackgroundColor(Color.RED);
			captureButton.setVisibility(View.VISIBLE);
			captureButton.setTextColor(Color.RED);
			setCaptureButtonText("< Recording >");
			isRecording = true;
			return true;
		} else {
			// prepare didn't work, release the camera
			releaseMediaRecorder();
			// inform user
			return false;
		}
	}

	public void stopRecording() {
		if (isRecording) {
			// stop recording and release camera
			mMediaRecorder.stop(); // stop the recording
			releaseMediaRecorder(); // release the MediaRecorder object
			mCamera.lock(); // take camera access back from MediaRecorder

			// inform the user that recording has stopped
			// setCaptureButtonText("Capture");
			captureButton.setEnabled(false);
			captureButton.setVisibility(View.INVISIBLE);
			isRecording = false;

			// release camera and quit
			releaseCamera();

			Intent intent = new Intent(this, ConfirmVideo.class);
			intent.putExtra("videofile", videoFileName);
			final int result = 1;
			startActivityForResult(intent, result);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		//Bundle extras = getIntent().getExtras();
		int result = data.getIntExtra("code",0);
		Log.d(TAG, "get result = " + Integer.toString(result));

		if (result==0) {
			releaseCamera();
			finish();
		} else {
			initCamera();
			setupPreview();
			resetButtons();
		}
	}

	public void initCamera() {
		// Create an instance of Camera
		mCamera = getCameraInstance();
		// Camera.Parameters parameters = mCamera.getParameters();
		// parameters.set("orientation", "portrait");
		// mCamera.setParameters(parameters);

		mCamera.setDisplayOrientation(90);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camera);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		new Handler();
		// Create our Preview view and set it as the content of our activity.
		initCamera();
		setupPreview();

		countDown = (TextView) findViewById(R.id.countdown);
		setCountDownText("");
		switchButton = (ImageButton) findViewById(R.id.button_switch);
		switchButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Toggle camera
				if (camId == 0)
					camId = 1;
				else
					camId = 0;

				stopRecording();
				releaseMediaRecorder(); // if you are using MediaRecorder,
										// release it first
				releaseCamera();

				initCamera();
				setupPreview();
			}
		});

		captureButton = (Button) findViewById(R.id.button_capture);
		captureButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isRecording) {
					stopRecording();
				} else {
					// initialize video camera
					countDown.setTextColor(Color.GREEN);
					switchButton.setVisibility(View.INVISIBLE);
					captureButton.setEnabled(false);
					captureButton.setVisibility(View.INVISIBLE);

					new CountDownTimer(4000, 1000) {

						public void onTick(long millisUntilFinished) {
							setCountDownText(Long
									.toString(millisUntilFinished / 1000));
						}

						public void onFinish() {
							startRecording();
							setCountDownText("");
							countDown.setTextColor(Color.RED);

							new CountDownTimer(10000, 1000) {

								public void onTick(long timeleft) {
									setCountDownText(Long
											.toString(timeleft / 1000));
								}

								public void onFinish() {
									stopRecording();
									setCountDownText("");

								}
							}.start();

						}
					}.start();
				}
			}
		});

		resetButtons();
	}
	
	public void resetButtons() {
		captureButton.setVisibility(View.VISIBLE);
		captureButton.setEnabled(true);
		captureButton.setTextColor(Color.GREEN);
		setCaptureButtonText("<Let's start>");
		
		switchButton.setEnabled(true);
		switchButton.setVisibility(View.VISIBLE);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		releaseMediaRecorder(); // if you are using MediaRecorder, release it
								// first
		releaseCamera(); // release the camera immediately on pause event
	}

	private void releaseMediaRecorder() {
		if (mMediaRecorder != null) {
			mMediaRecorder.reset(); // clear recorder configuration
			mMediaRecorder.release(); // release the recorder object
			mMediaRecorder = null;
			mCamera.lock(); // lock camera for later use
		}
	}

	private void releaseCamera() {
		if (mCamera != null) {
			mCamera.release(); // release the camera for other applications
			mCamera = null;
		}
	}

	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;

	/** Create a File for saving an image or video */
	private static File getOutputMediaFile(int type) {
		// To be safe, you should check that the SDCard is mounted
		// using Environment.getExternalStorageState() before doing this.

		File mediaStorageDir = new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
				"CameraCaptureApp");
		// This location works best if you want the created images to be shared
		// between applications and persist after your app has been uninstalled.

		// Create the storage directory if it does not exist
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				Log.d("MyCameraApp", "failed to create directory");
				return null;
			}
		}

		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
				.format(new Date());
		File mediaFile;
		if (type == MEDIA_TYPE_IMAGE) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator
					+ "IMG_" + timeStamp + ".jpg");
		} else if (type == MEDIA_TYPE_VIDEO) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator
					+ "VID_" + timeStamp + ".mp4");
		} else {
			return null;
		}

		return mediaFile;
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

}