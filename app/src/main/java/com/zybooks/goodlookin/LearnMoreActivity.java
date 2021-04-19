package com.zybooks.goodlookin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class LearnMoreActivity extends AppCompatActivity
        implements ResetDialogFragment.OnResetSelectedListener, SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;

    private float mAccel;
    private float mAccelCurrent;
    private float mAccelLast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn_more);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this, mAccelerometer);
    }

    @Override
    public void onResetSelectedClick(Boolean reset) {
        if (reset) super.onBackPressed();
    }

    public void askToReset() {
        FragmentManager manager = getSupportFragmentManager();
        ResetDialogFragment dialog = new ResetDialogFragment();
        dialog.show(manager, "resetDialog");
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        mAccelLast = mAccelCurrent;
        mAccelCurrent = (float) Math.sqrt((double) (x * x + y * y + z * z));
        float delta = mAccelCurrent - mAccelLast;
        mAccel = mAccel * 0.9f + delta;
        if (mAccel > 12) {
            Log.d("SHAKE", "IN SHAKE CALL");
            askToReset();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

}