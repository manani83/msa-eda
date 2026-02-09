package com.example.hexagonal.adapters.order.in.web.dto;

import java.util.List;

public class CreateOrderRequest {
    private String userId;
    private List<Item> items;
    private Address shippingAddress;
    private String couponCode;

    public CreateOrderRequest() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public Address getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(Address shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }

    public static class Item {
        private String productId;
        private int quantity;
        private long unitPrice;

        public Item() {
        }

        public String getProductId() {
            return productId;
        }

        public void setProductId(String productId) {
            this.productId = productId;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public long getUnitPrice() {
            return unitPrice;
        }

        public void setUnitPrice(long unitPrice) {
            this.unitPrice = unitPrice;
        }
    }

    public static class Address {
        private String zip;
        private String line1;
        private String line2;

        public Address() {
        }

        public String getZip() {
            return zip;
        }

        public void setZip(String zip) {
            this.zip = zip;
        }

        public String getLine1() {
            return line1;
        }

        public void setLine1(String line1) {
            this.line1 = line1;
        }

        public String getLine2() {
            return line2;
        }

        public void setLine2(String line2) {
            this.line2 = line2;
        }
    }
}
