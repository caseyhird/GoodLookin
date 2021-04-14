package com.zybooks.goodlookin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;

public class ConfirmActivity extends AppCompatActivity {

    ImageView image;
    Bitmap btm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm);

        image = findViewById(R.id.takenImage);
        Intent intent = getIntent();
        btm = (Bitmap) intent.getParcelableExtra("bitmap");
        image.setImageBitmap(btm);
    }
}