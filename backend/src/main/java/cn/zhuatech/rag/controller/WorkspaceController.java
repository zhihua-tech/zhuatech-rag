/* Copyright 2026 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ */
package cn.zhuatech.rag.controller;

import cn.zhuatech.rag.ai.AiProvider;
import cn.zhuatech.rag.common.ApiResponse;
import cn.zhuatech.rag.dto.RagDto.*;
import cn.zhuatech.rag.service.RagService;
import cn.zhuatech.rag.service.GroundedAnswerGuardService;
import cn.zhuatech.rag.service.RetrievalQualityService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/shopfloor")
@PreAuthorize("hasAnyRole('DOMAIN_USER','ADMIN')")
public class WorkspaceController {
    private final RagService service;
    private final AiProvider ai;
    private final RetrievalQualityService qualityService;
    private final GroundedAnswerGuardService answerGuardService;

    public WorkspaceController(RagService service, AiProvider ai, RetrievalQualityService qualityService,
                               GroundedAnswerGuardService answerGuardService) {
        this.service = service;
        this.ai = ai;
        this.qualityService = qualityService;
        this.answerGuardService = answerGuardService;
    }

    @GetMapping("/dashboard")
    public ApiResponse<Dashboard> dashboard() { return ApiResponse.ok(service.shopfloorDashboard()); }

    @PostMapping("/work-orders/{id}/reports")
    public ApiResponse<ReportResult> report(@PathVariable Long id, @Valid @RequestBody ReportRequest request) {
        return ApiResponse.ok("反馈提交成功", service.report(id, request));
    }

    @PostMapping("/ai-preview")
    public ApiResponse<AiProvider.AiResult> preview(@RequestBody Map<String, String> body) {
        return ApiResponse.ok(ai.execute(body.getOrDefault("prompt", ""), Map.of("mode", "demo")));
    }

    @PostMapping("/retrieval-quality")
    public ApiResponse<RetrievalQualityService.QualityResult> evaluateRetrieval(@Valid @RequestBody RetrievalQualityService.QualityRequest request) {
        return ApiResponse.ok("检索质量评估完成", qualityService.evaluate(request));
    }

    @PostMapping("/answer-guard")
    public ApiResponse<GroundedAnswerGuardService.Result> guardAnswer(@Valid @RequestBody GroundedAnswerGuardService.Request request) {
        return ApiResponse.ok("回答安全门禁评估完成", answerGuardService.evaluate(request));
    }
}
