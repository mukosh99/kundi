package com.example.kundi5;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
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
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity
{
    private Button UpdateAccountSettings;
    private TextInputEditText userName,userStatus,emailTv;
    private CircleImageView userProfileImage;
    private String currentUserID, downloadImageUrl;
    private Uri imageUri = null;
    private FirebaseAuth mAuth;
    private ImageView editImage;
    private DatabaseReference RootRef;
    private StorageReference UserProfileImagesRef;
    private ProgressDialog loadingBar;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Settings");



        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();
        UserProfileImagesRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        InitializeFields();

        UpdateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                UpdateSettings();
            }
        });

        DisplayProfile ();

        editImage.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                                                    if (ContextCompat.checkSelfPermission(SettingsActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                                                        Toast.makeText(SettingsActivity.this, "Permission Denied", Toast.LENGTH_LONG).show();
                                                        ActivityCompat.requestPermissions(SettingsActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

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

    private void DisplayProfile()
    {
        String currentUserID = mAuth.getCurrentUser().getUid();
        DatabaseReference UsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        UsersRef.keepSynced(true);
        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name") && (dataSnapshot.hasChild("image"))))
                {

                    String name = dataSnapshot.child("name").getValue().toString();
                    String image = dataSnapshot.child("image").getValue().toString();
                    String status = dataSnapshot.child("status").getValue().toString();
                    String email = dataSnapshot.child("email").getValue().toString();
                    userName.setText(name);
                    userStatus.setText(status);
                    emailTv.setText(email);
                    Picasso.get().load(image).placeholder(R.drawable.profile_image).fit().centerCrop().into(userProfileImage);
                }
                else if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name")))
                {

                    String name = dataSnapshot.child("name").getValue().toString();
                    String status = dataSnapshot.child("status").getValue().toString();
                    String email = dataSnapshot.child("email").getValue().toString();
                    userName.setText(name);
                    userStatus.setText(status);
                    emailTv.setText(email);
                }
                else
                {
                    Toast.makeText(SettingsActivity.this, "Please Update Your profile Info", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void choseImage() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(SettingsActivity.this);
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
                userProfileImage.setImageURI(imageUri);

                final StorageReference filePath = UserProfileImagesRef.child(imageUri.getLastPathSegment()+ currentUserID + ".jpg");
                final UploadTask uploadTask =filePath.putFile(imageUri);

                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        String message = e.toString();
                        Toast.makeText(SettingsActivity.this, "Error:" + message, Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                    {
                        Toast.makeText(SettingsActivity.this, "Image Uploaded Successfully...", Toast.LENGTH_SHORT).show();

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
                                    Toast.makeText(SettingsActivity.this, "Got the profile image successfully", Toast.LENGTH_SHORT).show();
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
        RootRef.child("Users").child(currentUserID).child("image").setValue(downloadImageUrl)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            loadingBar.dismiss();
                            SendUserToMainActivity();
                            Toast.makeText(SettingsActivity.this, "Image added successfully", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            loadingBar.dismiss();
                            String message = task.getException().toString();
                            Toast.makeText(SettingsActivity.this, "Error" + message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void InitializeFields()
    {
        UpdateAccountSettings = (Button) findViewById(R.id.update_settings_button);
        userName = (TextInputEditText) findViewById(R.id.set_user_name);
        editImage = (ImageView) findViewById(R.id.imageView6);
        userStatus = (TextInputEditText) findViewById(R.id.set_profile_status);
        emailTv = (TextInputEditText) findViewById(R.id.set_email);
        userProfileImage = (CircleImageView) findViewById(R.id.circleImageView);
        loadingBar = new ProgressDialog(this);

    }

        private void UpdateSettings()
        {
            String setUserName = userName.getText().toString();
            String setStatus = userStatus.getText().toString();
            String setEmail = emailTv.getText().toString();

            if(TextUtils.isEmpty(setUserName))
            {
                Toast.makeText(this, "Please write your username...", Toast.LENGTH_SHORT).show();
            }

            else
            {
                loadingBar.setTitle("Adding new Profile");
                loadingBar.setMessage("please wait");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();
                HashMap<String, Object> profileMap = new HashMap<>();
                profileMap.put("uid", currentUserID);
                profileMap.put("name", setUserName);
                profileMap.put("status", setStatus);
                profileMap.put("onlineStatus", "online");
                profileMap.put("typingTo", "noOne");
                profileMap.put("email", setEmail);
                profileMap.put("state", "offline");


                RootRef.child("Users").child(currentUserID).updateChildren(profileMap)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task)
                            {
                                if (task.isSuccessful())
                                {
                                    loadingBar.dismiss();
                                    SendUserToMainActivity();
                                    Toast.makeText(SettingsActivity.this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                                }
                                else
                                {
                                    loadingBar.dismiss();
                                    String message = task.getException().toString();
                                    Toast.makeText(SettingsActivity.this, "Error" + message, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }

        }
        private void SendUserToMainActivity()
            {
                Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(mainIntent);
                finish();

            }
        }
