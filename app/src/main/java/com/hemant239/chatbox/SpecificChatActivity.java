package com.hemant239.chatbox;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hemant239.chatbox.chat.ChatObject;
import com.hemant239.chatbox.message.MessageAdapter;
import com.hemant239.chatbox.message.MessageObject;
import com.hemant239.chatbox.user.UserObject;
import com.onesignal.OneSignal;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.Scanner;

public class SpecificChatActivity extends AppCompatActivity implements MessageAdapter.OnItemDoubleClickListener {

    EditText mMessageText;
    Button mSendMessage,
            mAddMedia;

    public static RecyclerView.LayoutManager mMessageListLayoutManager;
    TextView chatName,
            mTaggedSender,
            mTaggedText;
    ImageView chatPhoto,
            mTaggedImage;
    RelativeLayout taggedLayout;
    Button cancelTagged;

    TextView onDateScrolling;

    RecyclerView mMessageList;
    public static RecyclerView.Adapter<MessageAdapter.ViewHolder> mMessageAdapter;
    int taggedPosition = -1;

    ArrayList<MessageObject> messageList;

    StorageReference profileStorage;
    UploadTask uploadTask;

    String chatKey;

    String mediaAdded;

    ChatObject curChatObject;

    UserObject userObject;
    String curUserPhone;

