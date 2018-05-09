package com.gitz.jeff.andrew.uberclone;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.Uri;
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
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
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
import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;
import com.pusher.client.channel.Channel;
import com.pusher.client.channel.SubscriptionEventListener;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import static com.gitz.jeff.andrew.uberclone.R.id.map;

public class DriverMapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener, RoutingListener
{

    private GoogleMap mMap;
    GoogleApiClient googleApiClient;
    LocationRequest locationRequest;
    Location lastLocation;
    LatLng currentLocation = new LatLng(0, 0);
    LatLng previousLocation = new LatLng(0, 0);
    LatLng currentDriverLocation = new LatLng(0, 0);
    LatLng startOfRideLocation = new LatLng(0, 0); //Location of Ride Start
    LatLng endOfRideLocation = new LatLng(0, 0);   //Location of Ride End
    Marker markerDriverLocation;   //My Current Location Marker
    Marker markerCustomerLocation;  //Customer Location Marker

    public static float myZoomLevel = 14;
    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.primary_dark,R.color.primary,R.color.primary_light,R.color.accent,R.color.primary_dark_material_light};
    final int LOCATION_REQUEST_CODE = 1;

    Button driverMainButton;
    Button endOfSession;
    Button customerInformation;

    boolean rideInSession;
    boolean readyToStartRide;
    boolean locationDataCopied = false;
    boolean driverAvailable = false;  //Set When Driver is Available
    boolean rideComplete = false;

    TinyDB savedUserPhoneNumber;
    TinyDB savedSelectedChoice;
    TinyDB savedSourceLatitude;
    TinyDB savedSourceLongitude;
    TinyDB savedDestinationLatitude;
    TinyDB savedDestinationLongitude;
    TinyDB savedRequestId;
    TinyDB savedDriverLatitude;
    TinyDB savedCustomerName;
    TinyDB savedCustomerPhone;
    TinyDB savedPickup;
    TinyDB savedDestination;

    TinyDB savedDriverLongitude;

    public static String driverPhoneNumber;

    //Public Customer Details
    String requestId;
    String pickupName = "Prestige Plaza";  //Name description of Pickup Point
    String destinationName = "Upper Hill"; //Name description of Destination Point
    String customerName = "Mrs. Lucy";
    String customerPhone = "0722833083";
    double latitudePickupLocation;  //Latitide of Pickup Location
    double longitudePickupLocation;  //Longitude of Destination Location
    double latitudeDestinationLocation;
    double longitudeDestinationLocation;
    double latitudeDriverLocation;
    double longitudeDriverLocation;

    LatLng customerPickupLocation = new LatLng(0, 0);
    LatLng customerDestinationLocation = new LatLng(0, 0);


    TextView pickup;
    TextView destination;
    TextView name;
    TextView phone;

    int selectedChoice;

    Dialog myDialog;
    Dialog rideDetailsDialog;

    String costOfRide = "0.0";
    double distanceOfRide = 0.0;

    private static DriverMapActivity inst;
    public static DriverMapActivity instance()
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
        setContentView(R.layout.activity_driver_map);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(map);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DriverMapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }
        else
        {
            mapFragment.getMapAsync(this);
        }



        savedUserPhoneNumber = new TinyDB(getBaseContext());
        savedSelectedChoice = new TinyDB(getBaseContext());
        savedSourceLatitude = new TinyDB(getBaseContext());
        savedSourceLongitude = new TinyDB(getBaseContext());
        savedDestinationLatitude = new TinyDB(getBaseContext());
        savedDestinationLongitude = new TinyDB(getBaseContext());
        savedDriverLatitude = new TinyDB(getBaseContext());
        savedSourceLongitude = new TinyDB(getBaseContext());
        savedRequestId = new TinyDB(getBaseContext());
        savedCustomerName = new TinyDB(getBaseContext());
        savedCustomerPhone = new TinyDB(getBaseContext());
        savedPickup = new TinyDB(getBaseContext());
        savedDestination = new TinyDB(getBaseContext());

        driverPhoneNumber = savedUserPhoneNumber.getString("userPhoneNumber");

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        polylines = new ArrayList<>();

        endOfSession  = (Button)findViewById(R.id.endOfSession);
        endOfSession.setVisibility(View.INVISIBLE);

        customerInformation = (Button)findViewById(R.id.customerInfo);
        customerInformation.setVisibility(View.INVISIBLE);


        driverMainButton = (Button)findViewById(R.id.driverMainButton);

        checkForPushMessagesFromServer();  //Check for Push Messages

        updateUIAfterCustomerRideRequest();  //Update UI based on Driver Choice to ride request

        checkIfLocationHasChangedConsiderably(); //Update Driver Location if Position Changed > 200m

        driverMainButton.setOnClickListener(new View.OnClickListener()       //Main Driver Functionality Button
        {
            @Override
            public void onClick(View v)
            {
                handleDriverMainActivityButton();
            }
        });

        endOfSession.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(rideInSession)
                {
                    endOfRideConfirmationAlert();
                }

                else
                {
                    cancelRideRequestConfirmationAlert();
                }
            }
        });

        customerInformation.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                showAssignedCustomerProfile();
            }
        });

    }

    public void handleDriverMainActivityButton()
    {
        if(readyToStartRide && !rideComplete)
        {
            endOfSession.setVisibility(View.VISIBLE);
            endOfSession.setText("End Session?");
            customerInformation.setVisibility(View.GONE);
            driverMainButton.setText("Status: Ride In Session");
            driverMainButton.setClickable(false);
            readyToStartRide = false;
            rideInSession = true;
            startOfRideLocation = currentLocation;   //Pick Exact Coordinates of when Ride Started

            sendUserData.sendRideStartedNotification(getBaseContext(), requestId, driverPhoneNumber);   //currentLatitudeLongitude is current Driver Location
        }

        else
        {
            if(!rideComplete)
            {
                driverAvailable = true;
                driverMainButton.setText("Checking For Customers");
                driverMainButton.setBackgroundColor(Color.RED);
                driverMainButton.setTextColor(Color.WHITE);
            }
        }


        if(rideComplete)
        {
            selectedChoice = 0;
            showRideDetails();
        }


    }

    public void checkForPushMessagesFromServer()
    {
        PusherOptions options = new PusherOptions();
        options.setCluster("ap2");
        Pusher pusher = new Pusher("830d3e455fd9cfbcec39", options);

        Channel channel = pusher.subscribe(driverPhoneNumber);   //use Phone Number as Channel

        channel.bind("ride_request", new SubscriptionEventListener()   //Events
        {
            @Override
            public void onEvent(String channelName, String eventName, final String data)
            {
                //Received Messages From Server
                Log.e("PushResponse", data);

                JSONObject jsonObj = null;   //Create JSON Object

                try
                {
                    jsonObj = new JSONObject(data);

                    requestId = jsonObj.getString("requestId");
                    savedRequestId.putString("id", requestId);

                    String statusResponse = jsonObj.getString("status");

                    customerName = jsonObj.getString("riderName");  //Get name of Customer
                    customerPhone = jsonObj.getString("riderPhone");  //Get Phone Number of Customer

                    //Get Pickup latlang coordinates
                    latitudePickupLocation = jsonObj.getDouble("sourceLatitude");
                    longitudePickupLocation = jsonObj.getDouble("sourceLongitude");

                    //Get Destination latlang coordinates
                    latitudeDestinationLocation = jsonObj.getDouble("destinationLatitude");
                    longitudeDestinationLocation = jsonObj.getDouble("destinationLongitude");

                    latitudeDriverLocation = currentDriverLocation.latitude;
                    longitudeDriverLocation = currentDriverLocation.longitude;

                    savedDriverLatitude.putDouble("driverLatitude", latitudeDriverLocation);  //Save current coordinates
                    savedDriverLatitude.putDouble("driverLongitude", longitudeDriverLocation);  //Save current ooordinates


                    //Get Pickup and Destination Locations Descriptions
                    //pickupName = jsonObj.getString("sourceDescription");
                    destinationName = jsonObj.getString("destinationDescription");

                    if(statusResponse.equals("Success"))
                    {
                        //Save the Co-ordinates
                        savedSourceLatitude.putDouble("sLatitude", latitudePickupLocation);
                        savedSourceLongitude.putDouble("sLongitude", longitudePickupLocation);
                        savedDestinationLatitude.putDouble("dLatitude", latitudeDestinationLocation);
                        savedDestinationLongitude.putDouble("dLongitude", longitudeDestinationLocation);

                        savedCustomerName.putString("customerName", customerName);
                        savedCustomerPhone.putString("customerPhone", customerPhone);
                        // savedPickup.putString("pickupPoint", pickupName);
                        savedDestination.putString("destinationPoint", destinationName);

                        Intent intent = new Intent(DriverMapActivity.this, newCustomerPopup.class);
                        intent.putExtra("pickup", pickupName);
                        intent.putExtra("destination", destinationName);
                        startActivity(intent);
                    }
                    else
                    {}

                }

                catch (Exception e)
                {
                    Log.e("JSONPARSE ERROR", "error parsing JSON from pusher");
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

                try
                {
                    jsonObj = new JSONObject(data);

                    requestId = jsonObj.getString("requestId");
                    statusResponse = jsonObj.getString("status");
                    costOfRide = jsonObj.getString("cost");

                    if(statusResponse.equals("Success"))
                    {
                        rideComplete = true;
                        runOnUiThread(new Runnable()   //Special Thread to do the work
                        {

                            @Override
                            public void run()
                            {
                                driverMainButton.setClickable(true);
                                driverMainButton.setBackgroundColor(Color.RED);
                                driverMainButton.setText("Ride Details");
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

    public void updateUIAfterCustomerRideRequest()
    {

        requestId = savedRequestId.getString("id");
        selectedChoice = savedSelectedChoice.getInt("select"); //1 for Accept, 2, for Reject, 3 for Cancel

        latitudePickupLocation = savedSourceLatitude.getDouble("sLatitude", 0.0);
        longitudePickupLocation = savedSourceLongitude.getDouble("sLongitude", 0.0);

        customerPickupLocation = new LatLng(latitudePickupLocation, longitudePickupLocation);
        customerDestinationLocation = new LatLng(latitudeDestinationLocation, longitudeDestinationLocation);
        currentDriverLocation = new LatLng(savedDriverLatitude.getDouble("driverLatitude", 0.0), savedDestinationLongitude.getDouble("driverLongitude", 0.0));

        //Toast.makeText(getBaseContext(), ""+selectedChoice, Toast.LENGTH_LONG).show();

        if(selectedChoice == 1)  //Accept
        {
            sendUserData.sendRideRequestAccepted(getBaseContext(), requestId, driverPhoneNumber, currentDriverLocation);
            driverMainButton.setVisibility(View.VISIBLE);
            driverMainButton.setClickable(true);
            driverMainButton.setText("START SESSION");
            endOfSession.setVisibility(View.VISIBLE);
            endOfSession.setText("Cancel Request?");
            customerInformation.setVisibility(View.VISIBLE);
            readyToStartRide = true;
        }

        else if(selectedChoice == 2)  //Reject
        {
            sendUserData.sendRideRequestRejected(getBaseContext(), requestId, driverPhoneNumber);
            defaultUI();

        }

        else if(selectedChoice == 3)  //Cancel
        {
            sendUserData.sendRideRequestRejected(getBaseContext(), requestId, driverPhoneNumber);
            defaultUI();
        }

        else{}
    }

    public void defaultUI()
    {
        int backgroundColour = Color.parseColor("#40E0D0");
        driverMainButton.setBackgroundColor(backgroundColour);
        driverMainButton.setText("Available");
        driverMainButton.setVisibility(View.VISIBLE); //Not Visible
        driverMainButton.setClickable(true);
        rideInSession = false;
    }

    public void  showAssignedCustomerProfile()
    {
        TextView txtclose;
        ImageView callBtn;
        ImageView sendSms;
        final EditText textMessage;

        myDialog = new Dialog(DriverMapActivity.this);
        myDialog.setContentView(R.layout.custompopup_customer_details);

        callBtn = (ImageView)myDialog.findViewById(R.id.callbutton);
        sendSms = (ImageView)myDialog.findViewById(R.id.sendSms);
        textMessage = (EditText)myDialog.findViewById(R.id.message);
        txtclose =(TextView) myDialog.findViewById(R.id.txtclose);

        name = (TextView) myDialog.findViewById(R.id.customerName);
        phone = (TextView)myDialog.findViewById(R.id.customerPhone);
        pickup = (TextView)myDialog.findViewById(R.id.pickupPoint);
        destination = (TextView)myDialog.findViewById(R.id.destinationPoint);

        customerName = savedCustomerName.getString("customerName");
        customerPhone = savedCustomerPhone.getString("customerPhone");
        //pickupName = savedPickup.getString("pickupPoint");
        destinationName = savedDestination.getString("destinationPoint");

        //Update Dialog Alert Popup UI Appropriately
        name.setText(customerName);     //Customer Name
        phone.setText(customerPhone);   //Customer Phone
        //pickup.setText(pickupName);     //Pickup Location Name
        destination.setText(destinationName);  //Destination Location Name

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
                callIntent.setData(Uri.parse("tel:" + customerPhone));

                if (ActivityCompat.checkSelfPermission(DriverMapActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED)
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
                    sms_manager.sendTextMessage(customerPhone, null, Message, null, null);
                    textMessage.setText("");
                    Toast.makeText(getApplicationContext(), "Message sent", Toast.LENGTH_LONG).show();
                }
                catch (Exception ex){
                    Toast.makeText(getApplicationContext(), ex.getMessage().toString(), Toast.LENGTH_LONG).show();
                }
            }
        });


        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialog.show();
    }

    public void showRideDetails()
    {
        rideDetailsDialog = new Dialog(DriverMapActivity.this);
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
                rideDetailsDialog.dismiss();
                defaultUI();
            }
        });


        rideCost = (TextView) rideDetailsDialog.findViewById(R.id.cost);
        distanceCovered =(TextView) rideDetailsDialog.findViewById(R.id.distance);

        distanceOfRide = getTotalDistanceTravelled();

        String doubleDistance = Double.toString(distanceOfRide);

        rideCost.setText(costOfRide + " Ksh");
        distanceCovered.setText(doubleDistance + " Km");

        rideDetailsDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        rideDetailsDialog.show();
    }

    public void showAssignedCustomerLocation()
    {
        int heightPerson = 35;
        int widthPerson = 30;
        BitmapDrawable bitmapdrawPerson =(BitmapDrawable)getResources().getDrawable(R.mipmap.person);
        Bitmap bPerson = bitmapdrawPerson.getBitmap();
        Bitmap smallPerson = Bitmap.createScaledBitmap(bPerson, widthPerson, heightPerson, false);

        markerCustomerLocation = mMap.addMarker(new MarkerOptions().position(customerPickupLocation).title("Customer Location").icon(BitmapDescriptorFactory.fromBitmap(smallPerson)));  //Add Marker, and Set Title of Marker

        mMap.moveCamera(CameraUpdateFactory.newLatLng(customerPickupLocation));
    }

    public void hideAssignedCustomerLocation()
    {
        markerCustomerLocation.remove();
    }

    public double getTotalDistanceTravelled()
    {
        double distanceTravelled;

        //Driver Co-ordinates
        Location endOfRideDriverLocation = new Location("");

        endOfRideDriverLocation.setLatitude(endOfRideLocation.latitude);  //Latitude when Ride Ends
        endOfRideDriverLocation.setLongitude(endOfRideLocation.longitude); //Longitude when Ride Ends

        Location startOfRideDriverLocation = new Location("");

        startOfRideDriverLocation.setLatitude(startOfRideLocation.latitude);
        startOfRideDriverLocation.setLongitude(startOfRideLocation.longitude);

        distanceTravelled = endOfRideDriverLocation.distanceTo(startOfRideDriverLocation);

        return distanceTravelled;  //Holds Length of Distance Travelled
    }

    public void cancelRideRequestConfirmationAlert()
    {
        //Pop up an Alert Dialog to confirm End of ride
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Cancel Request?");
        dialog.setMessage("Confirm you want to Cancel Request?");
        dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt)
            {
                //sendUserData.sendRideCompeteNotification(getBaseContext(), requestId, driverPhoneNumber);   //currentLatitudeLongitude is current Driver Location
                customerInformation.setVisibility(View.GONE);
                endOfSession.setVisibility(View.GONE);
                rideInSession = false;
                readyToStartRide = false;
                hideAssignedCustomerLocation(); //Hide Customer Location Marker
                clearRouteFromMap();  //Clear Route From Map
            }
        });

        dialog.setNegativeButton("No", new DialogInterface.OnClickListener()
        {

            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt)
            {
                //Do Nothing
                // rideInSession = true;
            }
        });

        dialog.show();

    }

    public void endOfRideConfirmationAlert()
    {
        //Pop up an Alert Dialog to confirm End of ride
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("End Session?");
        dialog.setMessage("Confirm you want to end Session?");
        dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt)
            {
                endOfRideLocation = currentLocation; //Pick Exact Coordinates of when Ride Stopped
                double distanceCovered = getTotalDistanceTravelled(); //Get Total Distance Travelled during Ride

                sendUserData.sendRideCompeteNotification(getBaseContext(), requestId, distanceCovered, currentDriverLocation);   //currentLatitudeLongitude is current Driver Location

                //customerInformation.setVisibility(View.GONE);
                int backgroundColour = Color.parseColor("#40E0D0");
                driverMainButton.setBackgroundColor(backgroundColour);
                driverMainButton.setVisibility(View.VISIBLE);
                driverMainButton.setClickable(true);
                driverMainButton.setText("Available");
                endOfSession.setVisibility(View.INVISIBLE);
                customerInformation.setVisibility(View.INVISIBLE);
                rideInSession = false;
                readyToStartRide = false;
                hideAssignedCustomerLocation(); //Hide Customer Marker
                clearRouteFromMap();  //Clear Route From Marker
            }
        });

        dialog.setNegativeButton("No", new DialogInterface.OnClickListener()
        {

            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt)
            {
                //Do Nothing
                // rideInSession = true;
            }
        });

        dialog.show();

    }

    public void drawRouteBetweenPickupAndDestination(LatLng pickUpPointCoordinates, LatLng destinationCoordinates)
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

    public void checkIfLocationHasChangedConsiderably()
    {
        String localRequestId = "UPDATE";
        Location previousLoc = new Location("");

        if(previousLocation != null)  //Avoid Null Pointer Exception
        {
            previousLoc.setLatitude(previousLocation.latitude);
            previousLoc.setLongitude(previousLocation.longitude);
        }

        Location currentLoc = new Location("");
        if(currentLocation != null)  //Avoid Null Pointer Exception
        {
            currentLoc.setLatitude(currentLocation.latitude);
            currentLoc.setLongitude(currentLocation.longitude);
        }

        float differenceInDistance = 0;

        differenceInDistance = previousLoc.distanceTo(currentLoc);

        if(differenceInDistance > 200)
        {
           // sendUserData.sendRideRequestAccepted(getBaseContext(), localRequestId, driverPhoneNumber, currentDriverLocation);
        }

    }

    @Override
    public void onLocationChanged(Location location)  //Will be called every second
    {
        int heightCar = 40;
        int widthCar = 35;
        BitmapDrawable bitmapdrawCar =(BitmapDrawable)getResources().getDrawable(R.mipmap.car);
        Bitmap bCar = bitmapdrawCar.getBitmap();
        Bitmap smallCar = Bitmap.createScaledBitmap(bCar, widthCar, heightCar, false);


        if(!locationDataCopied)
        {
            lastLocation = location;  //Copy the Data

            LatLng initLatLang = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
            currentDriverLocation = initLatLang;
            markerDriverLocation = mMap.addMarker(new MarkerOptions().position(initLatLang).title("My Current Location").icon(BitmapDescriptorFactory.fromBitmap(smallCar)));  //Add Marker, and Set Title of Marker

            locationDataCopied = true;
            mMap.moveCamera(CameraUpdateFactory.newLatLng(initLatLang));

        }

        else if(lastLocation != location)
        {
            if(rideInSession)  //Only recentre when Driver is in Motion
            {
                markerDriverLocation.remove();
                lastLocation = location;

                LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                currentDriverLocation = currentLatLng;

                markerDriverLocation = mMap.addMarker(new MarkerOptions().position(currentLatLng).title("My Current Location").icon(BitmapDescriptorFactory.fromBitmap(smallCar)));  //Add Marker, and Set Title of Marker
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

        if(selectedChoice == 1)
        {
            showAssignedCustomerLocation();
            drawRouteBetweenPickupAndDestination(currentDriverLocation, customerPickupLocation);
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

        if(readyToStartRide)
        {
            sendUserData.sendPeriodicDriverLocationToCustomer(getBaseContext(), requestId, driverPhoneNumber, currentDriverLocation);   //Send every 2.5s Driver Location
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
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
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    @Override
    protected void onStop()  //If Driver gets out of this activity, Notify Db, for Him to be removed as Hes is no longer Active
    {
        super.onStop();
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
        for (int i = 0; i <route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);

            //Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": Distance: "+ route.get(i).getDistanceValue()+": Duration- "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onRoutingCancelled()
    {

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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
                Intent intent4 = new Intent(getBaseContext(), driverRegister.class);
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

}


