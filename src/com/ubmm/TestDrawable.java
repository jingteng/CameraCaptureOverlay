package com.ubmm;

import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;

public class TestDrawable extends Activity {
	/** Called when the activity is first created. */
	public String usrname="testuser2";
	public Bitmap[] pics;
	public Bitmap newGamePic;
	public int picNum;
	
	public void loadImages() { 
		picNum=5;
		pics = new Bitmap[picNum];
		pics[0]=BitmapFactory.decodeResource(getResources(), R.drawable.p1);
		pics[1]=BitmapFactory.decodeResource(getResources(), R.drawable.p2);
		pics[2]=BitmapFactory.decodeResource(getResources(), R.drawable.p3);
		pics[3]=BitmapFactory.decodeResource(getResources(), R.drawable.p4);
		pics[4]=BitmapFactory.decodeResource(getResources(), R.drawable.p5);
		
		newGamePic=BitmapFactory.decodeResource(getResources(), R.drawable.newgame);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		loadImages();
		Preview mPreview = new Preview(this);
		DrawOnTop mDraw = new DrawOnTop(this);
		setContentView(mPreview);
		addContentView(mDraw, new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
	}
	
	class DrawOnTop extends View {
		private static final String TAG = "DrawView";
		
		private float mPreviousX;
		private float mPreviousY;
		private boolean prepared=false;

		float canvasWidth;
		float canvasHeight;
		
		float padding = 15.0f;
		int unitSize;
		float pich;
		float picw;
		private float bannerh=0;
		private float bannerw=0;
		int p1L, p1R, p1T, p1B; // start point of the first picture
		int pNR; // end point (right) of the last picture
		private boolean pressedButton=false;
		
		Rect createGameButtonPos;

		public DrawOnTop(Context context) {
			super(context);
			// TODO Auto-generated constructor stub
		}

		public void prepareContent(Canvas canvas, Paint paint) {
			if (prepared==false) {
				prepared=true;
				calcPositions(canvas);
			}
		}
		
		public void calcPositions(Canvas canvas) {
			canvasWidth = canvas.getWidth();
			canvasHeight = canvas.getHeight();

			pich = canvasHeight / 2;
			picw = canvasWidth / 2;
			unitSize = (int)picw + (int)padding*2;
			
			bannerh = (canvasHeight - pich)/2 - padding;
			
			int createGameButtonH = (int)(canvasHeight - bannerh - pich - padding*2);
			int createGameButtonW = (int)(canvasWidth/2);
			
			int createGameButtonL = ((int)canvasWidth-createGameButtonW)/2;
			int createGameButtonR = createGameButtonL + createGameButtonW;
			int createGameButtonB = (int)(canvasHeight-2*padding);
			int createGameButtonT = createGameButtonB - createGameButtonH;
			
			Log.d(TAG,"Newgame button: ("+Integer.toString(createGameButtonL)+
					","+Integer.toString(createGameButtonT)+
					","+Integer.toString(createGameButtonR)+
					","+Integer.toString(createGameButtonB)+")");
			
			createGameButtonPos = new Rect(createGameButtonL, createGameButtonT,
					createGameButtonR, createGameButtonB);
			
			if (picNum%2==1) {
				p1L = (int)((canvasWidth-picw)/2) - unitSize*(picNum/2);
			} else {
				p1L = (int)(canvasWidth/2-padding) - unitSize*(picNum/2);
			}
				
			p1T = (int) bannerh;
			p1B = (int) (bannerh+pich);
			p1R = p1L + (int)picw;
			
			pNR = p1R+unitSize*(picNum-1);
			
			Log.d(TAG, "canvas (" + Float.toString(canvasWidth) + ","
						+ Float.toString(canvasHeight) + ")");
			Log.d(TAG, "pic (" + Float.toString(picw) + "," + Float.toString(pich) + ")");
			Log.d(TAG, "bannerh= "+Float.toString(bannerh));
			
			Log.d(TAG, "p1 ( Top " + Integer.toString(p1T) + "," +
						 " Bot " + Integer.toString(p1B) + "," +
						 " Left " + Integer.toString(p1L) + "," +
						 " Right " + Integer.toString(p1R) + ")");
			Log.d(TAG, "unitSize="+Integer.toString(unitSize));
			Log.d(TAG, "Most right point="+Integer.toString(pNR));
		
		}
		
		public void drawBanners(Canvas canvas, Paint mPaint) {
			
			mPaint.setAntiAlias(true);
			mPaint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.BOLD));
			
			float textsize=bannerh;
			
			do {
				mPaint.setTextSize(textsize--);
				bannerw = mPaint.measureText(usrname);
			} while (bannerw>=canvasWidth);
			
			float startPositionX = (canvasWidth - bannerw) / 2;
			float startPositionY = (bannerh + textsize/2) / 2;

			mPaint.setTextAlign(Paint.Align.LEFT);
			mPaint.setColor(Color.WHITE); 
			mPaint.setStyle(Style.FILL); 
			canvas.drawText(usrname, startPositionX, startPositionY, mPaint);
		}
		
