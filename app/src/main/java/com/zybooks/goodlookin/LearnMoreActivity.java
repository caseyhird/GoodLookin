package com.zybooks.goodlookin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;

public class LearnMoreActivity extends AppCompatActivity
        implements ResetDialogFragment.OnResetSelectedListener {


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
                // Create Reset dialog
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

    private void backToMain() {
        Intent confirm = new Intent(this, MainActivity.class);
        startActivity(confirm);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn_more);
    }

    @Override
    public void onResetSelectedClick(Boolean reset) {
        if (reset)
            backToMain();
    }

    public void testReset(View view) {
        FragmentManager manager = getSupportFragmentManager();
        ResetDialogFragment dialog = new ResetDialogFragment();
        dialog.show(manager, "resetDialog");
    }
}