package com.example.kundi5;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdapterDonations extends RecyclerView.Adapter<AdapterDonations.HolderDonations> {

    private Context context;
    private ArrayList<DonationsAmt> donationsAmtList;
    private String myGroupRole;
    private FirebaseAuth mAuth;
    private String currentUserID;



    public AdapterDonations(Context context, ArrayList<DonationsAmt> donationsAmtList) {
        this.context = context;
        this.donationsAmtList = donationsAmtList;

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

    }

    @NonNull
    @Override
    public HolderDonations onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.donations, parent, false);
        return new HolderDonations(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final HolderDonations holder, int position) {
        DonationsAmt donationsAmt = donationsAmtList.get(position);
        final String uid = donationsAmt.getUid();
        final String dId = donationsAmt.getDonationsId();
        final String GroupId = donationsAmt.getGroupId();
        String name = donationsAmt.getName();
        String donations = donationsAmt.getDonations();

        holder.DonationsName.setText(name);
        holder.DonationsAmt.setText(donations);

        holder.ParticipantsDonationsRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String dialogTitle="";
                String dialogDescription="";
                String positiveButtonTitle="";
                if (myGroupRole.equals("creator") || myGroupRole.equals("admin ")){
                    dialogTitle="Delete Donation";
                    dialogDescription = "Are You Sure You Want To Delete Donation Permanently?";
                    positiveButtonTitle = "DELETE";
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(dialogTitle)
                        .setMessage(dialogDescription)
                        .setPositiveButton(positiveButtonTitle, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (myGroupRole.equals("creator") || myGroupRole.equals("admin ")){
                                    deleteDonation();
                                }
                            }
                        })
                        .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }

            private void deleteDonation() {
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups").child(GroupId);
                ref.keepSynced(true);
                ref.child("Donations").child(dId).removeValue();
            }
        });

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.keepSynced(true);
        ref.child(GroupId).child("Participants")
                .orderByChild("uid").equalTo(currentUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds: dataSnapshot.getChildren()){
                            myGroupRole = ""+ds.child("role").getValue();
                            if (myGroupRole.equals("creator") || myGroupRole.equals("admin ")){
                                holder.ParticipantsDonationsRemove.setVisibility(View.VISIBLE);

                            }else {
                                holder.ParticipantsDonationsRemove.setVisibility(View.GONE);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return donationsAmtList.size();
    }

    class HolderDonations extends RecyclerView.ViewHolder{

        private TextView DonationsName,DonationsAmt;
        private Button ParticipantsDonationsRemove;

        public HolderDonations(@NonNull View itemView) {
            super(itemView);
            DonationsName = itemView.findViewById(R.id.donations_name);
            DonationsAmt = itemView.findViewById(R.id.participants_donationsAmt);
            ParticipantsDonationsRemove = itemView.findViewById(R.id.participants_donationsRemove);
        }
    }
}
