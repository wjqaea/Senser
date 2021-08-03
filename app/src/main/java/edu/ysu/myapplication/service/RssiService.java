package edu.ysu.myapplication.service;

import android.app.Service;
import android.content.Intent;
import android.hardware.SensorEventListener;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class RssiService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
