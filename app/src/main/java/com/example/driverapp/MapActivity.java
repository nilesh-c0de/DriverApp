package com.example.driverapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.driverapp.Model.DataModel;
import com.example.driverapp.Model.Token;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener {


    // constants
    private static final int REQUEST_CHECK_SETTINGS = 1;
    private static final int PERMISSION_CODE = 2;
    private static final int GALLERY_PICK = 3;
    // google map and location variables
    private GoogleMap map;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location mCurrentLocation;
    private Marker mCurrentPosiMarker;

    private SharedPreferences.Editor editor;
    private DrawerLayout drawerLayout;
    private Switch location_switch;
    private Uri imageUri, imgUri;
    private TextView textHeaderName, textHeaderEmail;
    private ImageView pImage;

    private DatabaseReference driver, mRef, mRefToDriverData;
    private GeoFire geoFire;
    private StorageReference mStorageRef;
    private boolean firstTime = false;

    private int i;
    private long backPressedTime;
    private Toast backToast;

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        //toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // drawer
        drawerLayout = findViewById(R.id.drawer_layout);
        // navigation view
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // initializing fusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // map fragment
        SupportMapFragment mapFragment = new SupportMapFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.map, mapFragment).commit();
        mapFragment.getMapAsync(this);

        // firebase initialization
        FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
        driver = mDatabase.getReference("onlineDriver");
        mRef = mDatabase.getReference().child("online");
        mRefToDriverData = mDatabase.getReference().child("driverData");
        mStorageRef = FirebaseStorage.getInstance().getReference();

        // shared preferences initialization
        SharedPreferences sharedPreferences = getSharedPreferences("userData", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        //SharedPreferences settings = getSharedPreferences("iValue", 0);
        //SharedPreferences.Editor editor = settings.edit();

        // setting imageuri to null
        imageUri = null;
        // header reference
        View view = navigationView.getHeaderView(0);
        textHeaderName = view.findViewById(R.id.header_username);
        textHeaderEmail = view.findViewById(R.id.header_email);
        pImage = view.findViewById(R.id.profile_image);

        // setting name, email and profile picture in navigation drawer
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseDatabase.getInstance().getReference("driverData").child(uid).child("fullName").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = dataSnapshot.getValue(String.class);


                if (name != null && !name.isEmpty()) textHeaderName.setText(name);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        FirebaseDatabase.getInstance().getReference("driverData").child(uid).child("email").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String email = dataSnapshot.getValue(String.class);

                if (email != null && !email.isEmpty()) textHeaderEmail.setText(email);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        FirebaseDatabase.getInstance().getReference("driverData").child(uid).child("imageUri").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String url = dataSnapshot.getValue(String.class);
                //Log.i("url", url);

                if (url != null && !url.isEmpty())
                    Picasso.with(getApplicationContext()).load(url).into(pImage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // choosing profile image
        pImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Select an image"), GALLERY_PICK);
            }
        });

        // location switch button

        location_switch = findViewById(R.id.location_switch);

        location_switch.setEnabled(false);

        if (checkLocationPermissions()) {
            location_switch.setEnabled(true);
        } else {
            requestLocationPermissions();
        }

        location_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {

                    startLocationUpdates();
                    updateMarkerUI();


                } else {

                    if (mCurrentPosiMarker != null && mLocationCallback != null) {

                        // remove marker
                        mCurrentPosiMarker.remove();

                        // remove location
                        geoFire.removeLocation(FirebaseAuth.getInstance().getCurrentUser().getUid());

                        removeLocationFromDatabase(FirebaseAuth.getInstance().getCurrentUser().getUid());

                        // remove location updates
                        fusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
                    }

                }
            }
        });

        // update token to "Token" database
        updateFirebaseToken();

    }


    private void updateFirebaseToken() {

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference tokens = db.getReference("Tokens");

        Token token = new Token(FirebaseInstanceId.getInstance().getToken());

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            tokens.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(token);
        }
    }

    // my own implementation of add location to database "online"
    private void addLocationToDatabase(final double latitude, final double longitude) {

        mRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(new DataModel(latitude, longitude))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //Toast.makeText(getApplicationContext(), "location updated", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // my own implementation of remove location from database "online"
    private void removeLocationFromDatabase(String uid) {

        mRef.child(uid).removeValue();
    }

    private boolean checkLocationPermissions() {

        // Changed 1
        return ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermissions() {

        // Changed 2
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) && ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {

            new AlertDialog.Builder(this)
                    .setTitle("Permission needed")
                    .setMessage("This permission is needed for accessing location!")
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MapActivity.this,
                                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_CODE);
                        }
                    })
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();

        } else {

            // Changed 3
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_CODE);
        }
    }

    private void startLocationUpdates() {
        createLocationRequest();
        createLocationCallback();
        buildLocationSettingsRequest();
    }


    private void createLocationRequest() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void createLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                mCurrentLocation = locationResult.getLastLocation();
                updateMarkerUI();
            }
        };
    }

    private void updateMarkerUI() {
        if (mCurrentLocation != null) {

            geoFire = new GeoFire(driver);
            geoFire.setLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(), new GeoLocation(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));

            addLocationToDatabase(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            LatLng mPosi = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());

            if (mCurrentPosiMarker != null)
                mCurrentPosiMarker.remove();
            mCurrentPosiMarker = map.addMarker(new MarkerOptions().title("You").position(mPosi));
            // map.setMyLocationEnabled(true);

            if (firstTime)
                map.moveCamera(CameraUpdateFactory.newLatLng(mPosi));
            else {
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(mPosi, 15f));
                firstTime = true;
            }

        }
    }

    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @SuppressLint("MissingPermission")
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {

                fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                // map.setMyLocationEnabled(true);
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(MapActivity.this,
                                REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }

            }
        });
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        //Log.i(TAG, "User agreed to make required location settings changes.");
                        // Nothing to do. startLocationupdates() gets called in onResume again.
                        startLocationUpdates();
                        updateMarkerUI();
                        break;
                    case Activity.RESULT_CANCELED:
                        //Log.i(TAG, "User chose not to make required location settings changes.");
                        //mRequestingLocationUpdates = false;
                        //updateUI();
                        if (location_switch.isChecked())
                            location_switch.setChecked(false);
                        break;
                }
                break;
        }

        if (requestCode == GALLERY_PICK && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                Toast.makeText(getApplicationContext(), "Image not selected!", Toast.LENGTH_SHORT).show();
            } else {
                imageUri = data.getData();

                //Picasso.with(getApplicationContext()).load(imageUri).into(pImage);
//                Calendar calendar = Calendar.getInstance();
//                String currentDate = DateFormat.getDateInstance(DateFormat.FULL).format(calendar.getTime());
//                currentDate = currentDate.replace(" ", "_");

                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

                final StorageReference ref = mStorageRef.child("IMG_" + timeStamp + ".jpg");
                ref.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(final Uri uri) {
                                Log.i("uri", uri.toString());

                                Picasso.with(getApplicationContext()).load(uri).into(pImage);
                                DatabaseReference mRefToUid = mRefToDriverData.child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                mRefToUid.child("imageUri").setValue(uri.toString());
                            }
                        });
                    }
                });
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // Changed 4
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission GRANTED", Toast.LENGTH_SHORT).show();
                location_switch.setEnabled(true);

            } else {
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            //super.onBackPressed();
            if (backPressedTime + 2000 > System.currentTimeMillis()) {
                backToast.cancel();
                super.onBackPressed();
                return;
            } else {
                backToast = Toast.makeText(getBaseContext(), "Press back again to exit", Toast.LENGTH_SHORT);
                backToast.show();
            }
            backPressedTime = System.currentTimeMillis();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        switch (menuItem.getItemId()) {
            case R.id.edit_profile:
                startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                break;
            case R.id.log_out:
                logoutUser();
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.map_type, menu);
        return true;
    }

    private void logoutUser() {
        editor.remove("email");
        editor.remove("pass");
        editor.commit();
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.normal_map:
                if (map != null)
                    map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case R.id.satellite_map:
                if (map != null)
                    map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
        }

        return true;
    }

}
