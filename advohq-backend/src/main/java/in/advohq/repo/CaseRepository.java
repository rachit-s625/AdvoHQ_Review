package in.advohq.repo;

import in.advohq.domain.CaseEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CaseRepository extends JpaRepository<CaseEntity, UUID> {

    @EntityGraph(attributePaths = "importantDates")
    List<CaseEntity> findByUserIdOrderByCreatedAtDesc(UUID userId);

    @EntityGraph(attributePaths = "importantDates")
    Optional<CaseEntity> findByIdAndUserId(UUID id, UUID userId);
}
