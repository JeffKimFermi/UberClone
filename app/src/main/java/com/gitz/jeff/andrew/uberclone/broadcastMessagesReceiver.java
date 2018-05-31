package com.gitz.jeff.andrew.uberclone;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;

import java.util.ArrayList;


/**
 * Created by Jeff Kims  on 05-24-2017.
 */

public class broadcastMessagesReceiver extends BroadcastReceiver
{

    @Override
    public void onReceive(Context context, Intent intent)
    {
        String action = intent.getAction(); //Get action that triggered BroadcastReceiver
        userType instUserType = userType.instance();

        //Network Adapter Enabled
        if (action.equals("android.net.conn.CONNECTIVITY_CHANGE")) //If network adapter caused the action
        {

            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            if (activeNetwork != null  && activeNetwork.isAvailable() && activeNetwork.isConnected())
            {

                //Connected to Internet either via Mobile Data or Wifi
                if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE || activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)
                {
                    if(instUserType != null)
                    {
                        instUserType.setMobiledataboolean();  //Set Mobile Network Boolean
                    }
                }

            }

            else   //Reset connected to network boolean to false
            {
                if(instUserType != null)
                {
                    instUserType.resetMobiledataboolean();  //Set Mobile Network Boolean
                }
            }


        }

        if (intent.getAction().matches("android.location.PROVIDERS_CHANGED"))
        {
            LocationManager locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
            boolean gps_enabled;
            boolean network_enabled;

            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if(!gps_enabled && !network_enabled)     //Connected successfully to GPS
            {
                if(instUserType != null)
                {
                    instUserType.resetGpsboolean();
                }
            }

            else   //Reset gpsEnabled boolean to false
            {
                if (instUserType != null)
                {
                    instUserType.setGpsboolean();
                }

            }
        }


    }

}





