package com.example.mindvisualizer;

import android.content.Intent;
//import android.support.annotation.NonNull;
//import android.support.design.widget.CoordinatorLayout;
//import android.support.design.widget.Snackbar;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
//import com.google.android.gms.tasks.OnSuccessListener;
//import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class StartActivity extends AppCompatActivity {

    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth mAuth;
    private Button startBtn;

    @Override
    protected void onStart() {
        super.onStart();
        //mAuth.addAuthStateListener(authListener);
        //Check if user is logged in, if so, open menuactivity, otherwise do nothing
        //if (FirebaseAuth.getInstance().getCurrentUser() != null) {
        //    System.out.println("Login test print 1");
        //    finish();
        //}
    }

    @Override
    protected void onResume() {
        super.onResume();
        //startBtn = findViewById(R.id.startBtn);
        //startBtn.setEnabled(true);
        //if (FirebaseAuth.getInstance().getCurrentUser() != null) {
        //    System.out.println("Login test print 2");
        //    finish();
        //}
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //FIREBASE ANON AUTHENTICATION CREATION:
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            //Log.d(TAG, "signInAnonymously:success");
                            //FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(StartActivity.this, "Authentication success.",
                                    Toast.LENGTH_SHORT).show();

                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            //Log.w(TAG, "signInAnonymously:failure", task.getException());
                            Toast.makeText(StartActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }

                        // ...
                    }
                });

        startBtn = findViewById(R.id.startBtn);
        //System.out.println("Login test print 4");

        //START BUTTON:
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(StartActivity.this, ConnectionActivity.class));
            }
        });
    }
}