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
        implements ResetDialogFragment.OnResetSelectedListener {

    ImageView image;
    String image_path;

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
                FragmentManager manager = getSupportFragmentManager();
                ResetDialogFragment dialog = new ResetDialogFragment();
                dialog.show(manager, "resetDialog");
                //backToMain();
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
        Intent intent = getIntent();
        image_path = intent.getStringExtra("image_path");

        image = findViewById(R.id.takenImage);

        Bitmap bitmap = BitmapFactory.decodeFile(image_path);
        image.setImageBitmap(bitmap);


    }

    private void backToMain() {
        super.onBackPressed();
    }


    public void onConfirmClick(View view) throws IOException {
        Log.d("START","DETECT STARTED");
        VisionSearch.detectLabels(image_path);
        Log.d("FINISH","DETECT FINISHED");

    }

    public void onRetakeClick(View view) {
        File file = new File(image_path);
        boolean deleted = file.delete();
        backToMain();
    }

    @Override
    public void onResetSelectedClick(Boolean reset) {
        if (reset)
            backToMain();
    }
}