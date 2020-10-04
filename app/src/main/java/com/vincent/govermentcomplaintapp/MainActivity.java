package com.vincent.govermentcomplaintapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.vincent.govermentcomplaintapp.models.Admin;
import com.vincent.govermentcomplaintapp.models.Services;
import com.vincent.govermentcomplaintapp.models.User;
import com.vincent.govermentcomplaintapp.models.UserNotification;
import com.vincent.govermentcomplaintapp.ui.HomeFragment;
import com.vincent.govermentcomplaintapp.ui.LoginActivity;
import com.vincent.govermentcomplaintapp.ui.NotificationsActivity;
import com.vincent.govermentcomplaintapp.ui.ProfileSetupActivity;
import com.vincent.govermentcomplaintapp.ui.RegisterActivity;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import pl.droidsonroids.gif.GifImageView;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private Toolbar toolbar;
    public  static final String USER = "userType";

    private Fragment homeFragment;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private String currentUser;
    private List<Services> servicesList;
    private CircleImageView profileImg;

    private String imageUri;
    private String userType;
    private ProgressBar progress;
    private TextView notificationCountView;
    private ImageView notificationView;
    private int notificationCount = 0;

    private List<UserNotification> userNotificationList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        servicesList = new ArrayList<>();

        initFirebase();

        if(savedInstanceState!=null){
            userType =  savedInstanceState.getString("userType");
        }
    }

    @Override
    protected void onStart() {

        super.onStart();
        if(currentUser != null){
            initViews();
            progress.setVisibility(View.VISIBLE);
            getUserData();
            initToolbar();
        }else{

            redToLogin();
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        userType = savedInstanceState.getString("userType");
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {

        Log.d(TAG, "Calling savedInstance");
        outState.putString("userType", userType);
        super.onSaveInstanceState(outState);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.logout:
                logoutUser();
                return true;

            default:
                return false;
        }

    }

    private void profileImgClicked() {
        profileImg.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "MainActivity: User profile pic clicked");
                        if(userType!=null){
                            if(userType.matches("Admin")){
                                redToAdminProfileSetup("profile_edit");
                            }else if(userType.matches("Users")){
                                redToProfileSetup("profile_edit");
                            }

                        }else{
                            Log.d(TAG, "MainActivity: userType is null");
                            //redToReg();
                        }
                    }
                }
        );
    }


    public void getUserData(){
        firestore
                .collection("Users")
                .document(currentUser)
                .get()
                .addOnSuccessListener(
                        new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if(documentSnapshot!=null){
                                      if(documentSnapshot.get("user_type")!= null){
                                            Log.d(TAG, "MainActivity: " + "user logged in");
                                            User user = documentSnapshot.toObject(User.class);
                                            if(user!=null){
                                                imageUri = user.getImage();
                                                userType= "Users";

                                                RequestOptions requestOptions = new RequestOptions();
                                                requestOptions.placeholder(getDrawable(R.drawable.logo));
                                                Glide.with(MainActivity.this).setDefaultRequestOptions(requestOptions).load(imageUri).into(profileImg);

                                                profileImgClicked();
                                                initFragment();
                                                replaceFragment(homeFragment);
                                                Log.d(TAG, "MainActivity: " + userType);
//                                                progressBar.setVisibility(View.INVISIBLE);
                                                progress.setVisibility(View.INVISIBLE);

                                                documentSnapshot
                                                        .getReference()
                                                        .collection("Notifications")
                                                        .get()
                                                        .addOnSuccessListener(
                                                                new OnSuccessListener<QuerySnapshot>() {
                                                                    @Override
                                                                    public void onSuccess(QuerySnapshot querySnapshot) {
                                                                        if(querySnapshot!=null){

                                                                            for(DocumentChange doc_change: querySnapshot.getDocumentChanges()){

                                                                                UserNotification userNotification = doc_change.getDocument().toObject(UserNotification.class);
                                                                                userNotificationList.add(userNotification);

                                                                            }
                                                                            notificationCount = userNotificationList.size();
                                                                            notificationCountView.setText(String.valueOf(notificationCount));


                                                                        }else{
                                                                            Log.d(TAG, "Querysnapshot is null");
                                                                        }
                                                                    }
                                                                }
                                                        ).addOnFailureListener(
                                                        new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                );

                                                notificationView.setOnClickListener(
                                                        new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                redToNotifications();
                                                            }
                                                        }
                                                );



                                            }else{
                                                redToProfileSetup("Profile incomplete");
                                            }

                                        }else{
                                          userType = "Admin";
                                            getAdminData();
                                        }




                                }else{
                                    Log.d(TAG, "Get user data: Document not found");
                                }

                            }
                        }
                );
    }


    public void getAdminData(){
        firestore
                .collection("Admin")
                .document(currentUser)
                .get()
                .addOnSuccessListener(
                        new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if(documentSnapshot!=null){
                                    if(documentSnapshot.get("user_type") !=null){
                                        Admin admin = documentSnapshot.toObject(Admin.class);
                                        if(admin!=null){
                                            imageUri = admin.getImage();
                                            userType = "Admin";


                                            RequestOptions requestOptions = new RequestOptions();
                                            requestOptions.placeholder(getDrawable(R.drawable.logo));
                                            Glide.with(MainActivity.this).setDefaultRequestOptions(requestOptions).load(imageUri).into(profileImg);

                                            profileImgClicked();
                                            initFragment();
                                            replaceFragment(homeFragment);
                                            Log.d(TAG, "MainActivity: " + userType);
//                                            progressBar.setVisibility(View.INVISIBLE);
                                            progress.setVisibility(View.INVISIBLE);

                                        }else{
                                            Log.d(TAG, "Get admin data: Profile incomplete");
                                            redToAdminProfileSetup("profile_incomplete");
                                        }

                                    }else{
                                        Log.d(TAG, "Get admin data: usertype not found");
                                    }

                                }else{
                                    Log.d(TAG, "Get admin data: Document not found");
                                }
                            }
                        }
                ).addOnFailureListener(
                new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        );

    }

    public void redToNotifications(){
        Intent intent = new Intent(MainActivity.this, NotificationsActivity.class);
        startActivity(intent);
    }

    public void redToAdminProfileSetup(String reason){
        Log.d(TAG, "Redirection to admin profile activity: " + reason);
        Intent intent = new Intent(MainActivity.this, AdminProfileSetup.class);

        intent.putExtra(AdminProfileSetup.USER,userType);
        intent.putExtra(AdminProfileSetup.REASON_VISITING_PAGE, reason);
        finish();
    }

    public void redToProfileSetup(String reason){
        Log.d(TAG, "Redirection to profile activity: " + reason);
        Intent intent = new Intent(MainActivity.this, ProfileSetupActivity.class);

        intent.putExtra(ProfileSetupActivity.USER,userType);
        intent.putExtra(ProfileSetupActivity.REASON_VISITING_PAGE, reason);
        startActivity(intent);
        finish();
    }

    public void redToReg(){
        Toast.makeText(this, "Registration incomplete", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
        startActivity(intent);
        finish();

    }


    public void logoutUser(){
        auth.signOut();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    public void redToLogin(){
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    public void initFirebase(){
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        currentUser = auth.getUid();
    }

    public void initToolbar(){
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        profileImg = (CircleImageView) findViewById(R.id.user_profile_nav);
        getSupportActionBar().setTitle("");


    }

    public void replaceFragment(Fragment fragment){
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.main_container, fragment);
        ft.commitAllowingStateLoss();
    }

    public void initFragment(){
        homeFragment = new HomeFragment(this, userType);
    }

    public void initViews(){
        userNotificationList = new ArrayList<>();
        profileImg = findViewById(R.id.user_profile_nav);
        progress =  findViewById(R.id.main_loading);
        notificationCountView = findViewById(R.id.notification_counter);
        notificationView = findViewById(R.id.notification_icon);
    }



}