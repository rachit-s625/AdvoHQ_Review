package in.advohq.dto;

import java.time.Instant;

/** A short-lived, pre-signed S3 URL the browser can use to fetch a document directly. */
public record DownloadUrlResponse(
        String url,
        Instant expiresAt
) {}
