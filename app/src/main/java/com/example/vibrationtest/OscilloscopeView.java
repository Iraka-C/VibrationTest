package com.example.vibrationtest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewTreeObserver;

public class OscilloscopeView extends SurfaceView implements SurfaceHolder.Callback{
	public static String TAG="OscilloscopeView";

	private SurfaceHolder holder=null;
	private volatile boolean isRendering;
	public int W=0,H=0;

	private Rect bgRect;
	private Paint bgPaint=new Paint();

	public OscilloscopeView(Context context,AttributeSet attrs){
		super(context,attrs);
		SurfaceHolder holderT=getHolder();
		holderT.addCallback(this);
		holderT.setFormat(PixelFormat.TRANSLUCENT);
		setZOrderOnTop(true);
		setZOrderMediaOverlay(true);
		isRendering=false;
		bgPaint.setARGB(255,0,0,0);
		//setZOrderMediaOverlay(true);
	}
	public OscilloscopeView(Context context){
		super(context);
	}

	// ====================== Render Methods ======================
	public void render(SurfaceLayout[] layouts){
		if(isRendering){
			return;
		}
		if(holder!=null){
			isRendering=true;
			Canvas canvas=holder.lockCanvas();
			if(canvas==null){
				return;
			}
			canvas.drawRect(bgRect,bgPaint);

			// Draw the images on the canvas
			for(SurfaceLayout layout:layouts){
				layout.item.draw(canvas,new RectF(
						layout.border.left*W,
						layout.border.top*H,
						layout.border.right*W,
						layout.border.bottom*H
				));
			}
			holder.unlockCanvasAndPost(canvas);
			isRendering=false;
		}
	}

	// ====================== Default Methods =========================
	@Override
	public void surfaceCreated(SurfaceHolder holder){
		this.holder=holder;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder,int format,int width,int height){
		W=width;
		H=height;
		bgRect=new Rect(0,0,W,H);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder){
		this.holder=null;
	}
}
