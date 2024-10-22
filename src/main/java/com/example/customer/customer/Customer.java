package com.example.customer;
public class Customer{
    private String customerId;
    private String workloadId;
    private long timestamp;
    private String eventType;

    public Customer() {
    }

    public Customer(String customerId, String workloadId, long timestamp, String eventType){
        this.customerId = customerId;
        this.workloadId = workloadId;
        this.timestamp = timestamp;
        this.eventType = eventType;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getWorkloadId() {
        return workloadId;
    }

    public void setWorkloadId(String workloadId) {
        this.workloadId = workloadId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
}