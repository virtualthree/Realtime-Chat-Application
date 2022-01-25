package com.hemant239.chatbox.utils;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.hemant239.chatbox.AllChatsActivity;
import com.hemant239.chatbox.user.UserObject;

import java.util.HashMap;
import java.util.Objects;

public class AllContacts {

    Context context;
    HashMap<String, UserObject> allContacts, tempContacts;

    HashMap<String, Boolean> userChecked;


    public AllContacts(Context context, HashMap<String, UserObject> allContacts) {
        this.context = context;
        this.allContacts = allContacts;
        userChecked = new HashMap<>();
    }

    public void getAllContacts() {
        tempContacts = new HashMap<>();
        Cursor cursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);

        while (cursor != null && cursor.moveToNext()) {
            String phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));


            phoneNumber = phoneNumber.replace(" ", "");
            phoneNumber = phoneNumber.replace("-", "");
            phoneNumber = phoneNumber.replace("(", "");
            phoneNumber = phoneNumber.replace(")", "");
            phoneNumber = phoneNumber.replace("/", "");
            phoneNumber = phoneNumber.replace("#", "");

            if (phoneNumber.charAt(0) != '+') {
                phoneNumber = getIsoPrefix() + phoneNumber;
            }


            if (allContacts.get(phoneNumber) != null) {
                Objects.requireNonNull(allContacts.get(phoneNumber)).setName(name);
                tempContacts.put(phoneNumber, allContacts.get(phoneNumber));
            } else {
                String curUserPhone = AllChatsActivity.curUser.getPhoneNumber();
                if (!phoneNumber.equals(curUserPhone) && userChecked.get(phoneNumber) == null) {
                    userChecked.put(phoneNumber, true);
                    checkUserExists(phoneNumber, name);
                }
            }
        }
        assert cursor != null;
        cursor.close();


        AllChatsActivity.allContacts = tempContacts;

    }

    private void checkUserExists(final String phoneNumber, final String name) {

        DatabaseReference mUserDB = FirebaseDatabase.getInstance().getReference().child("Users");
        Query query = mUserDB.orderByChild("Phone Number").equalTo(phoneNumber);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                        String userImage = "";
                        String userKey;
                        userKey = childSnapshot.getKey();

                        if (childSnapshot.child("Profile Image Uri").getValue() != null) {
                            userImage = Objects.requireNonNull(childSnapshot.child("Profile Image Uri").getValue()).toString();
                        }
                        checkChatExists(userKey, userImage, name, phoneNumber);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void checkChatExists(final String userKey, final String userImage, final String name, final String phoneNumber) {
        final String curUserKey = AllChatsActivity.curUser.getUid();
        DatabaseReference mUserDB = FirebaseDatabase.getInstance().getReference().child("Users").child(curUserKey);
        mUserDB.child("Single chats").child(userKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists() && snapshot.getValue() != null) {
                    UserObject userObject = new UserObject(userKey, name, phoneNumber, snapshot.getValue().toString());
                    allContacts.put(phoneNumber, userObject);
                    tempContacts.put(phoneNumber, userObject);
                } else {
                    createChat(userKey, curUserKey, userImage, name, phoneNumber);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void createChat(String userKey, String curUserKey, String userImage, String name, String phoneNumber) {
        String chatKey = FirebaseDatabase.getInstance().getReference().child("Chats").push().getKey();
        DatabaseReference mUserDB = FirebaseDatabase.getInstance().getReference().child("Users");

        mUserDB.child(curUserKey).child("Single chats").child(userKey).setValue(chatKey);
        mUserDB.child(userKey).child("Single chats").child(curUserKey).setValue(chatKey);

        HashMap<String, Object> mChatInfo = new HashMap<>();

        mChatInfo.put("ID", chatKey);
        mChatInfo.put("isSingleChat", true);
        mChatInfo.put("Number Of Users", 1);
        mChatInfo.put("user/" + curUserKey + "/notificationKey", true);
        mChatInfo.put("user/" + userKey + "/notificationKey", true);
        mChatInfo.put("user/" + userKey + "/lastMessageId", true);
        mChatInfo.put("user/" + curUserKey + "/lastMessageId", true);
        mChatInfo.put(userKey + "/Name", AllChatsActivity.curUser.getPhoneNumber());
        mChatInfo.put(curUserKey + "/Name", phoneNumber);
        mChatInfo.put(userKey + "/Chat Profile Image Uri", AllChatsActivity.curUser.getProfileImageUri());
        mChatInfo.put(curUserKey + "/Chat Profile Image Uri", userImage);

        assert chatKey != null;
        FirebaseDatabase.getInstance().getReference().child("Chats").child(chatKey).child("info").updateChildren(mChatInfo);

        UserObject userObject = new UserObject(userKey, name, phoneNumber, chatKey);
        allContacts.put(phoneNumber, userObject);
        tempContacts.put(phoneNumber, userObject);
    }

    private String getIsoPrefix() {

        String iso = "";

        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null) {
            iso = CountryToPhonePrefix.prefixFor(telephonyManager.getNetworkCountryIso());
        }
        return iso;

    }
}
