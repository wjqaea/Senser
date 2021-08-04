package edu.ysu.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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


    private static PermissionListener mlistener;

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
        bindService(intentRssiService,conn, Context.BIND_AUTO_CREATE);
        //startService(intentRssiService);
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




    /**
     * 权限申请
     * @param permissions 待申请的权限集合
     * @param listener  申请结果监听事件
     */
    public static void requestRunTimePermission(String[] permissions, PermissionListener listener) {
        mlistener = listener;

        Activity topActivity = ActivityCollector.getTopActivity();
        if (topActivity == null) {
            return;
        }

        //用于存放为授权的权限
        List<String> permissionList = new ArrayList<>();
        //遍历传递过来的权限集合
        for (String permission : permissions) {
            //判断是否已经授权
            if (ContextCompat.checkSelfPermission(topActivity, permission) != PackageManager.PERMISSION_GRANTED) {
                //未授权，则加入待授权的权限集合中
                permissionList.add(permission);
            }
        }

        //判断集合
        if (!permissionList.isEmpty()) {  //如果集合不为空，则需要去授权
            ActivityCompat.requestPermissions(topActivity, permissionList.toArray(new String[permissionList.size()]), 1);
        } else {  //为空，则已经全部授权
            listener.onGranted();
        }
    }


    /**
     * 权限申请结果
     * @param requestCode  请求码
     * @param permissions  所有的权限集合
     * @param grantResults 授权结果集合
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0){
                    //被用户拒绝的权限集合
                    List<String> deniedPermissions = new ArrayList<>();
                    //用户通过的权限集合
                    List<String> grantedPermissions = new ArrayList<>();
                    for (int i = 0; i < grantResults.length; i++) {
                        //获取授权结果，这是一个int类型的值
                        int grantResult = grantResults[i];

                        if (grantResult != PackageManager.PERMISSION_GRANTED){ //用户拒绝授权的权限
                            String permission = permissions[i];
                            deniedPermissions.add(permission);
                        }else{  //用户同意的权限
                            String permission = permissions[i];
                            grantedPermissions.add(permission);
                        }
                    }

                    if (deniedPermissions.isEmpty()){  //用户拒绝权限为空
                        mlistener.onGranted();
                    }else {  //不为空
                        //回调授权成功的接口
                        mlistener.onDenied(deniedPermissions);
                        //回调授权失败的接口
                        mlistener.onGranted(grantedPermissions);
                    }
                }
                break;
            default:
                break;
        }
    }


}