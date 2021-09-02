package com.example.driverapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.driverapp.Model.Driver;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {

    EditText txtName, txtMb, txtEmail;
    Button save_changes;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
        setTitle("Edit profile");

        txtName = findViewById(R.id.edit_profile_name);
        txtMb = findViewById(R.id.edit_profile_mb);
        txtEmail = findViewById(R.id.edit_profile_email);

        save_changes = findViewById(R.id.save_changes);

        final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        save_changes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase.getInstance().getReference("driverData")
                        .child(uid)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                String name = txtName.getText().toString();
                                String mb = txtMb.getText().toString();
                                String email = txtEmail.getText().toString();

                                if(name.isEmpty() || mb.isEmpty() || email.isEmpty())
                                {
                                    Toast.makeText(getApplicationContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                                }
                                else
                                {
                                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("driverData")
                                            .child(uid);
                                    ref.child("fullName").setValue(name);
                                    ref.child("mobileNumber").setValue(mb);
                                    ref.child("email").setValue(email);

                                    Toast.makeText(getApplicationContext(), "Saved changes successfully!", Toast.LENGTH_SHORT).show();
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
            }
        });



        FirebaseDatabase.getInstance().getReference("driverData")
                .child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Driver driver = dataSnapshot.getValue(Driver.class);

                        if(driver!=null) {
                            txtName.setText(driver.getFullName());
                            txtMb.setText(driver.getMobileNumber());
                            txtEmail.setText(driver.getEmail());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.single_item, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.item_edit:
                //Toast.makeText(getApplicationContext(), "clicked", Toast.LENGTH_SHORT).show();
                enableAll();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void enableAll() {

        txtName.setEnabled(true);
        txtEmail.setEnabled(true);
        txtMb.setEnabled(true);

        save_changes.setEnabled(true);

    }
}
