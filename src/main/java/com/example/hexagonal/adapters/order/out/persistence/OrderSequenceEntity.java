package com.example.hexagonal.adapters.order.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "order_sequences")
public class OrderSequenceEntity {
    @Id
    @Column(name = "date_prefix", length = 8)
    private String datePrefix;

    @Column(name = "next_seq", nullable = false)
    private long nextSeq;

    protected OrderSequenceEntity() {
    }

    public OrderSequenceEntity(String datePrefix, long nextSeq) {
        this.datePrefix = datePrefix;
        this.nextSeq = nextSeq;
    }

    public String getDatePrefix() {
        return datePrefix;
    }

    public long getNextSeq() {
        return nextSeq;
    }

    public void setNextSeq(long nextSeq) {
        this.nextSeq = nextSeq;
    }
}
