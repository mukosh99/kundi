package com.example.kundi5;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements GroupClickInterface {

    private FirebaseAuth mAuth;
    private String currentUser,myGroupRole;
    private FirebaseUser firebaseUser;
    private Toolbar mToolbar;
    private String groupId = "";
    private String groupIdImage;
    private TextView ProfileName, WordText,groupView;
    private CircleImageView ProfileImageView;
    private RecyclerView recyclerView;
    private String image;
    private String name;
    private Button createGroup;
    private DatabaseReference GroupRef,RootRef;
    private ArrayList<Groups>groupsArrayList;
    private AdapterGroup adapterGroup;
    private ConstraintLayout parent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Kundi");

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        groupsArrayList = new ArrayList<>();

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser().getUid();


        GroupRef = FirebaseDatabase.getInstance().getReference("Groups");
        RootRef = FirebaseDatabase.getInstance().getReference();

        createGroup = (Button) findViewById(R.id.CreateGroupView);
        ProfileImageView = (CircleImageView) findViewById(R.id.profileImageView);
        ProfileName = (TextView) findViewById(R.id.profileName);
        groupView = (TextView) findViewById(R.id.GroupView);
        WordText = (TextView) findViewById(R.id.wordView);
        parent = (ConstraintLayout) findViewById(R.id.parent);
        adapterGroup = new AdapterGroup(groupsArrayList, this);

        recyclerView = findViewById(R.id.recycler);
        recyclerView.invalidate();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.hasFixedSize();

        loadGroups();
        checkConnection();
        profileInfoDisplay();
        loadMyGroupRole();

        createGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToCreateGroupActivity();
            }
        });

        ProfileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent profileIntent = new Intent(MainActivity.this, ViewImageActivity.class);
                startActivity(profileIntent);
            }
        });

        GroupRef.addValueEventListener(new ValueEventListener() {
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

    private void loadMyGroupRole() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.keepSynced(true);
        ref.child(groupId).child("Participants")
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

    private void checkConnection() {
        ConnectivityManager manager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
        if (null != activeNetwork ) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {

            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {

            }
        } else {
            showSnackBar();
        }
    }

    private void showSnackBar() {
        Snackbar.make(parent, "Connect To Internet", Snackbar.LENGTH_LONG).show();
    }

    private void loadGroups() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups").child(groupId);
        reference.keepSynced(true);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                groupsArrayList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    groupId = ""+ds.child("groupId").getValue();
                    if (ds.child("Participants").child(currentUser).exists()){
                        Groups groups = ds.getValue(Groups.class);
                        groupsArrayList.add(groups);
                    }
                }
                recyclerView.setAdapter(adapterGroup);
                groupView.setText(" Groups("+groupsArrayList.size()+")");
                adapterGroup.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void profileInfoDisplay() {
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

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menus, menu);
        MenuItem menuItem=menu.findItem(R.id.search);
        SearchView searchView= (SearchView) menuItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                adapterGroup.getFilter().filter(s.toString());
                return false;
            }
        });

        return  super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == R.id.main_logout_option) {
            mAuth.signOut();
            SendUserToPhoneLoginActivity();
        }
        if (item.getItemId() == R.id.main_settings_option) {
            SendUserToSettingsActivity();
        }
        if (item.getItemId() == R.id.main_profile_option) {
            SendUserToProfileActivity();
        }
        return true;
    }

    private void SendUserToSettingsActivity() {
        Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
        settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(settingsIntent);

    }

    private void SendUserToPhoneLoginActivity() {
        Intent mainIntent = new Intent(MainActivity.this, PhoneLoginActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);

    }

    private void SendUserToCreateGroupActivity() {
        Intent createGroupIntent = new Intent(MainActivity.this, CreateGroupActivity.class);
        startActivity(createGroupIntent);
    }

    private void SendUserToProfileActivity() {
        Intent profileIntent = new Intent(MainActivity.this, ProfileActivity.class);
        startActivity(profileIntent);
    }



    @Override
    public void onItemClick(int position) {
        Intent groupIntent = new Intent(getApplicationContext(),GroupInfoActivity.class);
        groupIntent.putExtra("groupId", groupsArrayList.get(position).getGroupId());
        groupIntent.putExtra("groupName", groupsArrayList.get(position).getGroup());
        groupIntent.putExtra("groupImage", groupsArrayList.get(position).getGroupImage());
        groupIntent.putExtra("groupDescription", groupsArrayList.get(position).getDescription());
        groupIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        startActivity(groupIntent);

    }

    @Override
    public void onLongItemClick(int position) {
        adapterGroup.notifyItemRemoved(position);
        adapterGroup.notifyItemRangeChanged(position, groupsArrayList.size());

    }
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUserId = mAuth.getCurrentUser();
        if (currentUserId == null){
            SendUserToPhoneLoginActivity();
        }
    }
}
