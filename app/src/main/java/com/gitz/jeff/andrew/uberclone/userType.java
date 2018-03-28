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

import com.google.android.gms.maps.SupportMapFragment;

import java.util.ArrayList;
import java.util.List;

import static com.gitz.jeff.andrew.uberclone.R.id.map;

public class userType extends AppCompatActivity
{
    TinyDB saveUserType;
    TinyDB savedRegistrationComplete;
    int userType = 0;
    int registrationStatus = 0; //0 is Default, 1 is when registration already done
    int customerUser = 1;  // 1 For Customer, 2 For Driver;
    int driverUser = 2;    // 1 For Customer, 2 For Driver;
    private static final int MY_PERMISSIONS_REQUEST_ACCOUNTS = 1;
    boolean gpsEnabled = true;       //Must be true for one to use app
    boolean mobileDataEnabled = true; //Must be true for one to use app

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
        registrationStatus = savedRegistrationComplete.getInt("registrationStatus");  //Get status of Registration

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
            Intent intent = new Intent(getBaseContext(), registerActivity.class);
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
            dialog.setMessage("GPS Services Disabled");
            dialog.setPositiveButton("Enable", new DialogInterface.OnClickListener()

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

            dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
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
            AlertDialog.Builder dialog = new AlertDialog.Builder(userType.this);
            dialog.setMessage("Mobile Data Disabled");
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
            if (registrationStatus == 1) //If user has already logged In Before
            {
                userType = savedRegistrationComplete.getInt("usesType");

                if (userType == 1)   //If Customer Initially signed In
                {
                    Intent intent = new Intent(getBaseContext(), CustomerMapActivity.class);
                    startActivity(intent);
                    return;   //Fucking exit
                }
                else if (userType == 2)  //If Driver Initially signed in
                {
                    Intent intent = new Intent(getBaseContext(), DriverMapActivity.class);
                    startActivity(intent);
                    return;   //Fucking exit
                }
                else   //Default
                {
                    Intent intent = new Intent(getBaseContext(), CustomerMapActivity.class);
                    startActivity(intent);
                    return;   //Fucking exit
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
