package com.example.teeceecoderpics;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Button button;
    private ImageView imageView;

    public static final int REQUEST_IMAGE = 100;
    public static final int REQUEST_PERMISSION = 200;
    private String imageFilePath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = findViewById(R.id.button);
        imageView = findViewById(R.id.image);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION);
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCameraIntent();
            }
        });

    }

    private void openCameraIntent() {
        Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (pictureIntent.resolveActivity(getPackageManager()) != null) {

            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            Uri photoUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", photoFile);
            pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            startActivityForResult(pictureIntent, REQUEST_IMAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Thanks for granting Permission", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE) {
            if (resultCode == RESULT_OK) {
                imageView.setImageURI(Uri.parse(imageFilePath));


                // Get list of folders/files in given directory
                String path = Environment.getExternalStorageDirectory().toString() + "/Pictures";
                Log.d("Files", "Path: " + path);
                File directory = new File("/storage/emulated/0/DCIM/Camera/");
                File[] files = directory.listFiles();
                Log.d("Files", "Size: " + files.length);
                for (int i = 0; i < files.length; i++) {
                    Log.d("Files", "FileName:" + files[i].getName());
                }
                // Get list of folders/files in given directory

//                Bitmap bitmap = BitmapFactory.decodeFile(mImageUri.getPath());


                Log.i("imageFilePath", imageFilePath);

//                ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.logo);
//                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
//                byte[] imageBytes = baos.toByteArray();
//                String imageString = Base64.encodeToString(imageBytes, Base64.DEFAULT);


                uploadImageToAWS(imageFilePath);


            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "You cancelled the operation", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static void longLog(String str) {
        if (str.length() > 4000) {
            Log.d("", str.substring(0, 4000));
            longLog(str.substring(4000));
        } else
            Log.d("", str);
    }

    private File createImageFile() throws IOException {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        imageFilePath = image.getAbsolutePath();
        uploadImageToAWS(imageFilePath);
        return image;
    }

    private void uploadImageToAWS(final String imageFilePath) {
        AsyncTask<String, String, String> _Task = new AsyncTask<String, String, String>() {

            @Override
            protected void onPreExecute() {
            }

            @Override
            protected String doInBackground(String... arg0) {

                if ("JOE" == "JOE") {
                    try {
                        java.util.Date expiration = new java.util.Date();
                        long msec = expiration.getTime();
                        msec += 1000 * 60 * 60; // 1 hour.
                        expiration.setTime(msec);
                        publishProgress(arg0);
                        String filePath = imageFilePath;

                        String result = filePath.substring(filePath.lastIndexOf('/') + 1).trim();

                        String existingBucketName = "";////////////
                        String keyName = result;
                        AmazonS3Client s3Client1 = new AmazonS3Client(new BasicAWSCredentials("", ""));
                        PutObjectRequest por = new PutObjectRequest(existingBucketName,
                                keyName, new File(filePath));//key is  URL

                        //making the object Public
                        por.setCannedAcl(CannedAccessControlList.PublicRead);
                        s3Client1.putObject(por);
                        String _finalUrl = "https://" + existingBucketName + ".s3.amazonaws.com/" + keyName + ".png";

                    } catch (Exception e) {
                        // writing error to Log
                        e.printStackTrace();
                    }
                } else {

                }
                return null;
            }

            @Override
            protected void onProgressUpdate(String... values) {
                // TODO Auto-generated method stub
                super.onProgressUpdate(values);
                System.out.println("Progress : " + values);
            }

            @Override
            protected void onPostExecute(String result) {

            }
        };
        _Task.execute((String[]) null);


    }


}
