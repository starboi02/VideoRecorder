package com.example.videorecorder;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    static final int REQUEST_VIDEO_CAPTURE = 1;
    static final int REQUEST_WRITE_EXTERNAL_STORAGE=2;
    File file;
    Button btn,admin,logout;
    ProgressDialog progressDialog;
    GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn=findViewById(R.id.btn);
        admin=findViewById(R.id.admin);
        logout=findViewById(R.id.logout);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("60686680706-80h80o1o2mncrnhrr6pqut6hg9kg9lsh.apps.googleusercontent.com")
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        progressDialog= new ProgressDialog(MainActivity.this);
        progressDialog.setTitle("Uploading...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);

        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE},
                REQUEST_WRITE_EXTERNAL_STORAGE);

        file = new File(getExternalFilesDir(Environment.DIRECTORY_DCIM), "recorded.mp4");

        Uri fileUri = FileProvider.getUriForFile(MainActivity.this, "com.example.videorecorder", file);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
                }

            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                mAuth.signOut();

                mGoogleSignInClient.signOut().addOnCompleteListener(MainActivity.this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(MainActivity.this,"Logged Out",Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(MainActivity.this,LoginActivity.class));
                    }
                });
            }
        });

        admin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,AdminActivity.class));
            }
        });

    }

    public void uploadFileToStorage(Uri uri){
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference =storage.getReference();
        final StorageReference ref
                = storageReference
                .child(
                        "files/"
                                + UUID.randomUUID().toString());
        UploadTask uploadTask=ref.putFile(uri);

        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return ref.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(MainActivity.this,"Upload Successfull!",Toast.LENGTH_SHORT).show();
                    Uri downloadUri = task.getResult();
                    String downloadUrl = downloadUri.toString();
                    Log.d("url", downloadUrl);
                    if(!downloadUrl.isEmpty()){
                        uploadUrlToFireStore(downloadUrl);
                    }

                } else {
                    progressDialog.dismiss();
                    Toast.makeText(MainActivity.this,"Upload Failed!",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public void uploadUrlToFireStore(String url){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> map = new HashMap<>();
        map.put("URL", url);

        db.collection("URLs")
                .add(map)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        progressDialog.dismiss();
                        delteFile();
                        Log.d("URL_Upload", "DocumentSnapshot added with ID: " + documentReference.getId());

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Log.w("URL_Upload", "Error adding document", e);
                    }
                });
    }

    public void delteFile(){
        boolean f =file.delete();
        Log.d("deleted", String.valueOf(f));
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            Uri videoUri = intent.getData();
            progressDialog.show();
            uploadFileToStorage(videoUri);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {

                Toast.makeText(MainActivity.this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
            }
            return;

        }
    }

    @Override
    public void onBackPressed() {
            Intent homeScreenIntent = new Intent(Intent.ACTION_MAIN);
            homeScreenIntent.addCategory(Intent.CATEGORY_HOME);
            homeScreenIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(homeScreenIntent);
    }

}