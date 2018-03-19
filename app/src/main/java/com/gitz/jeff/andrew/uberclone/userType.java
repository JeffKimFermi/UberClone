package com.gitz.jeff.andrew.uberclone;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class userType extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_type);
    }

    public void customerType(View view)
    {
        Intent intent = new Intent(getBaseContext(), registerActivity.class);
        startActivity(intent);
    }

    public void driverType(View view)
    {
        Intent intent = new Intent(getBaseContext(), registerActivity.class);
        startActivity(intent);
    }
}
