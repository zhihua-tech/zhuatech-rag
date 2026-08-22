/* Copyright 2026 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ */
package cn.zhuatech.rag.service;

import cn.zhuatech.rag.common.BusinessException;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/** 在答案展示前检查声明引用、来源时效和安全信号，降低无依据回答进入业务流程的风险。 */
@Service
public class GroundedAnswerGuardService {
    public Result evaluate(Request request) {
        if (request.citedClaims() > request.totalClaims()) throw new BusinessException("已引用声明数不能超过声明总数");
        double coverage = round(request.citedClaims() * 1d / request.totalClaims());
        double freshnessPenalty = Math.min(20d, request.sourceFreshnessDays() / 9d);
        double riskScore = Math.min(100d, round((1d - coverage) * 45d
            + (1d - request.averageSimilarity()) * 35d + freshnessPenalty
            + (request.promptInjectionDetected() ? 50d : 0d)
            + (request.sensitiveDataDetected() ? 50d : 0d)));

        String decision = request.promptInjectionDetected() || request.sensitiveDataDetected() ? "BLOCK"
            : coverage < 0.7 || request.averageSimilarity() < 0.75 || request.sourceFreshnessDays() > 180 ? "HUMAN_REVIEW"
            : coverage < 0.9 || request.averageSimilarity() < 0.85 || request.sourceFreshnessDays() > 90 ? "RETRIEVE_MORE" : "ALLOW";
        List<String> reasons = new ArrayList<>();
        if (coverage < 0.9) reasons.add("部分关键声明缺少可核验引用");
        if (request.averageSimilarity() < 0.85) reasons.add("支撑片段与问题的平均相似度不足");
        if (request.sourceFreshnessDays() > 90) reasons.add("引用来源可能已经过期");
        if (request.promptInjectionDetected()) reasons.add("检测到提示注入特征");
        if (request.sensitiveDataDetected()) reasons.add("答案可能包含敏感信息");
        if (reasons.isEmpty()) reasons.add("声明、引用和来源时效均满足展示门槛");
        String nextAction = switch (decision) {
            case "BLOCK" -> "阻断答案并提交安全审计";
            case "HUMAN_REVIEW" -> "交由知识管理员核验后发布";
            case "RETRIEVE_MORE" -> "扩大检索范围并重新生成引用答案";
            default -> "允许展示，并保留引用审计记录";
        };
        return new Result(request.question(), coverage, riskScore, decision, List.copyOf(reasons), nextAction);
    }

    private double round(double value) { return Math.round(value * 10d) / 10d; }

    public record Request(@NotBlank String question,
                          @Positive int totalClaims,
                          @PositiveOrZero int citedClaims,
                          @DecimalMin("0.0") @DecimalMax("1.0") double averageSimilarity,
                          @PositiveOrZero int sourceFreshnessDays,
                          boolean promptInjectionDetected,
                          boolean sensitiveDataDetected) {}
    public record Result(String question, double citationCoverage, double riskScore,
                         String decision, List<String> reasons, String nextAction) {}
}
