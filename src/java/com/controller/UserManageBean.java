package com.controller;

import com.dao.OrderItemsDAO;
import com.model.pojo.OrderItems;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.List;
import javax.annotation.PostConstruct;

@Named(value = "userManageBean")
@SessionScoped
public class UserManageBean implements Serializable {

    @Inject
    private OrderItemsDAO orderItemsDAO;

    private List<OrderItems> userOrderItemsList;

    // Default constructor
    public UserManageBean() {
    }
    
    @PostConstruct
public void init() {
    fetchUserShippedOrders();  // Automatically fetch shipped orders for the user
}


    // Method to retrieve userId from session
    private int getUserIdFromSession() {
        Object userIdObj = FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("userId");
        if (userIdObj != null) {
            return (Integer) userIdObj; // assuming userId is an integer in the session
        } else {
            throw new RuntimeException("User is not logged in.");
        }
    }

    // Method to fetch the user's shipped orders (status: 'Dikirim')
    public void fetchUserShippedOrders() {
        int userId = getUserIdFromSession(); // Retrieve the user ID from the session
        userOrderItemsList = orderItemsDAO.getUserOrderItems(userId); // Fetch orders with status 'Dikirim'
    }

    // Method to update order status to 'Diterima' (Received)
    public void markAsReceived(int orderId) {
        orderItemsDAO.updateOrderStatusToReceived(orderId);
        // Reload the user's order items to reflect changes
        fetchUserShippedOrders();
    }

    // Getters and Setters
    public List<OrderItems> getUserOrderItemsList() {
        return userOrderItemsList;
    }

    public void setUserOrderItemsList(List<OrderItems> userOrderItemsList) {
        this.userOrderItemsList = userOrderItemsList;
    }
}
