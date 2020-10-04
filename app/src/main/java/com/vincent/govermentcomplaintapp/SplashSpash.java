package com.vincent.govermentcomplaintapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import static java.lang.Thread.sleep;

public class SplashSpash extends AppCompatActivity {

    public static final String ACTIVITY_TYPE = "activity";

    FirebaseAuth auth;
    String currentUser;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
//        auth = FirebaseAuth.getInstance();
//        currentUser = auth.getCurrentUser().getUid();

        Thread thread = new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        try{
                            sleep(1000);
                            Intent intent = new Intent(SplashSpash.this, MainActivity.class);
                            startActivity(intent);
                            finish();
//                            finish();
//                            if(auth != null){
//                                Intent intent = new Intent(SplashSpash.this, MainActivity.class);
//                                startActivity(intent);
//                                finish();
//                            }else{
//                                Intent intent = new Intent(SplashSpash.this, LoginActivity.class);
//                                startActivity(intent);
//                                finish();
//                            }

                        }catch(InterruptedException e){
                            e.printStackTrace();
                        }
                    }
                }
        );
        thread.start();


    }
}
