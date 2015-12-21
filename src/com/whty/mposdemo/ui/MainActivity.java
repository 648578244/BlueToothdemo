
package com.whty.mposdemo.ui;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.whty.bluetooth.manage.util.BlueToothUtil;
import com.whty.bluetoothsdk.util.Utils;
import com.whty.comm.inter.ICommunication;
import com.whty.mposdemo.R;
import com.whty.mposdemo.utilClass.DeviceDelegate;
import com.whty.mposdemo.utils.BlueToothDeviceReceiver;
import com.whty.mposdemo.utils.DeviceDialogUtil;
import com.whty.mposdemo.utils.SharedMSG;
import com.whty.tymposapi.DeviceApi;

public class MainActivity extends Activity {

    private Button initDevice, connDevice, disconnDevice, isConnected, updateWorkingKey, getMac,
            readCard, getPinBlock, getSubApplicationInfo;
    private Button getSN, getCSN, getVersion, confirmTransaction, cancel, ICTradeResponse;
    private TextView showstatus, showResult;

    private DeviceApi deviceApi;
    private DeviceDelegate delegate;
    private Handler mHandler;

    private DialogHandler dialogHandler;
    private BroadcastReceiver receiver = null;
    private DeviceDialogUtil devicedialog = null;
    private BluetoothDevice currentDevice;
    private BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
    private String mDeviceAddress;
    private String mDeviceName;

    private boolean isInited = false;
    private boolean deviceConnected = false;
    private String tag = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        initDevice = (Button) findViewById(R.id.initDevice);
        connDevice = (Button) findViewById(R.id.connDevice);
        disconnDevice = (Button) findViewById(R.id.disconnDevice);
        isConnected = (Button) findViewById(R.id.isConnected);
        updateWorkingKey = (Button) findViewById(R.id.updateWorkingKey);
        getMac = (Button) findViewById(R.id.getMac);
        getSN = (Button) findViewById(R.id.getSN);
        getCSN = (Button) findViewById(R.id.getCSN);
        getVersion = (Button) findViewById(R.id.getVersion);
        cancel = (Button) findViewById(R.id.cancel);
        readCard = (Button) findViewById(R.id.readCard);
        getPinBlock = (Button) findViewById(R.id.getPinBlock);
        getSubApplicationInfo = (Button) findViewById(R.id.getSubApplicationInfo);
        confirmTransaction = (Button) findViewById(R.id.confirmTransaction);
        ICTradeResponse = (Button) findViewById(R.id.ICTradeResponse);
        showstatus = (TextView) findViewById(R.id.showStatus);
        showResult = (TextView) findViewById(R.id.showResult);

        this.setTitle(this.getTitle());
        showResult.setText("结果显示区域");

        initUI();

        mHandler = new MyHandler();
        delegate = new DeviceDelegate(mHandler);
        deviceApi = new DeviceApi(MainActivity.this);
        deviceApi.setDelegate(delegate);

        // 保持屏幕唤醒状态
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // handler用于跟UI的交互
        dialogHandler = new DialogHandler();
        devicedialog = new DeviceDialogUtil(dialogHandler);

        // 广播接收者接收监听蓝牙状态，然后将需要的信息由Hanlder放到队列以便更新UI使用
        receiver = new BlueToothDeviceReceiver(dialogHandler);
        IntentFilter intent = new IntentFilter();
        intent.addAction(BluetoothDevice.ACTION_FOUND);
        intent.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        intent.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        intent.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intent.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        intent.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        intent.addAction(BluetoothDevice.ACTION_NAME_CHANGED);
        intent.setPriority(-1000);
        getApplicationContext().registerReceiver(receiver, intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (deviceConnected)
        {
            new Thread()
            {
                public void run()
                {
                    deviceApi.disconnectDevice();
                }
            }.start();
        }

        Log.d(tag, "MainActivity is destroied.");
    }

    @Override
    public void finish() {
        super.finish();
        if (deviceConnected)
        {
            new Thread()
            {
                public void run()
                {
                    deviceApi.disconnectDevice();
                }
            }.start();
        }
        Log.e(tag, "finish() function is involked");
    }

