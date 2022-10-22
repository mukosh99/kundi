package com.example.kundi5;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
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
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.vanniktech.emoji.EmojiPopup;
import com.vanniktech.emoji.EmojiTextView;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupChatActivity extends AppCompatActivity {
    private String GroupId, currentUserID, currentUserName, currentDate, currentTime, currentUserImage,GroupName;
    private EditText userMessageInput;
    private String saveCurrentDate, saveCurrentTime;
    private TextView textView1,GroupName2,Typing;
    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef, MessagesRef;
    private RecyclerView userMessagesList;
    private Toolbar mToolbar;
    private ImageButton SelectImageButton, SelectFileButton,sendMessageButton,btEmoji;
    private Uri ImageUri;
    private String myUri = "";
    private String myUid;
    private StorageReference ChatImageRef,ChatFileRef;
    private StorageTask uploadTask;
    private ProgressDialog progressDialog;


    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    private MessagesAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        mToolbar = (Toolbar) findViewById(R.id.main_custom_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);



        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        GroupId = getIntent().getExtras().get("ChatId").toString();
        GroupName = getIntent().getExtras().get("groupName").toString();

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        MessagesRef = FirebaseDatabase.getInstance().getReference("Groups");
        ChatImageRef = FirebaseStorage.getInstance().getReference().child("Chat Images");
        ChatFileRef = FirebaseStorage.getInstance().getReference().child("Chat Files");



        sendMessageButton = (ImageButton) findViewById(R.id.send_message_button);
        SelectFileButton = (ImageButton) findViewById(R.id.select_file_button);
        SelectImageButton = (ImageButton) findViewById(R.id.select_image_button);
        btEmoji = (ImageButton) findViewById(R.id.select_emoji_button);
        userMessageInput = (EditText) findViewById(R.id.input_group_message);
        textView1 = (TextView) findViewById(R.id.wordView);
        Typing = (TextView) findViewById(R.id.chat_typing);
        GroupName2 = (TextView) findViewById(R.id.group_name_chat);
        userMessagesList = (RecyclerView) findViewById(R.id.recycler);
        mLinearLayout = new LinearLayoutManager(this);
        mLinearLayout.setStackFromEnd(true);

        GroupName2.setText(GroupName);

        mAdapter = new MessagesAdapter(messagesList);

        progressDialog = new ProgressDialog(this);

        userMessagesList.setHasFixedSize(true);
        userMessagesList.setLayoutManager(mLinearLayout);
        userMessagesList.setAdapter(mAdapter);

        loadMessages();

        GetUserInfo();

        final EmojiPopup popup = EmojiPopup.Builder.fromRootView(
                findViewById(R.id.root_view)
        ).build(userMessageInput);

        btEmoji.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popup.toggle();
            }
        });

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EmojiTextView emojiTextView = (EmojiTextView) LayoutInflater
                        .from(view.getContext())
                        .inflate(R.layout.emoji_text_view,userMessagesList,false);
                emojiTextView.setText(userMessageInput.getText().toString());
                userMessagesList.addView(emojiTextView);
                userMessageInput.getText().clear();
            }
        });

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveMessageToDatabase();
                userMessageInput.setText("");
            }
        });



        SelectImageButton.setOnClickListener(new View.OnClickListener() {
                                                 @Override
                                                 public void onClick(View view) {

                                                     if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                                                         if (ContextCompat.checkSelfPermission(GroupChatActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                                                             Toast.makeText(GroupChatActivity.this, "Permission Denied", Toast.LENGTH_LONG).show();
                                                             ActivityCompat.requestPermissions(GroupChatActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

                                                         } else {

                                                             choseImage();

                                                         }

                                                     } else {

                                                         choseImage();

                                                     }

                                                 }

                                             }

        );

        SelectFileButton.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {

                                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                                                        if (ContextCompat.checkSelfPermission(GroupChatActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                                                            Toast.makeText(GroupChatActivity.this, "Permission Denied", Toast.LENGTH_LONG).show();
                                                            ActivityCompat.requestPermissions(GroupChatActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

                                                        } else {

                                                            choseFile();

                                                        }

                                                    } else {

                                                        choseFile();

                                                    }

                                                }

                                            }

        );

        getImage();
    }

    private void getImage(){
        if (getIntent().hasExtra("image")){
            String image = getIntent().getStringExtra("image");
            setImage(image);
        }
    }
    private void setImage(String image){
        CircleImageView circleImageView = findViewById(R.id.groupImageView);
        Picasso.get().load(image).fit().centerCrop().placeholder(R.drawable.profile2).into(circleImageView);
    }

    private void choseFile() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, 1);
    }

    private void choseImage() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(GroupChatActivity.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            progressDialog.setTitle("Sending File");
            progressDialog.setMessage("Please Wait...");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            ImageUri = result.getUri();

            final StorageReference filePath = ChatImageRef.child(UUID.randomUUID().toString() + ".jpg");

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

                        Calendar calendar = Calendar.getInstance();

                        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
                        saveCurrentDate = currentDate.format(calendar.getTime());

                        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss a");
                        saveCurrentTime = currentTime.format(calendar.getTime());

                        final String mId = MessagesRef.push().getKey();

                        HashMap<String, Object> groupMap = new HashMap<>();
                        groupMap.put("sender", currentUserID);
                        groupMap.put("messagesId", mId);
                        groupMap.put("groupId", GroupId);
                        groupMap.put("name", currentUserName);
                        groupMap.put("name2", ImageUri.getLastPathSegment());
                        groupMap.put("type", "image");
                        groupMap.put("date", saveCurrentDate);
                        groupMap.put("time", saveCurrentTime);
                        groupMap.put("message", myUri);
                        groupMap.put("image", currentUserImage);

                        MessagesRef.child(GroupId).child("Messages").child(mId).updateChildren(groupMap);
                        MessagesRef.keepSynced(true);

                        progressDialog.dismiss();

                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull @NotNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(GroupChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {

                uploadFile(data.getData());
        }
    }

    private void uploadFile(Uri data) {

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        StorageReference filePath = ChatFileRef.child(UUID.randomUUID().toString() + ".pdf");
        filePath.putFile(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uri = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uri.isComplete());
                        Uri url = uri.getResult();

                        Calendar calendar = Calendar.getInstance();

                        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
                        saveCurrentDate = currentDate.format(calendar.getTime());

                        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss a");
                        saveCurrentTime = currentTime.format(calendar.getTime());

                        final String mId = MessagesRef.push().getKey();

                        HashMap<String, Object> groupMap = new HashMap<>();
                        groupMap.put("sender", currentUserID);
                        groupMap.put("messagesId", mId);
                        groupMap.put("groupId", GroupId);
                        groupMap.put("name", currentUserName);
                        groupMap.put("type", "file");
                        groupMap.put("date", saveCurrentDate);
                        groupMap.put("time", saveCurrentTime);
                        groupMap.put("message", url.toString());
                        groupMap.put("image", currentUserImage);

                        MessagesRef.child(GroupId).child("Messages").child(mId).updateChildren(groupMap);
                        MessagesRef.keepSynced(true);

                        Toast.makeText(GroupChatActivity.this, "File Uploaded", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @NotNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(GroupChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                double progress = (100.0*taskSnapshot.getBytesTransferred())/taskSnapshot.getTotalByteCount();

                progressDialog.setMessage("Uploaded: "+(int)progress+"%");

            }
        });
    }

    private void loadMessages() {
        MessagesRef.child(GroupId).child("Messages").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                messagesList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    Messages message = ds.getValue(Messages.class);
                    messagesList.add(message);
                    textView1.setVisibility(View.INVISIBLE);
                }
                mAdapter.notifyDataSetChanged();
                userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void GetUserInfo() {
        UsersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("image"))) {
                    currentUserName = dataSnapshot.child("name").getValue().toString();
                    currentUserImage = dataSnapshot.child("image").getValue().toString();
                } else {
                    currentUserName = dataSnapshot.child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void SaveMessageToDatabase() {
        String message = userMessageInput.getText().toString().trim();

        if (TextUtils.isEmpty(message)) {
            Toast.makeText(this, "Please Enter Message...", Toast.LENGTH_SHORT).show();
        } else {
            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd, yyyy");
            currentDate = currentDateFormat.format(calForDate.getTime());

            Calendar calForTime = Calendar.getInstance();
            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
            currentTime = currentTimeFormat.format(calForTime.getTime());

            final String mId = MessagesRef.push().getKey();

            HashMap<String, Object> messageInfoMap = new HashMap<>();
            messageInfoMap.put("sender", currentUserID);
            messageInfoMap.put("messagesId", mId);
            messageInfoMap.put("groupId", GroupId);
            messageInfoMap.put("type", "text");
            messageInfoMap.put("name", currentUserName);
            messageInfoMap.put("message", message);
            messageInfoMap.put("date", currentDate);
            messageInfoMap.put("time", currentTime);
            messageInfoMap.put("image", currentUserImage);


            MessagesRef.child(GroupId).child("Messages").child(mId).updateChildren(messageInfoMap);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu3, menu);

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == R.id.clear_chats_option) {

        }
        if (item.getItemId() == R.id.settings_option) {

        }
        if (item.getItemId() == R.id.file_option) {

        }
        if (item.getItemId() == R.id.media_option) {

        }

        return true;
    }

    private void SendUserToGroupInfoActivity() {
        Intent groupInfoIntent = new Intent(GroupChatActivity.this, GroupInfoActivity.class);
        groupInfoIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(groupInfoIntent);
    }

}
