package com.example.kundi5;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class GroupInformationActivity extends AppCompatActivity {

    private String GroupId;
    private String myGroupRole = "";
    private TextView editGroupTv,addParticipantTv,leaveGroupTv,participantsTv;
    private RecyclerView participantsRv;
    private Toolbar mToolbar;
    private FirebaseAuth mAuth;
    private String currentUserID;
    private ArrayList<Participants> userList;
    private AdapterParticipant adapterParticipant;
    private DatabaseReference ref;
    private StorageReference storageRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_information);

        editGroupTv = findViewById(R.id.editGroupTv);
        leaveGroupTv = findViewById(R.id.leaveGroupTv);
        addParticipantTv = findViewById(R.id.addParticipantTv);
        participantsTv = findViewById(R.id.participantsTv);
        participantsRv = findViewById(R.id.participantsRv);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        participantsRv.addItemDecoration(dividerItemDecoration);


        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Group Information");

        GroupId = getIntent().getExtras().get("ChatId").toString();
        ref = FirebaseDatabase.getInstance().getReference().child("Groups").child(GroupId);
        storageRef = FirebaseStorage.getInstance().getReference().child("Groups Images").child(GroupId+".jpg");

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        loadMyGroupRole();
        loadMyGroupRole2();

        leaveGroupTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               String dialogTitle="";
               String dialogDescription="";
               String positiveButtonTitle="";
                if (myGroupRole.equals("participants") || myGroupRole.equals("admin ")){
                    dialogTitle="Leave Group";
                    dialogDescription = "Are You Sure You Want To Leave Group Permanently?";
                    positiveButtonTitle = "LEAVE";
               }
                AlertDialog.Builder builder = new AlertDialog.Builder(GroupInformationActivity.this);
                builder.setTitle(dialogTitle)
                        .setMessage(dialogDescription)
                        .setPositiveButton(positiveButtonTitle, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (myGroupRole.equals("participants") || myGroupRole.equals("admin ")){
                                    leaveGroup();
                                }
                            }
                        })
                        .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });
    }

    private void loadMyGroupRole2() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.keepSynced(true);
        ref.child(GroupId).child("Participants")
                .orderByChild("uid").equalTo(currentUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds: dataSnapshot.getChildren()){
                            myGroupRole = ""+ds.child("role").getValue();
                            if (myGroupRole.equals("creator") || myGroupRole.equals("admin ")){
                                addParticipantTv.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent(GroupInformationActivity.this, GroupParticipantAddActivity.class);
                                        intent.putExtra("ChatId", GroupId);
                                        startActivity(intent);
                                    }
                                });
                                editGroupTv.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent(GroupInformationActivity.this, EditGroupActivity.class);
                                        intent.putExtra("ChatId", GroupId);
                                        startActivity(intent);
                                    }
                                });
                            }else {
                                addParticipantTv.setVisibility(View.GONE);
                                editGroupTv.setVisibility(View.GONE);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void leaveGroup() {
        ref.child("Participants").child(currentUserID)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(GroupInformationActivity.this, "Group Left Successfully...", Toast.LENGTH_SHORT).show();
                        SendUserToMainActivity();
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(GroupInformationActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadMyGroupRole() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.keepSynced(true);
        ref.child(GroupId).child("Participants").orderByChild("uid")
                .equalTo(currentUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds: dataSnapshot.getChildren()){
                            myGroupRole = ""+ds.child("role").getValue();
                            if (myGroupRole.equals("participants")){
                                editGroupTv.setVisibility(View.GONE);
                                addParticipantTv.setVisibility(View.GONE);
                                leaveGroupTv.setText("Leave Group");

                            }else if (myGroupRole.equals("admin")){
                                editGroupTv.setVisibility(View.GONE);
                                addParticipantTv.setVisibility(View.VISIBLE);
                                leaveGroupTv.setText("Leave Group");

                            }else if (myGroupRole.equals("creator")){
                                editGroupTv.setVisibility(View.VISIBLE);
                                addParticipantTv.setVisibility(View.VISIBLE);
                                leaveGroupTv.setVisibility(View.GONE);
                            }
                        }

                    loadParticipants();
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void loadParticipants() {
        userList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.keepSynced(true);
        ref.child(GroupId).child("Participants").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    String uid = ""+ds.child("uid").getValue();
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                    ref.orderByChild("uid").equalTo(uid).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot ds: dataSnapshot.getChildren()){
                                Participants participants = ds.getValue(Participants.class);
                                userList.add(participants);
                            }
                            adapterParticipant = new AdapterParticipant(GroupInformationActivity.this, userList, GroupId, myGroupRole);
                            participantsRv.setAdapter(adapterParticipant);
                            participantsTv.setText("Participants("+userList.size()+")");
                            adapterParticipant.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
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

    private void SendUserToMainActivity()
    {
        Intent MainIntent = new Intent(GroupInformationActivity.this, MainActivity.class);
        MainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(MainIntent);
        finish();
    }

}