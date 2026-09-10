/* Copyright 2026 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ */
package cn.zhuatech.rag.service;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RetrievalAccessPolicyServiceTest {
    private final RetrievalAccessPolicyService service = new RetrievalAccessPolicyService();

    @Test
    void allowsCurrentInternalKnowledgeWithTraceableCitation() {
        var result = service.decide(request(RetrievalAccessPolicyService.Classification.INTERNAL,
                false, true, true, 10, 30, 0.92, true));
        assertThat(result.decision()).isEqualTo(RetrievalAccessPolicyService.Decision.ALLOW);
        assertThat(result.auditKey()).hasSize(64);
    }

    @Test
    void allowsConfidentialKnowledgeOnlyAfterRedaction() {
        var result = service.decide(request(RetrievalAccessPolicyService.Classification.CONFIDENTIAL,
                true, true, true, 10, 30, 0.92, true));
        assertThat(result.decision()).isEqualTo(RetrievalAccessPolicyService.Decision.ALLOW_REDACTED);
        assertThat(result.obligations()).contains("仅返回脱敏片段");
    }

    @Test
    void reviewsRestrictedOrStaleKnowledge() {
        var result = service.decide(request(RetrievalAccessPolicyService.Classification.RESTRICTED,
                false, true, true, 45, 30, 0.55, false));
        assertThat(result.decision()).isEqualTo(RetrievalAccessPolicyService.Decision.REVIEW);
        assertThat(result.obligations()).hasSize(3);
    }

    @Test
    void deniesCrossTenantAndUnauthorizedRetrieval() {
        var base = request(RetrievalAccessPolicyService.Classification.INTERNAL,
                false, true, true, 10, 30, 0.92, true);
        var unsafe = new RetrievalAccessPolicyService.DecisionRequest("tenant-a", "tenant-b",
                Set.of("sales"), Set.of("legal"), "support", Set.of("legal"), base.classification(),
                false, true, false, false, 10, 30, 0.92, 0.8, false, true, true);
        var result = service.decide(unsafe);
        assertThat(result.decision()).isEqualTo(RetrievalAccessPolicyService.Decision.DENY);
        assertThat(result.blockers()).hasSize(5);
    }

    private RetrievalAccessPolicyService.DecisionRequest request(
            RetrievalAccessPolicyService.Classification classification, boolean pii, boolean redaction,
            boolean aclFresh, int sourceAge, int maxAge, double citationCoverage, boolean approval) {
        return new RetrievalAccessPolicyService.DecisionRequest("tenant-a", "tenant-a", Set.of("sales"),
                Set.of("sales", "legal"), "support", Set.of("support", "training"), classification,
                true, true, aclFresh, false, sourceAge, maxAge, citationCoverage, 0.8, pii, redaction, approval);
    }
}
