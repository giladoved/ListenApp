package com.gilad.oved.telme;

import java.util.ArrayList;

import android.graphics.Bitmap;

public class Group {
	
	private String Name;
	private String Number;
	private Bitmap Picture;
    private ArrayList<Child> Items;

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        this.Name = name;
    }
    
    public String getNumber() {
        return Number;
    }

    public void setNumber(String number) {
        this.Number = number;
    }
    
    public Bitmap getPicture() {
    	return this.Picture;
    }
    
    public void setPicture(Bitmap bmp) {
    	this.Picture = bmp;
    }

    public ArrayList<Child> getItems() {
        return Items;
    }

    public void setItems(ArrayList<Child> Items) {
        this.Items = Items;
    }

}