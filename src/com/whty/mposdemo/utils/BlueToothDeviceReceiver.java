package com.whty.mposdemo.utils;

import java.util.ArrayList;

import com.whty.bluetooth.manage.util.BluetoothStruct;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

public class BlueToothDeviceReceiver extends BroadcastReceiver {
	public static ArrayList<BluetoothStruct> items;
	private Handler handler;
	private String tag = BlueToothDeviceReceiver.class.getSimpleName();

	public BlueToothDeviceReceiver(Handler mHandler)
	{
		this.handler = mHandler;
		items = new ArrayList<BluetoothStruct>();
	}

	@Override
	public void onReceive(Context context, Intent intent) {

		String action = intent.getAction();
		Bundle b = intent.getExtras();
		Object[] lstName = b.keySet().toArray();

		// 显示所有收到的消息及其细节
		for (int i = 0; i < lstName.length; i++) {
			String keyName = lstName[i].toString();
			Log.d(keyName, String.valueOf(b.get(keyName)));
		}

		if (BluetoothDevice.ACTION_FOUND.equals(action)) {

			BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			int index = findBluetoothDevice(bluetoothDevice.getAddress(), items);
			if (index < 0 && bluetoothDevice.getName() != null) {
				items.add(new BluetoothStruct(bluetoothDevice.getName(), bluetoothDevice.getAddress(), bluetoothDevice));
				handler.obtainMessage(SharedMSG.Device_Found, bluetoothDevice).sendToTarget();
			}
		}

		if (BluetoothDevice.ACTION_NAME_CHANGED.equals(action)) {

			BluetoothDevice bluetoothDevice = intent
					.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			int index = findBluetoothDevice(bluetoothDevice.getAddress(), items);
			if (index >= 0) {
				items.remove(index);
				items.add(new BluetoothStruct(bluetoothDevice.getName(), bluetoothDevice
						.getAddress(), bluetoothDevice));
				handler.obtainMessage(SharedMSG.Device_Found, bluetoothDevice).sendToTarget();
			}

		}

		if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)){
			Log.e(tag,"收到连接蓝牙的广播");
		}

		if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)){
			Log.e(tag,"收到蓝牙连接断开的广播");
			handler.obtainMessage(SharedMSG.Device_Disconnected).sendToTarget();
		}
	}

	private int findBluetoothDevice(String mac, ArrayList<BluetoothStruct> deviceList) {
		for (int i = 0; i < deviceList.size(); i++) {
			if (((BluetoothStruct) deviceList.get(i)).getMac().equals(mac))
				return i;
		}
		return -1;
	}


}
