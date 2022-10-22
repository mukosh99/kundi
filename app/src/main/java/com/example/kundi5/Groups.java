package com.example.kundi5;

public class Groups
{

    public String groupId,date,time,group,description,groupImage,name,groupIdImage;

    public Groups() {
    }

    public Groups(String groupId, String date, String time, String group, String description, String groupImage, String name, String groupIdImage) {
        this.groupId = groupId;
        this.date = date;
        this.time = time;
        this.group = group;
        this.description = description;
        this.groupImage = groupImage;
        this.name = name;
        this.groupIdImage = groupIdImage;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
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

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGroupImage() {
        return groupImage;
    }

    public void setGroupImage(String groupImage) {
        this.groupImage = groupImage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroupIdImage() {
        return groupIdImage;
    }

    public void setGroupIdImage(String groupIdImage) {
        this.groupIdImage = groupIdImage;
    }
}
