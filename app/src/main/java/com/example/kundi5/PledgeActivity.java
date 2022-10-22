package com.example.kundi5;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class PledgeActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private TextInputEditText pledgeEt;
    private Button pledgeBt;
    private FirebaseAuth mAuth;
    private String currentUser;
    private String ProfileName,ProfileImageView;
    private String GroupId;
    private DatabaseReference pledgeRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pledge);

        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Pledge");

        pledgeEt = (TextInputEditText) findViewById(R.id.etDonate);
        pledgeBt = (Button) findViewById(R.id.pledgeButton);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser().getUid();
        pledgeRef = FirebaseDatabase.getInstance().getReference().child("Groups");
        GroupId = getIntent().getExtras().get("ChatId").toString();
        profileInfoDisplay();

        pledgeBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Pledge();
                pledgeEt.setText("");
            }
        });
    }
    private void Pledge() {
        String setPledge = pledgeEt.getText().toString();

        if (TextUtils.isEmpty(setPledge))
        {
            Toast.makeText(this, "Please Enter An Amount...", Toast.LENGTH_SHORT).show();
        }
        else
        {
            HashMap<String, Object> pledgeMap = new HashMap<>();
            pledgeMap.put("uid", currentUser);
            pledgeMap.put("pledge", setPledge);
            pledgeMap.put("name", ProfileName);
            pledgeMap.put("image", ProfileImageView);
            pledgeMap.put("groupId",GroupId );
            pledgeRef.child(GroupId).child("Pledges").child(currentUser).setValue(pledgeMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid)
                        {
                            Toast.makeText(PledgeActivity.this, "Your Pledge Has Been Added Successfully...", Toast.LENGTH_SHORT).show();
                            SendUserToPledgesDisplayActivity();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(PledgeActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void profileInfoDisplay() {
        DatabaseReference UsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUser);
        UsersRef.keepSynced(true);
        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name") && (dataSnapshot.hasChild("image")))){
                    ProfileName = dataSnapshot.child("name").getValue().toString();
                    ProfileImageView = dataSnapshot.child("image").getValue().toString();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void SendUserToPledgesDisplayActivity() {
        String ChatId = GroupId;
        Intent pledgesDisplayIntent = new Intent(PledgeActivity.this, PledgesDisplayActivity.class);
        pledgesDisplayIntent.putExtra("ChatId", ChatId);
        startActivity(pledgesDisplayIntent);
    }
}