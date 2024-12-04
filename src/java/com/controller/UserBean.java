/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.controller;
import com.controller.EmailService;
import com.dao.UserDAO;
import com.model.pojo.Admin;
import com.model.pojo.User;
import java.io.Serializable;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.mindrot.jbcrypt.BCrypt;
import org.primefaces.PrimeFaces;
import com.hibernate.HibernateUtil;
import java.io.IOException;
import javax.annotation.PostConstruct;


@ManagedBean(name = "userBean")
@SessionScoped
@Named("userBean")


public class UserBean implements Serializable {
    

    private static final long serialVersionUID = 1L;
    
    

    private String username;
    private String password;
    private String confirmPassword;
    private boolean loggedIn;
    private String email;
    private String authCode;
    private String newPassword;
    private User user;
    private Admin admin;
    private EmailService emailService = new EmailService();
    private String status;
    private UserDAO userDAO = new UserDAO();
    private boolean showErrorDialog; // Flag to control error dialog display
    private boolean isEditingEmail = false;
    private boolean isEditingFirstName = false;
    private boolean isEditingAddress = false;
       private boolean isEditingPayment = false;
     private String lastName; // Add this line
    private String firstName;
    private String address;
    private String paymentMethod;

    
    
    
    public String redirectToForgotPassword() {
    try {
        // Redirect ke halaman lupa password
        FacesContext.getCurrentInstance().getExternalContext().redirect("forgotPassword.xhtml"); // Ganti dengan nama halaman yang sesuai
    } catch (IOException e) {
        // Tangani kesalahan redirect
        FacesContext.getCurrentInstance().addMessage(null, 
            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error redirecting to forgot password page", e.getMessage()));
        e.printStackTrace();
    }
    return null; // Tidak perlu mengembalikan nilai karena redirect sudah dilakukan
}

  
    public String deleteAccount() {
    try {
        // Ambil user ID dari session
        Object userIdObj = FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("userId");
        
  if (userIdObj == null) {
            throw new IllegalStateException("User ID is not available in session.");
        }

        // Konversi userIdObj ke tipe int
        int userId = (int) userIdObj;

        // Ambil user dari database berdasarkan user ID
        User user = userDAO.findById(userId);
        
        if (user == null) {
            throw new IllegalStateException("User not found for the given ID.");
        }

        // Ubah status user menjadi "Inactive"
        user.setStatus("Inactive");
        userDAO.update(user); // Update user data in the database

        // Hapus user ID dan informasi lainnya dari session
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove("userId");
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove("firstName");
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove("payment");

        // Pesan sukses
        FacesContext.getCurrentInstance().addMessage(null, 
            new FacesMessage("Account deactivated successfully. You will be redirected to the home page."));

        // Redirect to home page (atau halaman lain sesuai kebutuhan)
        return "login?faces-redirect=true"; // Pastikan untuk mengganti 'home' dengan nama halaman yang sesuai
    } catch (Exception e) {
        // Tangani error dan tampilkan pesan error
        FacesContext.getCurrentInstance().addMessage(null, 
            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error deactivating account", e.getMessage()));
        e.printStackTrace();
    }
    return null; // Stay on the same page jika ada error
}

        
public String savePayment() {
    try {
        // Ambil user ID dari session
        Object userIdObj = FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("userId");
        
        if (userIdObj == null) {
            throw new IllegalStateException("User ID is not available in session.");
        }

        // Konversi userIdObj ke tipe Long atau tipe yang sesuai
        int userId = (int) userIdObj;

        // Ambil user dari database berdasarkan user ID
        User user = userDAO.findById(userId);
        
        if (user == null) {
            throw new IllegalStateException("User not found for the given ID.");
        }

        // Update payment method user
        user.setPaymentMethod(this.paymentMethod);
        userDAO.update(user); // Update user data in the database

        // Update session map
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("payment", this.paymentMethod);

        // Success message
        FacesContext.getCurrentInstance().addMessage(null, 
            new FacesMessage("Payment method updated successfully!"));
        
        this.isEditingPayment = false; // Exit edit mode and return to display mode
    } catch (Exception e) {
        // Handle error and display error message
        FacesContext.getCurrentInstance().addMessage(null, 
            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error updating payment method", e.getMessage()));
        e.printStackTrace();
    }
    return null; // Stay on the same page
}

public String saveAddress() {
    try {
        // Ambil user ID dari session
        Object userIdObj = FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("userId");
        
        if (userIdObj == null) {
            throw new IllegalStateException("User ID is not available in session.");
        }

        // Konversi userIdObj ke tipe Long atau tipe yang sesuai
        int userId = (int) userIdObj;

        // Ambil user dari database berdasarkan user ID
        User user = userDAO.findById(userId);
        
        if (user == null) {
            throw new IllegalStateException("User not found for the given ID.");
        }

        // Update address user
        user.setAddress(this.address);
        userDAO.update(user); // Update user data in the database

        // Update session map
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("address", this.address);

        // Success message
        FacesContext.getCurrentInstance().addMessage(null, 
            new FacesMessage("Address updated successfully!"));
        
        this.isEditingAddress = false; // Exit edit mode and return to display mode
    } catch (Exception e) {
        // Handle error and display error message
        FacesContext.getCurrentInstance().addMessage(null, 
            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error updating address", e.getMessage()));
        e.printStackTrace();
    }
    return null; // Stay on the same page
}



public String saveEmail() {
    try {
        // Ambil user ID dari session
        Object userIdObj = FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("userId");
        
        if (userIdObj == null) {
            throw new IllegalStateException("User ID is not available in session.");
        }

        // Konversi userIdObj ke tipe Long atau tipe yang sesuai
        int userId = (int) userIdObj;

        // Ambil user dari database berdasarkan user ID
        User user = userDAO.findById(userId);
        
        if (user == null) {
            throw new IllegalStateException("User not found for the given ID.");
        }

        // Update email user
        user.setEmail(this.email);
        userDAO.update(user); // Update user data in the database

        // Update session map
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("email", this.email);

        // Success message
        FacesContext.getCurrentInstance().addMessage(null, 
            new FacesMessage("Email updated successfully!"));
        
        this.isEditingEmail = false; // Exit edit mode and return to display mode
    } catch (Exception e) {
        // Handle error and display error message
        FacesContext.getCurrentInstance().addMessage(null, 
            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error updating email", e.getMessage()));
        e.printStackTrace();
    }
    return null; // Stay on the same page
}

public String saveFirstName() {
    try {
        // Ambil user ID dari session
        Object userIdObj = FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("userId");
        
        if (userIdObj == null) {
            throw new IllegalStateException("User ID is not available in session.");
        }

        // Konversi userIdObj ke tipe Long atau tipe yang sesuai
        int userId = (int) userIdObj;

        // Ambil user dari database berdasarkan user ID
        User user = userDAO.findById(userId);
        
        if (user == null) {
            throw new IllegalStateException("User not found for the given ID.");
        }

        // Update first name user
        user.setFirstName(this.firstName);
        userDAO.update(user); // Update user data in the database

        // Update session map
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("firstName", this.firstName);

        // Success message
        FacesContext.getCurrentInstance().addMessage(null, 
            new FacesMessage("First name updated successfully!"));
        
        this.isEditingFirstName = false; // Exit edit mode and return to display mode
    } catch (Exception e) {
        // Handle error and display error message
        FacesContext.getCurrentInstance().addMessage(null, 
            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error updating first name", e.getMessage()));
        e.printStackTrace();
    }
    return null; // Stay on the same page
}


public String register() {
    boolean validationFailed = false;

    // Check if password and confirmPassword match
    if (password.equals(confirmPassword)) {
        // Check minimum password length
        if (password.length() < 8) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Password must be at least 8 characters long.", null));
            validationFailed = true;
        } else {
            // Cari user berdasarkan email
            User existingUser = userDAO.findByEmail(email);

            if (existingUser != null) {
                if ("Inactive".equalsIgnoreCase(existingUser.getStatus())) {
                    // Jika user ditemukan dengan status Nonaktif, aktifkan kembali akun tersebut
                    existingUser.setUsername(username);

                    // Hash password baru
                    String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(5));
                    existingUser.setPassword(hashedPassword);
                    
                    // Ubah status menjadi aktif
                    existingUser.setStatus("Active");

                    // Update user di database
                    userDAO.update(existingUser);

                    // Tambahkan pesan sukses
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Account reactivated successfully!"));

                    // Redirect ke halaman login
                    PrimeFaces.current().ajax().addCallbackParam("validationFailed", false);
                    return "login";
                } else {
                    // Jika email sudah terdaftar dengan status Aktif
                    FacesContext.getCurrentInstance().addMessage(null, 
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Email already registered.", null));
                    validationFailed = true;
                }
            } else {
                // Jika user tidak ditemukan, buat akun baru
                User newUser = new User();
                newUser.setUsername(username);
                newUser.setEmail(email);

                // Hash password
                String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(5));
                newUser.setPassword(hashedPassword);
                
                // Set status user baru menjadi Aktif
                newUser.setStatus("Active");

                // Simpan user baru ke database
                userDAO.save(newUser);

                // Tambahkan pesan sukses
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Registration successful!"));

                // Redirect ke halaman login
                PrimeFaces.current().ajax().addCallbackParam("validationFailed", false);
                return "login";
            }
        }
    } else {
        // Jika password dan konfirmasi password tidak cocok
        FacesContext.getCurrentInstance().addMessage(null, 
            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Passwords do not match.", null));
        validationFailed = true;
    }

    // Kirim status validasi ke JavaScript
    PrimeFaces.current().ajax().addCallbackParam("validationFailed", validationFailed);
    return null; // Tetap di halaman yang sama jika validasi gagal
}



       public String editName() {
        try {
            // Update the user with a new name (in this case, just an example)
            user.setUsername(this.username);

            // Update the user in the database
            userDAO.update(user);

            // Add a success message
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Username updated successfully!"));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error updating username", e.getMessage()));
        }
        return null;  // Stay on the same page
    }
       
       
         public String editFirstName() {
        try {
            userDAO.update(user); // Update the user object in the database

            // Add a success message
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("First name updated successfully!"));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error updating first name", e.getMessage()));
        }
        return null; // Stay on the same page
    }
         
public String saveLastName() {
    try {
        // Check if the user ID is available in the session
        Object userIdObj = FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("userId");
        
        if (userIdObj == null) {
            throw new IllegalStateException("User ID is not available in session.");
        }

        // Convert userIdObj to the appropriate type (int in this case)
        int userId = (int) userIdObj;

        // Fetch the user from the database based on user ID
        User user = userDAO.findById(userId);
        
        if (user == null) {
            throw new IllegalStateException("User not found for the given ID.");
        }

        // Update last name
        user.setLastName(this.lastName);

        // Persist the changes in the database
        userDAO.update(user);

        // Update session map
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("lastName", this.lastName);

        // Add a success message
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Last name updated successfully!"));

    } catch (Exception e) {
        // Handle exceptions
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error updating last name", e.getMessage()));
        e.printStackTrace(); // Optional: log the error for debugging
    }

    return null; // Stay on the same page
}

    // Method to update the email
    public String editEmail() {
        try {
            user.setEmail(this.email);

            userDAO.update(user); // Update the user object in the database

            // Add a success message
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Email updated successfully!"));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error updating email", e.getMessage()));
        }
        return null; // Stay on the same page
    }
    
public String login() {
    User user = userDAO.findByUsernameAndPassword(username, password);
    Admin admin = userDAO.findAdminByUsernameAndPassword(username, password);

    
    // Cek apakah user ditemukan
    if (user != null) {
        // Cek status user apakah aktif
        
        
        if ("Active".equalsIgnoreCase(user.getStatus())) {
            loggedIn = true;
            
                // Store user information in session
                 // Simpan informasi user ke session setelah login berhasil
                 // Simpan objek user di sesi
FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("user", user);

        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("userId", user.getUserId());
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("firstName", user.getFirstName());
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("lastName", user.getLastName() );
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("userId", user.getUserId());
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("username", user.getUsername());
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("email", user.getEmail());
           FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("address", user.getAddress());
         FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("payment", user.getPaymentMethod());

        
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("userType", "user");
        

            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("userType", "user");
            setShowErrorDialog(false); // Tidak ada error
            return "index?faces-redirect=true";
        } else {
            // Jika status tidak aktif
            loggedIn = false;
            setShowErrorDialog(true); // Tampilkan dialog error
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Account is inactive.", null));
            return "login";
        }
    }
    // Cek apakah admin ditemukan
    else if (admin != null) {
        // Cek status admin apakah aktif
        if ("Active".equalsIgnoreCase(admin.getStatusAdmin())) {
            loggedIn = true;
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("userType", "admin");
            setShowErrorDialog(false); // Tidak ada error
            return "admin?faces-redirect=true";
        } else {
            // Jika status tidak aktif
            loggedIn = false;
            setShowErrorDialog(true); // Tampilkan dialog error
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Admin account is inactive.", null));
            return "login";
        }
    } 
    // Jika tidak ada user atau admin yang cocok
    else {
        loggedIn = false;
        setShowErrorDialog(true); // Tampilkan dialog error
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid credentials.", null));
        return "login";
    }
}


