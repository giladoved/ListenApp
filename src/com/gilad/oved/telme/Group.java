package com.gilad.oved.telme;

import java.util.ArrayList;

import android.graphics.Bitmap;

public class Group {
	
	private String name;
	private String number;
	private Bitmap picture;
    private ArrayList<Child> items;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
    
    public Bitmap getPicture() {
    	return this.picture;
    }
    
    public void setPicture(Bitmap bmp) {
    	this.picture = bmp;
    }

    public ArrayList<Child> getItems() {
        return items;
    }

    public void setItems(ArrayList<Child> Items) {
        this.items = Items;
    }

}