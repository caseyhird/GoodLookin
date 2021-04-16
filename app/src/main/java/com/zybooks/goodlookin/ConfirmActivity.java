package com.zybooks.goodlookin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.IOException;

public class ConfirmActivity extends AppCompatActivity {

    ImageView image;
    Bitmap btm;
    String image_path;

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

    public void onConfirmClick(View view) throws IOException {
        Log.d("START","DETECT STARTED");
        VisionSearch.detectLabels(image_path);
        Log.d("FINISH","DETECT FINISHED");

    }

    public void onRetakeClick(View view) {

    }
}