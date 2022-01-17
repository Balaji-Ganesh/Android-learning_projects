package com.example.captureshare;

import static com.example.captureshare.R.id.captureBtn;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;


import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_CODE    = 1000;
    private static final int IMAGE_CAPTURE_CODE = 1001;
    private static final int CAMERA_CAPTURE_REQUEST = 1001;
    private static final int REQUEST_EXTERNAL_STORAGE_RESULT  = 1;

    private static final int IMAGE_REQUEST = 1;

    private ImageView imageView;
    private Button captureBtn;
    private Button shareBtn;
    private Bitmap bitmap;
    private File pictureDirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "captureDemo");
    private Uri fileUri;
    String currImgPath=null;
    File imageFile = null;

    Uri image_uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Linking
        imageView   = (ImageView) findViewById(R.id.imageView);
        captureBtn  = (Button) findViewById(R.id.captureBtn);
        shareBtn = (Button) findViewById(R.id.shareBtn);
        shareBtn.setEnabled(false);                          // As can't share until he captures photo..

        if(!pictureDirectory.exists())
            pictureDirectory.mkdirs();

        // Event listeners..
        captureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // if System version >=Marshmallow(23), request runtime permission..
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED ||
                            checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                        // permission, not enabled, request it....
                        String[] permission = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        // Show popup to request permission..

                        requestPermissions(permission, PERMISSION_CODE);
                        //dispatchTakePictureIntent();
                    } else {
                        // permission already granted
                        openCamera();
                    }
                } else {
                    // System os < Marshmallow(23)
                    openCamera();
                }
                //openCamera();
            }
        });
        
        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //https://stackoverflow.com/questions/29629205/sharing-image-from-android-app/29629335
                try{
    //                Intent intent = getIntent();
    //                Uri uri = intent.getParcelableExtra(currImgPath);
    //
    //                File file = new File(uri.getPath());
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
    //                Uri fileUri = FileProvider.getUriForFile(MainActivity.this, "com.example.captureshare.fileprovider", file); // getApplicationContext().getPackageName()+"fileprovider"
                        Uri imageUri = FileProvider.getUriForFile(MainActivity.this, "com.example.captureshare.fileprovider", imageFile); // copied from AndroidManifest

                    shareIntent.setDataAndType(imageUri, "image/*");
                    shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                    shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(shareIntent);
                }
                catch (Exception e){
                    Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void openCamera() {
        /*
        ContentValues values =  new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Capture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "from Camera capture");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        // Camera Intent..
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE);*/
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        File image    = new File(pictureDirectory, "testImage.jpg");
//        fileUri       = Uri.fromFile(image);
//        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
//        startActivityForResult(intent, CAMERA_CAPTURE_REQUEST);


        if (cameraIntent.resolveActivity(getPackageManager()) != null) {

            try {
                // get valid image file
                imageFile = getUniqueImgFileName();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Check whether created succesfully or not
            if (imageFile != null) {
                Uri imageUri = FileProvider.getUriForFile(MainActivity.this, "com.example.captureshare.fileprovider", imageFile); // copied from AndroidManifest
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

                startActivityForResult(cameraIntent, IMAGE_REQUEST);
            }
        }
    }

    @NonNull
    private File getUniqueImgFileName()throws IOException {
        // Uses timestamp to generate...
        String timeStamp =  new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageName = "CaptureImage"+timeStamp;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File imageFile = File.createTempFile(imageName, ".jpg", storageDir);
        currImgPath = imageFile.getAbsolutePath();
        return imageFile;
    }

    // handling permissions result..
    @Override
    public void onRequestPermissionsResult (int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // this method gets called, when user clicks on "Allow" or "Deny" on Permission request popup..
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // when permission popup was granted//
                    openCamera();
                } else {
                    // permission from popup was denied..
                    Toast.makeText(this, "Permission Denied, can't capture..", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        // Called when image was captured from camera..
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK) {
            // set the image captured to our imageView
//            File image = new File(pictureDirectory, "testImage.jpg");
//            fileUri = Uri.fromFile(image);
//            imageView.setImageURI(image_uri);

            //imageView = (ImageView) findViewById(R.id.imgView);
            Bitmap bitmap = BitmapFactory.decodeFile(currImgPath);
            imageView.setImageBitmap(bitmap);

            // Turn on the Share button.. as now photo is ready..
            shareBtn.setEnabled(true);
        }
    }
}

/*
public class MainActivity extends AppCompatActivity {

    private ImageView imageView;
    private Button captureBtn;
    private Button shareBtn;
    private Button viewBtn;
    String currImgPath = null;
    private static final int IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        captureBtn = (Button) findViewById(R.id.captureBtn);
        shareBtn = (Button) findViewById(R.id.shareBtn);
        viewBtn = (Button) findViewById(R.id.viewBtn);

        captureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                // check is it capable or not..
                if(cameraIntent.resolveActivity(getPackageManager()) != null){
                    File imageFile = null;
                    try{
                        // get valid image file
                        imageFile = getUniqueImgFileName();
                    }
                    catch (IOException e){
                        e.printStackTrace();
                    }
                    // Check whether created succesfully or not
                    if(imageFile!=null){
                        Uri imageUri = FileProvider.getUriForFile(MainActivity.this, "com.example.captureshare.fileprovider", imageFile); // copied from AndroidManifest
                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

                        startActivityForResult(cameraIntent, IMAGE_REQUEST);;
                    }
                }

            }
        });

//        viewBtn.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(MainActivity.this, DisplayImage.class);
//                intent.putExtra("image_path", currImgPath);
//                startActivity(intent);
//            }
//        });


    }
    private File getUniqueImgFileName()throws IOException {
        // Uses timestamp to generate...
        String timeStamp =  new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageName = "CaptureImage"+timeStamp;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File imageFile = File.createTempFile(imageName, ".jpg", storageDir);
        currImgPath = imageFile.getAbsolutePath();
        return imageFile;
    }
}*/

//https://youtu.be/LpL9akTG4hI
/*
* Toast.makeText(getBaseContext(), "entry", Toast.LENGTH_SHORT).show();
                //https://stackoverflow.com/questions/29629205/sharing-image-from-android-app/29629335
                try{
                    File myFile = new File(currImgPath);
                    MimeTypeMap mime = MimeTypeMap.getSingleton();
                    String ext = myFile.getName().substring(myFile.getName().lastIndexOf(".") + 1);
                    String type = mime.getMimeTypeFromExtension(ext);
                    Intent sharingIntent = new Intent("android.intent.action.SEND");
                    sharingIntent.setType(type);
                    sharingIntent.putExtra("android.intent.extra.STREAM", Uri.fromFile(myFile));
                    startActivity(Intent.createChooser(sharingIntent, "Share using"));
                }
                catch (Exception e){
                    Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                Toast.makeText(getBaseContext(), "exit", Toast.LENGTH_SHORT).show();
*
* */