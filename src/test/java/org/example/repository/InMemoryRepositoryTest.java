package org.example.repository;

import org.example.model.MonitorItem;
import org.example.model.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryRepositoryTest {

    private InMemoryRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryRepository();
    }

    @Test
    @DisplayName("저장 시 id가 1부터 순차 증가한다")
    void save_assignsIncrementalId() {
        MonitorItem first = new MonitorItem("cpu", "system", 10.0, Status.ACTIVE, LocalDateTime.now());
        MonitorItem second = new MonitorItem("memory", "system", 20.0, Status.ACTIVE, LocalDateTime.now());

        repository.save(first);
        repository.save(second);

        assertEquals(1L, first.getId());
        assertEquals(2L, second.getId());
    }

    @Test
    @DisplayName("존재하는 id로 조회하면 해당 항목을 반환한다")
    void findById_returnsItem() {
        MonitorItem item = new MonitorItem("cpu", "system", 10.0, Status.ACTIVE, LocalDateTime.now());
        repository.save(item);

        Optional<MonitorItem> result = repository.findById(1L);

        assertTrue(result.isPresent());
        assertEquals("cpu", result.get().getName());
    }

    @Test
    @DisplayName("존재하지 않는 id로 조회하면 Optional.empty()를 반환한다")
    void findById_returnsEmpty() {
        Optional<MonitorItem> result = repository.findById(999L);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("상태로 필터링하면 해당 상태의 항목만 반환한다")
    void findByStatus_filtersCorrectly() {
        repository.save(new MonitorItem("cpu", "system", 10.0, Status.ACTIVE, LocalDateTime.now()));
        repository.save(new MonitorItem("memory", "system", 80.0, Status.WARNING, LocalDateTime.now()));
        repository.save(new MonitorItem("disk", "system", 50.0, Status.WARNING, LocalDateTime.now()));

        List<MonitorItem> result = repository.findByStatus(Status.WARNING);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(i -> i.getStatus() == Status.WARNING));
    }

    @Test
    @DisplayName("카테고리 필터는 대소문자를 구분하지 않는다")
    void findByCategory_caseInsensitive() {
        repository.save(new MonitorItem("net-in", "Network", 100.0, Status.ACTIVE, LocalDateTime.now()));
        repository.save(new MonitorItem("net-out", "network", 90.0, Status.ACTIVE, LocalDateTime.now()));
        repository.save(new MonitorItem("cpu", "system", 45.0, Status.ACTIVE, LocalDateTime.now()));

        List<MonitorItem> result = repository.findByCategory("NETWORK");

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("삭제 후 findAll 목록에서 해당 항목이 제거된다")
    void deleteById_removesItem() {
        MonitorItem item = new MonitorItem("cpu", "system", 10.0, Status.ACTIVE, LocalDateTime.now());
        repository.save(item);

        boolean deleted = repository.deleteById(1L);

        assertTrue(deleted);
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    @DisplayName("존재하지 않는 id 삭제 시 false를 반환한다")
    void deleteById_returnsFalse_whenNotFound() {
        boolean result = repository.deleteById(999L);

        assertFalse(result);
    }

    @Test
    @DisplayName("clear 호출 후 findAll은 빈 리스트를 반환한다")
    void clear_removesAll() {
        repository.save(new MonitorItem("cpu", "system", 10.0, Status.ACTIVE, LocalDateTime.now()));
        repository.save(new MonitorItem("memory", "system", 80.0, Status.WARNING, LocalDateTime.now()));

        repository.clear();

        assertTrue(repository.findAll().isEmpty());
    }
}
