package com.controller;

import com.dao.SearchDAO;
import com.hibernate.HibernateUtil;
import com.model.pojo.Item;
import com.model.pojo.Brand;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.transaction.Transactional;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.primefaces.model.file.UploadedFile;



@ManagedBean

@SessionScoped

public class ProductBean implements Serializable {

    private int itemId;
    private Brand brand;
    private String itemName;
    private BigDecimal price;
    private String description;

    private UploadedFile file;
    private String previewImg;
    private int stocks;

    private List<String> predefinedCategories;
      private List<Item> allProducts; // Assume this contains all products
    private List<Item> sneakerProducts; // The products filtered by brand
    private List<Brand> availableBrands; // The available brands to select
    private Brand selectedBrand;
private Item itemToUpdate; // Add this variable
    private SessionFactory sessionFactory;
    private String category;
    private Item selectedProduct;
        private List<Item> sneakerProductsNike;
  private List<Item> sneakerProductsAdidas;
    private List<Item> sneakerProductsPuma;
      private List<Item> sneakerProductsOnitsuka;
  
    private String searchKeyword;  // Field untuk menyimpan keyword pencarian
    private List<Item> searchResults = new ArrayList<>(); // Inisialisasi sebagai list kosong  // Field untuk menyimpan hasil pencarian
    private SearchDAO searchDAO;  // DAO untuk pencarian
    

  
  
    
        public ProductBean() {
        sessionFactory = new Configuration().configure().buildSessionFactory();
        this.brand = new Brand();  // Inisialisasi brand
           searchDAO = new SearchDAO();  // Inisialisasi SearchDAO
               this.searchDAO = new SearchDAO(); // Pastikan ini terinisialisasi
    }
    
        
    public String getSearchKeyword() {
        return searchKeyword;
    }

    public void setSearchKeyword(String searchKeyword) {
        this.searchKeyword = searchKeyword;
    }

    public List<Item> getSearchResults() {
        return searchResults;
    }

