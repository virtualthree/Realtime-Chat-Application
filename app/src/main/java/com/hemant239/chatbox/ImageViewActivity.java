package com.hemant239.chatbox;

import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.util.Objects;

public class ImageViewActivity extends AppCompatActivity {

    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        initializeViews();

        String uri = getIntent().getStringExtra("URI");
        assert uri != null;
        if (!uri.equals("")) {
            Glide.with(this).load(Uri.parse(uri)).into(imageView);
        }
    }

    private void initializeViews() {
        imageView = findViewById(R.id.imageView);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
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