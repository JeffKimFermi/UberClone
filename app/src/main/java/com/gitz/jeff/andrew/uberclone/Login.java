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
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    public void login(View view)
    {
        loginPhone = (EditText)findViewById(R.id.phoneNumber);
        loginPassword = (EditText)findViewById(R.id.loginPass);

        String enteredPhoneNumber = loginPhone.getText().toString().trim();
        String enteredPassword = loginPassword.getText().toString().trim();

        Intent intent  = new Intent(getBaseContext(), CustomerMapActivity.class);
        startActivity(intent);
    }
}
