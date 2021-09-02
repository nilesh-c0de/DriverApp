package com.example.driverapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.driverapp.Common.Global;
import com.example.driverapp.Model.Driver;
import com.example.driverapp.Model.FCMResponse;
import com.example.driverapp.Model.Notification;
import com.example.driverapp.Model.Sender;
import com.example.driverapp.Model.Token;
import com.example.driverapp.Remote.IFCMService;
import com.example.driverapp.Remote.IGoogleAPI;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShowRouteActivity extends AppCompatActivity implements OnMapReadyCallback {

    GoogleMap map;
    private IFCMService service;
    private IGoogleAPI mService;
    private String customerId;
    private double lat, lng;

    private ImageView profileImage1, profileImage2;
    private TextView username1, username2;

    private BroadcastReceiver broadcastReceiver;

    private List<LatLng> polyLineList;
    private PolylineOptions polylineOptions, blackPolylineOptions;
    private Polyline blackPolyline, greyPolyline;
    private LatLng riderLoc;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_route);

        // getting retrofit client
        service = Global.getFCMService();
        mService = Global.getGoogleAPI();

        // map fragment
        SupportMapFragment mapFragment = new SupportMapFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.map, mapFragment).commit();
        mapFragment.getMapAsync(this);

        // getting references
        final Button btnArrived = findViewById(R.id.btn_arrived);
        Button btnStartTrip = findViewById(R.id.btn_start_trip);
        Button btnCompleteTrip = findViewById(R.id.btn_complete_trip);

        //get the bottom sheet view
        LinearLayout linearLayoutRequest = findViewById(R.id.route_bottom_sheet);
        LinearLayout linearLayoutStartTrip = findViewById(R.id.trip_bottom_sheet);
        LinearLayout linearLayoutCompleteTrip = findViewById(R.id.complete_trip_bottom_sheet);

        //init the bottom sheet view
        final BottomSheetBehavior bottomSheetBehaviorRequest = BottomSheetBehavior.from(linearLayoutRequest);
        BottomSheetBehavior bottomSheetBehaviorStartTrip = BottomSheetBehavior.from(linearLayoutStartTrip);
        final BottomSheetBehavior bottomSheetBehaviorCompleteTrip = BottomSheetBehavior.from(linearLayoutCompleteTrip);

        //call bottom sheet
        bottomSheetBehaviorRequest.setState(BottomSheetBehavior.STATE_EXPANDED);

        // taking references
        profileImage1 = findViewById(R.id.route_rider_image);
        username1 = findViewById(R.id.route_username);
        profileImage2 = findViewById(R.id.completed_rider_image);
        username2 = findViewById(R.id.completed_username);

        // getting lat lng riderid
        if (getIntent() != null) {

            lat = getIntent().getDoubleExtra("lat", -0.1);
            lng = getIntent().getDoubleExtra("lng", -0.1);
            customerId = getIntent().getStringExtra("customerId");

            riderLoc = new LatLng(lat, lng);

            // getting routes
            // getDirections(new LatLng(currentLocation.latitude, currentLocation.longitude), new LatLng(riderLoc.latitude, riderLoc.longitude));
        }

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (intent.getAction().equals("riderUid")) {
                    String text = intent.getStringExtra("id");

                    Log.i("lol", text);

                    FirebaseDatabase.getInstance().getReference("riderData").child(text).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Driver butRider = dataSnapshot.getValue(Driver.class);

                            String url = butRider.getImageUri();
                            String name = butRider.getFullName();
                            if (!url.isEmpty() && !name.isEmpty()) {

                                Picasso.with(getApplicationContext()).load(url).into(profileImage1);
                                Picasso.with(getApplicationContext()).load(url).into(profileImage2);

                                username1.setText(name);
                                username2.setText(name);

                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }
        };


        btnStartTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Token token = new Token(customerId);
                Notification notification = new Notification("Notice", "trip");
                Sender sender = new Sender(token.getToken(), notification);

                service.sendMessege(sender).enqueue(new Callback<FCMResponse>() {
                    @Override
                    public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                        if (response.body().success == 1) {
                            Toast.makeText(getApplicationContext(), "Your trip starts now!", Toast.LENGTH_SHORT).show();
                            //finish();
                        }
                    }

                    @Override
                    public void onFailure(Call<FCMResponse> call, Throwable t) {

                    }
                });

                // bottomSheetBehaviorCompleteTrip.setState(BottomSheetBehavior.STATE_EXPANDED);
                // bottomSheetBehaviorRequest.setState(BottomSheetBehavior.STATE_COLLAPSED);
                // bottomSheetBehaviorStartTrip.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });

        btnCompleteTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Token token = new Token(customerId);
                Notification notification = new Notification("Completed", "completed");
                Sender sender = new Sender(token.getToken(), notification);

                service.sendMessege(sender).enqueue(new Callback<FCMResponse>() {
                    @Override
                    public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                        if (response.body().success == 1) {
                            Toast.makeText(getApplicationContext(), "Your trip is completed!", Toast.LENGTH_SHORT).show();
                            //finish();
                        }
                    }

                    @Override
                    public void onFailure(Call<FCMResponse> call, Throwable t) {

                    }
                });
                startActivity(new Intent(getApplicationContext(), FeedbackActivity.class));// older was FeedbackActivity,again changed to FeedbackActivity
            }
        });

        btnArrived.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String btnText = btnArrived.getText().toString();

                if (btnText.equals("Start Trip")) {
                    btnArrived.setText("Confirm you have arrived");

                    Token token = new Token(customerId);
                    Notification notification = new Notification("Trip", "trip");
                    Sender sender = new Sender(token.getToken(), notification);

                    service.sendMessege(sender).enqueue(new Callback<FCMResponse>() {
                        @Override
                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                            if (response.body().success == 1) {
                                Toast.makeText(getApplicationContext(), "Your trip starts now!", Toast.LENGTH_SHORT).show();

                                //getDirections(new LatLng(riderLoc.latitude, riderLoc.longitude), new LatLng(riderDest.latitude, riderDest.longitude));
                                //finish();
                            }
                        }

                        @Override
                        public void onFailure(Call<FCMResponse> call, Throwable t) {

                        }
                    });

                    bottomSheetBehaviorCompleteTrip.setState(BottomSheetBehavior.STATE_EXPANDED);
                    bottomSheetBehaviorRequest.setState(BottomSheetBehavior.STATE_COLLAPSED);
                } else {
                    btnArrived.setText("Start Trip");

                    Token token = new Token(customerId);
                    Notification notification = new Notification("Arrived", "arrived");
                    Sender sender = new Sender(token.getToken(), notification);

                    service.sendMessege(sender).enqueue(new Callback<FCMResponse>() {
                        @Override
                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                            if (response.body().success == 1) {
                                Toast.makeText(getApplicationContext(), "Arrival message sent successfully!", Toast.LENGTH_SHORT).show();
                                //finish();
                            }
                        }

                        @Override
                        public void onFailure(Call<FCMResponse> call, Throwable t) {

                        }
                    });
                }


                // bottomSheetBehaviorRequest.setState(BottomSheetBehavior.STATE_COLLAPSED);
                // bottomSheetBehaviorStartTrip.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(broadcastReceiver, new IntentFilter("riderUid"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    // drawing route between two markers
    private void getDirections(LatLng currentPosition, LatLng destination) {

        String requestApi = null;

        requestApi = "https://maps.googleapis.com/maps/api/directions/json?" +
                "mode=driving&" +
                "transit_routing_preferences=less_driving&" +
                "origin=" + currentPosition.latitude + "," + currentPosition.longitude + "&" +
                "destination=" + destination.latitude + "," + destination.longitude + "&" +
                "key=" + getResources().getString(R.string.google_directions_api);

        mService.getPath(requestApi).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {


                try {
                    JSONObject jsonObject;
                    jsonObject = new JSONObject(response.body());
                    JSONArray jsonArray = jsonObject.getJSONArray("routes");

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject route = jsonArray.getJSONObject(i);
                        JSONObject poly = route.getJSONObject("overview_polyline");
                        String polyline = poly.getString("points");
                        polyLineList = decodePoly(polyline);

                    }

                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    for (LatLng latLng : polyLineList)
                        builder.include(latLng);

                    LatLngBounds bounds = builder.build();
                    CameraUpdate mCameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 2);
                    map.animateCamera(mCameraUpdate);

                    polylineOptions = new PolylineOptions();
                    polylineOptions.color(Color.GRAY);
                    polylineOptions.width(5);
                    polylineOptions.startCap(new SquareCap());
                    polylineOptions.endCap(new SquareCap());
                    polylineOptions.jointType(JointType.ROUND);
                    polylineOptions.addAll(polyLineList);
                    greyPolyline = map.addPolyline(polylineOptions);

                    blackPolylineOptions = new PolylineOptions();
                    blackPolylineOptions.color(Color.BLACK);
                    blackPolylineOptions.width(5);
                    blackPolylineOptions.startCap(new SquareCap());
                    blackPolylineOptions.endCap(new SquareCap());
                    blackPolylineOptions.jointType(JointType.ROUND);
                    //blackPolylineOptions.addAll(polyLineList);
                    blackPolyline = map.addPolyline(blackPolylineOptions);

                    map.addMarker(new MarkerOptions()
                            .position(polyLineList.get(polyLineList.size() - 1))
                            .title("Pickup Location"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });
    }

    // decoding
    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        LatLng latLng = new LatLng(lat, lng);
        map.addMarker(new MarkerOptions().position(latLng).title("Rider is here"));
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));
    }
}
