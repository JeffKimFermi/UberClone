package com.gitz.jeff.andrew.uberclone;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

public class CustomerMapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener, RoutingListener {

    private GoogleMap mMap;
    GoogleApiClient googleApiClient;
    Location lastLocation;
    Location currentLocation;
    public static float myZoomLevel = 14;
    LocationRequest locationRequest;
    TinyDB getSavedUserPhoneNumber; //Get user Driver User Phone Number to act as ID
    String customerUserId = null;
    Button request;   //Request for a Taxi
    Button cancelRequest;    //Cancel Taxi Request
    public LatLng pickUpLocation;   //Will Hold Pick Up Location Co-ordinates
    String pickUpPoint;  //Select where to be Picked
    String destination;     //Select your Destination
    Marker markerPickUp;    //Pickup Location Marker
    Marker markerDestination;  //Destination Marker
    Marker markerCurrentLocation; //My Current Locaton Marker
    public LatLng latlngDestinationCoordinates;    //Longitude Latitude coordates of your destination
    public LatLng latlngPickUpLocationCoordinates;  //Longitude Latitude co-ordinates of your preferred Pickup Location
    private List<Polyline> polylines;
    //private static final int[] COLORS = new int[]{R.color.primary_dark,R.color.primary,R.color.primary_light,R.color.accent,R.color.primary_dark_material_light};
    private static final int[] COLORS = new int[]{R.color.primary,};
    private PlaceAutocompleteFragment autocompleteFragmentPickup;
    private PlaceAutocompleteFragment autocompleteFragmentDestination;
    LinearLayout driverInformation;
    ImageView driverProfileImage;   //Assigned Customer Profile Image
    TextView driverCar, driverName, driverPhoneNumber;  //Assigned Customer Name and Phone Number
    public boolean driverAssigned = false;   //Driver has been successfully assigned
    public boolean driverFound = true;
    ImageView callDriver;
    private static final int MY_PERMISSIONS_REQUEST_ACCOUNTS = 1;
    final int LOCATION_REQUEST_CODE = 1;
    boolean locationDataCopied = false;
    TinyDB savedUserPhoneNumber;
    String userPhoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        savedUserPhoneNumber = new TinyDB(getBaseContext());
        userPhoneNumber = savedUserPhoneNumber.getString("userPhoneNumber");
        polylines = new ArrayList<>();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(CustomerMapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }

        else
        {
            mapFragment.getMapAsync(this);
        }

        driverInformation = (LinearLayout)findViewById(R.id.driverInfo);
        driverProfileImage = (ImageView)findViewById(R.id.driverProfileImage);
        driverName = (TextView)findViewById(R.id.driverName);
        driverPhoneNumber = (TextView)findViewById(R.id.driverPhoneNumber);
        driverCar = (TextView)findViewById(R.id.driverCar);

        getSavedUserPhoneNumber = new TinyDB(getBaseContext());
        customerUserId = getSavedUserPhoneNumber.getString("userPhoneNumber");  //Get Saved Phone Number to act as User ID
        request = (Button)findViewById(R.id.requestUber);
        cancelRequest = (Button)findViewById(R.id.cancelRequest);
        cancelRequest.setVisibility(View.INVISIBLE);
        callDriver = (ImageView) findViewById(R.id.callDriver);

        getAssignedDriver();   //Display Driver Details

