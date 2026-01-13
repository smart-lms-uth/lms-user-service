package uth.edu.vn.lms_user_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uth.edu.vn.lms_user_service.dto.AdminUserDTO;
import uth.edu.vn.lms_user_service.dto.ApiResponse;
import uth.edu.vn.lms_user_service.dto.UpdateRoleRequest;
import uth.edu.vn.lms_user_service.dto.UserStatisticsResponse;
import uth.edu.vn.lms_user_service.dto.AdminUpdateUserRequest;
import uth.edu.vn.lms_user_service.dto.LockUserRequest;
import uth.edu.vn.lms_user_service.entity.Role;
import uth.edu.vn.lms_user_service.service.AdminService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "User management APIs for Admin")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users")
    @Operation(summary = "Get all users with pagination")
    public ResponseEntity<ApiResponse<Page<AdminUserDTO>>> getAllUsers(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getAllUsers(pageable)));
    }

    @GetMapping("/users/role/{role}")
    @Operation(summary = "Get users by role")
    public ResponseEntity<ApiResponse<List<AdminUserDTO>>> getUsersByRole(@PathVariable Role role) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getUsersByRole(role)));
    }

    @GetMapping("/users/search")
    @Operation(summary = "Search users by keyword")
    public ResponseEntity<ApiResponse<List<AdminUserDTO>>> searchUsers(@RequestParam String keyword) {
        return ResponseEntity.ok(ApiResponse.success(adminService.searchUsers(keyword)));
    }

    @GetMapping("/users/{id}")
    @Operation(summary = "Get user details by ID")
    public ResponseEntity<ApiResponse<AdminUserDTO>> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getUserById(id)));
    }

    @PutMapping("/users/{id}/role")
    @Operation(summary = "Update user role")
    public ResponseEntity<ApiResponse<AdminUserDTO>> updateUserRole(
            @PathVariable Long id,
            @RequestBody UpdateRoleRequest request) {
        AdminUserDTO updated = adminService.updateUserRole(id, request.role());
        return ResponseEntity.ok(ApiResponse.success("Role updated to " + request.role(), updated));
    }

    @PatchMapping("/users/{id}/toggle-enabled")
    @Operation(summary = "Toggle user enabled status")
    public ResponseEntity<ApiResponse<AdminUserDTO>> toggleUserEnabled(@PathVariable Long id) {
        AdminUserDTO updated = adminService.toggleUserEnabled(id);
        String message = updated.enabled() ? "User enabled" : "User disabled";
        return ResponseEntity.ok(ApiResponse.success(message, updated));
    }

    @DeleteMapping("/users/{id}")
    @Operation(summary = "Delete user (soft delete)")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted", null));
    }

    @PutMapping("/users/{id}")
    @Operation(summary = "Update user information")
    public ResponseEntity<ApiResponse<AdminUserDTO>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody AdminUpdateUserRequest request) {
        AdminUserDTO updated = adminService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success("User updated", updated));
    }

    @PostMapping("/users/{id}/lock")
    @Operation(summary = "Lock user account")
    public ResponseEntity<ApiResponse<AdminUserDTO>> lockUser(
            @PathVariable Long id,
            @RequestBody LockUserRequest request) {
        AdminUserDTO updated = adminService.lockUser(id, request.reason());
        return ResponseEntity.ok(ApiResponse.success("User account locked", updated));
    }

    @PostMapping("/users/{id}/unlock")
    @Operation(summary = "Unlock user account")
    public ResponseEntity<ApiResponse<AdminUserDTO>> unlockUser(@PathVariable Long id) {
        AdminUserDTO updated = adminService.unlockUser(id);
        return ResponseEntity.ok(ApiResponse.success("User account unlocked", updated));
    }

    @DeleteMapping("/users/{id}/hard")
    @Operation(summary = "Permanently delete user (hard delete)")
    public ResponseEntity<ApiResponse<Void>> hardDeleteUser(@PathVariable Long id) {
        adminService.hardDeleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User permanently deleted", null));
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get user statistics")
    public ResponseEntity<ApiResponse<UserStatisticsResponse>> getUserStatistics() {
        return ResponseEntity.ok(ApiResponse.success(adminService.getUserStatistics()));
    }

    @GetMapping("/roles")
    @Operation(summary = "Get all available roles")
    public ResponseEntity<ApiResponse<Role[]>> getAllRoles() {
        return ResponseEntity.ok(ApiResponse.success(Role.values()));
    }
}
