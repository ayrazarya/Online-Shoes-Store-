package com.dao;

import com.model.pojo.OrderItems;
import com.hibernate.HibernateUtil;
import java.io.Serializable;
import java.util.List;
import org.hibernate.Session;
import org.hibernate.Transaction;
import javax.enterprise.context.Dependent;

@Dependent
public class OrderItemsDAO implements Serializable {

    @SuppressWarnings("unchecked")
    public List<OrderItems> getAllOrderItems() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;
        List<OrderItems> orderItemsList = null;

        try {
            transaction = session.beginTransaction();
            String hql = "FROM OrderItems oi WHERE oi.orders.user.id IS NOT NULL";
            orderItemsList = session.createQuery(hql).list();
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return orderItemsList;
    }

public List<OrderItems> getPaidOrdersByUserId(String userId) {
    Session session = HibernateUtil.getSessionFactory().openSession();
    Transaction transaction = null;
    List<OrderItems> orderItemsList = null;

    try {
        transaction = session.beginTransaction();
        // Use JOIN FETCH to eagerly load the item entity
        String hql = "SELECT oi FROM OrderItems oi JOIN FETCH oi.item i JOIN FETCH oi.orders o JOIN FETCH o.user u WHERE u.userId = :userId AND o.status = 'Paid Successfully'";
        orderItemsList = session.createQuery(hql)
                                .setParameter("userId", Integer.parseInt(userId))
                                .list();
        transaction.commit();
    } catch (Exception e) {
        if (transaction != null) {
            transaction.rollback();
        }
        e.printStackTrace();
    } finally {
        if (session != null) {
            session.close();
        }
    }
    return orderItemsList;
}
 @SuppressWarnings("unchecked")
    public List<OrderItems> getAllPaidOrderItems() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;
        List<OrderItems> orderItemsList = null;

        try {
            transaction = session.beginTransaction();
            // Fetch all order items with status "Paid Successfully"
            String hql = "FROM OrderItems oi JOIN FETCH oi.orders o JOIN FETCH oi.item i JOIN FETCH o.user u WHERE o.status = 'Paid Successfully'";
            orderItemsList = session.createQuery(hql).list();
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return orderItemsList;
    }
    
    
     @SuppressWarnings("unchecked")
    public List<OrderItems> getAllPaidOrderItemsHistory() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;
        List<OrderItems> orderItemsList = null;

        try {
            transaction = session.beginTransaction();
            // Fetch all order items with status "Paid Successfully"
            String hql = "FROM OrderItems oi JOIN FETCH oi.orders o JOIN FETCH oi.item i JOIN FETCH o.user u WHERE o.status = 'Order Received'";
            orderItemsList = session.createQuery(hql).list();
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return orderItemsList;
    }
    
    
    
    
    
    
    
    
    
     // New method to update the order status to 'Dikirim' (Shipped)
    public void updateOrderStatusToShipped(int orderId) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();
            String hql = "UPDATE Orders o SET o.status = 'Shipping' WHERE o.orderId = :orderId";
            session.createQuery(hql).setParameter("orderId", orderId).executeUpdate();
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }


    public List<OrderItems> getUserOrderItems(int userId) {
    Session session = HibernateUtil.getSessionFactory().openSession();
    Transaction transaction = null;
    List<OrderItems> orderItemsList = null;

    try {
        transaction = session.beginTransaction();
        // Fetch all order items for the user
        String hql = "FROM OrderItems oi JOIN FETCH oi.orders o JOIN FETCH oi.item i " +
                     "WHERE o.user.userId = :userId";
        orderItemsList = session.createQuery(hql)
                                .setParameter("userId", userId)
                                .list();
        transaction.commit();
        System.out.println("Fetched OrderItems size: " + orderItemsList.size());  // Add debug logging
    } catch (Exception e) {
        if (transaction != null) {
            transaction.rollback();
        }
        e.printStackTrace();
    } finally {
        if (session != null) {
            session.close();
        }
    }
    return orderItemsList;
}




// Method to update the order status to 'Diterima' (Received)
public void updateOrderStatusToReceived(int orderId) {
    Session session = HibernateUtil.getSessionFactory().openSession();
    Transaction transaction = null;

    try {
        transaction = session.beginTransaction();
        String hql = "UPDATE Orders o SET o.status = 'Order Received' WHERE o.orderId = :orderId";
        session.createQuery(hql).setParameter("orderId", orderId).executeUpdate();
        transaction.commit();
    } catch (Exception e) {
        if (transaction != null) {
            transaction.rollback();
        }
        e.printStackTrace();
    } finally {
        if (session != null) {
            session.close();
        }
    }
}

    
    
    
}
