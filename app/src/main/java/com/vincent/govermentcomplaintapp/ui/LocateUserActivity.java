package com.vincent.govermentcomplaintapp.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.vincent.govermentcomplaintapp.R;
import com.vincent.govermentcomplaintapp.models.Municipality;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class LocateUserActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final String SERVICE_TYPE = "service";

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private Boolean mLocationPermissionGranted = false;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 18f;

    //getting device location var
    private FusedLocationProviderClient fusedLocationProviderClient;

    private GoogleMap mMap;
    //views
    private TextView muniView;
    private TextView cityView;
    private TextView streetNameView;
    private TextView provinceNameView;
    private TextView postCode;
    private Button confirmBtn;
    private ProgressBar progressBar;

    private FirebaseAuth auth;
    private String currentUser;
    private FirebaseFirestore firestore;

    private List<Municipality> municipalityList;
    private String serviceType;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locate_user);
        municipalityList = new ArrayList<>();
        getLocationPermission();
        initViews();
        initFirebase();

        //get service type from prev act
        Intent intent = getIntent();
        serviceType = intent.getStringExtra("service");

        //confirm Btn clicked
        confirmBtn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        progressBar.setVisibility(View.VISIBLE);
                        saveData();
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                }
        );


    }

    public void getMunicipality(){
        progressBar.setVisibility(View.VISIBLE);
        if(!cityView.getText().toString().matches("")){
            firestore
                    .collection("Provinces")
                    .document(provinceNameView.getText().toString())
                    .collection("Municipalities")
                    .whereArrayContains("cities",cityView.getText().toString())
                    .get()
                    .addOnSuccessListener(
                            new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot querySnapshot) {
                                    progressBar.setVisibility(View.INVISIBLE);
                                    if(querySnapshot!=null){
                                        if(!querySnapshot.getDocuments().isEmpty()){

                                            Toast.makeText(LocateUserActivity.this, "document found", Toast.LENGTH_SHORT).show();
                                            for(DocumentChange doc_change: querySnapshot.getDocumentChanges()){
                                                if(doc_change.getType() == DocumentChange.Type.ADDED){
                                                    Municipality municipality = doc_change.getDocument().toObject(Municipality.class);
                                                    municipalityList.add(municipality);

                                                }

                                            }
                                            muniView.setText(municipalityList.get(0).getName());

                                            Log.d(TAG, muniView.getText().toString());

                                        }else{
                                            Log.d(TAG, "Document is empty");
                                        }

                                    }else{
                                        Log.d(TAG, "Document is null");
                                    }
                                    progressBar.setVisibility(View.INVISIBLE);
                                }
                            }
                    );

        }


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
                mLocationPermissionGranted = true;
                initMap();
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

    public void getDeviceLocation() {
        progressBar.setVisibility(View.VISIBLE);
        Log.d(TAG, "Getting device location: ");


        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
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
                                    try {
                                        moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }


                                } else {
                                    Log.d(TAG, "cannot get current location");
                                    Toast.makeText(LocateUserActivity.this, "unable to find current location", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                );
            }

        } catch (SecurityException e) {
            Log.d(TAG, "error getting client location: " + e.getMessage());

        }
    }

    //method to move camera
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


        String[] values = address.split(",");
        String strNamePlaceHolder = values[0] + ", " + values[1];
        streetNameView.setText(strNamePlaceHolder);
        cityView.setText(city);
        postCode.setText(postalCode);
        provinceNameView.setText(state);
        getMunicipality();


        Log.d(TAG, featureName);
        Log.d(TAG, subLocality);


    }

    public void saveData(){
        Map<String, Object> data = new HashMap<>();
        data.put("street_name", streetNameView.getText().toString());
        data.put("city", cityView.getText().toString());
        data.put("post_code", postCode.getText().toString());
        data.put("province", provinceNameView.getText().toString());
        data.put("municipality", muniView.getText().toString());
        data.put("user", currentUser);

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
                                    Toast.makeText(LocateUserActivity.this, "Complaint uploaded", Toast.LENGTH_SHORT).show();
                                    redToComplaints(serviceType);
                                }else{
                                    String error = task.getException().getMessage();
                                    Log.d(TAG, "SaveData: Error saving to firestore: " + error);
                                    Toast.makeText(LocateUserActivity.this, error, Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                );
    }

    public void redToComplaints(String serviceType){
        Intent intent = new Intent(LocateUserActivity.this, ComplaintRoomActivity.class);
        intent.putExtra(ComplaintRoomActivity.SERVICE_TYPE, serviceType);
        startActivity(intent);
    }

    private void initMap(){
        Log.d(TAG, "Initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

    }

    public void initFirebase(){
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUser  = auth.getUid();
    }

    private void initViews(){
        muniView= (TextView) findViewById(R.id.municipality);
        cityView = (TextView) findViewById(R.id.city);
        streetNameView = (TextView) findViewById(R.id.street_name_address);
        provinceNameView = (TextView) findViewById(R.id.province);
        postCode = (TextView) findViewById(R.id.post_code);
        confirmBtn= (Button) findViewById(R.id.confirm_data);
        progressBar = (ProgressBar) findViewById(R.id.locate_user_prog_bar);
    }
}