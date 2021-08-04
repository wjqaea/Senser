package edu.ysu.myapplication.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import edu.ysu.myapplication.MyBluetoothDevice;

public class RssiService extends Service {

    private BluetoothAdapter mBluetoothAdapter;
    private Handler mHandler;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final long SCAN_PERIOD = 10000;
    private BufferedWriter bufferedWriter;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new Handler();

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




    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                    try {
                        bufferedWriter.write(""+device.getAddress());
                        bufferedWriter.write(",");
                        bufferedWriter.write(""+device.getName());
                        bufferedWriter.write(",");
                        bufferedWriter.write(""+rssi);
                        bufferedWriter.newLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };



    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                }
            }, SCAN_PERIOD);
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
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
}
