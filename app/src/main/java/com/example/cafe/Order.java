package com.example.cafe;

import java.io.Serializable;
import java.util.Date; // <-- THAY ĐỔI IMPORT
import java.util.List;

public class Order implements Serializable {
    private String orderId;
    private String userId;
    private String customerName;
    private String customerPhone;
    private String customerAddress;
    private List<CartItem> items;
    private double totalPrice;
    private Date orderDate; // <-- THAY ĐỔI KIỂU DỮ LIỆU
    private String status;

    public Order() {
        // Constructor rỗng cần thiết cho Firebase
    }

    // --- Getters & Setters ---
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
    public String getCustomerAddress() { return customerAddress; }
    public void setCustomerAddress(String customerAddress) { this.customerAddress = customerAddress; }
    public List<CartItem> getItems() { return items; }
    public void setItems(List<CartItem> items) { this.items = items; }
    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
    public Date getOrderDate() { return orderDate; } // <-- CẬP NHẬT GETTER
    public void setOrderDate(Date orderDate) { this.orderDate = orderDate; } // <-- CẬP NHẬT SETTER
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

