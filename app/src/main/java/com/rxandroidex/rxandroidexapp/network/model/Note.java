package com.rxandroidex.rxandroidexapp.network.model;

public class Note extends BaseResponse {
    Integer id;
    String note;
    String timestamp;

    public Integer getId() {
        return id;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
