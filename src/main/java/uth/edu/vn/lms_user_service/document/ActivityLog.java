package uth.edu.vn.lms_user_service.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.util.Map;

/**
 * MongoDB Document for Activity Logs
 * Replaces the JPA UserActivity entity for better scalability
 */
@Document(collection = "activity_logs")
@CompoundIndexes({
    @CompoundIndex(name = "user_timestamp_idx", def = "{'userId': 1, 'timestamp': -1}"),
    @CompoundIndex(name = "session_timestamp_idx", def = "{'sessionId': 1, 'timestamp': 1}"),
    @CompoundIndex(name = "type_timestamp_idx", def = "{'activityType': 1, 'timestamp': -1}")
})
public class ActivityLog {

    @Id
    private String id;

    @Indexed
    @Field("userId")
    private Long userId;

    @Indexed
    @Field("sessionId")
    private String sessionId;

    @Field("activityType")
    private String activityType;

    @Field("action")
    private String action;

    @Field("pageUrl")
    private String pageUrl;

    @Field("pageTitle")
    private String pageTitle;

    @Field("elementId")
    private String elementId;

    @Field("elementText")
    private String elementText;

    @Field("apiEndpoint")
    private String apiEndpoint;

    @Field("httpMethod")
    private String httpMethod;

    @Field("responseStatus")
    private Integer responseStatus;

    @Field("responseTimeMs")
    private Long responseTimeMs;

    @Field("metadata")
    private Map<String, Object> metadata;

    @Field("ipAddress")
    private String ipAddress;

    @Field("userAgent")
    private String userAgent;

    @Field("deviceType")
    private String deviceType;

    @Field("browser")
    private String browser;

    @Field("os")
    private String os;

    @Field("screenWidth")
    private Integer screenWidth;

    @Field("screenHeight")
    private Integer screenHeight;

    @Indexed
    @Field("timestamp")
    private Instant timestamp;

    @Field("durationMs")
    private Long durationMs;

    // Constructors
    public ActivityLog() {
        this.timestamp = Instant.now();
    }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final ActivityLog log = new ActivityLog();

        public Builder userId(Long userId) {
            log.userId = userId;
            return this;
        }

        public Builder sessionId(String sessionId) {
            log.sessionId = sessionId;
            return this;
        }

        public Builder activityType(String activityType) {
            log.activityType = activityType;
            return this;
        }

        public Builder action(String action) {
            log.action = action;
            return this;
        }

        public Builder pageUrl(String pageUrl) {
            log.pageUrl = pageUrl;
            return this;
        }

        public Builder pageTitle(String pageTitle) {
            log.pageTitle = pageTitle;
            return this;
        }

        public Builder elementId(String elementId) {
            log.elementId = elementId;
            return this;
        }

        public Builder elementText(String elementText) {
            log.elementText = elementText;
            return this;
        }

        public Builder apiEndpoint(String apiEndpoint) {
            log.apiEndpoint = apiEndpoint;
            return this;
        }

        public Builder httpMethod(String httpMethod) {
            log.httpMethod = httpMethod;
            return this;
        }

        public Builder responseStatus(Integer responseStatus) {
            log.responseStatus = responseStatus;
            return this;
        }

        public Builder responseTimeMs(Long responseTimeMs) {
            log.responseTimeMs = responseTimeMs;
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            log.metadata = metadata;
            return this;
        }

        public Builder ipAddress(String ipAddress) {
            log.ipAddress = ipAddress;
            return this;
        }

        public Builder userAgent(String userAgent) {
            log.userAgent = userAgent;
            return this;
        }

        public Builder deviceType(String deviceType) {
            log.deviceType = deviceType;
            return this;
        }

        public Builder browser(String browser) {
            log.browser = browser;
            return this;
        }

        public Builder os(String os) {
            log.os = os;
            return this;
        }

        public Builder screenWidth(Integer screenWidth) {
            log.screenWidth = screenWidth;
            return this;
        }

        public Builder screenHeight(Integer screenHeight) {
            log.screenHeight = screenHeight;
            return this;
        }

        public Builder timestamp(Instant timestamp) {
            log.timestamp = timestamp;
            return this;
        }

        public Builder durationMs(Long durationMs) {
            log.durationMs = durationMs;
            return this;
        }

        public ActivityLog build() {
            return log;
        }
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getActivityType() {
        return activityType;
    }

    public void setActivityType(String activityType) {
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

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
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

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public Long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(Long durationMs) {
        this.durationMs = durationMs;
    }
}
