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

public class EditEmailActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private TextInputLayout mEmail;
    private Button mButton;
    private DatabaseReference mEmailDatabase;
    private FirebaseUser mCurrentUser;
    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_email);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = mCurrentUser.getUid();
        mEmailDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);

        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Edit Email");



        String email_value = getIntent().getStringExtra("email_value");

        mEmail = (TextInputLayout) findViewById(R.id.emailInputLayout);
        mButton = (Button) findViewById(R.id.emailEditButton);
        mEmail.getEditText().setText(email_value);

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgress = new ProgressDialog(EditEmailActivity.this);
                mProgress.setTitle("Saving changes");
                mProgress.setMessage("Please Wait...");
                mProgress.show();

                String email = mEmail.getEditText().getText().toString();
                mEmailDatabase.child("email").setValue(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            mProgress.dismiss();
                            Toast.makeText(EditEmailActivity.this, "Email Edited Successfully", Toast.LENGTH_SHORT).show();
                            SendUserToProfileActivity();
                        }else {
                            mProgress.dismiss();
                            String message = task.getException().toString();
                            Toast.makeText(EditEmailActivity.this, "Error" + message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    private void SendUserToProfileActivity()
    {
        Intent profileIntent = new Intent(EditEmailActivity.this, ProfileActivity.class);
        startActivity(profileIntent);
    }
}


