package com.zybooks.goodlookin;

// Cloud vision imports
import android.os.Build;
import android.util.Log;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.Credentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Feature.Type;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.ImageAnnotatorSettings;
import com.google.protobuf.ByteString;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import java.io.IOException;

// VisionSearch singleton class for using Google vision label detection api
public class VisionSearch {
    // FIXME: HOW TO STORE THIS SECURELY?
    private static final String CREDENTIALS_FILE = "{\n" +
            "  \"type\": \"service_account\",\n" +
            "  \"project_id\": \"goodlookintest\",\n" +
            "  \"private_key_id\": \"d1a7f976816e6e52e704816ebecadde32c8b0a56\",\n" +
            "  \"private_key\": \"-----BEGIN PRIVATE KEY-----\\nMIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC6/v+2Gc/4mFRy\\nYbPl8mqKH2gDqHwCQgbojpNJVn7WYvKB6dA0PU6CxyqA2IBbP1vpor3jN49HD2uK\\nE/PF7uYql18dr52HFPtveqgX3WYE8E4GyNPWwa4RhEc8Rfz4lK81N+R99jUDacXo\\nbxNFlohgXArewpfyQsI6z69LUfIijpQfo+dOIxWQNcURV7XfDML4TdKB6676uiRE\\nJtGoPkPhSZyEK01s34mqg4a/NWb0nz0cdDMY1cS3cIQgMrtkkhFJD06ta14uY1VM\\n0E79BzaCGbpAIRiYLe4ZDSdS5GuKkW5S79WK6FJXEi8RnPWe1Ue54b4ziV+2JEx3\\nTdYvqfAJAgMBAAECggEAAIdYSdI5R0NcqMKhNN8aYUKCY3vtAX92NXSIEtLiEqvZ\\nXO1AQ7zhIrzwADOB1brgVlukZGGGX+MZphx/6dE5jh5bBt0rCY7cAPY527BqRDe5\\njBVyW4xKYv21YFn1I5mdrwg14YvKHaolb2PsrBDnWONlu03ASs9uF7xkKzG4/iiZ\\nCqF+YhrhopVGnv7Z0hlB19dacLc0hCUxIj9mQa5YnZgEe4WwLcdsBEoPYE5rKSVL\\nk+swHvb//UEUxTZNOPjPU2DgEM10aN5Yz2aaK+SZ0e/cqlEnSOK3YB+13+xerdbQ\\nPaxIIAsxVlNFRrrU2ho+RnQVCIn5tw1LkVLSWoQPcQKBgQD5InS6yOJyd9xLvL2t\\n++9xyyFgA2NqMe5f6Z4M8xVf3JYpWLVJEBEc5qXTNXtUkOgMg8uauZK6Tr+DsZwK\\n3FDiDXdwBr7l3byvm9J0pidENU4U4TTQezAhIY6CnYC3fRkXqmh8t2oS+45GAd7F\\nfE8y3VnHor2PItadn+ElnnHj0QKBgQDAJi5Kfd0eG2iP7Ltxeh64t+0jPprUkBfF\\n0ZgiBwz2wMLFuS9kfKygoP1OopdnjqntsiOPlWFiXYqSEvUwh0ntLHadpIP+v2aP\\nA2AxRaGnELtiznNALDAKjcxcVbiH1yMk4bgKJKBRo44h0maECyKaJRb6QVd3bIv6\\nUgWwKJDuuQKBgGki4lIE/pNCA+SuZPmsbTL/fzkulOC265rsUveyCd4nj/Mo7XBE\\n6IPizi4gzsg0UskdQWotUD9xhh7EcE6hBT0wY6wSHLOS7NLLFniFueJuAGKNW6Vz\\npy8EI0j5wN0uXM2A65FeMdSK9lKS7Xk36ZBUm1PFWOuzzxA77V7by8JRAoGAWlpZ\\n0BtpO3wF6g+WgKC9C30pXDZXFfb/xrxs/is0lF9F9zXCLE1X21x4YF6iNUbnbuci\\nQ6UpmtiDjXaupvgm8gjDahNvIf4cmskqlWC5x9ZdVvPfS+C7YmsVGqIQWIf4daJS\\nKSrZ3Cm9bvgBzrtxtOwV5u2M1JJNLBMfq5sXs7kCgYEA4JKTTDUVMlohdUteNt8Y\\nTdKhY5yxMtwg9TNwmQ7iy/gjbzDLSkjOYigYMOBGYC/v/I3Z5eiLM4tdmjjKPTcu\\n+tbSXLpY7C2jsBp2kK/59HlUhisyakAqn0HYPzPCbDRvAuObSMD1k5SICK8oHuNT\\n1jWkw05STjwiOtq9SCFvVFo=\\n-----END PRIVATE KEY-----\\n\",\n" +
            "  \"client_email\": \"team4casey@goodlookintest.iam.gserviceaccount.com\",\n" +
            "  \"client_id\": \"109213886830804998532\",\n" +
            "  \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n" +
            "  \"token_uri\": \"https://oauth2.googleapis.com/token\",\n" +
            "  \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\",\n" +
            "  \"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/team4casey%40goodlookintest.iam.gserviceaccount.com\"\n" +
            "}\n";

