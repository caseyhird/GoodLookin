package com.zybooks.goodlookin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;

public class ConfirmActivity extends AppCompatActivity
        implements ResetDialogFragment.OnResetSelectedListener, SensorEventListener {

    public static final String EXTRA_IMAGE_PATH = "image_path";
    public static final int RESULT_CONFIRM = 1;
    public static final int RESULT_RETAKE = 0;
    ImageView image;
    String image_path;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;

    private float mAccel;
    private float mAccelCurrent;
    private float mAccelLast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm);

        // Get image path from main activity
        Intent intent = getIntent();
        image_path = intent.getStringExtra("image_path");
        image = findViewById(R.id.takenImage);
        // Decode image and set in image view
        Bitmap bitmap = BitmapFactory.decodeFile(image_path);
        image.setImageBitmap(bitmap);
        // Set up accelerometer sensor listener
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

    }

    /*
        Register event listener for accelerometer.
     */
    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    /*
        Unregister event listener for accelerometer.
     */
    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this, mAccelerometer);
    }

    /*
        Return image confirmed to main activity.
     */
    public void onConfirmClick(View view) throws IOException {
        Intent intent = new Intent();
        setResult(RESULT_CONFIRM, intent);
        finish();
    }

    /*
        Return retake image to main activity.
     */
    public void onRetakeClick(View view) {
        Intent intent = new Intent();
        setResult(RESULT_RETAKE, intent);
        finish();
    }

    /*
        Reset app to main when selected in dialog.
     */
    @Override
    public void onResetSelectedClick(Boolean reset) {
        if (reset) super.onBackPressed();
    }

    /*
        Listen for shake from accelerometer.
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        mAccelLast = mAccelCurrent;
        mAccelCurrent = (float) Math.sqrt((double) (x * x + y * y + z * z));
        float delta = mAccelCurrent - mAccelLast;
        mAccel = mAccel * 0.9f + delta;
        if (mAccel > 12) { askToReset(); }
    }

    /*
        Start dialog after shake event to allow user to confirm reset.
     */
    public void askToReset() {
        FragmentManager manager = getSupportFragmentManager();
        ResetDialogFragment dialog = new ResetDialogFragment();
        dialog.show(manager, "resetDialog");
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}