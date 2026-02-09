package com.shadsluiter.ordersapp.models;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

 
/**
 * OrderModel class representing an order.
 * Used for application logic and data transfer.
 * Contains fields for id, date, customerid, and notes.
 * In contrast, OrderEntity is used for data persistence and retrieval.
 * Use Converters class to convert between OrderModel and OrderEntity.
 */

public class OrderModel {

    private String id;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date date;
    private String customerid;
    private String notes;
    private String pictureUrl;

    public OrderModel() {
    }

    public OrderModel(String id, Date date, String customerid, String notes, String pictureUrl) {
        this.id = id;
        this.date = date;
        this.customerid = customerid;
        this.notes = notes;
        this.pictureUrl = pictureUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date2) {
        this.date = date2;
    }

    public String getCustomerid() {
        return customerid;
    }

    public void setCustomerid(String id) {
        this.customerid = id;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }
    
}
