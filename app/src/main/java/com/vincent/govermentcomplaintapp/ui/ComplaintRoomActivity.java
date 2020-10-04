package com.vincent.govermentcomplaintapp.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.vincent.govermentcomplaintapp.AdminProfileSetup;
import com.vincent.govermentcomplaintapp.MainActivity;
import com.vincent.govermentcomplaintapp.R;
import com.vincent.govermentcomplaintapp.adapters.ComplaintAdapter;
import com.vincent.govermentcomplaintapp.models.Admin;
import com.vincent.govermentcomplaintapp.models.Complaint;
import com.vincent.govermentcomplaintapp.models.User;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

public class ComplaintRoomActivity extends AppCompatActivity {
    public static final String USER = "userType";
    public static final String TAG  = "ComplaintRoomActivity";

    private Toolbar toolbar;
    public static final String SERVICE_TYPE = "service";
    private String serviceType;

    private ComplaintAdapter complaintAdapter;
    private RecyclerView complaintsRecycler;
    private TextView noComplaintsView;
    private ProgressBar progressBar;
    private TextView complaintUpdateView;

    //firebase vars
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private String currentUser;

    private List<Complaint> complaintList;
    private Query firstQuery;
    private Query lastQuery;
    private DocumentSnapshot lastVisible;

    private String currentUserPostalCode;
    private String user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaint_room);

        noComplaintsView =findViewById(R.id.no_complaints);
        complaintUpdateView = findViewById(R.id.complaints_loading_update);
        progressBar = findViewById(R.id.complaints_loading);

        complaintList = new ArrayList<>();


        getIntentData();

        initToolbar();
        initFirebase();
        getUserData();


    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.add_complaint_icon:
                redToCreateNewComplaint();
                return true;

            default:
                return false;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_complaint, menu);
        return true;
    }

    public void getUserData(){

        complaintUpdateView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        complaintUpdateView.setText("loading...");

        firestore
                .collection("Users")
                .document(currentUser)
                .get()
                .addOnSuccessListener(
                        new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if(documentSnapshot!=null) {
                                    if (documentSnapshot.get("user_type") != null) {
                                        user = "Users";
                                        Log.d(TAG, "MainActivity: " + "user logged in");
                                        User user = documentSnapshot.toObject(User.class);
                                        if(user!=null){
                                            currentUserPostalCode = user.getPostal_code();
                                            if(!serviceType.matches("")){
                                                initRecyclerAndAdapter();
                                                getComplaints();

                                            }
                                        }else{
                                        }

                                    }else{
                                        user = "Admin";
                                        getAdminData();
                                        progressBar.setVisibility(View.INVISIBLE);
                                        complaintUpdateView.setVisibility(View.INVISIBLE);
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
                                            currentUserPostalCode = admin.getPostal_code();
                                            if(!serviceType.matches("")){
                                                initRecyclerAndAdapter();
                                                getComplaints();
                                            }

                                        }else{
                                            Log.d(TAG, "Get admin data: Profile incomplete");
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
                        Toast.makeText(ComplaintRoomActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        );

    }


    public void redToComplaintProgress(String description, String date,String streetName, String city, String province, String postCode,  ArrayList<String> images, String municipality, String complaint_id){

        Intent intent = new Intent(ComplaintRoomActivity.this, ComplaintProgress.class);
        intent.putExtra(ComplaintProgress.SERVICE_TYPE, serviceType);
        intent.putExtra(ComplaintProgress.DESCRIPTION, description);
        intent.putExtra(ComplaintProgress.DATE, date);
        intent.putExtra(ComplaintProgress.STREET_NAME, streetName);
        intent.putExtra(ComplaintProgress.CITY, city);
        intent.putExtra(ComplaintProgress.PROVINCE, province);
        intent.putExtra(ComplaintProgress.POST_CODE, postCode);
        intent.putExtra(ComplaintProgress.MUNICIPALITY_NAME, municipality);
        intent.putExtra(ComplaintProgress.COMPLAINT_ID, complaint_id);
        intent.putStringArrayListExtra(ComplaintProgress.IMAGES, images);
        intent.putExtra(ComplaintProgress.USER_TYPE, user);

        startActivity(intent);


    }

    public void getComplaints(){

        complaintUpdateView.setText("loading issues from your area");
        if(complaintsRecycler!=null){
            complaintsRecycler.addOnScrollListener(
                    new RecyclerView.OnScrollListener() {
                        @Override
                        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                            super.onScrolled(recyclerView, dx, dy);
                            boolean reachedBottom = !recyclerView.canScrollVertically(1);
                            if(reachedBottom){
                                loadMoreComplaints();
                            }
                        }
                    }
            );

        }
        loadComplaints();

    }

    public void loadComplaints() {

        firestore
                .collection("Services")
                .document(serviceType)
                .get()
                .addOnSuccessListener(this,
                        new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {

                                if (documentSnapshot.exists()) {

                                    firstQuery = documentSnapshot
                                            .getReference()
                                            .collection("Complaints")
                                            .whereEqualTo("post_code", currentUserPostalCode)
                                            .orderBy("timeStamp", Query.Direction.DESCENDING)
                                            .limit(5);
                                    Log.d(TAG, "currentUserPostalCode: " + currentUserPostalCode);

                                    firstQuery.addSnapshotListener(ComplaintRoomActivity.this,
                                            new EventListener<QuerySnapshot>() {
                                                @Override
                                                public void onEvent(@Nullable QuerySnapshot querySnapshot, @Nullable FirebaseFirestoreException error) {
                                                    if (querySnapshot != null) {
                                                        if (!querySnapshot.getDocuments().isEmpty()) {
                                                            lastVisible = querySnapshot.getDocuments().get(querySnapshot.size() - 1);
                                                        } else {
                                                            Log.d(TAG, "Getting first complaints: Documents empty");
                                                            progressBar.setVisibility(View.INVISIBLE);
                                                            complaintUpdateView.setVisibility(View.INVISIBLE);
                                                            noComplaintsView.setVisibility(View.VISIBLE);

                                                        }

                                                        for (DocumentChange doc_change : querySnapshot.getDocumentChanges()) {
                                                            if (doc_change.getType() == DocumentChange.Type.ADDED) {
                                                                Complaint complaint = doc_change.getDocument().toObject(Complaint.class);
                                                                complaintList.add(complaint);
                                                                progressBar.setVisibility(View.INVISIBLE);
                                                                complaintUpdateView.setVisibility(View.INVISIBLE);
                                                                complaintAdapter.notifyDataSetChanged();
                                                            }

                                                        }
                                                    } else {
                                                        Log.d(TAG, "Getting first complaints: Documents null");
                                                    }
                                                }


                                            });
                                }
                            }
                        });
    }


    public void loadMoreComplaints(){
        Log.d(TAG, "Load more complaints: inside function" );
        firestore
                .collection("Services")
                .document(serviceType)
                .get()
                .addOnSuccessListener(ComplaintRoomActivity.this,
                        new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if(documentSnapshot.exists()){
                                    lastQuery = documentSnapshot
                                            .getReference()
                                            .collection("Complaints")
                                            .orderBy("timeStamp", Query.Direction.DESCENDING)
                                            .startAfter(lastVisible)
                                            .limit(5);

                                    lastQuery.addSnapshotListener(
                                            new EventListener<QuerySnapshot>() {
                                                @Override
                                                public void onEvent(@Nullable QuerySnapshot querySnapshot, @Nullable FirebaseFirestoreException error) {
                                                    if(querySnapshot!=null){
                                                        if(!querySnapshot.getDocuments().isEmpty()){
                                                            lastVisible = querySnapshot.getDocuments().get(querySnapshot.size() - 1);
                                                        }else{
                                                            Log.d(TAG, "Getting first complaints: Documents empty");
                                                        }

                                                        for(DocumentChange doc_change: querySnapshot.getDocumentChanges()){
                                                            if(doc_change.getType() == DocumentChange.Type.ADDED){

                                                                Complaint complaint = doc_change.getDocument().toObject(Complaint.class);
                                                                complaintList.add(complaint);
                                                                complaintAdapter.notifyDataSetChanged();
                                                            }


                                                        }
                                                    }else{
                                                        Log.d(TAG, "Getting first complaints: Documents null");
                                                    }
                                                }
                                            }
                                    );
                                }
                            }
                        }
                );



    }


    public void getIntentData(){
        Intent intent = getIntent();
        serviceType = intent.getStringExtra("service");
        user = intent.getStringExtra("userType");
    }

    public void redToProfileSetup(){
        Toast.makeText(ComplaintRoomActivity.this, "Postal code not set", Toast.LENGTH_SHORT).show();
        Intent intent= new Intent(ComplaintRoomActivity.this, ProfileSetupActivity.class);
        intent.putExtra(ProfileSetupActivity.USER, "User");
        intent.putExtra(ProfileSetupActivity.REASON_VISITING_PAGE, "edit");
        startActivity(intent);
        finish();
    }
    public void redToAdminProfileSetup(){
        Toast.makeText(ComplaintRoomActivity.this, "Postal code not set", Toast.LENGTH_SHORT).show();
        Intent intent= new Intent(ComplaintRoomActivity.this, AdminProfileSetup.class);
        intent.putExtra(AdminProfileSetup.USER, "Admin");
        intent.putExtra(AdminProfileSetup.REASON_VISITING_PAGE, "edit");
        startActivity(intent);
        finish();
    }

    public void redToCreateNewComplaint(){
        Intent intent = new Intent(ComplaintRoomActivity.this, CreateComplaint.class);
        intent.putExtra(CreateComplaint.SERVICE_TYPE, serviceType);
        startActivity(intent);
    }


    public void initToolbar(){

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(serviceType!=null){
            getSupportActionBar().setTitle(serviceType);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    public void initRecyclerAndAdapter(){
        complaintsRecycler = (RecyclerView) findViewById(R.id.complaint_services_recycler);
        complaintAdapter = new ComplaintAdapter(this, complaintList, this, user, serviceType);
        LinearLayoutManager lm = new LinearLayoutManager(this);
        complaintsRecycler.setLayoutManager(lm);
        complaintsRecycler.setAdapter(complaintAdapter);
    }

    public void initFirebase(){
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getUid();
    }
}