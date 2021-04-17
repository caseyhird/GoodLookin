package com.zybooks.goodlookin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.IOException;

public class ConfirmActivity extends AppCompatActivity {

    ImageView image;
    Bitmap btm;
    String image_path;

    private SensorManager mSensorManager;
    private float mAccel;
    private float mAccelCurrent;
    private float mAccelLast;

    private final SensorEventListener mSensorListener = new SensorEventListener() {
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
                backToMain();
                //Toast.makeText(getApplicationContext(), "Shake event detected", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm);

        // external storage version
        //Intent intent = getIntent();
        //image_path = intent.getStringExtra("image_path");
        image_path = "/Users/casey/Documents/Academic/Spring2021/CPSC4150/projects/final_project/project_code/app/src/main/res/drawable-v24/ic_launcher_foreground.xml";


        /*
        image = findViewById(R.id.takenImage);
        Intent intent = getIntent();
        btm = (Bitmap) intent.getParcelableExtra("bitmap");
        image.setImageBitmap(btm);
        */
    }

    private void backToMain() {
        Intent confirm = new Intent(this, MainActivity.class);
        startActivity(confirm);
    }


    public void onConfirmClick(View view) throws IOException {
        Log.d("START","DETECT STARTED");
        VisionSearch.detectLabels(image_path);
        Log.d("FINISH","DETECT FINISHED");

    }

    public void onRetakeClick(View view) {

    }
}