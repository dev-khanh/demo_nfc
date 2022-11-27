package com.project_rend_nfc.newarchitecture.modules.nfc;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.SysiotNetDriver.Transmission;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.project_rend_nfc.newarchitecture.modules.nfc.globalvariable.GlobalCommend;
import com.project_rend_nfc.newarchitecture.modules.nfc.globalvariable.GolbalVariable;
import com.project_rend_nfc.newarchitecture.modules.nfc.globalvariable.SearchTagInfo;
import com.project_rend_nfc.newarchitecture.modules.nfc.globalvariable.SearchTagInfoList;
import com.project_rend_nfc.newarchitecture.modules.nfc.thread.CheckVersionThread;
import com.project_rend_nfc.newarchitecture.modules.nfc.thread.RecvMuiltTagDataThread;
import com.project_rend_nfc.newarchitecture.modules.nfc.thread.SendCmdThread;
import com.project_rend_nfc.newarchitecture.modules.nfc.utils.Utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class NFCModules extends ReactContextBaseJavaModule implements CompoundButton.OnCheckedChangeListener  {

    private MyHandler handler;
    static final String TAG = "DEVK";
    private ExecutorService MultiTagReadThreadPool;
    private ExecutorService sendCmdThreadPool;
    private static int successCount = 0;
    private String HexStr;
    private android.content.IntentFilter IntentFilter;


    private static int ReciverCount = 0;
    private RecvMuiltTagDataThread multTagSearchThread;


    private String port = "200";

    private boolean isEnableMultiReadTag = true;

    public NFCModules(ReactApplicationContext reactContext) {
       super(reactContext);
    }

    @Override
    public String getName() {
       return "NFCModules";
    }

    @ReactMethod
    public void init() {
        GlobalCommend.mTransmission = new Transmission(); // Step 3
        initData();
        Utils.initSoundPool();
        InitThreadPool();
        checkBattery();
    }

    @ReactMethod
    public void connect(){
        ConnectReader(true);
    }

    @ReactMethod
    public void disConnect(){
        DisconnectReader();
    }

    @ReactMethod
    public void reConnect(){
        byte[] parameterBuff = new byte[50];
        int index = 0;
        parameterBuff[index++] = (byte) GlobalCommend.DEVICE_ADDRESS;
        parameterBuff[index++] = (byte) GlobalCommend.RESET_CMDH;
        parameterBuff[index++] = (byte) GlobalCommend.RESET_CMDL;
        sendCmdThreadPool.submit(new SendCmdThread(GlobalCommend.mTransmission, parameterBuff, index, handler));
    }

    @ReactMethod
    public void readTag(){
        showMultTagSearchListViewDialog();
        if(isEnableMultiReadTag){
            rendConnect();
        }
    }

    @ReactMethod
    public void exit(){
        MultiTagReadThreadPool.shutdownNow();
        GolbalVariable.CyclicVariable = false;
        GlobalCommend.mTransmission.NetMultiTagReadStop(GlobalCommend.NetHandle);
        isEnableMultiReadTag = true;
    }


    @ReactMethod
    public void emitAddress(String address){
        Log.e("DEVK", address);
    }

    @SuppressLint("HandlerLeak")
    class MyHandler extends Handler {
        @SuppressLint({ "HandlerLeak", "DefaultLocale" })
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            byte[] EPCBuffdata = new byte[50];
            String EPC = "";
            int OutBufLen = msg.what;
            byte[] OutBuf = (byte[]) msg.obj;
            Log.e(TAG+" message", "message" + msg.obj + msg.what);
            try {
                switch ((OutBuf[2])) {
                    case GlobalCommend.RESET_CMDH:
                        if (OutBuf[4] == GlobalCommend.FAIL_OK) {
                            DisplayToast("Reset Sucsess!");
                            Utils.playSound(1, 0);
                        }
                        break;
                    case GlobalCommend.GET_READER_VERSION_CMDH:
                        if (OutBuf[3] == GlobalCommend.GET_READER_VERSION_CMDL && OutBuf[4] == GlobalCommend.FAIL_OK) {
                            checkReturnMainVersionData(OutBufLen, OutBuf);
                        } else if (OutBuf[3] == GlobalCommend.GET_READER_MAIN_VERSION_CMDL
                                && OutBuf[4] == GlobalCommend.FAIL_OK) {
                            checkReturnModuleVersionData(OutBufLen, OutBuf);
                        }
                        break;

                    case GlobalCommend.DEVICE_ADDR_CMDH:
                        if (OutBuf[3] == GlobalCommend.GET_DEVICE_ADDR_CMDL && OutBuf[4] == GlobalCommend.FAIL_OK) {
                            String deviceAddr = Integer.toHexString(OutBuf[5] & 0xff);
                            Log.e(TAG, "Get Reader's Devcie Address : 0x" + deviceAddr.toUpperCase());
                        } else if (OutBuf[3] == GlobalCommend.SET_DEVICE_ADDR_CMDL && OutBuf[4] == GlobalCommend.FAIL_OK) {
                            DisplayToast("set Reader to new Device Address: " + (OutBuf[5] & 0xff));
                        }
                        break;

                    case GlobalCommend.SET_REGION_CMDH:
                        if (OutBuf[4] == GlobalCommend.FAIL_OK) {
                            DisplayToast("set Region Ok!");
                            Utils.playSound(1, 0);
                        }
                        break;

                    case GlobalCommend.SET_RFCH_CMDH:
                        if (OutBuf[4] == GlobalCommend.FAIL_OK) {
                            DisplayToast("set RFCH Success");
                            Utils.playSound(1, 0);
                        }
                        break;

                    case GlobalCommend.FHSS_ON_OFF_CMDH:
                        if (OutBuf[4] == GlobalCommend.FAIL_OK) {
                            DisplayToast("FHSS Set success!");
                            Utils.playSound(1, 0);
                        }
                        break;

                    case GlobalCommend.SET_CONTINUOUS_CARRIER_CMDH:
                        if (OutBuf[4] == GlobalCommend.FAIL_OK) {
                            DisplayToast("Set CONTINUOUS CARRIER Success!");
                            Utils.playSound(1, 0);
                        }
                        break;

                    case GlobalCommend.SET_TRANSMITTING_POWER_CMDH:
                        if (OutBuf[4] == GlobalCommend.FAIL_OK) {
                            DisplayToast("Set PA_Power Success!");
                            Utils.playSound(1, 0);
                        }
                        break;

                    case GlobalCommend.GET_TRANSMITTING_POWER_CMDH:
                        if (OutBuf[4] == GlobalCommend.FAIL_OK) {
                            int Power = ((OutBuf[5] << 8) | (OutBuf[6] & 0xff)) / 100;
                            String PAPower = Power + "";
                            Log.e(TAG+" Information", "Current PA_Power is " + PAPower + "dBm");
                            Log.e(TAG+" PA_Power", PAPower);
                        }
                        break;

                    case GlobalCommend.ANTENNA_PARAMETER_GET_CMDH:
                        if (OutBuf[3] == GlobalCommend.ANTENNA_PARAMETER_SET_CMDL && OutBuf[4] == GlobalCommend.FAIL_OK) {
                            DisplayToast("set Antenna Parameters Success!");
                            Utils.playSound(1, 0);

                        } else if (OutBuf[3] == GlobalCommend.ANTENNA_SWITCH_GET_CMDL && OutBuf[4] == GlobalCommend.FAIL_OK) {

                            GolbalVariable.AntRadio=OutBuf[5];
                            Log.e(TAG+" Information", "Current work Ant port is Ant" + OutBuf[5] );


                        } else if (OutBuf[3] == GlobalCommend.ANTENNA_SWITCH_SET_CMDL && OutBuf[4] == GlobalCommend.FAIL_OK) {
                            if (OutBuf[4] == GlobalCommend.FAIL_OK) {
                                DisplayToast("Switch Ant success!");
                                Utils.playSound(1, 0);
                            }
                        }
                        break;

                    case (byte) GlobalCommend.MULTI_READ_CMDH:
                        if (OutBuf[4] == GlobalCommend.FAIL_OK) {
                            int readTagCount = 0;
                            int RSSI = OutBuf[5];
                            int PC = (((OutBuf[6] << 8) | OutBuf[7]) >> 11) * 2;
                            int ANT = OutBuf[5 + 5 + PC];
                            if (PC > 0) {
                                System.arraycopy(OutBuf, 8, EPCBuffdata, 0, PC);
                                EPC = Utils.byteToHexstring(PC, EPCBuffdata);

                                SearchTagInfo sInfo = new SearchTagInfo(EPC, readTagCount, RSSI, ANT);
                                refreshMultTagListView(sInfo);
                            }
                        }
                        break;

                    case (byte) (GlobalCommend.SINGLE_TAG_INVENTORY_CMDH)://not use
                        if (OutBuf[4] == GlobalCommend.FAIL_OK) {
                            int readTagCount = 0;
                            int RSSI = OutBuf[5];
                            int PC = (((OutBuf[6] << 8) | OutBuf[7]) >> 11) * 2;
                            int ANT = OutBuf[5 + 5 + PC];
                            if (PC > 0) {
                                System.arraycopy(OutBuf, 8, EPCBuffdata, 0, PC);
                                EPC = Utils.byteToHexstring(PC, EPCBuffdata);
                                SearchTagInfo sInfo = new SearchTagInfo(EPC, readTagCount, RSSI, ANT);
                                refreshMultTagListView(sInfo);
                            }
                        }
                        break;

                    case (byte) GlobalCommend.STOP_MULTI_TAG_TNVENTORY_CMD:
                        isEnableMultiReadTag = true;
                        Utils.playSound(2, 0);
                        break;
                    default:
                        break;
                }
            } catch (NullPointerException e) {}

            switch (msg.what) {
                case GlobalCommend.CHECK_HARAWARE_VERSION_FAIL:
                    Utils.playSound(2, 0);
                    break;

                case GlobalCommend.CHECK_SOFT_VERSION_FAIL:
                    Utils.playSound(2, 0);
                    break;

                case GlobalCommend.CHECK_FIRMWARE_VERSION_FAIL:
                    Utils.playSound(2, 0);
                    Log.e(TAG+" Warning", "Connect fail,Plesae click Connect button!");
                    break;

                case GlobalCommend.STOP_MULTI_TAG_TNVENTORY_CMD:
                    isEnableMultiReadTag = true;
                    Utils.playSound(2, 0);
                    break;

                case GlobalCommend.GET_TAG_DATA_FAIL:
                    isEnableMultiReadTag = true;
                    Utils.playSound(2, 0);
                    break;

                case GlobalCommend.STOP_MULTI_TAG_INVENTORY_FAIL:
                    isEnableMultiReadTag = true;
                    Utils.playSound(2, 0);
                    break;
                case GlobalCommend.COMMUNICATION_FAILURE:
                    if (msg.obj != null) {
                        StausCodeCheckWithUser(OutBuf[4]);
                    } else {
                        DisplayToast("Communication failure");
                    }
                    Utils.playSound(2, 0);
                    break;


                default:
                    break;
            }
        }
    }

    public void showMultTagSearchListViewDialog() {
        SearchTagInfoList.SearchTagInfoList.clear();
    }

    private void rendConnect(){
        SearchTagInfoList.SearchTagInfoList.clear();
        byte[] parameterBuff = new byte[50];
        int index = 0;
        parameterBuff[index++] = (byte) GlobalCommend.DEVICE_ADDRESS;
        parameterBuff[index++] = (byte) GlobalCommend.MULTI_READ_CMDH;
        parameterBuff[index++] = (byte) 0;
        parameterBuff[index++] = (byte) 0;
        parameterBuff[index++] = (byte) 0x00;
        parameterBuff[index++] = (byte) 0x00;
        // TODO 可以使用线程池的概念去管理当前线程
        if (MultiTagReadThreadPool.isShutdown()) {
            MultiTagReadThreadPool = Executors.newSingleThreadExecutor();
        }
        multTagSearchThread = new RecvMuiltTagDataThread(GlobalCommend.mTransmission, parameterBuff, index, handler);
        MultiTagReadThreadPool.execute(multTagSearchThread);
        isEnableMultiReadTag = false;
        GolbalVariable.CyclicVariable = true;
        successCount = 0;
        Log.e(TAG,"Success:");
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        GolbalVariable.checkBox_AutoPollingValue=0;
        GolbalVariable.checkBox1Value=0;
        GolbalVariable.checkBox2Value=0;
        GolbalVariable.checkBox3Value=0;
        GolbalVariable.checkBox4Value=0;
    }

    private void initData() {
        SearchTagInfoList.SearchTagInfoList.clear();
    }

    private void checkBattery() {
        IntentFilter = new IntentFilter();
        IntentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        getReactApplicationContext().registerReceiver(BatteryChangeReceiver, IntentFilter);
    }

    public void onResume() {
        Log.d(TAG, "onResume");
        Utils.initSoundPool();
    }

    public void onPause() {
        System.gc();
        Log.d(TAG, "onPause");
    }

    public void onDestroy() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int rc=0;
                if (GlobalCommend.NetHandle>0) {
                    rc = GlobalCommend.mTransmission.NetDisconnet(GlobalCommend.NetHandle);
                    GlobalCommend.NetHandle=0;
                }
                Utils.soundPool.release();
                sendCmdThreadPool.shutdown();
                MultiTagReadThreadPool.shutdown();
            }
        }).start();
        getReactApplicationContext().unregisterReceiver(BatteryChangeReceiver);
    }

    private void DisconnectReader(){
        if (GlobalCommend.NetHandle>0) {
            GlobalCommend.mTransmission.NetDisconnet(GlobalCommend.NetHandle);
            GlobalCommend.NetHandle=0;
        }
    }

    private void ConnectReader(boolean CheckString) {
        int rc=0;
        try {
            DisconnectReader();
            Thread.sleep(300);
            if (CheckString) {

                if (false == Utils.isIP("192.168.0.200".replace(" ", ""))) {
                    return;
                }
                if (!checkDataIsNumberPara(port.replace(" ", ""))) {
                    return;
                }

                String str_NetIP = "192.168.0.200".replace(" ", "");
                int NetPort = Integer.parseInt(port.replace(" ", ""));

                GlobalCommend.NetIpStr = str_NetIP;
                GlobalCommend.NetPort = NetPort;
            }
            GlobalCommend.NetHandle = GlobalCommend.mTransmission.NetConnect(GlobalCommend.NetIpStr,GlobalCommend.NetPort);

            if (GlobalCommend.NetHandle>0) {
                Log.d(TAG, "Connect Thanh cong");
                new CheckVersionThread(handler).start(); // 检查底层的版本的线程
            } else {
                Log.d(TAG, "Connect That bai");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public void InitThreadPool() {
        handler = new MyHandler();
        GlobalCommend.globalContext = getReactApplicationContext();
        sendCmdThreadPool = Executors.newSingleThreadExecutor();
        MultiTagReadThreadPool = Executors.newSingleThreadExecutor();
    }

    public String dataBuffMainParsing(int OutBufLen, byte[] OutBuf) {
        HexStr = Utils.byteToHexstring(OutBufLen - 1, OutBuf);
        HexStr = "";
        if (OutBuf[4] == GlobalCommend.FAIL_OK) {
            byte[] Buff = new byte[4];
            StringBuffer FirmwareVersion = new StringBuffer();
            System.arraycopy(OutBuf, 5, Buff, 0, 4);
            HexStr = Utils.byteToHexstring(4, Buff);
            FirmwareVersion.append(HexStr.toString().replace(" ", ""));
            FirmwareVersion.insert(2, ".");
            FirmwareVersion.insert(5, ".");
            FirmwareVersion.insert(8, ".");
            return FirmwareVersion.toString();
        }
        return "Fail";
    }

    public String dataBuffVersionParsing(int OutBufLen, byte[] OutBuf) {
        HexStr = Utils.byteToHexstring(OutBufLen - 1, OutBuf);
        HexStr = "";
        char[] asc = new char[OutBufLen - 4];
        if (OutBuf[4] == GlobalCommend.FAIL_OK) {
            for (int i = 5, j = 0; i < OutBufLen; i++, j++) {
                char str = (char) ((char) OutBuf[i] & 0xff);
                asc[j] = str;
                Log.e(TAG+" Char", "" + str);
            }
            String Str = new String(asc);
            return Str;
        }
        return "Fail";
    }

    private void checkReturnMainVersionData(int outBufLen, byte[] outBuf) {
        if (outBuf[2] == GlobalCommend.GET_READER_VERSION_CMDH && outBuf[3] == GlobalCommend.GET_READER_VERSION_CMDL
                && outBuf[4] == GlobalCommend.FAIL_OK && outBufLen > 13) {
            dataBuffVersionParsing(outBufLen, outBuf);
        } else if (outBuf[2] == GlobalCommend.GET_READER_VERSION_CMDH
                && outBuf[3] == GlobalCommend.GET_READER_VERSION_CMDL && outBuf[4] == GlobalCommend.FAIL_OK
                && outBufLen == 12) {
            dataBuffVersionParsing(outBufLen, outBuf);
        }
    }

    private void checkReturnModuleVersionData(int outBufLen, byte[] outBuf) {
        if (outBuf[2] == GlobalCommend.GET_READER_VERSION_CMDH
                && outBuf[3] == GlobalCommend.GET_READER_MAIN_VERSION_CMDL && outBuf[4] == GlobalCommend.FAIL_OK) {
            String mainVersion = dataBuffMainParsing(outBufLen, outBuf);
            Log.e(TAG+" DEVK successful", mainVersion);
        }
    }

    public void refreshMultTagListView(SearchTagInfo sInfo) {
        successCount++;
        int flagCount = 0;
        int i = 0;
        String EPC = sInfo.getEPC();
        int readTagCount = 0;
        int RSSI = sInfo.getRSSI();
        int ANT = sInfo.getANT();
        if (SearchTagInfoList.SearchTagInfoList.size() != 0) {
            for (flagCount = 0, i = 0; i < SearchTagInfoList.SearchTagInfoList.size(); i++) {
                if (SearchTagInfoList.SearchTagInfoList.get(i).getEPC().replace(" ", "").equals(EPC.replace(" ", ""))) {
                    int reCount = SearchTagInfoList.SearchTagInfoList.get(i).getCount();
                    SearchTagInfoList.SearchTagInfoList.get(i).setCount(reCount + 1);
                    SearchTagInfoList.SearchTagInfoList.get(i).setRSSI(RSSI);
                    SearchTagInfoList.SearchTagInfoList.get(i).setANT(ANT);
                    if (reCount % 10 == 0) {
                        Utils.playSound(1, 0);
                    }
//                    Log.d(TAG+" EPC: ",  SearchTagInfoList.SearchTagInfoList.get(i).getEPC() + " DEVK RSSI: " +SearchTagInfoList.SearchTagInfoList.get(i).getRSSI() + "dBm");
//                    Log.e(TAG+" Count: " , SearchTagInfoList.SearchTagInfoList.get(i).getCount()+"" + " DEVK Ant: " + SearchTagInfoList.SearchTagInfoList.get(i).getANT()+"");

                    WritableMap writableMap =  Arguments.createMap();
                    writableMap.putString("EPC", SearchTagInfoList.SearchTagInfoList.get(i).getEPC() + " DEVK RSSI: " +SearchTagInfoList.SearchTagInfoList.get(i).getRSSI() + "dBm");
                    writableMap.putString("Count", SearchTagInfoList.SearchTagInfoList.get(i).getCount()+"" + " DEVK Ant: " + SearchTagInfoList.SearchTagInfoList.get(i).getANT()+"");
                    sendEvent(getReactApplicationContext(), "INFO_EVENT", writableMap);

                } else {
                    flagCount++;
                }
            }
            if (flagCount == SearchTagInfoList.SearchTagInfoList.size()) {
                readTagCount = 1;
                SearchTagInfo stInfo = new SearchTagInfo(EPC, readTagCount, RSSI, ANT);
                SearchTagInfoList.SearchTagInfoList.add(stInfo);
                Utils.playSound(1, 0);
            }
        } else {
            readTagCount = 1;
            SearchTagInfo stInfo = new SearchTagInfo(EPC, readTagCount, RSSI, ANT);
            SearchTagInfoList.SearchTagInfoList.add(stInfo);
            Utils.playSound(1, 0);
        }
        Log.e(TAG ," Success: "+ successCount);
    }

    private void sendEvent(ReactContext reactContext, String eventName, @Nullable WritableMap params) {
        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName, params);
    }

    public void StausCodeCheckWithUser(byte obj) {
        switch (obj & 0xff) {
            case 0x01:
                DisplayToast("No response and timeout, Error Code(" + Integer.toHexString((obj & 0xff))+")");
                break;
            case 0x09:
                DisplayToast("No tag response while reading a memory on a tag, Error Code(" + Integer.toHexString((obj & 0xff))+")");
                break;
            case 0x10:
                DisplayToast("No tag response while writing a memory on a tag, Error Code(" + Integer.toHexString((obj & 0xff))+")");
                break;
            case 0xA0:
                DisplayToast("Error Code Base while reading a memory on a tag, Error Code(" + Integer.toHexString((obj & 0xff))+")");
                break;
            case 0xB0:
                DisplayToast("Error Code Base while writing a memory on a tag, Error Code(" + Integer.toHexString((obj & 0xff))+")");
                break;
            case 0xC0:
                DisplayToast("Error Code Base while locking a tag, Error Code(" + Integer.toHexString((obj & 0xff))+")");
                break;
            case 0xD0:
                DisplayToast("Error Code Base while killing a tag, Error Code(" + Integer.toHexString((obj & 0xff))+")");
                break;
            case 0x0E:
                DisplayToast("Input Parameter is not right, Error Code(" + Integer.toHexString((obj & 0xff))+")");
                break;
            case 0x12:
                DisplayToast("No tag while killing a tag, Error Code(" + Integer.toHexString((obj & 0xff))+")");
                break;
            case 0x13:
                DisplayToast("No tag while lccking a tag, Error Code(" + Integer.toHexString((obj & 0xff))+")");
                break;
            case 0x16:
                DisplayToast("Access Password is Error, Error Code("  + Integer.toHexString((obj & 0xff))+")");
                break;
            case 0x17:
                DisplayToast("Command undefined, Error Code("  + Integer.toHexString((obj & 0xff))+")");
                break;
            case 0x20:
                DisplayToast("Hopping Frequency is Error, Error Code("  + Integer.toHexString((obj & 0xff))+")");
                break;
            case 0x21:
                DisplayToast("Antenna port is not available, Error Code("  + Integer.toHexString((obj & 0xff))+")");
                break;
            default:
                DisplayToast("Other Error, Error Code("  + Integer.toHexString((obj & 0xff))+")");
                break;
        }
        Utils.playSound(2, 0);
    }

    private boolean checkDataIsNumberPara(String length) {
       if (TextUtils.isEmpty(length)) {
          DisplayToast("Please fill in the parameters");
          Utils.playSound(2, 0);
          return false;
       }
       return true;
    }

    public void DisplayToast(String str) {
       Toast toast = Toast.makeText(getReactApplicationContext(), str, Toast.LENGTH_SHORT);
       toast.show();
    }

    private BroadcastReceiver BatteryChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int rawLevel = intent.getIntExtra("level", -1); // 电池当前的电量
            int scale = intent.getIntExtra("scale", -1); // 电池的总电量
            int status = intent.getIntExtra("status", -1);
            int health = intent.getIntExtra("health", -1);
            Log.e(TAG+" batteryInfo", "rawLevel:" + rawLevel + "scale:" + scale + "status:" + status + "health:" + health);
            if (rawLevel < 30 && ReciverCount < 1) {
                Log.e(TAG+" Warning", "The battery is too low. Please charge it");
                ReciverCount++;
            }
        }
    };
}