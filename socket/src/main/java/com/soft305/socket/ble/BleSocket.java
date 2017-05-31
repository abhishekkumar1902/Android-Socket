package com.soft305.socket.ble;

import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import com.soft305.socket.Socket;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/* package */ class BleSocket extends Socket<Socket.BleListener> {

    private static final String TAG = "BleSocket";
    private BleManager mBleManager;
    private BluetoothDevice mBleDevice;
    private UsbAoaListener mListener;
    private ParcelFileDescriptor mFileDescriptor;
    private InputStream mInputStream;
    private OutputStream mOutputStream;
    private ReceiverThread mReceiverThread;
    private SenderThread mSenderThread;

    private boolean mIsPendingPermission;
    private boolean mIsConnected;
    private boolean isError;

    /* package */ BleSocket(@NonNull BleManager bleManager, @NonNull BluetoothDevice bleDevice) {
        mBleManager = bleManager;
        mBleDevice = bleDevice;
    }

    public void setPendingPermission(boolean pendingPermission) {
        mIsPendingPermission = pendingPermission;
    }

    public boolean isPendingPermission() {
        return mIsPendingPermission;
    }

    @Override
    public void open(@NonNull Socket.BleListener listener) {



    }

    @Override
    public void close() {
        try {

            if (mIsConnected) {
                mIsConnected = false;
                mFileDescriptor.close();
                mReceiverThread.close();
                mSenderThread.send(new byte[]{'c','l','o','s','e'});
                mSenderThread.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void write(byte[] data) {
        mSenderThread.send(data);
    }

    private void handleDataReceived(byte[] inboundData) {
        mListener.onRead(inboundData);
    }

    private void handleError() {
        if (!isError) {
            isError = true;
            close();

        }
    }

    class ReceiverThread extends HandlerThread {

        private static final int MAX_BUF_SIZE = 1024;
        private Handler mHandler;
        private boolean mThreadRunning;


        public ReceiverThread(String name) {
            super(name);
        }

        @Override
        protected void onLooperPrepared() {
            mHandler = new Handler(getLooper());
            mHandler.post(mRunnable);
        }

        public void close() {
            mThreadRunning = false;
        }

        private Runnable mRunnable = new Runnable() {
            @Override
            public void run() {
                int length = 0;
                byte[] reusedBuffer = new byte[MAX_BUF_SIZE];
                byte[] inboundData;
                mThreadRunning = true;

                // Receiving loop
                while (mThreadRunning) {
                    try {

                        length = mInputStream.read(reusedBuffer);

                        if (length >= 0) {
                            inboundData = new byte[length];
                            System.arraycopy(reusedBuffer, 0, inboundData, 0, length);
                            handleDataReceived(inboundData);

                        } else {
                            handleError();
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                        handleError();
                    }
                }

                // Before leaving the Thread close the inputStream.
                try {

                    mInputStream.close();
                    mHandler.removeCallbacksAndMessages(null);
                    mHandler.getLooper().quit();

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        };

    }

    class SenderThread extends HandlerThread {

        private static final int MAX_BUF_SIZE = 1024;
        private Handler mHandler;
        private boolean mThreadStarted;


        public SenderThread(String name) {
            super(name);
        }

        @Override
        public synchronized void start() {
            super.start();
            mHandler = new Handler(getLooper());
            mThreadStarted = true;
        }

        public void send(final byte[] outboundData) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {

                        if (mThreadStarted) {
                            mOutputStream.write(outboundData);
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                        handleError();
                    }
                }
            });
        }

        public void close() {
            try {

                mThreadStarted = false;
                mOutputStream.close();
                mHandler.removeCallbacksAndMessages(null);
                mHandler.getLooper().quit();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    // region: package private

    /* package */ void handleAccessoryDetach() {
        handleError();
    }

    /* package */ BluetoothDevice getBleDevice() {
        return mBleDevice;
    }

    // endregion

}
