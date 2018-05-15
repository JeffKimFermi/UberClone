package com.gitz.jeff.andrew.uberclone;


import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;
import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;
import com.pusher.client.channel.Channel;
import com.pusher.client.channel.SubscriptionEventListener;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import dmax.dialog.SpotsDialog;

public class CustomerMapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener, RoutingListener {

    private GoogleMap mMap;
    GoogleApiClient googleApiClient;
    Location lastLocation;
    public static float myZoomLevel = 14;
    LocationRequest locationRequest;

    String customerUserId = null;
    String pickUpPointDescription;  //Select where to be Picked
    String destinationDescription;     //Select your Destination
    String userPhoneNumber;

    //Public Driver Details
    String requestId;
    String driverName = "Alex Mahone";
    String driverPhone = "0722833083";
    String vehicleRegistration = "KAV 587V";
    double driverLatitude;
    double driverLongitude;
    double interDistance = 0.0;  //Distance between Driver and Customer

    Button callTaxi;   //Request for a Taxi
    Button cancelRequest;    //Cancel Taxi Request
    Button driverInfo;

    boolean locationDataCopied = false;
    boolean taxiRequestMade = false;
    boolean rideInSession = false;
    boolean rideRequestAccepted = false;
    boolean rideComplete = false;

    TinyDB savedUserPhoneNumber;
    TinyDB getSavedUserPhoneNumber; //Get user Driver User Phone Number to act as ID

    Marker markerPickUp;    //Pickup Location Marker
    Marker markerDestination;  //Destination Marker
    Marker markerCurrentLocation; //My Current Locaton Marker
    Marker markerInitialDriverLocation;
    Marker markerCurrentDriverLocation;

    LatLng latlngDestinationCoordinates;    //Longitude Latitude coordates of your destination
    LatLng latlngPickUpLocationCoordinates;  //Longitude Latitude co-ordinates of your preferred Pickup Location
    LatLng currentCustomerLatLang = new LatLng(0,0);
    LatLng currentDriverLatLang = new LatLng(0,0);  //LatLang Object of current Driver Location


    private List<Polyline> polylines;

    private static final int[] COLORS = new int[]{R.color.primary,};
    //private static final int[] COLORS = new int[]{R.color.primary_dark,R.color.primary,R.color.primary_light,R.color.accent,R.color.primary_dark_material_light};

    public PlaceAutocompleteFragment autocompleteFragmentPickup;
    public PlaceAutocompleteFragment autocompleteFragmentDestination;

    //int distanceBtwnPickupAndDestination = 0;
    //int estimatedJourneyTime = 0;

    final int LOCATION_REQUEST_CODE = 1;

    Dialog myDialog;
    Dialog rideDetailsDialog;

    android.app.AlertDialog alertDialog;
    android.app.AlertDialog alertDialogCancel;

    String costOfRide = "0.0";


    private static CustomerMapActivity inst;
    public static CustomerMapActivity instance()
    {
        return inst;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        inst = this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_map);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(CustomerMapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }

        else
        {
            mapFragment.getMapAsync(this);
        }

        myDialog = new Dialog(this);
        alertDialogCancel = new SpotsDialog(CustomerMapActivity.this);  //Display Alert for 4 Seconds before going to next Activity
        alertDialog = new SpotsDialog(CustomerMapActivity.this);  //Display Alert for 4 Seconds before going to next Activity

        savedUserPhoneNumber = new TinyDB(getBaseContext());
        userPhoneNumber = savedUserPhoneNumber.getString("userPhoneNumber");
        polylines = new ArrayList<>();

        getSavedUserPhoneNumber = new TinyDB(getBaseContext());

        customerUserId = getSavedUserPhoneNumber.getString("userPhoneNumber");  //Get Saved Phone Number to act as User ID
        callTaxi = (Button)findViewById(R.id.callTaxi);

        cancelRequest = (Button)findViewById(R.id.cancelRequest);
        cancelRequest.setVisibility(View.INVISIBLE);

        driverInfo = (Button)findViewById(R.id.driverInformation);
        driverInfo.setVisibility(View.INVISIBLE);


