/* Copyright 2026 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ */
package cn.zhuatech.rag.service;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class KnowledgeIndexPromotionService {
    public Assessment assess(Request request) {
        List<String> blockers = new ArrayList<>();
        List<String> actions = new ArrayList<>();
        if (!request.sourceOwnershipVerified()) blockers.add("知识来源所有权与授权未验证");
        if (!request.accessAclSynchronized()) blockers.add("源系统访问权限未同步到索引");
        if (!request.piiScanPassed()) blockers.add("个人信息扫描未通过");
        if (!request.malwareScanPassed()) blockers.add("文档安全扫描未通过");
        if (request.citationEvalScore() < request.minCitationEvalScore()) blockers.add("引用评测低于晋级阈值");
        if (request.retrievalEvalScore() < request.minRetrievalEvalScore()) blockers.add("检索评测低于晋级阈值");
        if (!request.rollbackSnapshotReady()) blockers.add("生产索引回滚快照未就绪");
        if (!request.approvalComplete()) blockers.add("知识索引晋级审批未完成");
        if (!blockers.isEmpty()) {
            actions.add("阻断索引晋级并修复安全、权限或质量缺口");
            return new Assessment(Decision.BLOCKED, blockers, actions);
        }
        if (request.staleSourcePercent() > request.maxStaleSourcePercent()) {
            actions.add("仅灰度发布并清理过期来源后重新评估");
            return new Assessment(Decision.CANARY, blockers, actions);
        }
        actions.add("批准索引晋级并监控引用、召回、权限和新鲜度");
        return new Assessment(Decision.PROMOTE, blockers, actions);
    }

    public record Request(@NotBlank String indexVersion, boolean sourceOwnershipVerified,
                          boolean accessAclSynchronized, boolean piiScanPassed, boolean malwareScanPassed,
                          @DecimalMin("0.0") double citationEvalScore,
                          @DecimalMin("0.0") double minCitationEvalScore,
                          @DecimalMin("0.0") double retrievalEvalScore,
                          @DecimalMin("0.0") double minRetrievalEvalScore,
                          @DecimalMin("0.0") double staleSourcePercent,
                          @DecimalMin("0.0") double maxStaleSourcePercent,
                          boolean rollbackSnapshotReady, boolean approvalComplete) {}
    public record Assessment(Decision decision, List<String> blockers, List<String> actions) {}
    public enum Decision { PROMOTE, CANARY, BLOCKED }
}
