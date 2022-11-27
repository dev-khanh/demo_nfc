package com.project_rend_nfc.newarchitecture.modules.nfc.utils;

import android.annotation.SuppressLint;
import android.media.AudioManager;
import android.media.SoundPool;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @author hellokernel
 *
 */
public class Utils {
	
	/******************** 播放音乐的控件 **************************/
	public static SoundPool soundPool; // 播放声音是使用的声音池
	private static HashMap<Integer, Integer> spMap;
	/**
	 * 函数说明：初始化关于声音操作的函数
	 */
	@SuppressLint("UseSparseArrays")
	public static void initSoundPool() {
		soundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
		spMap = new HashMap<Integer, Integer>();
		spMap.put(1, soundPool.load("/system/media/audio/ui/VideoRecord.ogg", 1));
		spMap.put(2, soundPool.load("/system/media/audio/notifications/Argon.ogg", 1));
		spMap.put(3, soundPool.load("/system/media/audio/notifications/Beryllium.ogg", 1));
		spMap.put(4, soundPool.load("/system/media/audio/notifications/Fluorine.ogg", 1));

	}

	/**
	 * 函数说明：播放声音
	 * 
	 * @param sound
	 *            播放声音文件的序号
	 * @param num
	 *            循环的次数
	 */
	public static void playSound(int sound, int num) {
		soundPool.play(spMap.get(sound), 1, 1, 1, num, 1);
	}

	public static int Package(byte[] cmdBuff, byte[] parameterBuff, int parameterBuffLength) {
		int length = 0;
		int index = 0;

		cmdBuff[0] = parameterBuff[0];
		cmdBuff[1] = (byte) (parameterBuffLength + 2);
		cmdBuff[2] = parameterBuff[1];
		cmdBuff[3] = parameterBuff[2];

		for (index = 3; index < parameterBuffLength;  index++) {
			cmdBuff[1 + index] = parameterBuff[index];
		}
		length = parameterBuffLength+1;
		return length;
	}

	public static boolean isIP(String addr)
	{
		if(addr.length() < 7 || addr.length() > 15 || "".equals(addr))
		{
			return false;
		}
		/**
		 * 判断IP格式和范围
		 */
		String rexp = "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}";

		Pattern pat = Pattern.compile(rexp);

		Matcher mat = pat.matcher(addr);

		boolean ipAddress = mat.find();

		//============对之前的ip判断的bug在进行判断
		if (ipAddress==true){
			String ips[] = addr.split("\\.");

			if(ips.length==4){
				try{
					for(String ip : ips){
						if(Integer.parseInt(ip)<0||Integer.parseInt(ip)>255){
							return false;
						}

					}
				}catch (Exception e){
					return false;
				}

				return true;
			}else{
				return false;
			}
		}

		return ipAddress;
	}


	/**
	 * 函数说明：将byte数组转化位16进制的字符串
	 * @param length 所要转化数组的长度
	 * @param buff byte数组
	 * @return 返回转化完成以后的十六进制字符串
	 */
	@SuppressLint("DefaultLocale")
	public static String byteToHexstring(int length, byte[] buff) {
		String HexString = "";
		for (int i = 0; i < length; i++) {
			String hex = Integer.toHexString(buff[i] & 0xff);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			HexString += hex.toUpperCase() + " ";
		}
		return HexString;
	}

	/**
	 * 函数说明：延时
	 * @param time 延时的时间
	 */
	public static void threadSleep(long time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
