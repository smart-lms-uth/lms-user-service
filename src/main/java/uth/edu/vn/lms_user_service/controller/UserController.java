package uth.edu.vn.lms_user_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uth.edu.vn.lms_user_service.dto.*;
import uth.edu.vn.lms_user_service.entity.User;
import uth.edu.vn.lms_user_service.service.UserService;
import uth.edu.vn.lms_user_service.service.FileStorageService;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User Management", description = "User profile and management APIs")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;
    private final FileStorageService fileStorageService;

    public UserController(UserService userService, FileStorageService fileStorageService) {
        this.userService = userService;
        this.fileStorageService = fileStorageService;
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
        boolean hasPassword = userService.hasPassword(user.getId());
        return ResponseEntity.ok(ApiResponse.success(new ProfileStatusResponse(isComplete, hasPassword)));
    }

    @PostMapping("/profile/set-password")
    @Operation(summary = "Set password for OAuth account (allows login with username/password)")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Password set successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid password or already has password"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<ApiResponse<String>> setPassword(
            Authentication authentication,
            @Valid @RequestBody SetPasswordRequest request) {
        User user = (User) authentication.getPrincipal();
        userService.setPassword(user.getId(), request.password());
        return ResponseEntity.ok(ApiResponse.success("Password set successfully. You can now login with username/password."));
    }

    @PostMapping("/profile/change-password")
    @Operation(summary = "Change password (requires current password)")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Password changed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid current password or new password"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<ApiResponse<String>> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request) {
        User user = (User) authentication.getPrincipal();
        userService.changePassword(user.getId(), request.currentPassword(), request.newPassword());
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully."));
    }

    @PostMapping(value = "/profile/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload avatar image")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Avatar uploaded successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid file format or size"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<ApiResponse<AvatarResponse>> uploadAvatar(
            Authentication authentication,
            @RequestParam("file") MultipartFile file) {
        User user = (User) authentication.getPrincipal();
        String avatarUrl = fileStorageService.storeAvatar(file, user.getId());
        userService.updateAvatar(user.getId(), avatarUrl);
        return ResponseEntity.ok(ApiResponse.success("Avatar uploaded successfully", new AvatarResponse(avatarUrl)));
    }

    @DeleteMapping("/profile/avatar")
    @Operation(summary = "Remove avatar image")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Avatar removed"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<ApiResponse<Void>> removeAvatar(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        String currentAvatarUrl = userService.getAvatarUrl(user.getId());
        if (currentAvatarUrl != null) {
            fileStorageService.deleteFile(currentAvatarUrl);
        }
        userService.updateAvatar(user.getId(), null);
        return ResponseEntity.ok(ApiResponse.success("Avatar removed successfully", null));
    }
}
