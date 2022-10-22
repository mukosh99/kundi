package com.example.kundi5;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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

public class PledgesDisplayActivity extends AppCompatActivity {
    private androidx.appcompat.widget.Toolbar mToolbar;
    private FirebaseAuth mAuth;
    private String currentUser;
    private String GroupId;
    private TextView WordText,TotalPledges,TargetAmt,NoText;;
    private RecyclerView pledgesRecyclerview;
    private ArrayList<Pledges> pledgesList;
    private DonationsAdapter donationsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pledges_display);

        mToolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Pledges");

        pledgesRecyclerview = (RecyclerView) findViewById(R.id.pledgesRv);
        GroupId = getIntent().getExtras().get("ChatId").toString();
        WordText = (TextView) findViewById(R.id.wordView);
        NoText = (TextView) findViewById(R.id.no_text);
        TotalPledges = (TextView) findViewById(R.id.total_pledges);
        TargetAmt = (TextView) findViewById(R.id.target_amt);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser().getUid();

        DatabaseReference PledgeRef = FirebaseDatabase.getInstance().getReference("Groups").child(GroupId).child("Pledges");
        DatabaseReference TargetRef = FirebaseDatabase.getInstance().getReference("Groups").child(GroupId).child("Targets");

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        pledgesRecyclerview.setLayoutManager(linearLayoutManager);
        pledgesRecyclerview.hasFixedSize();
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        pledgesRecyclerview.addItemDecoration(dividerItemDecoration);

        loadPledge();

        PledgeRef.addValueEventListener(new ValueEventListener() {
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

        PledgeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int sum=0;
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){
                    Map<String,Object> map = (Map<String, Object>) dataSnapshot1.getValue();
                    Object Pledges = map.get("pledge");
                    int pValue = Integer.parseInt(String.valueOf(Pledges));
                    sum += pValue;

                    Log.d("Sum",String.valueOf(sum));

                    TotalPledges.setText(String.valueOf(sum));
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

    private void loadPledge() {
        pledgesList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.keepSynced(true);
        ref.child(GroupId).child("Pledges").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                pledgesList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    Pledges pledges = ds.getValue(Pledges.class);
                    pledgesList.add(pledges);
                    }

                donationsAdapter = new DonationsAdapter(PledgesDisplayActivity.this, pledgesList);
                pledgesRecyclerview.setAdapter(donationsAdapter);
                NoText.setText(" Pledges("+pledgesList.size()+")");
                donationsAdapter.notifyDataSetChanged();
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