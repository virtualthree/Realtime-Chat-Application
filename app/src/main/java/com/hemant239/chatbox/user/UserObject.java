package com.hemant239.chatbox.user;

import java.io.Serializable;

public class UserObject implements Serializable {

    private final String uid;
    private final String phoneNumber;
    private String name;
    private String status;
    private String profileImageUri;
    private String chatID;
    private String notificationKey;

    private boolean isSelected = false;


    public UserObject() {
        this.uid = "";
        this.name = "";
        this.phoneNumber = "";
        this.status = "";
        this.profileImageUri = "";
        this.chatID = "";

    }

    public UserObject(String uid, String name, String phoneNumber, String chatID) {
        this.uid = uid;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.chatID = chatID;
    }

    public UserObject(String uid, String name, String phoneNumber, String status, String notificationKey) {
        this.uid = uid;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.status = status;
        this.profileImageUri = "";
        this.notificationKey = notificationKey;
    }

    public UserObject(String uid, String name, String phoneNumber, String status, String profileImageUri, String chatID) {
        this.uid = uid;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.status = status;
        this.profileImageUri = profileImageUri;
        this.chatID = chatID;
    }

    public UserObject(String uid, String name, String phoneNumber, String status, String profileImageUri, String chatID, String notificationKey) {
        this.uid = uid;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.status = status;
        this.profileImageUri = profileImageUri;
        this.chatID = chatID;
        this.notificationKey = notificationKey;
    }

    public String getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getStatus() {
        return status;
    }

    public String getProfileImageUri() {
        return profileImageUri;
    }

    public String getChatID() {
        return chatID;
    }

    public String getNotificationKey() {
        return notificationKey;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setProfileImageUri(String profileImageUri) {
        this.profileImageUri = profileImageUri;
    }

    public boolean isSelected() {
        return isSelected;
    }


    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
