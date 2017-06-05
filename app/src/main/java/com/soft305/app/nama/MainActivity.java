package com.soft305.app.nama;

import android.bluetooth.le.ScanResult;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;

import com.soft305.mdb.log.LoggerListener;
import com.soft305.mdb.MdbManager;
import com.soft305.socket.Socket;
import com.soft305.socket.ble.BleManager;
import com.soft305.socket.usbaoa.UsbAoaManager;
import com.soft305.socket.usbhost.UsbDeviceConfiguration;
import com.soft305.socket.usbhost.UsbHostManager;

import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";


    private UsbAoaManager mUsbAoaManager;
    private Socket<Socket.UsbAoaListener> mAoaSocket;

    private UsbHostManager mUsbHostManager;
    private Socket<Socket.UsbHostListener> mUsbHostSocket;

    private BleManager mBleManager;
    private Socket<Socket.BleListener> mBleSocket;

    private MdbManager mMdbManager;
    private TextView mTxtMdbBus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupView();
        setupManagers();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mUsbAoaManager != null) {
            mUsbAoaManager.close();
        }
    }

    private void setupView() {

        findViewById(R.id.btn_begin_session).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: send purchase input to State Machine. It will send
            }
        });

        findViewById(R.id.btn_clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTxtMdbBus.setText("");
            }
        });

        mTxtMdbBus = (TextView) findViewById(R.id.txt_mdb_bus);
        mTxtMdbBus.setMovementMethod(new ScrollingMovementMethod());
    }

    private void setupManagers() {

        //**********************************************
        //****************** USB AOA *******************
        //**********************************************
        mUsbAoaManager = new UsbAoaManager(this);
        mUsbAoaManager.probe(new UsbAoaManager.Listener() {
            @Override
            public void onSelectUsbAoa(UsbAccessory[] accessoryArray) {
                mUsbAoaManager.selectAoa(accessoryArray[0]);
            }

            @Override
            public void onSocketCreated(Socket<Socket.UsbAoaListener> socket) {
                mAoaSocket = socket;

                //Here is the connection with the MDB Library
                mMdbManager = new MdbManager(mAoaSocket);
                mMdbManager.setLoggerListener(mLoggerListener);
                mMdbManager.start();

            }
        });


        //**********************************************
        //****************** USB HOST ******************
        //**********************************************
        mUsbHostManager = new UsbHostManager(this);
        mUsbHostManager.probe(new UsbHostManager.Listener() {
            @Override
            public void onSelectUsbDevice(Map<String, UsbDevice> usbDeviceMap) {

            }

            @Override
            public void onSocketCreated(Socket<Socket.UsbHostListener> socket) {

            }

            @Override
            public UsbDeviceConfiguration onProvideDeviceConfiguration(UsbDevice usbDevice) {
                UsbDeviceConfiguration configuration = new UsbDeviceConfiguration();
                configuration.deviceInterface = usbDevice.getInterface(0);
                configuration.readEndPoint = configuration.deviceInterface.getEndpoint(0);
                configuration.writeEndPoint = configuration.deviceInterface.getEndpoint(1);

                return configuration;
            }
        });


        //**********************************************
        //******************** BLE *********************
        //**********************************************
        mBleManager = new BleManager(this);
        mBleManager.probe(new BleManager.Listener() {

            @Override
            public void onBleNotSupported() {

            }

            @Override
            public void onBluetoothOff() {

            }

            @Override
            public void onBleScanFailed(int errorCode) {

            }

            @Override
            public void onSelectBleDevice(List<ScanResult> scanResult) {

            }

            @Override
            public void onSocketCreated(Socket<Socket.BleListener> bleSocket) {

            }
        });
    }


    private LoggerListener mLoggerListener = new LoggerListener() {

        @Override
        public void onError(final String errorInfo) {
            // This method is called from a handler thread
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTxtMdbBus.append("\n**************************************************\n");
                    mTxtMdbBus.append(errorInfo + "\n");
                }
            });
        }

        @Override
        public void onInputVmcData(final String dataHexFormat) {
            // This method is called from a handler thread
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTxtMdbBus.append(" << : " + dataHexFormat + "\n");
                }
            });

        }

        @Override
        public void onOutputVmcData(final String dataHexFormat) {
            // This method is called from a handler thread
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTxtMdbBus.append(" >> : " + dataHexFormat + "\n");
                }
            });
        }
    };

}
