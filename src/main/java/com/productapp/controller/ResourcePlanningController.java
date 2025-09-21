package com.productapp.controller;

import com.productapp.entity.*;
import com.productapp.service.ResourcePlanningService;
import com.productapp.repository.ProductRepository;
import com.productapp.dto.TeamRequest;
import com.productapp.dto.TeamMemberRequest;
import com.productapp.dto.ResourceAssignmentRequest;
import com.productapp.util.SlugUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v3/products/{productId}/resource-planning")
@CrossOrigin(origins = "*")
public class ResourcePlanningController {

    @Autowired
    private ResourcePlanningService resourcePlanningService;

    @Autowired
    private ProductRepository productRepository;

    // Teams endpoints
    @PostMapping("/teams")
    public ResponseEntity<Team> createTeam(@PathVariable Long productId,
                                         @Valid @RequestBody TeamRequest request) {
        try {
            Team team = resourcePlanningService.createTeam(productId, request);
            return ResponseEntity.ok(team);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/teams")
    public ResponseEntity<List<Team>> getTeams(@PathVariable Long productId) {
        try {
            List<Team> teams = resourcePlanningService.getTeamsByProduct(productId);
            return ResponseEntity.ok(teams);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("/teams/{teamId}")
    public ResponseEntity<Void> deleteTeam(@PathVariable Long productId,
                                         @PathVariable Long teamId) {
        try {
            resourcePlanningService.deleteTeam(teamId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // Members endpoints
    @PostMapping("/teams/{teamId}/members")
    public ResponseEntity<TeamMember> addMember(@PathVariable Long productId,
                                              @PathVariable Long teamId,
                                              @Valid @RequestBody TeamMemberRequest request) {
        try {
            TeamMember member = resourcePlanningService.addMemberToTeam(teamId, request);
            return ResponseEntity.ok(member);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/teams/{teamId}/members")
    public ResponseEntity<List<TeamMember>> getTeamMembers(@PathVariable Long productId,
                                                          @PathVariable Long teamId) {
        try {
            List<TeamMember> members = resourcePlanningService.getMembersByTeam(teamId);
            return ResponseEntity.ok(members);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/members")
    public ResponseEntity<List<TeamMember>> getAllMembers(@PathVariable Long productId) {
        try {
            List<TeamMember> members = resourcePlanningService.getMembersByProduct(productId);
            return ResponseEntity.ok(members);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/members/available")
    public ResponseEntity<List<TeamMember>> getAvailableMembers(@PathVariable Long productId,
                                                               @RequestParam String startDate,
                                                               @RequestParam String endDate) {
        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);

            List<TeamMember> members = resourcePlanningService.getAvailableMembers(
                    productId, start, end);
            return ResponseEntity.ok(members);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @DeleteMapping("/members/{memberId}")
    public ResponseEntity<Void> deleteMember(@PathVariable Long productId,
                                           @PathVariable Long memberId) {
        try {
            resourcePlanningService.deleteMember(memberId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // Integration endpoints - Use existing epic/story data
    @GetMapping("/epics")
    public ResponseEntity<List<BacklogEpic>> getPublishedEpics(@PathVariable Long productId) {
        try {
            List<BacklogEpic> epics = resourcePlanningService.getPublishedEpics(productId);
            return ResponseEntity.ok(epics);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/epics/{epicId}/user-stories")
    public ResponseEntity<List<UserStory>> getUserStoriesByEpic(@PathVariable Long productId,
                                                               @PathVariable String epicId) {
        try {
            List<UserStory> userStories = resourcePlanningService.getUserStoriesByEpic(epicId);
            return ResponseEntity.ok(userStories);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // Assignment endpoints
    @PostMapping("/assignments")
    public ResponseEntity<?> createAssignment(@PathVariable Long productId,
                                                              @Valid @RequestBody ResourceAssignmentRequest request) {
        try {
            System.out.println("Creating assignment with request: " + request);
            System.out.println("UserStoryId: " + request.getUserStoryId());
            System.out.println("MemberId: " + request.getMemberId());
            System.out.println("StartDate: " + request.getStartDate());
            System.out.println("EndDate: " + request.getEndDate());

            ResourceAssignment assignment = resourcePlanningService.assignMemberToStory(
                    productId, request);
            return ResponseEntity.ok(assignment);
        } catch (Exception e) {
            System.err.println("Error creating assignment: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/assignments/epic/{epicId}")
    public ResponseEntity<List<ResourceAssignment>> getEpicAssignments(@PathVariable Long productId,
                                                                      @PathVariable String epicId) {
        try {
            List<ResourceAssignment> assignments = resourcePlanningService.getAssignmentsByEpic(epicId);
            return ResponseEntity.ok(assignments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/assignments")
    public ResponseEntity<List<ResourceAssignment>> getAllAssignments(@PathVariable Long productId) {
        try {
            List<ResourceAssignment> assignments = resourcePlanningService.getAssignmentsByProduct(productId);
            return ResponseEntity.ok(assignments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("/assignments/{assignmentId}")
    public ResponseEntity<Void> deleteAssignment(@PathVariable Long productId,
                                                @PathVariable Long assignmentId) {
        try {
            resourcePlanningService.deleteAssignment(assignmentId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}