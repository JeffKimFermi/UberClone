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
    public static void sendCustomerRegestrationCredentials(Context myContext, final String startOfFrame, final String userFullNames, final String userMobileNumber, final String userEmailAddress, final String userConfirmedPassword, final String endOfFrame)
    {
        final Context context= myContext;

        String url = "http://159.65.197.113:22/phpmyadmin/sms_data/sms.php?destination="+ startOfFrame + "&name" + userFullNames + "&cell=" + userMobileNumber +"&email=" + userEmailAddress + "&pass=" + userConfirmedPassword + "&eof" + endOfFrame;

        StringRequest getRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response)
                    {
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

        //request_json.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 1, 1.0f));

        Volley.newRequestQueue(context).add(getRequest);

    }

    //Send Driver Registration Details
    public static void sendDriverRegestrationCredentials(Context myContext, final String startOfFrame, final String userFullNames, final String userMobileNumber, final String userEmailAddress, final String userConfirmedPassword, final String endOfFrame)
    {
        final Context context= myContext;
        JSONObject jsonObj = new JSONObject();
        try
        {

            jsonObj.put("name", startOfFrame); // Set the first name/pair
            jsonObj.put("surname", userFullNames);
        }
        catch (JSONException jse)
        {
            jse.printStackTrace();
        }
        String url= "http://date.jsontest.com";
        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url,null, //url,jsonObj
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response)
                    {
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

        //request_json.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 1, 1.0f));

        Volley.newRequestQueue(context).add(getRequest);

    }



    public static void sendEventData(Context myContext, final String startOfFrame, final String userType, final String phoneNumnberIdentifier, final String eventData, final String endOfFrame)
    {
        final Context context= myContext;

        String url = "http://159.65.197.113:22/phpmyadmin/sms_data/sms.php?destination="  +startOfFrame + "&userType" + userType + "&userIdentifier=" + phoneNumnberIdentifier +"&event=" + eventData + "&eof=" + endOfFrame ;

        StringRequest getRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response)
                    {
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

        //request_json.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 1, 1.0f));

        Volley.newRequestQueue(context).add(getRequest);

    }
}

