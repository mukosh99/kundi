package com.example.kundi5;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdapterParticipant extends RecyclerView.Adapter<AdapterParticipant.HolderParticipant> {

    private Context context;
    private ArrayList<Participants> participantsList;
    private String groupId, myGroupRole;
    private FirebaseAuth mAuth;
    private String currentUserID;

    public AdapterParticipant(Context context, ArrayList<Participants> participantsList, String groupId, String myGroupRole) {
        this.context = context;
        this.participantsList = participantsList;
        this.groupId = groupId;
        this.myGroupRole = myGroupRole;
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
    }

    @NonNull
    @Override
    public HolderParticipant onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.participants_layout,parent,false);
        return new HolderParticipant(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderParticipant holder, int position) {

        final Participants participants = participantsList.get(position);
        String name = participants.getName();
        String status = participants.getStatus();
        String image = participants.getImage();
        final String hisUID = participants.getUid();

        holder.participants_name.setText(name);
        holder.participants_status.setText(status);
        try {
            Picasso.get().load(image).placeholder(R.drawable.profile_image).into(holder.participants_image);
        }
        catch (Exception e){
            holder.participants_image.setImageResource(R.drawable.profile_image);
        }
        checkIfAlreadyExists(participants, holder);
        DisplayLastSeen(participants, holder);
    }

    private void DisplayLastSeen(Participants participants, final HolderParticipant holder) {
        DatabaseReference RootRef = FirebaseDatabase.getInstance().getReference("Users");
        RootRef.keepSynced(true);
        RootRef.child(currentUserID).child("onlineStatus").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("state")) {
                    String status = "" + dataSnapshot.child("onlineStatus").getValue().toString();
                    String date = "" + dataSnapshot.child("date").getValue().toString();
                    String time = "" + dataSnapshot.child("time").getValue().toString();
                    if (status.equals("online")) {
                        holder.participants_state.setText("online");
                    } else if ((status.equals("offline"))){
                        holder.participants_state2.setVisibility(View.VISIBLE);
                        holder.participants_state.setVisibility(View.INVISIBLE);
                        holder.participants_state_time2.setVisibility(View.VISIBLE);
                        holder.participants_state2.setText("offline");
                        holder.participants_state_time2.setText("Last Seen: " + date + " " + time);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void checkIfAlreadyExists(Participants participants, final HolderParticipant holder) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.keepSynced(true);
        ref.child(groupId).child("Participants").child(participants.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            String hisRole = ""+dataSnapshot.child("role").getValue();
                            holder.participants_status.setText(hisRole);
                        }
                        else {
                            holder.participants_status.setText("");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return participantsList.size();
    }

    class HolderParticipant extends RecyclerView.ViewHolder{
        private ImageView participants_image;
        private TextView participants_name,participants_status,participants_state,participants_state2,participants_state_time2;

        public HolderParticipant(@NonNull View itemView) {
            super(itemView);

            participants_image = itemView.findViewById(R.id.participants_image);
            participants_name = itemView.findViewById(R.id.participants_name);
            participants_status = itemView.findViewById(R.id.participants_status);
            participants_state = itemView.findViewById(R.id.participants_state);
            participants_state2 = itemView.findViewById(R.id.participants_state2);
            participants_state_time2 = itemView.findViewById(R.id.participants_state_time2);
        }
    }
}
