package in.advohq.service;

import in.advohq.config.AdvoHqProperties;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;

/**
 * Thin wrapper over AWS S3 for storing and serving client-provided documents.
 * Objects are private; the browser fetches them via short-lived pre-signed URLs.
 */
@Service
public class S3StorageService {

    private final S3Client s3;
    private final S3Presigner presigner;
    private final String bucket;
    private final Duration presignExpiry;

    public S3StorageService(S3Client s3, S3Presigner presigner, AdvoHqProperties props) {
        this.s3 = s3;
        this.presigner = presigner;
        this.bucket = props.aws().s3().bucket();
        this.presignExpiry = Duration.ofMinutes(props.aws().s3().presignExpiryMinutes());
    }

    /** Upload bytes to S3 under the given key. */
    public void upload(String key, InputStream content, long size, String contentType) throws IOException {
        PutObjectRequest req = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .contentLength(size)
                .build();
        s3.putObject(req, RequestBody.fromInputStream(content, size));
    }

    /**
     * @param inline true → the browser renders the file (PDF viewer, image);
     *               false → forces a download with the original file name.
     * @return a pre-signed GET URL valid for the configured expiry window.
     */
    public PresignedUrl presignDownload(String key, String downloadFileName, boolean inline) {
        String disposition = (inline ? "inline" : "attachment")
                + "; filename=\"" + downloadFileName.replace("\"", "") + "\"";
        GetObjectRequest get = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .responseContentDisposition(disposition)
                .build();
        GetObjectPresignRequest presign = GetObjectPresignRequest.builder()
                .signatureDuration(presignExpiry)
                .getObjectRequest(get)
                .build();
        var result = presigner.presignGetObject(presign);
        return new PresignedUrl(result.url().toString(), Instant.now().plus(presignExpiry));
    }

    /** @return the object's bytes as a stream (caller is responsible for closing it). */
    public InputStream download(String key) {
        return s3.getObject(GetObjectRequest.builder().bucket(bucket).key(key).build());
    }

    public void delete(String key) {
        s3.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
    }

    public record PresignedUrl(String url, Instant expiresAt) {}
}
