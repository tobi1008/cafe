package com.example.cafe;

import java.io.Serializable;
import java.util.Date;

public class Review implements Serializable { // <-- IMPLEMENT SERIALIZABLE
    private String reviewId;
    private String productId;
    private String userId;
    private String userName;
    private float rating;
    private String comment;
    private Date timestamp; // <-- THAY ĐỔI TỪ TIMESTAMP SANG DATE

    public Review() {
        // Constructor rỗng cần thiết cho Firestore
    }

    // --- Getters & Setters ---
    public String getReviewId() { return reviewId; }
    public void setReviewId(String reviewId) { this.reviewId = reviewId; }
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
}

