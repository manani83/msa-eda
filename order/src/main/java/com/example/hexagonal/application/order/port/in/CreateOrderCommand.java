package com.example.hexagonal.application.order.port.in;

import com.example.hexagonal.domain.order.Address;
import com.example.hexagonal.domain.order.Money;
import com.example.hexagonal.domain.order.Order;
import com.example.hexagonal.domain.order.OrderItem;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public class CreateOrderCommand {
    private final String userId;
    private final List<CreateOrderItem> items;
    private final CreateOrderAddress shippingAddress;
    private final String couponCode;

    public CreateOrderCommand(String userId,
                              List<CreateOrderItem> items,
                              CreateOrderAddress shippingAddress,
                              String couponCode) {
        this.userId = userId;
        this.items = items;
        this.shippingAddress = shippingAddress;
        this.couponCode = couponCode;
    }

    public String getUserId() {
        return userId;
    }

    public List<CreateOrderItem> getItems() {
        return items;
    }

    public CreateOrderAddress getShippingAddress() {
        return shippingAddress;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public Order toPendingOrder(Instant createdAt) {
        return Order.createPendingBenefits(
                userId,
                items.stream()
                        .map(CreateOrderItem::toDomain)
                        .collect(Collectors.toList()),
                shippingAddress.toDomain(),
                couponCode,
                createdAt
        );
    }

    public static class CreateOrderItem {
        private final String productId;
        private final int quantity;
        private final long unitPrice;

        public CreateOrderItem(String productId, int quantity, long unitPrice) {
            this.productId = productId;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }

        public String getProductId() {
            return productId;
        }

        public int getQuantity() {
            return quantity;
        }

        public long getUnitPrice() {
            return unitPrice;
        }

        private OrderItem toDomain() {
            return new OrderItem(productId, quantity, Money.of(unitPrice));
        }
    }

    public static class CreateOrderAddress {
        private final String zip;
        private final String line1;
        private final String line2;

        public CreateOrderAddress(String zip, String line1, String line2) {
            this.zip = zip;
            this.line1 = line1;
            this.line2 = line2;
        }

        public String getZip() {
            return zip;
        }

        public String getLine1() {
            return line1;
        }

        public String getLine2() {
            return line2;
        }

        private Address toDomain() {
            return new Address(zip, line1, line2);
        }
    }
}
