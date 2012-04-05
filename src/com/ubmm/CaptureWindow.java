package com.ubmm;

import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CaptureWindow extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = "captureWindow";
	private SurfaceHolder mHolder;
    private Camera mCamera;

    public CaptureWindow(Context context, Camera camera) {
        super(context);
        
        Log.d(TAG, "capturewindow constructor");
        mCamera = camera;

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
        	Log.d(TAG,"Surface created");
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (Exception e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
    	Log.d(TAG, "destructor called");
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

    	Log.d(TAG,"surfaceChanged");
        if (mHolder.getSurface() == null){
          // preview surface does not exist
          return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e){
          // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(mHolder);
            int ot = this.getResources().getConfiguration().orientation;
			switch (ot) {

			case Configuration.ORIENTATION_LANDSCAPE:
				mCamera.setDisplayOrientation(0);
				Log.d("my orient", "ORIENTATION_LANDSCAPE");
				break;
			case Configuration.ORIENTATION_PORTRAIT:
				mCamera.setDisplayOrientation(90);
				Log.d("my orient", "ORIENTATION_PORTRAIT");
				break;
			case Configuration.ORIENTATION_SQUARE:
				mCamera.setDisplayOrientation(270);
				Log.d("my orient", "ORIENTATION_SQUARE");
				break;
			case Configuration.ORIENTATION_UNDEFINED:
				mCamera.setDisplayOrientation(0);
				Log.d("my orient", "ORIENTATION_UNDEFINED");
				break;
			default:
				Log.d("my orient", "default val");
				break;
			}
            
            mCamera.startPreview();

        } catch (Exception e){
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }
}