        autocompleteFragmentPickup = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment_pickup);
        autocompleteFragmentPickup.setHint("Choose Pick Up Point");
        ((EditText)autocompleteFragmentPickup.getView().findViewById(R.id.place_autocomplete_search_input)).setTextSize(17.0f);

        checkForPushMessagesFromServer();

        autocompleteFragmentPickup.setOnPlaceSelectedListener(new PlaceSelectionListener()    //Pickup Point Autocomplete
        {
            @Override
            public void onPlaceSelected(Place placePickup)
            {
               handlePickupPointEntry(placePickup);
            }

            @Override
            public void onError(Status status)
            {}
        });

        autocompleteFragmentDestination= (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment_destination);
        autocompleteFragmentDestination.setHint("Choose Destination");
        ((EditText)autocompleteFragmentDestination.getView().findViewById(R.id.place_autocomplete_search_input)).setTextSize(17.0f);

        autocompleteFragmentDestination.setOnPlaceSelectedListener(new PlaceSelectionListener()    //Destination Point Autocomplete
        {
            @Override
            public void onPlaceSelected(Place placeDestination)
            {
                handleDestinationPointEntry(placeDestination);
            }

            @Override
            public void onError(Status status)
            {}
        });


        driverInfo.setOnClickListener(new View.OnClickListener()    //Get Driver Information
        {
            @Override
            public void onClick(View v)
            {
                showAssignedDriverPopup();
            }
        });

        cancelRequest.setOnClickListener(new View.OnClickListener()  //Cancel Ride Request
        {
            @Override
            public void onClick(View v)
            {
               handleCancelRideRequest();
            }
        });

        callTaxi.setOnClickListener(new View.OnClickListener()      //Make Taxi Request
        {
            @Override
            public void onClick(View v)
            {
                if(!rideComplete)
                {
                    handleCallTaxiRequest();
                    showDialogAlert();
                }

               if(rideComplete)  //If Ride Complete
               {
                 showRideDetails();
                 cancelRequest.setClickable(true);
                 driverInfo.setClickable(true);
               }
            }
        });

    }

    public void handlePickupPointEntry(Place place)
    {
        pickUpPointDescription =  place.getName().toString();

        latlngPickUpLocationCoordinates = place.getLatLng(); //Get Longitude and Latitude Coordinates
        if(markerPickUp != null)
        {
            markerPickUp.remove();
        }
        markerPickUp= mMap.addMarker(new MarkerOptions().position(latlngPickUpLocationCoordinates).title("Pick Up Point: " + pickUpPointDescription));  //Pick Up Point
        mMap.animateCamera(CameraUpdateFactory.zoomTo(12), 2000, null);

        autocompleteFragmentPickup.setHint("Enter Pickup Point");  //Change Hint. More Efficient instead of having two activities
    }

    public void handleDestinationPointEntry(Place place)
    {
        destinationDescription =  place.getName().toString();
        latlngDestinationCoordinates = place.getLatLng(); //Get Longitude and Latitude Coordinates

         //displayToast(getBaseContext(), "Location: " +latlngDestinationCoordinates);

        if(markerDestination != null)
        {
            markerDestination.remove();
        }
        markerDestination = mMap.addMarker(new MarkerOptions().position(latlngDestinationCoordinates).title("Destination: " + destinationDescription));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(12), 2000, null);
        drawRouteToBetweenPickupAndDestination(latlngPickUpLocationCoordinates, latlngDestinationCoordinates);  //Draw Route from PickUp Point to dstination
    }

    public void handleCallTaxiRequest()
    {
        if(pickUpPointDescription == null)
        {
            displayToast(getBaseContext(), "Error, Missing Pickup Point");
        }

        if(destinationDescription == null)
        {
            displayToast(getBaseContext(), "Error, Missing Pickup Point");
        }

        if(pickUpPointDescription != null && destinationDescription != null)
        {
            sendUserData.sendNewRideRequest(getBaseContext(), userPhoneNumber, latlngPickUpLocationCoordinates, latlngDestinationCoordinates, pickUpPointDescription, destinationDescription);
            updateUIAfterSuccessfulRideRequest();  //Getting you a Driver Stuff
        }
    }

    public void updateUIAfterSuccessfulRideRequest()
    {
        taxiRequestMade = true;

        callTaxi.setBackgroundColor(Color.RED);
        callTaxi.setTextColor(Color.WHITE);
        callTaxi.setText("Getting you a Driver...");
        callTaxi.setClickable(false);

        cancelRequest.setClickable(false);
        cancelRequest.setVisibility(View.INVISIBLE);

        driverInfo.setClickable(false);
        driverInfo.setVisibility(View.INVISIBLE);

        if(markerCurrentDriverLocation != null)
        {
            markerCurrentDriverLocation.remove();  //Removing Periodic Driver Location Marker Before Placing current Location Marker
        }

        cancelRequest.setText("Cancel Ride Request?");
        autocompleteFragmentDestination.setText("");
        autocompleteFragmentDestination.getView().setVisibility(View.GONE);
        autocompleteFragmentDestination.getView().setClickable(false);

        autocompleteFragmentPickup.setText("");
        autocompleteFragmentPickup.getView().setVisibility(View.GONE);
        autocompleteFragmentPickup.getView().setClickable(false);

    }


    public void updateDistancesDisplayedOnUI()
    {
        //Update Driver to Customer Distance Periodically
        double interDistanceInMetres ;
        double interDistanceInKms;

        interDistanceInMetres = interDistance;
        interDistanceInKms = (interDistanceInMetres/1000);

        Log.e("DinMetres", String.valueOf(interDistanceInMetres));

        //Update Distances Displayed on UI
        if(interDistanceInMetres <= 1000)
        {
            if (interDistanceInMetres < 60)  //If Distance Btwn Driver and Customer is less than 100m
            {
                callTaxi.setBackgroundColor(Color.RED);
                callTaxi.setTextColor(Color.WHITE);
                callTaxi.setText("Driver has Arrived");
            }
            else                                     //If Distance Btwn Driver and Customer is more than 100m
            {
                callTaxi.setBackgroundColor(Color.RED);
                callTaxi.setTextColor(Color.WHITE);
                callTaxi.setText("Driver Found: " + String.valueOf(interDistanceInMetres) + " m");  //Change Button Appropriately
            }
        }

        else
        {
            callTaxi.setBackgroundColor(Color.RED);
            callTaxi.setTextColor(Color.WHITE);
            callTaxi.setText("Driver Found: " + String.valueOf(interDistanceInKms) + " km");  //Change Button Appropriately
        }
    }

    public void rideComplete()
    {
        callTaxi.setText("Ride Details");
        callTaxi.setClickable(true);
    }

    public void showRideDetails()
    {
        rideDetailsDialog = new Dialog(CustomerMapActivity.this);
        rideDetailsDialog.setContentView(R.layout.ridedetails);

        TextView txtclose;
        txtclose = (TextView)rideDetailsDialog.findViewById(R.id.txtclose);

        TextView rideCost;
        TextView distanceCovered;

        txtclose.setText("X");

        txtclose.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
               // resetFlagsAndMemoryElements();
                rideDetailsDialog.dismiss();
                defaultUI();
            }
        });


        rideCost = (TextView) rideDetailsDialog.findViewById(R.id.cost);
        distanceCovered =(TextView) rideDetailsDialog.findViewById(R.id.distance);

        //rideDistance = getTotalDistanceTravelled();

        //String doubleDistance = Double.toString(rideDistance);

        rideCost.setText(costOfRide + " Ksh");
        //distanceCovered.setText(doubleDistance + " Km");

        rideDetailsDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        rideDetailsDialog.show();
    }

    public void showAssignedDriverPopup()
    {
        TextView txtclose;
        ImageView callBtn;
        ImageView sendSms;
        final EditText textMessage;
        TextView name, phone, numberPlate;
        myDialog.setContentView(R.layout.custompopup_driver_details);

        txtclose =(TextView) myDialog.findViewById(R.id.txtclose);
        name =(TextView) myDialog.findViewById(R.id.driverName);
        phone =(TextView) myDialog.findViewById(R.id.driverPhone);
        numberPlate = (TextView)myDialog.findViewById(R.id.vehicleRegistration);

        //Get Distance Between Driver and Customer
        name.setText(driverName);
        phone.setText(driverPhone);
        numberPlate.setText(vehicleRegistration);
        //distance.setText(String.valueOf(interDistanceInKms) + " Km ");

        callBtn = (ImageView)myDialog.findViewById(R.id.callbutton);
        sendSms = (ImageView)myDialog.findViewById(R.id.sendSms);
        textMessage = (EditText)myDialog.findViewById(R.id.message);


        txtclose.setText("X");
        txtclose.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                myDialog.dismiss();
            }
        });

        callBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + driverPhone));

                if (ActivityCompat.checkSelfPermission(CustomerMapActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED)
                {
                    return;
                }
                startActivity(callIntent);

            }
        });

        sendSms.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String Message = textMessage.getText().toString();

                try {
                    SmsManager sms_manager = SmsManager.getDefault();
                    sms_manager.sendTextMessage(driverPhone, null, Message, null, null);
                    textMessage.setText("");
                    displayToast(getBaseContext(), "Message Sent");
                }
                catch (Exception ex){
                    displayToast(getBaseContext(), ex.getMessage().toString());
                }
            }
        });


        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialog.show();
    }

    public void showAssignedDriverLocation()
    {
        //Driver Markers Stuff
        int heightCar = 40;
        int widthCar = 35;
        BitmapDrawable bitmapdrawCar =(BitmapDrawable)getResources().getDrawable(R.mipmap.car);
        Bitmap bCar = bitmapdrawCar.getBitmap();
        Bitmap smallCar = Bitmap.createScaledBitmap(bCar, widthCar, heightCar, false);


        cancelRequest.setClickable(true);
        cancelRequest.setVisibility(View.VISIBLE);

        driverInfo.setClickable(true);
        driverInfo.setVisibility(View.VISIBLE);

        markerInitialDriverLocation= mMap.addMarker(new MarkerOptions().position(currentDriverLatLang).title("Driver Location").icon(BitmapDescriptorFactory.fromBitmap(smallCar)));  //Add Marker, and Set Title of Marker
        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentDriverLatLang));
    }

    public void hideAssignedDriverLocation()
    {
        if(markerInitialDriverLocation != null && markerCurrentDriverLocation != null)
        {
            markerInitialDriverLocation.remove();
            markerCurrentDriverLocation.remove();
        }
    }

    /*
    public double getDistanceBtwnDriverAndCustomer()
    {
        Double currentDistanceBtwnDriverAndCustomer;
        currentDistanceBtwnDriverAndCustomer = SphericalUtil.computeDistanceBetween(periodicDriverLatLang,  currentCustomerLatLang);

        currentDistanceBtwnDriverAndCustomer = Math.floor(currentDistanceBtwnDriverAndCustomer*100) / 100;
        return currentDistanceBtwnDriverAndCustomer;
    }
*/

    public void handleCancelRideRequest()
    {
        taxiRequestMade = false;

        //Pop up an Alert Dialog to confirm End of ride
        AlertDialog.Builder dialog = new AlertDialog.Builder(CustomerMapActivity.this);
        dialog.setTitle("Cancel Request?");
        dialog.setMessage("Confirm you want to Cancel Request?");
        dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt)
            {
                alertDialogCancel.show();
                new Handler().postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        sendUserData.sendCancelRideRequest(getBaseContext(), requestId, userPhoneNumber);
                        alertDialogCancel.dismiss(); //Dismiss just in case there was network error

                        defaultUI();

                        hideAssignedDriverLocation();

                        displayToast(getBaseContext(), "Cancel Request Successful");
                    }
                }, 1700);
            }
        });

        dialog.setNegativeButton("No", new DialogInterface.OnClickListener()
        {

            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt)
            {
                //Do Nothing
            }
        });

        dialog.show();
    }

    public void rideHasStarted()
    {
        rideInSession = true;
        rideRequestAccepted = false;

        runOnUiThread(new Runnable()
        {

            @Override
            public void run()
            {

                callTaxi.setClickable(false);
                callTaxi.setText("Status: Ride in Session");

                cancelRequest.setClickable(false);
                cancelRequest.setVisibility(View.INVISIBLE);

                driverInfo.setClickable(false);
                driverInfo.setVisibility(View.INVISIBLE);

                clearRouteFromMap();  //Clear Route
                if(markerDestination != null)
                {
                    markerDestination.remove(); //Remove Destination Marker
                }

                if(markerPickUp != null)
                {
                    markerPickUp.remove();  //Remove Pickup Marker
                }

                if(markerCurrentDriverLocation != null && markerInitialDriverLocation != null)
                {
                    markerCurrentDriverLocation.remove();
                    markerInitialDriverLocation.remove();
                }

            }
        });
    }

    public void showPeriodicLocationOfAssignedDriver()
    {
        //Driver Markers Stuff
        int heightCar = 40;
        int widthCar = 35;
        BitmapDrawable bitmapdrawCar =(BitmapDrawable)getResources().getDrawable(R.mipmap.car);
        Bitmap bCar = bitmapdrawCar.getBitmap();
        Bitmap smallCar = Bitmap.createScaledBitmap(bCar, widthCar, heightCar, false);

        markerCurrentDriverLocation = mMap.addMarker(new MarkerOptions().position(currentDriverLatLang).title("Driver Location").icon(BitmapDescriptorFactory.fromBitmap(smallCar)));  //Add Marker, and Set Title of Marker
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(currentDriverLatLang));
    }

    public void checkForPushMessagesFromServer()
    {
        PusherOptions options = new PusherOptions();
        options.setCluster("ap2");
        Pusher pusher = new Pusher("830d3e455fd9cfbcec39", options);

        Channel channel = pusher.subscribe(userPhoneNumber);   //use Phone Number as Channel

        channel.bind("no_driver", new SubscriptionEventListener()   //Push Events
        {
            @Override
            public void onEvent(String channelName, String eventName, final String data)
            {
                //Received Messages From Server

                final String noDriverFound = "No Driver Currently Available";

                new Thread()
                {
                    public void run()
                    {
                        CustomerMapActivity.this.runOnUiThread(new Runnable()
                        {
                            public void run()
                            {
                                Toast.makeText(getBaseContext(), noDriverFound, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }.start();

            }
        });



        /*Driver has Accepted Customer's ride Request*/
        channel.bind("driver_accepted", new SubscriptionEventListener()   //Events
        {
            @Override
            public void onEvent(String channelName, String eventName, final String data)
            {
                rideRequestAccepted = true;

                //Received Messages From Server
                Log.e("DriverResponse: ", data);

                JSONObject jsonObj = null;

                try
                {
                    jsonObj = new JSONObject(data);

                    requestId = jsonObj.getString("requestId");
                    driverName = jsonObj.getString("driverName");
                    driverPhone = jsonObj.getString("driverPhone");
                    vehicleRegistration = jsonObj.getString("vehicleRegistration");
                    driverLatitude = jsonObj.getDouble("driverLatitude");
                    driverLongitude = jsonObj.getDouble("driverLongitude");
                    currentDriverLatLang = new LatLng(driverLatitude, driverLongitude);

                    runOnUiThread(new Runnable()   //Special Thread to do the work
                    {

                        @Override
                        public void run()
                        {
                            showAssignedDriverLocation();        //Display Driver's Current Location
                        }
                    });

                }

                catch (Exception e)
                {
                    e.printStackTrace();
                }

            }
        });


        channel.bind("ride_started", new SubscriptionEventListener()   //Events
        {
            @Override
            public void onEvent(String channelName, String eventName, final String data)
            {
                rideHasStarted();  //Update UI Appropriately since Ride has Started
            }
        });



        /*Update Driver Current Location*/
        channel.bind("driver_location_updated", new SubscriptionEventListener()   //Events
        {
            @Override
            public void onEvent(String channelName, String eventName, final String data)
            {
                //Received Messages From Server
                Log.e("Location response: ", data);

                JSONObject jsonObj = null;
                String statusResponse = null;
                String messageResponse = null;

                try
                {
                    jsonObj = new JSONObject(data);

                    statusResponse = jsonObj.getString("status");
                    messageResponse = jsonObj.getString("message");

                    if(statusResponse.equals("Success"))
                    {
                        driverLatitude = jsonObj.getDouble("driverLatitude");
                        driverLongitude = jsonObj.getDouble("driverLongitude");
                        currentDriverLatLang= new LatLng(driverLatitude, driverLongitude); //Co-ordinates of Periodic driver Location once Ride Accepted
                    }

                }

                catch (Exception e)
                {
                    e.printStackTrace();
                }

            }
        });


        channel.bind("ride_cancelled", new SubscriptionEventListener()   //Events
        {
            @Override
            public void onEvent(String channelName, String eventName, final String data)
            {
                //Received Messages From Server
                Log.e("CANCEL RESPONSE", data);
                JSONObject jsonObj = null;
                String statusResponse;

                try
                {
                    jsonObj = new JSONObject(data);

                    statusResponse = jsonObj.getString("status");

                    if(statusResponse.equals("Success"))
                    {
                        runOnUiThread(new Runnable()   //Special Thread to do the work
                        {

                            @Override
                            public void run()
                            {
                                defaultUI();
                            }
                        });
                    }

                }

                catch (Exception e)
                {
                    e.printStackTrace();
                }

            }
        });



         /*Driver has Accepted Customer's ride Request*/
        channel.bind("ride_completed", new SubscriptionEventListener()   //Events
        {
            @Override
            public void onEvent(String channelName, String eventName, final String data)
            {
                //Received Messages From Server
                Log.e("RideCompleteResponse: ", data);

                JSONObject jsonObj = null;
                String statusResponse;
                String messageResponse;

                try
                {
                    jsonObj = new JSONObject(data);

                    requestId = jsonObj.getString("requestId");

                    statusResponse = jsonObj.getString("status");
                    costOfRide = jsonObj.getString("cost");
                    rideComplete = true;

                    if(statusResponse.equals("Success"))
                    {
                        runOnUiThread(new Runnable()   //Special Thread to do the work
                        {
                            @Override
                            public void run()
                            {
                              rideComplete();
                            }
                        });
                    }
                }

                catch (Exception e)
                {
                    e.printStackTrace();
                }

            }
        });


        pusher.connect();
    }

    public void resetFlagsAndMemoryElements()  //Clear all Memory Elements associated with ride
    {
        taxiRequestMade = false;
        rideInSession = false;
        rideRequestAccepted = false;
        rideComplete = false;
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

    public void defaultUI()
    {
        int backgroundColour = Color.parseColor("#40E0D0");
        callTaxi.setBackgroundColor(backgroundColour);
        callTaxi.setText("Call Taxi");
        callTaxi.setClickable(true);

        rideRequestAccepted = false;
        rideInSession = false;

        cancelRequest.setVisibility(View.INVISIBLE);
        driverInfo.setVisibility(View.INVISIBLE);
        autocompleteFragmentDestination.getView().setVisibility(View.VISIBLE);
        autocompleteFragmentDestination.getView().setClickable(true);
        autocompleteFragmentPickup.getView().setVisibility(View.VISIBLE);
        autocompleteFragmentPickup.getView().setClickable(true);

        if(markerPickUp != null)
        {
            markerPickUp.remove();            //Clear Pick Up Point Marker
            clearRouteFromMap();   //Clear Route From Map
        }

        if(markerDestination != null)
        {
            markerDestination.remove();       //Clear the Marker for the Destination previously chosen
            clearRouteFromMap();   //Clear Route From Map
        }
        if(markerInitialDriverLocation != null && markerCurrentDriverLocation != null)
        {
            markerCurrentDriverLocation.remove();
            markerInitialDriverLocation.remove();
            clearRouteFromMap();
        }
    }

    public void showDialogAlert()
    {
        alertDialog.show();

        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                alertDialog.dismiss(); //Dismiss just in case there was network error
            }
        }, 3000);
    }

    public void hideDialogAlert()
    {
        alertDialog.dismiss();
    }


    public void displayToast(Context myContext, String displayToastMessage)
    {
        Context context = myContext;
        Toast.makeText(myContext, displayToastMessage, Toast.LENGTH_LONG).show();
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

        if(!locationDataCopied)
        {
            lastLocation = location;  //Copy the Data
            LatLng initLatLang = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
            currentCustomerLatLang = initLatLang;
            markerCurrentLocation = mMap.addMarker(new MarkerOptions().position(initLatLang).title("My Current Location"));  //Add Marker, and Set Title of Marker
            locationDataCopied = true;
            mMap.moveCamera(CameraUpdateFactory.newLatLng(initLatLang));
        }

        else if(lastLocation != location)
        {
            if(taxiRequestMade)  //If Either Driver of Customer in Motion, Always recenter periodically(4Secs)
            {
                markerCurrentLocation.remove();
                lastLocation = location;
                LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                currentCustomerLatLang = currentLatLng;
                markerCurrentLocation = mMap.addMarker(new MarkerOptions().position(currentLatLng).title("My Current Location"));  //Add Marker, and Set Title of Marker
                mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLatLng));

            }
        }


        mMap.animateCamera(CameraUpdateFactory.zoomTo(myZoomLevel), new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish()
            {
                myZoomLevel = mMap.getCameraPosition().zoom;
            }

            @Override
            public void onCancel()
            {

            }
        });

        Double currentDistanceBtwnDriverAndCustomer;
        if((currentDriverLatLang.latitude != 0.0 && currentDriverLatLang.longitude != 0.0) && (currentCustomerLatLang.latitude != 0.0 && currentCustomerLatLang.longitude != 0.0))  //If non is zero
        {
            currentDistanceBtwnDriverAndCustomer = SphericalUtil.computeDistanceBetween(currentDriverLatLang, currentCustomerLatLang);
            currentDistanceBtwnDriverAndCustomer = Math.floor(currentDistanceBtwnDriverAndCustomer * 100) / 100;

            interDistance = currentDistanceBtwnDriverAndCustomer;  //Get periodic distance between Driver and Customer
            Log.e("dLat", String.valueOf(currentDriverLatLang.latitude));
            Log.e("dLong", String.valueOf(currentDriverLatLang.longitude));
            Log.e("cLat", String.valueOf(currentCustomerLatLang.latitude));
            Log.e("cLong", String.valueOf(currentCustomerLatLang.longitude));

        }

        if(rideRequestAccepted)    //If Driver has Accepted Ride Request
        {
            if(markerInitialDriverLocation != null)
            {
                //markerInitialDriverLocation.remove();  //Remove Marker of Initial Driver Location
                showPeriodicLocationOfAssignedDriver();  //Show Periodic Location of Assigned Driver
                updateDistancesDisplayedOnUI();
            }
        }

    }

    @Override
    public void onConnected(@Nullable Bundle bundle)
    {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(2500);    //Refresh rate
        locationRequest.setFastestInterval(2500);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);  //Highest Accuracy, However drains a lot of battery power

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {

            return;
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    public void drawRouteToBetweenPickupAndDestination(LatLng pickUpPointCoordinates, LatLng destinationCoordinates)
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
            displayToast(getBaseContext(),"Error: " + e.getMessage() );
        }
        else
        {
            displayToast(getBaseContext(), "Something went wrong, Try again");
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

            //distanceBtwnPickupAndDestination = route.get(i).getDistanceValue();
            //estimatedJourneyTime = route.get(i).getDurationValue();

            //displayToast(getBaseContext(), "Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue());
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
    public boolean onOptionsItemSelected(MenuItem item)
    {
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
    }

    @Override
    public  void onBackPressed()
    {
        finish();
        super.onBackPressed();
    }

}
