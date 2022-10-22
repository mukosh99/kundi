package com.example.kundi5;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;

public class DonationsDisplayActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private FirebaseAuth mAuth;
    private String currentUser;
    private String donateId = "";
    private String GroupId;
    private TextView WordText,TotalDonations,TargetAmt,NoText;
    private RecyclerView donationsRecyclerview;
    private ArrayList<DonationsAmt> donationsAmtList;
    private AdapterDonations adapterDonations;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donations_display);

        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Donations");

        donationsRecyclerview = (RecyclerView) findViewById(R.id.donationsRv);
        GroupId = getIntent().getExtras().get("ChatId").toString();
        WordText = (TextView) findViewById(R.id.wordView);
        NoText = (TextView) findViewById(R.id.no_text);
        TotalDonations = (TextView) findViewById(R.id.total_donations);
        TargetAmt = (TextView) findViewById(R.id.target_amt);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser().getUid();
        DatabaseReference DonationsRef = FirebaseDatabase.getInstance().getReference("Groups").child(GroupId).child("Donations");
        DonationsRef.keepSynced(true);
        DatabaseReference TargetRef = FirebaseDatabase.getInstance().getReference("Groups").child(GroupId).child("Targets");
        TargetRef.keepSynced(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        donationsRecyclerview.setLayoutManager(linearLayoutManager);
        donationsRecyclerview.hasFixedSize();
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        donationsRecyclerview.addItemDecoration(dividerItemDecoration);

        loadDonations();

        DonationsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    WordText.setVisibility(View.GONE);
                }else {
                    WordText.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        DonationsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int sum=0;
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){
                    Map<String,Object> map = (Map<String, Object>) dataSnapshot1.getValue();
                    Object Donations = map.get("donations");
                    int dValue = Integer.parseInt(String.valueOf(Donations));
                    sum += dValue;

                    Log.d("Sum",String.valueOf(sum));

                    TotalDonations.setText(String.valueOf(sum));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

       TargetRef.addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){
                   if (dataSnapshot.exists()){
                       String target = dataSnapshot1.child("targetAmt").getValue().toString();
                       TargetAmt.setText(target);
                   }
               }
           }

           @Override
           public void onCancelled(@NonNull DatabaseError databaseError) {

           }
       });

    }

    private void loadDonations() {
        donationsAmtList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.keepSynced(true);
        ref.child(GroupId).child("Donations").child(donateId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                donationsAmtList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                        donateId = "" + ds.child("donationsId").getValue();
                        DonationsAmt donationsAmt = ds.getValue(DonationsAmt.class);
                        donationsAmtList.add(donationsAmt);
                    }
                adapterDonations = new AdapterDonations(DonationsDisplayActivity.this, donationsAmtList);
                donationsRecyclerview.setAdapter(adapterDonations);
                NoText.setText(" Donations("+donationsAmtList.size()+")");
                adapterDonations.notifyDataSetChanged();
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
}