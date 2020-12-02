package com.example.videorecorder;

public class User {

    String name,uid;

    public User(){}

    public User(String name, String uid){
        this.name=name;
        this.uid=uid;
    }

    public String getName() {
        return name;
    }

    public String getUid() {
        return uid;
    }
}
