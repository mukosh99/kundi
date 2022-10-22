package com.example.kundi5;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
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

import org.jetbrains.annotations.NotNull;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private TextView nameTv,emailTv,statusTv;
    private CircleImageView userProfileImage;
    private FirebaseAuth mAuth;
    private ImageView editImage,RemoveImage;
    private String currentUserID, downloadImageUrl;
    private Uri imageUri = null;
    private DatabaseReference RootRef;
    private DatabaseReference mNameDatabase;
    private DatabaseReference mNameDatabase2;
    private StorageReference UserProfileImagesRef;
    private StorageReference UserProfileImagesRef2;
    private ProgressDialog loadingBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Edit Profile");

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference(). child("");
        UserProfileImagesRef = FirebaseStorage.getInstance().getReference().child("Profile Images");
        UserProfileImagesRef2 = FirebaseStorage.getInstance().getReference().child("Profile Images").child(currentUserID+".jpg");
        mNameDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        mNameDatabase2 = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID).child("image");

        InitializeFields();

        RetrieveUserInfo();

        nameTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToNameActivity();
            }
        });

        statusTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToEditStatusActivity();
            }
        });

        emailTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToEditEmailActivity();
            }
        });

        RemoveImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mNameDatabase2.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        UserProfileImagesRef2.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(ProfileActivity.this, "Profile Image Deleted Successfully", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull @NotNull Exception e) {
                                Toast.makeText(getApplicationContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                            }
                        });
                    }
                });

            }
        });

        editImage.setOnClickListener(new View.OnClickListener() {
                                         @Override
                                         public void onClick(View view) {
                                             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                                                 if (ContextCompat.checkSelfPermission(ProfileActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                                                     Toast.makeText(ProfileActivity.this, "Permission Denied", Toast.LENGTH_LONG).show();
                                                     ActivityCompat.requestPermissions(ProfileActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

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
                .setAspectRatio(1, 1)
                .start(ProfileActivity.this);
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

                final StorageReference filePath = UserProfileImagesRef.child(currentUserID+".jpg");
                final UploadTask uploadTask =filePath.putFile(imageUri);

                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        String message = e.toString();
                        Toast.makeText(ProfileActivity.this, "Error:" + message, Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                    {
                        Toast.makeText(ProfileActivity.this, "Image Uploaded Successfully...", Toast.LENGTH_SHORT).show();

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
                                    Toast.makeText(ProfileActivity.this, "Got the profile image successfully", Toast.LENGTH_SHORT).show();
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
        mNameDatabase.child("image").setValue(downloadImageUrl)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            loadingBar.dismiss();
                            SendUserToMainActivity();
                            Toast.makeText(ProfileActivity.this, "Image added successfully", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            loadingBar.dismiss();
                            String message = task.getException().toString();
                            Toast.makeText(ProfileActivity.this, "Error" + message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }



    private void InitializeFields()
    {
        nameTv = (TextView) findViewById(R.id.textView6);
        statusTv = (TextView) findViewById(R.id.textView8);
        emailTv = (TextView) findViewById(R.id.textView10);
        userProfileImage = (CircleImageView) findViewById(R.id.circleImageView);
        loadingBar = new ProgressDialog(this);
        editImage = (ImageView) findViewById(R.id.imageView6);
        RemoveImage = (ImageView) findViewById(R.id.imageRemove);


    }

    private void RetrieveUserInfo()
    {
        RootRef.child("Users").child(currentUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if (dataSnapshot.exists() && dataSnapshot.hasChild("image"))
                        {
                            String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                            String retrieveStatus = dataSnapshot.child("status").getValue().toString();
                            String retrieveEmail = dataSnapshot.child("email").getValue().toString();
                            String retrieveImage = dataSnapshot.child("image").getValue().toString();


                            nameTv.setText(retrieveUserName);
                            statusTv.setText(retrieveStatus);
                            emailTv.setText(retrieveEmail);
                            Picasso.get().load(retrieveImage).placeholder(R.drawable.profile_image).fit().centerCrop().into(userProfileImage);

                        }

                        else
                        {
                            String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                            String retrieveStatus = dataSnapshot.child("status").getValue().toString();
                            String retrieveEmail = dataSnapshot.child("email").getValue().toString();

                            nameTv.setText(retrieveUserName);
                            statusTv.setText(retrieveStatus);
                            emailTv.setText(retrieveEmail);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    private void SendUserToNameActivity()
    {
        String name_value = nameTv.getText().toString();
        Intent userNameIntent = new Intent(ProfileActivity.this, UserNameActivity.class);
        userNameIntent.putExtra("name_value", name_value);
        startActivity(userNameIntent);

    }

    private void SendUserToEditStatusActivity()
    {
        String status_value = statusTv.getText().toString();
        Intent userStatusIntent = new Intent(ProfileActivity.this, EditStatusActivity.class);
        userStatusIntent.putExtra("status_value", status_value);
        startActivity(userStatusIntent);

    }

    private void SendUserToEditEmailActivity()
    {
        String email_value = emailTv.getText().toString();
        Intent userEmailIntent = new Intent(ProfileActivity.this, EditEmailActivity.class);
        userEmailIntent.putExtra("email_value", email_value);
        startActivity(userEmailIntent);

    }
    private void SendUserToMainActivity()
    {
        Intent mainIntent = new Intent(ProfileActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
