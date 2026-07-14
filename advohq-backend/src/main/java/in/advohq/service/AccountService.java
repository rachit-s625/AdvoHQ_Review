package in.advohq.service;

import in.advohq.domain.Document;
import in.advohq.repo.DocumentRepository;
import in.advohq.repo.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Permanently removes a user and everything they own. Database rows go via
 * the ON DELETE CASCADE foreign keys; S3 objects are deleted explicitly
 * first, since the database knows nothing about them.
 */
@Service
public class AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    private final UserRepository users;
    private final DocumentRepository documents;
    private final S3StorageService storage;

    public AccountService(UserRepository users, DocumentRepository documents, S3StorageService storage) {
        this.users = users;
        this.documents = documents;
        this.storage = storage;
    }

    @Transactional
    public void deleteAccount(UUID userId) {
        for (Document doc : documents.findByUserIdOrderByCreatedAtDesc(userId)) {
            try {
                storage.delete(doc.getS3Key());
            } catch (Exception ex) {
                // An unreachable/missing S3 object must not block account deletion.
                log.warn("Could not delete S3 object {} while deleting user {}", doc.getS3Key(), userId, ex);
            }
        }
        users.deleteById(userId);
    }
}
