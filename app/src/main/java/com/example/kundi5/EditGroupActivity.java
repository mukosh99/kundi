package com.example.kundi5;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditGroupActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private TextView groupNameTv,descriptionTv;
    private ImageView GroupImageEt,GroupImage5;
    private String downloadImageUrl;
    private Uri imageUri = null;
    private DatabaseReference RootRef;
    private DatabaseReference mGroupImageDatabase;
    private StorageReference GroupImageRef;
    private ProgressDialog loadingBar;
    private String GroupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_group);

        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Edit Group Info");

        GroupId = getIntent().getExtras().get("ChatId").toString();

        RootRef = FirebaseDatabase.getInstance().getReference().child("Groups");
        RootRef.keepSynced(true);
        GroupImageRef = FirebaseStorage.getInstance().getReference().child("Group Images");
        mGroupImageDatabase = FirebaseDatabase.getInstance().getReference().child("Groups").child(GroupId);

        InitializeFields();

        RetrieveUserInfo();

        groupNameTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToEditGroupNameActivity();
            }
        });

        descriptionTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToEditDescriptionActivity();
            }
        });



        GroupImageEt.setOnClickListener(new View.OnClickListener() {
                                         @Override
                                         public void onClick(View view) {
                                             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                                                 if (ContextCompat.checkSelfPermission(EditGroupActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                                                     Toast.makeText(EditGroupActivity.this, "Permission Denied", Toast.LENGTH_LONG).show();
                                                     ActivityCompat.requestPermissions(EditGroupActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

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

    private void choseImage() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setMinCropWindowSize(500,500)
                .setAspectRatio(1, 1)
                .start(EditGroupActivity.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                loadingBar.setTitle("Adding new image");
                loadingBar.setMessage("please wait");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                imageUri = result.getUri();
                GroupImageEt.setImageURI(imageUri);

                final String GroupIdImage = RootRef.push().getKey();

                final StorageReference filePath = GroupImageRef.child(GroupIdImage + ".jpg");
                final UploadTask uploadTask =filePath.putFile(imageUri);

                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        String message = e.toString();
                        Toast.makeText(EditGroupActivity.this, "Error:" + message, Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                    {
                        Toast.makeText(EditGroupActivity.this, "Image Uploaded Successfully...", Toast.LENGTH_SHORT).show();

                        Task<Uri> uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                if (!task.isSuccessful())
                                {
                                    throw task.getException();
                                }

                                downloadImageUrl = filePath.getDownloadUrl().toString();
                                return filePath.getDownloadUrl();
                            }
                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task)
                            {
                                if (task.isSuccessful())
                                {
                                    downloadImageUrl = task.getResult().toString();
                                    Toast.makeText(EditGroupActivity.this, "Got the profile image successfully", Toast.LENGTH_SHORT).show();
                                    SaveImageToDatabase ();
                                }
                            }
                        });
                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();

            }
        }

    }

    private void SaveImageToDatabase()
    {
        mGroupImageDatabase.child("groupImage").setValue(downloadImageUrl)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            loadingBar.dismiss();
                            SendUserToMainActivity();
                            Toast.makeText(EditGroupActivity.this, "Image added successfully", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            loadingBar.dismiss();
                            String message = task.getException().toString();
                            Toast.makeText(EditGroupActivity.this, "Error" + message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
             }



    private void InitializeFields()
    {
        groupNameTv = (TextView) findViewById(R.id.groupName);
        descriptionTv = (TextView) findViewById(R.id.description);
        loadingBar = new ProgressDialog(this);
        GroupImageEt=(ImageView) findViewById(R.id.groupImageEditText);
        GroupImage5=(ImageView) findViewById(R.id.imageView5);

    }

    private void RetrieveUserInfo()
    {
        RootRef.child(GroupId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("groupImage")))
                        {
                            String retrieveGroupName = dataSnapshot.child("group").getValue().toString();
                            String retrieveDescription = dataSnapshot.child("description").getValue().toString();
                            String retrieveGroupImage = dataSnapshot.child("groupImage").getValue().toString();
                            GroupImage5.setVisibility(View.INVISIBLE);
                            String retrieveAccountPayBill = dataSnapshot.child("accountPayBill").getValue().toString();

                            groupNameTv.setText(retrieveGroupName);
                            descriptionTv.setText(retrieveDescription);
                            Picasso.get().load(retrieveGroupImage).fit().centerCrop().into(GroupImageEt);

                        }
                        else
                        {
                            String retrieveGroupName = dataSnapshot.child("group").getValue().toString();
                            String retrieveDescription = dataSnapshot.child("description").getValue().toString();


                            groupNameTv.setText(retrieveGroupName);
                            descriptionTv.setText(retrieveDescription);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

    private void SendUserToEditGroupNameActivity()
    {
        String ChatId = GroupId;
        String group_name_value = groupNameTv.getText().toString();
        Intent groupIntent = new Intent(EditGroupActivity.this, EditGroupNameActivity.class);
        groupIntent.putExtra("group_name_value", group_name_value);
        groupIntent.putExtra("ChatId" , ChatId);
        startActivity(groupIntent);

    }

    private void SendUserToEditDescriptionActivity()
    {
        String ChatId = GroupId;
        String description_value = descriptionTv.getText().toString();
        Intent description_intent = new Intent(EditGroupActivity.this, EditDescripionActivity.class);
        description_intent.putExtra("description_value", description_value);
        description_intent.putExtra("ChatId" , ChatId);
        startActivity(description_intent);

    }

    private void SendUserToMainActivity()
    {
        Intent mainIntent = new Intent(EditGroupActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();

    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}
