package com.soft305.socket.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.ArrayMap;
import com.soft305.socket.Socket;
import java.util.List;
import java.util.Map;

/**
 * Created by pablo on 4/28/17.
 */
public class BleManager {

    private static final String TAG = "BleManager";
    private Context mContext;
    private Listener mListener;
    private BluetoothAdapter mBluetoothAdapter;
    private Map<BluetoothDevice, BleSocket> mBleSocketMap;
    private BleSocket mCurSocket;


    public BleManager(Context context) {
        mContext = context;
        mBleSocketMap = new ArrayMap<>();
    }

    public void probe(Listener listener) {
        mListener = listener;

        if (!mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            mListener.onBleNotSupported();
            return;
        }


        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            //Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            //startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            mListener.onBluetoothOff();
            return;
        }

        startBleScan();
    }

    public void selectBleDevice(BluetoothDevice bleDevice){


    }

    /**
     *   Only call this when the accessory permission has been granted.
     * */
    private void createBleSocket(BluetoothDevice bleDevice) {
        mCurSocket = new BleSocket(this, bleDevice);
        mBleSocketMap.put(bleDevice, mCurSocket);
        mListener.onSocketCreated(mCurSocket);
    }

    public void close() {
        for (BleSocket socket : mBleSocketMap.values()) {
            socket.close();
        }
        mBleSocketMap.clear();
    }

    private void startBleScan() {

    }

    // region: package accessible

    /* package */ void disposeBleSocket(BleSocket bleSocket) {
        mBleSocketMap.remove(bleSocket.getBleDevice());
    }

    /* package */ BluetoothAdapter provideAdapter() {
        return mBluetoothAdapter;
    }

    // endregion


    public interface Listener {
        void onBleNotSupported();
        void onBluetoothOff();
        void onBleScanFailed(int errorCode);
        void onSelectBleDevice(List<ScanResult> scanResult);
        void onSocketCreated(Socket<Socket.BleListener> bleSocket);
    }

}
