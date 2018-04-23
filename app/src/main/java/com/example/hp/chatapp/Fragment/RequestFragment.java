package com.example.hp.chatapp.Fragment;


import android.content.Context;
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
import com.example.hp.chatapp.Model.FriendRequest;
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


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestFragment extends Fragment {
    private RecyclerView mReqList;

    private DatabaseReference mFriendReqRef;
    private DatabaseReference mUsersDatabase;
    private FirebaseAuth mAuth;
    private String mCurrentUserId;

    private View mMainView;


    public RequestFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mMainView = inflater.inflate(R.layout.fragment_request, container, false);

        mReqList = (RecyclerView)mMainView.findViewById(R.id.req_recycler_view);

        mAuth = FirebaseAuth.getInstance();

        mCurrentUserId = mAuth.getCurrentUser().getUid();

        mFriendReqRef = FirebaseDatabase.getInstance().getReference().child("Friend_req").child(mCurrentUserId);
        mFriendReqRef.keepSynced(true);

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        mReqList.setHasFixedSize(true);
        mReqList.setLayoutManager(linearLayoutManager);

        // Inflate the layout for this fragment
        return mMainView;
    }


    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<FriendRequest,RequestFragment.RequestViewHolder> firebaseRequestAdapter = new FirebaseRecyclerAdapter<FriendRequest, RequestFragment.RequestViewHolder>(
                FriendRequest.class,
                R.layout.users_single_layout,
                RequestFragment.RequestViewHolder.class,
                mFriendReqRef
        ) {
            @Override
            protected void populateViewHolder(final RequestFragment.RequestViewHolder RequestHolder, FriendRequest model, int position) {
                String req_type = model.getRequest_type();
                final  String list_user_id = getRef(position).getKey();

                if(req_type.equals("received")){
                    mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                          final String userName   = dataSnapshot.child("name").getValue().toString();
                            String userThumb = dataSnapshot.child("thumb_image").getValue().toString();

                            if (dataSnapshot.hasChild("online")) {

                                String userOnline = dataSnapshot.child("online").getValue().toString();
                                RequestHolder.setUserOnline(userOnline);
                            }

                            RequestHolder.setName(userName);
                            RequestHolder.setThumbImage(userThumb,getContext());
                            RequestHolder.mView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {


                                    Intent profileIntent = new Intent(getContext(), ProfileActivity.class);
                                    profileIntent.putExtra("user_id", list_user_id);
                                    startActivity(profileIntent);
                                }
                            });
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

            }
        };
        mReqList.setAdapter(firebaseRequestAdapter);
    }

    public static class RequestViewHolder  extends RecyclerView.ViewHolder{
        private View mView;

        public RequestViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
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
