package com.hemant239.chatbox;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hemant239.chatbox.user.UserObject;

import java.util.Objects;

public class UserDetailsActivity extends AppCompatActivity {


    TextView mUserName,
            mUserPhone,
            mUserStatus;

    final int CHANGE_PROFILE_PHOTO_CODE = 1;
    ImageView mUserImage;
    final int CANCEL_UPLOAD_TASK = 3;
    EditText mUserEditStatus;
    Button mChangePhoto,
            mChangeStatus;
    String userId,
            userName,
            userPhone,
            userStatus,
            userImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        UserObject userObject = (UserObject) getIntent().getSerializableExtra("userObject");
        assert userObject != null;
        userId = userObject.getUid();
        userName = userObject.getName();
        userPhone = userObject.getPhoneNumber();
        userStatus = userObject.getStatus();
        userImage = userObject.getProfileImageUri();

        initializeViews();

        mUserName.setText(userName);
        mUserPhone.setText(userPhone);
        mUserStatus.setText(userStatus);

        if(!userImage.equals("")) {
            mUserImage.setClipToOutline(true);
            Glide.with(this).load(Uri.parse(userImage)).into(mUserImage);
            mUserImage.setOnClickListener(v -> {
                Intent intent = new Intent(getApplicationContext(), ImageViewActivity.class);
                intent.putExtra("URI", userImage);
                startActivity(intent);
            });
        }

        String curUserKey = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        if (curUserKey.equals(userId)) {
            mUserEditStatus.setText(userStatus);
            mUserStatus.setVisibility(View.GONE);
            mUserEditStatus.setVisibility(View.VISIBLE);
            mChangeStatus.setVisibility(View.VISIBLE);
            mChangePhoto.setVisibility(View.VISIBLE);

            mChangePhoto.setOnClickListener(v -> openGallery());

            mChangeStatus.setOnClickListener(v -> changeStatus());

        }
    }

    private void changeStatus() {
        String status = mUserEditStatus.getText().toString();
        if (status.equals("")) {
            Toast.makeText(getApplicationContext(), "Status cannot be empty", Toast.LENGTH_SHORT).show();
        } else if (status.equals(userStatus)) {
            Toast.makeText(getApplicationContext(), "Status is updated", Toast.LENGTH_SHORT).show();
        } else {
            FirebaseDatabase.getInstance().getReference().child("Users/" + userId + "/Status").setValue(status);
            AllChatsActivity.curUser.setStatus(status);
            Toast.makeText(getApplicationContext(), "Status is updated", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select an image"), CHANGE_PROFILE_PHOTO_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CHANGE_PROFILE_PHOTO_CODE:
                    final StorageReference profileStorage = FirebaseStorage.getInstance().getReference().child("ProfilePhotos").child(userId);
                    final DatabaseReference mUserDb = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);

                    assert data != null;
                    final UploadTask uploadTask = profileStorage.putFile(Objects.requireNonNull(data.getData()));
                    Intent intent = new Intent(getApplicationContext(), LoadingActivity.class);
                    intent.putExtra("message", "Your Image is being uploaded \\n please wait");
                    intent.putExtra("isNewUser", false);
                    startActivityForResult(intent, CANCEL_UPLOAD_TASK);
                    uploadTask.addOnSuccessListener(taskSnapshot -> profileStorage.getDownloadUrl().addOnSuccessListener(uri -> {
                        changeProfilePhoto(uri);
                        mUserDb.child("Profile Image Uri").setValue(uri.toString());
                        AllChatsActivity.curUser.setProfileImageUri(uri.toString());
                        Glide.with(getApplicationContext()).load(uri).into(mUserImage);
                        ((LoadingActivity) LoadingActivity.context).finish();
                    }));

                    break;

                case 100:
                    break;

                default:
                    Toast.makeText(getApplicationContext(), "something went wrong, please try again later onActivity result", Toast.LENGTH_SHORT).show();

            }

        }
    }

    private void changeProfilePhoto(final Uri uri) {
        final DatabaseReference mUserDb = FirebaseDatabase.getInstance().getReference().child("Users").child(userId).child("Single chats");
        final DatabaseReference mChatDb = FirebaseDatabase.getInstance().getReference().child("Chats");
        mUserDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                        String userKey = childSnapshot.getKey();
                        String chatKey = Objects.requireNonNull(childSnapshot.getValue()).toString();
                        assert userKey != null;
                        mChatDb.child(chatKey + "/info/" + userKey + "/Chat Profile Image Uri").setValue(uri.toString());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void initializeViews() {
        mUserName = findViewById(R.id.userDetailName);
        mUserPhone = findViewById(R.id.userDetailPhone);
        mUserStatus = findViewById(R.id.userDetailStatus);
        mUserImage = findViewById(R.id.userDetailProfileImage);
        mUserEditStatus = findViewById(R.id.userDetailEditStatus);

        mChangePhoto = findViewById(R.id.changeProfilePicture);
        mChangeStatus = findViewById(R.id.changeStatus);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;

            default:
                Toast.makeText(getApplicationContext(), "choose a valid button", Toast.LENGTH_SHORT).show();
        }
        return true;
    }
}