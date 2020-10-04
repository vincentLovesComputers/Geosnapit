package com.vincent.govermentcomplaintapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class AdminLogginInSplashScreen extends AppCompatActivity {


    public static final String TAG = "AdminLogginIn";

    public static final String NAME_INPUT = "name_input";
    public static final String IMAGE_URI = "image_uri";
    public static final String ADMIN_ID = "admin_id";
    public static final String CITY = "city";
    public static final String PROVINCE = "province";
    public static final String MUNICIPALITY = "municipality";
    public static final String POST_CODE = "post_code";
    public static final String ROLE = "role";
    public static final String IS_CHANGED = "isChanged";


    //views vars
    private TextView loggingHelperText;

    //firebase vars
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private String currentAdmin;
    private ProgressBar progressBar;
    StorageReference storageReference;

    private Intent intent;
    private String name;
    private Uri imageUri;
    private String adminId;
    private String city;
    private String province;
    private String municipality;
    private String postCode;
    private String role;
    private boolean isChanged;

    private TextView loadingProgressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_loggin_in_splash_screen);
        loadingProgressView = findViewById(R.id.loading_helper_text);
        initFirebase();
        getIntentData();
        saveToDb();

    }

    public void getIntentData() {
        intent = getIntent();
        name = intent.getStringExtra("name_input");
        imageUri = intent.getParcelableExtra("image_uri");
        adminId = intent.getStringExtra("admin_id");
        city = intent.getStringExtra("city");
        province = intent.getStringExtra("province");
        municipality = intent.getStringExtra("municipality");
        postCode = intent.getStringExtra("post_code");
        role = intent.getStringExtra("role");
        isChanged = intent.getBooleanExtra("isChanged", false);


    }

    public void saveToDb() {
        StorageReference imagePath = storageReference.child("profile_img").child("admin").child(currentAdmin + ".jpg");
        if (isChanged) {
            imagePath.putFile(imageUri)
                    .addOnSuccessListener(
                            new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    if (taskSnapshot.getMetadata() != null) {
                                        if (taskSnapshot.getMetadata().getReference() != null) {
                                            taskSnapshot.getMetadata().getReference().getDownloadUrl()
                                                    .addOnCompleteListener(
                                                            new OnCompleteListener<Uri>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Uri> task) {
                                                                    if (task.isSuccessful()) {
                                                                        saveToFirestore(task);
                                                                    } else {
                                                                        String error = task.getException().getMessage();
                                                                        Toast.makeText(AdminLogginInSplashScreen.this, error, Toast.LENGTH_SHORT).show();
                                                                    }
                                                                }
                                                            }
                                                    );
                                        }
                                    }
                                }
                            }
                    );
        } else {
            Log.d(TAG, "Profile setup: image not saved");
            saveToFirestore(null);
        }
    }

    public void saveToFirestore(Task<Uri> task) {
        Log.d(TAG, "Admin data: " + "Save to fire store function");
        Uri newImageUri;
        if (task != null) {
            newImageUri = task.getResult();
        } else {
            Log.d(TAG, "Task is null");
            newImageUri = imageUri;
        }

        if (newImageUri != null) {
            saveData(newImageUri);
        } else {
            Log.d(TAG, "Image uri is null");
        }

    }

    public void saveData(Uri newImageUri) {
        Map<String, String> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("image", newImageUri.toString());
        userData.put("admin_id", currentAdmin);
        userData.put("city", city);
        userData.put("province", province);
        userData.put("municipality", municipality);
        userData.put("postal_code", postCode);
        userData.put("role", role);
        userData.put("user_type", "Admin");

        firestore.collection("Admin").document(currentAdmin)
                .set(userData)
                .addOnCompleteListener(
                        new OnCompleteListener<Void>() {

                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {

                                    Log.d(TAG, "User Data: successfully saved");
                                    Toast.makeText(AdminLogginInSplashScreen.this, "Profile updated", Toast.LENGTH_SHORT).show();
                                    redToHome();

                                } else {

                                    String error = task.getException().getMessage();
                                    Log.d(TAG, "Saving User Data error: " + error);
                                    Toast.makeText(AdminLogginInSplashScreen.this, error, Toast.LENGTH_SHORT).show();
                                    redToAdminProfileSetup();
                                }
                            }
                        }
                );

    }
    public void redToHome(){
        intent = new Intent(AdminLogginInSplashScreen.this, MainActivity.class);
        intent.putExtra(MainActivity.USER, "Admin");
        startActivity(intent);
    }

    public void redToAdminProfileSetup(){
        intent = new Intent();
        intent.putExtra(AdminProfileSetup.REASON_VISITING_PAGE, "error_saving_profile_data");
        startActivity(intent);
    }

    public void initFirebase(){
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        currentAdmin = auth.getUid();
        storageReference = FirebaseStorage.getInstance().getReference();
    }
}