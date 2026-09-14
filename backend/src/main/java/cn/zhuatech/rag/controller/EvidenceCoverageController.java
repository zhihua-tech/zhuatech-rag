/* Copyright 2026 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ */
package cn.zhuatech.rag.controller;

import cn.zhuatech.rag.common.ApiResponse;
import cn.zhuatech.rag.service.EvidenceCoverageService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/enterprise/rag")
public class EvidenceCoverageController {
    private final EvidenceCoverageService service;

    public EvidenceCoverageController(EvidenceCoverageService service) {
        this.service = service;
    }

    @PostMapping("/evidence-coverage")
    public ApiResponse<EvidenceCoverageService.Result> evaluate(
            @Valid @RequestBody EvidenceCoverageService.Request request) {
        return ApiResponse.ok("逐项引用证据核验完成", service.evaluate(request));
    }
}
