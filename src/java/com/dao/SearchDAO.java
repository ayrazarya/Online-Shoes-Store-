package com.dao;

import com.hibernate.HibernateUtil;
import com.model.pojo.Item;
import java.util.ArrayList;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.Base64;
import java.util.List;

public class SearchDAO {

   @SuppressWarnings("unchecked") // Suppressing unchecked cast warning for the list
public List<Item> searchProductsByKeyword(String keyword) {
    Session session = HibernateUtil.getSessionFactory().openSession();
    List<Item> items = new ArrayList<>(); // Inisialisasi dengan daftar kosong

    try {
        session.beginTransaction();
        // Hapus kategori dari query
        String hql = "FROM Item WHERE lower(itemName) LIKE :keyword OR lower(description) LIKE :keyword";
        Query query = session.createQuery(hql);
        query.setParameter("keyword", "%" + keyword.toLowerCase() + "%");
        items = query.list();

        for (Item item : items) {
            if (item.getImage() != null) {
                String base64Image = Base64.getEncoder().encodeToString(item.getImage());
                item.setImageBase64(base64Image);
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
    return items; // Mengembalikan daftar, bisa kosong
}

}
