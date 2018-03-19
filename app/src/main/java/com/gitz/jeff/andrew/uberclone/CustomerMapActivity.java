package com.gitz.jeff.andrew.uberclone;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class CustomerMapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    GoogleApiClient googleApiClient;
    Location lastLocation;
    LocationRequest locationRequest;
    TinyDB getSavedUserPhoneNumber; //Get user Driver User Phone Number to act as ID
    String customerUserId = null;
    Button request;   //Request for a Taxi
    Button cancel;    //Cancel Taxi Request
    private LatLng pickUpLocation;   //Will Hold Pick Up Location Co-ordinates
    private Boolean driverFound = true;
    String destination;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(DriverMapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }

        else
        {
            mapFragment.getMapAsync(this);
        }
        getSavedUserPhoneNumber = new TinyDB(getBaseContext());
        customerUserId = getSavedUserPhoneNumber.getString("userPhoneNumber");  //Get Saved Phone Number to act as User ID
        request = (Button)findViewById(R.id.requestUber);
        cancel = (Button)findViewById(R.id.cancelRequest);
        cancel.setVisibility(View.INVISIBLE);


        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener()
        {
            @Override
            public void onPlaceSelected(Place place)  //Get Info on the Selected Place
            {
               destination = place.getName().toString();
            }

            @Override
            public void onError(Status status)   //Handle the Error
            {
                destination = place.getName().toString();
            }
        });
    }


    @Override
    public void onMapReady(GoogleMap googleMap)   //Notifies when map is ready for use
    {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {

            return;
        }

        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);
    }


    protected synchronized void buildGoogleApiClient()
    {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }


    @Override
    public void onLocationChanged(Location location)  //Will be called every second
    {
        lastLocation = location;
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));

        Double currentLatitudeAddress = location.getLatitude();     //Get current Latitude coordinates
        Double currentLongitudeAddress = location.getLongitude();   //Get current Longitude coordinates
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);    //Refresh rate
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);  //Highest Accuracy, However drains a lot of battery power

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {

            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    public void requestUber(View view)
    {
       if(lastLocation!= null && lastLocation!= null)
       {
           pickUpLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());   //Get Customer Pickup Location Co-ordinates
           mMap.addMarker(new MarkerOptions().position(pickUpLocation).title("Pick Me Up Here"));  //Add Marker, and Set Title of Marker
       }
       request.setText("Getting you a Driver...");

       if(driverFound) //If Appropriate Driver Has Been Found
       {
           /*
           Location customerLocation = new Location("");
           if(customerLocation != null)
           {
               customerLocation.setLatitude(pickUpLocation.latitude);
               customerLocation.setLongitude(pickUpLocation.longitude);
           }


           Location driverLocation = new Location("");  //Use Customer Location Till Driver Location Provided By Server System
           if(driverLocation != null)
           {
               driverLocation.setLatitude(pickUpLocation.latitude);
               driverLocation.setLongitude(pickUpLocation.longitude);
           }
           */

           //float distanceBtwnCustomerAndDriver = customerLocation.distanceTo(driverLocation);         //Distance in Metres
           float distanceBtwnCustomerAndDriver = 500;  //Use Dummy Data for the Time Being
           if(distanceBtwnCustomerAndDriver < 100)  //If Distance Btwn Driver and Customer is less than 100m
           {
               request.setText("Driver has Arrived");
           }

           else                                     //If Distance Btwn Driver and Customer is more than 100m
           {
               request.setText("Driver Found: " + String.valueOf(distanceBtwnCustomerAndDriver) + "m");  //Change Button Appropriately
           }

           cancel.setVisibility(View.VISIBLE);
       }

    }

    public void cancelRequest(View view)
    {
        if(driverFound)  //If Cancel Button Pressed while Driver has Already been Found
        {
            //sendUserData.sendUserID(getBaseContext(), "customerRequestCancelled", customerUserId);     //Send Driver ID that End of Activity
            request.setText("Cancel Request Successful");
        }
    }

    public  void appSettings(View view)
    {
      Intent intent = new Intent(getBaseContext(), customerSettingsActivity.class);
      startActivity(intent);
      return;
    }

    public void logOut(View view)
    {
        //sendUserData.sendDriverID(getBaseContext(), driverUserId);     //Send Driver ID that End of Activity
        Intent intent = new Intent(getBaseContext(), registerActivity.class);
        startActivity(intent);     //Go to the Main Activity after Logout
        finish();
        return;
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    final int LOCATION_REQUEST_CODE = 1;
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case LOCATION_REQUEST_CODE:
            {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                            .findFragmentById(R.id.map);
                    mapFragment.getMapAsync(this);
                }

                else
                {
                    Toast.makeText(getBaseContext(), "Please Provide Permission", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

    @Override
    protected void onStop()  //If Driver gets out of this activity, Notify Db, for Him to be removed as Hes is no longer Active
    {
        super.onStop();
        //sendUserData.sendDriverID(getBaseContext(), driverUserId);     //Send Driver ID that End of Activity
    }
}
