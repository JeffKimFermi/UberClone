package com.gitz.jeff.andrew.uberclone;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class settingsApp extends AppCompatActivity
{

    ListView listView;
    String settingsItems[] = new String [] {"Change Login Details", "Change User Type", "Change Font"};
    TinyDB saveUserType;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_app);

        saveUserType= new TinyDB(getBaseContext());

        listView = (ListView)findViewById(R.id.settingsListView);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, settingsItems);

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> Parent, View view, int position, long id)
            {

                switch( position )
                {
                    case 0:
                        if(saveUserType != null)
                        {
                            saveUserType.clear();
                        }
                        Intent intent1 = new Intent(getBaseContext(), registerActivity.class);
                        startActivity(intent1);
                        break;

                    case 1:
                        if(saveUserType != null)
                        {
                            saveUserType.clear();
                        }
                        Intent intent2 = new Intent(getBaseContext(), userType.class);
                        startActivity(intent2);
                        break;
                }

            }

        });
    }
}
