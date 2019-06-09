package com.example.vibrationtest;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

public class XYOscilloscope implements DrawableOnSurface{
	private int BUFFER_SIZE;
	private float[] bufferX; // The circulated list buffer containing data
	private float[] bufferY; // The circulated list buffer containing data
	private float range; // Displays in a square
	private int head;

	//private Paint linePaint;
	private Paint dotPaint;
	private static final int radius=10;

	public XYOscilloscope(int bufferSize){
		BUFFER_SIZE=bufferSize;
		bufferX=new float[BUFFER_SIZE];
		bufferY=new float[BUFFER_SIZE];
		head=0;
		range=1;
		dotPaint=new Paint();
	}

	public void setRange(float r){
		range=r;
	}

	public void append(float x,float y){
		bufferX[head]=x;
		bufferY[head]=y;
		if(++head>=BUFFER_SIZE)head=0;
	}

	/**
	 * Draw the wave images on the rect part of the canvas
	 * @param canvas the canvas to draw
	 * @param border the range on the canvas containing the image
	 */
	@Override
	public void draw(Canvas canvas,RectF border){
		// Draw sth
		float wMid=border.centerX();
		float hMid=border.centerY();
		float maxWindow=border.width()<border.height()?border.width():border.height();

		float lastX=0,lastY=0;
		for(int i=0;i<BUFFER_SIZE;i++){
			/*float step=(float)i/BUFFER_SIZE;
			float posX=w*(1-step)+border.left;
			float posY=hMid-bufferX[posI]/range*border.height()/2;*/

			int posI=head-1-i; // The position of the item in buffer to draw
			if(posI<0){
				posI+=BUFFER_SIZE;
			}

			float posX=wMid-bufferX[posI]/range*maxWindow/2;
			float posY=hMid-bufferY[posI]/range*maxWindow/2;

			dotPaint.setColor(Color.HSVToColor(255-i*255/BUFFER_SIZE,new float[]{i*180f/BUFFER_SIZE,1,1}));
			canvas.drawCircle(posX,posY,radius,dotPaint);

			lastX=posX;
			lastY=posY;
		}
		//canvas.drawLine(border.left,border.top,border.right,border.bottom,linePaint);
	}
}
