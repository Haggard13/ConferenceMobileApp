package com.example.conference.json;

public class UserInfo {
    public int id;
    public String email;
    public String name;
    public String surname;

    public UserInfo(int id, String email, String name, String surname) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.surname = surname;
    }
}