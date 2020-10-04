package com.vincent.govermentcomplaintapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.vincent.govermentcomplaintapp.MainActivity;
import com.vincent.govermentcomplaintapp.R;
import com.vincent.govermentcomplaintapp.models.Admin;
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

public class AdminProfileSetup extends AppCompatActivity implements OnMapReadyCallback {

    //google mapping
    private static final String TAG = "ProfileSetupActivity";
    public static final String USER = "userType";
    public static final String REASON_VISITING_PAGE = "reason";
    private List<Municipality> municipalityList;

    //vars
    private CircleImageView adminProfilePicImg;
    private TextInputEditText adminNameView;


    private Uri mainImageUri;
    private boolean isChanged = false;

    //firebase vars
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private String currentAdmin;
    private ProgressBar progressBar;
    StorageReference storageReference;


    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private Boolean mLocationPermissionGranted = false;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 18f;

    private GoogleMap mMap;
    private TextView adminMuniView;
    private TextView adminCityView;
    private TextView adminProvinceNameView;
    private TextView adminPostCode;
    private Button yesBtn;
    private Button noBtn;
    private ProgressBar adminProfileProgressBar;
    private MaterialCardView adminAddressCard;
    private TextInputLayout adminRoleLayout;
    private TextInputEditText adminRoleView;
    private TextInputLayout adminNameInputLayout;


    private TextInputEditText adminNew_muniView;
    private TextInputEditText adminNew_cityView;
    private TextInputEditText adminNew_provinceNameView;
    private TextInputEditText adminNewPostCode;
    private Button adminSaveProfile;

    private TextInputLayout adminNew_cityViewLayout;
    private TextInputLayout adminNew_muniViewLayout;
    private TextInputLayout adminNew_provinceNameViewLayout;
    private TextInputLayout adminNew_postCodeLayout;

    private TextView hintProfilRed;
    private Button adminEditBtn;

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

