package com.example.hp.chatapp.Activity;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.hp.chatapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class StatusActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private TextInputLayout mStatus;
    private Button mSaveBtn;

    //Firebase
    private DatabaseReference mStatusDatabase;
    private FirebaseUser mCurrentUser;

    //Progress
    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        //Firebase
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = mCurrentUser.getUid();
        mStatusDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

        mToolbar = (Toolbar)findViewById(R.id.status_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //getting status from settingActivity
        String statusValue = getIntent().getStringExtra("status_value");


        mStatus = (TextInputLayout) findViewById(R.id.status_input);
        mSaveBtn = (Button)findViewById(R.id.status_save_btn);
        mStatus.getEditText().setText(statusValue);

        mSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String status = mStatus.getEditText().getText().toString();
                if (status.length() > 50) {
                    Toast.makeText(StatusActivity.this, "Status should be less than 50 characters", Toast.LENGTH_SHORT).show();
                } else {
                    //Progress
                    mProgress = new ProgressDialog(StatusActivity.this);
                    mProgress.setTitle("Saving Changes");
                    mProgress.setMessage("Please Wait while we save the changes");
                    mProgress.show();


                    mStatusDatabase.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                mProgress.dismiss();
                            } else {
                                Toast.makeText(getApplicationContext(), "There was some error in saving Changes.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }
}
