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

    //Send Customer Registration Details
    public static void sendUserRegistrationCredentials(Context myContext, final String userType, final String userFullNames, final String userPhoneNumber, final String userConfirmedPassword)
    {
        final TinyDB saveRegistrationResponse = new TinyDB(myContext);
        final TinyDB registrationStatus = new TinyDB(myContext);  //Will save a boolean value representing registration status

        final Context context= myContext;
        JSONObject jsonObj = new JSONObject();
        try
        {

            jsonObj.put("userType", userType);
            jsonObj.put("userName", userFullNames); // Set the first name/pair
            jsonObj.put("userPhone", userPhoneNumber);
            jsonObj.put("userPassword", userConfirmedPassword);
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

                        String registrationResponse = response.toString();
                        if(registrationResponse.matches("registration Successfuly Added"))
                        {
                            registrationSuccessful= true;                //Set boolean value
                        }

                        else if(registrationResponse.matches("registration unsuccessful"))
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
                        String loginResponse = response.toString();
                        if(loginResponse.equals("User Added"))
                        {
                            booleanLogin = true;
                        }

                        else if(loginResponse.equals("Login unsuccessful"))
                        {
                            booleanLogin = false;
                        }

                        else if (loginResponse.equals("User already exists"))
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
    public static void sendEventData(Context myContext, final String userId, final String eventID, LatLng customerPickUpLocation, LatLng customerDestination, LatLng driverCurrentLocation)
    {
        final Context context= myContext;
        JSONObject jsonObj = new JSONObject();
        try
        {
            jsonObj.put("userID", userId);
            jsonObj.put("eventID", eventID); // Set the first name/pair
            jsonObj.put("customerPickUpLocation", customerPickUpLocation);
            jsonObj.put("customerDestination", customerDestination);
            jsonObj.put("driverCurrentLocation", driverCurrentLocation);

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
                        String serverResponse = response.toString();
                        Log.e("Response", response.toString());
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



    //Send Driver Registration Details
    /*
    public static void sendDriverRegestrationCredentials(Context myContext,final String userType, final String userFullNames, final String userPhoneNumber, final String userConfirmedPassword)
    {
        final Context context= myContext;
        JSONObject jsonObj = new JSONObject();
        try
        {

            jsonObj.put("userType", userType);
            jsonObj.put("userName", userFullNames); // Set the first name/pair
            jsonObj.put("userPhone", userPhoneNumber);
            jsonObj.put("userPassword", userConfirmedPassword);
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
                        //Get Response
                        String serverResponse = response.toString());
                        // display response
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

*/

}

