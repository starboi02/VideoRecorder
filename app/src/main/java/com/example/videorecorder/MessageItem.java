package com.example.videorecorder;

public class MessageItem {

    String message,date,time;

    public MessageItem(String message,String date,String time){
        this.message=message;
        this.date=date;
        this.time=time;
    }

    public MessageItem(){}

    public String getDate() {
        return date;
    }

    public String getMessage() {
        return message;
    }

    public String getTime() {
        return time;
    }
}
