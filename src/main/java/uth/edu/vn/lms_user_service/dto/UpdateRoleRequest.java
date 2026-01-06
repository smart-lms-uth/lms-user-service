package uth.edu.vn.lms_user_service.dto;

import uth.edu.vn.lms_user_service.entity.Role;

/**
 * DTO để cập nhật role của user
 */
public record UpdateRoleRequest(
    Role role
) {}
