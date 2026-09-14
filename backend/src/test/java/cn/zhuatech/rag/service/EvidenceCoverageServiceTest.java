/* Copyright 2026 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ */
package cn.zhuatech.rag.service;

import cn.zhuatech.rag.common.BusinessException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EvidenceCoverageServiceTest {
    private final EvidenceCoverageService service = new EvidenceCoverageService();

    private EvidenceCoverageService.Evidence evidence(String id, EvidenceCoverageService.Stance stance) {
        return new EvidenceCoverageService.Evidence(id, "tenant-a", true, true, 5, stance, false, false);
    }

    private EvidenceCoverageService.Request request(List<String> citations,
                                                     List<EvidenceCoverageService.Evidence> evidence) {
        return new EvidenceCoverageService.Request("tenant-a", 30,
                List.of(new EvidenceCoverageService.Claim("claim-1", citations)), evidence);
    }

    @Test
    void allowsClaimWithCurrentAuthorizedSupportingEvidence() {
        var result = service.evaluate(request(List.of("DOC-1"),
                List.of(evidence("DOC-1", EvidenceCoverageService.Stance.SUPPORT))));
        assertThat(result.decision()).isEqualTo(EvidenceCoverageService.Decision.ALLOW);
        assertThat(result.claims().getFirst().supportingEvidence()).isEqualTo(1);
    }

    @Test
    void requestsMoreEvidenceForMissingOrStaleCitation() {
        var stale = new EvidenceCoverageService.Evidence("DOC-1", "tenant-a", true, true, 45,
                EvidenceCoverageService.Stance.SUPPORT, false, false);
        var result = service.evaluate(request(List.of("DOC-1", "UNKNOWN"), List.of(stale)));
        assertThat(result.decision()).isEqualTo(EvidenceCoverageService.Decision.RETRIEVE_MORE);
        assertThat(result.claimsNeedingRetrieval()).isEqualTo(1);
    }

    @Test
    void sendsContradictoryEvidenceForReview() {
        var result = service.evaluate(request(List.of("DOC-1", "DOC-2"), List.of(
                evidence("DOC-1", EvidenceCoverageService.Stance.SUPPORT),
                evidence("DOC-2", EvidenceCoverageService.Stance.CONTRADICT))));
        assertThat(result.decision()).isEqualTo(EvidenceCoverageService.Decision.REVIEW);
    }

    @Test
    void deniesCrossTenantOrUnredactedPiiEvidence() {
        var forbidden = new EvidenceCoverageService.Evidence("DOC-1", "tenant-b", true, true, 2,
                EvidenceCoverageService.Stance.SUPPORT, true, false);
        var result = service.evaluate(request(List.of("DOC-1"), List.of(forbidden)));
        assertThat(result.decision()).isEqualTo(EvidenceCoverageService.Decision.DENY);
    }

    @Test
    void rejectsDuplicateEvidenceIds() {
        assertThatThrownBy(() -> service.evaluate(request(List.of("DOC-1"), List.of(
                evidence("DOC-1", EvidenceCoverageService.Stance.SUPPORT),
                evidence("DOC-1", EvidenceCoverageService.Stance.NEUTRAL)))))
                .isInstanceOf(BusinessException.class);
    }
}
