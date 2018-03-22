package com.gitz.jeff.andrew.uberclone;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class Login extends AppCompatActivity
{

    EditText loginPhone;
    EditText loginPassword;
    TinyDB savedUserType;
    int userType = 0;    //Whether Use is a Customer or a Driver


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        savedUserType = new TinyDB(getBaseContext());
    }

    public void login(View view)
    {
        loginPhone = (EditText)findViewById(R.id.phoneNumber);
        loginPassword = (EditText)findViewById(R.id.loginPass);
        userType = savedUserType.getInt("usesType");

        String enteredPhoneNumber = loginPhone.getText().toString().trim();
        String enteredPassword = loginPassword.getText().toString().trim();

        if(userType == 1) //If Customer in Use
        {
            Intent intent = new Intent(getBaseContext(), CustomerMapActivity.class);
            startActivity(intent);
        }

        else if(userType == 2) //If Driver in Use
        {
            Intent intent = new Intent(getBaseContext(), DriverMapActivity.class);
            startActivity(intent);
        }
    }
}
