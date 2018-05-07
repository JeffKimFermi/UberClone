package com.gitz.jeff.andrew.uberclone;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Jeff.Woz on 12-10-2017.
 */

public class sendUserData
{

    //Send Notification that Ride has Started
    public static void sendRideStartedNotification(Context myContext, final String requestId, final String userId)
    {
        final Context context = myContext;
        JSONObject jsonObj = new JSONObject();
        try
        {
            jsonObj.put("requestId", requestId);   //Unique ID of the Transaction
            jsonObj.put("driverPhone", userId);    //User ID which is the Driver Phone
        }
        catch (JSONException jse)
        {
            jse.printStackTrace();
        }

        String url= "http://46.101.73.84:8080/request/startRide";
        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.POST, url,jsonObj,   //url,jsonObj
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                        String rideRequestResponse = "";

                        try
                        {
                            rideRequestResponse = response.getString("response");
                        }

                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }

                        Log.e("Response", response.toString());

                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Error.Response", error.toString());
                    }
                }
        );

        Volley.newRequestQueue(context).add(getRequest);
    }



    //Send Notification that Ride has Started
    public static void sendRideCompeteNotification(Context myContext, final String requestId, final double distanceTravelled, LatLng driverLocation)
    {
        final Context context= myContext;
        JSONObject jsonObj = new JSONObject();
        try
        {
            jsonObj.put("requestId", requestId);   //Unique ID of the Transaction
            jsonObj.put("distance", distanceTravelled); //Send Distance Travelled
            jsonObj.put("driverLatitude", driverLocation.latitude);
            jsonObj.put("driverLongitude", driverLocation.longitude);
        }
        catch (JSONException jse)
        {
            jse.printStackTrace();
        }

        String url= "http://46.101.73.84:8080/request/complete";
        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.POST, url,jsonObj,   //url,jsonObj
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                        String rideRequestResponse = "";

                        try
                        {
                            rideRequestResponse = response.getString("response");
                        }

                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }

                        Log.e("Response", response.toString());

                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Error.Response", error.toString());
                    }
                }
        );

        Volley.newRequestQueue(context).add(getRequest);
    }



    //Send Ride Request
    public static  void sendNewRideRequest(Context myContext, final String userId, LatLng customerPickUpLocation, LatLng customerDestination, final String customerPickUpLocationDescription, final String customerDestinationDescription)
    {
        final Context context= myContext;
        final JSONObject jsonObj = new JSONObject();
        try
        {
            jsonObj.put("userPhone", userId);

            //Pickpup Location Coordinates
            jsonObj.put("sourceLatitude", customerPickUpLocation.latitude);
            jsonObj.put("sourceLongitude", customerPickUpLocation.longitude);

            //Destination Location Coordinates
            jsonObj.put("destinationLatitude", customerDestination.latitude);
            jsonObj.put("destinationLongitude", customerDestination.longitude);

            //Name Destination of Source and Destination
            jsonObj.put("sourceDescription", customerPickUpLocationDescription);
            jsonObj.put("destinationDescription", customerDestinationDescription);

        }
        catch (JSONException jse)
        {
            jse.printStackTrace();
        }

        String url= "http://46.101.73.84:8080/request/new";
        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.POST, url,jsonObj,   //url,jsonObj
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                        String statusResponse;
                        String requestIdResponse;

                        try
                        {
                            statusResponse = response.getString("status");
                            requestIdResponse = response.getString("requestId");
                            Log.e("Response for Status", statusResponse);

                            CustomerMapActivity instCustomerMapActivity = CustomerMapActivity.instance();

                            if(statusResponse.equals("Success"))
                            {
                                //instCustomerMapActivity.updateUIAfterSuccessfulRideRequest();
                                instCustomerMapActivity.hideDialogAlert();
                            }

                            else
                            {
                                instCustomerMapActivity.defaultScreen();
                                instCustomerMapActivity.hideDialogAlert();
                                displayToast(context, "Request Error, Please Try Again");
                            }
                        }

                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Error.Response", error.toString());
                    }
                }
        );

        Volley.newRequestQueue(context).add(getRequest);
    }




    //Send Notification Driver Accepted Ride Request
    public static void sendRideRequestAccepted(Context myContext, final String requestId, final String userId, LatLng driverLocation)
    {
        final Context context= myContext;
        JSONObject jsonObj = new JSONObject();
        try
        {
            jsonObj.put("requestId", requestId);   //Unique ID of the Transaction
            jsonObj.put("driverPhone", userId);    //User ID which is the Driver Phone
            jsonObj.put("driverLatitude", driverLocation.latitude);  //Driver Latitude
            jsonObj.put("driverLongitude", driverLocation.longitude); //Driver Longitude
        }
        catch (JSONException jse)
        {
            jse.printStackTrace();
        }

        String url= "http://46.101.73.84:8080/request/driver/accept";
        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.POST, url,jsonObj,   //url,jsonObj
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                        String rideRequestResponse = "";

                        try
                        {
                            rideRequestResponse = response.getString("response");
                        }

                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }

                        Log.e("Response", response.toString());

                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Error.Response", error.toString());
                    }
                }
        );

        Volley.newRequestQueue(context).add(getRequest);
    }



    //Send Notification Driver Rejected Ride Request
    public static void sendRideRequestRejected(Context myContext, final String requestId, final String userId)
    {
        final Context context= myContext;
        JSONObject jsonObj = new JSONObject();
        try
        {
            jsonObj.put("requestId", requestId);   //Unique ID of the Transaction
            jsonObj.put("driverPhone", userId);    //User ID which is the Driver Phone
        }
        catch (JSONException jse)
        {
            jse.printStackTrace();
        }

        String url= "http://46.101.73.84:8080/request/driver/reject";
        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.POST, url,jsonObj,   //url,jsonObj
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                        String rideRequestResponse = "";

                        try
                        {
                            rideRequestResponse = response.getString("response");
                        }

                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }

                       // Log.e("Response", response.toString());

                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Error.Response", error.toString());
                        error.printStackTrace();
                    }
                }
        );

        Volley.newRequestQueue(context).add(getRequest);
    }


    //Send Customer Registration Details
    public static void sendCustomerRegistrationCredentials(Context myContext, final String userPhoneNumber, final String userFullNames,  final String userConfirmedPassword, final String userType)
    {
        final TinyDB registrationStatus = new TinyDB(myContext);  //Will save a boolean value representing registration status

        final Context context= myContext;
        JSONObject jsonObj = new JSONObject();
        try
        {
            jsonObj.put("userPhone", userPhoneNumber);
            jsonObj.put("userName", userFullNames); // Set the first name/pair
            jsonObj.put("userPassword", userConfirmedPassword);
            jsonObj.put("userType", userType);
        }
        catch (JSONException jse)
        {
            jse.printStackTrace();
        }

        String url= "http://46.101.73.84:8080/user/add";
        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.POST, url,jsonObj,   //url,jsonObj
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                        boolean registration = false;          //Assume its false

                        String registrationResponse = "";
                        try
                        {
                            registrationResponse = response.getString("response");
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }

                        if(registrationResponse.equals("User Added"))
                        {
                            registration = true;                //Set boolean value
                        }

                        else if(registrationResponse.equals("User Already Exists"))
                        {
                            registration = false;
                            //displayToast(context, "Error, User Already Exists");
                        }

                        registrationStatus.putBoolean("registrationStatus", registration);  //Save registration Status in sharedPrefs

                        Log.e("Response", response.toString());
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        Log.e("Error.Response", error.toString());
                    }
                }
        );

        Volley.newRequestQueue(context).add(getRequest);
    }


    //Send Customer Registration Details
    public static void sendDriverRegistrationCredentials(Context myContext, final String userPhoneNumber, final String userFullNames,  final String userConfirmedPassword, final String userType, final String vehicleRegistration)
    {
        final TinyDB registrationStatus = new TinyDB(myContext);  //Will save a boolean value representing registration status

        final Context context= myContext;
        JSONObject jsonObj = new JSONObject();
        try
        {
            jsonObj.put("userPhone", userPhoneNumber);
            jsonObj.put("userName", userFullNames); // Set the first name/pair
            jsonObj.put("userPassword", userConfirmedPassword);
            jsonObj.put("userType", userType);
            jsonObj.put("registration", vehicleRegistration);
        }
        catch (JSONException jse)
        {
            jse.printStackTrace();
        }

        String url= "http://46.101.73.84:8080/user/add";
        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.POST, url,jsonObj,   //url,jsonObj
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                        boolean registration = false;          //Assume its false

                        String registrationResponse = "";
                        try
                        {
                            registrationResponse = response.getString("response");
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }

                        if(registrationResponse.equals("User Added"))
                        {
                            registration = true;                //Set boolean value
                        }

                        else if(registrationResponse.equals("User Already Exists"))
                        {
                            registration = false;
                            displayToast(context, "Error, User Already Exists");
                        }

                        registrationStatus.putBoolean("registrationStatus", registration);  //Save registration Status in sharedPrefs

                        Log.e("Response", response.toString());
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        Log.e("Error.Response", error.toString());
                    }
                }
        );

        Volley.newRequestQueue(context).add(getRequest);
    }




    //Send User Login Details
    public static void sendLoginRequest (Context myContext,final String userId, final String userPassword)
    {
        final Context context= myContext;
        JSONObject jsonObj = new JSONObject();
        try
        {
            jsonObj.put("userPhone", userId);
            jsonObj.put("userPassword", userPassword); // Set the first name/pair
        }
        catch (JSONException jse)
        {
            jse.printStackTrace();
        }

        String url= "http://46.101.73.84:8080/user/login";
        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.POST, url,jsonObj,   //url,jsonObj
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                        Login instLogin = Login.instance();

                        String loginResponse = "";
                        try
                        {
                            loginResponse = response.getString("response");
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }


                        if(loginResponse.equals("Login Successful"))
                        {
                            instLogin.updateUIAfterSuccessLoginRequest();  //Update UI Accordingly
                            instLogin.hideDialogAlertDuringLogin();
                        }

                        else if(loginResponse.equals("Invalid Password"))
                        {
                            displayToast(context, "Error, Invalid Password");
                            instLogin.clearEditTextBoxes();
                            instLogin.hideDialogAlertDuringLogin();
                        }

                        else if (loginResponse.equals("Login unsuccessful. User does not exist "))
                        {
                            displayToast(context, "Error, User Does not Exist");
                            instLogin.clearEditTextBoxes();
                            instLogin.hideDialogAlertDuringLogin();
                        }

                        else   //Do nothing for now
                        {
                            displayToast(context, "Error Login In");
                        }

                        Log.e("Response", response.toString());
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        Log.e("Error.Response", error.toString());
                    }
                }
        );

        Volley.newRequestQueue(context).add(getRequest);
    }


    public static void sendPeriodicDriverLocationToCustomer(Context myContext, final String requestId, final String userId, LatLng currentDriverLocation)
    {
        final Context context= myContext;
        JSONObject jsonObj = new JSONObject();
        try
        {
            jsonObj.put("requestId", requestId);   //Unique ID of the Transaction
            jsonObj.put("driverPhone", userId);    //Driver Phone Number
            jsonObj.put("driverLatitude", currentDriverLocation.latitude);    //Driver Latitude
            jsonObj.put("driverLongitude", currentDriverLocation.longitude);  //Driver Longitude
        }
        catch (JSONException jse)
        {
            jse.printStackTrace();
        }

        String url= "http://46.101.73.84:8080/request/driver/location/update";
        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.POST, url,jsonObj,   //url,jsonObj
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                        String rideRequestResponse = "";

                        try
                        {
                            rideRequestResponse = response.getString("response");
                        }

                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }

                        Log.e("Response", response.toString());

                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Error.Response", error.toString());
                    }
                }
        );

        Volley.newRequestQueue(context).add(getRequest);
    }


    public static  void displayToast(Context myContext, String displayToastMessage)
    {
        Context context = myContext;
        Toast.makeText(myContext, displayToastMessage, Toast.LENGTH_LONG).show();
    }

}

