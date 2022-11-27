package com.devk.demo_reduce.globalvariable;

import android.content.Context;

import com.SysiotNetDriver.Transmission;


public class GlobalCommend {
	public static Transmission mTransmission;
	public static String NetIpStr="192.168.0.200";
	public static int NetPort=200;
	public static int NetHandle=0;
	public static Context globalContext = null;	
	
	/**
	 * 以下是自己定义的命令码
	 */
	public static final int GET_TAG_DATA_FAIL = 100;
	public static final int CHECK_FIRMWARE_VERSION_FAIL = 104;
	public static final int CHECK_HARAWARE_VERSION_FAIL = 105;
	public static final int CHECK_SOFT_VERSION_FAIL = 106;
	public static final int DEVICE_ADDRESS = 0xff;
	public static final int STOP_MULTI_TAG_INVENTORY_FAIL = 113;
	/**
	 * 错误的命令码
	 */
	public static final int COMMUNICATION_FAILURE = 90; 
	public static final int FAIL_OK = 0x00; 					// Respons OK, No error.

	//发送命令的命令码
	public static final int GET_READER_VERSION_CMDH = 0x03;
	public static final int GET_READER_VERSION_CMDL = 0x00;
	public static final int GET_READER_MAIN_VERSION_CMDL = 0x01;
	
	public static final int DEVICE_ADDR_CMDH = 0x05;
	public static final int GET_DEVICE_ADDR_CMDL = 0x01;
	public static final int SET_DEVICE_ADDR_CMDL = 0x00;
	
	public static final int RESET_CMDH = 0x0f;
	public static final int RESET_CMDL = 0x00;

	public static final int SET_REGION_CMDH = 0x30;

	public static final int SET_RFCH_CMDH = 0x32;

	public static final int FHSS_ON_OFF_CMDH = 0x37;

	public static final int SET_TRANSMITTING_POWER_CMDH = 0x3B;

	public static final int GET_TRANSMITTING_POWER_CMDH = 0x3C;

	public static final int SET_CONTINUOUS_CARRIER_CMDH = 0x3D;

	public static final int ANTENNA_PARAMETER_SET_CMDL = 0x00;
	
	public static final int ANTENNA_PARAMETER_GET_CMDH = 0x3F;

	public static final int ANTENNA_SWITCH_SET_CMDL = 0x02;
	
	public static final int ANTENNA_SWITCH_GET_CMDL = 0x03;
	
	public static final int STOP_MULTI_TAG_TNVENTORY_CMD = 0xC0;
	
	public static final int MULTI_READ_CMDH = 0xC1;

	public static final int SINGLE_TAG_INVENTORY_CMDH = 0xC8;

	public static final byte[] Module_VERSION_HARAWARE_VERSION = { (byte) 0xFF, 0x06, 0x03, 0x00, 0x00 };
	public static final byte[] Module_VERSION_SOFT_VERSION     = { (byte) 0xFF, 0x06, 0x03, 0x00, 0x01 };
	public static final byte[] GET_READER_FIRMWARE_VERSION     = { (byte) 0xFF, 0x05, 0x03, 0x01 };

}
