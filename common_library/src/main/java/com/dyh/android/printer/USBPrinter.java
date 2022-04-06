package com.dyh.android.printer;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.util.Log;

import java.util.HashMap;

public class USBPrinter {
    private static final String ACTION_USB_PERMISSION = "com.usb.printer.USB_PERMISSION";

    private static USBPrinter mInstance;

    private Context mContext;
    private PendingIntent mPermissionIntent;
    private UsbManager mUsbManager;
    private UsbDeviceConnection mUsbDeviceConnection;

    private UsbEndpoint printerEp;
    private UsbInterface usbInterface;

    private static final int TIME_OUT = 100000;

    public static USBPrinter getInstance() {
        if (mInstance == null) {
            synchronized (USBPrinter.class) {
                if (mInstance == null) {
                    mInstance = new USBPrinter();
                }
            }
        }
        return mInstance;
    }

    /**
     * 初始化打印机，需要与destroy对应
     *
     * @param context 上下文
     */
    public USBPrinter initPrinter(Context context) {
        mContext = context.getApplicationContext();
        mUsbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        mPermissionIntent = PendingIntent.getBroadcast(mContext, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        mContext.registerReceiver(mUsbDeviceReceiver, filter);
        return getInstance();
    }


    private final BroadcastReceiver mUsbDeviceReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d("action", action);
            UsbDevice mUsbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false) && mUsbDevice != null) {
                        connectUsbPrinter(mUsbDevice);
                    } else {
                        UsbPrintManager.getInstance().sendNotify(OnPrinterNotifyListener.NotifyMessage.USB_PERMISSION_REJECT);
                    }
                }
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                if (mUsbDevice != null) {
                    UsbPrintManager.getInstance().sendNotify(OnPrinterNotifyListener.NotifyMessage.USB_DEVICE_DETACHED);
                }
            } else if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                UsbPrintManager.getInstance().sendNotify(OnPrinterNotifyListener.NotifyMessage.USB_DEVICE_ATTACHED);
                if (mUsbDevice != null) {
                    if (!mUsbManager.hasPermission(mUsbDevice)) {
                        mUsbManager.requestPermission(mUsbDevice, mPermissionIntent);
                    }
                }
            }
        }
    };

    public void close() {
        if (mUsbDeviceConnection != null) {
            mUsbDeviceConnection.close();
            mUsbDeviceConnection = null;
        }
        if (null != mContext) {
            mContext.unregisterReceiver(mUsbDeviceReceiver);
            mContext = null;
        }
        mUsbManager = null;
    }

    /**
     * 连接设备
     *
     * @param mUsbDevice
     */
    private void connectUsbPrinter(UsbDevice mUsbDevice) {
        if (mUsbDevice != null) {
            for (int i = 0; i < usbInterface.getEndpointCount(); i++) {
                UsbEndpoint ep = usbInterface.getEndpoint(i);
                if (ep.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                    if (ep.getDirection() == UsbConstants.USB_DIR_OUT) {
                        mUsbDeviceConnection = mUsbManager.openDevice(mUsbDevice);
                        printerEp = ep;
                        if (mUsbDeviceConnection != null) {
                            UsbPrintManager.getInstance().sendNotify(OnPrinterNotifyListener.NotifyMessage.USB_PRINTER_CONNECTED);
                            mUsbDeviceConnection.claimInterface(usbInterface, true);
                            mUsbDeviceConnection.releaseInterface(usbInterface);
                            return;
                        }
                    }
                }
            }
            UsbPrintManager.getInstance().sendNotify(OnPrinterNotifyListener.NotifyMessage.USB_PRINTER_CONNECTED_ERROR);
        } else {
            UsbPrintManager.getInstance().sendNotify(OnPrinterNotifyListener.NotifyMessage.USB_PRINTER_NOT_FOUND);
        }
    }

    public USBPrinter write(byte[] bytes) {
        if (mUsbDeviceConnection != null) {
            UsbPrintManager.getInstance().sendNotify(OnPrinterNotifyListener.NotifyMessage.PRINT_START);
            try {
                int b = mUsbDeviceConnection.bulkTransfer(printerEp, bytes, bytes.length, TIME_OUT);
                Log.i("Return Status", "b-->" + b);
                UsbPrintManager.getInstance().sendNotify(OnPrinterNotifyListener.NotifyMessage.PRINT_FINISH);
            } catch (Exception e) {
                UsbPrintManager.getInstance().sendNotify(OnPrinterNotifyListener.NotifyMessage.PRINT_FAILED_PRINT_ERROR);
            }
        } else {
            UsbPrintManager.getInstance().sendNotify(OnPrinterNotifyListener.NotifyMessage.USB_PRINTER_NOT_FOUND);
        }
        return getInstance();
    }

    /**
     * @return true表示设备已初始化
     */
    public boolean isInit() {
        if (null != mUsbManager) {
            return true;
        }
        return false;
    }

    /**
     * @return true表示设备已连接
     */
    public boolean isConnected() {
        if (null != mUsbDeviceConnection) {
            return true;
        }
        return false;
    }

    public void connect(Context context) {
        if (!isInit()) {
            initPrinter(context);
        }
        // 列出所有的USB设备，并且都请求获取USB权限
        HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
        for (UsbDevice device : deviceList.values()) {
            usbInterface = device.getInterface(0);
            //筛选出打印机
            if (usbInterface.getInterfaceClass() == 7) {
                Log.d("device", device.getVendorId() + "     " + device.getProductId() + "      " + device.getDeviceId());
                Log.d("device", usbInterface.getInterfaceClass() + "");
                if (!mUsbManager.hasPermission(device)) {
                    mUsbManager.requestPermission(device, mPermissionIntent);
                } else {
                    connectUsbPrinter(device);
                }
            }
        }
    }
}