package com.example.hexagonal.application;

import com.example.hexagonal.domain.GreetingRecord;
import java.util.List;

public interface GreetingQueryUseCase {
    List<GreetingRecord> list();
}
