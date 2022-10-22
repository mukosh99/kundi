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

public class EditDescripionActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private TextInputLayout mDescription;
    private Button mDescriptionButton;
    private DatabaseReference mDescriptionDatabase;
    private ProgressDialog mProgress;
    private String GroupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_descripion);

        GroupId = getIntent().getExtras().get("ChatId").toString();

        mDescriptionDatabase = FirebaseDatabase.getInstance().getReference().child("Groups").child(GroupId);

        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Edit Description");



        String description_value = getIntent().getStringExtra("description_value");

        mDescription = (TextInputLayout) findViewById(R.id.descriptionInputLayout);
        mDescriptionButton = (Button) findViewById(R.id.descriptionEditButton);
        mDescription.getEditText().setText(description_value);

        mDescriptionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgress = new ProgressDialog(EditDescripionActivity.this);
                mProgress.setTitle("Saving changes");
                mProgress.setMessage("Please Wait...");
                mProgress.show();

                String description = mDescription.getEditText().getText().toString();
                mDescriptionDatabase.child("description").setValue(description).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            mProgress.dismiss();
                            Toast.makeText(EditDescripionActivity.this, "Description Edited Successfully", Toast.LENGTH_SHORT).show();
                            SendUserToEditGroupActivity();
                        }else {
                            mProgress.dismiss();
                            String message = task.getException().toString();
                            Toast.makeText(EditDescripionActivity.this, "Error" + message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    private void SendUserToEditGroupActivity()
    {
        Intent EditGroupIntent = new Intent(EditDescripionActivity.this, EditGroupActivity.class);
        String ChatId = GroupId;
        EditGroupIntent.putExtra("ChatId", ChatId);
        startActivity(EditGroupIntent);
        finish();
    }
}
