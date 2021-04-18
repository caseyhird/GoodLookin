package com.zybooks.goodlookin;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.extensions.HdrImageCaptureExtender;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    EditText searchText;
    String image_path;
    File image_file;

    // Permsission and androix camera from https://akhilbattula.medium.com/android-camerax-java-example-aeee884f9102
    // note requesting permisson for camera and external storage?
    private int REQUEST_CODE_PERMISSIONS = 1001;
    private final String[] REQUIRED_PERMISSIONS = new String[]
            {"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE"};
    private final int REQUEST_LOCATION_PERMISSIONS = 0;
    private Executor executor = Executors.newSingleThreadExecutor();
    private String locationFromMain = null;
    PreviewView mPreviewView;
    ImageView captureImage;
    ImageView learnMore;
    Camera camera;
    Preview preview;
    ProcessCameraProvider cameraProvider;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchText = findViewById(R.id.searchEditText);
        mPreviewView = findViewById(R.id.preview_area);
        // do something with image capture or send straight to next activity?
        captureImage = findViewById(R.id.captureImg);
        learnMore = findViewById(R.id.learn_more);

        //Toggle button to determine if the search will use last known location
        ToggleButton myToggle = findViewById(R.id.locToggleButton);
        myToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    System.out.println("TOGGLE BUTTON ON");
                    if(hasLocationPermission()){
                        findLocation();
                    }
                } else {
                    System.out.println("TOGGLE BUTTON OFF");
                    locationFromMain = null;
                }
            }
        });

        learnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLearnMore();
            }
        });

        if(allPermissionsGranted()){
            startCamera(); //start camera if permission has been granted by user
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

        if (allPermissionsGranted()) {
            startCamera();
        }

    }

    public void startLearnMore() {
        Intent confirm = new Intent(this, LearnMoreActivity.class);
        startActivity(confirm);
    }

    // FIXME: FOR TESTING CONFIRM ACTIVITY--DELETE LATER
    public void startConfirm(View view) {
        Intent confirm = new Intent(this, ConfirmActivity.class);
        startActivity(confirm);
    }
    public void startConfirm() {
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                cameraProvider.unbindAll();

            }
        });
        Log.d("CONFIRM", "IN START CONFIRM");

        Intent confirm = new Intent(this, ConfirmActivity.class);
        startActivity(confirm);
    }

    private boolean allPermissionsGranted(){

        for (String permission : REQUIRED_PERMISSIONS){
            // FIXME: can just pass 'this' for context
            if(ContextCompat.checkSelfPermission(getApplicationContext(), permission) != PackageManager.PERMISSION_GRANTED){
                Log.d("PERMISSION DONT HAVE", permission);
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

//        super.onRequestPermissionsResult(requestCode, permissions, grantResults); //FIXME zybooks wants this but doesn't make difference?

        if(requestCode == REQUEST_CODE_PERMISSIONS){
            if(allPermissionsGranted()){
                startCamera();
            } else{
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                this.finish();
            }
        }
        if(requestCode == REQUEST_LOCATION_PERMISSIONS){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                findLocation();
            }
        }
    }

    private void startCamera() {

        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    // made global
                    cameraProvider = cameraProviderFuture.get();
                    bindPreview(cameraProvider);
                } catch (ExecutionException | InterruptedException e) {
                    // No errors need to be handled for this Future.
                    // This should never be reached.
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }

    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {

        preview = new Preview.Builder().build();

        // Requiring lens facing back
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
        ImageCapture.Builder builder = new ImageCapture.Builder();

        //Vendor-Extensions (The CameraX extensions dependency in build.gradle)
        HdrImageCaptureExtender hdrImageCaptureExtender = HdrImageCaptureExtender.create(builder);

        // Query if extension is available (optional).
        if (hdrImageCaptureExtender.isExtensionAvailable(cameraSelector)) {
            // Enable the extension if available.
            hdrImageCaptureExtender.enableExtension(cameraSelector);
        }

        final ImageCapture imageCapture = builder
                .setTargetRotation(this.getWindowManager().getDefaultDisplay().getRotation())
                .build();

        // FIXME changed from createSurfaceProvider()
        preview.setSurfaceProvider(mPreviewView.getSurfaceProvider());
        // FIXME removing imageAnalysis before imageCapture
        camera = cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, preview, imageCapture); //FIXME add , imageCapture
        // was Camera

        captureImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("CLICK", "CLICKED");
/*
                // Get unique file name
                SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
                //File file = new File(getBatchDirectoryName(), mDateFormat.format(new Date())+ ".jpg");
                String path = mDateFormat.format(new Date());
                File file = null;
                try {
                    file = File.createTempFile(path, ".jpg", null);
                } catch (IOException e) {
                    finish();
                }
                */

                try {
                    image_file = createImageFile();
                    FileOutputStream fos;

                    fos = new FileOutputStream(image_file);
                    fos.write(R.drawable.camera_icon);
                    Log.d("FILE PATH", image_file.getParent());
                }
                catch (IOException ex) {
                    // Error occurred while creating the File
                    ex.printStackTrace();
                    Log.d("FILE", "FILE NOT CREATED***********************************************************************************************");
                }

               // File file = new File(Environment.getExternalStorageDirectory(), image_path);
                ImageCapture.OutputFileOptions outputFileOptions;
                //if (file != null)
                outputFileOptions = new ImageCapture.OutputFileOptions.Builder(image_file).build();

                imageCapture.takePicture(outputFileOptions, executor, new ImageCapture.OnImageSavedCallback () {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Log.d("SAVED", "IMAGE SAVED");
                        startConfirm();
                        /*
                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                Log.d("SAVED", "IMAGE SAVED");
                                //camera.release();
                                startConfirm();
                                Toast.makeText(MainActivity.this, "Image Saved successfully", Toast.LENGTH_SHORT).show();
                            }
                        });
                        */
                    }
                    @Override
                    public void onError(@NonNull ImageCaptureException error) {
                        Log.d("ERROR", "IMAGE NOT SAVED");
                        error.printStackTrace();
                    }
                });


            }
        });
    }

    private File createImageFile() throws IOException {
        // Create a unique image filename
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        image_path = "photo_" + timeStamp + ".jpg";

        // Get file path where the app can save a private image
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
       // Log.d("DIRECTORY", storageDir.);
        return new File(storageDir, image_path);
    }

    public String getBatchDirectoryName() {

        String app_folder_path = "";
        app_folder_path = Environment.getExternalStorageDirectory().toString() + "/images";
        File dir = new File(app_folder_path);
        if (!dir.exists() && !dir.mkdirs()) {

        }

        return app_folder_path;
    }


    public void textSearchClick(View view) {
        String searchVal = searchText.getText().toString();
        Intent intent = new Intent(this, ResultsActivity.class);
        intent.putExtra(ResultsActivity.EXTRA_SEARCH_VAL, searchVal);
        intent.putExtra(ResultsActivity.EXTRA_LOC_VAL, locationFromMain);
        startActivity(intent);
    }

    @SuppressLint("MissingPermission")
    private void findLocation(){
        FusedLocationProviderClient client =
                LocationServices.getFusedLocationProviderClient(this);
        client.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        Log.d("Location Service: ", "location = " + location);

                        if (location != null) {
                            //Geocoder to parse location object
                            Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                            try {
                                //Use geocoder to translate latitude and longitude to city and state to send to results activity
                                List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

                                //Set locationFromMain to the cityName based on Lat and Long
                                locationFromMain = addresses.get(0).getLocality();

                            } catch (IOException e) {
                                Log.d("Location Service: ", "Failed to get lat/long from geocoder");
                            }
                        }

                    }
                });
    }

    private boolean hasLocationPermission(){
        // Request fine location permission if not already granted
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this ,
                    new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                    REQUEST_LOCATION_PERMISSIONS);
            return false;
        }

        return true;
    }
}