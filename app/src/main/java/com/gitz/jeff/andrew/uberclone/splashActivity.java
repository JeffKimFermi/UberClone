package com.gitz.jeff.andrew.uberclone;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.logging.Handler;

public class splashActivity extends AppCompatActivity
{
    private static int splashTimeOut = 1200;   //500mS Timeout

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new android.os.Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                Intent intent = new Intent(getBaseContext(), userType.class);
                startActivity(intent);
                finish();
            }
        }, splashTimeOut);
    }
}
