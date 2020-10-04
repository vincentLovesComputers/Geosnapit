package com.vincent.govermentcomplaintapp.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.vincent.govermentcomplaintapp.MainActivity;
import com.vincent.govermentcomplaintapp.R;

public class LoginActivity extends AppCompatActivity {

    public static final String TAG = "LoginActivity";

    FirebaseAuth auth;

    //var
    private TextInputEditText emailView;
    private TextInputEditText pwdView;
    private Button signInBtn;
    private TextView signUpView;

    //layouts
    TextInputLayout emailLayout;
    TextInputLayout pwdlayout;

    ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        refViews();
        auth = FirebaseAuth.getInstance();
        loginUser();
        signUpBtnPressed();

        hideSoftKeyboard();


    }

    public void loginUser(){

        signInBtn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String emailInput = emailView.getText().toString();
                        final String pwdInput = pwdView.getText().toString();
                        progressBar.setVisibility(View.VISIBLE);
                        if(validateInputs(emailInput, pwdInput)){
                            auth.signInWithEmailAndPassword(emailInput, pwdInput).addOnCompleteListener(
                                    new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if(task.isSuccessful()){
                                                progressBar.setVisibility(View.INVISIBLE);
                                                //loggin in  successful
                                                Log.d(TAG, "Redirecting home");
                                                redToHome();
                                            }else{
                                                progressBar.setVisibility(View.INVISIBLE);
                                                //error loggin in
                                                Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }
                            );
                        }else{
                            //loggin in error inputs error
                            Log.d(TAG, "Login: inputs invalid");
                            if(emailInput.matches("")){
                                emailLayout.setError(getResources().getString(R.string.empty_fields));
                            }
                            else if(pwdInput.matches("")){
                                pwdlayout.setError(getResources().getString(R.string.empty_fields));
                                pwdView.setText("");
                            }


                        }
                    }
                }
        );
    }


    public boolean validateInputs(String email, String pwd){
        if(email.matches("") && pwd.matches("")){
            return false;
        }else{
            return true;
        }
    }

    public void signUpBtnPressed(){
        signUpView.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        redToSignUp();
                    }
                }
        );
    }

    public void redToSignUp(){
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
        finish();
    }

    public void redToHome(){
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void refViews(){
        emailView = (TextInputEditText) findViewById(R.id.signin_email);
        pwdView = (TextInputEditText) findViewById(R.id.signin_pwd);
        signInBtn = (Button) findViewById(R.id.signin_btn);
        signUpView = (TextView) findViewById(R.id.signin_signup_btn);
        progressBar = (ProgressBar) findViewById(R.id.login_prog_bar);
       emailLayout = (TextInputLayout) findViewById(R.id.login_field_layout);
       pwdlayout = (TextInputLayout) findViewById(R.id.password_field_layout);
    }

    private void hideSoftKeyboard(){
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }
}