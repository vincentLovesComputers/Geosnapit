package com.vincent.govermentcomplaintapp.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.vincent.govermentcomplaintapp.R;
import com.vincent.govermentcomplaintapp.adapters.NotificationsAdapter;
import com.vincent.govermentcomplaintapp.models.UserNotification;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class NotificationsActivity extends AppCompatActivity {

    private static final String TAG = "NotificationsActivity";

    private Toolbar toolbar;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private String currentUser;
    private ArrayList<UserNotification> userNotificationsList = new ArrayList<>();
    private NotificationsAdapter notificationsAdapter;
    private RecyclerView notificationRecycler;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
        initViews();
        initFirebase();
        initToolBar();
        initRecyclerAndAdapter();
        getUser();

        if(userNotificationsList.size() == 0){
          //  noNotiView.setVisibility(View.VISIBLE);
        }else{
        }
    }

    public void getUser(){
        progressBar.setVisibility(View.VISIBLE);
        firestore
                .collection("Users")
                .document(currentUser)
                .get()
                .addOnSuccessListener(
                        new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
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
                                                               if(doc_change.getType() == DocumentChange.Type.ADDED){

                                                                   UserNotification userNotification = doc_change.getDocument().toObject(UserNotification.class);
                                                                   userNotificationsList.add(userNotification);
                                                                   notificationsAdapter.notifyDataSetChanged();
                                                               }
                                                           }

                                                       }else{
                                                           Log.d(TAG, "Querysnapshot is null");
                                                       }

                                                       progressBar.setVisibility(View.INVISIBLE);
                                                   }
                                               }
                                       ).addOnFailureListener(
                                        new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(NotificationsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                );
                            }
                        }
                ).addOnFailureListener(
                new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(NotificationsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Notifications get user error: " + e.getMessage());
                    }
                }
        );
    }

    private void initRecyclerAndAdapter(){
        notificationsAdapter = new NotificationsAdapter(this, userNotificationsList);
        notificationRecycler = findViewById(R.id.notifications_recycler);
        LinearLayoutManager lm = new LinearLayoutManager(this);
        notificationRecycler.setLayoutManager(lm);
        notificationRecycler.setAdapter(notificationsAdapter);

    }

    private void initFirebase(){
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        currentUser = auth.getCurrentUser().getUid();
    }

    private void initToolBar(){
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.notifications_activity_title));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void initViews(){
        toolbar = (Toolbar) findViewById(R.id.notifications_toolbar);
        progressBar = (ProgressBar) findViewById(R.id.notifications_loading);
    }
}