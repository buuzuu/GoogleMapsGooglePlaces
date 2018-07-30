package com.example.hritik.googlemapsgoogleplaces;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.GeoDataApi;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import Models.PlaceInfo;


public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "MapActivity";
    private GoogleMap mMap;
    public static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    public static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private Boolean mLocationPermissionGranted = false;
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private FusedLocationProviderClient fusedLocationProviderClient;
    public static final float DEFAULT_ZOOM = 15F;
    public static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(new LatLng(-40, -148), new LatLng(71, 136));
    private PlaceAutocompleteAdapter placeAutocompleteAdapter;
    private AutoCompleteTextView mSearchText;
    private ImageView mGps,mInfo,mPlacePicker;
    private PlaceInfo mPlace;
    public static final int PLACE_PICKER_REQUEST = 1;
    public Marker mMarker;
    private GeoDataClient mGeoDataClient;
    private Location currentLOcation;
    private GoogleApiClient mGoogleApiClient;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mSearchText = findViewById(R.id.input_search);
        mGps = findViewById(R.id.id_gps);
        mInfo=findViewById(R.id.place_info);
        mPlacePicker=findViewById(R.id.place_picker);

        getLocationPermission();
        getDevceLOcation();


    }

    private void inIt() {

        Log.d(TAG, "inIt: Initializig...");

        mGeoDataClient = Places.getGeoDataClient(this, null);
        mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(Places.GEO_DATA_API).addApi(Places.PLACE_DETECTION_API).enableAutoManage(this, this)
                .build();

        mSearchText.setOnItemClickListener(mAutocompleteClickListener);
        placeAutocompleteAdapter = new PlaceAutocompleteAdapter(this, mGeoDataClient, LAT_LNG_BOUNDS, null);

        mSearchText.setAdapter(placeAutocompleteAdapter);


        mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER) {
                    // execute methods for searching

                    geoLocate();
                }


                return false;
            }
        });
        mGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.d(TAG, "onClick: Clicked GPS icon");
                getDevceLOcation();
            }
        });
        mInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MapActivity.this, "Clicked", Toast.LENGTH_SHORT).show();

                Log.d(TAG, "onClick: clicked plce info ");

                try{
                    if (mMarker.isInfoWindowShown()){
                        mMarker.hideInfoWindow();
                    }else {
                        Log.d(TAG, "onClick: place info"+ mPlace.toString());
                        mMarker.showInfoWindow();
                    }



                }catch (NullPointerException e){
                    Log.d(TAG, "onClick: "+e.getMessage());
                }


            }
        });

        mPlacePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

                try {
                    startActivityForResult(builder.build(MapActivity.this), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }


            }
        });
        hideSoftKeyBoard();

    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);