    //case admin
    private String roleInput;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_profile_setup);
        Intent intent = getIntent();
        userType = intent.getStringExtra("userType");
        String reasonVisitingPage = intent.getStringExtra("reason");
        municipalityList = new ArrayList<>();
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        currentAdmin = auth.getUid();
        storageReference = FirebaseStorage.getInstance().getReference();

        if(savedInstanceState != null){
            userType = savedInstanceState.getString("userType", "Admin");
        }

        refToViews();
        profilePicSetup();
        getLocationPermission();


        if(reasonVisitingPage!=null){
            if(!reasonVisitingPage.matches("")){
                if(reasonVisitingPage.matches("new_admin")){
                    Log.d(TAG, "New admin profile setup");
                }else if(reasonVisitingPage.matches("profile_edit")){
                    progressBar.setVisibility(View.VISIBLE);
                    noBtn.setVisibility(View.INVISIBLE);
                    yesBtn.setVisibility(View.INVISIBLE);
                    adminEditBtn.setVisibility(View.VISIBLE);
                    adminAddressCard.setVisibility(View.VISIBLE);

                    adminSaveProfile.setVisibility(View.INVISIBLE);
                    Log.d(TAG, "Edit user profile");
                    getUserData();

                }else if(reasonVisitingPage.matches("profile_incomplete")){
                    Log.d(TAG, "Helper text reason for redirection to page: " + hintProfilRed);
                    hintProfilRed.setVisibility(View.VISIBLE);
                    hintProfilRed.setText("Profile incomplete");
                }else if(reasonVisitingPage.matches("error_saving_profile_data")){
                    Log.d(TAG, "Helper text redirection to page: " + reasonVisitingPage);
                    hintProfilRed.setVisibility(View.VISIBLE);
                    hintProfilRed.setText("Error saving profile, try again");
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
                        adminAddressCard.setVisibility(View.INVISIBLE);
                        adminNew_muniViewLayout.setVisibility(View.VISIBLE);
                        adminNew_cityViewLayout.setVisibility(View.VISIBLE);
                        adminNew_provinceNameViewLayout.setVisibility(View.VISIBLE);
                        adminSaveProfile.setVisibility(View.VISIBLE);
                        adminNew_postCodeLayout.setVisibility(View.VISIBLE);
                        saveNewAddress();
                    }
                }
        );
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences settings = getSharedPreferences("AdminSettings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor= settings.edit();
        editor.putString(USER, userType);
        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences settings = getSharedPreferences("AdminSettings", Context.MODE_PRIVATE);
        userType = settings.getString(USER, "Admin");
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
                                    Toast.makeText(AdminProfileSetup.this, "unable to find current location", Toast.LENGTH_SHORT).show();
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


        adminCityView.setText(city);
        adminPostCode.setText(postalCode);
        adminProvinceNameView.setText(state);

        String profile_city = adminCityView.getText().toString();
        String profile_province = adminProvinceNameView.getText().toString();

        if(!profile_city.equals("") && !profile_province.equals("")){
            Log.d(TAG, "location Fields:  are not empty");

            getUserData();

        }

        Log.d(TAG, city);
        Log.d(TAG, state);
        getMunicipality(adminProvinceNameView.getText().toString(), adminCityView.getText().toString());


    }

    private void getMunicipality(String db_muni, String db_city) {
        firestore
                .collection("Provinces")
                .document(db_muni)
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
                                                adminMuniView.setText(municipality.getName());
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
                                adminProfileProgressBar.setVisibility(View.INVISIBLE);

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
                .collection("Admin")
                .document(currentAdmin)
                .get()
                .addOnSuccessListener(
                        new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                Log.d(TAG, "Get user data: User data successfully found");

                                if(documentSnapshot!=null){
                                    if(documentSnapshot.exists()){
                                        Log.d(TAG, "Document exists");

                                        Admin admin = documentSnapshot.toObject(Admin.class);
                                        if(admin!=null){
                                            progressBar.setVisibility(View.INVISIBLE);
                                            String imageUri = admin.getImage();
                                            String name = admin.getName();
                                            String city = admin.getCity();
                                            String role = admin.getRole();
                                            String province = admin.getProvince();
                                            String municipality = admin.getMunicipality();
                                            String postCode = admin.getPostal_code();
                                            setUserUi(imageUri, name, city, province, municipality, postCode, role);
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
                        Toast.makeText(AdminProfileSetup.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    public void setUserUi(final String image, final String name, final String city, final String province, final String muni, final String postCode, String role){
        adminNameView.setText(name);
        adminProvinceNameView.setText(province);
        adminMuniView.setText(muni);
        adminCityView.setText(city);
        adminRoleView.setText(role);

        RequestOptions reqOpt = new RequestOptions();
        reqOpt.placeholder(getDrawable(R.drawable.profile_icon));
        Glide.with(this).setDefaultRequestOptions(reqOpt).load(image).into(adminProfilePicImg);

        adminEditBtn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        adminAddressCard.setVisibility(View.GONE);
                        adminNew_muniViewLayout.setVisibility(View.VISIBLE);
                        adminNew_cityViewLayout.setVisibility(View.VISIBLE);
                        adminNew_provinceNameViewLayout.setVisibility(View.VISIBLE);
                        adminNew_postCodeLayout.setVisibility(View.VISIBLE);
                        adminSaveProfile.setVisibility(View.VISIBLE);

                        if(!isChanged){
                            mainImageUri = Uri.parse(image);
                        }
                        adminNameView.setText(name);
                        adminNew_cityView.setText(city);
                        adminNew_provinceNameView.setText(province);
                        adminNew_muniView.setText(muni);
                        adminPostCode.setText(postCode);
                        adminSaveProfile.setOnClickListener(
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
        adminProfilePicImg.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (ContextCompat.checkSelfPermission(
                                    AdminProfileSetup.this,
                                    Manifest.permission.READ_EXTERNAL_STORAGE
                            ) != PackageManager.PERMISSION_GRANTED) {
                                //permission not granted
                                Log.d(TAG, "Permission to pic image not given");
                                Log.d(TAG, "requesting permission");

                                ActivityCompat.requestPermissions(
                                        AdminProfileSetup.this,
                                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                        1
                                );


                            } else {
                                ActivityCompat.requestPermissions(
                                        AdminProfileSetup.this,
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
                .start(AdminProfileSetup.this)


        ;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mainImageUri = result.getUri();
                adminProfilePicImg.setImageURI(mainImageUri);
                isChanged = true;
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                String error = result.getError().getMessage();
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }

        }
    }

    public void saveNewAddress(){
        adminSaveProfile.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        progressBar.setVisibility(View.VISIBLE);
                        new_name = adminNameView.getText().toString();
                        new_city = adminCityView.getText().toString();
                        new_muni = adminMuniView.getText().toString();
                        new_prov = adminProvinceNameView.getText().toString();
                        new_post_code = adminPostCode.getText().toString();
                        roleInput = adminRoleView.getText().toString();

                        if(!new_name.matches("") && !new_city.matches("") && !new_muni.matches("") && !new_prov.matches("") && !roleInput.matches("")){
                            Log.d(TAG, "NEW USER DATA: Not  empty");
                            redToSplashScreen(new_name, new_city, new_prov,new_post_code, new_muni, roleInput);
                            progressBar.setVisibility(View.INVISIBLE);
                        }else{
                            if(new_city.matches("")){
                                adminNew_cityViewLayout.setError("City cannot be empty");
                            }else if(new_prov.matches("")){
                                adminNew_provinceNameViewLayout.setError("Province cannot be empty");

                            }else if(new_post_code.matches("")){
                                adminNew_postCodeLayout.setError("Postal code cannot be empty");

                            }else if(new_muni.matches("")){
                                adminNew_muniViewLayout.setError("Municipality name cannot be empty");

                            }else if(roleInput.matches("")){
                                adminRoleLayout.setError("Role cannot be empty");
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
                        adminProfileProgressBar.setVisibility(View.INVISIBLE);
                        progressBar.setVisibility(View.VISIBLE);

                        nameInput = adminNameView.getText().toString();
                        cityData = adminCityView.getText().toString();
                        provinceData = adminProvinceNameView.getText().toString();
                        postCodeData = adminPostCode.getText().toString();
                        municipality = adminMuniView.getText().toString();
                        roleInput = adminRoleView.getText().toString();

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

                            redToSplashScreen(nameInput, cityData, provinceData, postCodeData, municipality, roleInput);

                        } else{

                        }
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                }

        );

    }

    public void redToSplashScreen(String name, String city, String province, String postCode, String municipality, String role){
        Intent intent = new Intent(AdminProfileSetup.this, AdminLogginInSplashScreen.class);
        intent.putExtra(AdminLogginInSplashScreen.NAME_INPUT, name);
        intent.putExtra(AdminLogginInSplashScreen.IMAGE_URI, mainImageUri);
        intent.putExtra(AdminLogginInSplashScreen.ADMIN_ID, currentAdmin);
        intent.putExtra(AdminLogginInSplashScreen.CITY, city);
        intent.putExtra(AdminLogginInSplashScreen.PROVINCE, province);
        intent.putExtra(AdminLogginInSplashScreen.MUNICIPALITY, municipality);
        intent.putExtra(AdminLogginInSplashScreen.POST_CODE, postCode);
        intent.putExtra(AdminLogginInSplashScreen.ROLE, role);
        intent.putExtra(AdminLogginInSplashScreen.IS_CHANGED, isChanged);
        startActivity(intent);

    }




    public void redToHome() {
        Intent intent = new Intent(AdminProfileSetup.this, MainActivity.class);
        intent.putExtra(MainActivity.USER, userType);
        startActivity(intent);
        finish();
    }

    public void refToViews() {
        adminProfilePicImg = (CircleImageView) findViewById(R.id.admin_profile_pic);
        adminNameView = (TextInputEditText) findViewById(R.id.admin_setup_name);
        adminNameInputLayout = (TextInputLayout) findViewById(R.id.admin_setup_field_layout);
        adminMuniView= (TextView) findViewById(R.id.admin_profile_municipality);
        adminCityView = (TextView) findViewById(R.id.admin_profile_city);
        adminProvinceNameView = (TextView) findViewById(R.id.admin_profile_province);
        adminPostCode = (TextView) findViewById(R.id.admin_profile_post_code);
        yesBtn= (Button) findViewById(R.id.admin_yes_addreess_btn);
        noBtn= (Button) findViewById(R.id.admin_no_addreess_btn);
        progressBar = (ProgressBar) findViewById(R.id.admin_setup_prog_bar);
        adminProfileProgressBar = (ProgressBar) findViewById(R.id.admin_profile_progress_bar);
        hintProfilRed = (TextView) findViewById(R.id.admin_profile_hint_redirection);



        adminNew_muniView = (TextInputEditText) findViewById(R.id.admin_new_profile_muni_text);
        adminNew_cityView = (TextInputEditText) findViewById(R.id.admin_new_profile_city_name);
        adminNew_provinceNameView= (TextInputEditText) findViewById(R.id.admin_new_profile_province);
        adminSaveProfile =  (Button) findViewById(R.id.admin_new_profile_submit);
        adminNewPostCode = (TextInputEditText) findViewById(R.id.admin_new_profile_post_code);
        adminAddressCard = (MaterialCardView) findViewById(R.id.admin_materialCardView);

        adminNew_muniViewLayout = (TextInputLayout) findViewById(R.id.admin_new_profile_muni_layout);
        adminNew_cityViewLayout = (TextInputLayout) findViewById(R.id.admin_new_profile_city_layout);
        adminNew_provinceNameViewLayout = (TextInputLayout) findViewById(R.id.admin_new_profile_province_name_layout);
        adminNew_postCodeLayout =  (TextInputLayout) findViewById(R.id.admin_new_profile_postal_code_layout);

        adminRoleLayout = (TextInputLayout) findViewById(R.id.profile_admin_role_layout);
        adminRoleView = (TextInputEditText) findViewById(R.id.profile_admin_role);

        adminEditBtn = (Button) findViewById(R.id.admin_edit_profile);


    }


}