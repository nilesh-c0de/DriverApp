package com.example.driverapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.chip.Chip;

public class FeedbackActivity extends AppCompatActivity {

    BroadcastReceiver broadcastReceiver;
    ImageView imgDone;
    ProgressBar progressBar;
    TextView txtMesg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        setTitle("Trip Ended");

        final TextView fareBase = findViewById(R.id.fare_base);
        final TextView fareLyft = findViewById(R.id.fare_lyft);
        final TextView fareTax = findViewById(R.id.fare_tax);
        final TextView fareDiscount = findViewById(R.id.fare_discount);
        final TextView fareTotal = findViewById(R.id.fare_total);

        imgDone = findViewById(R.id.img_done);
        progressBar = findViewById(R.id.pbb);
        txtMesg = findViewById(R.id.text_msg);

        final Button ap = findViewById(R.id.btn_ap);

        ap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), MapActivity.class));
            }
        });

        //final Chip chip = findViewById(R.id.collect_cash);

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if(intent.getAction().equals("submit"))
                {

                    ap.setEnabled(true);
                    imgDone.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.INVISIBLE);
                    txtMesg.setText("Payment done!");

                    int amt = intent.getIntExtra("amt", 0);

                    int lyft = amt-(20+2+0);
                    fareBase.setText(fareBase.getText().toString()+" 20");
                    fareLyft.setText(fareLyft.getText().toString()+lyft);
                    fareTax.setText(fareTax.getText().toString()+" 2");
                    fareDiscount.setText(fareDiscount.getText().toString()+" 0");
                    fareTotal.setText(fareTotal.getText().toString()+amt);



                }
            }
        };

        registerReceiver(broadcastReceiver, new IntentFilter("submit"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }
}
