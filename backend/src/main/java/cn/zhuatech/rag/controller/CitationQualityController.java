/* Copyright 2026 上海如静知华信息科技有限公司 */
package cn.zhuatech.rag.controller;

import cn.zhuatech.rag.common.ApiResponse;
import cn.zhuatech.rag.service.CitationQualityService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rag/insights")
public class CitationQualityController {
    private final CitationQualityService service;
    public CitationQualityController(CitationQualityService service) { this.service = service; }

    @PostMapping("/citation-quality")
    public ApiResponse<CitationQualityService.Result> evaluate(@Valid @RequestBody CitationQualityService.Request request) {
        return ApiResponse.ok(service.evaluate(request));
    }
}
