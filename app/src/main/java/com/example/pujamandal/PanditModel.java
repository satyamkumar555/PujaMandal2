package com.example.pujamandal;

public class PanditModel {
    private String name, city, experience, rating, imageUrl, phone;

    // Empty Constructor (Firebase ke liye zaroori)
    public PanditModel() {}

    public PanditModel(String name, String city, String experience, String rating, String imageUrl, String Phone) {
        this.name = name;
        this.city = city;
        this.experience = experience;
        this.rating = rating;
        this.imageUrl = imageUrl;
        this.phone = Phone;
    }

    // Getters
    public String getName() { return name; }
    public String getCity() { return city; }
    public String getExperience() { return experience; }
    public String getRating() { return rating; }
    public String getImageUrl() { return imageUrl; }

    public String getPhone(){return phone; }

    public void setphone(String phone) { this.phone = phone; }
}
