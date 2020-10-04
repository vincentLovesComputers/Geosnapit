package com.vincent.govermentcomplaintapp.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.vincent.govermentcomplaintapp.R;
import com.vincent.govermentcomplaintapp.SpacesItemDecoration;
import com.vincent.govermentcomplaintapp.adapters.ImagesAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CreateComplaint extends AppCompatActivity  {

    //error user gets when they dont have correct version of google services
    private static final int ERROR_DIALOG_REQUEST = 9001;

    private static final String TAG = "CreateComplaint";

    public static final String SERVICE_TYPE = "service";

    private ArrayList<Uri> imagesList;
    private Uri mobileFileUri;
    private String mDownloadedStorgeUri;
    Context mContext;
    private Toolbar toolbar;
    private Uri mainImageUri;
    private boolean isChanged = false;

    private TextInputEditText titleView;
    private TextInputEditText descriptionView;
    private Button submitBtn;

    private ImagesAdapter imagesAdapter;
    private RecyclerView recyclerView;

    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private String currentUser;
    private StorageReference storageReference;
    private Bitmap compressedImageBitmap;
    private String serviceType;
    private TextView textAsst;
    private ImageView placeHolderImg;
    private ProgressBar progressBar;

    private TextInputLayout titleLayout;
    private TextInputLayout descriptionLayout;
    private TextView pickImage;

    //store list of images in array
    ArrayList<String> images = new ArrayList<>();
    ArrayList<String> thumb_images = new ArrayList<>();
    Map<String, Object> data = new HashMap<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        setContentView(R.layout.activity_create_complaint);
        initViews();
        serviceType = intent.getStringExtra("service");

        imagesList = new ArrayList<>();

        initToolbar();
        initRecyclerAndAdapter();
        initFirebase();

        submitBtnClicked();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_create_complaint_details_fragment, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch  (item.getItemId()){
            case R.id.add_complaint_image:
                if(imagesList.size()!=4){
                    setImages();
                }
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mainImageUri = result.getUri();
                if(imagesList.size() != 4){
                    pickImage.setVisibility(View.INVISIBLE);
                    imagesList.add(mainImageUri);
                }
                else{
                    pickImage.setVisibility(View.INVISIBLE);
                    Toast.makeText(mContext, "Maximum number of images reached", Toast.LENGTH_SHORT).show();

                }
                imagesAdapter.notifyDataSetChanged();
                isChanged = true;
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                String error = result.getError().getMessage();
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }

        }
    }


    public void submitBtnClicked(){
        submitBtn.setOnClickListener(

                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        progressBar.setVisibility(View.VISIBLE);
                        final String title = titleView.getText().toString();
                        final String description = descriptionView.getText().toString();

                        if(!title.matches("") && !description.matches("")){
                            Log.d(TAG, "Submit button clicked: valid inputs");

                            saveToDb(title, description);

                        }else{
                            Log.d(TAG, "Submit button clicked: invalid inputs");
                            progressBar.setVisibility(View.INVISIBLE);

                            if(title.matches("")){
                                titleLayout.setError("Enter a title for your complaint");
                            }else if(description.matches("")){
                                descriptionLayout.setError("Describe your complaint");
                            }
                        }

                    }
                }
        );

    }

    public void saveToDb(final String title, final String desciption) {


        for (int i = 0; i < imagesList.size(); i++) {

            mobileFileUri = imagesList.get(i);
            Log.d(TAG, "Image uri" + String.valueOf(i));

            final String randomString = UUID.randomUUID().toString();

            final StorageReference imagePath = storageReference
                    .child("complaints")
                    .child(title)
                    .child(randomString + mobileFileUri + ".jpg");

            imagePath.putFile(mobileFileUri).addOnSuccessListener(
                    new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //successfully put file in storage

                                taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(
                                            new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {

                                                    if(uri!=null){
                                                        saveToFirestore(uri.toString(), title, desciption);
                                                    }else{
                                                        Toast.makeText(mContext, "Uri is null", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            }
                                    );
                                }


                    }
            );


        }
    }



    private void saveToFirestore(String mDownloadedStorgeUri, String title, String description){

        images.add(mDownloadedStorgeUri);

        data.put("title", title);
        data.put("description", description);
        data.put("images", images);
        data.put("timeStamp", FieldValue.serverTimestamp());
        data.put("issue_update", "no");

        firestore
                .collection("Services/" + serviceType + "/Complaints")
                .document(currentUser)
                .get()
                .addOnSuccessListener(
                        new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if(documentSnapshot.exists()){
                                    firestore
                                    .collection("Services/" + serviceType + "/Complaints")
                                    .document(currentUser)
                                    .update(data)
                                            .addOnCompleteListener(
                                                    new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if(task.isSuccessful()){
                                                                Log.d(TAG, "SaveData: Service data successful");

                                                            if(isServiceOK()){
                                                                redToLocate();
                                                            }
                                                            }else{
                                                                String error = task.getException().getMessage();
                                                                Log.d(TAG, "SaveData: Error saving to firestore: " + error);
                                                                Toast.makeText(CreateComplaint.this, error, Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    }
                                            );
                                }
                                else{
                                    firestore
                                            .collection("Services/" + serviceType + "/Complaints")
                                            .document(currentUser)
                                            .set(data)
                                            .addOnCompleteListener(
                                                    new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if(task.isSuccessful()){
                                                                Log.d(TAG, "SaveData: Service data successful");

                                                                if(isServiceOK()){
                                                                    progressBar.setVisibility(View.INVISIBLE);
                                                                    redToLocate();
                                                                }
                                                            }else{
                                                                String error = task.getException().getMessage();
                                                                Log.d(TAG, "SaveData: Error saving to firestore: " + error);
                                                                Toast.makeText(CreateComplaint.this, error, Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    }
                                            );
                                }
                            }
                        }
                );


    }

    public void redToLocate(){

        Intent intent = new Intent(CreateComplaint.this, LocateUserActivity.class);
        intent.putExtra(LocateUserActivity.SERVICE_TYPE, serviceType);
        startActivity(intent);
        finish();


    }
    //check version of google services
    public boolean isServiceOK(){
        Log.d(TAG, "isServiceOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(CreateComplaint.this);
        if(available == ConnectionResult.SUCCESS){
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServiceOK: Google Play services is working");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error occured but we can resolve it
            Log.d(TAG, "isServiceOK: an error ocurred but we can fix it" );
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(CreateComplaint.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }else{
            Toast.makeText(this, "We can't make map requests ", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    public void setImages() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED) {
                //permission not granted
                Log.d(TAG, "Permission to pic image not given");
                Log.d(TAG, "requesting permission");

                ActivityCompat.requestPermissions(this
                        ,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        1
                );

            } else {
                imagePicker();
            }
        } else {
            imagePicker();
        }
    }

    public void imagePicker() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(this);
    }

    public void initViews(){
        titleView = (TextInputEditText) findViewById(R.id.edit_complaint_title);
        descriptionView = (TextInputEditText) findViewById(R.id.edit_complaint_description);
        submitBtn = (Button) findViewById(R.id.edit_submit_complaint);
        progressBar = (ProgressBar) findViewById(R.id.compl_prog_bar);
        titleLayout = (TextInputLayout) findViewById(R.id.edit_complaint_title_layout);
        descriptionLayout = (TextInputLayout) findViewById(R.id.edit_complaint_description_layout);
        pickImage = (TextView) findViewById(R.id.pick_image);


    }

    public void initRecyclerAndAdapter(){
        recyclerView = (RecyclerView) findViewById(R.id.create_complaint_images_recycler_view);
        imagesAdapter = new ImagesAdapter(mContext, imagesList);
        GridLayoutManager gm = new GridLayoutManager(mContext, 2);
        recyclerView.setLayoutManager(gm);
        recyclerView.setAdapter(imagesAdapter);
        recyclerView.addItemDecoration(new SpacesItemDecoration(10));
    }

    public void initFirebase(){
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getUid();
        storageReference = FirebaseStorage.getInstance().getReference();
    }

    public void initToolbar(){
        toolbar = (Toolbar) findViewById(R.id.complaint_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }



}