package com.example.hp.chatapp.Activity;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.example.hp.chatapp.Adapter.MessageAdapter;
import com.example.hp.chatapp.GetTimeAgo;
import com.example.hp.chatapp.Model.Messages;
import com.example.hp.chatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private static final int GALLERY_PICK = 1;
    private String mChatUser; //to whom we want to message
    private Toolbar mChatToolbar;
    private DatabaseReference mRootRef;
    private TextView mTitleView;
    private TextView mLastSeenView;
    private CircleImageView mProfileImage;
    private FirebaseAuth mAuth;
    private String mCurrentUserId;
    private ImageButton mChatAddBtn;
    private ImageButton mChatSendBtn;
    private EditText mChatMessageView;
    private RecyclerView mMessagesList;

    ///////////////////////////////
    private Query messageQuery ;
    private ChildEventListener listener;

    private SwipeRefreshLayout mRefreshLayout;
    private final List<Messages> messagesList = new ArrayList<>();

    private LinearLayoutManager mLinearLayout;
    private MessageAdapter mAdapter;
    private static final int TOTAL_ITEMS_TO_LOAD = 10;
    private int currentPage = 1;

    //new solution
    private int itemPos = 0;
    private String mLastkey = "";
    private String mPrevkey = "";

    // Storage Firebase
    private StorageReference mImageStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //*********************get data from intent ***********************************************
        mChatUser = getIntent().getStringExtra("user_id");
        String userName = getIntent().getStringExtra("user_name");

        mRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.message_swipe_layout);

        //********************For toolbar **********************************************************
        mChatToolbar = (Toolbar)findViewById(R.id.chat_app_bar);
        setSupportActionBar(mChatToolbar);
        //*********************For ActionBar ******************************************************
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle(userName);

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();


        /*  inflate the custom View */
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.chat_custom_bar,null);
        actionBar.setCustomView(action_bar_view);

        // ****************Custom action Bar items ***************************
        mTitleView = (TextView)findViewById(R.id.custom_bar_title);
        mLastSeenView =(TextView)findViewById(R.id.custom_bar_seen);
        mProfileImage = (CircleImageView)findViewById(R.id.custom_bar_image);

        //************************Set the title of chat *******************************************
        mTitleView.setText(userName);

        //Chat.xml intinalisation
        mChatAddBtn = (ImageButton)findViewById(R.id.chat_add_btn);
        mChatSendBtn = (ImageButton)findViewById(R.id.chat_send_btn);
        mChatMessageView = (EditText) findViewById(R.id.chat_message_view);

        //***********************Setting up Recycler View ******************************************
        mAdapter = new MessageAdapter(messagesList,getApplicationContext());
        mMessagesList = (RecyclerView)findViewById(R.id.messages_list);

        //************************ Intansitiate RecyclerView  for chat ****************************
        mLinearLayout = new LinearLayoutManager(this);
        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(mLinearLayout);

        //********************set adapter for Recycler view ***************************************
        mMessagesList.setAdapter(mAdapter);


        /* load the messages and populate recycler view with messages*/
        loadMessages();


        /******************online functionality ************************/
        mRootRef.child("Users").child(mChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String online = dataSnapshot.child("online").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();
                if(online.equals("true")){
                    mLastSeenView.setText("Online");
                }else{
                    GetTimeAgo getTimeAgo = new GetTimeAgo();
                    long lastTime = Long.parseLong(online);
                    String lastSeenTime = getTimeAgo.getTimeAgo(lastTime,getApplicationContext());
                    mLastSeenView.setText(lastSeenTime);
                }

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

         /***********chat functionality **********************************/
        mRootRef.child("Chat").child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Here mChatUser is the id of user with whom current logged in user is chatting
                if(!dataSnapshot.hasChild(mChatUser)){
                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen",false);
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat/" + mCurrentUserId + "/" + mChatUser , chatAddMap);
                    chatUserMap.put("Chat/" + mChatUser + "/" + mCurrentUserId , chatAddMap);

                    mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError != null){
                                Log.d("CHAT_LOG",databaseError.getMessage().toString());
                            }
                        }
                    });

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        //*******************************listener for chat send btn*********************************
        mChatSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
             sendMessage();
            }
        });

        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
            /* currentPage++;
            // messagesList.clear(); // everytime clear the list so that refresh will work from start
                itemPos = 0;
             loadMoreMessages();*/
                messageQuery.removeEventListener(listener);
                refreshItems();
            }
        });


    }

    private void refreshItems(){
        currentPage++;
        messagesList.clear();
        loadMessages();
        mRefreshLayout.setRefreshing(false);
    }



    private void loadMessages() {
        DatabaseReference messageRef = mRootRef.child("messages").child(mCurrentUserId).child(mChatUser);
         messageQuery = messageRef.limitToLast(currentPage * TOTAL_ITEMS_TO_LOAD);

       listener =  messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Messages message = dataSnapshot.getValue(Messages.class);
                /*itemPos++;

                if(itemPos == 1){
                  String messageKey = dataSnapshot.getKey();
                  mLastkey = messageKey;
                  mPrevkey = messageKey;

                }*/
                messagesList.add(message);
                mAdapter.notifyDataSetChanged();
                mMessagesList.scrollToPosition(messagesList.size() - 1); // move the recyclerview to Bottom most position
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }



    private void sendMessage() {
        String message  = mChatMessageView.getText().toString();
        if(!TextUtils.isEmpty(message)){
            String current_user_ref = "messages/" + mCurrentUserId + "/" + mChatUser ;
            String chat_user_ref = "messages/" + mChatUser + "/" + mCurrentUserId;

            DatabaseReference user_message_push = mRootRef.child("messages")
                    .child(mCurrentUserId).child(mChatUser).push();
            //getting message pushing id
            String push_id = user_message_push.getKey();


            Map messageMap = new HashMap();
            messageMap.put( "message" , message );
            messageMap.put( "seen" , false);
            messageMap.put( "type" , "text");
            messageMap.put("time" , ServerValue.TIMESTAMP);
            messageMap.put("from", mCurrentUserId);

            Map messageUserMap = new HashMap();
            messageUserMap.put(current_user_ref + "/" + push_id , messageMap);
            messageUserMap.put(chat_user_ref + "/" + push_id , messageMap);

            mChatMessageView.setText(""); // for setting blank message view after data is sent

            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                   if(databaseError != null){
                       Log.d("CHAT_LOG",databaseError.getMessage().toString());
                       Toast.makeText(getApplicationContext(),"Error",Toast.LENGTH_SHORT).show();
                   }
                }
            });

        }
    }



}
