package com.example.hexagonal.adapters.order.out.persistence;

import com.example.hexagonal.application.order.port.out.OrderCommandPort;
import com.example.hexagonal.domain.order.Address;
import com.example.hexagonal.domain.order.Money;
import com.example.hexagonal.domain.order.Order;
import com.example.hexagonal.domain.order.OrderItem;
import com.example.hexagonal.domain.order.OrderStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderPersistenceAdapter implements OrderCommandPort {
    private static final ZoneId ORDER_ID_ZONE = ZoneId.of("Asia/Seoul");
    private final OrderJpaRepository repository;
    private final OrderSequenceJpaRepository sequenceRepository;

    public OrderPersistenceAdapter(OrderJpaRepository repository, OrderSequenceJpaRepository sequenceRepository) {
        this.repository = repository;
        this.sequenceRepository = sequenceRepository;
    }

    @Override
    @Transactional
    public Order save(Order order) {
        OrderEntity entity = toEntity(order);
        OrderEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    private OrderEntity toEntity(Order order) {
        OrderEntity entity = new OrderEntity();
        if (order.getOrderId() == null) {
            entity.setOrderId(generateOrderId(order.getCreatedAt()));
        } else {
            entity.setOrderId(order.getOrderId());
        }
        entity.setUserId(order.getUserId());
        entity.setStatus(order.getStatus().name());
        entity.setTotalAmount(order.getTotalAmount().getAmount());
        entity.setDiscountAmount(order.getDiscountAmount().getAmount());
        entity.setCouponCode(order.getCouponCode());
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
                OrderStatus.valueOf(entity.getStatus()),
                entity.getCouponCode(),
                Money.of(entity.getDiscountAmount()),
                Money.of(entity.getTotalAmount()),
                entity.getCreatedAt()
        );
    }

    private String generateOrderId(Instant createdAt) {
        LocalDate date = createdAt.atZone(ORDER_ID_ZONE).toLocalDate();
        String datePrefix = DateTimeFormatter.BASIC_ISO_DATE.format(date);
        sequenceRepository.initSequence(datePrefix);
        OrderSequenceEntity sequence = sequenceRepository.findForUpdate(datePrefix)
                .orElseThrow(() -> new IllegalStateException("Order sequence missing for " + datePrefix));
        long nextSequence = sequence.getNextSeq();
        sequence.setNextSeq(nextSequence + 1);
        sequenceRepository.save(sequence);
        return datePrefix + String.format("%06d", nextSequence);
    }
}
