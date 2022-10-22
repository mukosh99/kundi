package com.example.kundi5;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

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

public class DonationsAdapter extends RecyclerView.Adapter<DonationsAdapter.HolderDonations> {

    private Context context;
    private ArrayList<Pledges>pledgesList;
    private String myGroupRole;
    private FirebaseAuth mAuth;
    private String currentUserID;

    public DonationsAdapter(Context context, ArrayList<Pledges> pledgesList) {
        this.context = context;
        this.pledgesList = pledgesList;

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
    }

    @NonNull
    @Override
    public HolderDonations onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.pledges_layout, parent, false);
        return new HolderDonations(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final HolderDonations holder, int position) {
        Pledges pledges = pledgesList.get(position);
        final String uid = pledges.getUid();
        final String GroupId = pledges.getGroupId();
        String name = pledges.getName();
        String image = pledges.getImage();
        String pledgeAmt = pledges.getPledge();

        holder.PledgesName.setText(name);
        holder.PledgesAmt.setText(pledgeAmt);

        try {
            Picasso.get().load(image).placeholder(R.drawable.profile_image).into(holder.PledgesImage);
        }
        catch (Exception e){
            holder.PledgesImage.setImageResource(R.drawable.profile_image);
        }

        holder.ParticipantsPledgesDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String dialogTitle="";
                String dialogDescription="";
                String positiveButtonTitle="";
                if (myGroupRole.equals("creator") || myGroupRole.equals("admin ")){
                    dialogTitle="Delete Pledge";
                    dialogDescription = "Are You Sure You Want To Delete Pledge Permanently?";
                    positiveButtonTitle = "DELETE";
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(dialogTitle)
                        .setMessage(dialogDescription)
                        .setPositiveButton(positiveButtonTitle, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (myGroupRole.equals("creator") || myGroupRole.equals("admin ")){
                                    deletePledge();
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

            private void deletePledge(){
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups").child(GroupId);
                ref.child("Pledges").child(currentUserID).removeValue();
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
                                holder.ParticipantsPledgesDelete.setVisibility(View.VISIBLE);

                            }else {
                                holder.ParticipantsPledgesDelete.setVisibility(View.GONE);
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
        return pledgesList.size();
    }

    class HolderDonations extends RecyclerView.ViewHolder{

        private ImageView PledgesImage;
        private TextView PledgesName,PledgesAmt;
        private Button ParticipantsPledgesDelete;

        public HolderDonations(@NonNull View itemView) {
            super(itemView);
            PledgesImage = itemView.findViewById(R.id.pledges_image);
            PledgesName = itemView.findViewById(R.id.pledges_name);
            PledgesAmt = itemView.findViewById(R.id.participants_pledgeAmt);
            ParticipantsPledgesDelete = itemView.findViewById(R.id.participants_pledges_delete);
        }
    }
}
