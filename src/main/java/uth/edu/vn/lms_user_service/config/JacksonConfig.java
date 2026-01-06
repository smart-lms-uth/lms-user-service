package uth.edu.vn.lms_user_service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Jackson Configuration for HTTP request/response
 * This ensures the primary ObjectMapper does NOT have default typing enabled
 * (which causes issues with JSON deserialization)
 */
@Configuration
public class JacksonConfig {

    /**
     * Primary ObjectMapper for HTTP request/response serialization
     * Does NOT use default typing - this is important for REST endpoints
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}
