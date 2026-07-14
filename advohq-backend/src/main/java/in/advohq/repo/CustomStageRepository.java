package in.advohq.repo;

import in.advohq.domain.CustomStage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomStageRepository extends JpaRepository<CustomStage, UUID> {
    List<CustomStage> findByUserIdOrderBySortOrderAsc(UUID userId);
    Optional<CustomStage> findByIdAndUserId(UUID id, UUID userId);
}
