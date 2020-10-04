package com.vincent.govermentcomplaintapp.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.vincent.govermentcomplaintapp.MainActivity;
import com.vincent.govermentcomplaintapp.R;
import com.vincent.govermentcomplaintapp.adapters.ViewPagerAdapter;
import com.vincent.govermentcomplaintapp.adapters.ReviewRoomAdapter;
import com.vincent.govermentcomplaintapp.models.Admin;
import com.vincent.govermentcomplaintapp.models.ReviewRoom;
import com.vincent.govermentcomplaintapp.models.User;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ComplaintProgress extends AppCompatActivity {

    private static final String TAG = "ComplaintProgress";
    public static final String USER_TYPE = "userType";

    public static final String SERVICE_TYPE = "service";
    public static final String DESCRIPTION = "description";
    public static final String STREET_NAME = "streetName";
    public static final String CITY = "city";
    public static final String PROVINCE = "province";
    public static final String POST_CODE = "postCode";
    public static final String DATE = "date";
    public static final String IMAGES = "images";
    public static final String MUNICIPALITY_NAME = "municipality";
    public static final String COMPLAINT_ID = "complaint_id";


    private Fragment complaintReviewsFragment;
    private Fragment complaintDetailFragment;

    private ViewPager viewPager;
    private TabLayout tabLayout;
    private ViewPagerAdapter viewPagerAdapter;
    private ProgressBar progressBar;
    private TextView progressUpdate;

    //firebase vars
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private String currentUser;

    private Toolbar toolbar;

    private String serviceType;
    private String description;
    private String streetName;
    private String city;
    private String province;
    private String municipality;
    private String postCode;
    private String date;
    private ArrayList<String> imagesList;
    private ArrayList<ReviewRoom> reviewRooms = new ArrayList<>();
    private ReviewRoomAdapter reviewRoomAdapter;
    private RecyclerView reviewRoomRecyclerView;
    private Set<String> reviewRoomsId = new HashSet<>();
    private ReviewRoom mRreviewRoom;
    private String complaintId;

    private String userType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaint_progress);
        mRreviewRoom = new ReviewRoom();
        initViews();
        initToolbar();
        initFirebase();
        initVarsFromIntent();
        getUserData();


    }
    public void getUserData(){
        progressBar.setVisibility(View.VISIBLE);
        progressUpdate.setText("Loading...");
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
                                            progressBar.setVisibility(View.INVISIBLE);
                                            userType= "Users";
                                            getReviewsData();

                                            Log.d(TAG, "Complaint progress: get user data " + userType);

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
                                            progressBar.setVisibility(View.INVISIBLE);
                                            getReviewsData();
                                            Log.d(TAG, "MainActivity: " + userType);
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
                        Toast.makeText(ComplaintProgress.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        );

    }

    public void getReviewsData(){
        progressBar.setVisibility(View.VISIBLE);
        progressUpdate.setVisibility(View.VISIBLE);
        progressUpdate.setText("Loading...");

        firestore
                .collection("Reviewrooms")
                .document(currentUser)
                .get()
                .addOnSuccessListener(
                new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Log.d(TAG, "Success query ReviewRooms");
                        if(documentSnapshot.exists()){
                            Log.d(TAG, "document already exists");
                            getReviewRoom();
                        }else{
                            buildNewReviewRoom();
                        }
                    }
                }
        ).addOnFailureListener(
                new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Failed loading review room collection");
                        Toast.makeText(ComplaintProgress.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void buildNewReviewRoom() {

        Log.d(TAG, "Starting buildNewReviewRoom");
        progressBar.setVisibility(View.VISIBLE);
        progressUpdate.setVisibility(View.VISIBLE);
        progressUpdate.setText("Building review room...");

        mRreviewRoom = new ReviewRoom();

        mRreviewRoom.setMunicipailty(municipality);


        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .build();

        firestore.setFirestoreSettings(settings);

        DocumentReference reviewRoomRef = firestore
                .collection("Reviewrooms")
                .document(complaintId);

        mRreviewRoom.setReview_room_id(reviewRoomRef.getId());
        mRreviewRoom.setMunicipailty(municipality);

        reviewRoomRef.set(mRreviewRoom).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {
                    getReviewRoom();
                } else {
                    View parentLayout = findViewById(android.R.id.content);
                    Snackbar.make(parentLayout, "Something went wrong.", Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void getReviewRoom(){
        progressUpdate.setText("Getting review room...");

        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .build();

        firestore.setFirestoreSettings(settings);

        final DocumentReference documentReference = firestore
                .collection("Reviewrooms")
                .document(complaintId);

        documentReference.addSnapshotListener(
                new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@androidx.annotation.Nullable DocumentSnapshot documentSnapshot, @androidx.annotation.Nullable FirebaseFirestoreException error) {
                        if(documentSnapshot!=null){
                            if(documentSnapshot.exists()){
                                ReviewRoom reviewRoom = documentSnapshot.toObject(ReviewRoom.class);
                                if(!reviewRoomsId.contains(reviewRoom.getReview_room_id())){
                                    reviewRoomsId.add(reviewRoom.getReview_room_id());
                                    mRreviewRoom.setMunicipailty(municipality);
                                    mRreviewRoom.setReview_room_id(reviewRoom.getReview_room_id());
                                    reviewRooms.add(reviewRoom);
                                    Log.d(TAG, reviewRoom.getReview_room_id() + ": " + reviewRoom.getMunicipailty());
                                    initFragments();

                                    tabLayout.setupWithViewPager(viewPager);
                                    viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), 0);
                                    initViewPager();

                                    viewPager.setAdapter(viewPagerAdapter);
                                    viewPagerAdapter.notifyDataSetChanged();

                                    progressBar.setVisibility(View.INVISIBLE);
                                    progressUpdate.setVisibility(View.INVISIBLE);
                                }else{
                                    Log.d(TAG, "Contains reviewRooms");
                                    Log.d(TAG, "ReviewRoomsId: "+ reviewRoomsId);

                                }

                            }else{
                                Log.d(TAG, "Document dont exist");
                            }
                        }else{
                            Log.d(TAG, "Document is null");
                        }
                    }
                }
        );

    }



    public void initVarsFromIntent(){
        Intent intent = getIntent();
        imagesList = new ArrayList<>();
        serviceType = intent.getStringExtra("service");
        description = intent.getStringExtra("description");
        streetName = intent.getStringExtra("streetName");
        city = intent.getStringExtra("city");
        province = intent.getStringExtra("province");
        postCode = intent.getStringExtra("postCode");
        date = intent.getStringExtra("date");
        imagesList = intent.getStringArrayListExtra("images");
        municipality = intent.getStringExtra("municipality");
        complaintId = intent.getStringExtra("complaint_id");
    }

    public void initViewPager(){
        viewPagerAdapter.addFragment(complaintDetailFragment, "Details");
        viewPagerAdapter.addFragment(complaintReviewsFragment, "Reviews");

    }

    public void initFirebase(){
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser().getUid();
        firestore = FirebaseFirestore.getInstance();
    }

    public void initToolbar(){
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(SERVICE_TYPE);
    }

    public void initViews(){
        viewPager = (ViewPager) findViewById(R.id.compl_prog_view_pager);
        toolbar = (Toolbar) findViewById(R.id.compl_prog_toolbar);
        tabLayout = (TabLayout) findViewById(R.id.compl_prog_tablayout);
        progressBar = (ProgressBar) findViewById(R.id.progress_loading);
        progressUpdate = (TextView) findViewById(R.id.progress_loading_update);

    }

    public void initFragments(){
        Log.d(TAG, mRreviewRoom.getMunicipailty());
        Log.d(TAG, mRreviewRoom.getReview_room_id());
        complaintReviewsFragment = new ComplaintReviewsFragment(this, mRreviewRoom, userType, serviceType);
        complaintDetailFragment = new ComplaintDetailFragment(this, date, streetName, city, province, description, imagesList );

    }
}