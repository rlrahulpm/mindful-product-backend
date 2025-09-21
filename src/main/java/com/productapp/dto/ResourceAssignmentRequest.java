package com.productapp.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Future;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public class ResourceAssignmentRequest {

    @NotNull(message = "User story ID is required")
    private Long userStoryId;

    @NotNull(message = "Member ID is required")
    private Long memberId;

    @NotNull(message = "Start date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    public ResourceAssignmentRequest() {}

    public ResourceAssignmentRequest(Long userStoryId, Long memberId, LocalDate startDate, LocalDate endDate) {
        this.userStoryId = userStoryId;
        this.memberId = memberId;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Long getUserStoryId() {
        return userStoryId;
    }

    public void setUserStoryId(Long userStoryId) {
        this.userStoryId = userStoryId;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}