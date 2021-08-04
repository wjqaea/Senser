package edu.ysu.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

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

    int btFlag = 0;
    long baseTime = 1624408500;

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
        intentAccelerometerService = new Intent(MainActivity.this , AccelerometerService.class);
        intentGyroscopeService = new Intent(MainActivity.this , GyroscopeService.class);
        intentMagneticService = new Intent(MainActivity.this , MagneticService.class);
        intentRssiService = new Intent(MainActivity.this , RssiService.class);
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