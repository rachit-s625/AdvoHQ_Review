package in.advohq.web;

import in.advohq.dto.CaseRequest;
import in.advohq.dto.CaseResponse;
import in.advohq.security.AppUserPrincipal;
import in.advohq.security.CurrentUser;
import in.advohq.service.CaseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/cases")
public class CaseController {

    private final CaseService cases;

    public CaseController(CaseService cases) {
        this.cases = cases;
    }

    @GetMapping
    public List<CaseResponse> list(@CurrentUser AppUserPrincipal me) {
        return cases.list(me.getId());
    }

    @GetMapping("/{id}")
    public CaseResponse get(@CurrentUser AppUserPrincipal me, @PathVariable UUID id) {
        return cases.get(me.getId(), id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CaseResponse create(@CurrentUser AppUserPrincipal me, @Valid @RequestBody CaseRequest req) {
        return cases.create(me.getId(), req);
    }

    @PutMapping("/{id}")
    public CaseResponse update(@CurrentUser AppUserPrincipal me,
                               @PathVariable UUID id,
                               @Valid @RequestBody CaseRequest req) {
        return cases.update(me.getId(), id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@CurrentUser AppUserPrincipal me, @PathVariable UUID id) {
        cases.delete(me.getId(), id);
    }
}
