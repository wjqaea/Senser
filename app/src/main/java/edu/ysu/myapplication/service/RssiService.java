package edu.ysu.myapplication.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
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
    private Handler mHandler = new Handler();
    private static final long SCAN_PERIOD = 10000;
    private BufferedWriter bufferedWriter;
    Map<String, Integer> deviceNumMap, deviceAvgMap;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
//        Log.d("TAG", "onBind: in");
//
//        //requestPermission();
//
//        String dateString = intent.getStringExtra("dateString");
//        String pathString = getExternalFilesDir(null).getAbsolutePath()+"/"+dateString+"/";
//        File path = new File(pathString);
//        boolean mkd = false;
//        if (!path.exists() || !path.isDirectory()) mkd = path.mkdirs();
//        if (!mkd) Toast.makeText(this, "文件夹创建失败", Toast.LENGTH_SHORT).show();
//        File file = new File(pathString,"RSSIData.csv");
//        try {
//            if(!file.exists()){
//                boolean isCreate =  file.createNewFile();
//                if (isCreate) {
//                    Toast.makeText(this, "RSSI数据创建成功", Toast.LENGTH_SHORT).show();
//                }
//            }
//            bufferedWriter = new BufferedWriter(new FileWriter(file , true));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        scanLeDevice(true);
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

    private final BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                    if (!deviceNumMap.containsKey(device.getAddress())) {
                        Log.e("TAG", "onLeScan: "+ device.getAddress()+"  is  "+ device.getName());
                        deviceNumMap.put(device.getAddress(), 1);
                        deviceAvgMap.put(device.getAddress(), rssi);
                    } else {
                        int tmp = deviceNumMap.get(device.getAddress());
                        int avg = deviceAvgMap.get(device.getAddress());
                        avg = (tmp * avg + rssi) / (tmp + 1);
                        deviceAvgMap.put(device.getAddress(), avg);
                        deviceNumMap.put(device.getAddress(), tmp + 1);
                    }

//                    writeInCsv(device, rssi);
                }
            };

//    private final ScanCallback scanCallback = new ScanCallback() {
//        @Override
//        public void onScanResult(int callbackType, ScanResult result) {
////            if (deviceNumMap.keySet().contains(result.getDevice().getAddress())) {
////                writeInCsv(result.getDevice(), result.getRssi());
////                deviceSet.add(result.getDevice().getAddress());
////            }
//            writeInCsv(result.getDevice(), result.getRssi());
//            super.onScanResult(callbackType, result);
//        }
//    };


    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(() -> {
//                mBluetoothAdapter.getBluetoothLeScanner().stopScan(scanCallback);
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
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            }, SCAN_PERIOD * 3);
            mBluetoothAdapter.startLeScan(mLeScanCallback);
//            mBluetoothAdapter.getBluetoothLeScanner().startScan(scanCallback);
        } else {
            Log.e("TAG", "scanLeDevice: STOP YET");
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
//            mBluetoothAdapter.getBluetoothLeScanner().stopScan(scanCallback);
        }
    }


    void writeInCsv(BluetoothDevice device, int rssi) {
        try {
            bufferedWriter.write("" + device.getAddress());
            bufferedWriter.write(",");
            bufferedWriter.write("" + device.getName());
            bufferedWriter.write(",");
            bufferedWriter.write("" + rssi);
            bufferedWriter.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
