package com.example.hp.chatapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import com.example.hp.chatapp.Adapter.SectionsPagerAdapter;
import com.example.hp.chatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference mUserRef;
    private Toolbar mToolbar;
    private ViewPager mViewPager;
    private SectionsPagerAdapter mSectionPagerAdapter;
    private TabLayout mTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //to initialise  various instances
        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser() != null) {
            mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
        }

        mToolbar = (Toolbar)findViewById(R.id.main_page_tool_bar);
       setSupportActionBar(mToolbar);
       getSupportActionBar().setTitle("Chat App");

       //Tabs
        mViewPager = (ViewPager)findViewById(R.id.main_tab_pager);
        mSectionPagerAdapter  = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mSectionPagerAdapter);

        mTabLayout = (TabLayout)findViewById(R.id.main_tabs);
        mTabLayout.setupWithViewPager(mViewPager);


    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null){
           sendToStart();
        }/*else{
                mUserRef.child("online").setValue("true");

        }*/
    }

  /*  @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null) {
            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);

        }
    }
*/
    private void sendToStart() {
        Intent startIntent = new Intent(MainActivity.this,StartActivity.class);
        startActivity(startIntent);
        finish(); // when back btn pressed not moved to previous activity
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
         getMenuInflater().inflate(R.menu.main_menu,menu);
         return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
         super.onOptionsItemSelected(item);
         if(item.getItemId() == R.id.main_logout_btn){
             mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
           FirebaseAuth.getInstance().signOut();
           sendToStart();
         }
         if(item.getItemId()== R.id.main_setting_btn){
             Intent settingsIntent = new Intent(MainActivity.this,SettingActivity.class);
             startActivity(settingsIntent);
         }
         if(item.getItemId() == R.id.main_all_users_btn){
             Intent usersIntent = new Intent(MainActivity.this,UsersActivity.class);
             startActivity(usersIntent);
         }
         return true;
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(mAuth.getCurrentUser()!=null)
            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mUserRef.child("online").setValue("true");
    }
}
