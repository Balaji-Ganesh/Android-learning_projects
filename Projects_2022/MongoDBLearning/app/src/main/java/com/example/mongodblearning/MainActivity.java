package com.example.mongodblearning;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.bson.Document;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import io.realm.Realm;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.Credentials;
import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoClient;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.MongoDatabase;

public class MainActivity extends AppCompatActivity {
    private MongoClient mongoClient;
    private MongoDatabase mongoDatabase;
    private EditText sourceEditText;
    private App app;
    private boolean permissionGranted;

    MongoCollection<Document> mongoCollection;
    User user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Link with the UI elements..
        sourceEditText          = (EditText) findViewById(R.id.srcEditText);
        Button uploadBtn        = (Button) findViewById(R.id.uploadBtn);
        Button findBtn          = (Button) findViewById(R.id.findBtn);
        Button updatePermissionBtn= (Button) findViewById(R.id.updatePermissionBtn);
        RadioGroup radioGroup   = (RadioGroup)findViewById(R.id.permissionsRadioGroup);
        // Unchecking initially..
        //radioGroup.clearCheck();
        // Initializing the application with Realm.
        Realm.init(getApplicationContext());

        // Get it from the MongoDB Realm's application.
        String appId = "gatepassapplication-dbzuf";
        app = new App(new AppConfiguration.Builder(appId).build()); // This will create the instance of the realm application, which is linked to online realm.

        // Trying to login anonymously..
        //Credentials credentials = Credentials.anonymous();
        // Via some, user credentials..
        Credentials credentials = Credentials.emailPassword("user@kmit.in", "user@kmit.in");
        app.loginAsync(credentials, result -> {
            if(result.isSuccess()) {
                Log.v("User", "Anonymous login success");
                user       = app.currentUser();
                mongoClient     = user.getMongoClient("mongodb-atlas");
                mongoDatabase   = mongoClient.getDatabase("KMIT_College");
                mongoCollection = mongoDatabase.getCollection("Students");
                Toast.makeText(getApplicationContext(), "User logged in successfully", Toast.LENGTH_SHORT).show();
            }
            else {
                Log.v("User", "Anonymous login failed");
                Toast.makeText(getApplicationContext(), "User Login failed", Toast.LENGTH_SHORT).show();
            }
        });

        // Registration of the new user..
        app.getEmailPassword().registerUserAsync("someone@one.org", "password", status->{
            if(status.isSuccess())
                Log.v("User", "User registered Successfully");
            else
                Log.v("User","User registration failed");
        });

        // For uploading..
        uploadBtn.setOnClickListener(view -> {
            // check for empty field..
            if(!sourceEditText.getText().toString().equals("")){
                if(uploadDetails(sourceEditText.getText().toString()))
                    Toast.makeText(getApplicationContext(), "Upload Success", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getApplicationContext(), "Upload Failed", Toast.LENGTH_SHORT).show();
            }
            else
                Toast.makeText(getApplicationContext(), "Please fill the \"collegeId\"", Toast.LENGTH_SHORT).show();
        });

        // For Finding..
        //  needed 2 things: Query filter (A BSON document)
        findBtn.setOnClickListener(view -> {
            // Check for empty field..
            if(!sourceEditText.getText().toString().equals("")) {
                if (finder(sourceEditText.getText().toString()))
                    Toast.makeText(getApplicationContext(), "Details Found successfully - in onCreate", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getApplicationContext(), "No such student roll no found. \nPlease try uploading.", Toast.LENGTH_SHORT).show();
            }
            else
                Toast.makeText(getApplicationContext(), "Please fill the \"collegeId\"", Toast.LENGTH_SHORT).show();
         });

        // for updating....
        updatePermissionBtn.setOnClickListener(view -> {
            // Check for empty field..
            if(!sourceEditText.getText().toString().equals("")) {
                if (updateDetails(sourceEditText.getText().toString(), permissionGranted))
                    Toast.makeText(getApplicationContext(), "Permission Updating done successfully", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getApplicationContext(), "Permission Updating failed", Toast.LENGTH_SHORT).show();
            }
            else
                Toast.makeText(getApplicationContext(), "Please fill the \"collegeId\"", Toast.LENGTH_SHORT).show();
        });

