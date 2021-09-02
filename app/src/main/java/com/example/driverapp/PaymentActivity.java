package com.example.driverapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class PaymentActivity extends AppCompatActivity {


    BroadcastReceiver broadcastReceiver;
    TextView txtMesg;
    ImageView imgDone;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);


        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals("submit"))
                {
                    imgDone.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.INVISIBLE);
                    txtMesg.setText("Payment done!");
                }
            }
        };


        registerReceiver(broadcastReceiver, new IntentFilter("sumbit"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }
}
