package com.vincent.govermentcomplaintapp.ui;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.vincent.govermentcomplaintapp.R;

public class RegisterActivity extends AppCompatActivity  {

    private static final String TAG = "RegisterActivity";

    //error user gets when they dont have correct version of google services
    private static final int ERROR_DIALOG_REQUEST = 9001;

    //vars
    private TextInputEditText emailView;
    private TextInputEditText pwdView;
    private TextInputEditText confirmPwdView;
    private Button signUpBtn;
    private TextView signInBtn;
    private ProgressBar progressBar;
    private ImageView adminImg;

    //layouts
    TextInputLayout emailLayout;
    TextInputLayout pwdLayout;
    TextInputLayout confirmPwdLayout;

    //firebase vars
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        emailView = (TextInputEditText) findViewById(R.id.signup_email);
        pwdView = (TextInputEditText) findViewById(R.id.signup_pwd);
        confirmPwdView = (TextInputEditText) findViewById(R.id.signup_pwd_confirm);
        signUpBtn = (Button) findViewById(R.id.signup_btn);
        signInBtn = (TextView) findViewById(R.id.signup_sign_in_btn);
        progressBar = (ProgressBar) findViewById(R.id.reg_prog_bar);
        emailLayout = (TextInputLayout) findViewById(R.id.signup_email_layout);
        pwdLayout = (TextInputLayout) findViewById(R.id.signup_pwd_layout);
        confirmPwdLayout = (TextInputLayout) findViewById(R.id.signup_conf_pwd_layout);
        adminImg = (ImageView) findViewById(R.id.admin_icon);

        auth = FirebaseAuth.getInstance();


        signUpUser();


        //go to login page
        signBtnPressed();

        adminImg.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        signUpAdmin();
                    }
                }
        );

    }

    public void signUpAdmin(){
        redToAdminActivity();
    }

    public void signUpUser(){


        signUpBtn.setOnClickListener(

                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        signUpBtn.setBackgroundColor(getResources().getColor(R.color.orange2));
                        progressBar.setVisibility(View.VISIBLE);
                        final String emailInput = emailView.getText().toString();
                        final String pwdInput = pwdView.getText().toString();
                        final String confirmPwdInput = confirmPwdView.getText().toString();

                        Log.d(TAG, "Email: " + emailInput);
                        Log.d(TAG, "password: " + pwdInput);
                        Log.d(TAG, "confPwd: " + confirmPwdInput);
                        if (!emailInput.matches("") && !pwdInput.matches("") && !confirmPwdInput.matches("")) {
                            if(pwdInput.equals(confirmPwdInput)){

                                auth.createUserWithEmailAndPassword(emailInput, pwdInput)
                                        .addOnCompleteListener(
                                                new OnCompleteListener<AuthResult>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                                        Log.d(TAG, "Background donw: Calling setup");
                                                            if(task.isSuccessful()){
                                                                if(isServiceOK()){
                                                                    redToProfileSetup();
                                                                }
                                                            }else{
                                                                Log.d(TAG, "Error registering: " + task.getException().getMessage());
                                                                Toast.makeText(RegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                                                        }


                                                    }
                                                }

                                        );

                            }else{
                                progressBar.setVisibility(View.INVISIBLE);
                                Log.d(TAG, "Passwords dont match");
                                pwdLayout.setError(getResources().getString(R.string.pwd_confirm_label));
                                confirmPwdLayout.setError(getResources().getString(R.string.pwd_confirm_label));
                            }

                        } else {
                            progressBar.setVisibility(View.INVISIBLE);
                            Log.d(TAG, "fields are empty");
                            emailLayout.setError(getResources().getString(R.string.empty_fields));
                            pwdLayout.setError(getResources().getString(R.string.empty_fields));
                            confirmPwdLayout.setError(getResources().getString(R.string.empty_fields));
                        }


                    }
                }
        );

    }

    public void signBtnPressed(){
        signInBtn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        signInBtn.setBackgroundColor(getResources().getColor(R.color.blue));
                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
        );
    }

    //check version of google services
    public boolean isServiceOK(){
        Log.d(TAG, "isServiceOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(RegisterActivity.this);
        if(available == ConnectionResult.SUCCESS){
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServiceOK: Google Play services is working");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error occured but we can resolve it
            Log.d(TAG, "isServiceOK: an error ocurred but we can fix it" );
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(RegisterActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }else{
            Toast.makeText(this, "We can't make map requests ", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    public void redToAdminActivity(){
        //admin page
        Intent intent = new Intent(RegisterActivity.this, AdminActivity.class);
        startActivity(intent);
    }

    public void redToProfileSetup(){
        //user page
        Log.d(TAG, "Redirecting to profile setup: " + "Users");
            Intent intent = new Intent(RegisterActivity.this, ProfileSetupActivity.class);
            intent.putExtra(ProfileSetupActivity.USER, "Users");
            intent.putExtra(ProfileSetupActivity.REASON_VISITING_PAGE, "new_user");
            startActivity(intent);
            finish();
    }

        }
