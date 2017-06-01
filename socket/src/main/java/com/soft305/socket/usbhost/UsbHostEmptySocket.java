package com.soft305.socket.usbhost;

import android.hardware.usb.UsbDevice;
import android.support.annotation.NonNull;


/* package */ class UsbHostEmptySocket extends UsbHostSocket {


    UsbHostEmptySocket(@NonNull UsbHostManager usbHostManager, UsbDevice usbDevice) {
        super(usbHostManager, usbDevice);
    }

    @Override
    public void open(@NonNull UsbHostListener listener) {

    }

    @Override
    public void close() {

    }

    @Override
    public void write(byte[] data) {

    }
}
