package com.dao;

import com.hibernate.HibernateUtil;
import com.model.pojo.Item;
import com.model.pojo.OrderItems;
import com.model.pojo.Orders;
import com.model.pojo.User;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.Query;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CartDAO {

    // Method 1: Find current order for a user based on userId (new method)
    public Orders findCurrentOrderForUser(Integer userId) {
        Session session = HibernateUtil.getSessionFactory().openSession();
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

    // Method 2: Clear cart for a given orderId (new method)
    public void clearCart(int orderId) {
        Transaction transaction = null;
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();

            // Delete all OrderItems associated with the orderId
            String hql = "DELETE FROM OrderItems WHERE orders.orderId = :orderId";
            Query query = session.createQuery(hql);
            query.setParameter("orderId", orderId);
            int result = query.executeUpdate();

            if (result > 0) {
                System.out.println("Cart cleared successfully for order ID: " + orderId);
            } else {
                System.out.println("No items found in the cart for order ID: " + orderId);
            }

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

    // Method 3: Add item to the cart for a specific order (improved)
    public void addItemToCart(Orders order, Item item) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();

            // Ensure the order is saved first
            if (order.getOrderId() == 0) {
                session.save(order);
                session.flush(); // Make sure orderId is generated
                System.out.println("New order created with ID: " + order.getOrderId());
            }

            // Check if the item is already in the order (in OrderItems)
            String hql = "FROM OrderItems WHERE orders.orderId = :orderId AND item.itemId = :itemId";
            Query query = session.createQuery(hql);
            query.setParameter("orderId", order.getOrderId());
            query.setParameter("itemId", item.getItemId());
            OrderItems existingOrderItem = (OrderItems) query.uniqueResult();

            if (existingOrderItem != null) {
                // If the item exists, increase the quantity
                existingOrderItem.setQuantity(existingOrderItem.getQuantity() + 1);
                session.update(existingOrderItem);
                System.out.println("Updated item quantity: " + existingOrderItem.getQuantity());
            } else {
                // If the item doesn't exist, create a new OrderItems entry
                OrderItems newOrderItem = new OrderItems();
                newOrderItem.setOrders(order);
                newOrderItem.setItem(item);
                newOrderItem.setQuantity(1);
                newOrderItem.setPrice(item.getPrice());

                session.save(newOrderItem);
                System.out.println("New item added to cart with OrderItem ID: " + newOrderItem.getOrderItemId());
            }

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

    // Method 4: Update item quantity (improved)
    public void updateItemQuantity(int itemId, int delta) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();

            // Fetch OrderItems by itemId
            OrderItems orderItem = (OrderItems) session.createQuery("FROM OrderItems WHERE item.itemId = :itemId")
                    .setParameter("itemId", itemId)
                    .uniqueResult();

            if (orderItem != null) {
                // Update quantity
                int newQuantity = orderItem.getQuantity() + delta;
                if (newQuantity > 0) {
                    orderItem.setQuantity(newQuantity);
                    session.update(orderItem);
                    System.out.println("Item quantity updated to: " + newQuantity);
                } else {
                    session.delete(orderItem);
                    System.out.println("Item removed from cart due to zero quantity.");
                }
                transaction.commit();
            } else {
                System.out.println("Item not found in cart.");
            }
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        } finally {
            session.close();
        }
    }

    // Method 5: Update order in database (new method)
    public void updateOrderInDatabase(Orders order) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            session.update(order);
            transaction.commit();
            System.out.println("Order updated successfully with ID: " + order.getOrderId());
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        } finally {
            session.close();
        }
    }
    
     // Method 1: Remove an item from the cart based on itemId
    public void removeItemFromCart(int itemId) {
        Transaction transaction = null;
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();

            // Query to delete the OrderItems entry based on itemId
            String hql = "DELETE FROM OrderItems WHERE item.itemId = :itemId";
            Query query = session.createQuery(hql);
            query.setParameter("itemId", itemId);
            int result = query.executeUpdate();

            if (result > 0) {
                System.out.println("Item removed successfully with ID: " + itemId);
            } else {
                System.out.println("No such item found in cart with ID: " + itemId);
            }

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

    // Method 2: Add a product to the cart (represented by an order)
    public void addProductToCart(Orders order) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            session.save(order); // Save the new order
            transaction.commit();
            System.out.println("Order saved with ID: " + order.getOrderId());
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        } finally {
            session.close();
        }
    }

public List<OrderItems> getCartItemsForUser(int userId) {
    Session session = null;
    Transaction tx = null;
    List<OrderItems> cartItems = new ArrayList<>();

    try {
        session = HibernateUtil.getSessionFactory().openSession();
        tx = session.beginTransaction();

        // Query to fetch order items related to user's current order (with status 'Cart')
        String hql = "FROM OrderItems oi JOIN FETCH oi.item i " +
                     "WHERE oi.orders.user.userId = :userId AND oi.orders.status = 'Cart'";
        Query query = session.createQuery(hql);
        query.setParameter("userId", userId);
        cartItems = query.list();

        tx.commit();
        System.out.println("Order items loaded for user ID: " + userId);
    } catch (Exception e) {
        if (tx != null) {
            tx.rollback();
        }
        e.printStackTrace();
    } finally {
        if (session != null) {
            session.close();
        }
    }

    return cartItems;
}


public Orders findCurrentOrderForUser(int userId) {
    Session session = HibernateUtil.getSessionFactory().openSession();
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


public OrderItems findOrderItem(int orderId, int itemId) {
    Session session = HibernateUtil.getSessionFactory().openSession();
    OrderItems orderItem = null;
    try {
        String hql = "FROM OrderItems oi WHERE oi.orders.orderId = :orderId AND oi.item.itemId = :itemId";
        Query query = session.createQuery(hql);
        query.setParameter("orderId", orderId);
        query.setParameter("itemId", itemId);
        orderItem = (OrderItems) query.uniqueResult();
    } catch (Exception e) {
        e.printStackTrace();
    } finally {
        session.close();
    }
    return orderItem;
}

}