    private static VisionSearch search_instance = null;

    private VisionSearch() {

    }

    public static VisionSearch getInstance() {
        if (search_instance == null)
            search_instance = new VisionSearch();
        return search_instance;
    }
/*
    // attempt 2, tyring to add authentication
    // using https://code.tutsplus.com/tutorials/how-to-use-the-google-cloud-vision-api-in-android-apps--cms-29009
    public static void detectLabels(String filepath) {
        Vision.Builder visionBuilder = new Vision.Builder(
                new NetHttpTransport(),
                new AndroidJsonFactory(),
                null);

        visionBuilder.setVisionRequestInitializer(
    }
*/

    // label detection function from https://www.programcreek.com/java-api-examples/?class=com.google.cloud.vision.v1.AnnotateImageResponse&method=getLabelAnnotationsList
    public static void detectLabels(String filepath) throws IOException {
        // Initialize client that will be used to send requests. This client only needs to be created
        // once, and can be reused for multiple requests. After completing all of your requests, call
        // the "close" method on the client to safely clean up any remaining background resources.
        Log.d("FUNCTION", "IN FUNCTION");
        InputStream fin;
        Credentials myCredentials;
        ImageAnnotatorSettings imageAnnotatorSettings;
        ImageAnnotatorClient vision;
        //try {
            fin = new ByteArrayInputStream(CREDENTIALS_FILE.getBytes(StandardCharsets.UTF_8));
        //} catch(IOException e) {
        //    throw e;
        //}
        try {
            myCredentials = ServiceAccountCredentials.fromStream(fin);
        } catch(IOException e) {
            throw e;
        }
        try {
            imageAnnotatorSettings = ImageAnnotatorSettings.newBuilder()
                            .setCredentialsProvider(FixedCredentialsProvider.create(myCredentials))
                            .build();
            Log.d("SETTINGS", "BUILT SETTINGS");
        } catch(IOException e) {
            throw e;
        }
        try {
            vision = ImageAnnotatorClient.create(imageAnnotatorSettings);
        } catch(IOException e) {
            throw e;
        }
      //   try (
                /*
                ImageAnnotatorSettings.Builder imageAnnotatorSettingsBuilder = ImageAnnotatorSettings.newBuilder();
                imageAnnotatorSettingsBuilder
                        .batchAnnotateImagesSettings()
                        .setRetrySettings(
                                imageAnnotatorSettingsBuilder
                                        .batchAnnotateImagesSettings()
                                        .getRetrySettings()
                                        .toBuilder()
                                        .setTotalTimeout(Duration.ofSeconds(30))
                                        .build());
                ImageAnnotatorSettings imageAnnotatorSettings = imageAnnotatorSettingsBuilder.build();
                */
                /*
                // authentication attempt from https://stackoverflow.com/questions/47609597/how-can-i-use-imageannotatorclient-with-explicit-authentication
                val credStream = getInputStream( "my-api-key.json" )
                val credentials = GoogleCredentials.fromStream(credStream)
                val imageAnnotatorSettings = ImageAnnotatorSettings.newBuilder()
                    .setCredentialsProvider( FixedCredentialsProvider.create( credentials ) )
                    .build();
                ImageAnnotatorClient.create( imageAnnotatorSettings )
                ImageAnnotatorClient vision = ImageAnnotatorClient.create();
                */
                // trying again from https://stackoverflow.com/questions/48850479/google-vision-api-load-credentials-from-file


      //  ) {

            // The path to the image file to annotate
            //String fileName = "./resources/ic_launcher_foreground.xml";
            Log.d("CLIENT", "GOT CLIENT");
            // Reads the image file into memory
            // FIXME: IS THIS AN ACCEPTABLE MINIMUM API LEVEL?
            if (Build.VERSION.SDK_INT >= 26) {
                filepath = "/Users/casey/Documents/Academic/Spring2021/CPSC4150/glasses.jpg";
                Path path = Paths.get(filepath);
                byte[] data = Files.readAllBytes(path);
                ByteString imgBytes = ByteString.copyFrom(data);

                // Builds the image annotation request
                List<AnnotateImageRequest> requests = new ArrayList<>();
                Image img = Image.newBuilder().setContent(imgBytes).build();
                Feature feat = Feature.newBuilder().setType(Type.LABEL_DETECTION).build();
                AnnotateImageRequest request =
                        AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
                requests.add(request);

                // Performs label detection on the image file
                BatchAnnotateImagesResponse response = vision.batchAnnotateImages(requests);
                List<AnnotateImageResponse> responses = response.getResponsesList();

                for (AnnotateImageResponse res : responses) {
                    if (res.hasError()) {
                        Log.d("Error: ", res.getError().getMessage());
                        return;
                    }

                    for (EntityAnnotation annotation : res.getLabelAnnotationsList()) {
                        annotation.getAllFields()
                                .forEach((k, v) -> Log.d("Label: ", k + " " + v.toString()));
                    }
                }
            }
       // }
    }

}
