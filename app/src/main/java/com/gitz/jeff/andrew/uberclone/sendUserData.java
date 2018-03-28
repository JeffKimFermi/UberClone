package com.gitz.jeff.andrew.uberclone;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

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
                        String registrationResponse = response.toString();
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
    //Send User Login Details
    public static void sendLoginRequest (Context myContext,final String userId, final String userPassword)
    {
        final TinyDB saveLoginRequestResponse = new TinyDB(myContext);

        final Context context= myContext;
        JSONObject jsonObj = new JSONObject();
        try
        {
            jsonObj.put("userID", userId);
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
                        String loginResponse = response.toString();
                        saveLoginRequestResponse.putString("loginResponse", loginResponse);
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
    public static void sendEventData(Context myContext,final String userId, final String eventID)
    {
        final Context context= myContext;
        JSONObject jsonObj = new JSONObject();
        try
        {
            jsonObj.put("userID", userId);
            jsonObj.put("eventID", eventID); // Set the first name/pair
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

