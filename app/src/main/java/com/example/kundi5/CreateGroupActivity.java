package com.example.kundi5;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.UUID;

public class CreateGroupActivity extends AppCompatActivity {

    private Button CreateGroup;
    private TextInputEditText GroupEt, DescriptionEt;
    private ImageView GroupImageEt, imageView1;
    private String currentUserID;
    private DatabaseReference UsersRef,GroupRef;
    private Uri ImageUri;
    private String myUri = "";
    private String saveCurrentDate, saveCurrentTime;
    private StorageReference GroupImageRef;
    private ProgressDialog loadingBar;
    private String checker = "";
    private StorageTask uploadTask;
    private Toolbar mToolbar;
    private FirebaseAuth mAuth;
    private String currentUserName;
    private Switch aSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Create Group");

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        GroupRef = FirebaseDatabase.getInstance().getReference().child("Groups");
        UsersRef.keepSynced(true);

        GroupImageRef = FirebaseStorage.getInstance().getReference().child("Group Images");

        CreateGroup = (Button) findViewById(R.id.createGroupButton);
        GroupEt = (TextInputEditText) findViewById(R.id.groupNameEditText);
        DescriptionEt = (TextInputEditText) findViewById(R.id.groupDescriptionEditText);
        GroupImageEt = (ImageView) findViewById(R.id.groupImageEditText);
        imageView1 = (ImageView) findViewById(R.id.imageView5);
       

        loadingBar = new ProgressDialog(this);

        GetUserInfo();

        CreateGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checker.equals("clicked")) {
                    groupInfoSaved();
                } else {
                    updateGroupInfo();
                }
            }
        });

        GroupImageEt.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {

                                                checker = "clicked";
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                                                    if (ContextCompat.checkSelfPermission(CreateGroupActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                                                        Toast.makeText(CreateGroupActivity.this, "Permission Denied", Toast.LENGTH_LONG).show();
                                                        ActivityCompat.requestPermissions(CreateGroupActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

                                                    } else {

                                                        choseImage();

                                                    }

                                                } else {

                                                    choseImage();

                                                }

                                            }

                                        }

        );

    }

    private void GetUserInfo() {
        UsersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name"))) {
                    currentUserName = dataSnapshot.child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void updateGroupInfo() {

        if (TextUtils.isEmpty(GroupEt.getText().toString())) {
            Toast.makeText(this, "Group Name Is Mandatory", Toast.LENGTH_SHORT).show();
        } else {

            Calendar calendar = Calendar.getInstance();

            SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
            saveCurrentDate = currentDate.format(calendar.getTime());

            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss a");
            saveCurrentTime = currentTime.format(calendar.getTime());

            final String Id = GroupRef.push().getKey();

            HashMap<String, Object> groupMap = new HashMap<>();
            groupMap.put("groupId", Id);
            groupMap.put("name", currentUserName);
            groupMap.put("date", saveCurrentDate);
            groupMap.put("time", saveCurrentTime);
            groupMap.put("group", GroupEt.getText().toString());
            groupMap.put("description", DescriptionEt.getText().toString());

            GroupRef.child(Id).updateChildren(groupMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        HashMap<String, Object> partMap = new HashMap<>();
                        partMap.put("uid", currentUserID);
                        partMap.put("role", "creator");
                        partMap.put("groupId", Id);
                        DatabaseReference partRef = FirebaseDatabase.getInstance().getReference().child("Groups");
                        partRef.child(Id).child("Participants").child(currentUserID).setValue(partMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    loadingBar.dismiss();
                                    Toast.makeText(CreateGroupActivity.this, "Group added successfully", Toast.LENGTH_SHORT).show();
                                } else {
                                    loadingBar.dismiss();
                                    String message = task.getException().toString();
                                    Toast.makeText(CreateGroupActivity.this, "Error" + message, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
            });

            loadingBar.dismiss();
            startActivity(new Intent(CreateGroupActivity.this, MainActivity.class));
            Toast.makeText(CreateGroupActivity.this, "Group Created Successfully", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void choseImage() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(CreateGroupActivity.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            ImageUri = result.getUri();

            GroupImageEt.setImageURI(ImageUri);
            imageView1.setVisibility(View.INVISIBLE);
        } else {
            Toast.makeText(this, "Error, Try Again.", Toast.LENGTH_SHORT).show();

            startActivity(new Intent(CreateGroupActivity.this, CreateGroupActivity.class));
            finish();
        }
    }

    private void groupInfoSaved() {
        if (TextUtils.isEmpty(GroupEt.getText().toString())) {
            Toast.makeText(this, "Group Name Is Mandatory", Toast.LENGTH_SHORT).show();
        } else if (checker.equals("clicked")) {
            uploadGroupImage();
        }
    }

    private void uploadGroupImage() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Creating Group...");
        progressDialog.setMessage("Please Wait...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        if (ImageUri != null) {

            final String GroupIdImage = GroupRef.push().getKey();

            final StorageReference filePath = GroupImageRef.child(GroupIdImage + ".jpg");

            uploadTask = filePath.putFile(ImageUri);
            uploadTask.continueWithTask(new Continuation() {

                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return filePath.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        myUri = downloadUri.toString();

                        final String Id = GroupRef.push().getKey();

                        Calendar calendar = Calendar.getInstance();

                        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
                        saveCurrentDate = currentDate.format(calendar.getTime());

                        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss a");
                        saveCurrentTime = currentTime.format(calendar.getTime());

                        HashMap<String, Object> groupMap = new HashMap<>();
                        groupMap.put("groupId", Id);
                        groupMap.put("groupIdImage", GroupIdImage);
                        groupMap.put("name", currentUserName);
                        groupMap.put("date", saveCurrentDate);
                        groupMap.put("time", saveCurrentTime);
                        groupMap.put("group", GroupEt.getText().toString());
                        groupMap.put("description", DescriptionEt.getText().toString());
                        groupMap.put("groupImage", myUri);

                        GroupRef.child(Id).updateChildren(groupMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    HashMap<String, Object> partMap = new HashMap<>();
                                    partMap.put("uid", currentUserID);
                                    partMap.put("role", "creator");
                                    partMap.put("groupId", Id);
                                    GroupRef.child(Id).child("Participants").child(currentUserID).setValue(partMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                progressDialog.dismiss();
                                                Toast.makeText(CreateGroupActivity.this, "Group added successfully", Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(CreateGroupActivity.this, MainActivity.class));
                                                finish();
                                            } else {
                                                progressDialog.dismiss();
                                                String message = task.getException().toString();
                                                Toast.makeText(CreateGroupActivity.this, "Error" + message, Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }

                            }
                        });
                    }
                }
            });
        } else {
            Toast.makeText(this, "Image not selected", Toast.LENGTH_SHORT).show();
        }
    }
}
