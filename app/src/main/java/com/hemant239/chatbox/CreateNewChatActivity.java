package com.hemant239.chatbox;

import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hemant239.chatbox.user.UserAdapter;
import com.hemant239.chatbox.user.UserObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

public class CreateNewChatActivity extends AppCompatActivity {

    Button mCreateChat;
    EditText mChatName;

    static Context context;

    RecyclerView mUserList;
    RecyclerView.Adapter<UserAdapter.ViewHolder> mUserListAdapter;
    RecyclerView.LayoutManager mUserListLayoutManager;

    ArrayList<UserObject> userList;

    HashMap<String, UserObject> contacts;


    boolean isSingleChatActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_chat);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);


        userList = new ArrayList<>();
        contacts = AllChatsActivity.allContacts;


        context = this;

        isSingleChatActivity = getIntent().getBooleanExtra("isSingleChatActivity", false);

        initializeViews();
        initializeRecyclerViews();

        if (isSingleChatActivity) {
            mCreateChat.setVisibility(View.GONE);
            mChatName.setVisibility(View.GONE);
        }

        if(!isSingleChatActivity) {
            mCreateChat.setOnClickListener(v -> {
                if (mChatName != null) {
                    String s = mChatName.getText().toString().trim();
                    if (s.length() != 0) {
                        createChat();
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), "Give the name to the chat mf", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        getContactList();
    }

    private void getContactList() {
        for (final UserObject userObject : contacts.values()) {
            final String userKey = userObject.getUid();
            FirebaseDatabase.getInstance().getReference().child("Users").child(userKey).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String userName = userObject.getName();
                        String userPhone = userObject.getPhoneNumber();
                        String userImage = "";
                        String userStatus = "";
                        String chatId = userObject.getChatID();
                        String notificationKey = "";
                        if (snapshot.child("Profile Image Uri").getValue() != null) {
                            userImage = Objects.requireNonNull(snapshot.child("Profile Image Uri").getValue()).toString();
                        }
                        if (snapshot.child("Status").getValue() != null) {
                            userStatus = Objects.requireNonNull(snapshot.child("Status").getValue()).toString();
                        }
                        if (snapshot.child("notificationKey").getValue() != null) {
                            notificationKey = Objects.requireNonNull(snapshot.child("notificationKey").getValue()).toString();
                        }

                        UserObject newUser = new UserObject(userKey, userName, userPhone, userStatus, userImage, chatId, notificationKey);
                        userList.add(newUser);
                        mUserListAdapter.notifyItemInserted(userList.size() - 1);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    private void createChat() {

        DatabaseReference mChatDB = FirebaseDatabase.getInstance().getReference().child("Chats");
        String key = mChatDB.push().getKey();
        DatabaseReference mUserDB = FirebaseDatabase.getInstance().getReference().child("Users");

        assert key != null;
        mChatDB = mChatDB.child(key).child("info");

        HashMap<String,Object> mChatInfo=new HashMap<>();

        int noOfUsers=1;
        Date date= Calendar.getInstance().getTime();

        userList.add(AllChatsActivity.curUser);
        for(UserObject user:userList){
            if (user.isSelected() && user.getUid() != null) {
                noOfUsers++;
                mChatInfo.put("user/" + user.getUid() + "/notificationKey", user.getNotificationKey());
                mChatInfo.put("user/" + user.getUid() + "/lastMessageId", true);
                mUserDB.child(user.getUid()).child("chat").child(key).setValue(-date.getTime());
            }
        }
        mChatInfo.put("Name",mChatName.getText().toString());
        mChatInfo.put("ID",key);
        mChatInfo.put("Number Of Users",noOfUsers);

        FirebaseUser mUser= FirebaseAuth.getInstance().getCurrentUser();
        if(mUser!=null) {
            mChatInfo.put("user/" + mUser.getUid() + "/notificationKey", true);
            mChatInfo.put("user/" + mUser.getUid() + "/lastMessageId", true);
            mUserDB.child(mUser.getUid()).child("chat").child(key).setValue(-date.getTime());
        }
        mChatDB.updateChildren(mChatInfo);
    }

    private void initializeViews() {
        mCreateChat = findViewById(R.id.newChat);
        mChatName=findViewById(R.id.chatName);
    }
    private void initializeRecyclerViews() {
        mUserList = findViewById(R.id.recyclerViewList);
        mUserList.setHasFixedSize(false);
        mUserList.setNestedScrollingEnabled(false);

        mUserListAdapter = new UserAdapter(userList, this, isSingleChatActivity, false);
        mUserList.setAdapter(mUserListAdapter);

        mUserListLayoutManager = new LinearLayoutManager(getApplicationContext(), RecyclerView.VERTICAL, false);
        mUserList.setLayoutManager(mUserListLayoutManager);
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