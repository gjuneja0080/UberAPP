package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CustomerLoginActivity extends AppCompatActivity {
    private EditText uEmail, uPass;
    private Button uLogin, uRegistration;

    //firebase authentication variables, to manipulate data that goes into the database
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_login);
        uEmail = (EditText) findViewById(R.id.email);
        uPass = (EditText) findViewById(R.id.password);
        uLogin = (Button) findViewById(R.id.login);
        uRegistration = (Button) findViewById(R.id.registration);

        mAuth = FirebaseAuth.getInstance();

        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {                                 //User logs in and goes to the next page after this method is executed
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();                                //stores the information of the currently logged in user
                if(user != null){
                    Intent intent = new Intent (CustomerLoginActivity.this, MapsActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                }
            }
        };


        //Button for registration on the app
        uRegistration.setOnClickListener(new View.OnClickListener() {                                           //Button to register on the app
            @Override
            public void onClick(View v) {
                final String email = uEmail.getText().toString();
                final String password = uPass.getText().toString();
                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(CustomerLoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(!task.isSuccessful()) {

                            FirebaseException e = (FirebaseException) task.getException();
                            Toast.makeText(CustomerLoginActivity.this, "Failed Registration: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                            return;
                        }else{
                            //Adds the user's credentials to the database
                            String user_id = mAuth.getCurrentUser().getUid();
                            DatabaseReference current_user_db = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child(user_id);
                            current_user_db.setValue(true);
                        }
                    }
                });
            }
        });

        //Button for logging in the application
        uLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = uEmail.getText().toString();
                final String pass = uPass.getText().toString();
                mAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(CustomerLoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(!task.isSuccessful()) {
                            Toast.makeText(CustomerLoginActivity.this, "Failed to sign in", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(firebaseAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(firebaseAuthListener);
    }
}