        callDriver.setOnClickListener(new View.OnClickListener()    //Call Customer Listener
        {
            @Override
            public void onClick(View v) {
                String eventID = "customerCalledDriver";
                String customerNumber = "0735555255";
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + customerNumber));

                if (ActivityCompat.checkSelfPermission(CustomerMapActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED)
                {

                    return;
                }
                startActivity(callIntent);
                sendUserData.sendEventData(getBaseContext(), userPhoneNumber, eventID,  null, null, null);
            }
        });

        autocompleteFragmentPickup = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment_pickup);
        autocompleteFragmentPickup.setHint("Choose Pick Up Point");

        autocompleteFragmentPickup.setOnPlaceSelectedListener(new PlaceSelectionListener()
        {
            @Override
            public void onPlaceSelected(Place place)
            {
                pickUpPoint =  place.getName().toString();
                latlngPickUpLocationCoordinates = place.getLatLng(); //Get Longitude and Latitude Coordinates
                if(markerPickUp != null)
                {
                    markerPickUp.remove();
                }
                markerPickUp= mMap.addMarker(new MarkerOptions().position(latlngPickUpLocationCoordinates).title("Pick Up Point: " + pickUpPoint));  //Pick Up Point
                mMap.animateCamera(CameraUpdateFactory.zoomTo(12), 2000, null);

                autocompleteFragmentPickup.setHint("Enter Pickup Point");  //Change Hint. More Efficient instead of having two activities
                //getRouteToMarker(latlngDestinationCoordinates);
                autocompleteFragmentPickup.getView().setVisibility(View.GONE);
                autocompleteFragmentDestination.getView().setVisibility(View.VISIBLE);
            }

            @Override
            public void onError(Status status)
            {

            }
        });

        autocompleteFragmentDestination= (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment_pickup);
        autocompleteFragmentDestination.setHint("Choose Destination");
        autocompleteFragmentDestination.isVisible();
        autocompleteFragmentDestination.getView().setVisibility(View.GONE);
        autocompleteFragmentDestination.setOnPlaceSelectedListener(new PlaceSelectionListener()
        {
            @Override
            public void onPlaceSelected(Place place)
            {

                destination =  place.getName().toString();
                latlngDestinationCoordinates = place.getLatLng(); //Get Longitude and Latitude Coordinates

                if(markerDestination != null)
                {
                    markerDestination.remove();
                }
                markerDestination = mMap.addMarker(new MarkerOptions().position(latlngDestinationCoordinates).title("Destination: " + destination));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(12), 2000, null);
                getRouteToMarker(latlngPickUpLocationCoordinates, latlngDestinationCoordinates);  //Draw Route from PickUp Point to dstination

                autocompleteFragmentPickup.getView().setVisibility(View.VISIBLE);
                autocompleteFragmentDestination.getView().setVisibility(View.GONE);

            }

            @Override
            public void onError(Status status)
            {
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

        mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener()   //Update my Zoom Level to Manual Inputs
        {
            @Override
            public void onCameraMove() {
                myZoomLevel = mMap.getCameraPosition().zoom;
            }
        });
    }

    public void requestUber(View view)
    {
        /*
        if(lastLocation!= null && lastLocation!= null)
        {
            pickUpLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());   //Get Customer Pickup Location Co-ordinates
            markerOrigin = mMap.addMarker(new MarkerOptions().position(pickUpLocation).title("Pick Me Up Here"));  //Add Marker, and Set Title of Marker
        }
        */
        int delayTime = 4000;
        String eventID = "taxiRequest";

        sendUserData.sendEventData(getBaseContext(), userPhoneNumber, eventID,  latlngPickUpLocationCoordinates, latlngDestinationCoordinates, null);
        request.setText("Getting you a Driver...");


        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
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
                    float distanceBtwnCustomerAndDriver = 5324;  //Use Dummy Data for the Time Being
                    if(distanceBtwnCustomerAndDriver < 100)  //If Distance Btwn Driver and Customer is less than 100m
                    {
                        request.setBackgroundColor(Color.RED);
                        request.setTextColor(Color.WHITE);
                        request.setText("Driver has Arrived");
                    }

                    else                                     //If Distance Btwn Driver and Customer is more than 100m
                    {
                        request.setBackgroundColor(Color.RED);
                        request.setTextColor(Color.WHITE);
                        request.setText("Driver Found: " + String.valueOf(distanceBtwnCustomerAndDriver) + " m");  //Change Button Appropriately
                    }

                    cancelRequest.setVisibility(View.VISIBLE);
                    cancelRequest.setBackgroundColor(Color.BLUE);
                    cancelRequest.setTextColor(Color.WHITE);
                    cancelRequest.setText("Ride in Session");

                }

                if(!driverFound)   //If Driver not found
                {

                }

            }
        }, delayTime);


    }



    public void cancelRequest(View view)
    {
        final int displayTime = 1200;
        String eventID = "customerCanceledRideRequest";

        if(driverFound)  //If Cancel Button Pressed while Driver has Already been Found
        {
            sendUserData.sendEventData(getBaseContext(), userPhoneNumber, eventID,  null, null, null);  //Send Data

            request.setBackgroundColor(Color.RED);
            request.setTextColor(Color.WHITE);
            request.setText("Call Queencia");
            cancelRequest.setText("Cancel Request Successful");

            if(markerPickUp != null && markerDestination != null)
            {
                markerPickUp.remove();            //Clear Pick Up Point Marker
                markerDestination.remove();       //Clear the Marker for the Destination previously chosen
                clearRouteFromMap();   //Clear Route From Map
            }


            new Handler().postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    cancelRequest.setVisibility(View.INVISIBLE);
                }
            }, displayTime);

        }
    }



    public void getAssignedDriver()
    {
        String vehicleNumberPlate = "KAV 587V";
        String name = "Tony Almeida";  //Dummy Data
        String phoneNumber = "0728648142";  //Dummy Data
        if(driverAssigned)   //If Driver Assigned Successfully
        {
            driverInformation.setVisibility(View.VISIBLE);
            driverName.setText(name);
            driverPhoneNumber.setText(phoneNumber);
            driverCar.setText(vehicleNumberPlate);
        }

        else
        {
            driverInformation.setVisibility(View.GONE);
            driverName.setText("");
            driverCar.setText("");
        }
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

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        if(!locationDataCopied)
        {
            lastLocation = location;  //Copy the Data
            locationDataCopied = true;
        }

        if(lastLocation != location)
        {
            markerCurrentLocation.remove();
            lastLocation = location;
        }

        markerCurrentLocation = mMap.addMarker(new MarkerOptions().position(latLng).title("My Current Location"));  //Add Marker, and Set Title of Marker


        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

        mMap.animateCamera(CameraUpdateFactory.zoomTo(myZoomLevel), new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
                myZoomLevel = mMap.getCameraPosition().zoom;
            }

            @Override
            public void onCancel() {

            }
        });

        // Log.e("ZOOM_LEVEL",""+myZoomLevel);
        Double currentLatitudeAddress = location.getLatitude();     //Get current Latitude coordinates
        Double currentLongitudeAddress = location.getLongitude();   //Get current Longitude coordinates

    }


    @Override
    public void onConnected(@Nullable Bundle bundle)
    {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(5000);    //Refresh rate
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);  //Highest Accuracy, However drains a lot of battery power

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {

            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }


    public void getRouteToMarker(LatLng pickUpPointCoordinates, LatLng destinationCoordinates)
    {
        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false)  //Disable Alternative Routes for Now
                .waypoints(pickUpPointCoordinates, destinationCoordinates)     //end is pickUpLatLang, start will be last latlong coordininates
                .build();
        routing.execute();
    }


    public void clearRouteFromMap()
    {
        for(Polyline line: polylines)
        {
            line.remove();
        }
        polylines.clear();
    }


    @Override
    public void onRoutingFailure(RouteException e)
    {
        if(e != null)
        {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
        else
        {
            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingStart()
    {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex)
    {
        if(polylines.size()>0)
        {
            for (Polyline poly : polylines)
            {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i <route.size(); i++)
        {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);

            Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onRoutingCancelled()
    {

    }



    @Override
    public void onConnectionSuspended(int i)
    {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.settingsId:
                Intent intent1 = new Intent(getBaseContext(), settingsApp.class);
                startActivity(intent1);
                break;

            case R.id.logoutId:
                Intent intent4 = new Intent(getBaseContext(), customerRegister.class);
                startActivity(intent4);
                break;


            case R.id.helpId:
                Intent intent2 = new Intent(getBaseContext(), Help.class);
                startActivity(intent2);
                break;

            case R.id.aboutId:
                Intent intent3 = new Intent(getBaseContext(), About.class);
                startActivity(intent3);
                break;

        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onStop()  //If Driver gets out of this activity, Notify Db, for Him to be removed as Hes is no longer Active
    {
        super.onStop();
        //sendUserData.sendDriverID(getBaseContext(), driverUserId);     //Send Driver ID that End of Activity
    }
}