    // Metode untuk reset password user
    public String forgotPassword() {
        user = userDAO.findByEmail(email);
        if (user != null) {
            String code = userDAO.generateAndSaveAuthCode(user);
            String content = "Kode OTP: " + code;
            emailService.sendEmail(user.getEmail(), "Reset Password", content);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("An email has been sent with the authentication code."));
            return "enterAuthCode"; // Redirect ke halaman untuk memasukkan kode autentifikasi
        } else {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Email not found."));
            return null;
        }
    }

    // Metode untuk verifikasi kode autentifikasi user
    public String verifyAuthCode() {
        boolean isVerified = userDAO.verifyAuthCodeForUser(user, authCode);
        if (isVerified) {
            return "resetPassword?faces-redirect=true"; // Redirect ke halaman reset password
        } else {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Invalid authentication code."));
            return null;
        }
    }

    // Metode untuk reset password user
    public String resetPassword() {
        if (newPassword.equals(confirmPassword)) {
            boolean success = userDAO.resetPassword(user, newPassword);
            if (success) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Password has been reset successfully."));
                return "login"; // Redirect ke halaman login setelah berhasil reset password
            } else {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Failed to reset password."));
                return null;
            }
        } else {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Passwords do not match."));
            return null;
        }
    }

    // Metode untuk reset password admin
    public String adminForgotPassword() {
        admin = userDAO.findAdminByEmail(email);
        if (admin != null) {
            String code = userDAO.generateAndSaveAuthCodeForAdmin(admin);
            String content = "Kode OTP: " + code;
            emailService.sendEmail(admin.getEmail(), "Reset Password", content);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("An email has been sent with the authentication code."));
            return "enterAdminAuthCode"; // Redirect ke halaman untuk memasukkan kode autentifikasi admin
        } else {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Username not found."));
            return null;
        }
    }

    // Metode untuk verifikasi kode autentifikasi admin
    public String adminVerifyAuthCode() {
        boolean isVerified = userDAO.verifyAuthCodeForAdmin(admin, authCode);
        if (isVerified) {
            return "adminResetPassword"; // Redirect ke halaman reset password admin
        } else {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Invalid authentication code."));
            return null;
        }
    }

    // Metode untuk reset password admin
    public String adminResetPassword() {
        if (newPassword.equals(confirmPassword)) {
            boolean success = userDAO.resetPasswordForAdmin(admin, newPassword);
            if (success) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Password has been reset successfully."));
                return "login"; // Redirect ke halaman login admin setelah berhasil reset password
            } else {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Failed to reset password."));
                return null;
            }
        } else {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Passwords do not match."));
            return null;
        }
    }
    
    public String goToAdminPage() {
    if ("admin".equals(FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("userType"))) {
        return "/admin?faces-redirect=true"; // Redirect ke halaman user.xhtml di root
    }
    return null; // Jika bukan user, tidak melakukan apa-apa
}

