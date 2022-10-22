package com.example.kundi5;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import static com.example.kundi5.R.layout.activity_view_chat_image;

public class ViewChatImageActivity extends AppCompatActivity {

    private ImageView imageView;
    private String imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(activity_view_chat_image);

        imageView = findViewById(R.id.image_viewer);
        imageUri = getIntent().getStringExtra("url");

        Picasso.get().load(imageUri).placeholder(R.drawable.profile_image).into(imageView);
    }
}