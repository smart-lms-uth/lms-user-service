package uth.edu.vn.lms_user_service.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity để lưu trữ hoạt động của người dùng
 * Dùng để thống kê và phân tích hành vi người dùng
 */
@Entity
@Table(name = "user_activities", indexes = {
    @Index(name = "idx_activity_user", columnList = "user_id"),
    @Index(name = "idx_activity_type", columnList = "activity_type"),
    @Index(name = "idx_activity_timestamp", columnList = "timestamp"),
    @Index(name = "idx_activity_session", columnList = "session_id")
})
public class UserActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "session_id", length = 100)
    private String sessionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false, length = 50)
    private ActivityType activityType;

    @Column(name = "action", length = 100)
    private String action;

    @Column(name = "page_url", length = 500)
    private String pageUrl;

    @Column(name = "page_title", length = 200)
    private String pageTitle;

    @Column(name = "element_id", length = 100)
    private String elementId;

    @Column(name = "element_text", length = 200)
    private String elementText;

    @Column(name = "api_endpoint", length = 500)
    private String apiEndpoint;

    @Column(name = "http_method", length = 10)
    private String httpMethod;

    @Column(name = "response_status")
    private Integer responseStatus;

    @Column(name = "response_time_ms")
    private Long responseTimeMs;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata; // JSON format for additional data

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "device_type", length = 50)
    private String deviceType;

    @Column(name = "browser", length = 50)
    private String browser;

    @Column(name = "os", length = 50)
    private String os;

    @Column(name = "screen_width")
    private Integer screenWidth;

    @Column(name = "screen_height")
    private Integer screenHeight;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "duration_ms")
    private Long durationMs; // Time spent on page/action

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }

    // Constructors
    public UserActivity() {}

    public UserActivity(User user, ActivityType activityType, String action) {
        this.user = user;
        this.activityType = activityType;
        this.action = action;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public ActivityType getActivityType() {
        return activityType;
    }

    public void setActivityType(ActivityType activityType) {
        this.activityType = activityType;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getPageUrl() {
        return pageUrl;
    }

    public void setPageUrl(String pageUrl) {
        this.pageUrl = pageUrl;
    }

    public String getPageTitle() {
        return pageTitle;
    }

    public void setPageTitle(String pageTitle) {
        this.pageTitle = pageTitle;
    }

    public String getElementId() {
        return elementId;
    }

    public void setElementId(String elementId) {
        this.elementId = elementId;
    }

    public String getElementText() {
        return elementText;
    }

    public void setElementText(String elementText) {
        this.elementText = elementText;
    }

    public String getApiEndpoint() {
        return apiEndpoint;
    }

    public void setApiEndpoint(String apiEndpoint) {
        this.apiEndpoint = apiEndpoint;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public Integer getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(Integer responseStatus) {
        this.responseStatus = responseStatus;
    }

    public Long getResponseTimeMs() {
        return responseTimeMs;
    }

    public void setResponseTimeMs(Long responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public Integer getScreenWidth() {
        return screenWidth;
    }

    public void setScreenWidth(Integer screenWidth) {
        this.screenWidth = screenWidth;
    }

    public Integer getScreenHeight() {
        return screenHeight;
    }

    public void setScreenHeight(Integer screenHeight) {
        this.screenHeight = screenHeight;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(Long durationMs) {
        this.durationMs = durationMs;
    }
}
