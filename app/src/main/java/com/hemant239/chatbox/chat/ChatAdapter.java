package com.hemant239.chatbox.chat;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.hemant239.chatbox.AllChatsActivity;
import com.hemant239.chatbox.ImageViewActivity;
import com.hemant239.chatbox.R;
import com.hemant239.chatbox.SpecificChatActivity;

import java.util.ArrayList;



public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    ArrayList<ChatObject> mChatList;
    Context context;

    public ChatAdapter(ArrayList<ChatObject> chatList, AllChatsActivity allChatsActivity){
        mChatList=chatList;
        context=allChatsActivity;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat,null,false);

        RecyclerView.LayoutParams layoutParams=new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(layoutParams);

        return new ChatAdapter.ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        final ChatObject chatObject=mChatList.get(position);
        holder.mChatName.setText(chatObject.getName());

        if (chatObject.isSingleChat() || chatObject.getLastSenderId().equals(AllChatsActivity.curUser.getUid())) {
            holder.mLastSender.setVisibility(View.GONE);
            holder.mColon.setVisibility(View.GONE);
        }

        if(chatObject.getLastMessageText()!=null) {
            holder.mLastSender.setText(chatObject.getLastMessageSender());
            holder.mLastMessage.setText(chatObject.getLastMessageText());
            holder.mLastMessageTime.setText(chatObject.getLastMessageTime());
        }

        if (!chatObject.getImageUri().equals("")) {
            holder.mChatImage.setClipToOutline(true);
            Glide.with(context).load(Uri.parse(chatObject.getImageUri())).into(holder.mChatImage);
        } else {
            holder.mChatImage.setImageResource(R.drawable.ic_baseline_person_24);
        }
        holder.mChatImage.setOnClickListener(v -> {
            Intent intent = new Intent(context, ImageViewActivity.class);
            intent.putExtra("URI", chatObject.getImageUri());
            context.startActivity(intent);
        });

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, SpecificChatActivity.class);
            intent.putExtra("chatObject", chatObject);
            context.startActivity(intent);
        });


    }

    @Override
    public int getItemCount() {
        return mChatList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView mChatName, mLastMessage, mLastSender,mLastMessageTime,mColon;

        ImageView mChatImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mChatName=itemView.findViewById(R.id.chatName);
            mChatImage=itemView.findViewById(R.id.chatProfileImage);
            mLastMessage=itemView.findViewById(R.id.lastMessage);
            mLastSender=itemView.findViewById(R.id.lastMessageSender);
            mLastMessageTime=itemView.findViewById(R.id.lastMessageTime);
            mColon=itemView.findViewById(R.id.colon);
        }

    }
}
