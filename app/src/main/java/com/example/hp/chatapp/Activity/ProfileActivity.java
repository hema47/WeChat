package com.example.hp.chatapp.Activity;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.hp.chatapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
    private CircleImageView mProfileImage;
    private TextView mProfileName,mProfileStatus,mProfileFriendsCount;
    private Button mProfileSendReqBtn,mDeclineBtn;
    private ProgressDialog mProgressDialog;
    private String mCurrentState;
    private DatabaseReference mUserDatabase;
    private DatabaseReference mFriendRequestDatabase;
    private DatabaseReference mFriendsDatabase;
    private DatabaseReference mNotificationDatabase;
    private DatabaseReference mUserRef;
    private DatabaseReference mRootRef;
    private FirebaseUser mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String user_id = getIntent().getStringExtra("user_id");
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mFriendRequestDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mFriendsDatabase =  FirebaseDatabase.getInstance().getReference().child("Friends");
        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("notifications");
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid());

        mProfileImage = (CircleImageView)findViewById(R.id.profile_user_image);
        mProfileStatus = (TextView)findViewById(R.id.profile_user_status);
        mProfileFriendsCount= (TextView)findViewById(R.id.profile_total_friends);
        mProfileName = (TextView)findViewById(R.id.profile_display_name);
        mProfileSendReqBtn= (Button) findViewById(R.id.profile_send_req_btn);
        mDeclineBtn = (Button)findViewById(R.id.profile_decline_request);

        if(mCurrentState == null) {
            mCurrentState = "not_friends";
            //in starting decline btn
            mDeclineBtn.setVisibility(View.INVISIBLE);
            mDeclineBtn.setEnabled(false);
        }

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Loading User Data..");
        mProgressDialog.setMessage("Please wait while we load the user data.");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String displayName = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();

                mProfileName.setText(displayName);
                mProfileStatus.setText(status);

                Picasso.with(ProfileActivity.this).load(image).placeholder(R.drawable.defaultuser).into(mProfileImage);
                //if current user is same as profile user
                if(mCurrentUser.getUid().equals(user_id)){

                    mDeclineBtn.setEnabled(false);
                    mDeclineBtn.setVisibility(View.INVISIBLE);

                    mProfileSendReqBtn.setEnabled(false);
                    mProfileSendReqBtn.setVisibility(View.INVISIBLE);

                }


                // -------------------FriendsList || Request Feature -------------------------
                mFriendRequestDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(user_id)) {
                            String req_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();
                            //current user get the request from profile User so get received req_type and
                            // btn will show to accept request text
                            if (req_type.equals("received")) {

                                mCurrentState = "req_received";
                                mProfileSendReqBtn.setText("Accept Friend Request");
                                // enabling the decline request btn
                                mDeclineBtn.setVisibility(View.VISIBLE);
                                mDeclineBtn.setEnabled(true);

                            } else if (req_type.equals("sent")) {
                                //current user send the request to profile user
                                // so btn will show cancel request
                                mCurrentState = "req_sent";
                                mProfileSendReqBtn.setText("Cancel Friend Request");

                                // invisible the decline request btn
                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                mDeclineBtn.setEnabled(false);
                            }

                            mProgressDialog.dismiss();
                        }
                        else{

                            mFriendsDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        if(dataSnapshot.hasChild(user_id)){

                                            mCurrentState = "friends";
                                            mProfileSendReqBtn.setText("Unfriend This Person");

                                            //both are friends so invisible the decline btn
                                            mDeclineBtn.setVisibility(View.INVISIBLE);
                                            mDeclineBtn.setEnabled(false);
                                        }

                                        mProgressDialog.dismiss();
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                        mProgressDialog.dismiss();

                                    }
                                });
                            }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //-----------------------Friend Request Send Btn Listener -------------------

        mProfileSendReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProfileSendReqBtn.setEnabled(false);

                //------------------------Not Firends State--------------------------------
                if(mCurrentState.equals("not_friends")){

                    //For notification
                    DatabaseReference newNotificationRef = mRootRef.child("notifications").child(user_id).push();
                    String newNotificationId = newNotificationRef.getKey();

                    HashMap<String,String> notificationData = new HashMap<>();
                    notificationData.put("from",mCurrentUser.getUid());
                    notificationData.put("type","request");

                    Map requestMap = new HashMap();
                    requestMap.put("Friend_req/"+ mCurrentUser.getUid() + "/" + user_id +"/request_type" , "sent");
                    requestMap.put("Friend_req/"+ user_id + "/" +   mCurrentUser.getUid() +  "/request_type" , "received");
                    requestMap.put("notifications/"+ user_id + "/" + newNotificationId , notificationData);

                    mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                           if(databaseError != null){
                               Toast.makeText(ProfileActivity.this,"there is some error in sending request",Toast.LENGTH_LONG).show();
                           }
                           else{
                               mCurrentState = "req_sent";
                               mProfileSendReqBtn.setText("Cancel Friend Request");
                           }
                           mProfileSendReqBtn.setEnabled(true);
                        }
                    });
                }


                //----------------------------------Cancel Friend Request State----------------------------


                if(mCurrentState.equals("req_sent")){
                    mFriendRequestDatabase.child(mCurrentUser.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendRequestDatabase.child(user_id).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mProfileSendReqBtn.setEnabled(true);
                                    mCurrentState = "not_friends";
                                    mProfileSendReqBtn.setText("Send Friend Request");

                                    mDeclineBtn.setVisibility(View.INVISIBLE);
                                    mDeclineBtn.setEnabled(false);
                                }
                            });
                        }
                    });

                }
                //------------------------------Accept Request State ---------------------------------------------
                if(mCurrentState.equals("req_received")){
                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                    Map friendsMap = new HashMap();
                    friendsMap.put("Friends/" + mCurrentUser.getUid() + "/" + user_id + "/date" , currentDate);
                    friendsMap.put("Friends/" +user_id  + "/" + mCurrentUser.getUid()+ "/date" , currentDate);

                    friendsMap.put("Friend_req/" + mCurrentUser.getUid() + "/" + user_id , null );
                    friendsMap.put("Friend_req/" + user_id + "/" + mCurrentUser.getUid() , null );
                    mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if(databaseError == null){
                              mProfileSendReqBtn.setEnabled(true);
                              mCurrentState = "friends";
                              mProfileSendReqBtn.setText("Unfriend this Person");

                              //decline btn visibility
                              mDeclineBtn.setVisibility(View.INVISIBLE);
                              mDeclineBtn.setEnabled(false);
                            }else{
                                String error = databaseError.getMessage();
                                Toast.makeText(ProfileActivity.this, error , Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }

                //  ---------------------------- Unfriend State ------------------------------------
                if(mCurrentState.equals("friends")){
                    Map unfriendMap = new HashMap();
                    unfriendMap.put("Friends/" + mCurrentUser.getUid() + "/" + user_id , null);
                    unfriendMap.put("Friends/" + user_id + "/" + mCurrentUser.getUid() , null);

                    unfriendMap.put("Chat/" + mCurrentUser.getUid() + "/" + user_id ,null);
                    unfriendMap.put("Chat/" + user_id + "/" + mCurrentUser.getUid() , null);

                    mRootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                           if(databaseError == null){
                               mCurrentState = "not_friends";
                               mProfileSendReqBtn.setText("Send Friend Request");

                               //decline btn
                               mDeclineBtn.setVisibility(View.INVISIBLE);
                               mDeclineBtn.setEnabled(false);
                           }
                           else{
                               String error = databaseError.getMessage();
                               Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();
                           }
                            mProfileSendReqBtn.setEnabled(true);
                        }
                    });

                }


            }
        });


        mDeclineBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDeclineBtn.setEnabled(false);
                mProfileSendReqBtn.setEnabled(false);
                mFriendRequestDatabase.child(mCurrentUser.getUid()).child(user_id).removeValue()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                              mFriendRequestDatabase.child(user_id).child(mCurrentUser.getUid()).removeValue()
                                      .addOnSuccessListener(new OnSuccessListener<Void>() {
                                          @Override
                                          public void onSuccess(Void aVoid) {
                                            mProfileSendReqBtn.setEnabled(true);
                                            mCurrentState = "not_friends";
                                            mProfileSendReqBtn.setText("SEND FRIEND REQUEST");
                                            mDeclineBtn.setVisibility(View.INVISIBLE);
                                            mDeclineBtn.setEnabled(false);
                                          }
                                      });
                            }
                        });

            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        mRootRef.child("Users").child(mCurrentUser.getUid()).child("online").setValue("true");

    }

    @Override
    protected void onPause() {
        super.onPause();
        mRootRef.child("Users").child(mCurrentUser.getUid()).child("online").setValue(ServerValue.TIMESTAMP);
    }


}
