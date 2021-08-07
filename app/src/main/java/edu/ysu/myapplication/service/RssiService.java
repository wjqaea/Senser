package edu.ysu.myapplication.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
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


public class RssiService extends Service {

    private BluetoothAdapter mBluetoothAdapter;
    private final Handler mHandler = new Handler(Looper.myLooper());
    private static final long SCAN_PERIOD = 10000;
    private BufferedWriter bufferedWriter;
    Map<String, Integer> deviceNumMap, deviceAvgMap;
    
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        deviceNumMap = new HashMap<>();
        deviceAvgMap = new HashMap<>();
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "不支持蓝牙", Toast.LENGTH_SHORT).show();
            stopSelf();
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String dateString = intent.getStringExtra("dateString");
        String pathString = getExternalFilesDir(null).getAbsolutePath() + "/" + dateString + "/";
        File path = new File(pathString);
        boolean mkd = false;
        if (!path.exists() || !path.isDirectory()) mkd = path.mkdirs();
        if (!mkd) Toast.makeText(this, "文件夹创建失败", Toast.LENGTH_SHORT).show();
        File file = new File(pathString, "RSSIData.csv");
        try {
            if (!file.exists()) {
                boolean isCreate = file.createNewFile();
                if (isCreate) {
                    Toast.makeText(this, "RSSI数据创建成功", Toast.LENGTH_SHORT).show();
                }
            }
            bufferedWriter = new BufferedWriter(new FileWriter(file, true));
        } catch (IOException e) {
            e.printStackTrace();
        }

        deviceAvgMap.clear();
        deviceNumMap.clear();

        scanLeDevice(true);

        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        try {
            scanLeDevice(false);
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }


    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            if (!deviceNumMap.containsKey(result.getDevice().getAddress())) {
                Log.e("TAG", "onLeScan: "+ result.getDevice().getAddress()+"  is  "+ result.getDevice().getName());
                deviceNumMap.put(result.getDevice().getAddress(), 1);
                deviceAvgMap.put(result.getDevice().getAddress(), result.getRssi());
            } else {
                Object tmpO = deviceNumMap.get(result.getDevice().getAddress());
                Object avgO = deviceAvgMap.get(result.getDevice().getAddress());
                int tmp = 0;
                int avg = 0;
                if (tmpO != null && avgO != null) {
                    tmp = (int) tmpO;
                    avg = (int) avgO;
                }
                avg = (tmp * avg + result.getRssi()) / (tmp + 1);
                deviceAvgMap.put(result.getDevice().getAddress(), avg);
                deviceNumMap.put(result.getDevice().getAddress(), tmp + 1);
            }
            super.onScanResult(callbackType, result);
        }
    };


    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(() -> {
                Log.e("TAG", "scanLeDevice: STOP!!!");
                Toast.makeText(this, "采集完成", Toast.LENGTH_SHORT).show();
                try {
                    for (Map.Entry<String, Integer> entry : deviceAvgMap.entrySet()) {
                        bufferedWriter.write("" + entry.getKey());
                        bufferedWriter.write(",");
                        bufferedWriter.write("" + deviceNumMap.get(entry.getKey()));
                        bufferedWriter.write(",");
                        bufferedWriter.write("" + entry.getValue());
                        bufferedWriter.newLine();
                    }

                    bufferedWriter.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mBluetoothAdapter.getBluetoothLeScanner().stopScan(scanCallback);
            }, SCAN_PERIOD * 3);
            mBluetoothAdapter.getBluetoothLeScanner().startScan(scanCallback);
        } else {
            Log.e("TAG", "scanLeDevice: STOP YET");
            mBluetoothAdapter.getBluetoothLeScanner().stopScan(scanCallback);
        }
    }


}
