package in.advohq.service;

import in.advohq.domain.CaseEntity;
import in.advohq.domain.ImportantDate;
import in.advohq.dto.CaseRequest;
import in.advohq.dto.CaseResponse;
import in.advohq.dto.ImportantDateDto;
import in.advohq.exception.NotFoundException;
import in.advohq.repo.CaseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CaseService {

    private final CaseRepository cases;

    public CaseService(CaseRepository cases) {
        this.cases = cases;
    }

    @Transactional(readOnly = true)
    public List<CaseResponse> list(UUID userId) {
        return cases.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(CaseResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public CaseResponse get(UUID userId, UUID caseId) {
        return CaseResponse.from(require(userId, caseId));
    }

    @Transactional
    public CaseResponse create(UUID userId, CaseRequest req) {
        CaseEntity c = new CaseEntity();
        c.setUserId(userId);
        apply(c, req);
        return CaseResponse.from(cases.save(c));
    }

    @Transactional
    public CaseResponse update(UUID userId, UUID caseId, CaseRequest req) {
        CaseEntity c = require(userId, caseId);
        apply(c, req);
        return CaseResponse.from(cases.save(c));
    }

    @Transactional
    public void delete(UUID userId, UUID caseId) {
        CaseEntity c = require(userId, caseId);
        cases.delete(c);
    }

    private CaseEntity require(UUID userId, UUID caseId) {
        return cases.findByIdAndUserId(caseId, userId)
                .orElseThrow(() -> new NotFoundException("Case not found"));
    }

    /** Copy editable fields and fully re-sync the important-dates collection. */
    private void apply(CaseEntity c, CaseRequest req) {
        c.setTitle(req.title().trim());
        c.setStage(req.stage());
        c.setPoints(req.points());
        c.setCaseNumber(req.caseNumber());
        c.setCourt(req.court());
        c.setAssignedTo(req.assignedTo());
        c.setHall(req.hall());
        c.setLinkedEventId(req.linkedEventId());

        c.getImportantDates().clear();
        if (req.importantDates() != null) {
            for (ImportantDateDto d : req.importantDates()) {
                ImportantDate dt = new ImportantDate();
                dt.setCaseEntity(c);
                dt.setDateIso(d.dateISO());
                dt.setLabel(d.label());
                dt.setNotified(d.notified());
                c.getImportantDates().add(dt);
            }
        }
    }
}
