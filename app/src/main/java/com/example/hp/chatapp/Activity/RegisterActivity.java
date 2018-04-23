package com.example.hp.chatapp.Activity;

        import android.app.ProgressDialog;
        import android.content.Intent;
        import android.support.annotation.NonNull;
        import android.support.design.widget.TextInputLayout;
        import android.support.v7.app.AppCompatActivity;
        import android.os.Bundle;
        import android.support.v7.widget.Toolbar;
        import android.text.TextUtils;
        import android.util.Log;
        import android.view.View;
        import android.widget.Button;
        import android.widget.Toast;

        import com.example.hp.chatapp.R;
        import com.google.android.gms.tasks.OnCompleteListener;
        import com.google.android.gms.tasks.Task;
        import com.google.firebase.FirebaseException;
        import com.google.firebase.auth.AuthResult;
        import com.google.firebase.auth.FirebaseAuth;
        import com.google.firebase.auth.FirebaseUser;
        import com.google.firebase.database.DatabaseReference;
        import com.google.firebase.database.FirebaseDatabase;
        import com.google.firebase.iid.FirebaseInstanceId;

        import java.util.HashMap;


public class RegisterActivity extends AppCompatActivity {
    private static final String TAG ="new" ;
    private TextInputLayout mDisplayName;
    private TextInputLayout mEmail;
    private TextInputLayout mPassword;
    private Button mCreateBtn;
    private Toolbar mToolbar;

    //Progress Dialog
    private ProgressDialog mRegProgress;

    //Firebase auth
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Toolbar set
         mToolbar = (Toolbar)findViewById(R.id.register_toolbar);
         setSupportActionBar(mToolbar);
         getSupportActionBar().setTitle("Create Account");
         getSupportActionBar().setDisplayHomeAsUpEnabled(true);

         mRegProgress = new ProgressDialog(this);


        //firebase auth
        mAuth = FirebaseAuth.getInstance();

        //initiaisation
        mDisplayName = (TextInputLayout)findViewById(R.id.reg_display_name);
        mEmail = (TextInputLayout)findViewById(R.id.reg_email);
        mPassword = (TextInputLayout)findViewById(R.id.reg_password);
        mCreateBtn = (Button) findViewById(R.id.reg_create_account_btn);

        //listener for button
        mCreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String displayName = mDisplayName.getEditText().getText().toString();
                String email = mEmail.getEditText().getText().toString();
                String password = mPassword.getEditText().getText().toString();

                if(!TextUtils.isEmpty(displayName) || !TextUtils.isEmpty(email) || !TextUtils.isEmpty(password)) {
                    //progress dialog set
                    mRegProgress.setTitle("Registering User");
                    mRegProgress.setMessage("Please wait while we create your account !");
                    mRegProgress.setCanceledOnTouchOutside(false);
                    mRegProgress.show();
                    register_user(displayName, email, password);
                }
            }
        });
    }

    private void register_user(final String displayName, String email, String password) {
        Log.d(TAG,displayName+" "+email+" "+ password);
        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                 if(task.isSuccessful()){
                     FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                     String uid = currentUser.getUid();
                     String deviceToken = FirebaseInstanceId.getInstance().getToken();

                     mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
                     //For data
                     HashMap<String,String> userMap = new HashMap<>();
                     userMap.put("name",displayName);
                     userMap.put("status","Hi there I'm using Chat App.");
                     userMap.put("image","default");
                     userMap.put("thumb_image","default");
                     userMap.put("device_token",deviceToken);

                     mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                         @Override
                         public void onComplete(@NonNull Task<Void> task) {
                             if(task.isSuccessful()){
                                 mRegProgress.dismiss();
                                 Intent mainIntent = new Intent(RegisterActivity.this,MainActivity.class);
                                 mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                 startActivity(mainIntent);
                                 finish();
                             }
                         }
                     });
                 }
                 else{
                     mRegProgress.hide();
                     FirebaseException e = (FirebaseException)task.getException();
                     Log.d(TAG, "Reason: " +  e.getMessage());
                     Toast.makeText(RegisterActivity.this,"Cannot Sign in. Please check the form and try again.",Toast.LENGTH_LONG).show();
                 }
            }
        });

    }
}
