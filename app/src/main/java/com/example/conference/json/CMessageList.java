package com.example.conference.json;

import com.example.conference.db.entity.CMessageEntity;

import java.util.List;

public class CMessageList {
    public List<CMessageEntity> list;

    public CMessageList() {
    }

    public CMessageList(List<CMessageEntity> messages) {
        this.list = messages;
    }
}