public String goToUserPage() {
    // Periksa apakah userType di session adalah 'user'
    if ("user".equals(FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("userType"))) {
        // Ambil informasi user dari session
        Object userId = FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("userId");
        Object username = FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("username");

        // Periksa apakah informasi user ada
        if (userId != null && username != null) {
            // Lakukan redirect ke halaman user
            return "/user?faces-redirect=true";
        } else {
            // Jika informasi user tidak ditemukan, mungkin session sudah habis
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "User not logged in.", null));
            return "login?faces-redirect=true"; // Redirect ke login jika user tidak ditemukan di session
        }
    }
    
    // Jika userType bukan 'user', jangan lakukan apa-apa
    return null;
}


    
public String logout() {
    try {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();
        HttpSession session = request.getSession(false); // Ambil session jika ada

        if (session != null) {
            session.invalidate(); // Invalidate session
            System.out.println("Session invalidated successfully.");
        } else {
            System.out.println("No session found to invalidate.");
        }

        facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Logout successful", "You have been logged out."));
    } catch (Exception e) {
        e.printStackTrace();
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Logout failed", "An error occurred during logout."));
    }

    return "login?faces-redirect=true"; // Ganti dengan outcome halaman yang sesuai
}

    
    // Getter dan Setter untuk admin
    public Admin getAdmin() {
        return admin;
    }

    public void setAdmin(Admin admin) {
        this.admin = admin;
    }
    
      public boolean isShowErrorDialog() {
        return showErrorDialog;
    }

    public void setShowErrorDialog(boolean showErrorDialog) {
        this.showErrorDialog = showErrorDialog;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Getter dan Setter
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAuthCode() {
        return authCode;
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public EmailService getEmailService() {
        return emailService;
    }

    public void setEmailService(EmailService emailService) {
        this.emailService = emailService;
    }

    public UserDAO getUserDAO() {
        return userDAO;
    }

    public void setUserDAO(UserDAO userDAO) {
        this.userDAO = userDAO;
    }
    
    
            public boolean isEditingEmail() {
    return isEditingEmail;
}

public void setEditingEmail(boolean isEditingEmail) {
    this.isEditingEmail = isEditingEmail;
}

// Method untuk memulai pengeditan email
public String startEditingEmail() {
    this.isEditingEmail = true;
    return null; // Tetap di halaman yang sama
}






    public String getAddress() {
        return address;
    }

public void setAddress(String address) {
    this.address = address;
}

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firName) {
        this.firstName = firName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public boolean isEditingPayment() {
    return isEditingPayment;
}

public void setEditingPayment(boolean isEditingPayment) {
    this.isEditingPayment = isEditingPayment;
}

  public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    
}
