package com.SysiotNetDriver;


import android.util.Log;

public class Transmission {

	public native int NetConnect(String NetIpStr,int iPort);
	public native int NetExeCution(int iReadHandle, int[] InBufLen,byte[] InBuf,int[] OutBufLen,byte[] OutBuf);
	public native int NetDisconnet(int iReadHandle);
	
	public native int NetMultiTagReadStart(int iReadHandle,int[] InBufLen,byte[] InBuf);
	
	public native int NetMultiTagGetData(int iReadHandle,int[] OutBufLen,byte[] OutBuf);
	
	public native int NetMultiTagReadStop(int iReadHandle);

	static {
        try {  
        	Log.i("JNI", "Trying to load so");
        	System.loadLibrary("SYSIOT_NET_Driver");
        }  
        catch (UnsatisfiedLinkError ule) {  
        	Log.e("JNI", "WARNING: Could not load so");
        }  
	}
}
