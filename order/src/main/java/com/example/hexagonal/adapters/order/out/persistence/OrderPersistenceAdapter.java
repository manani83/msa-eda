package com.example.hexagonal.adapters.order.out.persistence;

import com.example.hexagonal.application.order.port.out.OrderCommandPort;
import com.example.hexagonal.application.order.port.out.OrderQueryPort;
import com.example.hexagonal.domain.order.Address;
import com.example.hexagonal.domain.order.Money;
import com.example.hexagonal.domain.order.Order;
import com.example.hexagonal.domain.order.OrderItem;
import com.example.hexagonal.domain.order.OrderStatus;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class OrderPersistenceAdapter implements OrderCommandPort, OrderQueryPort {
    private static final ZoneId ORDER_ID_ZONE = ZoneId.of("Asia/Seoul");
    private final OrderJpaRepository repository;
    private final OrderSequenceJpaRepository sequenceRepository;

    public OrderPersistenceAdapter(OrderJpaRepository repository, OrderSequenceJpaRepository sequenceRepository) {
        this.repository = repository;
        this.sequenceRepository = sequenceRepository;
    }

    @Override
    public Order save(Order order) {
        if (order.getOrderId() == null) {
            return toDomain(repository.save(toNewEntity(order)));
        }

        OrderEntity entity = repository.findById(order.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + order.getOrderId()));
        updateMutableFields(entity, order);
        return order;
    }

    @Override
    public Optional<Order> findById(String orderId) {
        return repository.findById(orderId)
                .map(this::toDomain);
    }

    private OrderEntity toNewEntity(Order order) {
        OrderEntity entity = new OrderEntity();
        entity.setOrderId(generateOrderId(order.getCreatedAt()));
        entity.setUserId(order.getUserId());
        entity.setStatus(order.getStatus().getCode());
        entity.setTotalAmount(order.getTotalAmount().getAmount());
        entity.setDiscountAmount(order.getDiscountAmount().getAmount());
        entity.setCouponCode(order.getCouponCode());
        entity.setCreatedAt(order.getCreatedAt());
        entity.setShippingZip(order.getShippingAddress().getZip());
        entity.setShippingLine1(order.getShippingAddress().getLine1());
        entity.setShippingLine2(order.getShippingAddress().getLine2());

        for (OrderItem item : order.getItems()) {
            OrderItemEntity itemEntity = new OrderItemEntity(
                    item.getProductId(),
                    item.getQuantity(),
                    item.getUnitPrice().getAmount()
            );
            entity.addItem(itemEntity);
        }

        return entity;
    }

    // 주문 생성 이후에는 본문과 품목을 다시 쓰지 않고 변경 컬럼만 갱신한다.
    private void updateMutableFields(OrderEntity entity, Order order) {
        entity.setStatus(order.getStatus().getCode());
        entity.setTotalAmount(order.getTotalAmount().getAmount());
        entity.setDiscountAmount(order.getDiscountAmount().getAmount());
        entity.setCouponCode(order.getCouponCode());
    }

    private Order toDomain(OrderEntity entity) {
        Address address = new Address(
                entity.getShippingZip(),
                entity.getShippingLine1(),
                entity.getShippingLine2()
        );

        List<OrderItem> items = entity.getItems().stream()
                .map(item -> new OrderItem(
                        item.getProductId(),
                        item.getQuantity(),
                        Money.of(item.getUnitPrice())
                ))
                .collect(Collectors.toList());

        return Order.rehydrate(
                entity.getOrderId(),
                entity.getUserId(),
                items,
                address,
                OrderStatus.fromCode(entity.getStatus()),
                entity.getCouponCode(),
                Money.of(entity.getDiscountAmount()),
                Money.of(entity.getTotalAmount()),
                entity.getCreatedAt()
        );
    }

    private String generateOrderId(Instant createdAt) {
        LocalDate date = createdAt.atZone(ORDER_ID_ZONE).toLocalDate();
        String datePrefix = DateTimeFormatter.BASIC_ISO_DATE.format(date);
        OrderSequenceEntity sequence = sequenceRepository.findForUpdate(datePrefix)
                .orElseGet(() -> initSequence(datePrefix));
        long nextSequence = sequence.getNextSeq();
        sequence.setNextSeq(nextSequence + 1);
        return datePrefix + String.format("%06d", nextSequence);
    }

    private OrderSequenceEntity initSequence(String datePrefix) {
        sequenceRepository.initSequence(datePrefix);
        return sequenceRepository.findForUpdate(datePrefix)
                .orElseThrow(() -> new IllegalStateException("Order sequence missing for " + datePrefix));
    }
}
