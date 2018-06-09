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
import android.location.Criteria;
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
    LatLng initialDriverLocation = new LatLng(0,0);
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

    boolean locationDataCopied = false;
    boolean registrationComplete = false;
    boolean driverAvailable = false;  //Set When Driver is Available

    boolean readyToStartRide = false;
    boolean rideInSession = false;
    boolean rideComplete = false;

    TinyDB savedUserPhoneNumber;
    TinyDB savedRequestId;

    TinyDB savedRegistrationStatus;

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

    int selectedChoice = 0;

    Dialog myDialog;
    Dialog rideDetailsDialog;

    //End Of Journey Parameters
    String costOfRide = "0.0";
    String rideDistance = "0.0";
    String rideTime = "0.0";

    float totalDistanceTravelled = 0;
    float currentDistance = 0;
    float previousDistance = 0;
    long startTime = 0;  //Time between two Locations
    long stopTime = 0;

    Location loc1 = new Location("");
    Location loc2 = new Location("");


    //New Customer Popup
    Dialog  dialog;

    //Button display;
    String errorMsg = "Network Error, Call Customer Support";


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

        loc1.setLatitude(0);
        loc1.setLongitude(0);

        loc2.setLatitude(0);
        loc2.setLongitude(0);

        //display = (Button)findViewById(R.id.distanceDisplay);

        savedUserPhoneNumber = new TinyDB(getBaseContext());

        savedRequestId = new TinyDB(getBaseContext());
        savedRegistrationStatus = new TinyDB(getBaseContext());

        driverPhoneNumber = savedUserPhoneNumber.getString("userPhoneNumber");

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        polylines = new ArrayList<>();

        endOfSession  = (Button)findViewById(R.id.endOfSession);
        endOfSession.setVisibility(View.INVISIBLE);

        customerInformation = (Button)findViewById(R.id.customerInfo);
        customerInformation.setVisibility(View.INVISIBLE);


        driverMainButton = (Button)findViewById(R.id.driverMainButton);

        checkForPushMessagesFromServer();  //Check for Push Messages

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
        if(readyToStartRide == false && rideInSession == false && rideComplete == false) //If Process has started
        {
            boolean available = true;
            String localRequestId = "UPDATE";

            driverMainButton.setText("Checking For Customers...");
            driverMainButton.setBackgroundColor(Color.RED);
            driverMainButton.setTextColor(Color.WHITE);
            previousLocation = currentLocation;  //Pick Coordinates At exactly when driver says he is Available
            sendUserData.sendDriverAvailable(getBaseContext(), driverPhoneNumber, available);  //Send Notification that you are Available
            sendUserData.sendPeriodicDriverLocationToCustomer(getBaseContext(), localRequestId, driverPhoneNumber, currentDriverLocation);   //Send every 2.5s Driver Location


            initialDriverLocation = currentLocation;  //Pick co-ordinated of Initial Driver Location once he/she was available

            driverAvailable = true;
           // readyToStartRide = true; //We are now ready to start ride
            rideInSession = false;
            rideComplete = false;
        }


        else if(readyToStartRide)
        {
            endOfSession.setVisibility(View.VISIBLE);
            endOfSession.setText("End Session?");
            customerInformation.setVisibility(View.GONE);
            driverMainButton.setText("Status: Ride In Session");
            driverMainButton.setClickable(false);

            startOfRideLocation = currentLocation;   //Pick Exact Coordinates of when Ride Started

            clearRouteFromMap();  //Clear Drawn Route to Customer

            if(markerCustomerLocation != null)
            {
                markerCustomerLocation.remove();
            }

            sendUserData.sendRideStartedNotification(getBaseContext(), requestId, driverPhoneNumber);   //currentLatitudeLongitude is current Driver Location

            //Set boolean Values Appropriately
            readyToStartRide = false;
            rideInSession = true;  //We shift to the next state
            rideComplete = false;
        }

        else if(rideComplete)  //If ride Complete
        {
            selectedChoice = 0;  //Reset Selected Choice
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

                    //Get Pickup and Destination Locations Descriptions
                    pickupName = jsonObj.getString("sourceDescription");
                    destinationName = jsonObj.getString("destinationDescription");

                    if(statusResponse.equals("Success"))
                    {
                        runOnUiThread(new Runnable()   //Special Thread to do the work
                        {

                            @Override
                            public void run()
                            {
                                newCustomerAlertPopup();
                            }
                        });
                    }
                    else
                    {}

                }

                catch (Exception e)
                {
                    //displayToast(getBaseContext(), errorMsg);
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
        channel.bind("ride_cancelled", new SubscriptionEventListener()   //Events
        {
            @Override
            public void onEvent(String channelName, String eventName, final String data)
            {
                Log.e("CANCEL RESPONSE", data);

                //Received Messages From Server
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
                    //displayToast(getBaseContext(), errorMsg);
                    e.printStackTrace();
                }
            }
        });


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

                    //requestId = jsonObj.getString("requestId");
                    statusResponse = jsonObj.getString("status");

                    costOfRide = jsonObj.getString("cost");
                    rideTime = jsonObj.getString("rideTime");
                    rideDistance = jsonObj.getString("distance");

                    if(statusResponse.equals("Success"))
                    {
                        rideComplete = true;
                        rideInSession = false;
                        readyToStartRide = false;

                        runOnUiThread(new Runnable()   //Special Thread to do the work
                        {

                            @Override
                            public void run()
                            {
                                driverMainButton.setClickable(true);
                                driverMainButton.setBackgroundColor(Color.RED);
                                driverMainButton.setText("Ride Details");

                                endOfSession.setVisibility(View.INVISIBLE);
                                customerInformation.setVisibility(View.INVISIBLE);

                            }
                        });
                    }
                }

                catch (Exception e)
                {
                    //displayToast(getBaseContext(), errorMsg);
                    e.printStackTrace();
                }


            }
        });



         /*Driver has Accepted Customer's ride Request*/
        channel.bind("registration_completed", new SubscriptionEventListener()   //Events
        {
            @Override
            public void onEvent(String channelName, String eventName, final String data)
            {
                //Received Messages From Server
                Log.e("Registration: ", data);

                JSONObject jsonObj = null;
                String statusResponse;

                try
                {
                    jsonObj = new JSONObject(data);

                    requestId = jsonObj.getString("requestId");
                    statusResponse = jsonObj.getString("status");

                    if(statusResponse.equals("Success"))
                    {
                        registrationComplete = true;
                        savedRegistrationStatus.putBoolean("regStatus", registrationComplete);
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


    public void defaultUI()
    {
        int backgroundColour = Color.parseColor("#40E0D0");
        driverMainButton.setBackgroundColor(backgroundColour);
        driverMainButton.setText("Available");
        driverMainButton.setVisibility(View.VISIBLE); //Not Visible
        driverMainButton.setClickable(true);
        endOfSession.setVisibility(View.INVISIBLE);
        customerInformation.setVisibility(View.INVISIBLE);
        rideInSession = false;
        readyToStartRide = false;
        rideComplete = false;
    }

    public void newCustomerAlertPopup()
    {
        TextView txtclose;
        TextView pickupLocation;
        TextView destinationLocation;

        Button acceptRequest;
        Button rejectRequest;

        dialog = new Dialog(DriverMapActivity.this);
        dialog.setContentView(R.layout.newcustomeralert);
        dialog.setCanceledOnTouchOutside(false);  //Prevent it from disappearing when touches outside
        dialog.setCancelable(false);  //Protection from disappearing on back press

        txtclose = (TextView)dialog.findViewById(R.id.txtclose);
        pickupLocation = (TextView)dialog.findViewById(R.id.pickup);
        destinationLocation = (TextView)dialog.findViewById(R.id.destination);

        acceptRequest = (Button)dialog.findViewById(R.id.accept);
        rejectRequest = (Button)dialog.findViewById(R.id.reject);

        pickupLocation.setText(pickupName);
        destinationLocation.setText(destinationName);


        customerPickupLocation = new LatLng(latitudePickupLocation, longitudePickupLocation);
        customerDestinationLocation = new LatLng(latitudeDestinationLocation, longitudeDestinationLocation);
        currentDriverLocation = new LatLng(latitudeDriverLocation, longitudeDriverLocation);

        txtclose.setText("X");

        txtclose.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //selectedChoice = 3;

                sendUserData.sendRideRequestRejected(getBaseContext(), requestId, driverPhoneNumber);
                defaultUI();

                dialog.dismiss();
            }
        });

        acceptRequest.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //selectedChoice = 1;  //Request Accepted

                sendUserData.sendRideRequestAccepted(getBaseContext(), requestId, driverPhoneNumber, currentDriverLocation);
                driverMainButton.setVisibility(View.VISIBLE);
                driverMainButton.setClickable(true);
                driverMainButton.setText("START SESSION");
                endOfSession.setVisibility(View.VISIBLE);
                endOfSession.setText("Cancel Request?");
                customerInformation.setVisibility(View.VISIBLE);
                readyToStartRide = true;

                dialog.dismiss();
            }
        });


        rejectRequest.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //selectedChoice = 2; //Request Rejected


                sendUserData.sendRideRequestRejected(getBaseContext(), requestId, driverPhoneNumber);
                defaultUI();

                dialog.dismiss();
            }
        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

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


        //Update Dialog Alert Popup UI Appropriately
        name.setText(customerName);     //Customer Name
        phone.setText(customerPhone);   //Customer Phone
        pickup.setText(pickupName);     //Pickup Location Name
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
        rideDetailsDialog.setCanceledOnTouchOutside(false);  //Prevent it from disappearing when touches outside
        rideDetailsDialog.setCancelable(false);  //Protection from disappearing on back press


        TextView txtclose;
        txtclose = (TextView)rideDetailsDialog.findViewById(R.id.txtclose);

        TextView rideCost;
        TextView distanceCovered;
        TextView rideDuration;

        float timeHours = 0;
        float timeMins = 0;

        txtclose.setText("X");

        txtclose.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                rideDetailsDialog.dismiss();  //Dismiss Dialog
                defaultUI();
            }
        });


        rideCost = (TextView) rideDetailsDialog.findViewById(R.id.cost);
        distanceCovered =(TextView) rideDetailsDialog.findViewById(R.id.distance);
        rideDuration = (TextView)rideDetailsDialog.findViewById(R.id.duration);


        float cost = Float.parseFloat(costOfRide);
        float distance = Float.parseFloat(rideDistance);
        float time = Float.parseFloat(rideTime);

        if(time >= 60)
        {
           timeMins = (time%60);
           timeHours = (time/60);

           rideCost.setText(String.format("%.2f", cost) + " Ksh");
           distanceCovered.setText(String.format("%.2f", distance) + " Km");
           rideDuration.setText((String.format("%.2f", timeMins) + " Mins") + (String.format(String.format("%.2f", timeHours) + " Hrs")));

        }

        else  //If Ride more than 1hr
        {
            rideCost.setText(String.format("%.2f", cost) + " Ksh");
            distanceCovered.setText(String.format("%.2f", distance) + " Km");
            rideDuration.setText(String.format("%.2f", timeMins) + " Mins");
        }

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
        if(markerCustomerLocation != null)
        {
            markerCustomerLocation.remove();
        }
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
                sendUserData.sendCancelRideRequest(getBaseContext(), requestId, driverPhoneNumber);   //currentLatitudeLongitude is current Driver Location
                customerInformation.setVisibility(View.GONE);
                endOfSession.setVisibility(View.GONE);
                rideInSession = false;
                readyToStartRide = false;
                hideAssignedCustomerLocation(); //Hide Customer Location Marker
                clearRouteFromMap();  //Clear Route From Map
                defaultUI();
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

                double distanceCovered = totalDistanceTravelled; //Get Total Distance Travelled during Ride
                distanceCovered = distanceCovered/1000; //In Km

                sendUserData.sendRideCompleteNotification(getBaseContext(), requestId, distanceCovered, currentDriverLocation);   //currentLatitudeLongitude is current Driver Location

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

    public void drawRouteBetweenDriverAndPickupLocation(LatLng pickUpPointCoordinates, LatLng destinationCoordinates)
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

        Location initialLoc = new Location("");
        if(initialDriverLocation != null)  //Avoid Null Pointer Exception
        {
            initialLoc.setLatitude(initialDriverLocation.latitude);
            initialLoc.setLongitude(initialDriverLocation.longitude);
        }

        Location currentLoc = new Location("");
        if(currentLocation != null)  //Avoid Null Pointer Exception
        {
            currentLoc.setLatitude(currentLocation.latitude);
            currentLoc.setLongitude(currentLocation.longitude);
        }

        float differenceInDistance;

        if((initialDriverLocation.latitude != 0 &&initialDriverLocation.longitude!= 0) && (currentLocation.latitude != 0 && currentLocation.longitude != 0))
        {
            differenceInDistance = initialLoc.distanceTo(currentLoc);

            if(differenceInDistance > 400) //If greater than 400m
            {
                sendUserData.sendPeriodicDriverLocationToCustomer(getBaseContext(), localRequestId, driverPhoneNumber, currentDriverLocation);   //Send every 2.5s Driver Location
                initialDriverLocation = currentLocation; //Update state of Initial Driver Location
            }
        }

    }

    @Override
    public void onLocationChanged(Location location)  //Will be called every second
    {
        int heightCar = 40;
        int widthCar = 35;
        BitmapDrawable bitmapdrawCar = (BitmapDrawable) getResources().getDrawable(R.mipmap.car);
        Bitmap bCar = bitmapdrawCar.getBitmap();
        Bitmap smallCar = Bitmap.createScaledBitmap(bCar, widthCar, heightCar, false);


        if (!locationDataCopied)
        {
            lastLocation = location;  //Copy the Data

            LatLng initLatLang = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
            currentDriverLocation = initLatLang;
            markerDriverLocation = mMap.addMarker(new MarkerOptions().position(initLatLang).title("My Current Location").icon(BitmapDescriptorFactory.fromBitmap(smallCar)));  //Add Marker, and Set Title of Marker

            locationDataCopied = true;
            mMap.moveCamera(CameraUpdateFactory.newLatLng(initLatLang));

        }
        else if (lastLocation != location)
        {
            if (rideInSession)  //Only recentre when Driver is in Motion
            {
                markerDriverLocation.remove();
                lastLocation = location;

                LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                currentDriverLocation = currentLatLng;

                markerDriverLocation = mMap.addMarker(new MarkerOptions().position(currentLatLng).title("My Current Location").icon(BitmapDescriptorFactory.fromBitmap(smallCar)));  //Add Marker, and Set Title of Marker
                mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLatLng));

            }
        }


        mMap.animateCamera(CameraUpdateFactory.zoomTo(myZoomLevel), new GoogleMap.CancelableCallback()
        {
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


        if (readyToStartRide)   //If assigned successfully
        {
            checkIfLocationHasChangedConsiderably(); //Update Driver Location if Position Changed > 200m
            // sendUserData.sendPeriodicDriverLocationToCustomer(getBaseContext(), requestId, driverPhoneNumber, currentDriverLocation);   //Send every 2.5s Driver Location
        }



        //if (rideInSession)
        //{
        Log.e("disLat", ""+location.getLatitude());
        Log.e("disLong", ""+location.getLongitude());


        if ((loc1.getLatitude() == 0 && loc1.getLongitude() == 0) && (loc2.getLatitude() == 0 && loc2.getLongitude() == 0))  //If First Time
        {
            loc1.setLatitude(location.getLatitude());
            loc1.setLongitude(location.getLongitude());
            startTime = System.currentTimeMillis();  //Pick System Time
        }
        else
        {
            Location location1 = location;

            location1.setLatitude(location.getLatitude());
            location1.setLongitude(location.getLongitude());

            //Error Correction Mechanism
            stopTime = System.currentTimeMillis();
            long timeDifference = (stopTime - startTime)/1000;  //Difference in Seconds

            /*I don't expect someone to drive 130km/hr in Nairobi = 36m/s*/
            long normalDistance = (timeDifference * 36);   //Expected Normal distance at a speed of 130km/hr
            Log.e("disNormal", ""+normalDistance);

            float distanceMetres = loc1.distanceTo(location1); //Formula to calculate Distance
            currentDistance = distanceMetres;

            float deltaDistance = currentDistance - previousDistance;

            if((deltaDistance > 6 || deltaDistance < -6)  || (distanceMetres > normalDistance))  //If covered > 180km/hr
            {
                distanceMetres = previousDistance;  //Pick the Previous distance instead
            }

            if((distanceMetres < 2))   //Assume Stationary if less than 2m/s
            {
                distanceMetres = 0;    //Assign least possible value
            }

            totalDistanceTravelled = (totalDistanceTravelled + distanceMetres);

            //Update previous distance, location and time
            previousDistance = currentDistance;  //Update Distance
            loc1 = location1;  //Update Location
            startTime = stopTime; //Update start Time

           // display.setText(String.format("%.2f", totalDistanceTravelled));  //2 decimal places

            Log.e("disMetres", ""+distanceMetres);
            Log.e("disDelta", ""+deltaDistance);


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

        if(selectedChoice == 1)
        {
            showAssignedCustomerLocation();
            drawRouteBetweenDriverAndPickupLocation(currentDriverLocation, customerPickupLocation);
        }

    }

    @Override
    public void onConnected(@Nullable Bundle bundle)
    {
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


    @Override
    public void onResume()
    {
        requestId = savedRequestId.getString("id");
        super.onResume();
    }

    @Override
    public  void onBackPressed()
    {
        quitAlertPopup();
        // super.onBackPressed();
    }

    public static  void displayToast(Context myContext, String displayToastMessage)
    {
        Context context = myContext;
        Toast.makeText(myContext, displayToastMessage, Toast.LENGTH_LONG).show();
    }


    public void quitAlertPopup()
    {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//***Change Here***
        startActivity(intent);

        /*
        AlertDialog.Builder dialog = new AlertDialog.Builder(DriverMapActivity.this);
        dialog.setTitle("Exit Session?");
        dialog.setMessage("Are you sure you want to exit session");
        dialog.setPositiveButton("YES", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt)
            {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//***Change Here***
                startActivity(intent);
                //finish();  //Kill App
               // System.exit(0);  //Kill App
            }

        });

        dialog.setNegativeButton("NO", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt)
            {
                //Do Nothing
            }
        });

        dialog.show();



        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//***Change Here***
        startActivity(intent);
        */
    }


/*
    if(rideInSession)   //Compute Distance only Once Ride has Started
    {
        //Calculate Total Distance Travelled
        if ((to.latitude == 0 && from.latitude == 0) && (from.latitude == 0 && from.longitude == 0))  //If Initial Point
        {
            from = new LatLng(location.getLatitude(), location.getLongitude());  //Original Location
        }
        else
        {
            to = new LatLng(location.getLatitude(), location.getLongitude());
            Double distance = SphericalUtil.computeDistanceBetween(from, to);
            from = to; //Update value of to
            totalDistanceTravelled = (totalDistanceTravelled + distance);  //Add Initial Value to Current Value

            Log.e("distance", String.valueOf(distance));
            display.setText(String.valueOf(totalDistanceTravelled));

            //totalDistanceTravelled = Math.floor(totalDistanceTravelled*100) / 100;
            //totalDistanceTravelled = (totalDistanceTravelled/1000);  //Send Distance in Km

            //String doubleDistance = Double.toString(totalDistanceTravelled);
            //distanceDisplay.setText(doubleDistance);
        }
    }
*/

}


