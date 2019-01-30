package com.example.dine.dine;

public class Item {
    private String title;
    private String description;
    private String imageUri;
    private int price;
    private boolean promo;
    private boolean menu;

    public Item(){
        // Empty Constructor needed
    }

    public Item(String title, String description, String imageUri, int price, boolean promo, boolean menu) {
        this.title = title;
        this.description = description;
        this.imageUri = imageUri;
        this.price = price;
        this.promo = promo;
        this.menu = menu;
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

    public boolean isPromo() {
        return promo;
    }

    public boolean isMenu() { return menu; }
}