    float density;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_specific_chat);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Objects.requireNonNull(getSupportActionBar()).setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setCustomView(R.layout.app_bar_specific_chat);
        View view = getSupportActionBar().getCustomView();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        density = displayMetrics.density;

        curUserPhone = AllChatsActivity.curUser.getPhoneNumber();

        chatName = view.findViewById(R.id.chatAppName);
        chatPhoto = view.findViewById(R.id.chatAppProfileImage);

        chatPhoto.setClipToOutline(true);

        userObject = new UserObject();
        curChatObject = (ChatObject) getIntent().getSerializableExtra("chatObject");
        assert curChatObject != null;
        chatKey = curChatObject.getUid();

        chatName.setText(curChatObject.getName());
        if (curChatObject.getImageUri() != null && !curChatObject.getImageUri().equals("")) {
            Glide.with(getApplicationContext()).load(Uri.parse(curChatObject.getImageUri())).into(chatPhoto);
        }
        chatPhoto.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), ImageViewActivity.class);
            intent.putExtra("URI", curChatObject.getImageUri());
            startActivity(intent);

        });


        initializeViews();
        messageList = new ArrayList<>();
        initializeRecyclerViews(density);

        final String curUserKey = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();


        if(curChatObject.isSingleChat()){
            FirebaseDatabase.getInstance().getReference().child("Chats/"+chatKey+"/info/user").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        for(DataSnapshot childSnapshot:snapshot.getChildren()){
                            if(!Objects.equals(childSnapshot.getKey(), curUserKey)){
                                getUserDetails(childSnapshot.getKey());
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });


            chatName.setOnClickListener(v -> {
                Intent intent = new Intent(getApplicationContext(), UserDetailsActivity.class);
                intent.putExtra("userObject", userObject);
                startActivity(intent);
            });

        } else {
            chatName.setOnClickListener(v -> {
                Intent intent = new Intent(getApplicationContext(), GroupDetailsActivity.class);
                intent.putExtra("Key", curChatObject.getUid());
                intent.putExtra("Name", curChatObject.getName());
                intent.putExtra("Image", curChatObject.getImageUri());
                startActivity(intent);
            });


        }

        mediaAdded="";

        getMessageList(chatKey);

        mSendMessage.setOnClickListener(v -> sendMessage(chatKey));

        mAddMedia.setOnClickListener(v -> openGalleryToAddMedia());

        mMessageList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    onDateScrolling.setVisibility(View.GONE);
                } else if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    int position = ((LinearLayoutManager) mMessageListLayoutManager).findFirstCompletelyVisibleItemPosition();
                    if (position > 0) {
                        onDateScrolling.setText(messageList.get(position).getDate());
                        onDateScrolling.setVisibility(View.VISIBLE);
                    }
                }
            }
        });


        cancelTagged.setOnClickListener(v -> {
            taggedLayout.setVisibility(View.GONE);
            taggedPosition = -1;
        });


    }

    private void getUserDetails(final String userKey) {
        FirebaseDatabase.getInstance().getReference().child("Users").child(userKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String userName="";
                    String userPhone = "";
                    String userImage = "";
                    String userStatus = "";
                    String chatID = "";
                    String notificationKey = "";

                    if (snapshot.child("Name").getValue() != null) {
                        userName = Objects.requireNonNull(snapshot.child("Name").getValue()).toString();
                    }
                    if (snapshot.child("Phone Number").getValue() != null) {
                        userPhone = Objects.requireNonNull(snapshot.child("Phone Number").getValue()).toString();
                    }
                    if (snapshot.child("Profile Image Uri").getValue() != null) {
                        userImage = Objects.requireNonNull(snapshot.child("Profile Image Uri").getValue()).toString();
                    }
                    if (snapshot.child("Status").getValue() != null) {
                        userStatus = Objects.requireNonNull(snapshot.child("Status").getValue()).toString();
                    }
                    if (snapshot.child("notificationKey").getValue() != null) {
                        notificationKey = Objects.requireNonNull(snapshot.child("notificationKey").getValue()).toString();
                    }

                    userName = userPhone;
                    if (AllChatsActivity.allContacts.get(userPhone) != null) {
                        userName = Objects.requireNonNull(AllChatsActivity.allContacts.get(userPhone)).getName();
                        chatID = Objects.requireNonNull(AllChatsActivity.allContacts.get(userPhone)).getChatID();
                    }
                    userObject = new UserObject(userKey, userName, userPhone, userStatus, userImage, chatID, notificationKey);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    final int ADD_MEDIA_CODE=2;
    private void openGalleryToAddMedia() {
        Intent intent=new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,false);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select an image"),ADD_MEDIA_CODE);
    }

    final int CHANGE_CHAT_PHOTO_CODE=1;
    final int CANCEL_UPLOAD_TASK=3;
    private void openGalleryToChangeProfilePhoto() {
        Intent intent=new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,false);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select an image"),CHANGE_CHAT_PHOTO_CODE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            switch (requestCode) {
                case CHANGE_CHAT_PHOTO_CODE:

                    profileStorage = FirebaseStorage.getInstance().getReference().child("ChatProfilePhotos").child(chatKey);
                    final DatabaseReference mChatDb = FirebaseDatabase.getInstance().getReference().child("Chats").child(chatKey).child("info");

                    assert data != null;
                    uploadTask = profileStorage.putFile(Objects.requireNonNull(data.getData()));
                    Intent intent = new Intent(getApplicationContext(), LoadingActivity.class);
                    intent.putExtra("message", "Your Image is being uploaded \\n please wait");
                    intent.putExtra("isNewUser", false);
                    startActivityForResult(intent, CANCEL_UPLOAD_TASK);
                    uploadTask.addOnSuccessListener(taskSnapshot -> profileStorage.getDownloadUrl().addOnSuccessListener(uri -> {
                        mChatDb.child("Chat Profile Image Uri").setValue(uri.toString());
                        Glide.with(getApplicationContext()).load(uri).into((ImageView) findViewById(R.id.chatAppProfileImage));
                        ((LoadingActivity) LoadingActivity.context).finish();
                    }));

                    break;


                case ADD_MEDIA_CODE:
                    assert data != null;
                    mediaAdded = Objects.requireNonNull(data.getData()).toString();
                    break;

                case CANCEL_UPLOAD_TASK:
                    if(uploadTask.isComplete()){
                        profileStorage.delete();
                    }
                    uploadTask.cancel();
                    break;

                default:
                    Toast.makeText(getApplicationContext(),"something went wrong, please try again later",Toast.LENGTH_SHORT).show();

            }

        }
    }

    private void getMessageList(final String key) {

        FirebaseUser mUser=FirebaseAuth.getInstance().getCurrentUser();
        final DatabaseReference mMessageDb=FirebaseDatabase.getInstance().getReference().child("Chats").child(key).child("Messages");

        if(mUser!=null){


            mMessageDb.orderByPriority().addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        if(Objects.requireNonNull(snapshot.getValue()).toString().equals("true")) {
                            mMessageDb.setValue(null);
                            messageList = new ArrayList<>();
                            initializeRecyclerViews(density);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });


            mMessageDb.orderByPriority().addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        int numberOfMessages= (int) snapshot.getChildrenCount();
                        for(int i=0;i<numberOfMessages;i++){
                            messageList.add(new MessageObject());
                        }


                        for(DataSnapshot childSnapshot:snapshot.getChildren()) {
                            numberOfMessages--;
                            if (childSnapshot.exists() && !childSnapshot.child("Deleted For/" + AllChatsActivity.curUser.getUid()).exists()) {
                                getMessageData(childSnapshot.getKey(), childSnapshot, false, numberOfMessages);
                            } else {
                                messageList.remove(0);
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            mMessageDb.limitToFirst(1).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    if (snapshot.exists() && !Objects.equals(snapshot.getKey(), curChatObject.getLastMessageId()) && !snapshot.child("Deleted For/" + AllChatsActivity.curUser.getUid()).exists()) {
                        getMessageData(snapshot.getKey(), snapshot, true, 0);
                    }
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }

    }

    private void getMessageData(final String messageKey, DataSnapshot snapshot, boolean isNewMessage, int index) {
        String senderId = "",
                senderPhone = "",
                senderName = "",
                text = "",
                imageUri = "",
                time = "",
                date = "",
                taggedId = "",
                taggedText = "",
                taggedSender = "",
                taggedImage = "";


        long timeStampLong = 0L;
        boolean isInfo = false;
        boolean isDeletedForEveryone = false;
        boolean isTagged = false;


        if (snapshot.child("Sender").getValue() != null) {
            senderId = Objects.requireNonNull(snapshot.child("Sender").getValue()).toString();
        }
        if (snapshot.child("Sender Phone").getValue() != null) {
            senderPhone = Objects.requireNonNull(snapshot.child("Sender Phone").getValue()).toString();
        }
        if (snapshot.child("text").getValue() != null) {
            text = Objects.requireNonNull(snapshot.child("text").getValue()).toString();
        }
        if (snapshot.child("Image Uri").getValue() != null) {
            imageUri = Objects.requireNonNull(snapshot.child("Image Uri").getValue()).toString();
        }
        if (snapshot.child("timestamp").getValue() != null) {
            time = Objects.requireNonNull(snapshot.child("timestamp").getValue()).toString();
        }
        if (snapshot.child("date").getValue() != null) {
            date = Objects.requireNonNull(snapshot.child("date").getValue()).toString();
        }
        if (snapshot.child("timestampLong").getValue() != null) {
            timeStampLong = Long.parseLong(Objects.requireNonNull(snapshot.child("timestampLong").getValue()).toString());
        }
        if (snapshot.child("isInfo").getValue() != null) {
            isInfo = true;
        }

        if (snapshot.child("isTagged").getValue() != null) {
            isTagged = true;
            if (snapshot.child("taggedId").getValue() != null) {
                taggedId = Objects.requireNonNull(snapshot.child("taggedId").getValue()).toString();
            }
            if (snapshot.child("taggedSender").getValue() != null) {
                taggedSender = Objects.requireNonNull(snapshot.child("taggedSender").getValue()).toString();
            }
            if (snapshot.child("taggedText").getValue() != null) {
                taggedText = Objects.requireNonNull(snapshot.child("taggedText").getValue()).toString();
            }
            if (snapshot.child("taggedImage").getValue() != null) {
                taggedImage = Objects.requireNonNull(snapshot.child("taggedImage").getValue()).toString();
            }

            if (AllChatsActivity.allContacts.get(taggedSender) != null) {
                taggedSender = Objects.requireNonNull(AllChatsActivity.allContacts.get(taggedSender)).getName();
            } else if (curUserPhone.equals(taggedSender)) {
                taggedSender = "You";
            }
        }

        if (AllChatsActivity.allContacts.get(senderPhone) != null) {
            senderName = Objects.requireNonNull(AllChatsActivity.allContacts.get(senderPhone)).getName();
        } else if (curUserPhone.equals(senderPhone)) {
            senderName = "You";
        } else {
            senderName = String.valueOf(senderPhone);
        }

        if (snapshot.child("Deleted For Everyone").getValue() != null) {
            isDeletedForEveryone = true;
            if (Objects.requireNonNull(snapshot.child("Deleted For Everyone").getValue()).toString().equals(AllChatsActivity.curUser.getUid())) {
                text = "You Deleted this Message";
            } else {
                text = "This Message was Deleted";
            }
        }


        FirebaseDatabase.getInstance().getReference().child("Chats/" + chatKey + "/Messages/" + messageKey + "/Deleted For Everyone").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    MessageObject messageObject = new MessageObject(messageKey);
                    int indexOfMessage = messageList.indexOf(messageObject);
                    String deletedText = "This Message Was Deleted";
                    if (Objects.requireNonNull(snapshot.getValue()).toString().equals(AllChatsActivity.curUser.getUid())) {
                        deletedText = "You Deleted This Message";
                    }
                    if (indexOfMessage > -1) {
                        messageList.get(indexOfMessage).setText(deletedText);
                        messageList.get(indexOfMessage).setImageUri("");
                        messageList.get(indexOfMessage).setDeletedForEveryone(true);
                        messageList.get(indexOfMessage).setTagged(false);
                        mMessageAdapter.notifyItemChanged(indexOfMessage);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        MessageObject newMessage = new MessageObject(messageKey, text, imageUri, senderId, senderPhone, senderName, time, date, timeStampLong,
                isInfo, isDeletedForEveryone, isTagged, taggedId, taggedSender, taggedText, taggedImage);

        if (isNewMessage) {
            messageList.add(newMessage);
            if (senderId.equals(AllChatsActivity.curUser.getUid())) {
                mMessageListLayoutManager.scrollToPosition(messageList.size() - 1);
            }
            mMessageAdapter.notifyItemInserted(messageList.size() - 1);
        } else {
            messageList.set(index, newMessage);
            mMessageListLayoutManager.scrollToPosition(messageList.size() - 1);
            mMessageAdapter.notifyItemChanged(index);
        }

    }

    private void sendMessage(final String chatKey) {

        final DatabaseReference mChatDb = FirebaseDatabase.getInstance().getReference().child("Chats").child(chatKey);
        final FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();

        final DatabaseReference mUserDb = FirebaseDatabase.getInstance().getReference().child("Users");

        final HashMap<String, Object> newMessageMap = new HashMap<>();

        if (mUser != null && (!mMessageText.getText().toString().equals("") || !mediaAdded.equals(""))) {

            Calendar calendar = Calendar.getInstance();
            final Date date = calendar.getTime();

            SimpleDateFormat simpleDateFormatTime = new SimpleDateFormat("h:mm a");
            String time = simpleDateFormatTime.format(date);
            SimpleDateFormat simpleDateFormatDate = new SimpleDateFormat("EEE, MMM dd, yyyy");
            String dateTemp = simpleDateFormatDate.format(date);


            if (messageList.size() == 0 || (messageList.get(messageList.size() - 1).getDate() != null && !messageList.get(messageList.size() - 1).getDate().equals(dateTemp.toUpperCase()))) {
                final String dateMessageId = mChatDb.child("Messages").push().getKey();
                HashMap<String, Object> newDateMap = new HashMap<>();
                newDateMap.put("isInfo", true);
                newDateMap.put("date", dateTemp.toUpperCase());

                assert dateMessageId != null;
                mChatDb.child("Messages").child(dateMessageId).updateChildren(newDateMap);
                mChatDb.child("Messages").child(dateMessageId).setPriority(-date.getTime() + 1);
            }


            final String messageId = mChatDb.child("Messages").push().getKey();

            newMessageMap.put("Sender", mUser.getUid());
            newMessageMap.put("Sender Phone", curUserPhone);
            if (!mMessageText.getText().toString().equals("")) {
                newMessageMap.put("text", mMessageText.getText().toString());
            }
            newMessageMap.put("timestampLong", date.getTime());
            newMessageMap.put("timestamp", time.toUpperCase());
            newMessageMap.put("date", dateTemp.toUpperCase());

            if (taggedPosition > -1) {
                MessageObject taggedMessage = messageList.get(taggedPosition);

                newMessageMap.put("isTagged", true);
                newMessageMap.put("taggedId", taggedMessage.getMessageId());
                newMessageMap.put("taggedSender", taggedMessage.getSenderPhone());
                newMessageMap.put("taggedText", taggedMessage.getText());
                newMessageMap.put("taggedImage", taggedMessage.getImageUri());


                taggedPosition = -1;
                taggedLayout.setVisibility(View.GONE);

            }


            String messageText = mMessageText.getText().toString();
            if (messageText.equals("")) {
                messageText = "Photo";
            }
            String finalMessageText = messageText;

            if (!mediaAdded.equals("")) {
                assert messageId != null;
                final StorageReference mediaStorage = FirebaseStorage.getInstance().getReference().child("ChatPhotos").child(chatKey).child(messageId);
                UploadTask uploadTask = mediaStorage.putFile(Uri.parse(mediaAdded));
                uploadTask.addOnSuccessListener(taskSnapshot -> mediaStorage.getDownloadUrl().addOnSuccessListener(uri -> {
                    newMessageMap.put("Image Uri", uri.toString());
                    mChatDb.child("Messages").child(messageId).updateChildren(newMessageMap);
                    mChatDb.child("Messages").child(messageId).setPriority(-date.getTime());
                }));
            } else {
                assert messageId != null;
                mChatDb.child("Messages").child(messageId).updateChildren(newMessageMap);
                mChatDb.child("Messages").child(messageId).setPriority(-date.getTime());
            }

            mChatDb.child("info").child("user").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                            mChatDb.child("info/user/" + Objects.requireNonNull(childSnapshot.getKey()) + "/lastMessageId").setValue(messageId);
                            mUserDb.child(Objects.requireNonNull(childSnapshot.getKey())).child("chat").child(chatKey).setValue(-date.getTime());

                            if (!childSnapshot.getKey().equals(AllChatsActivity.curUser.getUid()) && childSnapshot.child("notificationKey").getValue() != null) {
                                sendNotification(AllChatsActivity.curUser, finalMessageText, mediaAdded, childSnapshot.getKey());
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });


            mMessageText.setText("");
            mediaAdded = "";


        }
    }

    private void sendNotification(UserObject curUser, String messageText, String imageUri, String externalUserId) {
        try {
            String jsonResponse;

            URL url = new URL("https://onesignal.com/api/v1/notifications");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setUseCaches(false);
            con.setDoOutput(true);
            con.setDoInput(true);

            con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            con.setRequestProperty("Authorization", "Basic ZWVlMjJkNWItNjI0OS00ZjhiLWFiZTEtOWVhNmQ3MjZkOTlk");
            con.setRequestMethod("POST");

            String strJsonBody = "{"
                    + "\"app_id\":\"c01d8a82-28eb-4d70-8242-4a6c33a4bdd4\","
                    + "\"include_external_user_ids\":[\"" + externalUserId + "\"],"
                    + "\"channel_for_external_user_ids\":\"push\","
                    + "\"data\":{\"foo\":\"bar\"},"
                    + "\"contents\":{\"en\":\"" + messageText + "\"},"
                    + "\"headings\":{\"en\":\"" + curUserPhone + "\"}"
                    + "}";


            System.out.println("strJsonBody:\n" + strJsonBody);

            byte[] sendBytes = strJsonBody.getBytes("UTF-8");
            con.setFixedLengthStreamingMode(sendBytes.length);

            OutputStream outputStream = con.getOutputStream();
            outputStream.write(sendBytes);

            int httpResponse = con.getResponseCode();
            System.out.println("httpResponse: " + httpResponse);

            if (httpResponse >= HttpURLConnection.HTTP_OK
                    && httpResponse < HttpURLConnection.HTTP_BAD_REQUEST) {
                Scanner scanner = new Scanner(con.getInputStream(), "UTF-8");
                jsonResponse = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
                scanner.close();
            } else {
                Scanner scanner = new Scanner(con.getErrorStream(), "UTF-8");
                jsonResponse = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
                scanner.close();
            }
            System.out.println("jsonResponse:\n" + jsonResponse);

            OneSignal.postNotification(jsonResponse, new OneSignal.PostNotificationResponseHandler() {
                @Override
                public void onSuccess(JSONObject response) {
                    Log.i("OneSignalExample", "postNotification Success: " + response.toString());
                }

                @Override
                public void onFailure(JSONObject response) {
                    Log.i("OneSignalExample", "postNotification failure: " + response.toString());
                }
            });

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }


    private void initializeRecyclerViews(float density) {

        mMessageList = findViewById(R.id.recyclerViewListMessages);
        mMessageList.setHasFixedSize(false);
        mMessageList.setNestedScrollingEnabled(false);

        mMessageAdapter = new MessageAdapter(messageList, this, curChatObject.getNumberOfUsers(), density, chatKey);
        mMessageAdapter.setHasStableIds(true);
        mMessageList.setAdapter(mMessageAdapter);
        mMessageListLayoutManager=new LinearLayoutManager(getApplicationContext(), RecyclerView.VERTICAL, false);
        mMessageList.setLayoutManager(mMessageListLayoutManager);
    }

    private void initializeViews() {

        mMessageText = findViewById(R.id.messageText);
        mSendMessage = findViewById(R.id.sendMessage);
        mAddMedia = findViewById(R.id.addMedia);
        onDateScrolling = findViewById(R.id.onScrollingDate);

        taggedLayout = findViewById(R.id.taggedLayout);
        mTaggedImage = findViewById(R.id.taggedImage);
        mTaggedSender = findViewById(R.id.taggedSender);
        mTaggedText = findViewById(R.id.taggedText);
        cancelTagged = findViewById(R.id.cancelTagged);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.specific_chat_menu,menu);

        if(curChatObject.isSingleChat()){
            menu.findItem(R.id.changeChatPictureMenu).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){

            case android.R.id.home:
                onBackPressed();
                break;

            case R.id.changeChatPictureMenu:
                openGalleryToChangeProfilePhoto();
                break;

            case R.id.clearChat:
                DatabaseReference mMessageDb = FirebaseDatabase.getInstance().getReference().child("Chats").child(chatKey);
                mMessageDb.child("Messages").setValue(true);
                mMessageDb.child("info/user").child(AllChatsActivity.curUser.getUid()).setValue("true");
                break;

            default:
                Toast.makeText(getApplicationContext(), "please select proper option", Toast.LENGTH_SHORT).show();
                break;
        }
        return true;
    }

    @Override
    public void OnItemDoubleClicked(int position) {
        MessageObject messageObject = messageList.get(position);

        mTaggedSender.setText(messageObject.getSenderName());
        String text = messageObject.getText();
        if (text.equals("")) {
            text = "photo";
        }
        mTaggedText.setText(text);
        if (!messageObject.getImageUri().equals("")) {
            mTaggedImage.setVisibility(View.VISIBLE);
            Glide.with(getApplicationContext()).load(Uri.parse(messageObject.getImageUri())).into(mTaggedImage);
        } else {
            mTaggedImage.setVisibility(View.GONE);
        }
        taggedLayout.setVisibility(View.VISIBLE);
        taggedPosition = position;
    }
}