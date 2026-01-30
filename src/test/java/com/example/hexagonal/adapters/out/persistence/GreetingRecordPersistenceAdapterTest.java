package com.example.hexagonal.adapters.out.persistence;

import com.example.hexagonal.domain.GreetingRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(GreetingRecordPersistenceAdapter.class)
class GreetingRecordPersistenceAdapterTest {

    @Autowired
    private GreetingRecordPersistenceAdapter adapter;

    @Test
    void save_and_findAll_round_trip() {
        adapter.save("hello world");

        List<GreetingRecord> records = adapter.findAll();

        assertThat(records).hasSize(1);
        assertThat(records.get(0).getMessage()).isEqualTo("hello world");
        assertThat(records.get(0).getId()).isNotNull();
        assertThat(records.get(0).getCreatedAt()).isNotNull();
    }
}
