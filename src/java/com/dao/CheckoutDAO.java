package com.dao;

import com.controller.CheckoutBean;
import com.hibernate.HibernateUtil;
import com.model.pojo.Item;
import com.model.pojo.Orders;
import com.model.pojo.OrderItems;
import com.model.pojo.User;
import java.io.Serializable;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.hibernate.Query;

public class CheckoutDAO implements Serializable {

    private transient SessionFactory sessionFactory;
CheckoutBean checkoutBean = new CheckoutBean();
    public CheckoutDAO() {
        sessionFactory = HibernateUtil.getSessionFactory();
    }

   public void updateOrderStatusAndReduceStock(User user, List<OrderItems> selectedCartItems, BigDecimal totalAmount) {
    if (selectedCartItems == null || selectedCartItems.isEmpty()) {
        throw new IllegalArgumentException("No items in cart.");
    }

    Session session = sessionFactory.openSession();
    Transaction transaction = null;

    try {
        transaction = session.beginTransaction();

        // Fetch the current order for the user
        Orders currentOrder = findCurrentOrderForUser(user.getUserId());
        if (currentOrder != null) {
            currentOrder.setStatus("Paid Successfully");
            
            // Update total pesanan using the provided totalAmount
            currentOrder.setTotal(totalAmount); // Use the passed total amount
            session.update(currentOrder);

            // Reduce stock for each selected order item
            for (OrderItems orderItem : selectedCartItems) {
                int productId = orderItem.getItem().getItemId();
                int quantityOrdered = orderItem.getQuantity();

                // Reduce product stock
                Item product = (Item) session.get(Item.class, productId); // Cast to Item
                if (product != null) {
                    int currentStock = product.getStocks();
                    System.out.println("Produk ditemukan: " + product.getItemName() + " dengan stok: " + currentStock);
                    if (currentStock >= quantityOrdered) {
                        product.setStocks(currentStock - quantityOrdered);
                        session.update(product);
                    } else {
                        throw new IllegalArgumentException("Stok tidak cukup untuk produk: " + product.getItemName());
                    }
                } else {
                    throw new IllegalArgumentException("Produk dengan ID " + productId + " tidak ditemukan.");
                }
            }
        } else {
            throw new IllegalStateException("No current order found for user.");
        }

        // Commit transaction after all changes are successful
        transaction.commit();
    } catch (Exception e) {
        if (transaction != null) {
            transaction.rollback();
        }
        e.printStackTrace();
    } finally {
        session.close();
    }
}



  
   public List<OrderItems> getOrderItemsByUser(User user) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;
        List<OrderItems> orderItemsList = null;

        try {
            transaction = session.beginTransaction();
            
            String hql = "FROM OrderItems oi WHERE oi.user = :user";
            Query query = session.createQuery(hql);
            query.setParameter("user", user); // Mengatur parameter pengguna

            orderItemsList = query.list(); // Mendapatkan hasil query
            transaction.commit(); // Commit jika berhasil
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback(); // Rollback jika ada kesalahan
            }
            e.printStackTrace(); // Log kesalahan
        } finally {
            session.close(); // Pastikan sesi ditutup
        }
        
        return orderItemsList; // Mengembalikan daftar order items
    }
   

public List<User> getUsersWithPaidOrders() {
    Session session = HibernateUtil.getSessionFactory().openSession();
    Transaction transaction = null;
    List<User> paidUsers = null;

    try {
        transaction = session.beginTransaction();
        
        // HQL query to fetch users with paid orders without using FETCH
      String hql = "SELECT DISTINCT u FROM User u " +
             "JOIN FETCH u.orderses o " +
             "JOIN FETCH o.orderItemses oi " +
             "WHERE o.status = :status";
Query query = session.createQuery(hql);
query.setParameter("status", "Paid Successfully");


        paidUsers = query.list(); 
        transaction.commit(); 
    } catch (Exception e) {
        if (transaction != null) {
            transaction.rollback(); 
        }
        e.printStackTrace(); 
    } finally {
        session.close(); 
    }
    
    return paidUsers; 
}




  
  
  public List<Orders> getPaidOrders() {
    Session session = HibernateUtil.getSessionFactory().openSession();
    Transaction transaction = null;
    List<Orders> paidOrders = null;
    
    try {
        transaction = session.beginTransaction();
        
        // Menggunakan HQL untuk mengatur query
        String hql = "FROM Orders o "
                   + "JOIN FETCH o.orderItemses oi "
                   + "JOIN FETCH oi.item i "
                   + "WHERE o.status = :status";
        Query query = session.createQuery(hql);
        query.setParameter("status", "Paid Successfully");
        
        paidOrders = query.list();
        
        transaction.commit(); // commit transaction if you modify data
    } catch (Exception e) {
        if (transaction != null) {
            transaction.rollback(); // rollback in case of an error
        }
        e.printStackTrace(); // log the exception
    } finally {
        session.close(); // always close the session
    }
    
    return paidOrders;
}

  




    // Method untuk mendapatkan order yang saat ini ada di keranjang
    public Orders findCurrentOrderForUser(Integer userId) {
        Session session = sessionFactory.openSession();
        Orders currentOrder = null;
        try {
            String hql = "FROM Orders o WHERE o.user.userId = :userId AND o.status = 'Cart'";
            Query query = session.createQuery(hql);
            query.setParameter("userId", userId);
            currentOrder = (Orders) query.uniqueResult();

            if (currentOrder != null) {
                System.out.println("Current order with items found for user ID: " + userId);
            } else {
                System.out.println("No current order found for user ID: " + userId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
        }
        return currentOrder;
    }
}
