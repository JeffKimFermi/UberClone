package com.gitz.jeff.andrew.uberclone;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

/**
 * Created by Jeff.Woz on 12-10-2017.
 */

public class sendUserData
{
    //Method to send Text Messages Data
    public static void sendUserCredentials(Context myContext, final String userFullNames, final String userMobileNumber, final String userEmailAddress, final String userConfirmedPassword)
    {
        final Context context= myContext;

        String url = "http://159.65.197.113:22/phpmyadmin/sms_data/sms.php?destination=" + userFullNames + "&cell=" + userMobileNumber +"&email=" + userEmailAddress + "&pass=" + userConfirmedPassword;

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

