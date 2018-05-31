package com.gitz.jeff.andrew.uberclone;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class userType extends AppCompatActivity
{
    TinyDB saveUserType;
    TinyDB savedRegistrationComplete;
    int userType = 0;
    int customerUser = 1;                              // 1 For Customer, 2 For Driver;
    int driverUser = 2;                                // 1 For Customer, 2 For Driver;
    private static final int MY_PERMISSIONS_REQUEST_ACCOUNTS = 1;
    boolean gpsEnabled = false;                       //Must be true for one to use app
    boolean mobileDataEnabled = false;                //Must be true for one to use app
    TinyDB savedGpsStatus;  //Save gps status
    TinyDB savedMobileDataStatus;
    TinyDB loginStatus;                               //Will save a boolean value representing registration status
    boolean loginSuccessful = false;
    Button customer;
    Button driver;

    private static userType inst;
    public static userType instance()
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
        setContentView(R.layout.activity_user_type);
        saveUserType= new TinyDB(getBaseContext());
        savedGpsStatus = new TinyDB(getBaseContext());
        savedMobileDataStatus = new TinyDB(getBaseContext());
        savedRegistrationComplete = new TinyDB(getBaseContext());
        loginStatus = new TinyDB(getBaseContext());
        loginSuccessful = loginStatus.getBoolean("loginStatus");                           //Get True or false
        customer = (Button) findViewById(R.id.customerType);
        driver = (Button) findViewById(R.id.driverType);


        if (Build.VERSION.SDK_INT < 19)
        {
            //No Need to check for permissions
        }
        else
        {
            checkAndRequestPermission();
            boolean permissionsGranted =  checkAndRequestPermission();
            if(permissionsGranted)
            {
                goToSavedUserTypeActivity();  //Go to saved activity
            }
        }

        customer.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                runPrerequisiteChecks(getBaseContext());  //Check Boolean Values

                if(mobileDataEnabled && gpsEnabled)
                {
                    saveUserType.putInt("usesType", customerUser);  //Save that the User is a customer
                    Intent intent = new Intent(getBaseContext(), customerRegister.class);
                    startActivity(intent);
                }
            }
        });


        driver.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                runPrerequisiteChecks(getBaseContext());  //Check Boolean Values

                if(mobileDataEnabled && gpsEnabled)
                {
                    saveUserType.putInt("usesType", driverUser);  //Save that the User is a customer
                    Intent intent = new Intent(getBaseContext(), driverRegister.class);
                    startActivity(intent);
                }
            }

        });

    }

    public void runPrerequisiteChecks(Context context)
    {
        /*Check Mobile Data/Wifi Connectivity*/
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();


        if (activeNetwork != null  && activeNetwork.isAvailable() && activeNetwork.isConnected())
        {

            //Connected to Internet either via Mobile Data or Wifi
            if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE || activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)
            {
                mobileDataEnabled= true;
            }
        }


        /*Check GPS Connectivity*/
        LocationManager locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled;
        boolean network_enabled;

        gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if(gps_enabled && network_enabled)     //Connected successfully to GPS
        {
            gpsEnabled = true;
        }


        if(!gpsEnabled)  //If gps is not enabled, prompt alert
        {
            displayToast(getBaseContext(), "Please Enable GPS Location to use this App");
        }

        if(!mobileDataEnabled)
        {
            displayToast(getBaseContext(), "Please Turn ON Mobile Data to use this App");
        }

    }


    public void goToSavedUserTypeActivity()
    {
        runPrerequisiteChecks(getBaseContext());

        Log.e("loginUser", ""+loginSuccessful);
        Log.e("logingps", ""+gpsEnabled);
        Log.e("logindata", ""+mobileDataEnabled);
        Log.e("loginSuccs", ""+loginSuccessful);

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



    public void setGpsboolean()
    {
       gpsEnabled = true;
    }

    public void resetGpsboolean()
    {
        gpsEnabled = false;
    }

    public void setMobiledataboolean()
    {
        mobileDataEnabled = true;
    }

    public void resetMobiledataboolean()
    {
        mobileDataEnabled = false;
    }

    private boolean checkAndRequestPermission()
    {
        // Manifest.permission.READ_CONTACTS, Manifest.permission.READ_SMS, Manifest.permission.READ_PHONE_STATE
        int permissionAccessFineLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int permissionAccessCoarseLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        int permissionMakePhoneCall = ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE);
        int permissionSendSms = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS);


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

        if(permissionSendSms != PackageManager.PERMISSION_GRANTED)
        {
            listPermissionsNeeded.add(Manifest.permission.SEND_SMS);
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

    @Override
    public  void onBackPressed()
    {
        finish();
        super.onBackPressed();
    }

    public static  void displayToast(Context myContext, String displayToastMessage)
    {
        Toast.makeText(myContext, displayToastMessage, Toast.LENGTH_LONG).show();
    }
}
