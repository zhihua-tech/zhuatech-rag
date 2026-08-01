/* Copyright 2026 上海如静知华信息科技有限公司 */
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

/** 对 RAG 检索结果做引用覆盖率和相似度门禁，避免低依据回答直接展示。 */
@Service
public class RetrievalQualityService {
    public QualityResult evaluate(QualityRequest request) {
        if (request.citedChunks() > request.retrievedChunks()) {
            throw new BusinessException("引用片段数量不能超过检索片段数量");
        }

        double coverage = round(request.citedChunks() * 1.0 / request.retrievedChunks());
        List<String> warnings = new ArrayList<>();
        if (coverage < 0.6) warnings.add("引用覆盖率低于 60%");
        if (request.averageSimilarity() < 0.75) warnings.add("平均相似度低于 0.75");
        if (request.sensitiveQuery()) warnings.add("敏感问题需要人工复核");

        String confidence = coverage >= 0.8 && request.averageSimilarity() >= 0.85 ? "HIGH"
            : coverage >= 0.6 && request.averageSimilarity() >= 0.75 ? "MEDIUM" : "LOW";
        String recommendation = request.sensitiveQuery() ? "HUMAN_REVIEW"
            : coverage < 0.6 || request.averageSimilarity() < 0.75 ? "RETRIEVE_MORE" : "ANSWER";
        int suggestedTopK = "RETRIEVE_MORE".equals(recommendation) ? Math.min(12, request.retrievedChunks() + 3) : request.retrievedChunks();

        return new QualityResult(coverage, confidence, recommendation, suggestedTopK, List.copyOf(warnings));
    }

    private double round(double value) {
        return Math.round(value * 1000d) / 1000d;
    }

    public record QualityRequest(
        @NotBlank(message = "请输入检索问题") String query,
        @Positive(message = "检索片段数量必须大于 0") int retrievedChunks,
        @PositiveOrZero(message = "引用片段数量不能为负数") int citedChunks,
        @DecimalMin(value = "0.0", message = "相似度不能小于 0")
        @DecimalMax(value = "1.0", message = "相似度不能大于 1") double averageSimilarity,
        boolean sensitiveQuery
    ) {}

    public record QualityResult(
        double citationCoverage,
        String confidence,
        String recommendation,
        int suggestedTopK,
        List<String> warnings
    ) {}
}
