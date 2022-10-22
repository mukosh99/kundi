package com.example.kundi5;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class GroupParticipantAddActivity extends AppCompatActivity {

    private RecyclerView participantsRv;
    private Toolbar mToolbar;
    private FirebaseAuth mAuth;
    private String currentUser;
    private String GroupId;
    private String myGroupRole;
    private TextView WordText,NoText;

    private ArrayList<Participants> userList;
    private AdapterParticipantAdd adapterParticipantAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_participant_add);

        mToolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Add Participants");

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser().getUid();
        participantsRv = findViewById(R.id.participantsRv);
        GroupId = getIntent().getExtras().get("ChatId").toString();
        WordText = (TextView) findViewById(R.id.wordView);
        NoText = (TextView) findViewById(R.id.no_text);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        participantsRv.addItemDecoration(dividerItemDecoration);

        DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("Users");

        loadGroupInfo();

        ref1.addValueEventListener(new ValueEventListener() {
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

    }

    private void getAllUsers() {
        userList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.keepSynced(true);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               userList.clear();
               for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){
                   Participants participants = dataSnapshot1.getValue(Participants.class);

                   if (!currentUser.equals(participants.getUid())){
                       userList.add(participants);
                   }
               }

               adapterParticipantAdd = new AdapterParticipantAdd(GroupParticipantAddActivity.this, userList, ""+GroupId, ""+myGroupRole);
               NoText.setText(" Participants("+userList.size()+")");
               participantsRv.setAdapter(adapterParticipantAdd);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void loadGroupInfo() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.keepSynced(true);
        ref.child(GroupId).child("Participants").child(currentUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    myGroupRole = ""+dataSnapshot.child("role").getValue();
                    getAllUsers();
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
        return super.onSupportNavigateUp();
    }
}