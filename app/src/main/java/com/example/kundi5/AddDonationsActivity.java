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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class AddDonationsActivity extends AppCompatActivity {

    private TextInputEditText EtTarget,EtNameDonate,EtDonate;
    private Button DonateButton,TargetButton;
    private Toolbar mToolbar;
    private String currentUser;
    private FirebaseAuth mAuth;
    private String GroupId;
    private DatabaseReference donationsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_donations);

        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Add Donations");

        EtTarget = (TextInputEditText) findViewById(R.id.etTarget);
        EtNameDonate = (TextInputEditText) findViewById(R.id.etNameDonate);
        EtDonate = (TextInputEditText) findViewById(R.id.etDonate);
        DonateButton = (Button) findViewById(R.id.donateButton);
        TargetButton = (Button) findViewById(R.id.targetButton);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser().getUid();
        donationsRef = FirebaseDatabase.getInstance().getReference().child("Groups");
        GroupId = getIntent().getExtras().get("ChatId").toString();

        DonateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Donations();
                EtNameDonate.setText("");
                EtDonate.setText("");
            }
        });

        TargetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Target();
                EtTarget.setText("");
            }
        });
    }

    private void Target() {
        String setTarget = EtTarget.getText().toString();

        if (TextUtils.isEmpty(setTarget))
        {
            Toast.makeText(this, "Please Enter Target...", Toast.LENGTH_SHORT).show();
        }
        else
        {
            HashMap<String, String> targetMap = new HashMap<>();
            targetMap.put("uid", currentUser);
            targetMap.put("targetAmt", setTarget);
            donationsRef.child(GroupId).child("Targets").child(currentUser).setValue(targetMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid)
                        {
                            Toast.makeText(AddDonationsActivity.this, "Your Target Has Been Added Successfully...", Toast.LENGTH_SHORT).show();
                            SendUserToDonationsDisplayActivity();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(AddDonationsActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void Donations() {
        String setName = EtNameDonate.getText().toString();
        String setDonate = EtDonate.getText().toString();

        if (TextUtils.isEmpty(setName))
        {
            Toast.makeText(this, "Please Enter Name...", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(setDonate))
        {
            Toast.makeText(this, "Please Enter Donation Amt...", Toast.LENGTH_SHORT).show();
        }
        else
        {
            final String dId = donationsRef.push().getKey();
            HashMap<String, Object> donationsMap = new HashMap<>();
            donationsMap.put("donationsId",dId );
            donationsMap.put("groupId",GroupId );
            donationsMap.put("uid", currentUser);
            donationsMap.put("name", setName);
            donationsMap.put("donations", setDonate);
            donationsRef.child(GroupId).child("Donations").child(dId).setValue(donationsMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid)
                        {
                            Toast.makeText(AddDonationsActivity.this, "Your Donations Have Been Added Successfully...", Toast.LENGTH_SHORT).show();
                            SendUserToDonationsDisplayActivity();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(AddDonationsActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void SendUserToDonationsDisplayActivity() {
        String ChatId = GroupId;
        Intent profileIntent = new Intent(AddDonationsActivity.this, DonationsDisplayActivity.class);
        profileIntent.putExtra("ChatId", ChatId);
        startActivity(profileIntent);
    }
}