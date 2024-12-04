/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dao;



import com.model.pojo.User;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.Query;
import org.mindrot.jbcrypt.BCrypt;

import com.hibernate.HibernateUtil;
import com.model.pojo.Admin;
import com.hibernate.HibernateUtil;
import com.model.pojo.Otpcode;
import java.util.Random;
import java.util.Date;
import com.dao.OtpCodeDAO;
import javax.enterprise.context.Dependent;
import javax.inject.Named;

@Named
@Dependent // or appropriate scope
public class UserDAO {

 public void update(User user) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            
            // Update user di database
            session.update(user);  // Hibernate akan mengupdate user yang ada
            
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
    }


    
    // Other methods...

    // Simpan User
    public void save(User user) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            
            // Hash password with specific log rounds
            String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt(5));
            user.setPassword(hashedPassword);
            
            session.save(user);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
    }

    // Temukan User berdasarkan Username dan Password
public User findByUsernameAndPassword(String username, String password) {
    Session session = HibernateUtil.getSessionFactory().openSession();
    User user = null;
    try {
        String hql = "FROM User WHERE username = :username";
        Query query = session.createQuery(hql);
        query.setParameter("username", username);
        user = (User) query.uniqueResult();
        
        if (user != null) {
            // Manual case-sensitive check in Java
            if (!user.getUsername().equals(username)) {
                user = null; // Username case does not match
            } else if (!BCrypt.checkpw(password, user.getPassword())) {
                user = null; // Password tidak cocok
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    } finally {
        session.close();
    }
    return user;
}


    // Temukan User berdasarkan Email
    public User findByEmail(String email) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        User user = null;
        try {
            String hql = "FROM User WHERE email = :email";
            Query query = session.createQuery(hql);
            query.setParameter("email", email);
            user = (User) query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
        }
        return user;
    }

    // Generate dan simpan kode autentifikasi
    public String generateAndSaveAuthCode(User user) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            
            // Generate kode autentifikasi 6 digit
            String authCode = String.format("%06d", new Random().nextInt(1000000));
            
            Otpcode otpCode = new Otpcode();
            otpCode.setUser(user);
            otpCode.setAuthCode(authCode);
            otpCode.setExpiryTime(new Date(System.currentTimeMillis() + 15 * 60 * 1000)); // 15 menit valid
            otpCode.setIsUsed(false);
            
            OtpCodeDAO otpCodeDAO = new OtpCodeDAO();
            otpCodeDAO.saveOtpCode(otpCode);
            
            transaction.commit();
            return authCode;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
        return null;
    }

