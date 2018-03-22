package com.gitz.jeff.andrew.uberclone;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class userType extends AppCompatActivity
{
    TinyDB saveUserType;
    TinyDB savedRegistrationComplete;
    int userType = 0;
    int registrationStatus = 0; //0 is Default, 1 is when registration already done
    int customerUser = 1;  // 1 For Customer, 2 For Driver;
    int driverUser = 2;    // 1 For Customer, 2 For Driver;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_type);

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
}
