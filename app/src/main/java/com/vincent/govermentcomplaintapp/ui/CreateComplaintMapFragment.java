package com.vincent.govermentcomplaintapp.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.vincent.govermentcomplaintapp.R;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static android.content.ContentValues.TAG;

public class CreateComplaintMapFragment extends Fragment  implements OnMapReadyCallback {

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private Boolean mLocationPermissionGranted = false;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 25f;

    //getting device location var
    private FusedLocationProviderClient fusedLocationProviderClient;

    private GoogleMap mMap;
    private Context mContext;
    private Activity mActivity;
    private View mView;


    public CreateComplaintMapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
        mActivity = getActivity();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_create_complaint_map, container, false);
        mView = view;
        getLocationPermission();

        return view;


    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        //ready to map
        Log.d(TAG, "Map is ready");
        mMap = googleMap;
        if (mLocationPermissionGranted) {
            getDeviceLocation();

            if (ActivityCompat.checkSelfPermission(mContext,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(mContext,
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
        if(ContextCompat.checkSelfPermission(mContext.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            //fine location permission granted
            if(ContextCompat.checkSelfPermission(mContext.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                //course permission granted
                //all permissions granted
                mLocationPermissionGranted = true;
                initMap();
            }
            else{
                //ask for permission
                ActivityCompat.requestPermissions(
                        mActivity,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE
                );
            }
        }
        else{
            //ask for permission
            ActivityCompat.requestPermissions(
                    mActivity,
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
        Log.d(TAG, "Getting device location: ");


        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(mContext);
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
                                    try {
                                        moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }


                                } else {
                                    Log.d(TAG, "cannot get current location");
                                    Toast.makeText(mContext, "unable to find current location", Toast.LENGTH_SHORT).show();
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
        geocoder = new Geocoder(mContext, Locale.getDefault());

        addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

        String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
        String city = addresses.get(0).getLocality();
        String state = addresses.get(0).getAdminArea();
        String country = addresses.get(0).getCountryName();
        String postalCode = addresses.get(0).getPostalCode();
        String knownName = addresses.get(0).getFeatureName();

        Log.d(TAG, address);
        Log.d(TAG, city);
        Log.d(TAG, country);
        Log.d(TAG, postalCode);
        Log.d(TAG, knownName);



    }

    private void initMap(){
        Log.d(TAG, "Initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync((OnMapReadyCallback) mContext);

    }
}