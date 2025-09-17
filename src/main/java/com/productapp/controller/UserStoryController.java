package com.productapp.controller;

import com.productapp.dto.UserStoryRequest;
import com.productapp.dto.UserStoryResponse;
import com.productapp.entity.UserStory;
import com.productapp.entity.Product;
import com.productapp.repository.ProductRepository;
import com.productapp.service.UserStoryService;
import com.productapp.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v3/products/{productId}")
@Tag(name = "User Story", description = "User story management for epics")
public class UserStoryController {

    private static final Logger logger = LoggerFactory.getLogger(UserStoryController.class);

    @Autowired
    private UserStoryService userStoryService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private Long getUserIdFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return jwtUtil.getUserIdFromJwtToken(token);
    }

    private boolean isProductOwner(Long productId, Long userId) {
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty()) {
            return false;
        }
        Product product = productOpt.get();
        return product.getUser().getId().equals(userId);
    }

    @GetMapping("/epics/{epicId}/user-stories")
    @Operation(summary = "Get all user stories for an epic", description = "Retrieve all user stories associated with a specific epic")
    public ResponseEntity<?> getStoriesByEpic(
            @PathVariable Long productId,
            @PathVariable String epicId,
            HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);

            if (!isProductOwner(productId, userId)) {
                logger.warn("User ID: {} attempted to access stories for product ID: {} they don't own", userId, productId);
                return ResponseEntity.status(403).body("Access denied");
            }

            List<UserStory> stories = userStoryService.getStoriesByProductAndEpic(productId, epicId);
            List<UserStoryResponse> responses = stories.stream()
                    .map(UserStoryResponse::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            logger.error("Error fetching user stories for epic ID: {}", epicId, e);
            return ResponseEntity.status(500).body("Error fetching user stories: " + e.getMessage());
        }
    }

    @PostMapping("/epics/{epicId}/user-stories")
    @Operation(summary = "Create a new user story", description = "Create a new user story for a specific epic")
    @Transactional
    public ResponseEntity<?> createUserStory(
            @PathVariable Long productId,
            @PathVariable String epicId,
            @RequestBody UserStoryRequest request,
            HttpServletRequest httpRequest) {
        try {
            Long userId = getUserIdFromRequest(httpRequest);

            if (!isProductOwner(productId, userId)) {
                logger.warn("User ID: {} attempted to create story for product ID: {} they don't own", userId, productId);
                return ResponseEntity.status(403).body("Access denied");
            }

            if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Story title is required");
            }

            UserStory userStory = userStoryService.createUserStory(
                    productId,
                    epicId,
                    request.getTitle(),
                    request.getDescription(),
                    request.getAcceptanceCriteria(),
                    request.getPriority(),
                    request.getStoryPoints(),
                    userId
            );

            UserStoryResponse response = new UserStoryResponse(userStory);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid request for creating user story: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error creating user story for epic ID: {}", epicId, e);
            return ResponseEntity.status(500).body("Error creating user story: " + e.getMessage());
        }
    }

    @PutMapping("/user-stories/{storyId}")
    @Operation(summary = "Update a user story", description = "Update an existing user story")
    @Transactional
    public ResponseEntity<?> updateUserStory(
            @PathVariable Long productId,
            @PathVariable Long storyId,
            @RequestBody UserStoryRequest request,
            HttpServletRequest httpRequest) {
        try {
            Long userId = getUserIdFromRequest(httpRequest);

            if (!isProductOwner(productId, userId)) {
                logger.warn("User ID: {} attempted to update story for product ID: {} they don't own", userId, productId);
                return ResponseEntity.status(403).body("Access denied");
            }

            UserStory userStory = userStoryService.updateUserStory(
                    storyId,
                    productId,
                    request.getTitle(),
                    request.getDescription(),
                    request.getAcceptanceCriteria(),
                    request.getPriority(),
                    request.getStoryPoints(),
                    request.getStatus()
            );

            UserStoryResponse response = new UserStoryResponse(userStory);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid request for updating user story: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error updating user story ID: {}", storyId, e);
            return ResponseEntity.status(500).body("Error updating user story: " + e.getMessage());
        }
    }

    @DeleteMapping("/user-stories/{storyId}")
    @Operation(summary = "Delete a user story", description = "Delete an existing user story")
    @Transactional
    public ResponseEntity<?> deleteUserStory(
            @PathVariable Long productId,
            @PathVariable Long storyId,
            HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);

            if (!isProductOwner(productId, userId)) {
                logger.warn("User ID: {} attempted to delete story for product ID: {} they don't own", userId, productId);
                return ResponseEntity.status(403).body("Access denied");
            }

            userStoryService.deleteUserStory(storyId, productId);
            return ResponseEntity.ok().body("User story deleted successfully");

        } catch (IllegalArgumentException e) {
            logger.error("Invalid request for deleting user story: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error deleting user story ID: {}", storyId, e);
            return ResponseEntity.status(500).body("Error deleting user story: " + e.getMessage());
        }
    }

    @GetMapping("/user-stories")
    @Operation(summary = "Get all user stories for a product", description = "Retrieve all user stories for a specific product")
    public ResponseEntity<?> getAllStoriesByProduct(
            @PathVariable Long productId,
            HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);

            if (!isProductOwner(productId, userId)) {
                logger.warn("User ID: {} attempted to access all stories for product ID: {} they don't own", userId, productId);
                return ResponseEntity.status(403).body("Access denied");
            }

            Optional<Product> productOpt = productRepository.findById(productId);
            if (productOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            List<UserStory> stories = userStoryService.getAllStoriesByProduct(productOpt.get());
            List<UserStoryResponse> responses = stories.stream()
                    .map(UserStoryResponse::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            logger.error("Error fetching all user stories for product ID: {}", productId, e);
            return ResponseEntity.status(500).body("Error fetching user stories: " + e.getMessage());
        }
    }
}