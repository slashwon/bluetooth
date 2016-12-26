package com.slashwang.blootoothcommunication;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.slashwang.blootoothcommunication.adapter.BtAdapter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int MSG_SCAN_FINISHED = 10;
    public static final int MSG_FOUND_DEVICE = 20;
    public static final int MSG_SCAN_STARTED = 30;
    private BthHelper mBthHelper;

    public Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case MSG_SCAN_STARTED:
                    mPb.setVisibility(View.VISIBLE);
                    break;
                case MSG_SCAN_FINISHED:
                    mBtAdapter.notifyDataSetChanged();
                    mPb.setVisibility(View.GONE);
                    break;
                case MSG_FOUND_DEVICE:
                    BluetoothDevice newDevice = (BluetoothDevice) msg.obj;
                    mDeviceList.add(newDevice);
                    mBtAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };
    private ProgressBar mPb;
    private List<BluetoothDevice> mDeviceList = new ArrayList<>();
    private BtAdapter mBtAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBthHelper = new BthHelper(this,mHandler);

        findViewById(R.id.btn_scan_start).setOnClickListener(this);
        ListView lv = (ListView) findViewById(R.id.lv_device);
        mBtAdapter = new BtAdapter(this, mDeviceList);
        mBtAdapter.init(R.layout.item_device,lv);
        mPb = (ProgressBar) findViewById(R.id.pb_loading);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mBthHelper.onActiveResult(requestCode,resultCode,data);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_scan_start:
                startScan();
                break;
        }
    }

    private void startScan() {
        mBthHelper.startScan();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBthHelper.onDestroy();
    }

    public BthHelper getBtHelper() {
        return mBthHelper;
    }
}