		private void drawPictures(Canvas canvas, Paint paint) {
			Rect rect = new Rect(p1L, p1T, p1R, p1B);
			
			// draw create button
			canvas.drawBitmap(newGamePic,null,createGameButtonPos,null);
			
			for (int i=0; i<picNum; i++) {
				canvas.drawBitmap(pics[i], null, rect, null);
				rect.offset(unitSize, 0);
			}
			pNR = p1R+unitSize*(picNum-1);
			
		}
		
		// return -1 if nothing is clicked
		private int getClickId(float x, float y) {
			if (y>p1T && y<p1B) {
				int d = (int)x - p1L;
				int m = d % unitSize;
				if (m<picw) 
					return d/unitSize;
			} else {
				if (createGameButtonPos.contains((int)x, (int)y))
					return picNum;
			}
			return -1;
		}
		
		@Override
		protected void onDraw(Canvas canvas) {
			// TODO Auto-generated method stub
			Paint mPaint = new Paint();
			prepareContent(canvas, mPaint);
			
			drawBanners(canvas, mPaint);

			drawPictures(canvas, mPaint);
			
			super.onDraw(canvas);

		}

		@Override
		public boolean onTouchEvent(MotionEvent e) {
			// MotionEvent reports input details from the touch screen
			// and other input controls. In this case, you are only
			// interested in events where the touch position changed.

			float x = e.getX();
			float y = e.getY();
			
			
			switch (e.getAction()) {

			case MotionEvent.ACTION_DOWN:
				pressedButton = true;
				break;				
			case MotionEvent.ACTION_UP:
				if (pressedButton) {
					Log.d(TAG, "Click!");
					pressedButton=false;
					int clickedId = getClickId(x,y);
					Log.d(TAG, "Selected image:"+Integer.toString(clickedId));
				}
				break;
			case MotionEvent.ACTION_MOVE:
				float dx = x - mPreviousX;
				pressedButton = false;
				
				if ((p1L < 2 * padding) && (pNR >= (canvasWidth - 2 * padding))) {
					p1L = (int) (p1L + dx * 2);
					p1R = (int) (p1R + dx * 2);
				} else {
					if (p1L >= 2 * padding) {
						p1L = p1L - (int) padding;
						p1R = p1R - (int) padding;
					} else {
						p1L += padding;
						p1R += padding;
						pNR = p1R + unitSize * (picNum - 1);
					}
				}
				break;
			}
			mPreviousX = x;
			mPreviousY = y;
			invalidate();
			
			return true;
		}
	}
}



// ----------------------------------------------------------------------
class Preview extends SurfaceView implements SurfaceHolder.Callback {
	SurfaceHolder mHolder;
	Camera mCamera;

	Preview(Context context) {
		super(context);
		// Install a SurfaceHolder.Callback so we get notified when
		// underlying surface is created and destroyed.
		mHolder = getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	public void surfaceCreated(SurfaceHolder holder) {
		// The Surface has been created, acquire the camera and tell it where
		// to draw.
		mCamera = Camera.open(1); // hard coded for face fronting camera
		mCamera.setDisplayOrientation(90);
		try {
			mCamera.setPreviewDisplay(holder);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		// Surface will be destroyed when we return, so stop the preview.
		// Because the CameraDevice object is not a shared resource, it's very
		// important to release it when the activity is paused.
		mCamera.stopPreview();
		mCamera.release();
		mCamera = null;
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		// Now that the size is known, set up the camera parameters and begin
		// the preview.
		Camera.Parameters parameters = mCamera.getParameters();
		parameters.setPreviewSize(w, h);
		mCamera.setParameters(parameters);
		mCamera.startPreview();
	}
}