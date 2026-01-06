package uth.edu.vn.lms_user_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uth.edu.vn.lms_user_service.dto.ApiResponse;
import uth.edu.vn.lms_user_service.dto.UserResponse;
import uth.edu.vn.lms_user_service.service.UserService;

import java.util.List;

/**
 * Internal API Controller for inter-service communication.
 * These endpoints are called by other microservices (e.g., course-service via OpenFeign).
 * All endpoints require JWT authentication to ensure security.
 */
@RestController
@RequestMapping("/api/v1/internal/users")
@Tag(name = "Internal API", description = "Internal APIs for inter-service communication")
@SecurityRequirement(name = "bearerAuth")
public class InternalApiController {

    private final UserService userService;

    public InternalApiController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Internal API to get user information by ID")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", user));
    }

    @GetMapping("/batch")
    @Operation(summary = "Get multiple users by IDs", 
               description = "Internal API to get multiple users at once (batch lookup)")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Users retrieved"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<ApiResponse<List<UserResponse>>> getUsersByIds(
            @RequestParam List<Long> ids) {
        List<UserResponse> users = userService.getUsersByIds(ids);
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", users));
    }

    @GetMapping("/teachers")
    @Operation(summary = "Get all active teachers", 
               description = "Internal API to get all users with TEACHER role")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Teachers retrieved"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllTeachers() {
        List<UserResponse> teachers = userService.getAllTeachers();
        return ResponseEntity.ok(ApiResponse.success("Teachers retrieved successfully", teachers));
    }

    @GetMapping("/students")
    @Operation(summary = "Get all active students", 
               description = "Internal API to get all users with STUDENT role")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Students retrieved"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllStudents() {
        List<UserResponse> students = userService.getAllStudents();
        return ResponseEntity.ok(ApiResponse.success("Students retrieved successfully", students));
    }
}
