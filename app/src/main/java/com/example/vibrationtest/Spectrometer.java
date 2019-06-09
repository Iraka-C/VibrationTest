package com.example.vibrationtest;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

public class Spectrometer implements DrawableOnSurface{
	private int BUFFER_SIZE;
	private float[] buffer; // The circulated list buffer containing data
	private float[] bufferR,bufferI;
	private float[] lastBufferR;
	private FFT fft;
	private float range; // The value of 0dB is range
	private final float mindB=-30; // The minimum value is mindB
	private int head;

	private Paint linePaint;

	public Spectrometer(int bufferSize){
		BUFFER_SIZE=bufferSize;
		buffer=new float[BUFFER_SIZE];
		fft=new FFT(bufferSize);
		bufferR=new float[BUFFER_SIZE];
		bufferI=new float[BUFFER_SIZE];
		lastBufferR=new float[BUFFER_SIZE];

		head=0;
		range=1;
		linePaint=new Paint();
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

		for(int i=0;i<BUFFER_SIZE;i++){
			bufferR[i]=buffer[i];
			bufferI[i]=0;
		}
		fft.transform(bufferR,bufferI);
		for(int i=0;i<BUFFER_SIZE/2;i++){
			double v=Math.hypot(bufferR[i],bufferI[i]);
			double dB=10*Math.log10(v/range);
			if(dB<mindB)dB=mindB;
			bufferR[i]=(float)dB;
			if(bufferR[i]>lastBufferR[i]){ // Larger: tracing
				lastBufferR[i]=(lastBufferR[i]*2+bufferR[i])/3;
			}
			else{
				lastBufferR[i]=(lastBufferR[i]*63+bufferR[i])/64;
			}
		}

		linePaint.setARGB(255,0,255,200);
		float lastX=0,lastY=0;
		for(int i=0;i<BUFFER_SIZE/2;i++){
			float step=(float)i/BUFFER_SIZE*2;
			float posX=w*step+border.left;
			float posY=border.top+lastBufferR[i]/mindB*border.height();
			if(i>0){
				canvas.drawLine(lastX,lastY,posX,posY,linePaint);
			}
			lastX=posX;
			lastY=posY;
		}

		linePaint.setARGB(127,255,255,255);
		lastX=0;
		lastY=0;
		for(int i=0;i<BUFFER_SIZE/2;i++){
			float step=(float)i/BUFFER_SIZE*2;
			float posX=w*step+border.left;
			float posY=border.top+bufferR[i]/mindB*border.height();
			if(i>0){
				canvas.drawLine(lastX,lastY,posX,posY,linePaint);
			}
			lastX=posX;
			lastY=posY;
		}
		//canvas.drawLine(border.left,border.top,border.right,border.bottom,linePaint);
	}
}
