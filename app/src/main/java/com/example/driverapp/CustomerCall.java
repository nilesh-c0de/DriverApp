package com.example.driverapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.driverapp.Common.Global;
import com.example.driverapp.Model.FCMResponse;
import com.example.driverapp.Model.Notification;
import com.example.driverapp.Model.Sender;
import com.example.driverapp.Model.Token;
import com.example.driverapp.Remote.IFCMService;
import com.example.driverapp.Remote.IGoogleAPI;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.auth.FirebaseAuth;
import com.skyfishjy.library.RippleBackground;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomerCall extends AppCompatActivity {

    GoogleMap map;
    private IFCMService service;
    private IGoogleAPI mService;
    private Geocoder geocoder;

    double lat, lng;
    private String customerId;

    private MediaPlayer mediaPlayer;
    private RippleBackground rippleBackground;

    private BroadcastReceiver broadcastReceiver;
    private TextView textTime;
    private TextView textDistance;
    private TextView textAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_call);

        // getting retrofit client
        service = Global.getFCMService();
        mService = Global.getGoogleAPI();

        geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

        //get the bottom sheet view
        //LinearLayout linearLayout = findViewById(R.id.request_bottom_sheet);
        //init the bottom sheet view
        //BottomSheetBehavior bottomSheetBehaviorRequest = BottomSheetBehavior.from(linearLayout);
        //call bottom sheet
        //bottomSheetBehaviorRequest.setState(BottomSheetBehavior.STATE_EXPANDED);

        // map fragment
        //SupportMapFragment mapFragment = new SupportMapFragment();
        //getSupportFragmentManager().beginTransaction().replace(R.id.map, mapFragment).commit();
       // mapFragment.getMapAsync(this);

        // setting time, distance and address
        textTime = findViewById(R.id.textTime);
        textDistance = findViewById(R.id.textDistance);
        textAddress = findViewById(R.id.textAddress);

        // accept or decline
        Button btnAccept = findViewById(R.id.btnAccept);
        Button btnDecline = findViewById(R.id.btnDecline);

        // playing sound
        mediaPlayer = MediaPlayer.create(this, R.raw.ringtone);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        // broadcastreceiver for cancelled request by rider
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (intent.getAction().equals("rider")) {
                    String text = intent.getStringExtra("msg");

                    if (text != null) {
                        if (text.equals("reject")) {
                            Toast.makeText(getApplicationContext(), "rider cancelled!", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                }

            }
        };

        // code if driver accepts request
        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String driverUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                Token token = new Token(customerId);
                Notification notification = new Notification("Accepted", driverUid);
                Sender sender = new Sender(token.getToken(), notification);

                service.sendMessege(sender).enqueue(new Callback<FCMResponse>() {
                    @Override
                    public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                        if (response.body().success == 1) {
                            Toast.makeText(getApplicationContext(), "Trip accepted!", Toast.LENGTH_SHORT).show();
                            //finish();
                        }
                    }

                    @Override
                    public void onFailure(Call<FCMResponse> call, Throwable t) {

                    }
                });
                Intent intent = new Intent(CustomerCall.this, ShowRouteActivity.class);
                intent.putExtra("lat", lat);
                intent.putExtra("lng", lng);
                intent.putExtra("customerId", customerId);
                startActivity(intent);
                finish();
            }
        });

        // code if driver rejects request
        btnDecline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!TextUtils.isEmpty(customerId)) {
                    Toast.makeText(getApplicationContext(), "Cancelled!", Toast.LENGTH_SHORT).show();

                    cancelRequest(customerId);
                }
            }
        });

        // getting lat, lng and rider id
        if (getIntent() != null) {
            lat = getIntent().getDoubleExtra("lat", -0.1);
            lng = getIntent().getDoubleExtra("lng", -0.1);
            customerId = getIntent().getStringExtra("customer");

            String address = getAddress(lat, lng);
            if(address!=null)
                textAddress.setText(address);

            //getData(lat, lng);
        }



        startTimer();

    }
    private String getAddress(double latitude, double longitude) {

        List<Address> addressList;

        try {

            addressList = geocoder.getFromLocation(latitude, longitude, 1);

            String address = addressList.get(0).getAddressLine(0);
            String city = addressList.get(0).getLocality();
            String state = addressList.get(0).getAdminArea();
            String country = addressList.get(0).getCountryName();
            String postalCode = addressList.get(0).getPostalCode();
            String knownName = addressList.get(0).getFeatureName();


            return knownName + ", " + city;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void startTimer() {
    }

//    private void getData(double lat, double lng) {
//
//
//        String requestApi = null;
//
//        requestApi = "https://maps.googleapis.com/maps/api/directions/json?" +
//                "mode=driving&" +
//                "transit_routing_preferences=less_driving&" +
//                "origin=" + currentPosition.latitude + "," + currentPosition.longitude + "&" +
//                "destination=" + lat + "," + lng + "&" +
//                "key=" + getResources().getString(R.string.google_directions_api);
//
//        mService.getPath(requestApi).enqueue(new Callback<String>() {
//            @Override
//            public void onResponse(Call<String> call, Response<String> response) {
//
//                JSONObject jsonObject = null;
//                try {
//
//                    jsonObject = new JSONObject(response.body().toString());
//                    JSONArray routes = jsonObject.getJSONArray("routes");
//
//                    JSONObject object = routes.getJSONObject(0);
//
//                    JSONArray legs = object.getJSONArray("legs");
//
//                    JSONObject legsObject= legs.getJSONObject(0);
//
//                    // distance
//                    JSONObject distance = legsObject.getJSONObject("distance");
//                    textDistance.setText(distance.getString("text"));
//
//                    // time
//                    JSONObject time = legsObject.getJSONObject("duration");
//                    textTime.setText(time.getString("text"));
//
//                    // address
//                    String address = legsObject.getString("end_address");
//                    textAddress.setText(address);
//
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//
//
//            }
//
//            @Override
//            public void onFailure(Call<String> call, Throwable t) {
//
//            }
//        });
//
//
//    }

    private void cancelRequest(String customerId) {

        Token token = new Token(customerId);

        Notification notification = new Notification("Rejected", "Driver has cancelled your request!");
        Sender sender = new Sender(token.getToken(), notification);

        service.sendMessege(sender)
                .enqueue(new Callback<FCMResponse>() {
                    @Override
                    public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                        if (response.body().success == 1) {
                            Toast.makeText(getApplicationContext(), "Cancelled!", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                    @Override
                    public void onFailure(Call<FCMResponse> call, Throwable t) {

                    }
                });
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    protected void onStop() {
        super.onStop();

        // stop music
        mediaPlayer.release();

        // unregister receiver
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // pause sound
        mediaPlayer.release();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // start sound
        if (mediaPlayer != null)
            mediaPlayer.start();

        // register receiver
        registerReceiver(broadcastReceiver, new IntentFilter("rider"));
    }

}
