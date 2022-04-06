package com.dyh.android.printer.receiver;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.dyh.android.printer.OnPrinterNotifyListener;

import com.dyh.android.printer.BluetoothPrintManager;

public class BluetoothStateBroadcastReceive extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        switch (action) {
            case BluetoothDevice.ACTION_ACL_CONNECTED:
            case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                BluetoothPrintManager.getInstance().resetCacheBoundPrinterInfo(context);
                break;
            case BluetoothAdapter.ACTION_STATE_CHANGED:
                int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                switch (blueState) {
                    case BluetoothAdapter.STATE_OFF:
                        BluetoothPrintManager.getInstance().sendNotify(OnPrinterNotifyListener.NotifyMessage.BLUETOOTH_STATE_CLOSED);
                        break;
                    case BluetoothAdapter.STATE_ON:
                        BluetoothPrintManager.getInstance().sendNotify(OnPrinterNotifyListener.NotifyMessage.BLUETOOTH_STATE_OPENED);
                        break;
                }
                break;
        }
    }
}