package edu.ysu.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import edu.ysu.myapplication.service.AccelerometerService;
import edu.ysu.myapplication.service.GyroscopeService;
import edu.ysu.myapplication.service.MagneticService;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    Button buttonStart,buttonStop;

    Intent intentAccelerometerService;
    Intent intentGyroscopeService;
    Intent intentMagneticService;

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
        buttonStart.setOnClickListener(this);
        buttonStop.setOnClickListener(this);
        intentAccelerometerService = new Intent(MainActivity.this , AccelerometerService.class);
        intentGyroscopeService = new Intent(MainActivity.this , GyroscopeService.class);
        intentMagneticService = new Intent(MainActivity.this , MagneticService.class);
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
            default:
                break;
        }
    }

    private void startServices(){
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

    private void stopServices(){
        stopService(intentAccelerometerService);
        stopService(intentGyroscopeService);
        stopService(intentMagneticService);
    }
}