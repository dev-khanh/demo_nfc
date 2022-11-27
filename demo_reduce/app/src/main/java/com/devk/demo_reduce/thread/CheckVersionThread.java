package com.devk.demo_reduce.thread;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.devk.demo_reduce.globalvariable.*;
import com.devk.demo_reduce.utils.Utils;

/**
 * @author Administrator
 *	检查版本的线程
 */
public class CheckVersionThread extends Thread {

	private Handler handler;
	
	public CheckVersionThread(Handler handler) {
		super();
		this.handler = handler;
	}

	public void run() {
		Log.e("Check version thread", "Enter thread");
		int re = -10;
		byte[] OutBuf = new byte[1024];
		int[] OutBufLen = new int[2];
		int[] InBufLen = new int[2];
		//此处的延时是等待上电完成
		
		//发送三次命令，是为了提高命令的成功率
		for(int retimes = 0; retimes < 10; retimes++) {
			Utils.threadSleep(200);		
			InBufLen[0] = GlobalCommend.Module_VERSION_HARAWARE_VERSION.length;
			re = GlobalCommend.mTransmission.NetExeCution(GlobalCommend.NetHandle,InBufLen, GlobalCommend.Module_VERSION_HARAWARE_VERSION, OutBufLen, OutBuf);
			if (re == 0) {
				Message mes = new Message();
				mes.what = OutBufLen[0];
				mes.obj = OutBuf;
				handler.sendMessage(mes);
				break;
			} else {
				if (retimes == 10) {
					Message mes = new Message();
					mes.what = GlobalCommend.CHECK_HARAWARE_VERSION_FAIL;
					handler.sendMessage(mes);
				}
			}
		}

		Utils.threadSleep(100);
		clearBuff(OutBufLen, OutBuf);
		InBufLen[0] = GlobalCommend.Module_VERSION_SOFT_VERSION.length;

		re = GlobalCommend.mTransmission.NetExeCution(GlobalCommend.NetHandle,InBufLen, GlobalCommend.Module_VERSION_SOFT_VERSION, OutBufLen, OutBuf);
		if (re == 0) {
			Message mes = new Message();
			mes.what = OutBufLen[0];
			mes.obj = OutBuf;
			handler.sendMessage(mes);
		} else {
			Message mes = new Message();
			mes.what = GlobalCommend.CHECK_SOFT_VERSION_FAIL;
			handler.sendMessage(mes);
		}
		
		Utils.threadSleep(600);
		clearBuff(OutBufLen, OutBuf);
		InBufLen[0] = GlobalCommend.GET_READER_FIRMWARE_VERSION.length;
		re = GlobalCommend.mTransmission.NetExeCution(GlobalCommend.NetHandle,InBufLen, GlobalCommend.GET_READER_FIRMWARE_VERSION, OutBufLen, OutBuf);
		if (re == 0) {
			Message mes = new Message();
			mes.obj = OutBuf;
			handler.sendMessage(mes);
			mes.what = OutBufLen[0];
		} else {
			Message mes = new Message();
			mes.what = GlobalCommend.CHECK_FIRMWARE_VERSION_FAIL;
			handler.sendMessage(mes);
		}
	}

	private void clearBuff(int[] outBufLen, byte[] outBuf) {
		for(int i = 0; i < outBuf.length; i++) {
			outBuf[i] = 0;
		}
		outBufLen[0] = 0;
		outBufLen[1] = 0;
	}

}
