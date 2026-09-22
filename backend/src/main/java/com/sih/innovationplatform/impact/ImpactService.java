package com.sih.innovationplatform.impact;

import com.sih.innovationplatform.common.ApiException;
import com.sih.innovationplatform.common.CurrentUser;
import com.sih.innovationplatform.project.Project;
import com.sih.innovationplatform.project.ProjectRepository;
import com.sih.innovationplatform.user.User;
import com.sih.innovationplatform.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ImpactService {

    private final ImpactRecordRepository recordRepo;
    private final ImpactEvidenceRepository evidenceRepo;
    private final ProjectRepository projectRepo;
    private final UserRepository userRepo;
    private final CurrentUser currentUser;

    @Transactional
    public ImpactDtos.ImpactResult submit(Long projectId, ImpactDtos.SubmitRequest req, Long userId) {
        Project project = projectRepo.findById(projectId)
                .orElseThrow(() -> ApiException.notFound("Project not found"));
        User user = userRepo.findById(userId).orElseThrow();

        ImpactRecord rec = new ImpactRecord();
        rec.setProject(project);
        rec.setBeneficiaries(req.beneficiaries() != null ? req.beneficiaries() : 0);
        rec.setLocationsCovered(req.locationsCovered() != null ? req.locationsCovered() : 0);
        rec.setCostSavings(req.costSavings() != null ? req.costSavings() : 0);
        rec.setProblemReduction(req.problemReduction());
        rec.setDurationMonths(req.durationMonths() != null ? req.durationMonths() : 0);
        rec.setImplementationPercent(req.implementationPercent() != null ? req.implementationPercent() : 0);
        rec.setDomainMetrics(req.domainMetrics());
        rec.setVerificationStatus(ImpactRecord.PENDING);
        rec.setSubmittedBy(user);
        ImpactRecord saved = recordRepo.save(rec);
        return toResult(saved);
    }

    @Transactional
    public ImpactDtos.ImpactResult addEvidence(Long projectId, ImpactDtos.EvidenceRequest req, Long userId) {
        ImpactRecord rec = recordRepo.findById(req.impactRecordId())
                .orElseThrow(() -> ApiException.notFound("Impact record not found"));
        if (!rec.getProject().getId().equals(projectId)) {
            throw ApiException.badRequest("Impact record does not belong to this project");
        }
        User user = userRepo.findById(userId).orElseThrow();
        ImpactEvidence ev = new ImpactEvidence();
        ev.setImpactRecord(rec);
        ev.setEvidenceType(req.evidenceType() == null ? "DOCUMENT" : req.evidenceType());
        ev.setDescription(req.description());
        ev.setFileUrl(req.fileUrl());
        ev.setStatus("PENDING");
        ev.setSubmittedBy(user);
        evidenceRepo.save(ev);
        return toResult(rec);
    }

    @Transactional
    public ImpactDtos.ImpactResult reviewEvidence(Long evidenceId, ImpactDtos.ReviewRequest req, Long reviewerId) {
        if (!currentUser.hasAnyRole("PLATFORM_ADMIN", "GOVERNMENT_ADMIN")) {
            throw ApiException.forbidden("Only admins can review evidence");
        }
        ImpactEvidence ev = evidenceRepo.findById(evidenceId)
                .orElseThrow(() -> ApiException.notFound("Evidence not found"));
        String status = req.status() == null ? "VERIFIED" : req.status().toUpperCase();
        if (!List.of("VERIFIED", "REJECTED").contains(status)) {
            throw ApiException.badRequest("Review status must be VERIFIED or REJECTED");
        }
        User reviewer = userRepo.findById(reviewerId).orElseThrow();
        ev.setStatus(status);
        ev.setReviewedBy(reviewer);
        ev.setReviewedAt(LocalDateTime.now());
        evidenceRepo.save(ev);

        ImpactRecord rec = ev.getImpactRecord();
        List<ImpactEvidence> evidence = evidenceRepo.findByImpactRecordIdOrderByCreatedAtAsc(rec.getId());
        long reviewed = evidence.stream().filter(e -> e.getStatus().equals("VERIFIED")).count();
        boolean anyRejected = evidence.stream().anyMatch(e -> e.getStatus().equals("REJECTED"));
        if (reviewed > 0 && reviewed == evidence.size()) {
            rec.setVerificationStatus(ImpactRecord.VERIFIED);
        } else if (reviewed > 0 || anyRejected) {
            rec.setVerificationStatus(ImpactRecord.PARTIALLY_VERIFIED);
        } else {
            rec.setVerificationStatus(ImpactRecord.PENDING);
        }
        rec.setUpdatedAt(LocalDateTime.now());
        recordRepo.save(rec);
        return toResult(rec);
    }

    public List<ImpactDtos.ImpactResult> listForProject(Long projectId) {
        return recordRepo.findByProjectIdOrderBySubmittedAtDesc(projectId).stream()
                .map(this::toResult).toList();
    }

    public List<ImpactDtos.ImpactResult> listAll() {
        return recordRepo.findAll().stream().map(this::toResult).toList();
    }

    public List<ImpactDtos.EvidenceResult> evidenceAll() {
        return evidenceRepo.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toEvidence).toList();
    }

    public long verifiedBeneficiaries() {
        return recordRepo.findByVerificationStatus(ImpactRecord.VERIFIED).stream()
                .mapToLong(ImpactRecord::getBeneficiaries).sum();
    }

    public long totalBeneficiaries() {
        return recordRepo.findAll().stream().mapToLong(ImpactRecord::getBeneficiaries).sum();
    }

    public long recordCount() {
        return recordRepo.count();
    }

    public long verifiedRecordCount() {
        return recordRepo.countByVerificationStatus(ImpactRecord.VERIFIED);
    }

    private ImpactDtos.ImpactResult toResult(ImpactRecord r) {
        List<ImpactDtos.EvidenceResult> evidence = evidenceRepo
                .findByImpactRecordIdOrderByCreatedAtAsc(r.getId()).stream()
                .map(this::toEvidence).toList();
        return new ImpactDtos.ImpactResult(
                r.getId(),
                r.getProject() == null ? null : r.getProject().getId(),
                r.getProject() == null ? "" : r.getProject().getTitle(),
                r.getBeneficiaries(), r.getLocationsCovered(), r.getCostSavings(),
                r.getProblemReduction(), r.getDurationMonths(), r.getImplementationPercent(),
                r.getDomainMetrics(), r.getVerificationStatus(),
                r.getSubmittedBy() == null ? "" : r.getSubmittedBy().getName(),
                r.getSubmittedAt() == null ? null : r.getSubmittedAt().toString(),
                evidence);
    }

    private ImpactDtos.EvidenceResult toEvidence(ImpactEvidence e) {
        return new ImpactDtos.EvidenceResult(
                e.getId(), e.getImpactRecord() == null ? null : e.getImpactRecord().getId(),
                e.getEvidenceType(), e.getDescription(), e.getFileUrl(), e.getStatus(),
                e.getSubmittedBy() == null ? "" : e.getSubmittedBy().getName(),
                e.getReviewedBy() == null ? "" : e.getReviewedBy().getName(),
                e.getReviewedAt() == null ? null : e.getReviewedAt().toString());
    }
}