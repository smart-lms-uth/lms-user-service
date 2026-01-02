# Multi-stage build để tự động build project

# Stage 1: Build với Maven
FROM eclipse-temurin:21-jdk-alpine AS builder

# Set working directory
WORKDIR /app

# Copy Maven wrapper và pom.xml trước để cache dependencies
COPY .mvn ./.mvn
COPY mvnw .
COPY pom.xml .

# Cấp quyền thực thi cho mvnw
RUN chmod +x ./mvnw

# Download dependencies (layer này sẽ được cache)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Build project
RUN ./mvnw clean package -DskipTests -B

# Stage 2: Runtime image (sử dụng JRE thay vì JDK để giảm kích thước)
FROM eclipse-temurin:21-jre-alpine

# Thêm thông tin
LABEL maintainer="Thanh Dev"
LABEL description="LMS User Service - UTH Smart System"
LABEL version="0.0.1-SNAPSHOT"

# Tạo user không phải root để chạy ứng dụng (bảo mật)
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Set working directory
WORKDIR /app

# Copy JAR từ stage builder
COPY --from=builder /app/target/*.jar app.jar

# Đổi ownership cho user appuser
RUN chown -R appuser:appgroup /app

# Chuyển sang user không phải root
USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# JVM optimization cho container
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

# Run the application với Spring profile docker
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dspring.profiles.active=docker -jar app.jar"]