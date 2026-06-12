package org.example.service;

import org.example.model.MonitorItem;
import org.example.model.Status;
import org.example.repository.InMemoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class MonitorServiceTest {

    private MonitorService service;

    @BeforeEach
    void setUp() {
        service = new MonitorService(new InMemoryRepository());
    }

    @Test
    @DisplayName("add: 항목을 추가하면 id가 부여된 MonitorItem을 반환한다")
    void add_returnsItemWithId() {
        MonitorItem item = service.add("cpu", "system", 45.0, Status.ACTIVE);

        assertTrue(item.getId() > 0);
        assertEquals("cpu", item.getName());
        assertEquals("system", item.getCategory());
        assertEquals(45.0, item.getValue());
        assertEquals(Status.ACTIVE, item.getStatus());
        assertNotNull(item.getCreatedAt());
    }

    @Test
    @DisplayName("listAll: 추가된 모든 항목을 반환한다")
    void listAll_returnsAllItems() {
        service.add("cpu", "system", 45.0, Status.ACTIVE);
        service.add("memory", "system", 80.0, Status.WARNING);

        List<MonitorItem> result = service.listAll();

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("listByStatus: 해당 상태의 항목만 반환한다")
    void listByStatus_filtersCorrectly() {
        service.add("cpu", "system", 45.0, Status.ACTIVE);
        service.add("memory", "system", 80.0, Status.WARNING);
        service.add("disk", "storage", 90.0, Status.WARNING);

        List<MonitorItem> result = service.listByStatus(Status.WARNING);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(i -> i.getStatus() == Status.WARNING));
    }

    @Test
    @DisplayName("listByCategory: 해당 카테고리 항목만 반환한다 (대소문자 무시)")
    void listByCategory_filtersCorrectly() {
        service.add("net-in", "Network", 100.0, Status.ACTIVE);
        service.add("net-out", "network", 90.0, Status.ACTIVE);
        service.add("cpu", "system", 45.0, Status.ACTIVE);

        List<MonitorItem> result = service.listByCategory("NETWORK");

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("findById: 존재하는 id로 조회하면 해당 항목을 반환한다")
    void findById_returnsItem() {
        MonitorItem added = service.add("cpu", "system", 45.0, Status.ACTIVE);

        Optional<MonitorItem> result = service.findById(added.getId());

        assertTrue(result.isPresent());
        assertEquals("cpu", result.get().getName());
    }

    @Test
    @DisplayName("findById: 존재하지 않는 id는 Optional.empty()를 반환한다")
    void findById_returnsEmpty() {
        Optional<MonitorItem> result = service.findById(999L);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("updateValue: value를 변경하면 변경된 값이 반영된다")
    void updateValue_changesValue() {
        MonitorItem added = service.add("cpu", "system", 45.0, Status.ACTIVE);

        MonitorItem updated = service.updateValue(added.getId(), 99.9);

        assertEquals(99.9, updated.getValue());
    }

    @Test
    @DisplayName("updateValue: 존재하지 않는 id이면 예외가 발생한다")
    void updateValue_throwsWhenNotFound() {
        assertThrows(IllegalArgumentException.class, () -> service.updateValue(999L, 50.0));
    }

    @Test
    @DisplayName("updateStatus: status를 변경하면 변경된 상태가 반영된다")
    void updateStatus_changesStatus() {
        MonitorItem added = service.add("cpu", "system", 45.0, Status.ACTIVE);

        MonitorItem updated = service.updateStatus(added.getId(), Status.INACTIVE);

        assertEquals(Status.INACTIVE, updated.getStatus());
    }

    @Test
    @DisplayName("updateStatus: 존재하지 않는 id이면 예외가 발생한다")
    void updateStatus_throwsWhenNotFound() {
        assertThrows(IllegalArgumentException.class, () -> service.updateStatus(999L, Status.INACTIVE));
    }

    @Test
    @DisplayName("delete: 항목 삭제 후 listAll에서 제거된다")
    void delete_removesItem() {
        MonitorItem added = service.add("cpu", "system", 45.0, Status.ACTIVE);

        boolean deleted = service.delete(added.getId());

        assertTrue(deleted);
        assertTrue(service.listAll().isEmpty());
    }

    @Test
    @DisplayName("summarize: 전체 건수·상태별 건수·평균값을 반환한다")
    void summarize_returnsCorrectCounts() {
        service.add("cpu", "system", 40.0, Status.ACTIVE);
        service.add("memory", "system", 80.0, Status.WARNING);
        service.add("disk", "storage", 60.0, Status.INACTIVE);

        SummaryResult result = service.summarize();

        assertEquals(3, result.getTotal());
        assertEquals(1, result.getActiveCount());
        assertEquals(1, result.getInactiveCount());
        assertEquals(1, result.getWarningCount());
        assertEquals(60.0, result.getAverageValue(), 0.001);
    }
}
