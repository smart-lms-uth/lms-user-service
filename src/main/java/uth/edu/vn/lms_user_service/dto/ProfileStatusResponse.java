package uth.edu.vn.lms_user_service.dto;

public record ProfileStatusResponse(
    boolean profileComplete,
    boolean hasPassword
) {}
