package com.model.pojo;

import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import javax.persistence.FetchType;

@Entity
@Table(name = "order_items")
public class OrderItems implements java.io.Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    
    private int orderItemId;

    @ManyToOne(fetch = FetchType.LAZY)  // Use LAZY fetching for better performance
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Orders orders;

        @Column(name = "quantity")
    private int quantity;
    private BigDecimal price;

   
    private BigDecimal total; // Total harga
    

    public OrderItems() {
    }

    public OrderItems(Item item, Orders orders, int quantity, BigDecimal price) {
        this.item = item;
        this.orders = orders;
        this.quantity = quantity;
        this.price = price;

    }

    // Getters and Setters

    public int getOrderItemId() {
        return this.orderItemId;
    }

    public void setOrderItemId(int orderItemId) {
        this.orderItemId = orderItemId;
    }

    public Item getItem() {
        return this.item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public Orders getOrders() {
        return this.orders;
    }

    public void setOrders(Orders orders) {
        this.orders = orders;
    }

    public int getQuantity() {
        return this.quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return this.price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }


    
      public BigDecimal getTotal() {
        return price.multiply(new BigDecimal(quantity)); // Menghitung total harga
    }
    }
    
