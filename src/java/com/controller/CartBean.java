package com.controller;

import com.dao.CartDAO;
import com.dao.UserDAO;
import com.dao.viewProductDAO;
import com.model.pojo.Item;
import com.model.pojo.Orders;
import com.model.pojo.User;
import com.model.pojo.OrderItems;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

@ManagedBean(name = "cartBean")
@SessionScoped
public class CartBean implements Serializable {

    private int itemId;
    private Item selectedProduct; 
    private viewProductDAO productDAO;
    private Orders currentOrder; 
    private List<Item> cartItems;
    private Map<Integer, Boolean> selectedItems = new HashMap<>();  // Tracks selected items

    private List<OrderItems> orderItemsList; 
    private BigDecimal selectedTotal = BigDecimal.ZERO;
    private int totalSelectedItems;
    private boolean allSelected;

    private CartDAO cartDAO;

    public CartBean() {
        cartItems = new ArrayList<>(); 
        orderItemsList = new ArrayList<>(); 
        cartDAO = new CartDAO(); 
    }

    @PostConstruct
    public void init() {
        loadCartItems(); 
        updateSelectedItems() ;
    }
    
    
    public String proceedToCheckout(CheckoutBean checkoutBean) {
        List<OrderItems> selectedOrderItems = orderItemsList.stream()
                .filter(orderItem -> selectedItems.get(orderItem.getOrderItemId()) != null && selectedItems.get(orderItem.getOrderItemId()))
                .collect(Collectors.toList());

        checkoutBean.setOrderItemsList(selectedOrderItems);

        return "checkout.xhtml?faces-redirect=true";
    }
    
      public List<OrderItems> getSelectedOrderItems() {
        List<OrderItems> selectedOrderItems = new ArrayList<>();
        for (OrderItems orderItem : orderItemsList) {
            Boolean isSelected = selectedItems.get(orderItem.getItem().getItemId());
            if (Boolean.TRUE.equals(isSelected)) {
                selectedOrderItems.add(orderItem);
            }
        }
        return selectedOrderItems;
    }
    
    

    public void updateSelectedItems() {
    selectedTotal = BigDecimal.ZERO; // Reset total
    totalSelectedItems = 0; // Reset jumlah item yang dipilih

    for (OrderItems orderItem : orderItemsList) {
        Boolean isSelected = selectedItems.getOrDefault(orderItem.getItem().getItemId(), false); // Check if the item is selected
        if (Boolean.TRUE.equals(isSelected)) { // Jika item dicentang
            totalSelectedItems += orderItem.getQuantity(); // Tambahkan jumlah item ke total
            selectedTotal = selectedTotal.add(orderItem.getPrice().multiply(new BigDecimal(orderItem.getQuantity()))); // Hitung harga total
        }
    }

    System.out.println("Selected items updated: " + totalSelectedItems + " items, Total: " + selectedTotal);
}

    public boolean isAllSelected() {
        return allSelected;
    }

    public void setAllSelected(boolean allSelected) {
        this.allSelected = allSelected;
    }
    
    
    public void selectAllItems() {
    // Jika checkbox "Select All" dicentang, pilih semua item, sebaliknya batalkan semua pilihan
    for (OrderItems orderItem : orderItemsList) {
        selectedItems.put(orderItem.getItem().getItemId(), allSelected); // Set all items to selected/deselected
    }

    // Perbarui total berdasarkan item yang dipilih
    updateSelectedItems();
}


    public List<OrderItems> getOrderItemsList() {
    return orderItemsList; // Pastikan ini mengembalikan daftar item yang benar
}

public BigDecimal calculateTotal() {
    BigDecimal total = BigDecimal.ZERO;
    if (orderItemsList != null && !orderItemsList.isEmpty()) {
        for (OrderItems orderItem : orderItemsList) {
            Boolean isSelected = selectedItems.getOrDefault(orderItem.getItem().getItemId(), false); // Hanya hitung jika item dicentang
            if (Boolean.TRUE.equals(isSelected)) {
                total = total.add(orderItem.getPrice().multiply(new BigDecimal(orderItem.getQuantity())));
            }
        }
    }
    return total.setScale(2, RoundingMode.HALF_UP);
}



    // Load cart items for the current user session
public void loadCartItems() {
    Integer userId = (Integer) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("userId");
    if (userId != null) {
        // Ambil OrderItems bukan hanya Item
        orderItemsList = cartDAO.getCartItemsForUser(userId);

        // Kosongkan daftar item keranjang
        cartItems.clear();

        // Loop melalui OrderItems dan tambahkan Item ke cartItems
        for (OrderItems orderItem : orderItemsList) {
            cartItems.add(orderItem.getItem());
            selectedItems.putIfAbsent(orderItem.getItem().getItemId(), false);
            System.out.println("Item ID: " + orderItem.getItem().getItemId() + ", Price: " + orderItem.getPrice() + ", Quantity: " + orderItem.getQuantity());
        }

        currentOrder = cartDAO.findCurrentOrderForUser(userId);
        updateSelectedItems();
    }
    
    
}

    // Update order total for the selected items
    public void updateOrderTotal(Orders order) {
        BigDecimal total = BigDecimal.ZERO;

        for (OrderItems item : order.getOrderItemses()) {
            BigDecimal itemPrice = item.getPrice();
            int itemQuantity = item.getQuantity();
            total = total.add(itemPrice.multiply(new BigDecimal(itemQuantity)));
        }

        order.setTotal(total);
        cartDAO.updateOrderInDatabase(order);
    }



