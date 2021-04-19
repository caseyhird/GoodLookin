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
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
    // note requesting permisson for camera and external storage
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

    /*
        Start learn more activity.
     */
    public void startLearnMore() {
        Intent confirm = new Intent(this, LearnMoreActivity.class);
        startActivity(confirm);
    }

    /*
        Start confirm activity
     */
    public void startConfirm() {
        Intent confirm = new Intent(this, ConfirmActivity.class);
        confirm.putExtra(ConfirmActivity.EXTRA_IMAGE_PATH, image_path);
        startActivityForResult(confirm, 0);
    }

    /*
        When confirm activity returns a value, if image was confirmed start the vision search
        and if not wait for a new picture.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == ConfirmActivity.RESULT_CONFIRM && requestCode == 0) {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    cameraProvider.unbindAll();
                }
            });
            try {
                String label = VisionSearch.detectLabels(image_path);
                Intent intent = new Intent(this, ResultsActivity.class);
                intent.putExtra(ResultsActivity.EXTRA_SEARCH_VAL, label);
                startActivity(intent);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /*
        Disconnect camera when main activity is destoryed.
     */
    @Override
    protected void onDestroy() {
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                cameraProvider.unbindAll();
            }
        });
        super.onDestroy();
    }

    /*
        Check that app has all necessary permissions to use the camera and store images.
     */
    private boolean allPermissionsGranted(){
        for (String permission : REQUIRED_PERMISSIONS){
            if(ContextCompat.checkSelfPermission(getApplicationContext(), permission) != PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return true;
    }

    /*
        Check that all permission have in fact been granted, and if so start the camera.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
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

    /*
        Start camera
     */
    private void startCamera() {
        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    cameraProvider = cameraProviderFuture.get();
                    bindPreview(cameraProvider);
                } catch (ExecutionException | InterruptedException e) {
                    // No errors need to be handled for this Future.
                    // This should never be reached.
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }

    /*
        Bind preview and image capture to camera, and set up image capture click
     */
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

        preview.setSurfaceProvider(mPreviewView.getSurfaceProvider());
        camera = cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, preview, imageCapture);
        /*
            When image capture is clicked, save image to external storage and pass file path
            to confirm activity.
         */
        captureImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    image_file = createImageFile();
                    image_path = image_file.getAbsolutePath();
                }
                catch (IOException ex) {
                    ex.printStackTrace();
                }

                ImageCapture.OutputFileOptions outputFileOptions;
                outputFileOptions = new ImageCapture.OutputFileOptions.Builder(image_file).build();

                imageCapture.takePicture(outputFileOptions, executor, new ImageCapture.OnImageSavedCallback () {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        startConfirm();
                    }
                    @Override
                    public void onError(@NonNull ImageCaptureException error) {
                        error.printStackTrace();
                    }
                });
            }
        });
    }

    /*
        Create external storage file to hold new image.
     */
    private File createImageFile() throws IOException {
        // Create a unique image filename
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String path = "photo_" + timeStamp + ".jpg";

        // Get file path where the app can save a private image
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return new File(storageDir, path);
    }

    /*
        Start normal text search when search bar is used.
     */
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