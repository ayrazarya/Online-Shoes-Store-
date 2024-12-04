package com.dao;

import com.model.pojo.Item;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class ProductDAO {

    private SessionFactory factory;

    public ProductDAO() {
        factory = new Configuration().configure().buildSessionFactory();
    }

    // Method to save Item (product)
    public void saveProduct(Item item) {
        Session session = factory.openSession();
        Transaction tx = null;

        try {
            tx = session.beginTransaction();
            session.save(item);  // Save Item instead of Product
            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
    }

    // Optional: Method to update Item
    public void updateProduct(Item item) {
        Session session = factory.openSession();
        Transaction tx = null;

        try {
            tx = session.beginTransaction();
            session.update(item);  // Update Item
            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
    }

    // Optional: Method to delete Item
    public void deleteProduct(Item item) {
        Session session = factory.openSession();
        Transaction tx = null;

        try {
            tx = session.beginTransaction();
            session.delete(item);  // Delete Item
            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
    }

    // Optional: Method to fetch Item by ID
    public Item getProductById(int id) {
        Session session = factory.openSession();
        Item item = null;

        try {
            item = (Item) session.get(Item.class, id);  // Get Item by ID
        } catch (HibernateException e) {
            e.printStackTrace();
        } finally {
            session.close();
        }

        return item;
    }
}
