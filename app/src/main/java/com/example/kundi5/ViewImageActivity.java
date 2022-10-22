package com.example.kundi5;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewImageActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private Toolbar mToolbar;
    private CircleImageView ProfileImageView;
    private TextView ProfileName;
    private String image;
    private String name;
    private String currentUser;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);

        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Kundi");
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser().getUid();


        ProfileImageView = (CircleImageView) findViewById(R.id.image_viewer);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        ProfileName = (TextView) findViewById(R.id.name_viewer);
        profileInfoDisplay();

    }
    private void profileInfoDisplay() {
        loading(true);
        DatabaseReference UsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUser);
        UsersRef.keepSynced(true);
        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.hasChild("image")) {
                    name = dataSnapshot.child("name").getValue().toString();
                    image = dataSnapshot.child("image").getValue().toString();
                    ProfileName.setText(name);
                    Picasso.get().load(image).placeholder(R.drawable.profile_image).fit().centerCrop().into(ProfileImageView);

                } else {
                    String name = dataSnapshot.child("name").getValue().toString();
                    ProfileName.setText(name);

                }
                loading(false);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loading(Boolean isLoading){
        if (isLoading){
            progressBar.setVisibility(View.VISIBLE);
        }else {
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

}