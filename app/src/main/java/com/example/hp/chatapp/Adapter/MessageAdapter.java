package com.example.hp.chatapp.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.example.hp.chatapp.Model.Messages;
import com.example.hp.chatapp.R;
import com.google.firebase.auth.FirebaseAuth;


import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by HP on 16-04-2018.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Messages> mMessageList; // Contains the messages
    private FirebaseAuth mAuth;
    private Context mContext;


    public MessageAdapter(List<Messages> mMessageList, Context context) {
        this.mMessageList = mMessageList;
        this.mContext = context;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_single_layout, parent, false);
        return new MessageViewHolder(v);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(final MessageViewHolder holder, int position) {
        if(mMessageList.size() > 0) {
            final Messages c = mMessageList.get(position);
            final String from_user = c.getFrom();   // the user that sends message

            mAuth = FirebaseAuth.getInstance();
            String mCurrentUser = mAuth.getCurrentUser().getUid();
            Drawable background = holder.messageText.getBackground();
            GradientDrawable gradientDrawable = (GradientDrawable) background;

            if (from_user.equals(mCurrentUser)) {
                gradientDrawable.setColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
                holder.messageText.setTextColor(Color.WHITE);
                holder.profileImage.setVisibility(View.GONE);
                holder.mlayout.setGravity(Gravity.RIGHT);
            } else {
                gradientDrawable.setColor(Color.WHITE);
                holder.messageText.setTextColor(R.color.colorPrimary);
                holder.profileImage.setVisibility(View.GONE);
                holder.mlayout.setGravity(Gravity.LEFT);
            }
            holder.messageText.setText(c.getMessage());
        }

}

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }


    public class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView messageText;
        public CircleImageView profileImage;
        public RelativeLayout mlayout;
        //public TextView displayName;
       // public ImageView messageImage;

        public MessageViewHolder(View itemView) {
            super(itemView);

            messageText = (TextView)itemView.findViewById(R.id.message_text_layout);
            profileImage = (CircleImageView)itemView.findViewById(R.id.message_profile_layout);
            mlayout      = (RelativeLayout)itemView.findViewById(R.id.message_single_layout);
           // messageImage = (ImageView)itemView.findViewById(R.id.message_image_layout);
            //displayName = (TextView) itemView.findViewById(R.id.name_text_layout);

        }
    }
}


