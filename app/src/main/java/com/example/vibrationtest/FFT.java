package com.example.vibrationtest;

/**
 * Created by Iraka Crow on 2017/3/23.
 */

public class FFT{
	private final int exactN;
	private final int FFT_N_LOG; // FFT_N_LOG <= 13
	private final int FFT_N;
	private final float[] re,im,sinTable,cosTable;
	private final int[] bitReverse;
	
	public FFT(final int N){
		exactN=N;
		FFT_N_LOG=ceilLog2(N);
		FFT_N=1<<FFT_N_LOG;
		re=new float[FFT_N];
		im=new float[FFT_N];
		sinTable=new float[FFT_N>>1];
		cosTable=new float[FFT_N>>1];
		bitReverse=new int[FFT_N];

		for(int i=0;i<FFT_N;i++){
			int k=i,rev=0;
			for(int j=0;j<FFT_N_LOG;j++){
				rev<<=1;
				rev|=k&1;
				k>>>=1;
			}
			bitReverse[i]=rev;
		}
		
		double theta,dt=2*Math.PI/FFT_N;
		for(int i=0;i<(FFT_N>>1);i++){
			theta=i*dt;
			cosTable[i]=(float)Math.cos(theta);
			sinTable[i]=(float)Math.sin(theta);
		}
	}

	public void transform(float[] realIO,float[] imagIO){
		int ir,exchanges=1;
		int idx=FFT_N_LOG-1;
		float cosv,sinv,tmpr,tmpi;
		for(int i=0;i<FFT_N;i++){
			int pos=bitReverse[i];
			if(pos>=exactN){
				re[i]=0;
				im[i]=0;
			}
			else{
				re[i]=realIO[pos];
				im[i]=imagIO[pos];
			}
		}
		
		for(int i=FFT_N_LOG;i>0;i--,idx--){
			for(int j=0;j<exchanges;j++){
				cosv=cosTable[j<<idx];
				sinv=sinTable[j<<idx];
				for(int k=j;k<FFT_N;k+=exchanges<<1){
					ir=k+exchanges;
					tmpr=cosv*re[ir]-sinv*im[ir];
					tmpi=cosv*im[ir]+sinv*re[ir];
					re[ir]=re[k]-tmpr;
					im[ir]=im[k]-tmpi;
					re[k]+=tmpr;
					im[k]+=tmpi;
				}
			}
			exchanges<<=1;
		}
		
		for (int i=0;i<exactN;i++){
			realIO[i]=re[i];
			imagIO[i]=im[i];
		}
	}

	public void transformInv(float[] realIO,float[] imagIO){
		for(int i=0;i<exactN;i++){
			imagIO[i]=-imagIO[i];
		}
		transform(realIO,imagIO);
		for(int i=0;i<exactN;i++){
			realIO[i]/=FFT_N;
			imagIO[i]/=FFT_N;
		}
	}

	// Find an integer k and 2^k>=n (int)
	private static int ceilLog2(int n){
		if(n<=0){
			return 0;
		}
		if(n==1){
			return 1;
		}
		n-=1;
		int cnt=0;
		while(n>0){
			n>>>=1;
			cnt++;
		}
		return cnt;
	}
}