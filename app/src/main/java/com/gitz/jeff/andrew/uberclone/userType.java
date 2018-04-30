package com.gitz.jeff.andrew.uberclone;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class userType extends AppCompatActivity
{
    TinyDB saveUserType;
    TinyDB savedRegistrationComplete;
    int userType = 0;
    int customerUser = 1;                            // 1 For Customer, 2 For Driver;
    int driverUser = 2;                              // 1 For Customer, 2 For Driver;
    private static final int MY_PERMISSIONS_REQUEST_ACCOUNTS = 1;
    boolean gpsEnabled = true;                       //Must be true for one to use app
    boolean mobileDataEnabled = true;                //Must be true for one to use app
    TinyDB loginStatus;                              //Will save a boolean value representing registration status
    boolean loginSuccessful = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_type);


        checkIfMobileDataEnabled(getBaseContext());  //Set gpsEnabled if Everything Ok
        checkIfGPSLocationEnabled(getBaseContext()); //Set mobileDataEnabled if Everything Ok


        if (Build.VERSION.SDK_INT < 19)
        {
            //No Need to check for permissions
        }
        else
        {
            if (checkAndRequestPermission())
            {}
        }

        saveUserType= new TinyDB(getBaseContext());
        savedRegistrationComplete = new TinyDB(getBaseContext());
        loginStatus = new TinyDB(getBaseContext());
        loginSuccessful = loginStatus.getBoolean("loginStatus");                           //Get True or false

        openSavedUserTypeActivity();   //Open Saved User Activity

    }


    public void customerType(View view)
    {
        if(!mobileDataEnabled) //GPS and Mobile Data must be Enabled for one to Proceed to Maps Activity
        {
            checkIfMobileDataEnabled(getBaseContext());
        }

        if(!gpsEnabled)
        {
            checkIfGPSLocationEnabled(getBaseContext());
        }

        if(mobileDataEnabled && gpsEnabled)
        {
            saveUserType.putInt("usesType", customerUser);  //Save that the User is a customer
            Intent intent = new Intent(getBaseContext(), customerRegister.class);
            startActivity(intent);
        }
    }


    public void driverType(View view)
    {

        if(!mobileDataEnabled)
        {
            checkIfMobileDataEnabled(getBaseContext());
        }

        if(!gpsEnabled)
        {
            checkIfGPSLocationEnabled(getBaseContext());
        }

        if(mobileDataEnabled && gpsEnabled)
        {
            saveUserType.putInt("usesType", driverUser);  //Save that the User is a customer
            Intent intent = new Intent(getBaseContext(), driverRegister.class);
            startActivity(intent);
        }
    }


    public void checkIfGPSLocationEnabled(Context context)
    {
        LocationManager locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled;
        boolean network_enabled;

        gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);


        if(!gps_enabled && !network_enabled)
        {
            gpsEnabled = false;   //Set Boolean

            //Notify User
            AlertDialog.Builder dialog = new AlertDialog.Builder(userType.this);
            dialog.setTitle("The GPS is Off");
            dialog.setMessage("GPS Activation necessary for Accurate Tracking.");
            dialog.setPositiveButton("ACTIVATE", new DialogInterface.OnClickListener()

            {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt)
                {
                    Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(myIntent);

                    gpsEnabled = true;  //Set it True
                    openSavedUserTypeActivity();
                }


            });

            dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener()
            {

                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt)
                {
                    Toast.makeText(getBaseContext(), "Please Enable GPS Location to use this App", Toast.LENGTH_LONG).show();
                }
            });

            dialog.show();
        }

    }


    public void checkIfMobileDataEnabled(Context context)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) getBaseContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (activeNetwork == null ||  !activeNetwork.isAvailable() || !activeNetwork.isConnected())  //If Mobile Network not Enabled
        {
            mobileDataEnabled = false;

            //Notify User
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle("Mobile Data Turned Off");
            dialog.setMessage("Internet Connection necessary for Functionality.");
            dialog.setPositiveButton("Enable", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt)
                {
                    Intent myIntent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                    startActivity(myIntent);

                    mobileDataEnabled = true;   //Set it True
                    openSavedUserTypeActivity();
                }
            });

            dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
            {

                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt)
                {
                    Toast.makeText(getBaseContext(), "Please Turn ON Mobile Data to use this App", Toast.LENGTH_LONG).show();
                }
            });

            dialog.show();
        }
    }


    public void openSavedUserTypeActivity()
    {
        if(mobileDataEnabled && gpsEnabled)
        {
            if (loginSuccessful)         //If user successfully logged in before
            {
                userType = savedRegistrationComplete.getInt("usesType");

                if (userType == 1)   //If Customer Initially signed In
                {
                    Intent intent = new Intent(getBaseContext(), CustomerMapActivity.class);
                    startActivity(intent);
                    return;
                }
                else if (userType == 2)  //If Driver Initially signed in
                {
                    Intent intent = new Intent(getBaseContext(), DriverMapActivity.class);
                    startActivity(intent);
                    return;
                }
                else   //Default
                {
                    Intent intent = new Intent(getBaseContext(), CustomerMapActivity.class);
                    startActivity(intent);
                    return;
                }
            }
        }
    }


    public void refreshActivity()
    {
        finish();
        overridePendingTransition(0, 0);
        startActivity(getIntent());
        overridePendingTransition(0, 0);
    }


    private boolean checkAndRequestPermission()
    {
        // Manifest.permission.READ_CONTACTS, Manifest.permission.READ_SMS, Manifest.permission.READ_PHONE_STATE
        int permissionAccessFineLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int permissionAccessCoarseLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        int permissionMakePhoneCall = ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE);

        List<String> listPermissionsNeeded = new ArrayList<>();
        if(permissionAccessFineLocation != PackageManager.PERMISSION_GRANTED)
        {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if(permissionAccessCoarseLocation!= PackageManager.PERMISSION_GRANTED)
        {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        if(permissionMakePhoneCall != PackageManager.PERMISSION_GRANTED)
        {
            listPermissionsNeeded.add(Manifest.permission.CALL_PHONE);
        }

        if (!listPermissionsNeeded.isEmpty())
        {
            ActivityCompat.requestPermissions(this,
                    listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), MY_PERMISSIONS_REQUEST_ACCOUNTS);
            return false;
        }

        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        switch (requestCode)
        {
            case MY_PERMISSIONS_REQUEST_ACCOUNTS:
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {

            }

            break;
        }
    }
}
