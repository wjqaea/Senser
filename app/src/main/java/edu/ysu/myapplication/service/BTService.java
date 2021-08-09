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

public class BTService extends Service {

    private BluetoothAdapter mBluetoothAdapter;
    private final Handler mHandler = new Handler(Looper.myLooper());
    private static final long SCAN_PERIOD = 12000;
    private BufferedWriter bufferedWriter;
    Map<String, Integer> deviceNumMap, deviceAvgMap;
    Map<String, String> deviceNameMap;
    long baseTime = 0;

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
        IntentFilter filter= new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);

        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String dateString = intent.getStringExtra("dateString");
        String pathString = getExternalFilesDir(null).getAbsolutePath() + "/" + dateString + "/";
        File path = new File(pathString);
        boolean mkd = false;
        if (!path.exists() || !path.isDirectory()) mkd = path.mkdirs();
        if (!mkd) Toast.makeText(this, "文件夹创建失败", Toast.LENGTH_SHORT).show();
        File file = new File(pathString, "BTData.csv");
        try {
            if (!file.exists()) {
                boolean isCreate = file.createNewFile();
                if (isCreate) {
                    Toast.makeText(this, "BT数据创建成功", Toast.LENGTH_SHORT).show();
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

        scanDevice(true);
//        try {
//            looper();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        try {
            for (Map.Entry<String, Integer> entry : deviceAvgMap.entrySet()) {
                bufferedWriter.write("" + entry.getKey());
                bufferedWriter.write(",");
                bufferedWriter.write("" + deviceNameMap.get(entry.getKey()));
                bufferedWriter.write(",");
                bufferedWriter.write("" + deviceNumMap.get(entry.getKey()));
                bufferedWriter.write(",");
                bufferedWriter.write("" + entry.getValue());
                bufferedWriter.newLine();
            }

            bufferedWriter.flush();

            scanDevice(false);
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }


    private void scanDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(() -> {
                Log.e("TAG", "scanLeDevice: STOP!!!");
                Toast.makeText(this, "采集完成", Toast.LENGTH_SHORT).show();
//                try {
//                    for (Map.Entry<String, Integer> entry : deviceAvgMap.entrySet()) {
//                        bufferedWriter.write("" + entry.getKey());
//                        bufferedWriter.write(",");
//                        bufferedWriter.write("" + deviceNumMap.get(entry.getKey()));
//                        bufferedWriter.write(",");
//                        bufferedWriter.write("" + entry.getValue());
//                        bufferedWriter.newLine();
//                    }
//
//                    bufferedWriter.flush();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
                mBluetoothAdapter.cancelDiscovery();
            }, SCAN_PERIOD);
            mBluetoothAdapter.startDiscovery();
        } else {
            Log.e("TAG", "scanLeDevice: STOP YET");
            mBluetoothAdapter.cancelDiscovery();
        }
    }

    void looper() throws InterruptedException {

        Thread thread = new Thread(() -> {
            scanDevice(true);
            Log.e("TAG", "run: " + (System.currentTimeMillis() - baseTime) / 1000);
        });

        for (int i = 0; i < 3; i++){
            thread.start();
            Thread.sleep(12000);
        }
        Log.e("TAG", "looper: " + (System.currentTimeMillis() - baseTime) / 1000);
    }

    //广播接收器
    class SingBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action) ){
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int rssi = intent.getExtras().getShort(BluetoothDevice.EXTRA_RSSI);
                //下面的代码的效果是不让已经扫描到的设备重复显示

                if (!deviceNumMap.containsKey(device.getAddress())) {
                    Log.e("TAG", "onLeScan: "+ device.getAddress()+"  is  "+ device.getName() + "  "+ (System.currentTimeMillis() - baseTime)/1000);
                    deviceNumMap.put(device.getAddress(), 1);
                    deviceAvgMap.put(device.getAddress(), rssi);
                    deviceNameMap.put(device.getAddress(), device.getName());
                } else {
                    Object tmpO = deviceNumMap.get(device.getAddress());
                    Object avgO = deviceAvgMap.get(device.getAddress());
                    int tmp = 0;
                    int avg = 0;
                    if (tmpO != null && avgO != null) {
                        tmp = (int) tmpO;
                        avg = (int) avgO;
                    }
                    avg = (tmp * avg + rssi) / (tmp + 1);
                    deviceAvgMap.put(device.getAddress(), avg);
                    deviceNumMap.put(device.getAddress(), tmp + 1);
                }
            }
        }
    }
}
