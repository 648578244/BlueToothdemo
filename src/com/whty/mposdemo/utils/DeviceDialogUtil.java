package com.whty.mposdemo.utils;

import java.lang.reflect.Field;

import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.os.Handler;
import android.view.KeyEvent;
import android.widget.ArrayAdapter;

import com.whty.bluetooth.manage.util.BlueToothConfig;
import com.whty.bluetooth.manage.util.BlueToothUtil;
import com.whty.bluetooth.manage.util.BluetoothStruct;



public class DeviceDialogUtil {

	private Handler handler;
	private Dialog mDialog;
	private ArrayAdapter<BluetoothStruct> adapter = null;
	private BluetoothDevice device;
	private BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
	private int No_Device_Selected = 95;
	private int Device_Ensured = 99;

	public DeviceDialogUtil(Handler handler)
	{
		super();
		this.handler = handler;
		device = null;
	}


	private void createDialog(final Context context)
	{
		device = null;
		//adapter = new ArrayAdapter<BluetoothStruct>(context, android.R.layout.select_dialog_singlechoice, BlueToothUtil.items);
		adapter = new ArrayAdapter<BluetoothStruct>(context, android.R.layout.select_dialog_singlechoice, BlueToothDeviceReceiver.items);
		mDialog = new AlertDialog.Builder(context).setTitle("扫描到的蓝牙设备").setSingleChoiceItems(adapter, -1, new DialogInterface.OnClickListener()
		{
			public void onClick(final DialogInterface dialog, final int whichButton)
			{
				BlueToothConfig.cancelDiscovery();
				System.out.println("whichButton:" + whichButton);
				//System.out.println("keys[whichButton]:" + BlueToothUtil.items.get(whichButton).getName());
				System.out.println("keys[whichButton]:" + BlueToothDeviceReceiver.items.get(whichButton).getName());
				//device = BlueToothUtil.items.get(whichButton).getDevice();
				device = BlueToothDeviceReceiver.items.get(whichButton).getDevice();
				System.out.println("device:" + device);

			}
		}).setPositiveButton("重新扫描", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int whichButton)
			{
				btAdapter.cancelDiscovery();
				//BlueToothUtil.items.clear();
				BlueToothDeviceReceiver.items.clear();
				adapter.notifyDataSetChanged();
				btAdapter.startDiscovery();
				dismissDialog(mDialog, false);
			}
		}).setNegativeButton(" 确定 ", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int whichButton)
			{
				if(device != null)
				{
					handler.obtainMessage(Device_Ensured, device).sendToTarget();
					dismissDialog(mDialog, true);
					//BlueToothUtil.items.clear();
					BlueToothDeviceReceiver.items.clear();
					mDialog = null;
					btAdapter.cancelDiscovery();

				}
				else
				{
					dismissDialog(mDialog, false);
					handler.obtainMessage(No_Device_Selected, "未选择设备").sendToTarget();
				}
			}
		}).create();
		mDialog.setCancelable(false);
		mDialog.show();

		/**
		 * 当dialog在页面上的时候，监听此时的返回键
		 */
		mDialog.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				//按下返回键的时候关闭dialog
				if (keyCode == KeyEvent.KEYCODE_BACK)
				{
					dismissDialog(mDialog, true);
					btAdapter.cancelDiscovery();
					//BlueToothUtil.items.clear();
					BlueToothDeviceReceiver.items.clear();
					mDialog = null;
					return true;
				}
				return false;
			}
		});
	}

	/**
	 * 决定是否关闭dialog
	 * @param dialog
	 * @param flag
	 */
	private void dismissDialog(Dialog dialog, boolean flag) {
		try {
			Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
			field.setAccessible(true);
			field.set(dialog, flag);
			dialog.dismiss();
		} catch (Exception e) {

		}
	}

//	public void listDevice(final Context context, final Map<String, BluetoothDevice> map, final Handler handler) {
//		if (mDialog != null) {
//			notifyDataChange(map);
//		} else {
//			createDialog(context);
//			notifyDataChange(map);
//		}
//	}

	public void listDevice(final Context context)
	{
		if (mDialog == null)
		{
			createDialog(context);
		}
		adapter.notifyDataSetChanged();
	}

//	public void notifyDataChange(final Map<String, BluetoothDevice> map) {
//		for (String key : map.keySet().toArray(new String[] {})) {
//			String name = map.get(key).getName();
//			String mac = map.get(key).getAddress();
//			BluetoothDevice device = map.get(key);
//			BluetoothStruct bluetoothStruct = new BluetoothStruct(name, mac, device);
//			if (name != null && mac != null && device != null) {
//				int index = findBluetoothDevice(mac, items);
//				if (index < 0) {
//					items.add(bluetoothStruct);
//				} else {
//					items.get(index).setDevice(device);
//				}
//			}
//		}
//		adapter.notifyDataSetChanged();
//	}


//	private int findBluetoothDevice(String mac, ArrayList<BluetoothStruct> item) {
//		for (int i = 0; i < item.size(); i++) {
//			if (item.get(i).getMac().equals(mac))
//				return i;
//		}
//		return -1;
//	}

//	private class BluetoothStruct 
//	{
//		private String name = "";
//		private String mac = "";
//		private BluetoothDevice device = null;
//
//		public BluetoothStruct(String name, String mac, BluetoothDevice device) 
//		{
//			super();
//			this.name = name;
//			this.mac = mac;
//			this.device = device;
//		}
//
//		@Override
//		public String toString()
//		{
//			return name;
//		}
//		
//
//		public String getName() {
//			return name;
//		}
//
//		public BluetoothDevice getDevice() 
//		{
//			return device;
//		}
//
//		public void setDevice(BluetoothDevice device) 
//		{
//			this.device = device;
//		}
//
//		public String getMac()
//		{
//			return mac;
//		}
//
//	}
}
