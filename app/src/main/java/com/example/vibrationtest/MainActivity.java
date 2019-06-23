package com.example.vibrationtest;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements SensorEventListener{
	private OscilloscopeView oscView;
	private WaveOscilloscope oscX;
	private WaveOscilloscope oscY;
	private WaveOscilloscope oscZ;
	private XYOscilloscope oscXY;
	private Spectrometer spcX;
	private Spectrometer spcY;
	private Spectrometer spcZ;
	private SurfaceLayout[] layouts;

	private Button vibButton,recButton;

	private boolean isVibrating;
	private boolean isRecording;
	private SpectrumRecorder recorder;

	private static final int bufferSize=256;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initView();
		initSensor();
		initMainLoop();
	}

	private static final int colorX=Color.rgb(255,100,0);
	private static final int colorY=Color.rgb(0,255,200);
	private static final int colorZ=Color.rgb(100,100,255);

	@TargetApi(Build.VERSION_CODES.O)
	@SuppressLint("ClickableViewAccessibility")
	private void initView(){
		oscView=findViewById(R.id.osc_view);
		oscX=new WaveOscilloscope(bufferSize,colorX);
		oscY=new WaveOscilloscope(bufferSize,colorY);
		oscZ=new WaveOscilloscope(bufferSize,colorZ);
		oscXY=new XYOscilloscope(bufferSize);
		spcX=new Spectrometer(bufferSize,colorX);
		spcY=new Spectrometer(bufferSize,colorY);
		spcZ=new Spectrometer(bufferSize,colorZ);
		oscX.setRange(0.5f);
		oscY.setRange(0.5f);
		oscZ.setRange(0.5f);
		oscXY.setRange(0.5f);
		spcX.setRange(5f);
		spcY.setRange(5f);
		spcZ.setRange(5f);
		layouts=new SurfaceLayout[7];
		layouts[0]=new SurfaceLayout(oscX,new RectF(0,0,1,0.3f));
		layouts[1]=new SurfaceLayout(oscXY,new RectF(0,0.3f,1,0.7f));
		layouts[2]=new SurfaceLayout(spcX,new RectF(0,0.7f,1,1));
		layouts[3]=new SurfaceLayout(oscY,new RectF(0,0,1,0.3f));
		layouts[4]=new SurfaceLayout(spcY,new RectF(0,0.7f,1,1));
		layouts[5]=new SurfaceLayout(oscZ,new RectF(0,0,1,0.3f));
		layouts[6]=new SurfaceLayout(spcZ,new RectF(0,0.7f,1,1));

		isVibrating=false;
		isRecording=false;
		final Vibrator vibrator=(Vibrator)this.getSystemService(this.VIBRATOR_SERVICE);
		final VibrationEffect vibeEff=VibrationEffect.createWaveform( // Nearly White Noise from Signal
				new long[]{1,1},
				new int[]{128,0},
				0
		);

		vibButton=findViewById(R.id.vib_button);
		vibButton.setOnTouchListener(new View.OnTouchListener(){
			@Override
			public boolean onTouch(View v,MotionEvent event){
				if(event.getAction()!=MotionEvent.ACTION_DOWN){
					return false;
				}
				if(isVibrating){
					vibrator.cancel();
					isVibrating=false;
					vibButton.setTextColor(Color.argb(128,255,255,255));
				}
				else{
					isVibrating=true;
					vibButton.setTextColor(Color.argb(255,255,255,255));
					vibrator.vibrate(vibeEff);
				}
				return false;
			}
		});
		recButton=findViewById(R.id.rec_button);

		recButton.setOnTouchListener(new View.OnTouchListener(){
			@Override
			public boolean onTouch(View v,MotionEvent event){
				if(event.getAction()!=MotionEvent.ACTION_DOWN){
					return false;
				}
				if(isRecording){
					Log.i("Button Rec","Close recorder");
					recorder.close();
					Toast.makeText(MainActivity.this,
							"File "+recorder.getFilename()+" saved to "+recorder.rootFolder,
							Toast.LENGTH_SHORT).show();
					recorder=null;
					isRecording=false;
					recButton.setTextColor(Color.argb(128,255,255,255));
				}
				else{
					Log.i("Button Rec","Open recorder");
					isRecording=true;
					recButton.setTextColor(Color.argb(255,255,255,255));
					Toast.makeText(MainActivity.this,"Start recording",Toast.LENGTH_SHORT).show();
					SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy_MM_dd@hh_mm_ss",Locale.getDefault());
					recorder=new SpectrumRecorder("vib_spec_"+dateFormat.format(new Date()));
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
			float az=event.values[2];
			oscX.append(ax);
			oscY.append(ay);
			oscZ.append(az);
			oscXY.append(ax,ay);
			spcX.append(ax);
			spcY.append(ay);
			spcZ.append(az);
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		//TODO Auto-generated method stub
	}

	private void initMainLoop(){
		final int TIME_INV=20; // 50Hz
		new Timer().scheduleAtFixedRate(new RenderTask(layouts),0,TIME_INV);
	}

	class RenderTask extends TimerTask{
		private final String TAG="Main Run";
		private int cnt=0;

		// Render at 50Hz
		// Record at 1Hz;

		SurfaceLayout[] layouts;
		RenderTask(SurfaceLayout[] layouts_){
			layouts=layouts_;
		}

		@Override
		public void run(){
			// Renew the display data
			oscView.render(layouts);
			cnt++;
			// Record the data
			if(isRecording){
				if(cnt%50==0){
					recButton.setTextColor(Color.argb(255,255,0,0));
					synchronized(this){
						if(recorder!=null){
							recorder.writeFloatArray(spcX.getSpectrum());
							recorder.writeFloatArray(spcY.getSpectrum());
							recorder.writeFloatArray(spcZ.getSpectrum());
							recorder.flush();
						}
					}
				}
				else if(cnt%50==25){
					recButton.setTextColor(Color.argb(255,255,255,255));
				}
			}
		}
	}
}
