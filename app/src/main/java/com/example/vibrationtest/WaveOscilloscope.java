package com.example.vibrationtest;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

import static com.example.vibrationtest.OscilloscopeView.TAG;

public class WaveOscilloscope implements DrawableOnSurface{
	private int BUFFER_SIZE;
	private float[] buffer; // The circulated list buffer containing data
	private float range; // The wave is displayed in (-range, range)
	private int head;

	private Paint linePaint;

	public WaveOscilloscope(int bufferSize){
		BUFFER_SIZE=bufferSize;
		buffer=new float[BUFFER_SIZE];
		head=0;
		range=1;
		linePaint=new Paint();
		linePaint.setARGB(255,255,255,255);
		linePaint.setStyle(Paint.Style.STROKE);
		linePaint.setStrokeWidth(5f);
		linePaint.setAntiAlias(true);
	}

	public void setRange(float r){
		range=r;
	}

	public void append(float value){
		buffer[head++]=value;
		if(head>=BUFFER_SIZE)head=0;
	}

	/**
	 * Draw the wave images on the rect part of the canvas
	 * @param canvas the canvas to draw
	 * @param border the range on the canvas containing the image
	 */
	@Override
	public void draw(Canvas canvas,RectF border){
		// Draw sth
		float w=border.right-border.left;
		float hMid=border.centerY();

		float lastX=0,lastY=0;
		for(int i=0;i<BUFFER_SIZE;i++){
			float step=(float)i/BUFFER_SIZE;
			float posX=w*(1-step)+border.left;
			int posI=head-1-i; // The position of the item in buffer to draw
			if(posI<0){
				posI+=BUFFER_SIZE;
			}
			float posY=hMid-buffer[posI]/range*border.height()/2;
			if(i>0){
				canvas.drawLine(lastX,lastY,posX,posY,linePaint);
			}
			lastX=posX;
			lastY=posY;
		}
		//canvas.drawLine(border.left,border.top,border.right,border.bottom,linePaint);
	}

}
