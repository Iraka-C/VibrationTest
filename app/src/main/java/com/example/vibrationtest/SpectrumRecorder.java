package com.example.vibrationtest;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

public class SpectrumRecorder{
	public static final String rootFolder=Environment.getExternalStorageDirectory().getPath()+"/VibrationRecorder/";
	private String filename;
	private FileWriter writer=null;

	public SpectrumRecorder(String filename_){ // Record as .csv file
		filename=filename_;
		// Make root folder and output file
		try{
			if(!(new File(rootFolder).isDirectory())){
				if(!(new File(rootFolder).mkdir())){
					throw new IOException();
				}
			}
			writer=new FileWriter(rootFolder+filename+".csv");
		}catch(SecurityException e){
			e.printStackTrace();
		}catch(IOException e){
			e.printStackTrace();
		}
	}

	public void writeFloatArray(float[] data){
		String dataString=Arrays.toString(data);
		String line=dataString.substring(1,dataString.length()-1);
		try{
			if(writer==null){
				return;
			}
			writer.append(line);
			writer.append("\n");
		}catch(IOException e){
			// Closed
		}
	}

	public void flush(){
		try{
			if(writer==null){
				return;
			}
			writer.flush();
		}catch(IOException e){
			// Closed
		}
	}

	// Stop recording and save the file
	public void close(){
		try{
			writer.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}

	public String getFilename(){
		return filename;
	}
}
