package com.controller;

import com.dao.CheckoutDAO;
import com.dao.UserDAO;
import com.model.pojo.OrderItems;
import com.model.pojo.User;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;
@ManagedBean(name = "checkoutBean")
@SessionScoped
public class CheckoutBean implements Serializable {

    @ManagedProperty(value = "#{cartBean}")
    private CartBean cartBean;

    private List<OrderItems> orderItemsList;
    private BigDecimal totalAmount;
    private CheckoutDAO checkoutDAO;

    @PostConstruct
    public void init() {
        checkoutDAO = new CheckoutDAO();
    }

    // Tambahkan metode setter ini untuk memastikan injeksi dilakukan
    public void setCartBean(CartBean cartBean) {
        this.cartBean = cartBean;
    }
   public String prepareCheckout() {
        if (cartBean == null) {
            cartBean = (CartBean) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("cartBean");
        }

        if (cartBean != null) {
            orderItemsList = cartBean.getSelectedOrderItems();
            totalAmount = calculateTotal();
        } else {
            System.out.println("CartBean is null, no items to checkout.");
        }

        return "checkout.xhtml?faces-redirect=true";
    }
    
    
    public void clearCart() {
   orderItemsList .clear(); // Kosongkan item dari keranjang
}

   public String completeOrder() {
    try {
        // Validasi jika order items dan user sudah ada
        if (orderItemsList == null || orderItemsList.isEmpty()) {
            System.out.println("No items to checkout.");
            return null;
        }

        // Mengambil userId dari sesi
        Integer userId = (Integer) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("userId");
        if (userId == null) {
            System.out.println("User ID not found in session.");
            return null;
        }

        // Ambil objek User dari database berdasarkan userId
        UserDAO userDAO = new UserDAO();
        User user = userDAO.findById(userId);
        if (user == null) {
            System.out.println("User not found in database.");
            return null;
        }

          FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("firstName", user.getFirstName());

        // Update status order ke "Waiting to Paid" dan kurangi quantity produk
        checkoutDAO.updateOrderStatusAndReduceStock(user, orderItemsList, totalAmount);

        System.out.println("Order completed for user: " + user.getUsername());
      
        return "sukses.xhtml?faces-redirect=true";  // Navigasi ke halaman sukses
    } catch (Exception e) {
        e.printStackTrace();
        return "adidas.xhtml?faces-redirect=true";  // Navigasi ke halaman error
    }
}

    // Metode untuk menghitung total berdasarkan barang yang dipilih
    public BigDecimal calculateTotal() {
        if (orderItemsList == null || orderItemsList.isEmpty()) {
            return BigDecimal.ZERO;
        }

        totalAmount = orderItemsList.stream()
            .map(item -> item.getPrice().multiply(new BigDecimal(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalAmount;
    }

    // Getter dan Setter untuk CartBean dan property lain
    public CartBean getCartBean() {
        return cartBean;
    }

    public List<OrderItems> getOrderItemsList() {
        return orderItemsList;
    }

    public void setOrderItemsList(List<OrderItems> orderItemsList) {
        this.orderItemsList = orderItemsList;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
}
