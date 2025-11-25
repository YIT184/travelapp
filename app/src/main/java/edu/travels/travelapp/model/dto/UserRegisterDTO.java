package edu.travels.travelapp.model.dto;

public class UserRegisterDTO {
    public String phone;
    public String password;
    public String nickname;

    public UserRegisterDTO(String phone, String password, String nickname) {
        this.phone = phone;
        this.password = password;
        this.nickname = nickname;
    }
}