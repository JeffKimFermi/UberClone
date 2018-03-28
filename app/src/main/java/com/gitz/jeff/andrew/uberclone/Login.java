package com.gitz.jeff.andrew.uberclone;

import android.content.Intent;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.app.AlertDialog;
import android.widget.Toast;

import dmax.dialog.SpotsDialog;

public class Login extends AppCompatActivity
{

    EditText loginPhone;
    EditText loginPassword;
    TinyDB savedUserType;
    int userType = 0;    //Whether Use is a Customer or a Driver
    Button login;
    private static final int  displayTime = 2500;  //Alert DialogBox Display Time


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        login = (Button)findViewById(R.id.login);
        savedUserType = new TinyDB(getBaseContext());

    }


    public void login(View view)
    {
        final AlertDialog alertDialog = new SpotsDialog(Login.this, R.style.customLogin);  //Display Alert for 4 Seconds before going to next Activity
        alertDialog.show();

        loginPhone = (EditText)findViewById(R.id.phoneNumber);
        loginPassword = (EditText)findViewById(R.id.loginPass);
        userType = savedUserType.getInt("usesType");

        String enteredPhoneNumber = loginPhone.getText().toString().trim();
        String enteredPassword = loginPassword.getText().toString().trim();

        Toast.makeText(getBaseContext(), enteredPhoneNumber, Toast.LENGTH_LONG).show();
        Toast.makeText(getBaseContext(), enteredPassword, Toast.LENGTH_LONG).show();


        sendUserData.sendLoginRequest(getBaseContext(), enteredPhoneNumber, enteredPassword);  //Use Phone Number as ID and Password

        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {

                if(userType == 1) //If Customer in Use
                {
                    Intent intent = new Intent(getBaseContext(), CustomerMapActivity.class);
                    startActivity(intent);
                    alertDialog.dismiss();  //Dismiss it after 4 seconds

                }

                else if(userType == 2) //If Driver in Use
                {
                    Intent intent = new Intent(getBaseContext(), DriverMapActivity.class);
                    startActivity(intent);
                    alertDialog.dismiss();  //Dismiss it after 4 seconds
                }
            }
        }, displayTime);

    }

}
