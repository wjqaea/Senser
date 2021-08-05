package edu.ysu.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;

import java.util.ArrayList;
import java.util.List;

import edu.ysu.myapplication.service.AccelerometerService;
import edu.ysu.myapplication.service.GyroscopeService;
import edu.ysu.myapplication.service.MagneticService;
import edu.ysu.myapplication.service.RssiService;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    Button buttonStart,buttonStop,buttonRssi;

    Intent intentAccelerometerService;
    Intent intentGyroscopeService;
    Intent intentMagneticService;
    Intent intentRssiService;

    private static final int REQUEST_CODE = 0; // 请求码

    // 所需的全部权限
    static final String[] PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };




    int btFlag = 0;
    long baseTime = 1628042000;


    ServiceConnection conn = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // 当service绑定成功时，会调用次方法，可以在此申请权限
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},1);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String str : PERMISSIONS) {
                if (this.checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) { //申请权限
                    this.requestPermissions(PERMISSIONS, 1);
                }
            }
        }


            init();
    }

    private void init(){
        buttonStart = findViewById(R.id.button_start);
        buttonStop = findViewById(R.id.button_stop);
        buttonRssi = findViewById(R.id.button_rssi);
        buttonStart.setOnClickListener(this);
        buttonStop.setOnClickListener(this);
        buttonRssi.setOnClickListener(this);
//        intentAccelerometerService = new Intent(MainActivity.this , AccelerometerService.class);
//        intentGyroscopeService = new Intent(MainActivity.this , GyroscopeService.class);
//        intentMagneticService = new Intent(MainActivity.this , MagneticService.class);
//        intentRssiService = new Intent(MainActivity.this , RssiService.class);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button_start:
                startServices();
                break;
            case R.id.button_stop:
                stopServices();
                break;
            case R.id.button_rssi:
                getRssi();
                break;
            default:
                break;
        }
    }

    private void startServices(){
        btFlag = 1;
        String dateString = String.valueOf(System.currentTimeMillis()/1000 - baseTime);
        intentAccelerometerService = new Intent(MainActivity.this , AccelerometerService.class);
        intentGyroscopeService = new Intent(MainActivity.this , GyroscopeService.class);
        intentMagneticService = new Intent(MainActivity.this , MagneticService.class);
        intentAccelerometerService.putExtra("dateString" , dateString);
        intentGyroscopeService.putExtra("dateString" , dateString);
        intentMagneticService.putExtra("dateString" , dateString);
        startService(intentAccelerometerService);
        startService(intentGyroscopeService);
        startService(intentMagneticService);
    }

    private void getRssi(){
        btFlag = 2;
        String dateString = String.valueOf(System.currentTimeMillis()/1000 - baseTime);
        intentRssiService = new Intent(MainActivity.this , RssiService.class);
        intentRssiService.putExtra("dateString" , dateString);
        //bindService(intentRssiService,conn, Context.BIND_AUTO_CREATE);
        startService(intentRssiService);
    }

    private void stopServices(){
        if (btFlag == 1) {
            stopService(intentAccelerometerService);
            stopService(intentGyroscopeService);
            stopService(intentMagneticService);
        }else if (btFlag == 2){
            stopService(intentRssiService);
        }
        btFlag = 0;
    }




}