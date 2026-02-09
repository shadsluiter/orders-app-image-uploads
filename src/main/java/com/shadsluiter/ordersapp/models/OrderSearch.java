package com.shadsluiter.ordersapp.models;

/**
 * OrderSearch class representing search criteria for orders.
 * Contains a single field for searchString.
 * Used to encapsulate search parameters in the application.
 * Used in the screens where orders are searched or filtered.
 */
public class OrderSearch {
    private String searchString;

    public OrderSearch() {
        searchString = "";
    }

    public OrderSearch(String searchString) {
        this.searchString = searchString;
    }

    public String getSearchString() {
        return searchString;
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }  
    
}
