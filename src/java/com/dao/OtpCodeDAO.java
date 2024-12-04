/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dao;



import java.util.Date;
import java.util.List;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.Query; // Import Hibernate 4.x Query
import com.hibernate.HibernateUtil;
import com.model.pojo.Admin;
import com.model.pojo.User;
import com.model.pojo.Otpcode;

public class OtpCodeDAO {

    // Save OTP code
    public void saveOtpCode(Otpcode otpcode) {
        
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            session.save(otpcode);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
    }

    // Find OTP code by User
    public Otpcode findOtpCodeByUser(User user) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            String hql = "FROM Otpcode WHERE user = :user AND isUsed = false AND expiryTime > :currentTime ORDER BY expiryTime DESC";
            Query query = session.createQuery(hql);
            query.setParameter("user", user);
            query.setParameter("currentTime", new Date());
            List<Otpcode> otpCodes = query.list();

            if (otpCodes.isEmpty()) {
                return null; // No OTP codes found
            } else {
                // Get the most recent OTP code
                return otpCodes.get(0); // Assuming the list is ordered by expiry time
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
        }
        return null;
    }

    // Mark OTP code as used
    public void markOtpAsUsed(Otpcode otpCode) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            otpCode.setIsUsed(Boolean.TRUE);
            session.update(otpCode);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
    }

    // Find OTP code by Admin
    
    public Otpcode findOtpCodeByAdmin(Admin admin) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            String hql = "FROM OtpCode WHERE admin = :admin AND isUsed = false AND expiryTime > :currentTime ORDER BY expiryTime DESC";
            Query query = session.createQuery(hql);
            query.setParameter("admin", admin);
            query.setParameter("currentTime", new Date());
            List<Otpcode> otpCodes = query.list();

            if (otpCodes.isEmpty()) {
                return null; // No OTP codes found
            } else {
                // Get the most recent OTP code
                return otpCodes.get(0); // Assuming the list is ordered by expiry time
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
        }
        return null;
    }
}

