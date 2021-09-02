package com.example.driverapp.Service;

import android.content.Intent;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.driverapp.CustomerCall;
import com.example.driverapp.Model.Token;
import com.example.driverapp.R;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

public class MyFirebaseMessaging extends FirebaseMessagingService {


    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        updateTokenToServer(refreshedToken);
    }

    private void updateTokenToServer(String refreshedToken) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference tokens = db.getReference("Tokens");

        Token token = new Token(refreshedToken);


        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            tokens.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(token);
        }
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {




        String string = remoteMessage.getNotification().getTitle();

        if (string.equals("rejectedNotice")) {
            Intent acceptIntent = new Intent("rider");
            acceptIntent.putExtra("msg", "reject");
            sendBroadcast(acceptIntent);
        }
        else if(string.equals("riderUid"))
        {
            Intent riderIntent = new Intent("riderUid");
            riderIntent.putExtra("id", remoteMessage.getNotification().getBody());
            sendBroadcast(riderIntent);
        }
        else if(string.equals("submit"))
        {
            Intent submit = new Intent("submit");
            int amount = Integer.parseInt(remoteMessage.getNotification().getBody());
            submit.putExtra("amt", amount);
            sendBroadcast(submit);
        }
        else {

            LatLng customer_location = new Gson().fromJson(remoteMessage.getNotification().getBody(), LatLng.class);
            Intent intent = new Intent(getBaseContext(), CustomerCall.class);

            intent.putExtra("lat", customer_location.latitude);
            intent.putExtra("lng", customer_location.longitude);
            intent.putExtra("customer", remoteMessage.getNotification().getTitle());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            startActivity(intent);
        }
    }
}