//                String toastMsg = String.format("Place: %s", place.getName());
//                Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
                PendingResult<PlaceBuffer> placeBufferPendingResult = Places.GeoDataApi.getPlaceById(mGoogleApiClient, place.getId());
                placeBufferPendingResult.setResultCallback(mUpdatePlaceDetailsCallback);



           }
        }
    }

    private void getDevceLOcation() {

        Log.d(TAG, "getDeviceLocation: Getting device current location");
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {

            if (mLocationPermissionGranted) {

                Task location = fusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {

                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: Found Location");
                            currentLOcation = (Location) task.getResult();
                            MoveCamera(new LatLng(currentLOcation.getLatitude(), currentLOcation.getLongitude())
                                    , DEFAULT_ZOOM,
                                    "My Location");




                        } else {
                            Toast.makeText(MapActivity.this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "onComplete: Current location is null");
                        }


                    }
                });


            }


        } catch (SecurityException s) {
            Log.d(TAG, "getDevceLOcation: Securitu Exception :" + s.getMessage());
        }


    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this, "Map is Ready.", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onMapReady: Map is ready.");
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        if (mLocationPermissionGranted) {
            getDevceLOcation();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling

                return;
            }
            mMap.setMyLocationEnabled(true);

            //   mMap.getUiSettings().setMyLocationButtonEnabled(false);


            inIt();

        }


    }

    private void MoveCamera(LatLng latLng, float zoom, String title) {

        Log.d(TAG, "MoveCamera: MOving the camera to:" + latLng.latitude + "--" + latLng.longitude);
        //   mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        if (!title.equals("My Location")) {

            MarkerOptions options = new MarkerOptions()
                    .position(latLng)
                    .title(title);

            mMap.addMarker(options);
        }

        hideSoftKeyBoard();


    }

    private void MoveCamera(LatLng latLng, float zoom, PlaceInfo placeInfo) {

        Log.d(TAG, "MoveCamera: MOving the camera to:" + latLng.latitude + "--" + latLng.longitude);
        //   mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        mMap.clear();
        mMap.setInfoWindowAdapter(new CustomInfoAdapter(MapActivity.this));

        if (placeInfo != null){
                try{
                    String snippet ="Address: "+placeInfo.getAddress() +"\n" +
                    "Phone Number: "+placeInfo.getPhoneNumber() +"\n" +
                    "Website: "+placeInfo.getWebsiteUri() +"\n" +
                   "Price Rating: "+placeInfo.getRatings() +"\n" ;


                    MarkerOptions markerOptions=new MarkerOptions().position(latLng).title(placeInfo.getName())
                            .snippet(snippet);

                    mMarker=mMap.addMarker(markerOptions);



                }catch (NullPointerException e){
                    Log.d(TAG, "MoveCamera: "+e.getMessage());
                }


        }else {
                    mMap.addMarker(new MarkerOptions().position(latLng));
        }

            hideSoftKeyBoard();


    }


    private void geoLocate() {
        Log.d(TAG, "geoLocate: GeoLocating....");
        String search = mSearchText.getText().toString();
        Geocoder geocoder = new Geocoder(MapActivity.this);

        List<Address> list = new ArrayList<>();

        try {

            list = geocoder.getFromLocationName(search, 1);

        } catch (IOException e) {
            Log.d(TAG, "geoLocate: IOException" + e.getMessage());
        }

        if (list.size() > 0) {
            Address address = list.get(0);
            //Toast.makeText(this, address.toString(), Toast.LENGTH_SHORT).show();
            Log.d(TAG, "geoLocate: Location Found" + address.toString());


            MoveCamera(new LatLng(address.getLatitude(), address.getLongitude()), DEFAULT_ZOOM, address.getAddressLine(0));


        }


    }

    private void inItMap() {
        Log.d(TAG, "inItMap: Initializing Map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapActivity.this);
        getDevceLOcation();



    }

    private void getLocationPermission() {
        Log.d(TAG, "getLocationPermission: GettingLocation Permission");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this, FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            if (ContextCompat.checkSelfPermission(this, COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
                inItMap();

            } else {
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
            }


        } else {
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }


    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermissionsResult: Called");
        mLocationPermissionGranted = false;
        switch (requestCode) {

            case LOCATION_PERMISSION_REQUEST_CODE: {

                if (grantResults.length > 0) {

                    for (int i = 0; i < grantResults.length; i++) {

                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionGranted = false;

                            Log.d(TAG, "onRequestPermissionsResult: Permission Denied");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: Permission Granted");

                    mLocationPermissionGranted = true;
                    // inilialize our map
                    inItMap();
                }


            }


        }


    }

    private void hideSoftKeyBoard() {

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    /*
    ........................Google Places Api AutoComplete Suggestion......................
     */

    private AdapterView.OnItemClickListener mAutocompleteClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            hideSoftKeyBoard();
            final AutocompletePrediction item = placeAutocompleteAdapter.getItem(i);
            final String placeID = item.getPlaceId();
            PendingResult<PlaceBuffer> placeBufferPendingResult = Places.GeoDataApi.getPlaceById(mGoogleApiClient, placeID);
            placeBufferPendingResult.setResultCallback(mUpdatePlaceDetailsCallback);
        }
    };
    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(@NonNull PlaceBuffer places) {

            if (!places.getStatus().isSuccess()) {
                Log.d(TAG, "onResult: Place query did not complete sucessfull---" + places.getStatus().toString());
                places.release();
                return;
            }
            final Place place = places.get(0);


            try {
//                mPlace=new PlaceInfo(place.getName().toString(),place.getAttributions().toString(),place.getAddress().toString()
//                        ,place.getPhoneNumber().toString(),place.getId()
//                        ,place.getWebsiteUri(),place.getLatLng(),place.getRating());

                mPlace = new PlaceInfo();
                mPlace.setName(place.getName().toString());
                Log.d(TAG, "onResult: " + place.getName());
                mPlace.setAddress(place.getAddress().toString());
                Log.d(TAG, "onResult: " + place.getAddress());
                mPlace.setAttributes(place.getAttributions().toString());
                Log.d(TAG, "onResult: " + place.getAttributions());
                mPlace.setPhoneNumber(place.getPhoneNumber().toString());
                Log.d(TAG, "onResult: " + place.getPhoneNumber());
                mPlace.setId(place.getId());
                Log.d(TAG, "onResult: " + place.getId());
                mPlace.setWebsiteUri(place.getWebsiteUri());
                Log.d(TAG, "onResult: " + place.getWebsiteUri());
                mPlace.setLatLng(place.getLatLng());
                Log.d(TAG, "onResult: " + place.getLatLng());
                mPlace.setRatings(place.getRating());
                Log.d(TAG, "onResult: " + place.getRating());

                Log.d(TAG, "onResult: " + mPlace.toString());

            } catch (NullPointerException e) {
                Log.d(TAG, "onResult: " + e.getMessage());
            }

            MoveCamera(new LatLng(place.getViewport().getCenter().latitude, place.getViewport().getCenter().longitude), DEFAULT_ZOOM, mPlace);

            Location locationA = new Location("point A");

            locationA.setLatitude(currentLOcation.getLatitude());
            locationA.setLongitude(currentLOcation.getLongitude());

            Location locationB = new Location("point B");

            locationB.setLatitude(place.getViewport().getCenter().latitude);
            locationB.setLongitude(place.getViewport().getCenter().longitude);

            float distance = locationA.distanceTo(locationB);


            Log.d(TAG, "onResult: Distance is "+(distance/1000));
            Toast.makeText(MapActivity.this, String.valueOf(String.format("%.2f", (distance/1000)))+" Km", Toast.LENGTH_SHORT).show();

            places.release();


        }
    };


}