    public void setSearchResults(List<Item> searchResults) {
        this.searchResults = searchResults;
    }
      
      

public void searchProducts() {
    if (searchKeyword == null || searchKeyword.trim().isEmpty()) {
        FacesContext.getCurrentInstance().addMessage(null, 
            new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning", "Please enter a keyword to search."));
        return;
    }

    List<Item> resultFromDAO;
    
    try {
        resultFromDAO = searchDAO.searchProductsByKeyword(searchKeyword);
    } catch (Exception e) {
        FacesContext.getCurrentInstance().addMessage(null, 
            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "An error occurred while searching for products: " + e.getMessage()));
        return;
    }
    
    if (resultFromDAO == null) {
        resultFromDAO = new ArrayList<>(); // Inisialisasi dengan daftar kosong
    }

    searchResults = resultFromDAO;

    // Tidak ada perubahan di sini, logika sudah benar
    if (searchResults.isEmpty()) {
        FacesContext.getCurrentInstance().addMessage(null, 
            new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", "No products found for keyword: " + searchKeyword));
    } else {
        int productCount = searchResults.size();
        FacesContext.getCurrentInstance().addMessage(null, 
            new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", productCount + " products found."));
    }
}



    public List<Item> getSneakerProductsByBrand(String brandName) {
    Session session = sessionFactory.openSession();
    List<Item> items = null;
    try {
        session.beginTransaction();
        // Menggunakan parameter dinamis untuk merek
        items = session.createQuery("FROM Item WHERE brand.brandName = :brandName")
                .setParameter("brandName", brandName)
                .list();

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
    return items;
}

    
public List<Item> getPredefinedCategories() {
    Session session = sessionFactory.openSession();
    List<Item> items = null;

    try {
        session.beginTransaction();
        // Use a plain HQL string for the query
        items = session.createQuery("FROM Item").list();

        for (Item item : items) {
            // Convert image byte array to Base64 string
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
        e.printStackTrace(); // Log the exception
    } finally {
        session.close();
    }
    return items;
}




public void deleteProduct(Item product) {
    Session session = sessionFactory.openSession();
    Transaction transaction = null;

    try {
        transaction = session.beginTransaction();

        // Load the product by its ID
        Item itemToDelete = (Item) session.get(Item.class, product.getItemId());
        if (itemToDelete != null) {
            session.delete(itemToDelete); // Delete the item
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Product deleted successfully!"));
        } else {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Product not found."));
        }

        transaction.commit();
    } catch (Exception e) {
        if (transaction != null) {
            transaction.rollback();
        }
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Product could not be deleted."));
        e.printStackTrace();
    } finally {
        session.close();
    }
}


public void updateProduct(Item itemToUpdate) {
    // Check for negative price or stock values
    if (itemToUpdate.getPrice().compareTo(BigDecimal.ZERO) < 0 || itemToUpdate.getStocks() < 0) {
        // Trigger a validation failure message
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Validation Error", 
                                                "Price and stock must be non-negative values.");
        FacesContext.getCurrentInstance().addMessage(null, message);
        return; // Stop the update process if validation fails
    }

    Session session = HibernateUtil.getSessionFactory().openSession();
    Transaction transaction = null;
    try {
        transaction = session.beginTransaction();
        session.update(itemToUpdate);  // Update item details
        transaction.commit();

        // Show success message upon successful update
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Update Information", "Product updated successfully.");
        FacesContext.getCurrentInstance().addMessage(null, message);
    } catch (Exception e) {
        if (transaction != null) {
            transaction.rollback();
        }

        // Show error message if an exception occurs
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Product could not be updated.");
        FacesContext.getCurrentInstance().addMessage(null, message);
        e.printStackTrace();
    } finally {
        session.close();
    }
}


public void saveAndAddProduct() {
    Session session = sessionFactory.openSession();
    Transaction transaction = null;

    // Validation for negative values
    if (price.compareTo(BigDecimal.ZERO) < 0 || stocks < 0) {
        // Trigger validation error if price or stock is negative
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Validation Error", 
                                                "Price and Stock must be non-negative values.");
        FacesContext.getCurrentInstance().addMessage(null, message);
        return; // Stop the process if validation fails
    }

    try {
        transaction = session.beginTransaction();

        // Check if the brand already exists
        Brand existingBrand = (Brand) session.createQuery("FROM Brand WHERE brand_name = :name")
                .setParameter("name", brand.getBrandName())
                .uniqueResult();

        if (existingBrand == null) {
            // Save the new brand
            session.save(brand);
        } else {
            // Use the existing brand
            this.brand = existingBrand;
        }

        // Create a new Item object
        Item item = new Item();
        item.setBrand(brand); // Ensure the brand is set correctly
        item.setItemName(itemName);
        item.setPrice(price);
        item.setDescription(description);

        item.setStocks(stocks);

        // Convert List<String> to String for sizes


        // Handle image file upload
        if (file != null) {
            byte[] imageBytes = convertUploadedFileToBytes(file);
            item.setImage(imageBytes);
        }

        // Save the item to the database
        session.save(item);
        transaction.commit();

        // Show success message
        FacesMessage successMessage = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Product added successfully!");
        FacesContext.getCurrentInstance().addMessage(null, successMessage);
    } catch (Exception e) {
        if (transaction != null) {
            transaction.rollback();
        }
        FacesMessage errorMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Product could not be saved.");
        FacesContext.getCurrentInstance().addMessage(null, errorMessage);
        e.printStackTrace();
    } finally {
        session.close();
    }
}





    // Method pembantu untuk konversi file yang diunggah menjadi byte array
    private byte[] convertUploadedFileToBytes(UploadedFile file) throws IOException {
        InputStream inputStream = file.getInputStream();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        return outputStream.toByteArray();
    }
    
        public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public Item getSelectedProduct() {
        return selectedProduct;
    }

    public void setSelectedProduct(Item selectedProduct) {
        this.selectedProduct = selectedProduct;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

 

    // Getter dan Setter untuk properti
    public int getItemId() {
        return itemId;
    }
    
        public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public Brand getBrand() {
        return brand;
    }

    public void setBrand(Brand brand) {
        this.brand = brand;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

 

    public UploadedFile getFile() {
        return file;
    }

    public void setFile(UploadedFile file) {
        this.file = file;
    }

    public String getPreviewImg() {
        return previewImg;
    }

    public void setPreviewImg(String previewImg) {
        this.previewImg = previewImg;
    }

    public int getStocks() {
        return stocks;
    }

    public void setStocks(int stocks) {
        this.stocks = stocks;
    }


    
        public void setPredefinedCategories(List<String> predefinedCategories) {
        this.predefinedCategories = predefinedCategories;
    }

        
        public void setItemToUpdate(Item product) {
    this.itemToUpdate = product; // Use itemToUpdate instead of selectedProduct
}
            public Item getItemToUpdate() {
        return itemToUpdate;
    }
    
                public void loadAdidasProducts() {
        sneakerProductsAdidas = getSneakerProductsByBrand("Adidas");
    }
    
       public List<Item> getSneakerProductsAdidas() {
        return sneakerProductsAdidas;
    }

    public void setSneakerProductsAdidas(List<Item> sneakerProductsAdidas) {
        this.sneakerProductsAdidas = sneakerProductsAdidas;
    }
  
    
         public void loadNikeProducts() {
        sneakerProductsNike = getSneakerProductsByBrand("Nike");
    }
         
             public List<Item> getSneakerProductsNike() {
        return sneakerProductsNike;
    }
    public void setSneakerProductsNike(List<Item> sneakerProductsNike) {
        this.sneakerProductsNike = sneakerProductsNike;
    }
    
    
         public void loadPumaProducts() {
        sneakerProductsPuma = getSneakerProductsByBrand("Puma");
    }
         
             public List<Item> getSneakerProductsPuma() {
        return sneakerProductsPuma;
    }
    public void setSneakerProductsPuma(List<Item> sneakerProductsPuma) {
        this.sneakerProductsPuma = sneakerProductsPuma;
    }
    
    
             public void loadOnitsukaProducts() {
        sneakerProductsOnitsuka= getSneakerProductsByBrand("Onitsuka");
    }
         
             public List<Item> getSneakerProductsOnitsuka() {
        return sneakerProductsOnitsuka;
    }
    public void setSneakerProductsOnitsuka(List<Item> sneakerProductsOnitsuka) {
        this.sneakerProductsOnitsuka= sneakerProductsOnitsuka;
    }
    
    public boolean isOutOfStock(Item product) {
    return product.getStocks() <= 0;
}

}
