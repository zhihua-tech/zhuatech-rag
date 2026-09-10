/* Copyright 2026 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ */
package cn.zhuatech.rag.controller;

import cn.zhuatech.rag.common.ApiResponse;
import cn.zhuatech.rag.service.RetrievalAccessPolicyService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/enterprise/rag")
public class RetrievalAccessPolicyController {
    private final RetrievalAccessPolicyService service;

    public RetrievalAccessPolicyController(RetrievalAccessPolicyService service) {
        this.service = service;
    }

    @PostMapping("/retrieval-access-decision")
    public ApiResponse<RetrievalAccessPolicyService.DecisionResult> decide(
            @Valid @RequestBody RetrievalAccessPolicyService.DecisionRequest request) {
        return ApiResponse.ok("检索访问策略决策完成", service.decide(request));
    }
}
