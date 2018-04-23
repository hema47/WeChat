package com.example.hp.chatapp.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.example.hp.chatapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingActivity extends AppCompatActivity {
    private static final int GALLERY_PICK = 1;
    private DatabaseReference mUserDatabase;
    private FirebaseUser mCurrentUser;

    //Android Layout
    private CircleImageView mDisplayImage;
    private TextView mName;
    private TextView mStatus;
    private Button mSettingStatusBtn;
    private Button mImageBtn;
    private ProgressDialog mProgress;

    //Firebase Storage reference for images
    private StorageReference mImageStorage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        //initialisation
        mDisplayImage = (CircleImageView)findViewById(R.id.setting_circle_image);
        mName = (TextView)findViewById(R.id.setting_display_name);
        mStatus = (TextView)findViewById(R.id.setting_status);
        mSettingStatusBtn = (Button)findViewById(R.id.setting_change_status);
        mImageBtn = (Button)findViewById(R.id.setting_change_image);

        // firebase initailsiation

        mImageStorage = FirebaseStorage.getInstance().getReference();
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUID = mCurrentUser.getUid();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUID);
        mUserDatabase.keepSynced(true);

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) { // recieving data // data change
                String name = dataSnapshot.child("name").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                mName.setText(name);
                mStatus.setText(status);

                if (! image.equals("default")) {
                    //Picasso.with(SettingActivity.this).load(image).placeholder(R.drawable.defaultuser).into(mDisplayImage);
                    Picasso.with(SettingActivity.this).load(image).
                            networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.defaultuser).into(mDisplayImage, new Callback() {
                        @Override
                        public void onSuccess() {
                            //if successfully get image offline
                        }

                        @Override
                        public void onError() {
                            //if not then load
                            Picasso.with(SettingActivity.this).load(image).placeholder(R.drawable.defaultuser).into(mDisplayImage);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mSettingStatusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String status_value = mStatus.getText().toString();
                Intent statusIntent = new Intent(SettingActivity.this,StatusActivity.class);
                statusIntent.putExtra("status_value",status_value);
                startActivity(statusIntent);
            }
        });

        mImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent,"Select Image"),GALLERY_PICK);

               /* Intent galleryIntent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, GALLERY_PICK);
                */

               /* CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(SettingActivity.this);*/
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //for cropping
        if(requestCode == GALLERY_PICK && resultCode == RESULT_OK){
            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setAspectRatio(1,1) //for square pixel image
                    .start(this);
        }
        //to get the selected image
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                //progressDialog
                 mProgress = new ProgressDialog(this);
                 mProgress.setTitle("Uploading Image...");
                 mProgress.setMessage("Please wait while we upload and process the image");
                 mProgress.setCanceledOnTouchOutside(false);
                 mProgress.show();

                Uri resultUri = result.getUri();
                //compress Thumb Image
                File thumbFile = new File(resultUri.getPath());
                String current_user_id = mCurrentUser.getUid();

                Bitmap thumbBitmap= null;
                try {
                    thumbBitmap = new Compressor(this)
                    .setMaxHeight(200)
                    .setMaxWidth(200)
                    .setQuality(75)
                    .compressToBitmap(thumbFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //converted into byte
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumbBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                final byte[] thumb_byte = baos.toByteArray();

                StorageReference filePath = mImageStorage.child("profile_images").child(current_user_id+".jpg");
                //Storage Reference for thumbImage
                final StorageReference thumbFilePath = mImageStorage.child("profile_images").child("thumbs").child(current_user_id+".jpg");

                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            /*@SuppressWarnings("VisibleForTests")final */

                            final String downloadUrl = task.getResult().getDownloadUrl().toString();
                            UploadTask uploadTask = thumbFilePath.putBytes(thumb_byte);

                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {
                                    String thumbDownloadUrl= thumb_task.getResult().getDownloadUrl().toString();

                                    if(thumb_task.isSuccessful()) {
                                        Map update_hashmap = new HashMap<>();
                                        update_hashmap.put("image",downloadUrl);
                                        update_hashmap.put("thumb_image",thumbDownloadUrl);

                                        mUserDatabase.updateChildren(update_hashmap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    mProgress.dismiss();
                                                    Toast.makeText(SettingActivity.this, "Success Uploading", Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });
                                    }
                                    else{
                                        Toast.makeText(SettingActivity.this,"Error in uploading Thumb Image",Toast.LENGTH_LONG).show();
                                        mProgress.dismiss();
                                    }
                                }
                            });
                        }
                        else{
                            Toast.makeText(SettingActivity.this,"Error in uploading",Toast.LENGTH_LONG).show();
                            mProgress.dismiss();
                        }
                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mUserDatabase.child("online").setValue("false");
    }

    @Override
    protected void onResume() {
        super.onResume();
        mUserDatabase.child("online").setValue("true");

    }

}
