package com.example.vibrationtest;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.widget.Button;

import java.util.Arrays;

public class Spectrometer implements DrawableOnSurface{
	private int BUFFER_SIZE;
	private float[] buffer; // The circulated list buffer containing data
	private float[] bufferR,bufferI;
	private float[] lastBufferR;
	private FFT fft;
	private float range; // The value of 0dB is range
	private final float mindB=-18; // The minimum value is mindB
	private int head;

	private Paint linePaint;

	// Filter Mode
	private static final int timeFilter=256;
	private static final int spaceFilter=2;

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
		linePaint.setARGB(255,0,255,200);
	}

	public Spectrometer(int bufferSize,int color){
		this(bufferSize);
		linePaint.setColor(color);
	}

	public void setRange(float r){
		range=r;
	}

	public void append(float value){
		buffer[head++]=value;
		if(head>=BUFFER_SIZE)head=0;
	}

	public float[] getSpectrum(){ // Allow async. access
		return Arrays.copyOfRange(lastBufferR,0,BUFFER_SIZE/2);
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
		for(int i=0;i<=BUFFER_SIZE/2;i++){
			double v=Math.hypot(bufferR[i],bufferI[i]);
			double dB=10*Math.log10(v/range);
			if(dB<mindB)dB=mindB;
			bufferR[i]=(float)dB;
			/*if(bufferR[i]>lastBufferR[i]){ // Larger: tracing
				lastBufferR[i]=(lastBufferR[i]*2+bufferR[i])/3;
			}
			else{
				lastBufferR[i]=(lastBufferR[i]*255+bufferR[i])/256;
			}*/
			lastBufferR[i]+=(bufferR[i]-lastBufferR[i])/timeFilter;
		}

		float lastX=0,lastY=0;
		//linePaint.setARGB(255,0,255,200);
		for(int i=0;i<=BUFFER_SIZE/2;i++){
			float step=(float)i/BUFFER_SIZE*2;
			float posX=w*step+border.left;
			float posY=border.top+getLastBuffVal(i)/mindB*border.height(); // spatial filter
			if(i>0){
				canvas.drawLine(lastX,lastY,posX,posY,linePaint);
			}
			lastX=posX;
			lastY=posY;
		}
		/*
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
		}*/
	}

	// ============ Tool ==============
	float getLastBuffVal(int v){
		float sum=0;
		int cnt=0;
		for(int i=v-spaceFilter;i<=v+spaceFilter;i++){
			if(i>=0&&i<=BUFFER_SIZE/2){
				cnt++;
				sum+=lastBufferR[i];
			}
		}
		return sum/cnt;
	}
}
