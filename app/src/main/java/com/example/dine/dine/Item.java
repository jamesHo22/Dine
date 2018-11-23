package com.example.dine.dine;

public class Item {
    private String title;
    private String description;
    private String imageUri;
    private int price;

    public Item(){
        // Empty Constructor needed
    }

    public Item(String title, String description, String imageUri, int price) {
        this.title = title;
        this.description = description;
        this.imageUri = imageUri;
        this.price = price;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUri() {
        return imageUri;
    }

    public int getPrice() {
        return price;
    }
}
