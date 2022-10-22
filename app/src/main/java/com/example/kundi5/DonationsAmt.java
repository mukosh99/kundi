package com.example.kundi5;

public class DonationsAmt {
    private String donationsId,name,donations,uid,groupId;

    public DonationsAmt() {
    }

    @Override
    public String toString() {
        return "DonationsAmt{" +
                "donationsId='" + donationsId + '\'' +
                ", name='" + name + '\'' +
                ", donations='" + donations + '\'' +
                ", uid='" + uid + '\'' +
                ", groupId='" + groupId + '\'' +
                '}';
    }

    public DonationsAmt(String donationsId, String name, String donations, String uid, String groupId) {
        this.donationsId = donationsId;
        this.name = name;
        this.donations = donations;
        this.uid = uid;
        this.groupId = groupId;
    }

    public String getDonationsId() {
        return donationsId;
    }

    public void setDonationsId(String donationsId) {
        this.donationsId = donationsId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDonations() {
        return donations;
    }

    public void setDonations(String donations) {
        this.donations = donations;
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
