package com.example.kundi5;

public class TargetAmt {
    private String targetAmt,uid;

    public TargetAmt() {
    }

    public TargetAmt(String targetAmt, String uid) {
        this.targetAmt = targetAmt;
        this.uid = uid;
    }

    public String getTargetAmt() {
        return targetAmt;
    }

    public void setTargetAmt(String targetAmt) {
        this.targetAmt = targetAmt;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
