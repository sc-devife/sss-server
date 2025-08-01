package com.sss.app.dto;

public class UserRegistrationDto {
    private String name;
    private String password;

    public UserRegistrationDto() {
    }

    public UserRegistrationDto(String name, String password) {
        this.name = name;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
