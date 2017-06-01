package com.soft305.socket.usbhost;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import com.soft305.socket.Socket;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/* package */ class UsbHostSocket extends Socket<Socket.UsbHostListener> {

    private static final String TAG = "UsbHostSocket";
    private UsbHostManager mUsbHostManager;
    private UsbDevice mUsbDevice;
    private UsbHostListener mListener;
    private UsbDeviceConnection mDeviceConnection;
    private InputStream mInputStream;
    private OutputStream mOutputStream;
    private ReceiverThread mReceiverThread;
    private SenderThread mSenderThread;

    private boolean mIsPendingPermission;
    private boolean mIsConnected;
    private boolean isError;

    /* package */ UsbHostSocket(@NonNull UsbHostManager usbHostManager
            , @NonNull UsbDevice usbDevice) {

        mUsbHostManager = usbHostManager;
        mUsbDevice = usbDevice;
    }

    public void setPendingPermission(boolean pendingPermission) {
        mIsPendingPermission = pendingPermission;
    }

    public boolean isPendingPermission() {
        return mIsPendingPermission;
    }

    @Override
    public void open(@NonNull UsbHostListener listener) {

        mListener = listener;

        if (mIsConnected) {
            mListener.onOpen();
            return;
        }

        mDeviceConnection = mUsbHostManager.provideManager().openDevice(mUsbDevice);

        if (mDeviceConnection == null) {
            // Return if the connection could not be opened
            return;
        }

        mIsConnected = true;
        /*if (mFileDescriptor != null) {
            FileDescriptor fd = mFileDescriptor.getFileDescriptor();
            mOutputStream = new FileOutputStream(fd);
            mInputStream = new FileInputStream(fd);

            // Prepare handler threads
            mReceiverThread = new ReceiverThread("UsbAoaSocket.ReaderThread");
            mReceiverThread.start();

            mSenderThread = new SenderThread("UsbAoaSocket.SenderThread");
            mSenderThread.start();

            mIsConnected = true;
            mListener.onOpen();

        } else {
            mIsConnected = false;
            UsbAoaErrorInfo errorInfo = new UsbAoaErrorInfo();
            errorInfo.info = "Open Accessory Fail";
            mListener.onError(errorInfo);
        }*/

    }

    @Override
    public void close() {
        if (mIsConnected) {
            mIsConnected = false;
            mReceiverThread.close();
            mSenderThread.send(new byte[]{'c','l','o','s','e'});
            mSenderThread.close();
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
            //mUsbHosManager.disposeUsbHostSocket(this);
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

    /* package */ void handleUsbDeviceDetach() {
        handleError();
    }

    /* package */ UsbDevice getUsbDevice() {
        return mUsbDevice;
    }

    // endregion

}
