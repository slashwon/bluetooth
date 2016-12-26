package com.slashwang.blootoothcommunication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

/**
 * Created by PICO-USER on 2016/12/26.
 */
public class ConnetionController {

    private static final UUID MY_UUID_SECURE =
            UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    private static final int STATE_NONE = 0;
    private static final int STATE_LISTEN = 1;
    private static final int STATE_CONNECTING = 2;
    private static final int STATE_CONNECTED = 3;
    private final BluetoothAdapter mAdapter;

    private int mState;

    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private AcceptThread mSecureAcceptThread;
    private AcceptThread mInsesureAcceptThread;

    public ConnetionController(){
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        setState(STATE_NONE);
    }

    public synchronized int getState() {
        return mState;
    }

    public synchronized void setState(int state) {
        mState = state;
    }

    public synchronized void start(){
        checkAndClose(mConnectThread);
        checkAndClose(mConnectedThread);

        setState(STATE_LISTEN);

        initThread(mInsesureAcceptThread);
        initThread(mSecureAcceptThread);
    }

    private void initThread(BaseBtThread t){
        if(t == null){
            try {
                t = t.getClass().getConstructor(Boolean.class).newInstance(false);
                t.start();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
    }

    private void checkAndClose(BaseBtThread t) {
        if(t!=null){
            t.cancel();
            t = null;
        }
    }

    public synchronized void connect(BluetoothDevice device , boolean secure) {
        Log.d("BLUE_TOOTH"," connect device ");

        if(getState() == STATE_CONNECTING){
            checkAndClose(mConnectThread);
        }
        checkAndClose(mConnectedThread);

        mConnectThread = new ConnectThread(device,secure);
        mConnectThread.start();

        setState(STATE_CONNECTING);
    }

    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device, String socketType){
        stop();

        mConnectedThread = new ConnectedThread(socket,socketType);
        mConnectedThread.start();

        setState(STATE_CONNECTED);
    }

    public synchronized void stop(){
        checkAndClose(mConnectThread);
        checkAndClose(mConnectedThread);
        checkAndClose(mSecureAcceptThread);
        checkAndClose(mInsesureAcceptThread);
        setState(STATE_NONE);
    }

    public synchronized void connectFailed(){

    }

    static abstract class BaseBtThread extends Thread{
        public abstract void cancel();
    }

    class ConnectThread extends BaseBtThread{

        private final BluetoothSocket mSocket;
        private final BluetoothDevice mDevice;
        private String mSocketType;

        public ConnectThread(BluetoothDevice device, boolean secure) {
            mDevice = device;
            BluetoothSocket tmp = null;
            mSocketType = secure ? "secure" : "insecure";

            try {
                if(secure) {
                    tmp = device.createRfcommSocketToServiceRecord(MY_UUID_SECURE);
                } else {
                    tmp = device.createInsecureRfcommSocketToServiceRecord(MY_UUID_INSECURE);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            mSocket = tmp;
        }

        @Override
        public void run() {
            setName("connect Thread "+mSocketType);

            mAdapter.cancelDiscovery();

            try {
                mSocket.connect();
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    mSocket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                connectFailed();
                return;
            }

            synchronized (ConnectThread.this){
                mConnectThread = null;
            }

            connected(mSocket,mDevice,mSocketType);
        }

        @Override
        public void cancel() {
            try {
                mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class AcceptThread extends BaseBtThread{

        private final BluetoothServerSocket mServerSocket;
        private String mSecureType;

        public AcceptThread(boolean secure){
            BluetoothServerSocket tmp = null;
            mSecureType = secure ? "secureType" : "insecureType";

            try {
                if(secure) {
                    tmp = mAdapter.listenUsingRfcommWithServiceRecord("secure", MY_UUID_SECURE);
                } else {
                    tmp = mAdapter.listenUsingInsecureRfcommWithServiceRecord("insecure",MY_UUID_INSECURE);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            mServerSocket = tmp;
        }

        @Override
        public void run() {
            this.setName("acceptThread "+mSecureType);

            BluetoothSocket socket = null;
            while (ConnetionController.this.getState()!=STATE_CONNECTED){
                try {
                    socket = mServerSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if(socket!=null){
                    switch (ConnetionController.this.getState()){
                        case STATE_LISTEN:
                        case STATE_CONNECTING:
                            connected(socket,socket.getRemoteDevice(),mSecureType);
                            break;
                        case STATE_CONNECTED:
                        case STATE_NONE:
                            try {
                                socket.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            break;
                    }
                }
            }
        }

        @Override
        public void cancel() {
            if(mServerSocket!=null){
                try {
                    mServerSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class ConnectedThread extends BaseBtThread{

        private final BluetoothSocket mSocket;
        private final InputStream inStream;
        private final OutputStream outStream;

        public ConnectedThread(BluetoothSocket socket, String socketType){
            mSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            inStream = tmpIn;
            outStream = tmpOut;
        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int len = -1;
            while (ConnetionController.this.getState()==STATE_CONNECTED){
                try {
                    len = inStream.read(buffer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void write(){

        }

        @Override
        public void cancel() {

        }
    }
}
