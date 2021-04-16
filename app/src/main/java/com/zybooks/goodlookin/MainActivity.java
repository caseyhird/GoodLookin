package com.zybooks.goodlookin;

import androidx.annotation.NonNull;
import androidx.annotation.experimental.UseExperimental;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.extensions.HdrImageCaptureExtender;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    EditText searchText;

    // Permsission and androix camera from https://akhilbattula.medium.com/android-camerax-java-example-aeee884f9102
    // note requesting permisson for camera and external storage?
    private int REQUEST_CODE_PERMISSIONS = 1001;
    private final String[] REQUIRED_PERMISSIONS = new String[]
            {"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE"};
    private Executor executor = Executors.newSingleThreadExecutor();
    PreviewView mPreviewView;
    ImageView captureImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchText = findViewById(R.id.searchEditText);
        mPreviewView = findViewById(R.id.preview_area);
        // do something with image capture or send straight to next activity?
        captureImage = findViewById(R.id.captureImg);

        if(allPermissionsGranted()){
            startCamera(); //start camera if permission has been granted by user
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

        if (allPermissionsGranted()) {
            startCamera();
        }
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

        if(requestCode == REQUEST_CODE_PERMISSIONS){
            if(allPermissionsGranted()){
                startCamera();
            } else{
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                this.finish();
            }
        }
    }

    private void startCamera() {

        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                    bindPreview(cameraProvider);
                } catch (ExecutionException | InterruptedException e) {
                    // No errors need to be handled for this Future.
                    // This should never be reached.
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }

    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {

        Preview preview = new Preview.Builder().build();

        // Requiring lens facing back
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();
        // FIXME: shoudln't need image analysis, unless can start google lens here
    //    ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
    //            .build();

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
        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, preview); //FIXME add , imageCapture
        // was Camera


        captureImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
                File file = new File(getBatchDirectoryName(), mDateFormat.format(new Date())+ ".jpg");

                // Add picture to image view and
                // Called when picture is taken
                /*
                imageCapture.takePicture(executor, new ImageCapture.OnImageCapturedCallback() {
                    @Override
                    public void onCaptureSuccess(@NonNull ImageProxy image) {
                        super.onCaptureSuccess(image);
                        // Passing image to next activity from https://stackoverflow.com/questions/11519691/passing-image-from-one-activity-another-activity
                        // Image proxy to bitmap conversion https://stackoverflow.com/questions/56772967/converting-imageproxy-to-bitmap
                        //sendToConfirmation(image);

                      //  Bitmap b = convertImageProxyToBitmap(image);
                       // image.close();
                       // Intent confirm = new Intent(, ConfirmActivity.class);
                       /// confirm.putExtra("Bitmap", b);
                        //startActivity(confirm);


                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        super.onError(exception);
                        // Toast user and then do nothing
                        couldNotCaptureToast();
                    }
                });
                */


                ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(file).build();

                imageCapture.takePicture(outputFileOptions, executor, new ImageCapture.OnImageSavedCallback () {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "Image Saved successfully", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    @Override
                    public void onError(@NonNull ImageCaptureException error) {
                        error.printStackTrace();
                    }
                });


            }
        });
    }

    private void sendToConfirmation(ImageProxy image) {
        ByteBuffer byteBuffer = image.getPlanes()[0].getBuffer();
        byteBuffer.rewind();
        byte[] bytes = new byte[byteBuffer.capacity()];
        byteBuffer.get(bytes);
        byte[] clonedBytes = bytes.clone();
        Bitmap b = BitmapFactory.decodeByteArray(clonedBytes, 0, clonedBytes.length);

        image.close();
        Intent confirm = new Intent(this, ConfirmActivity.class);
        confirm.putExtra("Bitmap", b);
        startActivity(confirm);
    }

    // FIXME: no neede anymore?
    private Bitmap toBitmap(Image image) {
        Image.Plane[] planes = image.getPlanes();
        ByteBuffer yBuffer = planes[0].getBuffer();
        ByteBuffer uBuffer = planes[1].getBuffer();
        ByteBuffer vBuffer = planes[2].getBuffer();

        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();

        byte[] nv21 = new byte[ySize + uSize + vSize];
        //U and V are swapped
        yBuffer.get(nv21, 0, ySize);
        vBuffer.get(nv21, ySize, vSize);
        uBuffer.get(nv21, ySize + vSize, uSize);

        YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, image.getWidth(), image.getHeight(), null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, yuvImage.getWidth(), yuvImage.getHeight()), 75, out);

        byte[] imageBytes = out.toByteArray();
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
    }

    public void couldNotCaptureToast() {
        Toast.makeText(this, "Image Could Not Be Captured.", Toast.LENGTH_SHORT).show();
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
        startActivity(intent);
    }

}