package com.example.kundi5;

public class Pledges {
    private String name,pledge,image,uid,groupId;

    public Pledges() {
    }

    public Pledges(String name, String pledge, String image, String uid, String groupId) {
        this.name = name;
        this.pledge = pledge;
        this.image = image;
        this.uid = uid;
        this.groupId = groupId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPledge() {
        return pledge;
    }

    public void setPledge(String pledge) {
        this.pledge = pledge;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
}