        // listener for the radio group..
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            /**
             * This gets triggered, when some permission radiobutton is clicked;*/
            @SuppressLint("NonConstantResourceId")
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch(checkedId){
                    case R.id.grantRadioBtn : permissionGranted=true;   break;
                    case R.id.revokeRadioBtn: permissionGranted=false;  break;
                }
            }
        });
    }


    /***Helper functions***/
    // For inserting..
    private boolean uploadDetails(String collegeId){
        AtomicBoolean uploadStatus= new AtomicBoolean(true);
        // let's try to insert some data..
        // create a BSON document for that..
        Document document = new Document("collegeId", collegeId) // use user.getId() to get the unique id generated by Mongod
                                        .append("permissionStatus", "NOT_GRANTED");     // Initially not granting any permission..
        // make an insertion..
        mongoCollection.insertOne(document).getAsync(status->{
            if(status.isSuccess()){
                Log.v("Data Insertion", "Insertion done successfully");
                //Toast.makeText(MainActivity.this, "Data insertion done successfully.", Toast.LENGTH_SHORT).show();
                uploadStatus.set(true);
            }
            else {
                Log.v("Data Insertion", "Insertion failed. Error: " + status.getError().toString());
                //Toast.makeText(MainActivity.this, "Data insertion failed.\n Error: "+status.getError().toString(), Toast.LENGTH_SHORT).show();
                uploadStatus.set(false);
            }
        });
        return uploadStatus.get();
    }

    // For finding...
    private boolean findCollegeId(String collegeId){
        //Log.i("Finding", sourceEditText.getText().toString());
        Document queryFilter = new Document().append("collegeId", collegeId);
        //RealmResultTask<Document> findTask;
        AtomicBoolean foundStatus= new AtomicBoolean(true);
        mongoCollection.findOne(queryFilter).getAsync(status->{
            try {
                //Toast.makeText(getApplicationContext(), "Data after query: " + status.get().toString(), Toast.LENGTH_SHORT).show();
                Toast.makeText(getApplicationContext(), "status: "+status.get().toString(), Toast.LENGTH_SHORT).show();
                if (status.isSuccess()){// || status.get().getString("permissionStatus").length()>0) {
                    Toast.makeText(getApplicationContext(), "Found data.\n Permission Status: " + status.get().getString("permissionStatus").toString(), Toast.LENGTH_SHORT).show();
                    Log.v("Finding", "Found data: " + status.get().toString());
                    foundStatus.set(true);
                } else {
                    Toast.makeText(getApplicationContext(), "Data not found. Error: " + status.getError().toString(), Toast.LENGTH_SHORT).show();
                    Log.v("Data Finding", "Data not found. Error: " + status.getError().toString());
                    foundStatus.set(false);
                }
            }
            catch(Exception exception){
                foundStatus.set(false);

                Log.e("Finding error: ", "Error: "+ Arrays.toString(exception.getStackTrace()));
                exception.printStackTrace();
            }
        });
        return foundStatus.get();
    }

    private boolean finder(String collegeId){
        Document queryFilter = new Document("collegeId", collegeId);
        AtomicBoolean findStatus = new AtomicBoolean(true);
        mongoCollection.findOne(queryFilter).getAsync(task->{
            Toast.makeText(getApplicationContext(), task.get().toString(), Toast.LENGTH_SHORT).show();
            if(task.isSuccess()) {
                Toast.makeText(getApplicationContext(), "Find success", Toast.LENGTH_SHORT).show();
                findStatus.set(true);
            }
            else {
                Toast.makeText(getApplicationContext(), "Find failed", Toast.LENGTH_SHORT).show();
                findStatus.set(false);
            }
        });
        return findStatus.get();
    }

    // For updating the document..
    private boolean updateDetails(String collegeId, boolean permissionStatus){
        Document queryFilter =  new Document("collegeId", collegeId);
        Document updateDocument = new Document("$set", new Document("permissionStatus", permissionStatus?"GRANTED":"NOT_GRANTED"));
        AtomicBoolean updateStatus = new AtomicBoolean(true);                     // Assuming, the good case..
        mongoCollection.updateOne(queryFilter, updateDocument).getAsync(task->{
            Log.i("UpdateStatus", String.valueOf(task.get()));
            if(task.isSuccess()) {
                updateStatus.set(true);
                Log.v("UpdationStatus", "Update success");

                Log.v("UpdationStatus", "Data after update: "+task.get());
            }
            else {
                Log.v("UpdationStatus", "Update failed");
                updateStatus.set(false);
            }
        });
        return updateStatus.get();
    }
}

/*
* Getting some issue in finding..
*   may be the async causing issue..
* */