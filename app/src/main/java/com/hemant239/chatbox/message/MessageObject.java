package com.hemant239.chatbox.message;

import java.util.Objects;

public class MessageObject {


    String messageId,
            text,
            imageUri,
            senderId,
            senderPhone,
            senderName,
            time,
            date,
            taggedId,
            taggedSender,
            taggedText,
            taggedImageUri;

    long timeStamp;

    boolean isInfo,
            isDeletedForEveryone,
            isTagged;


    public MessageObject(String messageId) {
        this.messageId = messageId;
    }

    public MessageObject() {
        messageId = null;
        text = null;
        senderId = null;
        senderName = null;
        imageUri = null;
        time = null;
        date = null;
    }

    public MessageObject(String messageId, String text, String imageUri, String senderId, String senderPhone, String senderName, String time, String date, long timeStamp, boolean isInfo, boolean isDeletedForEveryone) {
        this.messageId = messageId;
        this.text = text;
        this.imageUri = imageUri;
        this.senderId = senderId;
        this.senderPhone = senderPhone;
        this.senderName = senderName;
        this.time = time;
        this.date = date;
        this.timeStamp = timeStamp;
        this.isInfo = isInfo;
        this.isDeletedForEveryone = isDeletedForEveryone;
    }

    public MessageObject(String messageId, String text, String imageUri, String senderId, String senderPhone, String senderName, String time, String date, long timeStamp, boolean isInfo, boolean isDeletedForEveryone, boolean isTagged, String taggedId, String taggedSender, String taggedText, String taggedImageUri) {
        this.messageId = messageId;
        this.text = text;
        this.imageUri = imageUri;
        this.senderId = senderId;
        this.senderPhone = senderPhone;
        this.senderName = senderName;
        this.time = time;
        this.date = date;
        this.timeStamp = timeStamp;
        this.isInfo = isInfo;
        this.isDeletedForEveryone = isDeletedForEveryone;

        this.isTagged = isTagged;
        this.taggedId = taggedId;
        this.taggedSender = taggedSender;
        this.taggedText = taggedText;
        this.taggedImageUri = taggedImageUri;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getImageUri() {
        return imageUri;
    }

    public String getTime() {
        return time;
    }

    public String getText() {
        return text;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getSenderPhone() {
        return senderPhone;
    }

    public String getDate() {
        return date;
    }

    public String getMessageId() {
        return messageId;
    }

    public boolean isInfo() {
        return isInfo;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public boolean isDeletedForEveryone() {
        return isDeletedForEveryone;
    }

    public void setDeletedForEveryone(boolean deletedForEveryone) {
        isDeletedForEveryone = deletedForEveryone;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public String getTaggedId() {
        return taggedId;
    }

    public String getTaggedSender() {
        return taggedSender;
    }

    public String getTaggedText() {
        return taggedText;
    }

    public String getTaggedImageUri() {
        return taggedImageUri;
    }

    public boolean isTagged() {
        return isTagged;
    }

    public void setTagged(boolean tagged) {
        isTagged = tagged;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MessageObject)) return false;
        MessageObject that = (MessageObject) o;
        return messageId.equals(that.messageId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageId);
    }

}
