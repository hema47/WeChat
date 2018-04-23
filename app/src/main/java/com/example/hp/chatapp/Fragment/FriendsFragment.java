package com.example.hp.chatapp.Fragment;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.hp.chatapp.Activity.ChatActivity;
import com.example.hp.chatapp.Activity.ProfileActivity;
import com.example.hp.chatapp.Model.Friends;
import com.example.hp.chatapp.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/*
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {
    private RecyclerView mFriendsList;
    private DatabaseReference mFriendsDatabase;
    private DatabaseReference mUsersDatabase;
    private FirebaseAuth mAuth;
    private String mCurrentUserId;
    private View mMainView;


    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mMainView = inflater.inflate(R.layout.fragment_friends,container,false);
        mFriendsList = (RecyclerView)mMainView.findViewById(R.id.friends_list);
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();

        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrentUserId);
     //   mFriendsDatabase.keepSynced(true);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
      //  mUsersDatabase.keepSynced(true);

        mFriendsList.setHasFixedSize(true);
        mFriendsList.setLayoutManager(new LinearLayoutManager(getContext()));
        // Inflate the layout for this fragment
        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<Friends,FriendsViewHolder> friendsRecyclerViewAdapter  = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(
                Friends.class,
                R.layout.users_single_layout,
                FriendsViewHolder.class,
                mFriendsDatabase
        ) {
            @Override
            protected void populateViewHolder(final FriendsViewHolder viewHolder, final Friends friends, int position) {

                viewHolder.setDate(friends.getDate());
                final String list_user_id = getRef(position).getKey();
                mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                     final String userName = dataSnapshot.child("name").getValue().toString();
                     final String userThumb = dataSnapshot.child("thumb_image").getValue().toString();


                     if(dataSnapshot.hasChild("online")){
                         String userOnline =  dataSnapshot.child("online").getValue().toString();
                         viewHolder.setUserOnline(userOnline);
                     }

                        viewHolder.setName(userName);
                        viewHolder.setThumbImage(userThumb,getContext());
                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                CharSequence options[] = new CharSequence[]{"Open Profile","Send Message"};
                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle("Select Options");
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                       //Click Event for each item
                                        if(i == 0){
                                            Intent profileIntent = new Intent(getContext(),ProfileActivity.class);
                                            profileIntent.putExtra("user_id",list_user_id);
                                            startActivity(profileIntent);
                                        }
                                        if(i == 1){
                                            Intent chatIntent = new Intent(getContext(),ChatActivity.class);
                                            chatIntent.putExtra("user_id",list_user_id);
                                            chatIntent.putExtra("user_name",userName);
                                            startActivity(chatIntent);
                                        }

                                    }
                                });
                                builder.show();

                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        };
        mFriendsList.setAdapter(friendsRecyclerViewAdapter);
    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public FriendsViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setDate(String date){
            TextView userStatusView = (TextView) mView.findViewById(R.id.user_single_status);
            userStatusView.setText(date);
        }

        public void setName(String name){
            TextView userNameView  = (TextView) mView.findViewById(R.id.user_single_name);
            userNameView.setText(name);
        }

       public void setThumbImage(String thumbImage,Context context){
            CircleImageView userThumbView = (CircleImageView) mView.findViewById(R.id.user_single_image);
           Picasso.with(context).load(thumbImage).placeholder(R.drawable.defaultuser).into(userThumbView);
        }

        public void setUserOnline(String online_status){
            ImageView userOnlineView = (ImageView)mView.findViewById(R.id.user_single_online);
            if(online_status.equals("true")){
                userOnlineView.setVisibility(View.VISIBLE);
            }else{
                userOnlineView.setVisibility(View.INVISIBLE);
            }
        }
    }
}
