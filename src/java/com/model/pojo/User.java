package com.model.pojo;
// Generated Sep 20, 2024 2:26:10 PM by Hibernate Tools 4.3.1


import java.util.HashSet;
import java.util.Set;

/**
 * User generated by hbm2java
 */
public class User  implements java.io.Serializable {


     private Integer userId;
     private String username;
     private String email;
     private String password;
     private String firstName;
     private String lastName;
     private String address;
     private String paymentMethod;
     private String status;
     private Set orderses = new HashSet(0);
     private Set otpcodes = new HashSet(0);

    public User() {
    }

	
    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }
    public User(String username, String email, String password, String firstName, String lastName, String address, String paymentMethod, String status, Set orderses, Set otpcodes) {
       this.username = username;
       this.email = email;
       this.password = password;
       this.firstName = firstName;
       this.lastName = lastName;
       this.address = address;
       this.paymentMethod = paymentMethod;
       this.status = status;
       this.orderses = orderses;
       this.otpcodes = otpcodes;
    }
   
    public Integer getUserId() {
        return this.userId;
    }
    
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    public String getUsername() {
        return this.username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    public String getEmail() {
        return this.email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    public String getPassword() {
        return this.password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    public String getFirstName() {
        return this.firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public String getLastName() {
        return this.lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public String getAddress() {
        return this.address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    public String getPaymentMethod() {
        return this.paymentMethod;
    }
    
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    public String getStatus() {
        return this.status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    public Set getOrderses() {
        return this.orderses;
    }
    
    public void setOrderses(Set orderses) {
        this.orderses = orderses;
    }
    public Set getOtpcodes() {
        return this.otpcodes;
    }
    
    public void setOtpcodes(Set otpcodes) {
        this.otpcodes = otpcodes;
    }




}

