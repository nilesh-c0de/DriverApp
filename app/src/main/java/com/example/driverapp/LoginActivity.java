package com.example.driverapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.driverapp.Model.Driver;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private DatabaseReference users;

    private BottomSheetBehavior bottomSheetBehaviorLogin, bottomSheetBehaviorRegister;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private EditText editEmail, editPassword;

    private String savedEmail, savedPassword;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // sharedPreferences
        sharedPreferences = getSharedPreferences("userData", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        // locading data
        loadUserData();

        // checking is user is logged in before
        if (savedEmail != null && savedPassword != null) {
            startActivity(new Intent(getApplicationContext(), MapActivity.class));
        }

        //get the bottom sheet view
        LinearLayout linearLayoutLogin = findViewById(R.id.bottom_sheet_login);
        LinearLayout linearLayoutRegister = findViewById(R.id.bottom_sheet_register);

        //init the bottom sheet view
        bottomSheetBehaviorLogin = BottomSheetBehavior.from(linearLayoutLogin);
        bottomSheetBehaviorRegister = BottomSheetBehavior.from(linearLayoutRegister);


        auth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        users = database.getReference("driverData");

        Button btnSignup = findViewById(R.id.btn_signup);
        Button btnLogin = findViewById(R.id.btn_login);

        editEmail = findViewById(R.id.input_email);
        editPassword = findViewById(R.id.input_password);

        final EditText rEmail = findViewById(R.id.edit_email);
        final EditText rName = findViewById(R.id.edit_name);
        final EditText rPass = findViewById(R.id.edit_pass);
        final EditText rCpass = findViewById(R.id.edit_cpass);
        final EditText rMb = findViewById(R.id.edit_mb);


        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //showRegisterDialog();
                bottomSheetBehaviorRegister.setState(BottomSheetBehavior.STATE_EXPANDED);

            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //showLoginDialog();
                bottomSheetBehaviorLogin.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });

        Button loginButton = findViewById(R.id.button_login);
        Button registerButton = findViewById(R.id.button_register);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String name = rName.getText().toString();
                final String email = rEmail.getText().toString();
                final String pass = rPass.getText().toString();
                final String cpass = rCpass.getText().toString();
                final String mb = rMb.getText().toString();

                if (name.isEmpty() || email.isEmpty() || pass.isEmpty() || cpass.isEmpty() || mb.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please fill all details", Toast.LENGTH_SHORT).show();
                } else {
                    auth.createUserWithEmailAndPassword(email, pass)
                            .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        users.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                final Driver usr = new Driver(email, pass, cpass, name, mb, "");
                                                users.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(usr);
                                                Toast.makeText(getApplicationContext(), "Registered successfully!", Toast.LENGTH_SHORT).show();
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                                Toast.makeText(getApplicationContext(), "Failed!", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    } else {
                                        Toast.makeText(getApplicationContext(), "Error!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String textEmail = editEmail.getText().toString();
                final String textPassword = editPassword.getText().toString();

                if (textEmail.isEmpty() || textPassword.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please enter email & password!", Toast.LENGTH_SHORT).show();
                } else {
                    auth.signInWithEmailAndPassword(textEmail, textPassword)
                            .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {

                                    if (task.isSuccessful()) {
                                        Toast.makeText(getApplicationContext(), "Authencation success :)", Toast.LENGTH_SHORT).show();

                                        saveLoginData(textEmail, textPassword);

                                        startActivity(new Intent(getApplicationContext(), MapActivity.class));
                                    } else {
                                        Toast.makeText(getApplicationContext(), "Authencation failed!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }

            }
        });

    }

    private void loadUserData() {

        savedEmail = sharedPreferences.getString("email", null);
        savedPassword = sharedPreferences.getString("pass", null);
    }

    private void saveLoginData(String textEmail, String textPassword) {

        editor.putString("email", textEmail);
        editor.putString("pass", textPassword);
        editor.commit();
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