    public void initUI()
    {
        // 监听各个按钮点击事件
        initDevice.setOnClickListener(new MyOnclickListener());
        connDevice.setOnClickListener(new MyOnclickListener());
        disconnDevice.setOnClickListener(new MyOnclickListener());
        isConnected.setOnClickListener(new MyOnclickListener());
        updateWorkingKey.setOnClickListener(new MyOnclickListener());
        getMac.setOnClickListener(new MyOnclickListener());
        getSN.setOnClickListener(new MyOnclickListener());
        getCSN.setOnClickListener(new MyOnclickListener());
        cancel.setOnClickListener(new MyOnclickListener());
        getVersion.setOnClickListener(new MyOnclickListener());
        getSubApplicationInfo.setOnClickListener(new MyOnclickListener());
        readCard.setOnClickListener(new MyOnclickListener());
        getPinBlock.setOnClickListener(new MyOnclickListener());
        confirmTransaction.setOnClickListener(new MyOnclickListener());
        ICTradeResponse.setOnClickListener(new MyOnclickListener());
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("提示")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setMessage("确认退出工具？")
                    .setPositiveButton("确认",
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    finish();
                                    System.exit(0);
                                }
                            }).setNegativeButton("取消", null).create().show();

            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    // handler获取队列中的信息,更新UI
    @SuppressLint("HandlerLeak")
    class DialogHandler extends Handler {

        @Override
        public void dispatchMessage(Message msg) {

            super.dispatchMessage(msg);

            switch (msg.what) {
                // 收到系统发现设备的广播，传给handler处理，弹出dialog
                case SharedMSG.No_Device_Selected:

                    Toast.makeText(MainActivity.this, "未选择设备", Toast.LENGTH_SHORT).show();
                    break;

                // 搜索到蓝牙设备
                case SharedMSG.Device_Found:

                    if (mDeviceAddress == null || mDeviceAddress.length() <= 0)
                    {
                        devicedialog.listDevice(MainActivity.this);
                    }
                    break;

                case SharedMSG.No_Device:

                    Toast.makeText(MainActivity.this, "当前无设备连接，请重新扫描设备连接", Toast.LENGTH_SHORT)
                            .show();
                    break;

                // 选中蓝牙设备
                case SharedMSG.Device_Ensured:

                    currentDevice = (BluetoothDevice) msg.obj;
                    mDeviceName = currentDevice.getName();
                    mDeviceAddress = currentDevice.getAddress();
                    showResult.setText("已选择设备" + mDeviceName);
                    break;

                // 蓝牙断开连接
                case SharedMSG.Device_Disconnected:

                    deviceConnected = false;
                    showResult.setText("设备断开连接");
                    break;
            }
        }
    }

    @SuppressLint("HandlerLeak")
    class MyHandler extends Handler {

        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
            String show_msg = "";
            switch (msg.what) {

                case SharedMSG.SHOW_MSG:

                    show_msg = (String) msg.obj;
                    showResult.setText(show_msg);
                    if (show_msg.equals("连接设备成功！"))
                        deviceConnected = true;

                    break;

                case SharedMSG.SHOW_STATUS:

                    show_msg = (String) msg.obj;
                    showstatus.setText(show_msg);
                    break;

                default:
                    break;
            }
        }

    }

    class MyOnclickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            showstatus.setText(((Button) v).getText() + "按钮被点击");
            showResult.setText("");
            switch (v.getId()) {
                // 扫描设备（初始化设备）
                case R.id.initDevice:
                    if (!isInited) {
                        if (deviceApi.initDevice(ICommunication.BLUETOOTH_DEVICE)) {
                            showResult.setText("初始化成功");
                            isInited = true;
                        } else {
                            showResult.setText("初始化失败");
                        }
                    }
                    if (!deviceConnected)
                    {
                        mDeviceAddress = null;
                        btAdapter.cancelDiscovery();
                        btAdapter.startDiscovery();
                        if (BlueToothUtil.mDialog != null)
                        {
                            BlueToothUtil.mDialog = null;
                        }
                        // BlueToothUtil.items.clear();
                        BlueToothDeviceReceiver.items.clear();
                    }
                    else
                    {
                        Toast.makeText(MainActivity.this, "当前有设备连接，请先断开连接再扫描",
                                Toast.LENGTH_SHORT).show();
                    }
                    break;

                // 连接设备
                case R.id.connDevice:
                    if (showResult.getText().toString().equals("正在连接中...请等候！"))
                    {
                        Toast.makeText(MainActivity.this, "正在连接中，无需重复连接!", Toast.LENGTH_SHORT)
                                .show();
                    }
                    else
                    {
                        if (deviceConnected)
                        {
                            Toast.makeText(MainActivity.this, "当前已有设备连接:" + mDeviceName,
                                    Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            showResult.setText("正在连接中...请等候！");
                            btAdapter.cancelDiscovery();
                            if (mDeviceAddress != null && mDeviceAddress.length() > 0)
                            {
                                new Thread() {
                                    public void run() {
                                        Looper.prepare();
                                        deviceApi.connectDevice(mDeviceAddress);
                                    }
                                }.start();
                            }
                            else
                            {
                                showResult.setText("请先扫描选择设备后再连接");
                            }
                        }
                    }
                    break;

                // 断开连接
                case R.id.disconnDevice:
                    if (deviceConnected)
                    {
                        new Thread()
                        {
                            public void run()
                            {
                                deviceApi.disconnectDevice();
                            }
                        }.start();
                    }
                    else
                    {
                        Toast.makeText(MainActivity.this, "当前无连接，无需断开", Toast.LENGTH_SHORT).show();
                    }
                    break;

                case R.id.isConnected:
                    if (deviceApi.isConnected()) {
                        showResult.setText("已连接设备:" + mDeviceName);
                    } else {
                        showResult.setText("未连接设备");
                    }
                    break;

                // 更新工作秘钥
                case R.id.updateWorkingKey:

                    new Thread()
                    {
                        public void run()
                        {
                            String tdk = "FA727B2F08101273A17712674D8CF21CAB3F69CE";
                            String pik = "4F0C5B17E48E20D69A2A284394F729DF46E3EF6D";
                            String mak = "4baa0a2ce07ba7f63ce73298";
                            deviceApi.updateWorkingKey(tdk, pik, mak);
                        }
                    }.start();
                    break;

                // 摘要加密
                case R.id.getMac:

                    new Thread()
                    {
                        public void run()
                        {
                            deviceApi.getMacWithMKIndex(0,
//                                    new byte[] {
//                                    0x19, (byte) 0x90, 0x05, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00,
//                                    0x00, 0x00, 0x01, 0x04, 0x27, 0x01, 0x12, 0x09, 0x32, 0x30,
//                                    0x31, 0x30, 0x31, 0x30, 0x30, 0x30, 0x30, 0x31, 0x31, 0x31}
                                    Utils.hexString2Bytes("0200702404c030c098111962122632020070732980000000000000000001000002230902100012376212263202007073298d23092205739991617f0094996212263202007073298d1561560000000000001003573999010000023090d000000000000d00000000d00000000f31313034303332363830363030313030303031303534383135367d109d8d4ad0a72a2600000000000000001322000006000000")
                            );
                        }
                    }.start();
                    break;

                case R.id.confirmTransaction:
                    if (deviceConnected) {
                        new Thread() {
                            public void run() {
                                deviceApi.confirmTransaction();
                            }
                        }.start();
                    } else {
                        Toast.makeText(MainActivity.this, "请先连接设备！", Toast.LENGTH_SHORT).show();
                    }
                    break;

                case R.id.cancel:
                    if (deviceConnected) {
                        new Thread() {
                            public void run() {
                                deviceApi.cancel();
                            }
                        }.start();
                    } else {
                        Toast.makeText(MainActivity.this, "请先连接设备！", Toast.LENGTH_SHORT).show();
                    }
                    break;

                case R.id.getSubApplicationInfo:
                    if (deviceConnected) {
                        showResult.setText("子应用信息:" + deviceApi.getDeviceSubApplicationParams());
                    } else {
                        Toast.makeText(MainActivity.this, "请先连接设备！", Toast.LENGTH_SHORT).show();
                    }
                    break;

                // 复合刷卡
                case R.id.readCard:

                    new Thread()
                    {
                        @SuppressLint("SimpleDateFormat")
                        public void run()
                        {
                            SimpleDateFormat format = new SimpleDateFormat("yyMMddHHmmss");
                            String terminalTime = format.format(new Date());
                            Log.e(tag, "terminalTime:" + terminalTime);
                            // 例如2014-12-03 16:20:55 则terminalTime传入"141203162055";
                            // 传入金额的时候注意不要传小数点，如果想要传1.50则写入"150";
                            // 传入交易类型 (byte)0x00代表消费，(byte)0x31代表查询余额
                            deviceApi.readCard("150", terminalTime, (byte) 0x00, (byte) 0x64);
                        }
                    }.start();
                    break;

                // 蓝牙刷卡头获取pinBlock
                case R.id.getPinBlock:
                    if (deviceConnected) {
                        showResult.setText("测试pin:123456\n得到密文:" +
                                deviceApi.getEncPinblock("123456"));
                    } else {
                        Toast.makeText(MainActivity.this, "请先连接设备！", Toast.LENGTH_SHORT).show();
                    }
                    break;

                case R.id.ICTradeResponse:

                    new Thread()
                    {
                        public void run()
                        {
                            String data = "3030910ae4280663aea43a313030";
                            deviceApi.confirmTradeResponse(data);
                        }
                    }.start();
                    break;

                case R.id.getVersion:
                    showResult.setText("当前API版本：" + deviceApi.getVersion());
                    break;

                case R.id.getSN:
                    if (deviceConnected) {
                        showResult.setText("设备SN号:" + deviceApi.getDeviceSN());
                    } else {
                        Toast.makeText(MainActivity.this, "请先连接设备！", Toast.LENGTH_SHORT).show();
                    }
                    break;

                case R.id.getCSN:
                    if (deviceConnected) {
                        showResult.setText("PSAM卡号:" + deviceApi.getDeviceCSN());
                    } else {
                        Toast.makeText(MainActivity.this, "请先连接设备！", Toast.LENGTH_SHORT).show();
                    }
                    break;

                default:
                    break;
            }
        }

    }

}
