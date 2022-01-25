package com.hemant239.chatbox;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hemant239.chatbox.chat.ChatObject;
import com.hemant239.chatbox.user.UserObject;

import java.util.HashMap;
import java.util.Objects;

public class CreateSingleChatActivity extends AppCompatActivity {

    String userKey,
            userName,
            userPhone,
            userImage,
            chatID;

    UserObject curUser, user;

    Button mCancelButton;


    DatabaseReference mUserDb,
            mChatDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_single_chat);

        initializeViews();

        curUser = AllChatsActivity.curUser;
        user = (UserObject) getIntent().getSerializableExtra("userObject");

        assert user != null;
        userKey = user.getUid();
        userName = user.getName();
        userPhone = user.getPhoneNumber();
        userImage = user.getProfileImageUri();
        chatID = user.getChatID();


        mCancelButton.setOnClickListener(v -> finish());


        if (!chatID.equals("")) {
            openChat(chatID);
        } else {
            checkChat();
        }


    }

    private void checkChat() {
        mUserDb = FirebaseDatabase.getInstance().getReference().child("Users").child(curUser.getUid()).child("Single chats").child(userKey);
        mUserDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getValue() != null) {
                    openChat(Objects.requireNonNull(snapshot.getValue()).toString());
                } else {
                    createChat();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void createChat() {
        mChatDb = FirebaseDatabase.getInstance().getReference().child("Chats");
        final String key = mChatDb.push().getKey();
        HashMap<String, Object> mUserInfo = new HashMap<>();


        mUserInfo.put(userKey + "/Single chats/" + curUser.getUid(), key);
        mUserInfo.put(curUser.getUid() + "/Single chats/" + userKey, key);


        FirebaseDatabase.getInstance().getReference().child("Users").updateChildren(mUserInfo);

        HashMap<String, Object> mChatInfo = new HashMap<>();

        mChatInfo.put("ID", key);
        mChatInfo.put("isSingleChat", true);
        mChatInfo.put("Number Of Users", 1);
        mChatInfo.put("user/" + curUser.getUid() + "/notificationKey", true);
        mChatInfo.put("user/" + userKey + "/notificationKey", true);
        mChatInfo.put("user/" + curUser.getUid() + "/lastMessageId", true);
        mChatInfo.put("user/" + userKey + "/lastMessageId", true);
        mChatInfo.put(userKey + "/Name", curUser.getPhoneNumber());
        mChatInfo.put(curUser.getUid() + "/Name", userPhone);
        mChatInfo.put(userKey + "/Chat Profile Image Uri", curUser.getProfileImageUri());
        mChatInfo.put(curUser.getUid() + "/Chat Profile Image Uri", userImage);


        assert key != null;
        mChatDb.child(key).child("info").updateChildren(mChatInfo).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                openChat(key);
            } else {
                Toast.makeText(getApplicationContext(), "chat loading failed", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void openChat(final String chatKey) {
        mChatDb = FirebaseDatabase.getInstance().getReference().child("Chats").child(chatKey).child("info").child("Last Message");
        mChatDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String lastMessageId = "";
                if (snapshot.exists() && snapshot.getValue() != null) {
                    lastMessageId = Objects.requireNonNull(snapshot.getValue()).toString();
                }
                ChatObject chatObject = new ChatObject(chatKey, userName, userImage, lastMessageId, 1, true);
                Intent intent = new Intent(getApplicationContext(), SpecificChatActivity.class);
                intent.putExtra("chatObject", chatObject);
                startActivity(intent);
                ((CreateNewChatActivity) CreateNewChatActivity.context).finish();
                finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void initializeViews() {
        mCancelButton=findViewById(R.id.cancelChatLoadButton);
    }
}