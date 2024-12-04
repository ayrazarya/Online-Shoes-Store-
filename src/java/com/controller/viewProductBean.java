package com.controller;

import com.dao.viewProductDAO;
import com.model.pojo.Item;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import org.primefaces.model.file.UploadedFile;

@ManagedBean(name = "viewProductBean")
@ViewScoped
public class viewProductBean implements Serializable {

    private int itemId;
    private Item selectedProduct; // Holds the product details
    private List<Item> similarProducts; // List to hold similar products
    private viewProductDAO productDAO;
    private String sizes; // Keep as String
    private String previewImg;
     @ManagedProperty(value = "#{cartBean}")
    private CartBean cartBean;
    

   @PostConstruct
public void init() {
    productDAO = new viewProductDAO();

    // Capture itemId from the URL parameter
    String itemIdParam = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("itemId");

    if (itemIdParam != null) {
        try {
            itemId = Integer.parseInt(itemIdParam);
            System.out.println("itemId from URL: " + itemId);  // Log itemId

            // Load the product using the captured itemId
            loadProduct();

            // Only load image after successfully loading product
            if (selectedProduct != null) {
                loadProductImage();  // Load product image after product is found
                loadSimilarProducts(); // Load similar products
            }

        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    } else {
        System.out.println("No itemId found in URL.");
    }
}


public void addItemToCart() {
    if (selectedProduct != null) {
        // Ensure the cartBean is not null
        if (cartBean != null) {
            // Add the selected product to the cart
            cartBean.addItemToCart(selectedProduct);
            System.out.println("Product added to cart: " + selectedProduct.getItemName());
        } else {
            System.out.println("CartBean is not initialized.");
        }
    } else {
        System.out.println("No product selected.");
    }
}




    public CartBean getCartBean() {
        return cartBean;
    }

    public void setCartBean(CartBean cartBean) {
        this.cartBean = cartBean;
    }

    // Method to load product details
    public void loadProduct() {
        if (itemId > 0) {  // Check if itemId is valid
            selectedProduct = productDAO.getProductById(itemId);
            if (selectedProduct != null) {
                System.out.println("Product loaded: " + selectedProduct.getItemName());
            } else {
                System.out.println("No product found for itemId: " + itemId);
            }
        } else {
            System.out.println("itemId is invalid, cannot load product.");
        }
    }

    // Method to load product image
    public void loadProductImage() {
        // Ensure itemId is valid
        if (itemId > 0) {
            selectedProduct = productDAO.getProductImageById(itemId);
            if (selectedProduct != null) {
                System.out.println("Image loaded for product: " + selectedProduct.getItemName());
            } else {
                System.out.println("Product not found for itemId: " + itemId);
            }
        } else {
            System.out.println("Invalid itemId.");
        }
    }

    // Method to load similar products
    public void loadSimilarProducts() {
        // Load similar products based on itemId
        if (itemId > 0) {
            similarProducts = productDAO.getSimilarProducts(itemId);
            if (similarProducts != null && !similarProducts.isEmpty()) {
                System.out.println("Similar products loaded for itemId: " + itemId);
            } else {
                System.out.println("No similar products found for itemId: " + itemId);
            }
        } else {
            System.out.println("Invalid itemId, cannot load similar products.");
        }
    }

    // Method to convert uploaded file to byte array
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

    // Getter for the Base64 image
    public String getImageBase64() {
        if (selectedProduct != null) {
            return selectedProduct.getImageBase64();
        }
        return null;
    }

    // Getters and Setters
    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public Item getSelectedProduct() {
        return selectedProduct;
    }

    public void setSelectedProduct(Item selectedProduct) {
        this.selectedProduct = selectedProduct;
    }

    public List<Item> getSimilarProducts() {
        return similarProducts;
    }

    public void setSimilarProducts(List<Item> similarProducts) {
        this.similarProducts = similarProducts;
    }

    public viewProductDAO getProductDAO() {
        return productDAO;
    }

    public void setProductDAO(viewProductDAO productDAO) {
        this.productDAO = productDAO;
    }

    public String getSizes() {
        return this.sizes;
    }

    public void setSizes(String sizes) {
        this.sizes = sizes;
    }

    public List<String> getSizeList() {
        return sizes != null ? Arrays.asList(sizes.split(",")) : new ArrayList<>();
    }

    public void setSizeList(List<String> selectedSizes) {
        this.sizes = String.join(",", selectedSizes);
    }
    
     public boolean isOutOfStock(Item product) {
    return product.getStocks() <= 0;
}
}
