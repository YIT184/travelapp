package edu.travels.travelapp.model.dto;

public class UserLoginDTO {
    public String phone;
    public String password;

    public UserLoginDTO(String phone, String password) {
        this.phone = phone;
        this.password = password;
    }
}