    // Add item to the cart
    // Add item to the cart with stock validation
    public void addItemToCart(Item product) {
        if (product == null) {
            System.out.println("Product is null. Cannot add to cart.");
            return;
        }

        Integer userId = (Integer) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("userId");
        System.out.println("Adding product to cart for user ID: " + userId);

        if (userId == null) {
            System.out.println("User is not logged in. Cannot add to cart.");
            return;
        }

        UserDAO userDAO = new UserDAO();
        User user = userDAO.findById(userId);
        if (user == null) {
            System.out.println("User not found. Cannot add to cart.");
            return;
        }

        // Fetch or create a new order
        currentOrder = cartDAO.findCurrentOrderForUser(userId);
        if (currentOrder == null) {
            currentOrder = createNewOrder(user);
        }

        // Check if the item is already in the cart and get the current quantity
        OrderItems existingOrderItem = cartDAO.findOrderItem(currentOrder.getOrderId(), product.getItemId());

        int currentQuantityInCart = (existingOrderItem != null) ? existingOrderItem.getQuantity() : 0;

        // Check available stock
        if (currentQuantityInCart + 1 > product.getStocks()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, 
                "Stock Limit Exceeded", "The quantity exceeds the available stock for this product."));
            return; // Do not add the item if stock is insufficient
        }

        // Proceed with adding the item to the cart
        cartDAO.addItemToCart(currentOrder, product);
        loadCartItems(); // Reload the updated cart items
    }


    // Create a new order for the user
    public Orders createNewOrder(User user) {
        Orders newOrder = new Orders();
        newOrder.setUser(user);
        newOrder.setOrderDate(new Date());
        newOrder.setStatus("Cart");
        newOrder.setTotal(BigDecimal.ZERO);

        cartDAO.addProductToCart(newOrder);
        System.out.println("Created new order for user ID: " + user.getUserId());
        return newOrder;
    }

public void increaseItemQuantity(int itemId) {
    System.out.println("Increasing quantity for item ID: " + itemId);

    // Fetch the item from the order
    OrderItems orderItem = orderItemsList.stream()
        .filter(item -> item.getItem().getItemId() == itemId)
        .findFirst()
        .orElse(null);

    if (orderItem != null) {
        // Check the stock availability
        int currentStock = orderItem.getItem().getStocks();  // Assuming Item has a stock field
        int currentQuantity = orderItem.getQuantity();

        // If the current quantity + 1 exceeds the available stock, notify the user
        if (currentQuantity + 1 > currentStock) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_WARN, 
                "Stock Limit Exceeded", 
                "The quantity exceeds the available stock for this product."));
            return;  // Do not increase the quantity
        }
        
        // Proceed with increasing the quantity if stock is sufficient
        cartDAO.updateItemQuantity(itemId, 1); 
        loadCartItems(); // Reload the updated cart items from the database
        updateSelectedItems(); // Recalculate totals and selected items
    } else {
        System.out.println("Item not found in the cart.");
    }
}


public void decreaseItemQuantity(int itemId) {
    System.out.println("Decreasing quantity for item ID: " + itemId);
    cartDAO.updateItemQuantity(itemId, -1); 
    loadCartItems(); // Reload the updated cart items from the database
    updateSelectedItems(); // Recalculate totals and selected items
}

    // Remove an item from the cart
    public void removeItem(int itemId) {
        System.out.println("Removing item ID: " + itemId);
        cartDAO.removeItemFromCart(itemId);
        loadCartItems(); 
    }

    // Clear all items from the cart
    public void clearCart() {
        System.out.println("Clearing cart for order ID: " + currentOrder.getOrderId());
        cartDAO.clearCart(currentOrder.getOrderId());
        loadCartItems(); 
    }

    // Calculate the total price of all items in the cart
   
    // Get total number of items in the cart
    public int getTotalItems() {
        return cartItems.size();
    }

    // Getters and setters for other properties

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public Item getSelectedProduct() {
        return selectedProduct;
    }

    public void setSelectedProduct(Item selectedProduct) {
        this.selectedProduct = selectedProduct;
    }

    public viewProductDAO getProductDAO() {
        return productDAO;
    }

    public void setProductDAO(viewProductDAO productDAO) {
        this.productDAO = productDAO;
    }

    public Orders getCurrentOrder() {
        return currentOrder;
    }

    public void setCurrentOrder(Orders currentOrder) {
        this.currentOrder = currentOrder;
    }

    public List<Item> getCartItems() {
        return cartItems;
    }

    public void setCartItems(List<Item> cartItems) {
        this.cartItems = cartItems;
    }

    public Map<Integer, Boolean> getSelectedItems() {
        return selectedItems;
    }

    public void setSelectedItems(Map<Integer, Boolean> selectedItems) {
        this.selectedItems = selectedItems;
    }


    public void setOrderItemsList(List<OrderItems> orderItemsList) {
        this.orderItemsList = orderItemsList;
    }

    public BigDecimal getSelectedTotal() {
        return selectedTotal.setScale(2, RoundingMode.HALF_UP);
    }

    public void setSelectedTotal(BigDecimal selectedTotal) {
        this.selectedTotal = selectedTotal;
    }

    public int getTotalSelectedItems() {
        return totalSelectedItems;
    }

    public void setTotalSelectedItems(int totalSelectedItems) {
        this.totalSelectedItems = totalSelectedItems;
    }
}
