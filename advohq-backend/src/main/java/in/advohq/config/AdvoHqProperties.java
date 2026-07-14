package in.advohq.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Strongly-typed binding for all {@code advohq.*} configuration in application.yml.
 */
@ConfigurationProperties(prefix = "advohq")
public record AdvoHqProperties(Security security, Aws aws) {

    public record Security(Jwt jwt, Cors cors) {
        public record Jwt(String secret, long expirationMs) {}
        public record Cors(String allowedOrigins) {}
    }

    public record Aws(String region, S3 s3) {
        public record S3(String bucket, String endpoint, long presignExpiryMinutes) {}
    }
}
