package com.example.arcard;

public class User {

    public String name;
    public String role;
    public String phone;
    public String email;
    public String company;
    public String website;
    public String imageUrl;

    // Required for Firebase
    public User() {
    }

    public User(String name, String role, String phone,
                String email, String company,
                String website, String imageUrl) {

        this.name = name;
        this.role = role;
        this.phone = phone;
        this.email = email;
        this.company = company;
        this.website = website;
        this.imageUrl = imageUrl;
    }
}