package com.example.kundi5;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder>
{
    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;

    private List<Messages> userMessagesList;
    private FirebaseAuth firebaseAuth;
    private String CurrentId;

    public MessagesAdapter (List<Messages> userMessagesList)
    {
        this.userMessagesList = userMessagesList;

        firebaseAuth = FirebaseAuth.getInstance();
        CurrentId = firebaseAuth.getCurrentUser().getUid();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder
    {
        public TextView senderMessageText,senderMessageName,createdAtText,createdAtImage,createdAtFile;
        public CircleImageView groupImage;
        public ImageView MessagesImage,FileImage;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            senderMessageText = (TextView)itemView.findViewById(R.id.groupMessageText);
            senderMessageName = (TextView)itemView.findViewById(R.id.messages_profile_name);
            createdAtText = (TextView)itemView.findViewById(R.id.dateTimeText);
            createdAtImage = (TextView)itemView.findViewById(R.id.dateTimeTextImage);
            createdAtFile = (TextView)itemView.findViewById(R.id.dateTimeTextFile);
            MessagesImage = (ImageView) itemView.findViewById(R.id.messages_image);
            FileImage = (ImageView) itemView.findViewById(R.id.file_image);
            groupImage = (CircleImageView) itemView.findViewById(R.id.messages_profile_image);

        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from((parent.getContext()))
                    .inflate(R.layout.group_messages_layout_right, parent, false);
            return new MessageViewHolder(view);
        }
        else {
            View view = LayoutInflater.from((parent.getContext()))
                    .inflate(R.layout.group_messages_layout_left, parent, false);
            return new MessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, final int position)
    {

        Messages messages = userMessagesList.get(position);
        String type = messages.getType();
        holder.senderMessageName.setText(messages.getName());
        holder.createdAtText.setText((messages.getTime()) + " " + (messages.getDate()));
        holder.createdAtImage.setText((messages.getTime()) + " " + (messages.getDate()));
        holder.createdAtFile.setText((messages.getTime()) + " " + (messages.getDate()));


        if (type.equals("image")){
            Picasso.get().load(messages.getMessage()).fit().centerCrop().into(holder.MessagesImage);
            holder.MessagesImage.setVisibility(View.VISIBLE);
            holder.FileImage.setVisibility(View.GONE);
            holder.senderMessageText.setVisibility(View.GONE);
            holder.createdAtText.setVisibility(View.GONE);
            holder.createdAtFile.setVisibility(View.GONE);
            holder.senderMessageName.setVisibility(View.GONE);

        }else if (type.equals("text")){
            holder.FileImage.setVisibility(View.GONE);
            holder.senderMessageText.setText(messages.getMessage());
            holder.createdAtImage.setVisibility(View.GONE);
            holder.createdAtFile.setVisibility(View.GONE);
            holder.MessagesImage.setVisibility(View.GONE);

        }else if (type.equals("file")){
            holder.senderMessageText.setVisibility(View.GONE);
            holder.createdAtText.setVisibility(View.GONE);
            holder.senderMessageName.setVisibility(View.GONE);
            holder.createdAtImage.setVisibility(View.GONE);
            holder.MessagesImage.setVisibility(View.GONE);
            holder.FileImage.setVisibility(View.VISIBLE);

        }

        try {
            Picasso.get().load(messages.getImage()).placeholder(R.drawable.profile_image).fit().centerCrop().into(holder.groupImage);

        }
        catch (Exception e){
            holder.groupImage.setImageResource(R.drawable.profile_image);
        }

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                if (userMessagesList.get(position).getType().equals("file"))
                {
                    CharSequence[] options = new CharSequence[]
                            {
                                    "Delete" ,
                                    "Download and View" ,
                                    "Cancel" ,
                            };
                    AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                    builder.setTitle("Delete File or View File");

                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int i) {
                            if (i == 0){
                                delete(position, holder);

                            }else if (i == 1){
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                                holder.itemView.getContext().startActivity(intent);
                            }
                        }
                    });
                    builder.show();
                }
                else if (userMessagesList.get(position).getType().equals("text"))
                {
                    CharSequence[] options = new CharSequence[]
                            {
                                    "Delete" ,
                                    "Cancel" ,
                            };
                    AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                    builder.setTitle("Delete Message?");

                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int i) {
                            if (i == 0){
                                delete(position, holder);

                            }
                        }
                    });
                    builder.show();
                }
                else if (userMessagesList.get(position).getType().equals("image"))
                {
                    CharSequence[] options = new CharSequence[]
                            {
                                    "Delete" ,
                                    "View Image" ,
                                    "Cancel" ,
                            };
                    AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                    builder.setTitle("Delete Message or View Image?");

                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int i) {
                            if (i == 0){
                                delete(position, holder);

                            }else if (i == 1){
                                Intent intent = new Intent(holder.itemView.getContext(), ViewChatImageActivity.class);
                                intent.putExtra("url", userMessagesList.get(position).getMessage());
                                holder.itemView.getContext().startActivity(intent);
                            }
                        }
                    });
                    builder.show();
                }
                return false;
            }
        });
        }

    @Override
    public int getItemCount()
    {
        return userMessagesList.size();
    }

    public void setUserMessagesList(List<Messages> userMessagesList) {
        this.userMessagesList = userMessagesList;
        notifyDataSetChanged();
    }

    private void delete(final int position, final MessageViewHolder messageViewHolder) {
        String messageID = userMessagesList.get(position).getMessagesId();
        String groupID = userMessagesList.get(position).getGroupId();
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference("Groups").child(groupID).child("Messages");
        rootRef.orderByChild("messagesId").equalTo(messageID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds: dataSnapshot.getChildren()){

                            if (ds.child("sender").getValue().equals(CurrentId)){
                                ds.getRef().removeValue();
                                Toast.makeText(messageViewHolder.itemView.getContext(), "Deleted Successfully...", Toast.LENGTH_SHORT).show();
                            }else {
                                Toast.makeText(messageViewHolder.itemView.getContext(), "You can only delete your messages...", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public int getItemViewType(int position) {
        if (userMessagesList.get(position).getSender().equals(CurrentId)){
            return MSG_TYPE_RIGHT;
        }else {
            return MSG_TYPE_LEFT;
        }
    }
}
