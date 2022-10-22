package com.example.kundi5;

public class Messages
{
    private String sender,messagesId,groupId,type,name,message,image,date,time,name2;

    public Messages () {
    }

    public Messages(String sender, String messagesId, String groupId, String type, String name, String message, String image, String date, String time, String name2) {
        this.sender = sender;
        this.messagesId = messagesId;
        this.groupId = groupId;
        this.type = type;
        this.name = name;
        this.message = message;
        this.image = image;
        this.date = date;
        this.time = time;
        this.name2 = name2;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getMessagesId() {
        return messagesId;
    }

    public void setMessagesId(String messagesId) {
        this.messagesId = messagesId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getName2() {
        return name2;
    }

    public void setName2(String name2) {
        this.name2 = name2;
    }
}
