package com.productapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.productapp.dto.AuthResponse;
import com.productapp.dto.LoginRequest;
import com.productapp.entity.User;
import com.productapp.repository.UserRepository;
import com.productapp.security.UserPrincipal;
import com.productapp.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    // In-memory storage for reset tokens (in production, use database with expiry)
    private static final Map<String, Long> resetTokens = new ConcurrentHashMap<>();
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    // Note: User signup is disabled - users are created by admins only
    
    @PostMapping("/login")
    @Operation(summary = "Login user", description = "Authenticate user and return JWT token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful",
                content = @Content(schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(responseCode = "401", description = "Invalid credentials",
                content = @Content)
    })
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElse(null);
        
        if (user == null || !passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            logger.warn("Login failed for email: {}", loginRequest.getEmail());
            throw new org.springframework.security.authentication.BadCredentialsException("Invalid email or password");
        }
        
        String jwt = jwtUtil.generateJwtToken(user.getEmail(), user.getId(), 
            user.getOrganization().getId(), user.getIsSuperadmin(), user.getIsGlobalSuperadmin());
        
        return ResponseEntity.ok(new AuthResponse(jwt, user.getId(), user.getEmail(), user.getIsSuperadmin()));
    }
    
    @PostMapping("/refresh")
    @Operation(summary = "Refresh JWT token", description = "Refresh the current user's JWT token")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token refreshed successfully",
                content = @Content(schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(responseCode = "401", description = "Invalid or expired token",
                content = @Content)
    })
    public ResponseEntity<?> refreshToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        String newToken = jwtUtil.generateJwtToken(user.getEmail(), user.getId(), 
            user.getOrganization().getId(), user.getIsSuperadmin(), user.getIsGlobalSuperadmin());
        
        return ResponseEntity.ok(new AuthResponse(newToken, user.getId(), user.getEmail(), user.getIsSuperadmin()));
    }
    
    @PostMapping("/logout")
    @Operation(summary = "Logout user", description = "Logout the current user")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Logout successful",
                content = @Content(schema = @Schema(implementation = AuthResponse.class)))
    })
    public ResponseEntity<?> logoutUser(Authentication authentication) {
        return ResponseEntity.ok(new AuthResponse("User logged out successfully"));
    }

    @GetMapping("/verify-token/{token}")
    @Operation(summary = "Verify reset token", description = "Verify if a password reset token is valid")
    public ResponseEntity<?> verifyToken(@PathVariable String token) {
        Long userId = resetTokens.get(token);

        Map<String, Object> response = new HashMap<>();

        if (userId == null) {
            response.put("valid", false);
            response.put("message", "Invalid or expired token");
            return ResponseEntity.ok(response);
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            resetTokens.remove(token);
            response.put("valid", false);
            response.put("message", "User not found");
            return ResponseEntity.ok(response);
        }

        response.put("valid", true);
        response.put("email", user.getEmail());
        response.put("tokenType", "SETUP");
        response.put("userId", userId);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/set-password")
    @Operation(summary = "Set password using token", description = "Set user password using a reset token")
    public ResponseEntity<?> setPassword(@Valid @RequestBody Map<String, String> request) {
        String token = request.get("token");
        String password = request.get("password");

        if (token == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Token and password are required"));
        }

        Long userId = resetTokens.get(token);
        if (userId == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid or expired token"));
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Set the new password
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);

        // Remove the used token
        resetTokens.remove(token);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Password set successfully");
        response.put("email", user.getEmail());

        return ResponseEntity.ok(response);
    }

    // Method to store reset token (called by AdminController)
    public static void storeResetToken(String token, Long userId) {
        resetTokens.put(token, userId);
    }
}