package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private Button enterApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //The continue button
        enterApp = (Button) findViewById(R.id.enterApp);

        //As soon as the continue button is clicked, it redirects the user to the login page
        enterApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent (MainActivity.this, CustomerLoginActivity.class);
                startActivity(intent);
                //finish();
                return;
            }
        });
    }
}

