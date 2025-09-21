package com.productapp.service;

import com.productapp.entity.*;
import com.productapp.repository.*;
import com.productapp.dto.TeamRequest;
import com.productapp.dto.TeamMemberRequest;
import com.productapp.dto.ResourceAssignmentRequest;
import com.productapp.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ResourcePlanningService {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    @Autowired
    private ResourceAssignmentRepository resourceAssignmentRepository;

    @Autowired
    private BacklogEpicRepository backlogEpicRepository;

    @Autowired
    private UserStoryRepository userStoryRepository;

    @Autowired
    private ProductRepository productRepository;

    // Team management methods
    public Team createTeam(Long productId, TeamRequest request) {
        // Validate product exists
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found with id: " + productId);
        }

        // Check if team name already exists for this product
        if (teamRepository.existsByProductIdAndNameAndIsActiveTrue(productId, request.getName(), null)) {
            throw new RuntimeException("Team name already exists for this product");
        }

        Team team = new Team();
        team.setName(request.getName());
        team.setDescription(request.getDescription());
        team.setProductId(productId);
        team.setIsActive(true);

        return teamRepository.save(team);
    }

    public List<Team> getTeamsByProduct(Long productId) {
        return teamRepository.findByProductIdAndIsActiveTrue(productId);
    }

    public void deleteTeam(Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + teamId));

        team.setIsActive(false);
        teamRepository.save(team);
    }

    // Member management methods
    public TeamMember addMemberToTeam(Long teamId, TeamMemberRequest request) {
        // Validate team exists
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + teamId));

        if (!team.getIsActive()) {
            throw new RuntimeException("Cannot add members to inactive team");
        }

        TeamMember member = new TeamMember();
        member.setTeamId(teamId);
        member.setMemberName(request.getMemberName());
        member.setRole(request.getRole());
        member.setEmail(request.getEmail());

        return teamMemberRepository.save(member);
    }

    public List<TeamMember> getMembersByTeam(Long teamId) {
        return teamMemberRepository.findByTeamId(teamId);
    }

    public List<TeamMember> getMembersByProduct(Long productId) {
        return teamMemberRepository.findByProductId(productId);
    }

    public List<TeamMember> getAvailableMembers(Long productId, LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new RuntimeException("Start date must be before or equal to end date");
        }
        return teamMemberRepository.findAvailableMembers(productId, startDate, endDate);
    }

    public void deleteMember(Long memberId) {
        TeamMember member = teamMemberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Team member not found with id: " + memberId));

        // Check if member has any assignments
        List<ResourceAssignment> assignments = resourceAssignmentRepository.findConflictingAssignments(
                memberId, LocalDate.now(), LocalDate.now().plusYears(10));

        if (!assignments.isEmpty()) {
            throw new RuntimeException("Cannot delete member with existing assignments");
        }

        teamMemberRepository.delete(member);
    }

    // Integration with existing epic/story system
    public List<BacklogEpic> getPublishedEpics(Long productId) {
        return backlogEpicRepository.findByProductId(productId);
    }

    public List<UserStory> getUserStoriesByEpic(String epicId) {
        return userStoryRepository.findByEpicIdOrderByDisplayOrderAsc(epicId);
    }

    // Assignment methods
    public ResourceAssignment assignMemberToStory(Long productId, ResourceAssignmentRequest request) {
        // Validate dates
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new RuntimeException("Start date must be before or equal to end date");
        }

        // Validate user story exists
        UserStory userStory = userStoryRepository.findById(request.getUserStoryId())
                .orElseThrow(() -> new ResourceNotFoundException("User story not found with id: " + request.getUserStoryId()));

        // Validate member exists
        TeamMember member = teamMemberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new ResourceNotFoundException("Team member not found with id: " + request.getMemberId()));

        // Check if member is available during the requested period
        if (resourceAssignmentRepository.hasConflictingAssignment(
                request.getMemberId(), request.getStartDate(), request.getEndDate())) {
            throw new RuntimeException("Member is already assigned during this period");
        }

        ResourceAssignment assignment = new ResourceAssignment();
        assignment.setUserStoryId(request.getUserStoryId());
        assignment.setMemberId(request.getMemberId());
        assignment.setStartDate(request.getStartDate());
        assignment.setEndDate(request.getEndDate());
        assignment.setProductId(productId);

        return resourceAssignmentRepository.save(assignment);
    }

    public boolean isMemberAvailable(Long memberId, LocalDate startDate, LocalDate endDate) {
        return !resourceAssignmentRepository.hasConflictingAssignment(memberId, startDate, endDate);
    }

    public List<ResourceAssignment> getAssignmentsByEpic(String epicId) {
        return resourceAssignmentRepository.findByEpicId(epicId);
    }

    public List<ResourceAssignment> getAssignmentsByProduct(Long productId) {
        return resourceAssignmentRepository.findByProductIdWithDetails(productId);
    }

    public void deleteAssignment(Long assignmentId) {
        ResourceAssignment assignment = resourceAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + assignmentId));

        resourceAssignmentRepository.delete(assignment);
    }
}