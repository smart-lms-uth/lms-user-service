package uth.edu.vn.lms_user_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import uth.edu.vn.lms_user_service.dto.ApiResponse;
import uth.edu.vn.lms_user_service.dto.UpdateProfileRequest;
import uth.edu.vn.lms_user_service.dto.UserResponse;
import uth.edu.vn.lms_user_service.entity.User;
import uth.edu.vn.lms_user_service.service.UserService;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User Management", description = "User profile and management APIs")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    @Operation(summary = "Get current user profile")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<ApiResponse<UserResponse>> getProfile(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        UserResponse profile = userService.getProfile(user.getId());
        return ResponseEntity.ok(ApiResponse.success("Profile retrieved successfully", profile));
    }

    @PutMapping("/profile")
    @Operation(summary = "Update current user profile")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Profile updated successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request) {
        User user = (User) authentication.getPrincipal();
        UserResponse updatedProfile = userService.updateProfile(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", updatedProfile));
    }

    @PostMapping("/profile/complete")
    @Operation(summary = "Complete profile setup (for first-time users)")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Profile setup completed"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<ApiResponse<UserResponse>> completeProfileSetup(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request) {
        User user = (User) authentication.getPrincipal();
        UserResponse completedProfile = userService.completeProfileSetup(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Profile setup completed successfully", completedProfile));
    }

    @GetMapping("/profile/status")
    @Operation(summary = "Check if profile setup is complete")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Status retrieved"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<ApiResponse<ProfileStatusResponse>> getProfileStatus(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        boolean isComplete = userService.isProfileComplete(user.getId());
        return ResponseEntity.ok(ApiResponse.success(new ProfileStatusResponse(isComplete)));
    }

    public record ProfileStatusResponse(boolean profileComplete) {}
}
