package com.gitz.jeff.andrew.uberclone;

import android.content.Context;
import android.util.Log;

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

    //Send Event Data
    public static void sendRideRequest(Context myContext, final String userId, LatLng customerPickUpLocation, LatLng customerDestination, final String customerPickUpLocationDescription, final String customerDestinationDescription)
    {
        final Context context= myContext;
        JSONObject jsonObj = new JSONObject();
        try
        {
            jsonObj.put("userPhone", userId);
            jsonObj.put("sourceLatitudeLongitude", customerPickUpLocation);
            jsonObj.put("destinationLatitudeLongitude", customerDestination);
            jsonObj.put("sourceDescription", customerPickUpLocationDescription);
            jsonObj.put("destinationDescription", customerDestinationDescription);

        }
        catch (JSONException jse)
        {
            jse.printStackTrace();
        }

        String url= "http://46.101.73.84:8080/request/add";
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

                        if(rideRequestResponse.equals("User Added"))
                        {
                            registrationSuccessful= true;                //Set boolean value
                        }

                        else if(rideRequestResponse.equals("User Already Exists"))
                        {
                            registrationSuccessful = false;
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

    //Send Customer Registration Details
    public static void sendCustomerRegistrationCredentials(Context myContext, final String userPhoneNumber, final String userFullNames,  final String userConfirmedPassword, final String userType)
    {
        final TinyDB saveRegistrationResponse = new TinyDB(myContext);
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
                        boolean registrationSuccessful = false;          //Assume its false

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
                            registrationSuccessful= true;                //Set boolean value
                        }

                        else if(registrationResponse.equals("User Already Exists"))
                        {
                            registrationSuccessful = false;
                        }

                        registrationStatus.putBoolean("registrationStatus", registrationSuccessful);  //Save registration Status in sharedPrefs
                        saveRegistrationResponse.putString("registrationResponse", registrationResponse);

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
        final TinyDB saveRegistrationResponse = new TinyDB(myContext);
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
                        boolean registrationSuccessful = false;          //Assume its false

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
                            registrationSuccessful= true;                //Set boolean value
                        }

                        else if(registrationResponse.equals("User Already Exists"))
                        {
                            registrationSuccessful = false;
                        }

                        registrationStatus.putBoolean("registrationStatus", registrationSuccessful);  //Save registration Status in sharedPrefs
                        saveRegistrationResponse.putString("registrationResponse", registrationResponse);

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
        final TinyDB loginStatus = new TinyDB(myContext);

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
                        boolean booleanLogin = false;

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
                            booleanLogin = true;
                        }

                        else if(loginResponse.equals("Invalid Password"))
                        {
                            booleanLogin = false;
                        }

                        else if (loginResponse.equals("User does not Exist"))
                        {
                            booleanLogin = false;
                        }


                        else if (loginResponse.equals("Login Unsuccessful, user does not Exist"))
                        {
                            booleanLogin = false;
                        }

                        else   //Do nothing for now
                        {
                            booleanLogin = true;
                        }

                        loginStatus.putBoolean("loginStatus",  booleanLogin);
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



    //Send Event Data
    public static void sendEventData(Context myContext, final String userId, final String eventID, LatLng customerPickUpLocation, LatLng customerDestination)
    {
        final Context context= myContext;
        JSONObject jsonObj = new JSONObject();
        try
        {
            jsonObj.put("userID", userId);
            jsonObj.put("eventID", eventID); // Set the first name/pair
            jsonObj.put("customerPickUpLocation", customerPickUpLocation);
            jsonObj.put("customerDestination", customerDestination);

        }
        catch (JSONException jse)
        {
            jse.printStackTrace();
        }

        String url= "http://date.jsontest.com";
        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.POST, url,jsonObj,   //url,jsonObj
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                        String serverResponse = "";

                        try
                        {
                            serverResponse = response.getString("response");
                        }

                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }

                        if(userId.equals("taxiRequest"))                //If Taxi requested
                        {
                            if(serverResponse.equals("driverFound"))
                            {

                            }

                            else
                            {}
                        }

                        else if(userId.equals("cancelRequest"))
                        {
                            if(serverResponse.equals("cancelRequestSuccessful"))
                            {

                            }

                            else
                            {}
                        }

                        else if(userId.equals("startSession"))
                        {
                            if(serverResponse.equals("sessionStarted"))
                            {

                            }

                            else
                            {}
                        }

                        else if(userId.equals("endSession"))
                        {
                            if(serverResponse.equals("sessionEnded"))
                            {

                            }

                            else
                            {}
                        }

                        else if(userId.equals("callCustomer"))
                        {
                            if(serverResponse.equals("ok"))
                            {

                            }

                            else
                            {}
                        }

                        else if(userId.equals("callDriver"))
                        {
                            if(serverResponse.equals("ok"))
                            {

                            }

                            else
                            {}
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

}

