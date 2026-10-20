package com.eventra.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * ConnectionRequest POJO representing a peer-to-peer networking invite between attendees.
 */
public class ConnectionRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int requesterId;
    private int receiverId;
    private String status; // PENDING, ACCEPTED, DECLINED
    private Timestamp createdAt;

    // View fields
    private String requesterName;
    private String receiverName;

    public ConnectionRequest() {
        this.status = "PENDING";
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }

    public ConnectionRequest(int id, int requesterId, int receiverId, String status, Timestamp createdAt) {
        this.id = id;
        this.requesterId = requesterId;
        this.receiverId = receiverId;
        this.status = status != null ? status : "PENDING";
        this.createdAt = createdAt != null ? createdAt : new Timestamp(System.currentTimeMillis());
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(int requesterId) {
        this.requesterId = requesterId;
    }

    public int getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(int receiverId) {
        this.receiverId = receiverId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getRequesterName() {
        return requesterName;
    }

    public void setRequesterName(String requesterName) {
        this.requesterName = requesterName;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConnectionRequest that = (ConnectionRequest) o;
        return id != 0 && id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ConnectionRequest{" +
                "id=" + id +
                ", requesterId=" + requesterId +
                ", receiverId=" + receiverId +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
