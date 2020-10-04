package com.vincent.govermentcomplaintapp.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.vincent.govermentcomplaintapp.AdminLogginInSplashScreen;
import com.vincent.govermentcomplaintapp.AdminProfileSetup;
import com.vincent.govermentcomplaintapp.MainActivity;
import com.vincent.govermentcomplaintapp.R;
import com.vincent.govermentcomplaintapp.models.Municipality;
import com.vincent.govermentcomplaintapp.models.User;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileSetupActivity extends AppCompatActivity implements OnMapReadyCallback {

    //google mapping
    private static final String TAG = "ProfileSetupActivity";
    public static final String USER = "userType";
    public static final String REASON_VISITING_PAGE = "reason";
    private List<Municipality> municipalityList;

    //vars
    private CircleImageView profilePicImg;
    private TextInputEditText nameView;


    private Uri mainImageUri;
    private boolean isChanged = false;

    //firebase vars
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private String currentUser;
    private ProgressBar progressBar;
    StorageReference storageReference;


    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private Boolean mLocationPermissionGranted = false;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 18f;

    private GoogleMap mMap;
    private TextView muniView;
    private TextView cityView;
    private TextView provinceNameView;
    private TextView postCode;
    private Button yesBtn;
    private Button noBtn;
    private ProgressBar profileProgressBar;
    private MaterialCardView addressCard;
    private TextInputLayout roleLayout;
    private TextInputEditText roleView;
    private TextInputLayout nameInputLayout;

    private TextInputEditText new_muniView;
    private TextInputEditText new_cityView;
    private TextInputEditText new_provinceNameView;
    private TextInputEditText newPostCode;
    private Button saveProfile;

    private TextInputLayout new_cityViewLayout;
    private TextInputLayout new_muniViewLayout;
    private TextInputLayout new_provinceNameViewLayout;
    private TextInputLayout new_postCodeLayout;

    private TextView hintProfilRed;
    private Button editBtn;

    private String userType;


    //var to save to db
    private String nameInput;
    private String cityData;
    private String provinceData;
    private String postCodeData;
    private String municipality;

    //edited vars to save to db
    private String new_name;
    private String new_city;
    private String new_muni;
    private String new_prov;
    private String new_post_code;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setup);
        Intent intent = getIntent();
        userType = intent.getStringExtra("userType");
        String reasonVisitingPage = intent.getStringExtra("reason");
        municipalityList = new ArrayList<>();
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        currentUser = auth.getUid();
        storageReference = FirebaseStorage.getInstance().getReference();

        refToViews();
        profilePicSetup();
        getLocationPermission();

        if(reasonVisitingPage!=null){
            if(!reasonVisitingPage.matches("")){
                if(reasonVisitingPage.matches("new_user")){
                    Log.d(TAG, "New user profile setup");
                }else if(reasonVisitingPage.matches("profile_edit")){
                    progressBar.setVisibility(View.VISIBLE);
                    noBtn.setVisibility(View.INVISIBLE);
                    yesBtn.setVisibility(View.INVISIBLE);
                    editBtn.setVisibility(View.VISIBLE);
                    addressCard.setVisibility(View.VISIBLE);

                    saveProfile.setVisibility(View.INVISIBLE);
                    Log.d(TAG, "Edit user profile");
                    getUserData();
                }else if(reasonVisitingPage.matches("profile_incomplete")){
                    Log.d(TAG, "Helper text reason for redirection to page: " + hintProfilRed);
                    hintProfilRed.setVisibility(View.VISIBLE);
                    hintProfilRed.setText("Profile incomplete");
                }
            }
        }
        else{
            Log.d(TAG, "Profile setup reason visiting page is null");
        }

        saveProfile();
        noBtn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addressCard.setVisibility(View.INVISIBLE);
                        new_muniViewLayout.setVisibility(View.VISIBLE);
                        new_cityViewLayout.setVisibility(View.VISIBLE);
                        new_provinceNameViewLayout.setVisibility(View.VISIBLE);
                        new_postCodeLayout.setVisibility(View.VISIBLE);
                        saveProfile.setVisibility(View.VISIBLE);
                        saveNewAddress();
                    }
                }
        );

    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        //ready to map
        Log.d(TAG, "Map is ready");
        mMap = googleMap;
        if (mLocationPermissionGranted) {
            getDeviceLocation();

            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                return;
            }
            mMap.setMyLocationEnabled(true);

            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            mMap.getUiSettings().setIndoorLevelPickerEnabled(true);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestionPermission called:  ");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mLocationPermissionGranted = false;
        switch (requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE:{
                if(grantResults.length>0 ){

                    for(int i=0; i < grantResults.length; i++){
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            Log.d(TAG, "Permission failed");

                            mLocationPermissionGranted = false;
                            return;
                        }
                    }
                    Log.d(TAG, "Permission granted");
                    mLocationPermissionGranted = true;

                    //initialize map if permissions are granted
                    initMap();


                }
            }
        }
    }

    //verify google services
    private void getLocationPermission(){
        Log.d(TAG, "Getting location permissions");

        //store permissions inside a list
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };

        //check if locations have been checked
        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            //fine location permission granted
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                //course permission granted
                //all permissions granted

                initMap();
                mLocationPermissionGranted = true;
            }
            else{
                //ask for permission
                ActivityCompat.requestPermissions(
                        this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE
                );
            }
        }
        else{
            //ask for permission
            ActivityCompat.requestPermissions(
                    this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE
            );
        }
    }


    public void getDeviceLocation() {
        progressBar.setVisibility(View.VISIBLE);
        Log.d(TAG, "Getting device location: ");

        //getting device location var
        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try {

            if (mLocationPermissionGranted) {
                final Task location = fusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(
                        new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task) {
                                if (task.isSuccessful()) {

                                    Location currentLocation = (Location) task.getResult();
                                    //move the camera
                                    progressBar.setVisibility(View.INVISIBLE);


                                    if(currentLocation!=null){
                                        Log.d(TAG, "Current location found: \n" + "Latitude: " + currentLocation.getLatitude() + "\n Longitude: " + currentLocation.getLongitude());
                                        try {
                                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }else{
                                        Log.d(TAG, "Getting current location: " + "null");
                                    }


                                } else {
                                    Log.d(TAG, "cannot get current location");
                                    Toast.makeText(ProfileSetupActivity.this, "unable to find current location", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                );
            }

        } catch (SecurityException e) {
            Log.d(TAG, "error getting client location: " + e.getMessage());

        }
    }

    public void moveCamera(LatLng latLng, float zoom) throws IOException {
        Log.d(TAG, "moving the camera to: " + latLng.latitude + ", longitude: " + latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

        String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
        String city = addresses.get(0).getLocality();
        String state = addresses.get(0).getAdminArea();
        String postalCode = addresses.get(0).getPostalCode();
        String featureName = addresses.get(0).getFeatureName();
        String subLocality = addresses.get(0).getSubLocality();


        cityView.setText(city);
        postCode.setText(postalCode);
        provinceNameView.setText(state);

        String profile_city = cityView.getText().toString();
        String profile_province = provinceNameView.getText().toString();

        if(!profile_city.equals("") && !profile_province.equals("")){
            Log.d(TAG, "location Fields:  are not empty");
            getUserData();
            getMunicipality(profile_province, profile_city);
        }


        Log.d(TAG, city);
        Log.d(TAG, state);

    }

    private void getMunicipality(String db_province, String db_city) {
        firestore
                .collection("Provinces")
                .document(db_province)
                .collection("Municipalities")
                .whereArrayContains("cities", db_city)
                .get()
                .addOnSuccessListener(
                        new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot querySnapshot) {

                                if (querySnapshot != null) {
                                    if (!querySnapshot.getDocuments().isEmpty()) {
                                        for (DocumentChange doc_change : querySnapshot.getDocumentChanges()) {
                                            if (doc_change.getType() == DocumentChange.Type.ADDED) {
                                                Municipality municipality = doc_change.getDocument().toObject(Municipality.class);
                                                municipalityList.add(municipality);
                                                Log.d(TAG, "Municipality: " + municipality.getName());
                                                muniView.setText(municipality.getName());
                                                if (municipalityList.size() == 0) {
                                                    Log.d(TAG, "Municipality not found");
                                                }

                                            }

                                        }


                                    } else {
                                        Log.d(TAG, "Municipality: Document is empty");
                                    }

                                } else {
                                    Log.d(TAG, "Municipality: Document is null");
                                }
                                profileProgressBar.setVisibility(View.INVISIBLE);

                            }
                        });
    }



    private void initMap(){
        Log.d(TAG, "Initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.profile_map);

        assert mapFragment != null;
        mapFragment.getMapAsync(this);

    }

    private void getUserData(){
        progressBar.setVisibility(View.VISIBLE);

        Log.d(TAG, "Get user data");
        firestore
                .collection("Users")
                .document(currentUser)
                .get()
                .addOnSuccessListener(
                        new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                Log.d(TAG, "Get user data: User data successfully found");

                               if(documentSnapshot!=null){
                                   if(documentSnapshot.exists()){
                                       Log.d(TAG, "Document exists");

                                       User user = documentSnapshot.toObject(User.class);
                                       if(user!=null){
                                           progressBar.setVisibility(View.INVISIBLE);
                                           String imageUri = user.getImage();
                                           String name = user.getName();
                                           String city = user.getCity();
                                           String province = user.getProvince();
                                           String municipality = user.getMunicipality();
                                           String postCode = user.getPostal_code();
                                           setUserUi(imageUri, name, city, province, municipality, postCode);
                                       }else{
                                           Log.d(TAG, "User is null");
                                       }

                                   }else{
                                       Log.d(TAG, "Document doesn't exist");
                                   }
                               }else{
                                   Log.d(TAG, "Document snapshot is null");
                               }
                                progressBar.setVisibility(View.INVISIBLE);

                            }
                        }
                ).addOnFailureListener(
                new OnFailureListener() {

                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.INVISIBLE);
                        Log.d(TAG, "Getting user data error");
                        Toast.makeText(ProfileSetupActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    public void setUserUi(final String image, final String name, final String city, final String province, final String muni, final String postCode){
        nameView.setText(name);
        provinceNameView.setText(province);
        muniView.setText(muni);
        cityView.setText(city);

        RequestOptions reqOpt = new RequestOptions();
        reqOpt.placeholder(getDrawable(R.drawable.profile_icon));
        Glide.with(this).setDefaultRequestOptions(reqOpt).load(image).into(profilePicImg);

        editBtn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addressCard.setVisibility(View.GONE);
                        new_muniViewLayout.setVisibility(View.VISIBLE);
                        new_cityViewLayout.setVisibility(View.VISIBLE);
                        new_provinceNameViewLayout.setVisibility(View.VISIBLE);
                        new_postCodeLayout.setVisibility(View.VISIBLE);
                        saveProfile.setVisibility(View.VISIBLE);

                        if(!isChanged){
                            mainImageUri = Uri.parse(image);
                        }
                        new_muniView.setText(name);
                        new_cityView.setText(city);
                        new_provinceNameView.setText(province);
                        new_muniView.setText(muni);
                        newPostCode.setText(postCode);
                        saveProfile.setOnClickListener(
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        progressBar.setVisibility(View.VISIBLE);
                                        Log.d(TAG, "Edited profile: Save edited profile");
                                        saveNewAddress();
                                    }
                                }
                        );

                    }
                }
        );
    }

    public void profilePicSetup() {
        profilePicImg.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (ContextCompat.checkSelfPermission(
                                    ProfileSetupActivity.this,
                                    Manifest.permission.READ_EXTERNAL_STORAGE
                            ) != PackageManager.PERMISSION_GRANTED) {
                                //permission not granted
                                Log.d(TAG, "Permission to pic image not given");
                                Log.d(TAG, "requesting permission");

                                ActivityCompat.requestPermissions(
                                        ProfileSetupActivity.this,
                                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                        1
                                );


                            } else {
                                ActivityCompat.requestPermissions(
                                        ProfileSetupActivity.this,
                                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                        1
                                );
                                imagePicker();
                            }
                        } else {
                            imagePicker();
                        }
                    }
                }
        );
    }

    public void imagePicker() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(ProfileSetupActivity.this)
        ;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mainImageUri = result.getUri();
                profilePicImg.setImageURI(mainImageUri);
                isChanged = true;
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                String error = result.getError().getMessage();
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }

        }
    }

    public void saveNewAddress(){
        saveProfile.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        progressBar.setVisibility(View.VISIBLE);
                        new_name = nameView.getText().toString();
                        new_city = new_cityView.getText().toString();
                        new_muni = new_muniView.getText().toString();
                        new_prov = new_provinceNameView.getText().toString();
                        new_post_code = newPostCode.getText().toString();

                        if(!new_name.matches("") && !new_city.matches("") && !new_muni.matches("") && !new_prov.matches("")){
                            Log.d(TAG, "NEW USER DATA: Not  empty");
                            redToSplashScreen(new_name, new_city, new_prov,new_post_code, new_muni);
                            progressBar.setVisibility(View.INVISIBLE);
                        }else{
                            if(new_city.matches("")){
                                new_cityViewLayout.setError("City cannot be empty");
                            }else if(new_prov.matches("")){
                                new_provinceNameViewLayout.setError("Province cannot be empty");

                            }else if(new_post_code.matches("")){
                                new_postCodeLayout.setError("Postal code cannot be empty");

                            }else if(new_muni.matches("")){
                                new_muniViewLayout.setError("Municipality name cannot be empty");

                            }

                        }
                    }
                }
        );

    }

    public void saveProfile() {

        yesBtn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                           Log.d(TAG, "Profile setup: Image changed");
                           profileProgressBar.setVisibility(View.INVISIBLE);
                           progressBar.setVisibility(View.VISIBLE);

                           nameInput = nameView.getText().toString();
                           cityData = cityView.getText().toString();
                           provinceData = provinceNameView.getText().toString();
                           postCodeData = postCode.getText().toString();
                           municipality = muniView.getText().toString();

                           if (!nameInput.matches("")
                                   && mainImageUri != null
                                   && !cityData.matches("")
                                   && !provinceData.matches("")
                                   && !municipality.matches("")
                                   && !postCodeData.matches("")
                           ) {
                               Log.d(TAG, "User data: " + "Inputs valid");
                               progressBar.setVisibility(View.VISIBLE);
                               Log.d(TAG, "saving data to database");
                               redToSplashScreen(nameInput, cityData, provinceData, postCodeData, municipality);

                           } else {
                               if(nameInput.matches("")){
                                   nameInputLayout.setError("Name field cannot be empty");
                               }else if(
                                       cityData.matches("")
                                       && provinceData.matches("")
                                       && municipality.matches("")
                                       && postCodeData.matches("")
                               ){
                                   Toast.makeText(ProfileSetupActivity.this, "Address fields are empty", Toast.LENGTH_SHORT).show();
                                   addressCard.setVisibility(View.INVISIBLE);
                                   new_muniViewLayout.setVisibility(View.VISIBLE);
                                   new_cityViewLayout.setVisibility(View.VISIBLE);
                                   new_provinceNameViewLayout.setVisibility(View.VISIBLE);
                                   new_postCodeLayout.setVisibility(View.VISIBLE);
                                   saveProfile.setVisibility(View.VISIBLE);
                                   saveNewAddress();
                               }

                           }

                       progressBar.setVisibility(View.INVISIBLE);
                       }
                    }

        );

    }


    public void redToSplashScreen(String name, String city, String province, String postCode, String municipality){
        Intent intent = new Intent(ProfileSetupActivity.this, RegisterSplashScreenActvity.class);
        intent.putExtra(RegisterSplashScreenActvity.NAME_INPUT, name);
        intent.putExtra(RegisterSplashScreenActvity.IMAGE_URI, mainImageUri);
        intent.putExtra(RegisterSplashScreenActvity.USER_ID, currentUser);
        intent.putExtra(RegisterSplashScreenActvity.CITY, city);
        intent.putExtra(RegisterSplashScreenActvity.PROVINCE, province);
        intent.putExtra(RegisterSplashScreenActvity.MUNICIPALITY, municipality);
        intent.putExtra(RegisterSplashScreenActvity.POST_CODE, postCode);
        intent.putExtra(RegisterSplashScreenActvity.IS_CHANGED, isChanged);
        startActivity(intent);
    }


    public void refToViews() {
        profilePicImg = (CircleImageView) findViewById(R.id.profile_pic);
        nameView = (TextInputEditText) findViewById(R.id.setup_name);
        nameInputLayout = (TextInputLayout) findViewById(R.id.setup_field_layout);
        muniView= (TextView) findViewById(R.id.profile_municipality);
        cityView = (TextView) findViewById(R.id.profile_city);
        provinceNameView = (TextView) findViewById(R.id.profile_province);
        postCode = (TextView) findViewById(R.id.profile_post_code);
        yesBtn= (Button) findViewById(R.id.yes_addreess_btn);
        noBtn= (Button) findViewById(R.id.no_addreess_btn);
        progressBar = (ProgressBar) findViewById(R.id.setup_prog_bar);
        profileProgressBar = (ProgressBar) findViewById(R.id.profile_progress_bar);
        hintProfilRed = (TextView) findViewById(R.id.profile_hint_redirection);

      new_muniView = (TextInputEditText) findViewById(R.id.profile_muni_text);
      new_cityView = (TextInputEditText) findViewById(R.id.profile_city_name);
      new_provinceNameView = (TextInputEditText) findViewById(R.id.profile_province_text);
      saveProfile =  (Button) findViewById(R.id.profile_submit);
      newPostCode = (TextInputEditText) findViewById(R.id.profile_postal_code);
        addressCard = (MaterialCardView) findViewById(R.id.materialCardView);

        new_muniViewLayout = (TextInputLayout) findViewById(R.id.profile_muni_layout);
        new_cityViewLayout = (TextInputLayout) findViewById(R.id.profile_city_layout);
        new_provinceNameViewLayout = (TextInputLayout) findViewById(R.id.profile_province_name);
        new_postCodeLayout =  (TextInputLayout) findViewById(R.id.profile_postal_code_layout);

        editBtn = (Button) findViewById(R.id.edit_profile);


    }

}