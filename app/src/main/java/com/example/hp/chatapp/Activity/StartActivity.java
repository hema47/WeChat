package com.example.hp.chatapp.Activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.hp.chatapp.R;

public class StartActivity extends AppCompatActivity {
    private Button registerBtn;
    private Button loginBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        //initialisation
        registerBtn = (Button)findViewById(R.id.start_reg_btn);
        loginBtn = (Button)findViewById(R.id.start_already_account_btn);
        //setting listener
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent registerIntent = new Intent(StartActivity.this,RegisterActivity.class);
                startActivity(registerIntent);
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent loginIntent = new Intent(StartActivity.this,LoginActivity.class);
                startActivity(loginIntent);
            }
        });
    }
}
