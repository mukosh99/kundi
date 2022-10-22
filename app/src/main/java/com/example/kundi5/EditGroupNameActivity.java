package com.example.kundi5;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EditGroupNameActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private TextInputLayout mGroupName;
    private Button mGroupNameButton;
    private DatabaseReference mGroupNameDatabase;
    private ProgressDialog mProgress;
    private String GroupId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_group_name);

        GroupId = getIntent().getExtras().get("ChatId").toString();

        mGroupNameDatabase = FirebaseDatabase.getInstance().getReference().child("Groups").child(GroupId);

        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Edit Group Name");



        String group_name_value = getIntent().getStringExtra("group_name_value");

        mGroupName = (TextInputLayout) findViewById(R.id.groupNameInputLayout);
        mGroupNameButton = (Button) findViewById(R.id.groupNameEditButton);
        mGroupName.getEditText().setText(group_name_value);

        mGroupNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgress = new ProgressDialog(EditGroupNameActivity.this);
                mProgress.setTitle("Saving changes");
                mProgress.setMessage("Please Wait...");
                mProgress.show();

                String groupName = mGroupName.getEditText().getText().toString();
                if (groupName.isEmpty()) {
                    mProgress.dismiss();
                    Toast.makeText(EditGroupNameActivity.this, "Group Name Is Mandatory ", Toast.LENGTH_SHORT).show();
                } else {
                    mGroupNameDatabase.child("group").setValue(groupName).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                mProgress.dismiss();
                                Toast.makeText(EditGroupNameActivity.this, "Group Name Edited Successfully", Toast.LENGTH_SHORT).show();
                                SendUserToEditGroupActivity();
                            } else {
                                mProgress.dismiss();
                                String message = task.getException().toString();
                                Toast.makeText(EditGroupNameActivity.this, "Error" + message, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }

    private void SendUserToEditGroupActivity()
    {
        Intent EditGroupIntent = new Intent(EditGroupNameActivity.this, EditGroupActivity.class);
        String ChatId = GroupId;
        EditGroupIntent.putExtra("ChatId", ChatId);
        startActivity(EditGroupIntent);
    }
}
