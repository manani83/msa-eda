package com.example.hexagonal.adapters.order.in.web.dto;

import com.example.hexagonal.application.order.port.in.CreateOrderCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

public class CreateOrderRequest {
    @NotBlank
    private String userId;

    @Valid
    @NotEmpty
    private List<Item> items;

    @Valid
    @NotNull
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

    public CreateOrderCommand toCommand() {
        return new CreateOrderCommand(
                userId,
                items.stream()
                        .map(Item::toCommandItem)
                        .collect(Collectors.toList()),
                shippingAddress.toCommandAddress(),
                couponCode
        );
    }

    public static class Item {
        @NotBlank
        private String productId;

        @Min(1)
        private int quantity;

        @Min(0)
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

        CreateOrderCommand.CreateOrderItem toCommandItem() {
            return new CreateOrderCommand.CreateOrderItem(productId, quantity, unitPrice);
        }
    }

    public static class Address {
        @NotBlank
        private String zip;

        @NotBlank
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

        CreateOrderCommand.CreateOrderAddress toCommandAddress() {
            return new CreateOrderCommand.CreateOrderAddress(zip, line1, line2);
        }
    }
}
