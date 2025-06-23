package com.example.pujamandal;

public class PanditModel {
    private String name, city, experience, phone,imageBase64,id;
    private double avgRating;        // ⭐ Rating now double
    private long totalRatings;       // 👥 Total rating count

    public PanditModel() {}

    public PanditModel(String name, String city, String experience, double avgRating, long totalRatings, String imageBase64, String phone) {
        this.name = name;
        this.city = city;
        this.experience = experience;
        this.avgRating = avgRating;
        this.totalRatings = totalRatings;
        this.imageBase64 = imageBase64;
        this.phone = phone;
        this.id = id;
    }


    // Getters
    public String getName() { return name; }
    public String getCity() { return city; }
    public String getExperience() { return experience; }
    public double getAvgRating() { return avgRating; }
    public long getTotalRatings() { return totalRatings; }
    public String getImageUrl() {
        return imageBase64;
    }
    public String getPhone() { return phone; }
    public String getId() { return id; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setCity(String city) { this.city = city; }
    public void setExperience(String experience) { this.experience = experience; }
    public void setAvgRating(double avgRating) { this.avgRating = avgRating; }
    public void setTotalRatings(long totalRatings) { this.totalRatings = totalRatings; }
    public void setImageUrl(String imageBase64) { this.imageBase64 = imageBase64; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setId(String id) { this.id = id; }
}
