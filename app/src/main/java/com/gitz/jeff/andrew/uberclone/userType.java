package com.gitz.jeff.andrew.uberclone;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

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


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_type);



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
        if(registrationStatus == 1) //If user has already logged In Before
        {
            userType = savedRegistrationComplete.getInt("usesType");

            if(userType == 1)   //If Customer Initially signed In
            {
                Intent intent = new Intent(getBaseContext(), CustomerMapActivity.class);
                startActivity(intent);
                return;   //Fucking exit
            }

            else if(userType == 2)  //If Driver Initially signed in
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

    public void customerType(View view)
    {
        saveUserType.putInt("usesType", customerUser);  //Save that the User is a customer
        Intent intent = new Intent(getBaseContext(), registerActivity.class);
        startActivity(intent);
    }

    public void driverType(View view)
    {
        saveUserType.putInt("usesType", driverUser);  //Save that the User is a customer
        Intent intent = new Intent(getBaseContext(), driverRegister.class);
        startActivity(intent);
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

    @Override    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
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
