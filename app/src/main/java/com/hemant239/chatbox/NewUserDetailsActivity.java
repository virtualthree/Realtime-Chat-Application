package com.hemant239.chatbox;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hemant239.chatbox.user.UserObject;
import com.onesignal.OneSignal;

import java.util.HashMap;
import java.util.Objects;

public class NewUserDetailsActivity extends AppCompatActivity {


    EditText    mName,
                mStatus;

    ImageView mImage;

    String imageUri;

    Button mSubmit;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user_details);
        initializeViews();

        imageUri = "";


        mImage.setOnClickListener(v -> openGallery());
        mImage.setClipToOutline(true);

        final String phoneNumber = getIntent().getStringExtra("phoneNumber");

        mSubmit.setOnClickListener(v -> {
            if (mName != null && !mName.getText().toString().equals("")) {
                uploadDetailsOnFirebase(phoneNumber);
            } else {
                Toast.makeText(getApplicationContext(), "Your parents have given you such a beautiful name, don't  be ashamed of using it", Toast.LENGTH_LONG).show();
            }
        });


    }

    private void uploadDetailsOnFirebase(final String phoneNumber) {
        final String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        final StorageReference profileStorage = FirebaseStorage.getInstance().getReference().child("ProfilePhotos").child(userId);
        final DatabaseReference mUserDb = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);

        final HashMap<String, Object> mUserInfo = new HashMap<>();

        String notificationKey = Objects.requireNonNull(OneSignal.getDeviceState()).getUserId();
        mUserInfo.put("Name", mName.getText().toString());
        mUserInfo.put("Phone Number", phoneNumber);
        mUserInfo.put("Status", mStatus.getText().toString());
        mUserInfo.put("notificationKey", notificationKey);


        if (!imageUri.equals("")) {
            final UploadTask uploadTask = profileStorage.putFile(Objects.requireNonNull(Uri.parse(imageUri)));
            Intent intent = new Intent(getApplicationContext(), LoadingActivity.class);
            intent.putExtra("message", "Your Account is being created \n please wait");
            intent.putExtra("isNewUser", true);
            startActivity(intent);
            uploadTask.addOnSuccessListener(taskSnapshot -> profileStorage.getDownloadUrl().addOnSuccessListener(uri -> {
                mUserInfo.put("Profile Image Uri", uri.toString());
                mUserDb.updateChildren(mUserInfo);
                ((LoadingActivity) LoadingActivity.context).finish();
                UserObject userObject = new UserObject(userId, mName.getText().toString(), phoneNumber, mStatus.getText().toString(), uri.toString(), "", notificationKey);
                userLoggedIn(userObject);
            }));
        }
        else {
            mUserDb.updateChildren(mUserInfo);
            UserObject userObject = new UserObject(userId, mName.getText().toString(), phoneNumber, mStatus.getText().toString(), "", "", notificationKey);
            userLoggedIn(userObject);
        }

    }


    private void userLoggedIn(UserObject userObject) {
        Intent intent = new Intent(this, AllChatsActivity.class);
        intent.putExtra("First time", true);
        intent.putExtra("curUser", userObject);
        startActivity(intent);
        finish();
    }


    final int ADD_PROFILE_PHOTO_CODE=1;
    private void openGallery() {
        Intent intent=new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,false);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select an image"),ADD_PROFILE_PHOTO_CODE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode==RESULT_OK){
            if(requestCode==ADD_PROFILE_PHOTO_CODE){
                assert data != null;
                imageUri= Objects.requireNonNull(data.getData()).toString();
                Glide.with(getApplicationContext()).load(data.getData()).into(mImage);
            }
        }
        else{
            Toast.makeText(getApplicationContext(),"something went wrong, please try again later onActivity result",Toast.LENGTH_SHORT).show();
        }
    }

    private void initializeViews() {
        mName  = findViewById(R.id.name);
        mStatus= findViewById(R.id.status);
        mImage = findViewById(R.id.newUserProfileImage);
        mSubmit= findViewById(R.id.submitDetails);
    }
}