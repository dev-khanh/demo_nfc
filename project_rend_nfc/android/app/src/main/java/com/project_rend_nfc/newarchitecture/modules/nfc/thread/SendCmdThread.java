package com.project_rend_nfc.newarchitecture.modules.nfc.thread;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.SysiotNetDriver.Transmission;
import com.project_rend_nfc.newarchitecture.modules.nfc.globalvariable.*;
import com.project_rend_nfc.newarchitecture.modules.nfc.utils.Utils;

/**
 * @author Administrator
 *	这是发送命令的线程
 */
public class SendCmdThread extends Thread {
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
	private int debugout = 1;

	public SendCmdThread(Transmission mTransmission, byte[] parameterBuff, int parameterBuffLength, Handler handler) {
		super();
		this.mTransmission = mTransmission;
		this.parameterBuff = parameterBuff;
		this.parameterBuffLength = parameterBuffLength;
		this.handler = handler;
	}

	@Override
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

		int rc = mTransmission.NetExeCution(GlobalCommend.NetHandle,InBufLen, InBuf, OutBufLen, OutBuf);
		if(rc != 0) {
			Message mes = new Message();
			mes.what = GlobalCommend.COMMUNICATION_FAILURE;
			handler.sendMessage(mes);
		} else if(OutBufLen[0] > 0){
			if (debugout == 1) {
				HexStr = "";
				Log.e("rc", rc + "");
				Log.e("OutBufLen", "" + OutBufLen[0]);
				HexStr = Utils.byteToHexstring(OutBufLen[0] - 1, OutBuf);
				Log.e("Receive Packet(OutBuf)", HexStr + "");
			}
			Message mes = new Message();
			mes.what = OutBufLen[0];
			mes.obj = OutBuf;
			handler.sendMessage(mes);
		}
	}
}
