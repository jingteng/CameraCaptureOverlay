package com.ubmm;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

public class CameraActivity extends Activity {

	private static final String TAG = "CameraActivity";
	private Camera mCamera;
	private CaptureWindow mPreview;
	private MediaRecorder mMediaRecorder;

	public int camId = 1;

	private boolean isRecording = false;

	Button captureButton, switchButton;

	// Add a listener to the Capture button

	void setCaptureButtonText(String t) {
		captureButton.setText(t);
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
		else
			mMediaRecorder.setOrientationHint(270);

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
		mMediaRecorder.setOutputFile(getOutputMediaFile(MEDIA_TYPE_VIDEO)
				.toString());

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
		}
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
			setCaptureButtonText("Stop");
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
			setCaptureButtonText("Capture");
			isRecording = false;
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

		// Create our Preview view and set it as the content of our activity.
		initCamera();
		setupPreview();

		switchButton = (Button) findViewById(R.id.button_switch);
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
					startRecording();
				}
			}
		});
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

}