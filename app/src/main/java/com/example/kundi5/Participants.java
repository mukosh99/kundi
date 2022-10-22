package com.example.kundi5;

public class Participants
{
    private String uid, name, status, image;

    public Participants() {
    }

    public Participants(String uid, String name, String status, String image) {
        this.uid = uid;
        this.name = name;
        this.status = status;
        this.image = image;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
