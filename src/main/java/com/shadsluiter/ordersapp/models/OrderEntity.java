package com.shadsluiter.ordersapp.models;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

 
/**
 * Entity class representing an order.
 * Maps to the orders table in the database.
 * Contains fields for id, date, customerid, and notes.
 * Used for data persistence and retrieval.
 * On contrast, OrderModel is used for application logic and data transfer.
 */

public class OrderEntity {

     
    private Long id;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date date;
    private String customerid;
    private String notes;
    private String pictureUrl;

    public OrderEntity() {
    }

    public OrderEntity(Long id, Date date, String customerid, String notes, String pictureUrl) {
        this.id = id;
        this.date = date;
        this.customerid = customerid;
        this.notes = notes;
        this.pictureUrl = pictureUrl;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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
