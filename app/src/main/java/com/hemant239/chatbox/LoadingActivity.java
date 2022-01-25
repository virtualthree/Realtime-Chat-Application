package com.hemant239.chatbox;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class LoadingActivity extends AppCompatActivity {

    Button mCancelUpload;
    static Context context;

    TextView displayText;
    boolean isNewUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        initializeViews();

        String text = getIntent().getStringExtra("message");
        isNewUser = getIntent().getBooleanExtra("isNewUser", false);
        displayText.setText(text);

        if (isNewUser) {
            mCancelUpload.setVisibility(View.GONE);
        }


        context = this;
        mCancelUpload.setOnClickListener(v -> {
            setResult(RESULT_OK);
            finish();
        });

    }

    private void initializeViews() {
        displayText=findViewById(R.id.loadingText);
        mCancelUpload=findViewById(R.id.cancelUploadButton);
    }
}