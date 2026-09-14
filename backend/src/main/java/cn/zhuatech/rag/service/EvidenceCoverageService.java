/* Copyright 2026 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ */
package cn.zhuatech.rag.service;

import cn.zhuatech.rag.common.BusinessException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** 将答案的每条声明与可访问、未过期的证据逐项核对。 */
@Service
public class EvidenceCoverageService {
    public Result evaluate(Request request) {
        Map<String, Evidence> catalog = new HashMap<>();
        for (Evidence evidence : request.evidence()) {
            if (catalog.putIfAbsent(evidence.citationId(), evidence) != null) {
                throw new BusinessException("引用编号重复: " + evidence.citationId());
            }
        }
        Set<String> claimIds = new HashSet<>();
        List<ClaimResult> results = new ArrayList<>();
        int denied = 0, review = 0, missing = 0;
        for (Claim claim : request.claims()) {
            if (!claimIds.add(claim.claimId())) throw new BusinessException("声明编号重复: " + claim.claimId());
            List<String> reasons = new ArrayList<>();
            int supporting = 0;
            boolean accessDenied = false, contradiction = false, stale = false;
            for (String citationId : claim.citationIds()) {
                Evidence evidence = catalog.get(citationId);
                if (evidence == null) {
                    reasons.add("引用不存在: " + citationId);
                    continue;
                }
                if (!request.tenantId().equals(evidence.tenantId()) || !evidence.aclAllowed()
                        || (evidence.piiDetected() && !evidence.redacted())) {
                    accessDenied = true;
                    continue;
                }
                if (!evidence.active() || evidence.ageDays() > request.maxEvidenceAgeDays()) {
                    stale = true;
                    continue;
                }
                if (evidence.stance() == Stance.CONTRADICT) contradiction = true;
                if (evidence.stance() == Stance.SUPPORT) supporting++;
            }
            ClaimDecision decision;
            if (accessDenied) {
                decision = ClaimDecision.DENY;
                denied++;
                reasons.add("引用不满足租户、ACL 或个人信息保护要求");
            } else if (contradiction) {
                decision = ClaimDecision.REVIEW;
                review++;
                reasons.add("存在与声明相矛盾的证据");
            } else if (supporting == 0 || stale || !reasons.isEmpty()) {
                decision = ClaimDecision.RETRIEVE_MORE;
                missing++;
                if (supporting == 0) reasons.add("声明缺少有效支持证据");
                if (stale) reasons.add("引用已停用或超过时效上限");
            } else {
                decision = ClaimDecision.ALLOW;
            }
            results.add(new ClaimResult(claim.claimId(), decision, supporting, List.copyOf(reasons)));
        }
        Decision decision = denied > 0 ? Decision.DENY : review > 0 ? Decision.REVIEW
                : missing > 0 ? Decision.RETRIEVE_MORE : Decision.ALLOW;
        return new Result(decision, results.size(), denied, review, missing, List.copyOf(results));
    }

    public record Request(@NotBlank String tenantId, @PositiveOrZero int maxEvidenceAgeDays,
                          @NotEmpty List<@Valid Claim> claims,
                          @NotEmpty List<@Valid Evidence> evidence) {}

    public record Claim(@NotBlank String claimId, @NotEmpty List<@NotBlank String> citationIds) {}

    public record Evidence(@NotBlank String citationId, @NotBlank String tenantId,
                           boolean aclAllowed, boolean active, @PositiveOrZero int ageDays,
                           @NotNull Stance stance, boolean piiDetected, boolean redacted) {}

    public record ClaimResult(String claimId, ClaimDecision decision, int supportingEvidence,
                              List<String> reasons) {}

    public record Result(Decision decision, int totalClaims, int deniedClaims,
                         int reviewClaims, int claimsNeedingRetrieval, List<ClaimResult> claims) {}

    public enum Stance { SUPPORT, CONTRADICT, NEUTRAL }
    public enum ClaimDecision { ALLOW, RETRIEVE_MORE, REVIEW, DENY }
    public enum Decision { ALLOW, RETRIEVE_MORE, REVIEW, DENY }
}
