package com.dao;


import com.model.pojo.Item;
import java.util.Base64;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import java.util.List;

public class viewProductDAO {

    private SessionFactory sessionFactory;

    public viewProductDAO() {
        // Initialize Hibernate session factory
        sessionFactory = new Configuration().configure().buildSessionFactory();
    }

    // Method to get all products
    public List<Item> getAllProducts() {
        Session session = sessionFactory.openSession();
        Transaction transaction = null;
        List<Item> products = null;

        try {
            transaction = session.beginTransaction();
            products = session.createQuery("FROM Item").list(); // Load all products
            transaction.commit();
        } catch (HibernateException e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        } finally {
            session.close();
        }
        return products;
    }

    // Method to get product by ID
    public Item getProductById(int itemId) {
        Session session = sessionFactory.openSession();
        Transaction transaction = null;
        Item product = null;

        try {
            transaction = session.beginTransaction();
            product = (Item) session.get(Item.class, itemId);  // Ensure this query works correctly
            transaction.commit();
            if (product != null) {
                System.out.println("Product retrieved: " + product.getItemName());
            } else {
                System.out.println("No product found with itemId: " + itemId);
            }
        } catch (HibernateException e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        } finally {
            session.close();
        }

        return product;
    }

    // Method to get product image by ID and convert image to Base64
    public Item getProductImageById(int itemId) {
        Session session = sessionFactory.openSession();
        Item item = null;

        try {
            session.beginTransaction();
            // Find product by itemId
            item = (Item) session.get(Item.class, itemId);

            // If item is found and has an image
            if (item != null && item.getImage() != null) {
                // Convert byte array to Base64
                String base64Image = Base64.getEncoder().encodeToString(item.getImage());
                item.setImageBase64(base64Image);  // Set Base64 image in the product
            }

            session.getTransaction().commit();
        } catch (Exception e) {
            if (session.getTransaction() != null) {
                session.getTransaction().rollback();
            }
            e.printStackTrace(); // Log error if any
        } finally {
            session.close();
        }

        return item;
    }

 public List<Item> getSimilarProducts(int itemId) {
    Session session = sessionFactory.openSession();
    List<Item> similarProducts = null;

    try {
        session.beginTransaction();

        // Query to find products with the same brand as the selected product
        similarProducts = session.createQuery("FROM Item WHERE brand = (SELECT brand FROM Item WHERE id = :itemId)")
                .setParameter("itemId", itemId)
                .setMaxResults(4)  // Limit the result to 4 similar products
                .list();

        // Convert each item's image to Base64
        for (Item product : similarProducts) {
            if (product.getImage() != null) {
                String base64Image = Base64.getEncoder().encodeToString(product.getImage());
                product.setImageBase64(base64Image);
            }
        }

        session.getTransaction().commit();
    } catch (Exception e) {
        if (session.getTransaction() != null) {
            session.getTransaction().rollback();
        }
        e.printStackTrace();
    } finally {
        session.close();
    }

    return similarProducts;
}

    
}
