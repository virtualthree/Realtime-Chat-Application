package com.hemant239.chatbox;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hemant239.chatbox.user.UserObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


public class LogInActivity extends AppCompatActivity {

    EditText    mPhoneNumber,
                mVerificationCode;

    Button      mSend;

    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    FirebaseAuth mAuth;

    String mVerificationId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        userLoggedIn();
        setContentView(R.layout.activity_login);

        initializeViews();

        mSend.setOnClickListener(v -> {
            if (mVerificationId != null) {
                verifyPhoneNumberWithCode();
            } else {
                startPhoneNumberVerification();
            }
        });

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Toast.makeText(getApplicationContext(), "On verification Failed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(verificationId, forceResendingToken);
                findViewById(R.id.codeLayout).setVisibility(View.VISIBLE);
                mVerificationId=verificationId;
                mSend.setText("Verify Code");
            }
        };

    }


    private void verifyPhoneNumberWithCode() {
        PhoneAuthCredential phoneAuthCredential=PhoneAuthProvider.getCredential(mVerificationId,mVerificationCode.getText().toString());
        signInWithPhoneAuthCredential(phoneAuthCredential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential phoneAuthCredential) {
        mAuth.signInWithCredential(phoneAuthCredential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                final FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
                if (mUser != null) {
                    final DatabaseReference mUserDB = FirebaseDatabase.getInstance().getReference().child("Users").child(mUser.getUid());
                    mUserDB.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (!snapshot.exists()) {
                                Intent intent = new Intent(getApplicationContext(), NewUserDetailsActivity.class);
                                intent.putExtra("phoneNumber", mPhoneNumber.getText().toString());
                                startActivity(intent);
                                finish();
                            } else {
                                userLoggedIn();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(getApplicationContext(), "On Cancelled", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            }
        });
    }

    private void userLoggedIn() {
        FirebaseUser mUser = mAuth.getCurrentUser();
        if (mUser != null) {
            File file = new File(getApplicationContext().getFilesDir(), "user.ser");
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                UserObject userObject = new UserObject();
                userObject = (UserObject) objectInputStream.readObject();
                objectInputStream.close();
                fileInputStream.close();


                if (mUser.getUid().equals(userObject.getUid())) {
                    Intent intent = new Intent(getApplicationContext(), AllChatsActivity.class);
                    intent.putExtra("First time", false);
                    intent.putExtra("curUser", userObject);
                    startActivity(intent);
                    finish();
                }
                //if the user logs in with another number
                else {
                    getUserDetails(mUser.getUid(), false);
                }


            } catch (IOException | ClassNotFoundException e) {
                getUserDetails(mUser.getUid(), true);
                e.printStackTrace();
            }

        }
    }

    private void getUserDetails(final String userKey, boolean firstTime) {
        FirebaseDatabase.getInstance().getReference().child("Users").child(userKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String userName = "";
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


                    UserObject userObject = new UserObject(userKey, userName, userPhone, userStatus, userImage, chatID, notificationKey);
                    Intent intent = new Intent(getApplicationContext(), AllChatsActivity.class);
                    intent.putExtra("First time", firstTime);
                    intent.putExtra("curUser", userObject);
                    intent.putExtra("userChanged", true);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void startPhoneNumberVerification() {
        PhoneAuthOptions phoneAuthOptions = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(mPhoneNumber.getText().toString())
                .setTimeout(90L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(mCallbacks)
                .build();

        PhoneAuthProvider.verifyPhoneNumber(phoneAuthOptions);
    }

    private void initializeViews() {
        mPhoneNumber        = findViewById(R.id.phoneNumber);
        mVerificationCode   = findViewById(R.id.verificationCode);
        mSend       = findViewById((R.id.buttonSend));
    }
}