// Verifikasi kode autentifikasi untuk User
public boolean verifyAuthCodeForUser(User user, String authCode) {
    OtpCodeDAO otpCodeDAO = new OtpCodeDAO();
    Otpcode otpCode = otpCodeDAO.findOtpCodeByUser(user);
    
    if (otpCode != null) {
        // Logging untuk debug
        System.out.println("Found OTP Code: " + otpCode.getAuthCode());
        
        if (otpCode.getAuthCode().equals(authCode)) {
            // Logging untuk debug
            System.out.println("OTP Code matches. Marking as used.");
            
            otpCodeDAO.markOtpAsUsed(otpCode);
            return true;
        } else {
            // Logging untuk debug
            System.out.println("OTP Code does not match.");
        }
    } else {
        // Logging untuk debug
        System.out.println("OTP Code not found.");
    }
    return false;
}
    
    public Admin findAdminByUsernameAndPassword(String username, String password) {
    Session session = HibernateUtil.getSessionFactory().openSession();
    Admin admin = null;
    try {
        String hql = "FROM Admin WHERE username = :username";
        Query query = session.createQuery(hql);
        query.setParameter("username", username);
        admin = (Admin) query.uniqueResult();
        
        if (admin != null) {
            // Verify password with BCrypt
            if (!BCrypt.checkpw(password, admin.getPassword())) {
                admin = null; // Password mismatch
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    } finally {
        session.close();
    }
    return admin;
}
    
    public String generateAndSaveAuthCodeForAdmin(Admin admin) {
    Session session = HibernateUtil.getSessionFactory().openSession();
    Transaction transaction = null;
    try {
        transaction = session.beginTransaction();
        
        // Generate kode autentifikasi 6 digit
        String authCode = String.format("%06d", new Random().nextInt(1000000));
        
        Otpcode otpCode = new Otpcode();
        otpCode.setAdmin(admin);
        otpCode.setAuthCode(authCode);
        otpCode.setExpiryTime(new Date(System.currentTimeMillis() + 15 * 60 * 1000)); // 15 menit valid
        otpCode.setIsUsed(false);
        
        OtpCodeDAO otpCodeDAO = new OtpCodeDAO();
        otpCodeDAO.saveOtpCode(otpCode);
        
        transaction.commit();
        return authCode;
    } catch (Exception e) {
        if (transaction != null) transaction.rollback();
        e.printStackTrace();
    } finally {
        session.close();
    }
    return null;
}

    public boolean resetPassword(User user, String newPassword) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            
            // Hash new password
            String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt(5));
            user.setPassword(hashedPassword);
            
            session.update(user);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
            return false;
        } finally {
            session.close();
        }
    }

    // Metode untuk reset password admin
    public boolean resetPasswordForAdmin(Admin admin, String newPassword) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            
            // Hash new password
            String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt(5));
            admin.setPassword(hashedPassword);
            
            session.update(admin);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
            return false;
        } finally {
            session.close();
        }
    }

    // Temukan Admin berdasarkan Email
    public Admin findAdminByEmail(String email) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Admin admin = null;
        try {
            String hql = "FROM Admin WHERE email = :email";
            Query query = session.createQuery(hql);
            query.setParameter("email", email);
            admin = (Admin) query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
        }
        return admin;
    }

    // Verifikasi kode autentifikasi untuk Admin
    public boolean verifyAuthCodeForAdmin(Admin admin, String authCode) {
        OtpCodeDAO otpCodeDAO = new OtpCodeDAO();
        Otpcode otpCode = otpCodeDAO.findOtpCodeByAdmin(admin);
        
        if (otpCode != null) {
            // Logging untuk debug
            System.out.println("Found OTP Code: " + otpCode.getAuthCode());
            
            if (otpCode.getAuthCode().equals(authCode)) {
                // Logging untuk debug
                System.out.println("OTP Code matches. Marking as used.");
                
                otpCodeDAO.markOtpAsUsed(otpCode);
                return true;
            } else {
                // Logging untuk debug
                System.out.println("OTP Code does not match.");
            }
        } else {
            // Logging untuk debug
            System.out.println("OTP Code not found.");
        }
        return false;
    }

    // Metode untuk mengecek keberadaan email
    public boolean emailExists(String email) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            String hql = "SELECT COUNT(*) FROM User WHERE email = :email";
            Query query = session.createQuery(hql);
            query.setParameter("email", email);
            
            Long count = (Long) query.uniqueResult();
            return count != null && count > 0;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
        }
        return false;
    }

public User findById(Integer userId) {
    Session session = HibernateUtil.getSessionFactory().openSession();
    User user = null;
    try {
        // Fetch user by ID
        user = (User) session.get(User.class, userId); 
    } catch (Exception e) {
        e.printStackTrace();
    } finally {
        session.close();
    }
    return user;
}


public User findByUsername(String username) {
    Session session = HibernateUtil.getSessionFactory().openSession();
    User user = null;
    try {
        String hql = "FROM User WHERE username = :username";
        Query query = session.createQuery(hql);
        query.setParameter("username", username);
        user = (User) query.uniqueResult();
    } catch (Exception e) {
        e.printStackTrace();
    } finally {
        session.close();
    }
    return user;
}

}
