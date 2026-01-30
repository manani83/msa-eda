package com.example.hexagonal.application.port.out;

import com.example.hexagonal.domain.GreetingRecord;
import java.util.List;

public interface GreetingRecordQueryPort {
    List<GreetingRecord> findAll();
}
