
package com.whty.mposdemo.utilClass;

import java.util.HashMap;

import android.os.Handler;

import com.whty.bluetoothsdk.util.Utils;
import com.whty.mposdemo.utils.SharedMSG;
import com.whty.tymposapi.IDeviceDelegate;

public class DeviceDelegate implements IDeviceDelegate {

    private Handler handler;

    public DeviceDelegate(Handler handler) {
        super();
        this.handler = handler;
    }

    @Override
    public void onConnectedDevice(boolean isSuccess) {
        if (isSuccess) {
            handler.obtainMessage(SharedMSG.SHOW_MSG, "连接设备成功！").sendToTarget();
        } else {
            handler.obtainMessage(SharedMSG.SHOW_MSG, "连接设备失败！").sendToTarget();
        }
    }

    @Override
    public void onDisconnectedDevice(boolean isSuccess) {
        if (isSuccess) {
            handler.obtainMessage(SharedMSG.SHOW_MSG, "设备断开成功！").sendToTarget();
        } else {
            handler.obtainMessage(SharedMSG.SHOW_MSG, "设备断开失败！").sendToTarget();
        }
    }

    @Override
    public void onUpdateWorkingKey(boolean[] isSuccess) {
        if (isSuccess != null) {
            handler.obtainMessage(
                    SharedMSG.SHOW_MSG,
                    "更新磁道密钥：" + String.valueOf(isSuccess[0]) +
                            "\n更新PIN密钥：" + String.valueOf(isSuccess[1]) +
                            "\n更新MAC密钥：" + String.valueOf(isSuccess[2])).sendToTarget();
        } else {
            handler.obtainMessage(SharedMSG.SHOW_MSG, "终端加密失败！").sendToTarget();
        }
    }

    @Override
    public void onGetMacWithMKIndex(byte[] data) {
        if (data != null) {
            handler.obtainMessage(SharedMSG.SHOW_MSG,
                    Utils.bytesToHexString(data, data.length))
                    .sendToTarget();
        } else {
            handler.obtainMessage(SharedMSG.SHOW_MSG, "终端加密失败！").sendToTarget();
        }
    }

    @Override
    public void onReadCard(HashMap data) {
        if (data != null) {
            handler.obtainMessage(SharedMSG.SHOW_MSG, data.toString()).sendToTarget();
        } else {
            handler.obtainMessage(SharedMSG.SHOW_MSG, "刷卡指令执行失败！").sendToTarget();
        }
    }

}
