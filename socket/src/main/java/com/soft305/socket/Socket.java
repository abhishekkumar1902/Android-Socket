package com.soft305.socket;

import android.support.annotation.NonNull;

/**
 * Created by pablo on 4/28/17.
 */
public abstract class Socket<T extends Socket.Listener> {


    public Socket() {}

    public abstract void open(@NonNull T listener);

    public abstract void close();

    public abstract void write(byte[] data);


    public interface Listener {
        void onOpen();
        void onRead(byte[] data);
    }


    public interface BtListener extends Listener {
        void onError(BtErrorInfo errorInfo);
    }

    public interface BleListener extends Listener {
        void onError(BleErrorInfo errorInfo);
    }

    public interface UsbHostListener extends Listener {
        void onError(UsbHostErrorInfo errorInfo);
    }

    public interface UsbAoaListener extends Listener {
        void onError(UsbAoaErrorInfo errorInfo);
    }


    public static class BtErrorInfo {}

    public static class BleErrorInfo {
        public String info;
    }

    public static class UsbHostErrorInfo {}

    public static class UsbAoaErrorInfo {
        public String info;
    }


}
