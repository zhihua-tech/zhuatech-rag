/* Copyright 2026 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ */
package cn.zhuatech.rag.controller;

import cn.zhuatech.rag.common.ApiResponse;
import cn.zhuatech.rag.service.KnowledgeIndexPromotionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
 */
@RestController
@RequestMapping("/api/enterprise/rag")
public class KnowledgeIndexPromotionController {
    private final KnowledgeIndexPromotionService service;
    /**
     * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
     */
    public KnowledgeIndexPromotionController(KnowledgeIndexPromotionService service) { this.service = service; }
    /**
     * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
     */
    @PostMapping("/knowledge-index-promotion")
    public ApiResponse<KnowledgeIndexPromotionService.Assessment> assess(
            @Valid @RequestBody KnowledgeIndexPromotionService.Request request) {
        return ApiResponse.ok(service.assess(request));
    }
}
