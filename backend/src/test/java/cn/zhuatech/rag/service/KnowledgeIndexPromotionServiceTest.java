/* Copyright 2026 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ */
package cn.zhuatech.rag.service;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
 */
class KnowledgeIndexPromotionServiceTest {
    private final KnowledgeIndexPromotionService service = new KnowledgeIndexPromotionService();
    /**
     * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
     */
    @Test void promotesTrustedIndex() {
        var result = service.assess(new KnowledgeIndexPromotionService.Request("I1", true, true, true, true,
                95, 90, 92, 90, 3, 10, true, true));
        assertThat(result.decision()).isEqualTo(KnowledgeIndexPromotionService.Decision.PROMOTE);
    }
    /**
     * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
     */
    @Test void canariesStaleIndex() {
        var result = service.assess(new KnowledgeIndexPromotionService.Request("I2", true, true, true, true,
                95, 90, 92, 90, 20, 10, true, true));
        assertThat(result.decision()).isEqualTo(KnowledgeIndexPromotionService.Decision.CANARY);
    }
    /**
     * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
     */
    @Test void blocksUnsafeIndex() {
        var result = service.assess(new KnowledgeIndexPromotionService.Request("I3", false, false, false, false,
                60, 90, 60, 90, 3, 10, false, false));
        assertThat(result.blockers()).hasSize(8);
    }
}
