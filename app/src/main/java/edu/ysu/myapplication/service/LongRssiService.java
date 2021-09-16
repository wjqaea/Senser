package edu.ysu.myapplication.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LongRssiService extends Service {

    private BluetoothAdapter mBluetoothAdapter;
    private final Handler mHandler = new Handler(Looper.myLooper());
    private final Handler handler = new Handler(Looper.myLooper());
    private static final long SCAN_PERIOD = 12000;
    private BufferedWriter bufferedWriter;
    Map<String, Integer> deviceNumMap, deviceAvgMap;
    Map<String, String> deviceNameMap;
    long baseTime = 0;
    int  times;
    Runnable runnable;

    private SingBroadcastReceiver mReceiver;//这个我自定义的一个广播接收器


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {

        deviceNumMap = new HashMap<>();
        deviceAvgMap = new HashMap<>();
        deviceNameMap = new HashMap<>();

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "不支持蓝牙", Toast.LENGTH_SHORT).show();
            stopSelf();
        }

        //注册广播
        mReceiver = new SingBroadcastReceiver();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);

        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String dateString = intent.getStringExtra("dateString");
        //String pathString = getExternalFilesDir(null).getAbsolutePath() + "/" + dateString + "/";
        String pathString = getExternalFilesDir(null).getAbsolutePath() + "/LongRssiData/";
        File path = new File(pathString);
        boolean mkd = false;
        if (!path.exists() || !path.isDirectory()) mkd = path.mkdirs();
        if (!mkd) Toast.makeText(this, "文件夹创建失败", Toast.LENGTH_SHORT).show();
        File file = new File(pathString, dateString + "LRData.csv");
        try {
            if (!file.exists()) {
                boolean isCreate = file.createNewFile();
                if (isCreate) {
                    Toast.makeText(this, "LongRssi数据创建成功", Toast.LENGTH_SHORT).show();
                    baseTime = System.currentTimeMillis();
                }
            }
            bufferedWriter = new BufferedWriter(new FileWriter(file, true));
        } catch (IOException e) {
            e.printStackTrace();
        }

        deviceAvgMap.clear();
        deviceNumMap.clear();
        deviceNameMap.clear();
        times = 0;

        deviceNumMap.put("",1);

        looper();

        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {

        scanDevice(false);
        try {
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Toast.makeText(this, "服务关闭", Toast.LENGTH_SHORT).show();
        times = 200;

        unregisterReceiver(mReceiver);
        super.onDestroy();
    }


    private void scanDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(() -> {
                Log.e("TAG", "scanLeDevice: STOP!!!");
                Toast.makeText(this, "采集完成", Toast.LENGTH_SHORT).show();
                //writeBtData();
                mBluetoothAdapter.cancelDiscovery();
            }, SCAN_PERIOD);
            mBluetoothAdapter.startDiscovery();
        } else {
            Log.e("TAG", "scanLeDevice: STOP YET");
            mBluetoothAdapter.cancelDiscovery();
        }
    }

    void looper() {

        runnable = new Runnable() {
            @Override
            public void run() {
                //这里写你想循环的代码
                scanDevice(true);
                times++;
                handler.postDelayed(this, 14000);//!!!绑定秒数
                if(times == 200) {
                    handler.removeCallbacks(this);//!!!停止循环器
                    stopSelf();
                }
            }
        };

        runnable.run();
    }

    //广播接收器
    class SingBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int rssi = intent.getExtras().getShort(BluetoothDevice.EXTRA_RSSI);

                if (deviceNumMap.containsKey(device.getAddress())) {
                    try {
                        bufferedWriter.write("" + device.getAddress());
                        bufferedWriter.write(",");
                        bufferedWriter.write("" + device.getName());
                        bufferedWriter.write(",");
                        bufferedWriter.write("" + rssi);
                        bufferedWriter.newLine();

                        bufferedWriter.flush();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        }
    }

}