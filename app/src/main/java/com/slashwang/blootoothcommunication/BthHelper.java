package com.slashwang.blootoothcommunication;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by PICO-USER on 2016/12/23.
 */
public class BthHelper {

    private final Context mContext;
    private final Handler mHandler;
    private BluetoothAdapter mBthAdapter;
    private List<BluetoothDevice> mPairedList = new ArrayList<>();
    private List<BluetoothDevice> mNewDeviceList = new ArrayList<>();
    private ConnetionController mConnetionController;

    public BthHelper(Context context, @Nullable Handler handler){
        mContext = context;
        mHandler = handler;
        init();
    }

    public void init(){
        mBthAdapter = BluetoothAdapter.getDefaultAdapter();
        ((Activity)mContext).startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),0);

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        mContext.registerReceiver(mReceiver,filter);

        initController();
    }

    private void initController() {
        mConnetionController = new ConnetionController();
    }

    public void onActiveResult(int requestCode, int resultCode, Intent data){
        if(requestCode==0){
            if(resultCode == Activity.RESULT_OK){
                Toast.makeText(mContext,"蓝牙已经打开",Toast.LENGTH_LONG).show();
            } else if(requestCode==Activity.RESULT_CANCELED) {
                Toast.makeText(mContext,"取消打开蓝牙",Toast.LENGTH_LONG).show();
            }
        }
    }

    public void startScan() {
        mBthAdapter.startDiscovery();
        Toast.makeText(mContext,"开始扫描",Toast.LENGTH_LONG).show();
    }

    public void onDestroy(){
        mContext.unregisterReceiver(mReceiver);
    }

    public void connectDevice(BluetoothDevice device){
        mConnetionController.start();
        mConnetionController.connect(device,true);
    }

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action){
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    Toast.makeText(mContext,"扫描开始",Toast.LENGTH_LONG).show();
                    mHandler.sendEmptyMessage(MainActivity.MSG_SCAN_STARTED);
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    Toast.makeText(mContext,"扫描结束",Toast.LENGTH_LONG).show();
                    mHandler.sendEmptyMessage(MainActivity.MSG_SCAN_FINISHED);
                    break;
                case BluetoothDevice.ACTION_FOUND:
                    BluetoothDevice newDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    mNewDeviceList.add(newDevice);
                    if(null!=mHandler){
                        Message msg = mHandler.obtainMessage(MainActivity.MSG_FOUND_DEVICE);
                        msg.obj = newDevice;
                        mHandler.sendMessage(msg);
                    }
                    break;
            }
        }
    } ;
}
