package com.hemant239.chatbox.chat;

import java.io.Serializable;
import java.util.Objects;

public class ChatObject implements Serializable {
    private final String uid;
    private final String lastSenderId;
    private String name;
    private String imageUri;
    private String lastMessageText;
    private String lastMessageSender;
    private String lastMessageTime;
    private String lastMessageId;

    private int numberOfUsers;

    private boolean isSingleChat;


    public ChatObject(String uid) {
        this.uid = uid;
        this.lastSenderId = "";
    }

    public ChatObject(String uid, String name, String imageUri, int numberOfUsers, boolean isSingleChat) {
        this.uid = uid;
        this.name = name;
        this.imageUri = imageUri;
        this.numberOfUsers = numberOfUsers;
        this.isSingleChat = isSingleChat;
        this.lastSenderId = "";
    }

    public ChatObject(String uid, String name, String imageUri, String lastMessageId,int numberOfUsers, boolean isSingleChat) {
        this.uid=uid;
        this.name=name;
        this.imageUri=imageUri;
        this.lastMessageId=lastMessageId;
        this.numberOfUsers=numberOfUsers;
        this.isSingleChat = isSingleChat;
        this.lastSenderId = "";
    }

    public ChatObject(String uid, String name, String imageUri, String lastMessageText, String lastMessageSender, String lastSenderId, String lastMessageTime, int numberOfUsers, String lastMessageId, boolean isSingleChat) {
        this.uid = uid;
        this.name = name;
        this.imageUri = imageUri;
        this.lastMessageText = lastMessageText;
        this.lastMessageSender = lastMessageSender;
        this.lastSenderId = lastSenderId;
        this.lastMessageTime = lastMessageTime;
        this.numberOfUsers = numberOfUsers;
        this.lastMessageId = lastMessageId;
        this.isSingleChat = isSingleChat;
    }

    public String getName() {
        return name;
    }

    public String getUid() {
        return uid;
    }

    public String getImageUri() {
        return imageUri;
    }


    public String getLastMessageText() {
        return lastMessageText;
    }

    public String getLastMessageId() {
        return lastMessageId;
    }

    public String getLastMessageSender() {
        return lastMessageSender;
    }

    public String getLastSenderId() {
        return lastSenderId;
    }

    public String getLastMessageTime() {
        return lastMessageTime;
    }

    public int getNumberOfUsers() {
        return numberOfUsers;
    }

    public boolean isSingleChat() {
        return isSingleChat;
    }

    public void setLastMessageText(String lastMessageText) {
        this.lastMessageText = lastMessageText;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatObject that = (ChatObject) o;
        return uid.equals(that.uid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uid);
    }

}
