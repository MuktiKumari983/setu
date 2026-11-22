package com.sih.innovationplatform.impact;

import com.sih.innovationplatform.common.CurrentUser;
import com.sih.innovationplatform.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ImpactController {

    private final ImpactService impactService;
    private final CurrentUser currentUser;

    @GetMapping("/projects/{id}/impact")
    public List<ImpactDtos.ImpactResult> listForProject(@PathVariable Long id) {
        return impactService.listForProject(id);
    }

    @PostMapping("/projects/{id}/impact")
    public ImpactDtos.ImpactResult submit(@PathVariable Long id,
                                          @Valid @RequestBody ImpactDtos.SubmitRequest request) {
        User user = currentUser.get();
        return impactService.submit(id, request, user.getId());
    }

    @PostMapping("/projects/{id}/impact/evidence")
    public ImpactDtos.ImpactResult addEvidence(@PathVariable Long id,
                                               @Valid @RequestBody ImpactDtos.EvidenceRequest request) {
        User user = currentUser.get();
        return impactService.addEvidence(id, request, user.getId());
    }

    @PutMapping("/impact/evidence/{evidenceId}/review")
    public ImpactDtos.ImpactResult review(@PathVariable Long evidenceId,
                                          @Valid @RequestBody ImpactDtos.ReviewRequest request) {
        User user = currentUser.get();
        return impactService.reviewEvidence(evidenceId, request, user.getId());
    }

    @GetMapping("/impact/evidence")
    public List<ImpactDtos.EvidenceResult> allEvidence() {
        return impactService.evidenceAll();
    }

    @GetMapping("/impact")
    public List<ImpactDtos.ImpactResult> allImpact() {
        return impactService.listAll();
    }

    @GetMapping("/impact/stats")
    public Map<String, Object> stats() {
        return Map.of(
                "verifiedBeneficiaries", impactService.verifiedBeneficiaries(),
                "totalBeneficiaries", impactService.totalBeneficiaries(),
                "records", impactService.recordCount(),
                "verifiedRecords", impactService.verifiedRecordCount());
    }
}