/* Copyright 2026 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ */
package cn.zhuatech.rag.service;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class CitationQualityService {
    public Result evaluate(Request request) {
        BigDecimal coverage = request.totalClaims() == 0 ? BigDecimal.ONE
            : BigDecimal.valueOf(Math.min(request.citedClaims(), request.totalClaims()))
                .divide(BigDecimal.valueOf(request.totalClaims()), 4, RoundingMode.HALF_UP);
        int score = coverage.multiply(BigDecimal.valueOf(60)).intValue();
        score += Math.min(25, request.authoritativeSources() * 5);
        score -= Math.min(25, request.staleSources() * 5);
        score -= Math.min(50, request.contradictionCount() * 20);
        if (!request.permissionFiltered()) score -= 30;
        score = Math.max(0, Math.min(100, score));

        String decision = !request.permissionFiltered() || request.contradictionCount() > 0 || score < 40 ? "BLOCK"
            : score < 75 ? "REVIEW" : "ALLOW";
        List<String> actions = new ArrayList<>();
        if (!request.permissionFiltered()) actions.add("先执行文档权限过滤再生成答案");
        if (coverage.compareTo(new BigDecimal("0.80")) < 0) actions.add("为未覆盖的关键结论补充可追溯引用");
        if (request.staleSources() > 0) actions.add("替换过期来源并重新执行检索排序");
        if (request.contradictionCount() > 0) actions.add("阻断答案并人工核对相互矛盾的来源");
        if (actions.isEmpty()) actions.add("引用覆盖和来源质量达标，可进入输出环节");
        return new Result(request.answerId(), coverage, score, decision, actions);
    }

    public record Request(@NotBlank String answerId, @Min(0) int totalClaims,
                          @Min(0) int citedClaims, @Min(0) int authoritativeSources,
                          @Min(0) int staleSources, @Min(0) int contradictionCount,
                          boolean permissionFiltered) {}
    public record Result(String answerId, BigDecimal citationCoverage, int qualityScore,
                         String decision, List<String> actions) {}
}
