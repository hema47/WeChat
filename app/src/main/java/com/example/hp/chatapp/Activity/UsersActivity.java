package com.example.hp.chatapp.Activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import com.example.hp.chatapp.Model.Users;
import com.example.hp.chatapp.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {
     private Toolbar mToolbar;
     private RecyclerView mUsersList;
     //Firebase Database reference for holder
     private DatabaseReference mUsersDatabase;
     private FirebaseAuth mAuth;
     private DatabaseReference mUserRef;

    @Override
    protected void onPause() {
        super.onPause();
        mUsersDatabase.child(mAuth.getCurrentUser().getUid()).child("online").setValue(ServerValue.TIMESTAMP);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mUsersDatabase.child(mAuth.getCurrentUser().getUid()).child("online").setValue("true");
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        mToolbar = (Toolbar)findViewById(R.id.users_appbar);

      //setting up toolbar
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser() != null){
            mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
        }


        mUsersList = (RecyclerView)findViewById(R.id.users_recycler_view_list);
        mUsersList.setHasFixedSize(true);
        mUsersList.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onStart() {
        super.onStart();

     //Adapter for RecyclerView
        FirebaseRecyclerAdapter<Users,UsersViewHolder>firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, UsersViewHolder>(
                Users.class,
                R.layout.users_single_layout,
                UsersViewHolder.class,
                mUsersDatabase
        ) {
            @Override
            protected void populateViewHolder(UsersViewHolder viewHolder, Users users, int position) {
            viewHolder.setName(users.getName());
            viewHolder.setStatus(users.getStatus());
            viewHolder.setThumbImage(users.getThumb_image(),getApplicationContext());

            final String userId = getRef(position).getKey();

            viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent profileIntent = new Intent(UsersActivity.this,ProfileActivity.class);
                    profileIntent.putExtra("user_id",userId);
                    startActivity(profileIntent);
                }
            });
            }
        };
        mUsersList.setAdapter(firebaseRecyclerAdapter);
    }

    //Creating Viewholder
    public static  class UsersViewHolder extends RecyclerView.ViewHolder{
        View mView;                                                        //require by firebase
        public UsersViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setName(String name){
            TextView userNameView = (TextView)mView.findViewById(R.id.user_single_name);
            userNameView.setText(name);
        }

        public void setStatus(String status){
            TextView userStatus = (TextView)mView.findViewById(R.id.user_single_status);
            userStatus.setText(status);
        }
        public void setThumbImage(String thumb_image, Context context){
            CircleImageView userThumbImage = (CircleImageView)mView.findViewById(R.id.user_single_image);
            Picasso.with(context).load(thumb_image).placeholder(R.drawable.defaultuser).into(userThumbImage);
        }
    }



}
