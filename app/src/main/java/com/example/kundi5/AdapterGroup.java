package com.example.kundi5;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class AdapterGroup extends RecyclerView.Adapter<AdapterGroup.HolderGroupChatList> implements Filterable {


    private ArrayList<Groups>groupsArrayList;
    private ArrayList<Groups>groupsArrayListFull;
    private int countFriends = 0;
    private String myGroupRole;
    private FirebaseAuth mAuth;
    private String currentUserID;
    private String groupIdImage;
    private String groupName, groupId;
    private GroupClickInterface groupClickInterface;


    public AdapterGroup(ArrayList<Groups> groupsArrayList, GroupClickInterface groupClickInterface) {
        this.groupsArrayList = groupsArrayList;
        this.groupClickInterface = groupClickInterface;
        groupsArrayListFull=new ArrayList<>(groupsArrayList);
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

    }

    @NonNull
    @Override
    public HolderGroupChatList onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.groups_display_layout, parent, false);

        return new HolderGroupChatList(view, groupClickInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull final HolderGroupChatList holder, int position) {

        Groups groups = groupsArrayList.get(position);
        groupId = groups.getGroupId();
        String groupImage = groups.getGroupImage();
        groupName = groups.getGroup();
        groupIdImage = groups.getGroupIdImage();

        holder.createdText.setText("Created At:" + (groups.getTime()) + " " + (groups.getDate()));
        holder.createdByText.setText("By:" + groups.getName());
        //holder.group_members.setText("Participants("+groupsArrayList.size()+")");

        holder.group_name.setText(groupName);
        try {
            Picasso.get().load(groupImage).placeholder(R.drawable.profile_image).fit().centerCrop().into(holder.group_image);
        }catch (Exception e){
            holder.group_image.setImageResource(R.drawable.profile_image);
        }


        DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("Groups").child(groupId).child("Participants");
        ref1.keepSynced(true);
        ref1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    countFriends = (int) dataSnapshot.getChildrenCount();
                    holder.group_members.setText(Integer.toString(countFriends));

                } else {
                    holder.group_members.setText("0");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.keepSynced(true);
        ref.child(groupId).child("Participants")
                .orderByChild("uid").equalTo(currentUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds: dataSnapshot.getChildren()){
                            myGroupRole = ""+ds.child("role").getValue();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return groupsArrayList.size();
    }

    @Override
    public Filter getFilter() {
        return FilterGroup;
    }
    private Filter FilterGroup=new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            String searchText=constraint.toString().toLowerCase();
            List<Groups>tempList=new ArrayList<>();
            if (searchText.length()==0 || searchText.isEmpty())
            {
                tempList.addAll(groupsArrayListFull);

            }
            else
            {
                for (Groups item:groupsArrayListFull)
                {
                    if (item.getGroup().toLowerCase().contains(searchText))
                    {
                        tempList.add(item);
                    }
                }
            }
            FilterResults filterResults=new FilterResults();
            filterResults.values=tempList;
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            groupsArrayList.clear();
            groupsArrayList.addAll((Collection<? extends Groups>) results.values);
            notifyDataSetChanged();
        }
    };

    class HolderGroupChatList extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView group_image;
        private TextView group_name,createdText,createdByText,group_members;
        GroupClickInterface groupClickInterface;

        public HolderGroupChatList(@NonNull final View itemView, final GroupClickInterface groupClickInterface) {
            super(itemView);

            group_image = itemView.findViewById(R.id.group_image);
            group_name = itemView.findViewById(R.id.group_name);
            createdText = itemView.findViewById(R.id.createdAt);
            createdByText = itemView.findViewById(R.id.createdBy);
            group_members = itemView.findViewById(R.id.group_members);
            this.groupClickInterface = groupClickInterface;

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (groupClickInterface !=null){
                        int pos = getAdapterPosition();
                        if (pos != RecyclerView.NO_POSITION){
                            groupClickInterface.onLongItemClick(pos);
                        }
                    }
                    String dialogTitle="";
                    String dialogDescription="";
                    String positiveButtonTitle="";
                    if (myGroupRole.equals("creator") || myGroupRole.equals("admin ")){
                        dialogTitle="Delete Group";
                        dialogDescription = "Are You Sure You Want To Delete Group Permanently?";
                        positiveButtonTitle = "DELETE";
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(itemView.getContext());
                    builder.setTitle(dialogTitle)
                            .setMessage(dialogDescription)
                            .setPositiveButton(positiveButtonTitle, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (myGroupRole.equals("creator") || myGroupRole.equals("admin ")){
                                        deleteGroup();
                                    }
                                }

                                private void deleteGroup() {
                                    DatabaseReference ref2 = FirebaseDatabase.getInstance().getReference().child("Groups").child(groupId);
                                    ref2.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Group Images").child(groupIdImage+".jpg");
                                            storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    Toast.makeText(itemView.getContext(), "Group Image Deleted Successfully", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                            Toast.makeText(itemView.getContext(), "Group Deleted Successfully", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                }
                            })
                            .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .create().show();
                    return true;
                }

            });
        }
        @Override
        public void onClick(View view) {
          if (groupClickInterface !=null){
              int pos = getAdapterPosition();
              if (pos != RecyclerView.NO_POSITION){
                  groupClickInterface.onItemClick(pos);
              }
          }
        }
    }
}
