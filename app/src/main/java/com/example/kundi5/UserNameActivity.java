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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UserNameActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private TextInputLayout mName;
    private Button mButton;
    private DatabaseReference mNameDatabase;
    private FirebaseUser mCurrentUser;
    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_name);
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = mCurrentUser.getUid();
        mNameDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);

        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Edit Name");



        String name_value = getIntent().getStringExtra("name_value");

        mName = (TextInputLayout) findViewById(R.id.nameInputLayout);
        mButton = (Button) findViewById(R.id.nameEditButton);
        mName.getEditText().setText(name_value);

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgress = new ProgressDialog(UserNameActivity.this);
                mProgress.setTitle("Saving changes");
                mProgress.setMessage("Please Wait...");
                mProgress.show();

                String name = mName.getEditText().getText().toString();
                mNameDatabase.child("name").setValue(name).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                       if (task.isSuccessful()){
                           mProgress.dismiss();
                           Toast.makeText(UserNameActivity.this, "Name Edited Successfully", Toast.LENGTH_SHORT).show();
                           SendUserToProfileActivity();
                       }else {
                           mProgress.dismiss();
                           String message = task.getException().toString();
                           Toast.makeText(UserNameActivity.this, "Error" + message, Toast.LENGTH_SHORT).show();
                       }
                    }
                });
            }
        });
    }

    private void SendUserToProfileActivity()
    {
        Intent profileIntent = new Intent(UserNameActivity.this, ProfileActivity.class);
        startActivity(profileIntent);
    }
}
