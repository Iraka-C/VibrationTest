package com.example.vibrationtest;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements SensorEventListener{
	private OscilloscopeView oscView;
	private WaveOscilloscope oscX;
	//private WaveOscilloscope oscY;
	private XYOscilloscope oscXY;
	private Spectrometer spcX;
	private SurfaceLayout[] layouts;

	private boolean isVibrating;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initView();
		initSensor();
		initMainLoop();
	}

	@SuppressLint("ClickableViewAccessibility")
	private void initView(){
		oscView=findViewById(R.id.osc_view);
		oscX=new WaveOscilloscope(256);
		oscXY=new XYOscilloscope(256);
		spcX=new Spectrometer(256);
		oscX.setRange(0.5f);
		oscXY.setRange(0.5f);
		spcX.setRange(5f);
		layouts=new SurfaceLayout[3];
		layouts[0]=new SurfaceLayout(oscX,new RectF(0,0,1,0.3f));
		layouts[1]=new SurfaceLayout(oscXY,new RectF(0,0.3f,1,0.7f));
		layouts[2]=new SurfaceLayout(spcX,new RectF(0,0.7f,1,1));

		isVibrating=false;
		final Vibrator vibrator=(Vibrator)this.getSystemService(this.VIBRATOR_SERVICE);
		oscView.setOnTouchListener(new View.OnTouchListener(){
			@Override
			public boolean onTouch(View v,MotionEvent event){
				if(isVibrating){
					isVibrating=false;
					vibrator.cancel();
				}
				else{
					isVibrating=true;
					vibrator.vibrate(100000);
				}
				return false;
			}
		});
	}

	private void initSensor(){
		SensorManager sensorManager=(SensorManager)getSystemService(Context.SENSOR_SERVICE);
		//Sensor rotationSensor=sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
		//sensorManager.registerListener(this,rotationSensor,SensorManager.SENSOR_DELAY_GAME);
		Sensor mSensor=sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
		sensorManager.registerListener(this,mSensor,SensorManager.SENSOR_DELAY_FASTEST);
	}

	@Override
	public void onSensorChanged(SensorEvent event){
		if(event.sensor.getType()==Sensor.TYPE_LINEAR_ACCELERATION){
			float ax=event.values[0];
			float ay=event.values[1];
			oscX.append(ax);
			oscXY.append(ax,ay);
			spcX.append(ax);
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		//TODO Auto-generated method stub
	}

	private void initMainLoop(){
		final int TIME_INV=20;
		new Timer().scheduleAtFixedRate(new RenderTask(layouts),0,TIME_INV);
	}

	class RenderTask extends TimerTask{
		private final String TAG="Main Run";

		SurfaceLayout[] layouts;
		RenderTask(SurfaceLayout[] layouts_){
			layouts=layouts_;
		}

		@Override
		public void run(){
			// Renew the display data
			oscView.render(layouts);
		}
	}
}
