package com.example.pujamandal;

import com.google.firebase.database.PropertyName;

public class chatuser {
    private String uid;
    private String name;
    private String phone;
    private String panditUid;

    public chatuser() {}

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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPanditUid() {
        return panditUid;
    }

    public void setPanditUid(String panditUid) {
        this.panditUid = panditUid;
    }
}

