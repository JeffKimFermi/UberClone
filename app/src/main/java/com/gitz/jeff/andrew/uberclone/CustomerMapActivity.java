package com.gitz.jeff.andrew.uberclone;


import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;
import com.pusher.client.channel.Channel;
import com.pusher.client.channel.SubscriptionEventListener;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

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

    Button callTaxi;   //Request for a Taxi
    Button cancelRequest;    //Cancel Taxi Request
    Button driverInfo;

    boolean locationDataCopied = false;
    boolean taxiRequestMade = false;
    boolean rideInSession = false;
    boolean rideRequestAccepted = true;


    TinyDB savedUserPhoneNumber;
    TinyDB getSavedUserPhoneNumber; //Get user Driver User Phone Number to act as ID
    TinyDB getRideRequestResponse;

    Marker markerPickUp;    //Pickup Location Marker
    Marker markerDestination;  //Destination Marker
    Marker markerCurrentLocation; //My Current Locaton Marker
    Marker markerDriverLocation;

    public LatLng latlngDestinationCoordinates;    //Longitude Latitude coordates of your destination
    public LatLng latlngPickUpLocationCoordinates;  //Longitude Latitude co-ordinates of your preferred Pickup Location
    public LatLng pickUpLocation;   //Will Hold Pick Up Location Co-ordinates

    private List<Polyline> polylines;

    private static final int[] COLORS = new int[]{R.color.primary,};
    //private static final int[] COLORS = new int[]{R.color.primary_dark,R.color.primary,R.color.primary_light,R.color.accent,R.color.primary_dark_material_light};

    public PlaceAutocompleteFragment autocompleteFragmentPickup;
    public PlaceAutocompleteFragment autocompleteFragmentDestination;

    final int LOCATION_REQUEST_CODE = 1;

    Dialog myDialog;

    //Public Driver Details
    public String latitudeDriver;
    public String longitudeDriver;
    public String driverName = "Alex Mahone";
    public String driverPhone = "0722833083";
    public String requestId;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        myDialog = new Dialog(this);
        driverInfo = (Button)findViewById(R.id.driverInformation);

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

        getSavedUserPhoneNumber = new TinyDB(getBaseContext());
        getRideRequestResponse = new TinyDB(getBaseContext());

        customerUserId = getSavedUserPhoneNumber.getString("userPhoneNumber");  //Get Saved Phone Number to act as User ID
        callTaxi = (Button)findViewById(R.id.callTaxi);
        cancelRequest = (Button)findViewById(R.id.cancelRequest);
        cancelRequest.setVisibility(View.INVISIBLE);
        driverInfo.setVisibility(View.INVISIBLE);

        checkForPushMessagesFromServer();

        autocompleteFragmentPickup = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment_pickup);
        autocompleteFragmentPickup.setHint("Choose Pick Up Point");
        ((EditText)autocompleteFragmentPickup.getView().findViewById(R.id.place_autocomplete_search_input)).setTextSize(17.0f);

        autocompleteFragmentPickup.setOnPlaceSelectedListener(new PlaceSelectionListener()
        {
            @Override
            public void onPlaceSelected(Place place)
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

            @Override
            public void onError(Status status)
            {

            }
        });

        autocompleteFragmentDestination= (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment_destination);
        autocompleteFragmentDestination.setHint("Choose Destination");
        ((EditText)autocompleteFragmentDestination.getView().findViewById(R.id.place_autocomplete_search_input)).setTextSize(17.0f);

        autocompleteFragmentDestination.setOnPlaceSelectedListener(new PlaceSelectionListener()
        {
            @Override
            public void onPlaceSelected(Place place)
            {

                destinationDescription =  place.getName().toString();
                latlngDestinationCoordinates = place.getLatLng(); //Get Longitude and Latitude Coordinates

               // displayToast(getBaseContext(), "Location: " +latlngDestinationCoordinates);

                if(markerDestination != null)
                {
                    markerDestination.remove();
                }
                markerDestination = mMap.addMarker(new MarkerOptions().position(latlngDestinationCoordinates).title("Destination: " + destinationDescription));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(12), 2000, null);
                drawRouteToMarker(latlngPickUpLocationCoordinates, latlngDestinationCoordinates);  //Draw Route from PickUp Point to dstination
            }

            @Override
            public void onError(Status status)
            {
            }
        });


        driverInfo.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                showAssignedDriverPopup();
            }
        });

        cancelRequest.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                taxiRequestMade = false;

                if(rideRequestAccepted)  //If Cancel Button Pressed while Driver has Already been Found
                {
                    //Pop up an Alert Dialog to confirm End of ride
                    AlertDialog.Builder dialog = new AlertDialog.Builder(CustomerMapActivity.this);
                    dialog.setTitle("Cancel Request?");
                    dialog.setMessage("Confirm you want to Cancel Request?");
                    dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt)
                        {

                            int backgroundColour = Color.parseColor("#40E0D0");
                            callTaxi.setBackgroundColor(backgroundColour);
                            callTaxi.setText("Call Taxi");
                            cancelRequest.setText("Cancel Request Successful");
                            callTaxi.setClickable(true);
                            rideRequestAccepted = true;
                            rideInSession = false;

                            cancelRequest.setVisibility(View.INVISIBLE);
                            driverInfo.setVisibility(View.INVISIBLE);
                            autocompleteFragmentDestination.getView().setVisibility(View.VISIBLE);
                            autocompleteFragmentDestination.getView().setClickable(true);
                            autocompleteFragmentPickup.getView().setVisibility(View.VISIBLE);
                            autocompleteFragmentPickup.getView().setClickable(true);

                            if(markerPickUp != null && markerDestination != null)
                            {
                                markerPickUp.remove();            //Clear Pick Up Point Marker
                                markerDestination.remove();       //Clear the Marker for the Destination previously chosen
                                clearRouteFromMap();   //Clear Route From Map
                            }

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

                if(rideInSession)   //If Ride has Already Started
                {
                    //Pop up an Alert Dialog to confirm End of ride
                    AlertDialog.Builder dialog = new AlertDialog.Builder(CustomerMapActivity.this);
                    dialog.setTitle("End Ride Session?");
                    dialog.setMessage("Confirm you want to End Session?");
                    dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt)
                        {

                            int backgroundColour = Color.parseColor("#40E0D0");
                            callTaxi.setBackgroundColor(backgroundColour);
                            callTaxi.setText("Call Taxi");
                            cancelRequest.setText("Session Ended Successfully");
                            callTaxi.setClickable(true);
                            rideRequestAccepted = true;
                            rideInSession = false;

                            cancelRequest.setVisibility(View.INVISIBLE);
                            driverInfo.setVisibility(View.INVISIBLE);
                            autocompleteFragmentDestination.getView().setVisibility(View.VISIBLE);
                            autocompleteFragmentDestination.getView().setClickable(true);
                            autocompleteFragmentPickup.getView().setVisibility(View.VISIBLE);
                            autocompleteFragmentPickup.getView().setClickable(true);

                            double currentFare = 734.54;
                            cancelRequest.setText("End Session?");
                            callTaxi.setText("Fare Payable: KSh" +currentFare);


                            if(markerPickUp != null && markerDestination != null)
                            {
                                markerPickUp.remove();            //Clear Pick Up Point Marker
                                markerDestination.remove();       //Clear the Marker for the Destination previously chosen
                                clearRouteFromMap();   //Clear Route From Map
                            }

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
            }
        });


        callTaxi.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

            /*
            if(lastLocation!= null && lastLocation!= null)
            {
                pickUpLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());   //Get Customer Pickup Location Co-ordinates
                markerOrigin = mMap.addMarker(new MarkerOptions().position(pickUpLocation).title("Pick Me Up Here"));  //Add Marker, and Set Title of Marker
            }
            */
           // if(pickUpPointDescription == null)
          //  {
          //      displayToast(getBaseContext(), "Error, Missing Pickup Point");
           // }

           // if(destinationDescription == null)
          //  {
            //    displayToast(getBaseContext(), "Error, Missing Pickup Point");
           // }

           // if(pickUpPointDescription != null && destinationDescription != null)
           // {
                new Handler().postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        String rideRequestResponse = getRideRequestResponse.getString("rideRequestResponse");  //Response From Server
                        JSONObject jsonObjResponse = null;
                        String status;
                        String requestId;
                        try
                        {
                            jsonObjResponse = new JSONObject(rideRequestResponse);  //Create New Json Object
                            status = jsonObjResponse.getString("status");
                            requestId = jsonObjResponse.getString("requestId");

                            if(status.equals("Success"))
                            {
                                //Do Nothing
                                displayToast(getBaseContext(), "Successful Request");

                            }

                            else
                            {
                                defaultScreen();
                                displayToast(getBaseContext(), "Request Error, Please Try Again");
                            }
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                },2000);


                taxiRequestMade = true;
                int delayTime = 3000;

                sendUserData.sendRideRequest(getBaseContext(), userPhoneNumber, latlngPickUpLocationCoordinates, latlngDestinationCoordinates, pickUpPointDescription, destinationDescription);

                callTaxi.setBackgroundColor(Color.RED);
                callTaxi.setTextColor(Color.WHITE);
                callTaxi.setText("Getting you a Driver...");

                new Handler().postDelayed(new Runnable()
                {

                    @Override
                    public void run()
                    {
                        TinyDB getRideRequestResponse = new TinyDB(getBaseContext());
                        String rideRequestResponse = getRideRequestResponse.getString("rideRequestResponse");

                        //Get Individual Components of the Response

                        if (rideRequestAccepted) //If Appropriate Driver Has Been Found
                        {
                            //float distanceBtwnCustomerAndDriver = customerLocation.distanceTo(driverLocation);         //Distance in Metres
                            float distanceBtwnCustomerAndDriver = 5324;  //Use Dummy Data for the Time Being
                            float distanceInKms = distanceBtwnCustomerAndDriver / 1000;
                            if (distanceBtwnCustomerAndDriver < 100)  //If Distance Btwn Driver and Customer is less than 100m
                            {
                                callTaxi.setBackgroundColor(Color.RED);
                                callTaxi.setTextColor(Color.WHITE);
                                callTaxi.setText("Driver has Arrived");
                            } else                                     //If Distance Btwn Driver and Customer is more than 100m
                            {

                                callTaxi.setBackgroundColor(Color.RED);
                                callTaxi.setTextColor(Color.WHITE);
                                callTaxi.setText("Driver Found: " + String.valueOf(distanceInKms) + " km");  //Change Button Appropriately
                            }


                            callTaxi.setClickable(false);
                            cancelRequest.setVisibility(View.VISIBLE);
                            driverInfo.setVisibility(View.VISIBLE);

                            cancelRequest.setText("Cancel Ride Request?");
                            autocompleteFragmentDestination.setText("");
                            autocompleteFragmentDestination.getView().setVisibility(View.GONE);
                            autocompleteFragmentDestination.getView().setClickable(false);

                            autocompleteFragmentPickup.setText("");
                            autocompleteFragmentPickup.getView().setVisibility(View.GONE);
                            autocompleteFragmentPickup.getView().setClickable(false);

                            new Handler().postDelayed(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    rideInSession = true;
                                    rideRequestAccepted = false;
                                    cancelRequest.setText("End Session?");
                                }
                            }, 15000);
                        }
                    }
                }, delayTime);

           //  }

            }
        });


        if(rideInSession) //If Ride has Started
        {
            double currentFare = 400.54;
            cancelRequest.setText("End Session?");
            callTaxi.setText("Fare Estimate: KSh" +currentFare);
        }

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


    public void showAssignedDriverPopup()
    {
        TextView txtclose;
        ImageView callBtn;
        ImageView sendSms;
        final EditText textMessage;
        TextView name, phone;
        myDialog.setContentView(R.layout.custompopup_driver_details);

        txtclose =(TextView) myDialog.findViewById(R.id.txtclose);
        name =(TextView) myDialog.findViewById(R.id.driverName);
        phone =(TextView) myDialog.findViewById(R.id.driverPhone);

        name.setText(driverName);
        phone.setText(driverPhone);


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




    public void checkForPushMessagesFromServer()
    {
        PusherOptions options = new PusherOptions();
        options.setCluster("ap2");
        Pusher pusher = new Pusher("830d3e455fd9cfbcec39", options);

        Channel channel = pusher.subscribe(userPhoneNumber);   //use Phone Number as Channel

        channel.bind("no_driver", new SubscriptionEventListener()   //Events
        {
            @Override
            public void onEvent(String channelName, String eventName, final String data)
            {
                //Received Messages From Server

                final String pushedMessages = data;

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



        channel.bind("driver_accepted", new SubscriptionEventListener()   //Events
        {
            @Override
            public void onEvent(String channelName, String eventName, final String data)
            {
                //Received Messages From Server
                Log.e("DriverResponse: ", data);
                final String pushedMessages = data;

                JSONObject jsonObj = null;

                try
                {
                    jsonObj = new JSONObject(pushedMessages);
                }

                catch (Exception e)
                {
                    e.printStackTrace();
                }


                try
                {
                    driverName = jsonObj.getString("driverName");
                    driverPhone = jsonObj.getString("driverPhone");
                    requestId = jsonObj.getString("requestId");
                    latitudeDriver = jsonObj.getString("latitude");
                    longitudeDriver = jsonObj.getString("longitude");

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
                //Received Messages From Server

                final String pushedMessages = data;

                //Act on Response
            }
        });

        pusher.connect();
    }


    public void hideAssignedDriverDetails()
    {
    }

    public void defaultScreen()
    {
        int backgroundColour = Color.parseColor("#40E0D0");
        callTaxi.setBackgroundColor(backgroundColour);
        callTaxi.setText("Call Taxi");
        cancelRequest.setText("Cancel Request Successful");
        callTaxi.setClickable(true);
        rideRequestAccepted = true;
        rideInSession = false;
    }

    public void showDriverLocationMarker()
    {
        if(markerDriverLocation != null)
        {
           markerDriverLocation.remove();
        }
        markerDriverLocation = mMap.addMarker(new MarkerOptions().position(latlngPickUpLocationCoordinates).title("Driver Location: " + pickUpPointDescription));  //Pick Up Point
        mMap.animateCamera(CameraUpdateFactory.zoomTo(12), 2000, null);
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

        Double currentLatitudeAddress = location.getLatitude();     //Get current Latitude coordinates
        Double currentLongitudeAddress = location.getLongitude();   //Get current Longitude coordinates

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


    public void drawRouteToMarker(LatLng pickUpPointCoordinates, LatLng destinationCoordinates)
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
    }

    public void displayToast(Context myContext, String displayToastMessage)
    {
        Context context = myContext;
        Toast.makeText(myContext, displayToastMessage, Toast.LENGTH_LONG).show();
    }
}
