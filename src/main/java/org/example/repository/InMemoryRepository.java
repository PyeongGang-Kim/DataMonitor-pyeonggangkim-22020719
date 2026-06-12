package org.example.repository;

import org.example.model.MonitorItem;
import org.example.model.Status;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryRepository {

    private final List<MonitorItem> store = new ArrayList<>();
    private final AtomicLong idSequence = new AtomicLong(0);

    public MonitorItem save(MonitorItem item) {
        item.setId(idSequence.incrementAndGet());
        store.add(item);
        return item;
    }

    public Optional<MonitorItem> findById(long id) {
        return store.stream()
                .filter(item -> item.getId() == id)
                .findFirst();
    }

    public List<MonitorItem> findAll() {
        return new ArrayList<>(store);
    }

    public List<MonitorItem> findByStatus(Status status) {
        return store.stream()
                .filter(item -> item.getStatus() == status)
                .toList();
    }

    public List<MonitorItem> findByCategory(String category) {
        return store.stream()
                .filter(item -> item.getCategory().equalsIgnoreCase(category))
                .toList();
    }

    public boolean deleteById(long id) {
        return store.removeIf(item -> item.getId() == id);
    }

    public void clear() {
        store.clear();
        idSequence.set(0);
    }
}
