package com.productapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;

public class TeamMemberRequest {

    @NotBlank(message = "Member name is required")
    private String memberName;

    private String role;

    @Email(message = "Invalid email format")
    private String email;

    public TeamMemberRequest() {}

    public TeamMemberRequest(String memberName, String role, String email) {
        this.memberName = memberName;
        this.role = role;
        this.email = email;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}