package org.example.service;

import org.example.model.MonitorItem;
import org.example.model.Status;
import org.example.repository.InMemoryRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class MonitorService {

    private final InMemoryRepository repository;

    public MonitorService(InMemoryRepository repository) {
        this.repository = repository;
    }

    public MonitorItem add(String name, String category, double value, Status status) {
        MonitorItem item = new MonitorItem(name, category, value, status, LocalDateTime.now());
        return repository.save(item);
    }

    public List<MonitorItem> listAll() {
        return repository.findAll();
    }

    public List<MonitorItem> listByStatus(Status status) {
        return repository.findByStatus(status);
    }

    public List<MonitorItem> listByCategory(String category) {
        return repository.findByCategory(category);
    }

    public Optional<MonitorItem> findById(long id) {
        return repository.findById(id);
    }

    public MonitorItem updateValue(long id, double newValue) {
        MonitorItem item = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("id not found: " + id));
        item.setValue(newValue);
        return item;
    }

    public MonitorItem updateStatus(long id, Status newStatus) {
        MonitorItem item = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("id not found: " + id));
        item.setStatus(newStatus);
        return item;
    }

    public boolean delete(long id) {
        return repository.deleteById(id);
    }

    public void seed() {
        repository.clear();
        new SeedData().seed(this);
    }

    public SummaryResult summarize() {
        List<MonitorItem> all = repository.findAll();
        long total = all.size();
        long active = all.stream().filter(i -> i.getStatus() == Status.ACTIVE).count();
        long inactive = all.stream().filter(i -> i.getStatus() == Status.INACTIVE).count();
        long warning = all.stream().filter(i -> i.getStatus() == Status.WARNING).count();
        double avg = total == 0 ? 0.0 : all.stream().mapToDouble(MonitorItem::getValue).average().orElse(0.0);
        return new SummaryResult(total, active, inactive, warning, avg);
    }
}
