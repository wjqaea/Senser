package edu.ysu.myapplication.service;

import android.Manifest;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import edu.ysu.myapplication.MainActivity;
import edu.ysu.myapplication.PermissionListener;


public class RssiService extends Service {

    private BluetoothAdapter mBluetoothAdapter;
//    private Handler mHandler = new Handler();
    private static final long SCAN_PERIOD = 10000;
    private BufferedWriter bufferedWriter;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d("TAG", "onBind: in");

        requestPermission();

        String dateString = intent.getStringExtra("dateString");
        String pathString = getExternalFilesDir(null).getAbsolutePath()+"/"+dateString+"/";
        File path = new File(pathString);
        boolean mkd = false;
        if (!path.exists() || !path.isDirectory()) mkd = path.mkdirs();
        if (!mkd) Toast.makeText(this, "文件夹创建失败", Toast.LENGTH_SHORT).show();
        File file = new File(pathString,"RSSIData.csv");
        try {
            if(!file.exists()){
                boolean isCreate =  file.createNewFile();
                if (isCreate) {
                    Toast.makeText(this, "RSSI数据创建成功", Toast.LENGTH_SHORT).show();
                }
            }
            bufferedWriter = new BufferedWriter(new FileWriter(file , true));
        } catch (IOException e) {
            e.printStackTrace();
        }

        scanLeDevice(true);
        return null;
    }




    @Override
    public void onCreate() {
        super.onCreate();

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "不支持蓝牙", Toast.LENGTH_SHORT).show();
            stopSelf();
        }
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        String dateString = intent.getStringExtra("dateString");
        String pathString = getExternalFilesDir(null).getAbsolutePath()+"/"+dateString+"/";
        File path = new File(pathString);
        boolean mkd = false;
        if (!path.exists() || !path.isDirectory()) mkd = path.mkdirs();
        if (!mkd) Toast.makeText(this, "文件夹创建失败", Toast.LENGTH_SHORT).show();
        File file = new File(pathString,"RSSIData.csv");
        try {
            if(!file.exists()){
                boolean isCreate =  file.createNewFile();
                if (isCreate) {
                    Toast.makeText(this, "RSSI数据创建成功", Toast.LENGTH_SHORT).show();
                }
            }
            bufferedWriter = new BufferedWriter(new FileWriter(file , true));
        } catch (IOException e) {
            e.printStackTrace();
        }

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

//    private BluetoothAdapter.LeScanCallback mLeScanCallback =
//            new BluetoothAdapter.LeScanCallback() {
//                @Override
//                public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
//                    try {
//                        bufferedWriter.write(""+device.getAddress());
//                        bufferedWriter.write(",");
//                        bufferedWriter.write(""+device.getName());
//                        bufferedWriter.write(",");
//                        bufferedWriter.write(""+rssi);
//                        bufferedWriter.newLine();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            };

    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            try {
                bufferedWriter.write(""+result.getDevice().getAddress());
                bufferedWriter.write(",");
                bufferedWriter.write(""+result.getDevice().getName());
                bufferedWriter.write(",");
                bufferedWriter.write(""+result.getRssi());
                bufferedWriter.newLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            super.onScanResult(callbackType, result);
        }
    };


    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
//            mHandler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    mBluetoothAdapter.getBluetoothLeScanner().stopScan(scanCallback);
//                    // mBluetoothAdapter.stopLeScan(mLeScanCallback);
//                }
//            }, SCAN_PERIOD);
            //mBluetoothAdapter.startLeScan(mLeScanCallback);
            mBluetoothAdapter.getBluetoothLeScanner().startScan(scanCallback);
        } else {
            //mBluetoothAdapter.stopLeScan(mLeScanCallback);
            mBluetoothAdapter.getBluetoothLeScanner().stopScan(scanCallback);
        }
    }


    private void requestPermission() {
        MainActivity.requestRunTimePermission(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}
                , new PermissionListener() {
                    @Override
                    public void onGranted() {

                    }

                    @Override
                    public void onGranted(List<String> grantedPermission) {

                    }

                    @Override
                    public void onDenied(List<String> deniedPermission) {

                    }
                });

    }
}
