/* Copyright 2026 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ */
package cn.zhuatech.rag.service;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/** 在生成答案前执行租户、ACL、用途、数据分级与引用质量的统一访问决策。 */
@Service
public class RetrievalAccessPolicyService {
    public DecisionResult decide(DecisionRequest request) {
        Set<String> userGroups = safe(request.userGroups());
        Set<String> allowedGroups = safe(request.allowedGroups());
        Set<String> allowedPurposes = safe(request.allowedPurposes());
        List<String> blockers = new ArrayList<>();
        List<String> obligations = new ArrayList<>();

        if (!request.identityVerified()) blockers.add("用户身份未验证");
        if (!request.tenantId().equals(request.documentTenantId())) blockers.add("请求租户与知识文档租户不一致");
        if (userGroups.stream().noneMatch(allowedGroups::contains)) blockers.add("用户组不具备文档访问权限");
        if (!allowedPurposes.contains(request.purpose())) blockers.add("检索用途不在允许范围内");
        if (!request.sourceActive()) blockers.add("知识来源已停用");
        if (!request.aclFresh()) blockers.add("知识索引 ACL 不是最新版本");
        if (request.legalHoldRestricted()) blockers.add("文档处于法务限制状态");

        if (!blockers.isEmpty()) {
            obligations.add("拒绝向量片段与原文内容返回");
            obligations.add("记录策略命中项并通知知识空间负责人");
            return result(Decision.DENY, request, blockers, obligations);
        }

        if (request.sourceAgeDays() > request.maxSourceAgeDays()) {
            obligations.add("刷新知识来源后重新检索");
        }
        if (request.citationCoverage() < request.minCitationCoverage()) {
            obligations.add("补充检索直至引用覆盖率达到阈值");
        }
        if (request.classification() == Classification.RESTRICTED && !request.humanApprovalComplete()) {
            obligations.add("限制级知识须由数据责任人审批");
        }
        if (request.piiDetected() && !request.redactionAvailable()) {
            obligations.add("个人信息无法脱敏，转人工安全复核");
        }
        if (!obligations.isEmpty()) {
            return result(Decision.REVIEW, request, blockers, obligations);
        }

        if (request.piiDetected() || request.classification() == Classification.CONFIDENTIAL) {
            obligations.add("仅返回脱敏片段");
            obligations.add("答案保留来源、用途与访问决策审计键");
            return result(Decision.ALLOW_REDACTED, request, blockers, obligations);
        }

        obligations.add("答案必须携带可追溯引用");
        return result(Decision.ALLOW, request, blockers, obligations);
    }

    private DecisionResult result(Decision decision, DecisionRequest request,
                                  List<String> blockers, List<String> obligations) {
        return new DecisionResult(decision, List.copyOf(blockers), List.copyOf(obligations),
                auditKey(request, decision));
    }

    private Set<String> safe(Set<String> values) {
        return values == null ? Set.of() : Set.copyOf(values);
    }

    private String auditKey(DecisionRequest request, Decision decision) {
        String source = String.join("|", request.tenantId(), request.documentTenantId(), request.purpose(),
                request.classification().name(), decision.name(),
                String.join(",", new TreeSet<>(safe(request.userGroups()))),
                String.join(",", new TreeSet<>(safe(request.allowedGroups()))));
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(source.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 unavailable", exception);
        }
    }

    public record DecisionRequest(
            @NotBlank String tenantId,
            @NotBlank String documentTenantId,
            @NotEmpty Set<String> userGroups,
            @NotEmpty Set<String> allowedGroups,
            @NotBlank String purpose,
            @NotEmpty Set<String> allowedPurposes,
            @NotNull Classification classification,
            boolean identityVerified,
            boolean sourceActive,
            boolean aclFresh,
            boolean legalHoldRestricted,
            @PositiveOrZero int sourceAgeDays,
            @PositiveOrZero int maxSourceAgeDays,
            @DecimalMin("0.0") @DecimalMax("1.0") double citationCoverage,
            @DecimalMin("0.0") @DecimalMax("1.0") double minCitationCoverage,
            boolean piiDetected,
            boolean redactionAvailable,
            boolean humanApprovalComplete
    ) {}

    public record DecisionResult(Decision decision, List<String> blockers,
                                 List<String> obligations, String auditKey) {}

    public enum Classification { PUBLIC, INTERNAL, CONFIDENTIAL, RESTRICTED }
    public enum Decision { ALLOW, ALLOW_REDACTED, REVIEW, DENY }
}
