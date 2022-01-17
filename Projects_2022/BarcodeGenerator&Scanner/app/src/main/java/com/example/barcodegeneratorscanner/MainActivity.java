package com.example.barcodegeneratorscanner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.File;
import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_CODE=101;
    private static final int FILE_SHARE_PERMISSION=102;
    private TextView textView;
    private ImageView barcodeImgView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get the handle of image view, ...
        barcodeImgView = findViewById(R.id.bar_codeImgView);
        // Get the handle of text view..
        textView = findViewById(R.id.textView);

        // Data to store in the QR code...
        String sourceData = "Testing the application of QR code generator and scanner";
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(sourceData, BarcodeFormat.QR_CODE, 200, 200);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            barcodeImgView.setImageBitmap(bitmap);
        } catch (Exception error) {
            error.printStackTrace();
        }
        //--------- Done with the generation of the QR code from the data that had.------

        // Now, starts the procedure of **Scanning a QR code**
        Button scanBtn = findViewById(R.id.scanBtn);
        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= 23) {
                    if (checkPermission(Manifest.permission.CAMERA)) {
                        openScanner();
                    } else {
                        requestPermission(Manifest.permission.CAMERA, CAMERA_PERMISSION_CODE);
                    }
                } else {
                    openScanner();
                }
            }
        });

        // Facility of sharing the QR code..
        // For sharing: Saving in temp location, then saving.
        //   so needed, FILE - Write permission
        Button shareQrCodeBtn = findViewById(R.id.shareQrCodeBtn);
        shareQrCodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT >= 23){
                     if(checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE))
                         shareQrCode();             // When granted permission, share the QR code..
                    else
                        requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, FILE_SHARE_PERMISSION);
                }
                else
                    shareQrCode();
            }
        });
    }


    private void shareQrCode(){
        Toast.makeText(this, "Sharing option .. work in progress", Toast.LENGTH_SHORT).show();
        // create a file provider..
        barcodeImgView.setDrawingCacheEnabled(true);
        Bitmap bitmap = barcodeImgView.getDrawingCache();
        File file = new File(Environment.getExternalStorageDirectory(), "bar_code1.jpg");
        try{
            file.createNewFile();
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            fileOutputStream.close();
            Toast.makeText(this, "Sharing option .. Saved image successfully", Toast.LENGTH_SHORT).show();

            // Now, share the file, which saved.
            Intent intent = new Intent(Intent.ACTION_SEND);
            // if SDK>7, read via FileProvider else, via normal method.
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.N) {  // N: Naughat - SDK 7
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(MainActivity.this, "com.example.barcodegeneratorscanner", file));
            }
            else{
                intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
            }
            intent.setType("image/*");
            startActivity(intent);
            Toast.makeText(this, "Sharing option .. work Done", Toast.LENGTH_SHORT).show();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    // After done with the part of having the permission to the camera..
    private void openScanner(){
        new IntentIntegrator(MainActivity.this).initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        // on successful scanner read, parse the data..
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null){
            if(result.getContents() == null)
                Toast.makeText(this, "No data found", Toast.LENGTH_SHORT).show();
            else
                textView.setText("Data: "+result.getContents());
        }else
            Toast.makeText(this, "No data to read", Toast.LENGTH_SHORT).show();
    }


    /* For API's >=6(Marshmallow) - 23 .. For checking permission..*/
    // A method for checking the already granted permission
    private boolean checkPermission(String permission){
        int result = ContextCompat.checkSelfPermission(MainActivity.this, permission);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    // A method for requesting permission..
    private void requestPermission(String permission, int code){
            if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permission)){
                ;   // empty block
            }
            else
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, code);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String [] permissions, @NonNull int [] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_CODE)
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                openScanner();
    }
}

// src: supercoders.in - YouTube: https://youtu.be/IIaymU1OaGQ?t=430 --  on 10th January, 2022
//  Done -  on 11th January, 2022
// Some issue:
    // UI glitch in horizontal mode -- currently out of project scope -- so neglected.
    // Sharing not working. -- Try using the previous work of """Capture & Share"""
// for integration with the mongodb-realm: https://youtu.be/4aScZNF2Tdw?t=333
