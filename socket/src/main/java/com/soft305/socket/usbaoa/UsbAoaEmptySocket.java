package com.soft305.socket.usbaoa;

import android.hardware.usb.UsbAccessory;
import android.support.annotation.NonNull;
import com.soft305.socket.Socket;


/* package */ class UsbAoaEmptySocket extends UsbAoaSocket {


    UsbAoaEmptySocket(@NonNull UsbAoaManager aoaManager, UsbAccessory accessory) {
        super(aoaManager, accessory);
    }

    @Override
    public void open(@NonNull Socket.UsbAoaListener listener) {

    }

    @Override
    public void close() {

    }

    @Override
    public void write(byte[] data) {

    }
}
