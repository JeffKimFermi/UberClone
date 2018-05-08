package com.gitz.jeff.andrew.uberclone;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class newCustomerPopup extends AppCompatActivity
{
    Dialog  dialog;
    TinyDB saveSelectedChoice;
    public int choiceSelected = 5; // 1 for Accept, 2 for Reject, 3 for Cancel


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_customer_popup);

        saveSelectedChoice = new TinyDB(getBaseContext());

        newCustomerAlertPopup();
    }

    public void newCustomerAlertPopup()
    {
        TextView txtclose;
        TextView pickupLocation;
        TextView destinationLocation;

        Button acceptRequest;
        Button rejectRequest;

        dialog = new Dialog(newCustomerPopup.this);
        dialog.setContentView(R.layout.newcustomeralert);

        txtclose = (TextView)dialog.findViewById(R.id.txtclose);
        pickupLocation = (TextView)dialog.findViewById(R.id.pickup);
        destinationLocation = (TextView)dialog.findViewById(R.id.destination);

        acceptRequest = (Button)dialog.findViewById(R.id.accept);
        rejectRequest = (Button)dialog.findViewById(R.id.reject);

        Intent i = getIntent();
        String pickupPoint = i.getStringExtra("pickup");
        String destinationPoint = i.getStringExtra("destination");
        pickupLocation.setText(pickupPoint);
        destinationLocation.setText(destinationPoint);

        txtclose.setText("X");

        txtclose.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                choiceSelected = 3;
                saveSelectedChoice.putInt("select", choiceSelected);

                dialog.dismiss();

                Intent intent = new Intent(getBaseContext(), DriverMapActivity.class);
                startActivity(intent);
            }
        });

        acceptRequest.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                choiceSelected = 1;
                saveSelectedChoice.putInt("select", choiceSelected);

                dialog.dismiss();

                Intent intent = new Intent(getBaseContext(), DriverMapActivity.class);
                startActivity(intent);
            }
        });


        rejectRequest.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                choiceSelected = 2;
                saveSelectedChoice.putInt("select", choiceSelected);
                dialog.dismiss();

                Intent intent = new Intent(getBaseContext(), DriverMapActivity.class);
                startActivity(intent);
            }
        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

    }

}
