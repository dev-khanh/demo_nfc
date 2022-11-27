package com.project_rend_nfc.newarchitecture.modules.nfc.thread;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.SysiotNetDriver.Transmission;
import com.project_rend_nfc.newarchitecture.modules.nfc.globalvariable.GlobalCommend;
import com.project_rend_nfc.newarchitecture.modules.nfc.globalvariable.GolbalVariable;
import com.project_rend_nfc.newarchitecture.modules.nfc.utils.Utils;

public class RecvMuiltTagDataThread extends Thread {
	private Transmission mTransmission; // Step 2
	private Handler handler;
	private byte[] parameterBuff;
	private byte[] cmd = new byte[50];
	private byte[] InBuf = null;
	private byte[] OutBuf = new byte[1024];
	private int[] InBufLen = new int[2];
	private int[] OutBufLen = new int[2];
	private int parameterBuffLength = 0;
	private int Length = 0;
	private String HexStr = "";
	private int debugout = 0;
	private int i = 0;
	private int rc;
	private long startTime;
	private long endTime;

	public RecvMuiltTagDataThread(Transmission mTransmission, byte[] parameterBuff, int parameterBuffLength, Handler handler) {
		super();
		this.mTransmission = mTransmission;
		this.parameterBuff = parameterBuff;
		this.parameterBuffLength = parameterBuffLength;
		this.handler = handler;
	}

	public void run() {
		Length = Utils.Package(cmd, parameterBuff, parameterBuffLength);
		InBuf = new byte[Length];
		System.arraycopy(cmd, 0, InBuf, 0, Length);
		InBufLen[0] = InBuf[1] - 1;
		InBufLen[1] = 0;

		if (debugout == 1) {
			HexStr = "";
			HexStr = Utils.byteToHexstring(InBufLen[0], InBuf);
			Log.e("Send Packet(InBuf)", HexStr + "");
			Log.e("Line", "---------------------------");
		}
		for (int i = 0; i < 200; i++)
			OutBuf[i] = 0;
		OutBufLen[0] = 0;
		
		rc = mTransmission.NetMultiTagReadStart(GlobalCommend.NetHandle,InBufLen, InBuf);
		if (rc != 0) {
			stopInventoryTag(GlobalCommend.COMMUNICATION_FAILURE);
			return;
		}
		Utils.threadSleep(50);
		measureTimeThread();
		while (GolbalVariable.CyclicVariable) {
			initData(); // OutBufLen, OutBuf 要重新赋初始化
			startTime = System.currentTimeMillis();
			rc = mTransmission.NetMultiTagGetData(GlobalCommend.NetHandle,OutBufLen, OutBuf);
			if (rc != 0) {
				// TODO BUG 这里的多标签盘存，一旦失败以后，以后盘存都会失败
				break;
			} else if (OutBuf[2] == 0xC0) {
				// 这里代表是底层发上来的停止多标签盘存命令
				stopInventoryTag(GlobalCommend.STOP_MULTI_TAG_TNVENTORY_CMD);
				break;
			} else if (OutBuf[4] == GlobalCommend.FAIL_OK) {
				HexStr = Utils.byteToHexstring(OutBufLen[0] - 1, OutBuf);
				Log.e("OutBuf", HexStr);
				Message mes = new Message();
				//TODO 这里注意要是用局部变量传到主线程中去，否则会出现数据滞后或者覆盖的情况
				byte[] buffData = new byte[OutBufLen[0]];
				System.arraycopy(OutBuf, 0, buffData, 0, OutBufLen[0]);
				mes.what = OutBufLen[0];
				mes.obj = buffData;
				handler.sendMessage(mes);
			}
			Log.e("Line", "---------------------------");
		}
		for(int i = 0; i < 3; i++) {
			int rc = mTransmission.NetMultiTagReadStop(GlobalCommend.NetHandle);
			if(rc == 0) {
				break;
			}
		}
	}

	private void initData() {
		for (i = 0; i < 200; i++)
			OutBuf[i] = 1;
		OutBufLen[0] = 0;
		HexStr = new String();
		rc = 0;		
	}

	public void stopInventoryTag(int CMD) {
		int rc = mTransmission.NetMultiTagReadStop(GlobalCommend.NetHandle);
		if(rc != 0) {
			Message mes = new Message();
			mes.what = GlobalCommend.STOP_MULTI_TAG_INVENTORY_FAIL;
			handler.sendMessage(mes);
		} else {
			Message mes = new Message();
			mes.what = CMD;
			handler.sendMessage(mes);
		}
	}
	

	private void measureTimeThread() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					if ((endTime - startTime) > 2000) {
						stopInventoryTag(GlobalCommend.GET_TAG_DATA_FAIL);
						break;
					}
					endTime = System.currentTimeMillis();
				}
			}
		}).start();		
	}
	
}
