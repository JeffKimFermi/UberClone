package com.gitz.jeff.andrew.uberclone;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;


public class Help extends AppCompatActivity {
    ImageButton sendBtn;
    EditText txtphone_no;
    EditText txtmsg;
    String phone_No = "0721512564";   //Gitz Number
    ImageView callCustomerSupport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        sendBtn = (ImageButton) findViewById(R.id.send_btn);
        txtphone_no = (EditText) findViewById(R.id.phone_no);
        txtmsg = (EditText) findViewById(R.id.message);
        txtphone_no.setText("Queencia Customer Support");
        txtphone_no.setEnabled(false);
        callCustomerSupport = (ImageView)findViewById(R.id.callCustomerCare);

        callCustomerSupport.setOnClickListener(new View.OnClickListener()    //Call Customer Listener
        {
            @Override
            public void onClick(View v) {
                String customerNumber = "0735555255";
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + customerNumber));

                if (ActivityCompat.checkSelfPermission(Help.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED)
                {

                    return;
                }
                startActivity(callIntent);

            }
        });
    }

    public void send_msg (View view)
    {
        String Message = txtmsg.getText().toString();

        try {
            SmsManager sms_manager = SmsManager.getDefault();
            sms_manager.sendTextMessage(phone_No, null, Message, null, null);
            txtphone_no.setText("");
            txtmsg.setText("");
            Toast.makeText(getApplicationContext(), "Message sent", Toast.LENGTH_LONG).show();
        }
        catch (Exception ex){
            Toast.makeText(getApplicationContext(), ex.getMessage().toString(), Toast.LENGTH_LONG).show();
        }
    }

}


