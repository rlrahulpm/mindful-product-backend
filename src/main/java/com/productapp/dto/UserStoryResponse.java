package com.productapp.dto;

import com.productapp.entity.UserStory;
import java.time.LocalDateTime;

public class UserStoryResponse {
    private Long id;
    private String epicId;
    private Long productId;
    private String title;
    private String description;
    private String acceptanceCriteria;
    private String priority;
    private Integer storyPoints;
    private String status;
    private Integer displayOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;

    // Constructors
    public UserStoryResponse() {
    }

    public UserStoryResponse(UserStory userStory) {
        this.id = userStory.getId();
        this.epicId = userStory.getEpicId();
        this.productId = userStory.getProduct() != null ? userStory.getProduct().getId() : null;
        this.title = userStory.getTitle();
        this.description = userStory.getDescription();
        this.acceptanceCriteria = userStory.getAcceptanceCriteria();
        this.priority = userStory.getPriority();
        this.storyPoints = userStory.getStoryPoints();
        this.status = userStory.getStatus();
        this.displayOrder = userStory.getDisplayOrder();
        this.createdAt = userStory.getCreatedAt();
        this.updatedAt = userStory.getUpdatedAt();
        this.createdBy = userStory.getCreatedBy();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEpicId() {
        return epicId;
    }

    public void setEpicId(String epicId) {
        this.epicId = epicId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAcceptanceCriteria() {
        return acceptanceCriteria;
    }

    public void setAcceptanceCriteria(String acceptanceCriteria) {
        this.acceptanceCriteria = acceptanceCriteria;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public Integer getStoryPoints() {
        return storyPoints;
    }

    public void setStoryPoints(Integer storyPoints) {
        this.storyPoints = storyPoints;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
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

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }
}