package com.hemant239.chatbox.message;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hemant239.chatbox.AllChatsActivity;
import com.hemant239.chatbox.ImageViewActivity;
import com.hemant239.chatbox.R;
import com.hemant239.chatbox.SpecificChatActivity;
import com.hemant239.chatbox.utils.OnDoubleClickListener;

import java.util.ArrayList;
import java.util.HashMap;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    ArrayList<MessageObject> messageList;

    String userKey,
            chatID;
    int numberOfUsers;
    float density;

    int lastMessageIndex;

    AlertDialog alertDialog;

    Context context;
    RecyclerView mRecyclerView;


    public MessageAdapter(ArrayList<MessageObject> messageList, Context context, int numberOfUsers, float density, String chatKey) {
        this.messageList = messageList;
        this.context = context;
        this.numberOfUsers = numberOfUsers;
        userKey = AllChatsActivity.curUser.getUid();
        this.density = density;
        this.chatID = chatKey;

        lastMessageIndex = messageList.size() - 1;

        setHasStableIds(true);
    }

    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message,null,false);

        RecyclerView.LayoutParams layoutParams= new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(layoutParams);

        mRecyclerView= (RecyclerView) parent;

        return new MessageAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageAdapter.ViewHolder holder, final int position) {
        final MessageObject curMessage = messageList.get(position);
        MessageObject nextMessage = new MessageObject();
        if (position != messageList.size() - 1) {
            nextMessage = messageList.get(position + 1);
        }


        if (curMessage.messageId == null && curMessage.text == null &&
                curMessage.senderId == null && curMessage.senderName == null &&
                curMessage.imageUri == null && curMessage.time == null && curMessage.date == null) {
            holder.relativeLayout.setVisibility(View.GONE);
        } else if (curMessage.isInfo()) {
            holder.messageText.setText(curMessage.getDate());
            if (nextMessage.getMessageId() != null && nextMessage.isInfo) {
                holder.relativeLayout.setVisibility(View.GONE);
            }
            holder.messageSender.setVisibility(View.GONE);
            holder.messageTime.setVisibility(View.GONE);
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) holder.relativeLayout.getLayoutParams();
            layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
            holder.relativeLayout.setLayoutParams(layoutParams);
            holder.relativeLayout.setBackgroundResource(R.drawable.custom_background_message_info);

        } else {
            holder.relativeLayout.setVisibility(View.VISIBLE);
            holder.messageText.setText(curMessage.getText());
            holder.messageSender.setText(curMessage.getSenderName());
            holder.messageTime.setText(curMessage.getTime());

            if (curMessage.isDeletedForEveryone()) {
                holder.messageText.setTypeface(null, Typeface.ITALIC);
            }


            if (curMessage.getText().equals("")) {
                holder.messageText.setVisibility(View.GONE);
                RelativeLayout.LayoutParams layoutParams1 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams1.addRule(RelativeLayout.ALIGN_END, R.id.mediaImage);
                layoutParams1.addRule(RelativeLayout.BELOW, R.id.mediaImage);
                holder.messageTime.setLayoutParams(layoutParams1);
            }


            if (numberOfUsers == 1 || curMessage.getSenderId().equals(userKey) || (position != 0 && messageList.get(position - 1).senderId != null && messageList.get(position - 1).senderId.equals(curMessage.senderId))) {
                holder.messageSender.setVisibility(View.GONE);
            } else if ((position != messageList.size() - 1 && curMessage.getSenderId().equals(nextMessage.getSenderId()))) {
                int pos = holder.getAdapterPosition();
                MessageAdapter.ViewHolder nextHolder = (ViewHolder) mRecyclerView.findViewHolderForAdapterPosition(pos + 1);
                if (nextHolder != null && nextHolder.messageSender != null) {
                    nextHolder.messageSender.setVisibility(View.GONE);
                }
            }


            holder.messageText.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            float width = holder.messageText.getMeasuredWidth() / density;

            if (width > 240) {
                RelativeLayout.LayoutParams layoutParams1 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams1.addRule(RelativeLayout.ALIGN_END, R.id.messageText);
                layoutParams1.addRule(RelativeLayout.BELOW, R.id.messageText);
                holder.messageTime.setLayoutParams(layoutParams1);
            }

            if (!curMessage.getImageUri().equals("")) {

                holder.mediaImage.setVisibility(View.VISIBLE);
                holder.mediaImage.setClipToOutline(true);
                if (!curMessage.getText().equals("")) {
                    RelativeLayout.LayoutParams layoutParams1 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    layoutParams1.addRule(RelativeLayout.ALIGN_END, R.id.mediaImage);
                    layoutParams1.addRule(RelativeLayout.BELOW, R.id.messageText);
                    holder.messageTime.setLayoutParams(layoutParams1);

                }

                if (curMessage.isTagged()) {
                    RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) holder.taggedLayout.getLayoutParams();
                    layoutParams.addRule(RelativeLayout.ALIGN_END, R.id.mediaImage);
                    layoutParams.addRule(RelativeLayout.ALIGN_START, R.id.mediaImage);
                    holder.taggedLayout.setLayoutParams(layoutParams);


                    RelativeLayout.LayoutParams layoutParams1 = (RelativeLayout.LayoutParams) holder.taggedImage.getLayoutParams();
                    layoutParams1.addRule(RelativeLayout.ALIGN_PARENT_END);
                    layoutParams1.removeRule(RelativeLayout.END_OF);
                    holder.taggedImage.setLayoutParams(layoutParams1);

                }


                holder.mediaImage.setOnClickListener(v -> {
                    Intent intent = new Intent(context, ImageViewActivity.class);
                    intent.putExtra("URI", curMessage.getImageUri());
                    context.startActivity(intent);
                });
                Glide.with(context).load(Uri.parse(curMessage.getImageUri())).into(holder.mediaImage);
            } else {
                holder.mediaImage.setVisibility(View.GONE);
            }


            if (curMessage.getSenderId().equals(userKey)) {
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) holder.relativeLayout.getLayoutParams();
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END);
                holder.relativeLayout.setLayoutParams(layoutParams);
                holder.relativeLayout.setBackgroundResource(R.drawable.custom_background_message_receiver);
                holder.taggedLayout.setBackgroundResource(R.drawable.custom_tagged_receiver);
            }


            if (curMessage.isTagged()) {
                holder.taggedLayout.setVisibility(View.VISIBLE);
                holder.taggedSender.setText(curMessage.getTaggedSender());
                String text = curMessage.getTaggedText();
                if (text.equals("")) {
                    text = "photo";
                }
                holder.taggedText.setText(text);

                String image = curMessage.getTaggedImageUri();
                if (!image.equals("")) {
                    holder.taggedImage.setVisibility(View.VISIBLE);
                    Glide.with(context).load(Uri.parse(curMessage.getTaggedImageUri())).into(holder.taggedImage);
                } else {
                    holder.taggedImage.setVisibility(View.GONE);
                    holder.taggedText.setMaxWidth((int) (250 * density));
                }


                if (curMessage.getImageUri().equals("")) {
                    holder.messageText.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                    float messageWidth = holder.messageText.getMeasuredWidth() / density;
                    holder.messageTime.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                    float timeWidth = holder.messageTime.getMeasuredWidth() / density;
                    holder.taggedLayout.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                    float taggedWidth = holder.taggedLayout.getMeasuredWidth() / density;

                    if (messageWidth + timeWidth > taggedWidth) {
                        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) holder.taggedLayout.getLayoutParams();
                        layoutParams.addRule(RelativeLayout.ALIGN_END, R.id.messageTime);
                        layoutParams.addRule(RelativeLayout.ALIGN_START, R.id.messageText);
                        holder.taggedLayout.setLayoutParams(layoutParams);


                        RelativeLayout.LayoutParams layoutParams1 = (RelativeLayout.LayoutParams) holder.taggedImage.getLayoutParams();
                        layoutParams1.addRule(RelativeLayout.ALIGN_PARENT_END);
                        layoutParams1.removeRule(RelativeLayout.END_OF);
                        holder.taggedImage.setLayoutParams(layoutParams1);
                    } else {
                        RelativeLayout.LayoutParams layoutParams1 = (RelativeLayout.LayoutParams) holder.messageTime.getLayoutParams();
                        layoutParams1.addRule(RelativeLayout.ALIGN_END, R.id.messageTaggedLayout);
                        layoutParams1.removeRule(RelativeLayout.END_OF);
                        holder.messageTime.setLayoutParams(layoutParams1);

                    }
                }


                holder.taggedLayout.setOnClickListener(v -> {
                    MessageObject taggedMessage = new MessageObject(curMessage.getTaggedId());
                    int indexOfTaggedMessage = messageList.indexOf(taggedMessage);
                    if (indexOfTaggedMessage > -1) {
                        SpecificChatActivity.mMessageListLayoutManager.scrollToPosition(indexOfTaggedMessage);
                    }
                });
            }


            holder.itemView.setOnLongClickListener(v -> {
                alertDialog = new AlertDialog.Builder(context)
                        .setTitle("Delete this message?")
                        .create();

                alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Delete For Me", (dialog, which) -> {
                    int holderPos = holder.getAdapterPosition();
                    databaseDeleteForMe(curMessage.getMessageId(), position, holderPos);
                });

                if (!curMessage.isDeletedForEveryone && curMessage.getSenderId().equals(userKey)) {
                    alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "Deleted For Everyone", (dialog, which) -> databaseDeleteForEveryone(curMessage.getMessageId()));
                }
                alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", (dialog, which) -> alertDialog.dismiss());

                alertDialog.show();


                return true;
            });


            OnDoubleClickListener onDoubleClickListener = new OnDoubleClickListener() {
                @Override
                public void onDoubleClick(View v) {
                    ((SpecificChatActivity) context).OnItemDoubleClicked(position);
                }

                @Override
                public void onSingleClick(View v) {
                }
            };

            if (!curMessage.isDeletedForEveryone()) {
                holder.itemView.setOnClickListener(onDoubleClickListener);
            }


        }
    }

    private void databaseDeleteForEveryone(String messageId) {
        DatabaseReference mMessageDb = FirebaseDatabase.getInstance().getReference().child("Chats/" + chatID + "/Messages/" + messageId);
        HashMap<String, Object> messageInfo = new HashMap<>();
        messageInfo.put("text", "");
        messageInfo.put("Image Uri", "");
        messageInfo.put("Deleted For Everyone", userKey);
        mMessageDb.updateChildren(messageInfo);
        mMessageDb.child("isTagged").setValue(null);
    }

    private void databaseDeleteForMe(String messageId, int position, int holderPos) {
        FirebaseDatabase.getInstance().getReference().child("Chats/" + chatID + "/Messages/" + messageId + "/Deleted For/" + userKey).setValue(true);
        MessageAdapter.ViewHolder curHolder = (ViewHolder) mRecyclerView.findViewHolderForAdapterPosition(holderPos);

        assert curHolder != null;
        messageList.set(position, new MessageObject());
        curHolder.relativeLayout.setVisibility(View.GONE);

        if (lastMessageIndex == -1) {
            lastMessageIndex = getItemCount() - 1;
        }
        if (position == lastMessageIndex) {
            String lastMessageId = "";
            long chatPriority = 0L;
            int i;
            for (i = lastMessageIndex; i >= 0; i--) {
                MessageObject lastMessage = messageList.get(i);
                if (lastMessage.getMessageId() != null && !lastMessage.isInfo()) {
                    lastMessageIndex = i;
                    lastMessageId = lastMessage.getMessageId();
                    chatPriority = lastMessage.getTimeStamp();
                    break;
                }
            }

            FirebaseDatabase.getInstance().getReference().child("Chats/" + chatID + "/info/user/" + userKey).setValue(lastMessageId);
            FirebaseDatabase.getInstance().getReference().child("Users/" + userKey + "/chat/" + chatID).setValue(chatPriority);
            ((AllChatsActivity) AllChatsActivity.context).recreate();
        }

        int nextHolderPos = holderPos + 1;
        int prevHolderPos = holderPos - 1;
        MessageAdapter.ViewHolder nextHolder = null;
        MessageAdapter.ViewHolder prevHolder = null;

        SpecificChatActivity.mMessageAdapter.notifyItemRangeChanged(holderPos, nextHolderPos - holderPos);

        if (nextHolderPos != getItemCount()) {
            nextHolder = (ViewHolder) mRecyclerView.findViewHolderForAdapterPosition(nextHolderPos);
            while (true) {
                assert nextHolder != null;
                if (!(nextHolder.relativeLayout.getVisibility() == View.GONE && nextHolderPos < getItemCount() - 1))
                    break;
                nextHolderPos++;
                nextHolder = (ViewHolder) mRecyclerView.findViewHolderForAdapterPosition(nextHolderPos);
            }
        }
        if (prevHolderPos != -1) {
            prevHolder = (ViewHolder) mRecyclerView.findViewHolderForAdapterPosition(prevHolderPos);
            while (true) {
                assert prevHolder != null;
                if (!(prevHolder.relativeLayout.getVisibility() == View.GONE && prevHolderPos > 0))
                    break;
                prevHolderPos--;
                prevHolder = (ViewHolder) mRecyclerView.findViewHolderForAdapterPosition(prevHolderPos);
            }
        }


        if (nextHolderPos != getItemCount()) {
            MessageObject nextMessage = messageList.get(nextHolderPos);
            MessageObject prevMessage = messageList.get(prevHolderPos);

            if (nextMessage.isInfo() && prevMessage.isInfo()) {
                assert prevHolder != null;
                prevHolder.relativeLayout.setVisibility(View.GONE);
            } else if (nextMessage.isInfo() || nextMessage.getSenderId().equals(userKey) || numberOfUsers == 1) {
                assert nextHolder != null;
                nextHolder.messageSender.setVisibility(View.GONE);
            } else if (prevMessage.isInfo() || !prevMessage.getSenderId().equals(nextMessage.getSenderId())) {
                assert nextHolder != null;
                nextHolder.messageSender.setVisibility(View.VISIBLE);
            } else {
                assert nextHolder != null;
                nextHolder.messageSender.setVisibility(View.GONE);
            }
        }

    }


    @Override
    public int getItemCount() {
        return messageList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }


    public interface OnItemDoubleClickListener {
        void OnItemDoubleClicked(int position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView messageText,
                messageSender,
                messageTime,
                taggedText,
                taggedSender;

        ImageView mediaImage,
                taggedImage;

        RelativeLayout relativeLayout;

        RelativeLayout taggedLayout;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            messageSender = itemView.findViewById(R.id.messageSender);
            messageText = itemView.findViewById(R.id.messageText);
            messageTime = itemView.findViewById(R.id.messageTime);
            mediaImage = itemView.findViewById(R.id.mediaImage);

            taggedText = itemView.findViewById(R.id.messageTaggedText);
            taggedSender = itemView.findViewById(R.id.messageTaggedSender);
            taggedImage = itemView.findViewById(R.id.messageTaggedImage);
            taggedLayout = itemView.findViewById(R.id.messageTaggedLayout);

            relativeLayout = itemView.findViewById(R.id.parentRelativeLayout);
        }
    }
}
