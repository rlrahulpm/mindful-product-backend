package com.productapp.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;

@Entity
@Table(name = "teams")
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "capacity_plan_id", nullable = false)
    private Long capacityPlanId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "capacity_plan_id", insertable = false, updatable = false)
    @JsonIgnore
    private CapacityPlan capacityPlan;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Team() {}

    public Team(String name, String description, Long capacityPlanId) {
        this.name = name;
        this.description = description;
        this.capacityPlanId = capacityPlanId;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Long getCapacityPlanId() {
        return capacityPlanId;
    }

    public void setCapacityPlanId(Long capacityPlanId) {
        this.capacityPlanId = capacityPlanId;
    }

    public CapacityPlan getCapacityPlan() {
        return capacityPlan;
    }

    public void setCapacityPlan(CapacityPlan capacityPlan) {
        this.capacityPlan = capacityPlan;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}