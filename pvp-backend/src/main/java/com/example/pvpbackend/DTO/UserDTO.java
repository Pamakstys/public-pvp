package com.example.pvpbackend.DTO;

import com.example.pvpbackend.models.User;

public class UserDTO {
    private User user;
    private String type;

    public User getUser(){return user;}
    public void setUser(User user){this.user = user;}
    public String getType(){return type;}
    public void setType(String type){this.type = type;}
}

