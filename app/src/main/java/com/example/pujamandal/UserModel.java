package com.example.pujamandal;

public class UserModel {
    private String name, email, phone, city;

    public UserModel() {} // Needed for Firestore

    public UserModel(String name, String email, String phone, String city) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.city = city;
    }

    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getCity() { return city; }
}
