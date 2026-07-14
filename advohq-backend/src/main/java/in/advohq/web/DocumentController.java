package in.advohq.web;

import in.advohq.dto.DocumentResponse;
import in.advohq.dto.DownloadUrlResponse;
import in.advohq.security.AppUserPrincipal;
import in.advohq.security.CurrentUser;
import in.advohq.service.DocumentService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documents;

    public DocumentController(DocumentService documents) {
        this.documents = documents;
    }

    @GetMapping
    public List<DocumentResponse> list(@CurrentUser AppUserPrincipal me,
                                       @RequestParam(required = false) UUID caseId) {
        return documents.list(me.getId(), caseId);
    }

    /** Documents currently in the Trash. */
    @GetMapping("/trash")
    public List<DocumentResponse> trash(@CurrentUser AppUserPrincipal me) {
        return documents.listTrash(me.getId());
    }

    /** Move a document to the Trash (recoverable; bytes are kept). */
    @PostMapping("/{id}/trash")
    public DocumentResponse moveToTrash(@CurrentUser AppUserPrincipal me, @PathVariable UUID id) {
        return documents.moveToTrash(me.getId(), id);
    }

    /** Restore a document from the Trash back into the Library. */
    @PostMapping("/{id}/restore")
    public DocumentResponse restore(@CurrentUser AppUserPrincipal me, @PathVariable UUID id) {
        return documents.restore(me.getId(), id);
    }

    /** Upload a client-provided document (stored in S3). Optionally attach it to a case. */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentResponse upload(@CurrentUser AppUserPrincipal me,
                                   @RequestParam("file") MultipartFile file,
                                   @RequestParam(required = false) UUID caseId) throws IOException {
        return documents.upload(me.getId(), caseId, file);
    }

    /**
     * Returns a short-lived pre-signed URL for the file. With
     * {@code ?disposition=inline} the browser renders it (PDF viewer, image);
     * the default forces a download.
     */
    @GetMapping("/{id}/download-url")
    public DownloadUrlResponse downloadUrl(@CurrentUser AppUserPrincipal me, @PathVariable UUID id,
                                           @RequestParam(defaultValue = "attachment") String disposition) {
        return documents.downloadUrl(me.getId(), id, "inline".equalsIgnoreCase(disposition));
    }

    /**
     * Streams the file bytes through the backend. The AdvoHQ editor uses this
     * to load and render documents (it can't fetch the S3 URL directly).
     */
    @GetMapping("/{id}/content")
    public ResponseEntity<InputStreamResource> content(@CurrentUser AppUserPrincipal me, @PathVariable UUID id) {
        DocumentService.FileContent fc = documents.content(me.getId(), id);
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, fc.contentType());
        headers.set(HttpHeaders.CONTENT_DISPOSITION,
                "inline; filename=\"" + fc.fileName().replace("\"", "") + "\"");
        if (fc.sizeBytes() > 0) {
            headers.setContentLength(fc.sizeBytes());
        }
        return new ResponseEntity<>(new InputStreamResource(fc.stream()), headers, HttpStatus.OK);
    }

    /** Saves an edited version of the document back to S3, keeping the same id. */
    @PutMapping(value = "/{id}/content", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DocumentResponse replaceContent(@CurrentUser AppUserPrincipal me, @PathVariable UUID id,
                                           @RequestParam("file") MultipartFile file) throws IOException {
        return documents.replaceContent(me.getId(), id, file);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@CurrentUser AppUserPrincipal me, @PathVariable UUID id) {
        documents.delete(me.getId(), id);
    }
}
