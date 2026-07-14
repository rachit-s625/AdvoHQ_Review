package in.advohq.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

/**
 * Builds the AWS S3 client and presigner used to store and serve client-provided documents.
 *
 * <p>Credentials are resolved via the {@link DefaultCredentialsProvider} chain
 * (env vars, shared profile, container/instance role) — never hard-coded.
 * Set a custom {@code advohq.aws.s3.endpoint} to point at LocalStack/MinIO during development.
 */
@Configuration
public class S3Config {

    private final AdvoHqProperties props;

    public S3Config(AdvoHqProperties props) {
        this.props = props;
    }

    private Region region() {
        return Region.of(props.aws().region());
    }

    private boolean hasCustomEndpoint() {
        return StringUtils.hasText(props.aws().s3().endpoint());
    }

    @Bean
    public S3Client s3Client() {
        var builder = S3Client.builder()
                .region(region())
                .credentialsProvider(DefaultCredentialsProvider.create());
        if (hasCustomEndpoint()) {
            builder.endpointOverride(URI.create(props.aws().s3().endpoint()))
                   .serviceConfiguration(S3Configuration.builder()
                           .pathStyleAccessEnabled(true)   // required for LocalStack/MinIO
                           .build());
        }
        return builder.build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        var builder = S3Presigner.builder()
                .region(region())
                .credentialsProvider(DefaultCredentialsProvider.create());
        if (hasCustomEndpoint()) {
            builder.endpointOverride(URI.create(props.aws().s3().endpoint()))
                   .serviceConfiguration(S3Configuration.builder()
                           .pathStyleAccessEnabled(true)
                           .build());
        }
        return builder.build();
    }
}
