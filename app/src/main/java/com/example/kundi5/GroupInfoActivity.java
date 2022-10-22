package com.example.kundi5;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupInfoActivity extends AppCompatActivity {

    private String GroupId, myGroupRole,GroupName,description1;
    private ImageView groupChatImage;
    private TextView groupName, description;
    private DatabaseReference GroupRef;
    private FirebaseAuth mAuth;
    private String currentUser,groupImage1;
    private Toolbar mToolbar;
    private AppBarLayout appBarLayout;
    private androidx.appcompat.widget.LinearLayoutCompat Layout1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);
        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);


        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent profileIntent = new Intent(GroupInfoActivity.this, MainActivity.class);
                startActivity(profileIntent);
            }
        });

        Bundle extras = getIntent().getExtras();
        if (extras != null){
            GroupId = extras.getString("groupId");
            GroupName = extras.getString("groupName");
            groupImage1 = extras.getString("groupImage");
            description1 = extras.getString("groupDescription");

        }

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser().getUid();

        GroupRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(GroupId);
        GroupRef.keepSynced(true);

        groupChatImage = (ImageView) findViewById(R.id.groupImageView);
        groupName = (TextView) findViewById(R.id.groupTextView1);
        description = (TextView) findViewById(R.id.groupTextView2);
        appBarLayout = (AppBarLayout) findViewById(R.id.appBarLayout1);
        Layout1 = (androidx.appcompat.widget.LinearLayoutCompat) findViewById(R.id.layout1);


        FloatingActionButton floatingActionButton = findViewById(R.id.fab1);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToGroupChatActivity();
            }
        });

        RetrieveUserInfo();
        loadMyGroupRole();

    }

    private void loadMyGroupRole() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.keepSynced(true);
        ref.child(GroupId).child("Participants")
                .orderByChild("uid").equalTo(currentUser)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds: dataSnapshot.getChildren()){
                            myGroupRole = ""+ds.child("role").getValue();
                            invalidateOptionsMenu();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void RetrieveUserInfo() {
        GroupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && (dataSnapshot.hasChild("groupImage"))) {
                    //groupImage1 = dataSnapshot.child("groupImage").getValue().toString();
                    if (description1.isEmpty()) {
                        Layout1.setVisibility(View.GONE);
                    }
                    Picasso.get().load(groupImage1).fit().centerCrop().into(groupChatImage);
                    groupName.setText(GroupName);
                    description.setText(description1);

                } else if (dataSnapshot.exists() && (!dataSnapshot.hasChild("groupImage"))){

                    //String description1 = dataSnapshot.child("description").getValue().toString();
                    if (description1.isEmpty()) {
                        Layout1.setVisibility(View.GONE);

                    }

                    groupName.setText(GroupName);
                    description.setText(description1);

                }else {

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu2, menu);

        menu.findItem(R.id.find_friends_option).setVisible(false);
        menu.findItem(R.id.add_donations_option).setVisible(false);
        menu.findItem(R.id.group_info_option).setVisible(true);
        menu.findItem(R.id.pledge_option).setVisible(true);
        menu.findItem(R.id.view_pledge_option).setVisible(true);
        menu.findItem(R.id.view_donations_option).setVisible(true);

        if (myGroupRole.equals("creator") || myGroupRole.equals("admin ")){
            menu.findItem(R.id.find_friends_option).setVisible(true);
            menu.findItem(R.id.add_donations_option).setVisible(true);
            menu.findItem(R.id.group_info_option).setVisible(true);
            menu.findItem(R.id.pledge_option).setVisible(true);
            menu.findItem(R.id.view_pledge_option).setVisible(true);
            menu.findItem(R.id.view_donations_option).setVisible(true);
        }else {
            menu.findItem(R.id.find_friends_option).setVisible(false);
            menu.findItem(R.id.add_donations_option).setVisible(false);
            menu.findItem(R.id.group_info_option).setVisible(true);
            menu.findItem(R.id.pledge_option).setVisible(true);
            menu.findItem(R.id.view_pledge_option).setVisible(true);
            menu.findItem(R.id.view_donations_option).setVisible(true);
        }
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.find_friends_option) {
            SendUserToGroupParticipantAddActivity();
        }
        if (item.getItemId() == R.id.group_info_option) {
            SendUserToGroupInformationActivity();
        }

        if (item.getItemId() == R.id.pledge_option) {
            SendUserToPledgeActivity();
        }
        if (item.getItemId() == R.id.view_pledge_option) {
            SendUserToPledgesDisplayActivity();
        }
        if (item.getItemId() == R.id.view_donations_option) {
            SendUserToDonationsDisplayActivity();
        }
        if (item.getItemId() == R.id.add_donations_option) {
            SendUserToAddDonationsActivity();
        }

        return super.onOptionsItemSelected(item);
    }

    private void SendUserToGroupChatActivity() {

        String ChatId = GroupId;
        String groupName = GroupName;
        String image = groupImage1 ;
        Intent groupChatIntent = new Intent(GroupInfoActivity.this, GroupChatActivity.class);
        groupChatIntent.putExtra("ChatId", ChatId);
        groupChatIntent.putExtra("groupName", groupName);
        groupChatIntent.putExtra("image", groupImage1);
        startActivity(groupChatIntent);

    }

    private void SendUserToGroupParticipantAddActivity() {
        String ChatId = GroupId;
        Intent profileIntent = new Intent(GroupInfoActivity.this, GroupParticipantAddActivity.class);
        profileIntent.putExtra("ChatId", ChatId);
        startActivity(profileIntent);
    }

    private void SendUserToGroupInformationActivity() {
        String ChatId = GroupId;
        Intent profileIntent = new Intent(GroupInfoActivity.this, GroupInformationActivity.class);
        profileIntent.putExtra("ChatId", ChatId);
        startActivity(profileIntent);
    }

    private void SendUserToPledgeActivity() {
        String ChatId = GroupId;
        Intent pledgeIntent = new Intent(GroupInfoActivity.this, PledgeActivity.class);
        pledgeIntent.putExtra("ChatId", ChatId);
        startActivity(pledgeIntent);
    }
    private void SendUserToDonationsDisplayActivity() {
        String ChatId = GroupId;
        Intent donationsDisplayIntent = new Intent(GroupInfoActivity.this, DonationsDisplayActivity.class);
        donationsDisplayIntent.putExtra("ChatId", ChatId);
        startActivity(donationsDisplayIntent);
    }
    private void SendUserToAddDonationsActivity() {
        String ChatId = GroupId;
        Intent addDonationsIntent = new Intent(GroupInfoActivity.this, AddDonationsActivity.class);
        addDonationsIntent.putExtra("ChatId", ChatId);
        startActivity(addDonationsIntent);
    }
    private void SendUserToPledgesDisplayActivity() {
        String ChatId = GroupId;
        Intent DonationsIntent = new Intent(GroupInfoActivity.this, PledgesDisplayActivity.class);
        DonationsIntent.putExtra("ChatId", ChatId);
        startActivity(DonationsIntent);
    }

}
