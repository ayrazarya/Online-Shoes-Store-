package com.controller;

import com.dao.OrderItemsDAO;
import com.model.pojo.OrderItems;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.List;
import javax.inject.Inject;

@Named(value = "adminBean")
@SessionScoped
public class AdminBean implements Serializable {

    @Inject
    private OrderItemsDAO orderItemsDAO;

    private List<OrderItems> orderItemsList;
    
    private List<OrderItems> orderItemsListHistory;

    @PostConstruct
    public void init() {
        // Automatically fetch all paid order items when the bean is initialized
        fetchAllPaidOrderItems();
        fetchAllPaidOrderItemsHistory() ;
    }

    // Method to fetch all paid order items
    public void fetchAllPaidOrderItems() {
        orderItemsList = orderItemsDAO.getAllPaidOrderItems();
    }

    
        public void fetchAllPaidOrderItemsHistory() {
       orderItemsListHistory = orderItemsDAO.getAllPaidOrderItemsHistory();
    }
    
    
    // New method to update order status to 'Dikirim' (Shipped)
    public void markAsShipped(int orderId) {
        orderItemsDAO.updateOrderStatusToShipped(orderId);
        // Reload the order items to reflect changes
        fetchAllPaidOrderItems();
    }

    public List<OrderItems> getOrderItemsList() {
        return orderItemsList;
    }

    public void setOrderItemsList(List<OrderItems> orderItemsList) {
        this.orderItemsList = orderItemsList;
    }

    public List<OrderItems> getOrderItemsListHistory() {
        return orderItemsListHistory;
    }

    public void setOrderItemsListHistory(List<OrderItems> orderItemsListHistory) {
        this.orderItemsListHistory = orderItemsListHistory;
    }
    
    
    
}
