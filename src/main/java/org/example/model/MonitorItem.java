package org.example.model;

import java.time.LocalDateTime;

public class MonitorItem {

    private long id;
    private String name;
    private String category;
    private double value;
    private Status status;
    private LocalDateTime createdAt;

    public MonitorItem(String name, String category, double value, Status status, LocalDateTime createdAt) {
        this.name = name;
        this.category = category;
        this.value = value;
        this.status = status;
        this.createdAt = createdAt;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
