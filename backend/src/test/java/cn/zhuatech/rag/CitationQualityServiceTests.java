/* Copyright 2026 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ */
package cn.zhuatech.rag;

import cn.zhuatech.rag.service.CitationQualityService;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
 */
class CitationQualityServiceTests {
    private final CitationQualityService service = new CitationQualityService();

    /**
     * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
     */
    @Test void blocksContradictoryUnfilteredAnswer() {
        var result = service.evaluate(new CitationQualityService.Request("A-100", 10, 5, 1, 2, 1, false));
        assertThat(result.decision()).isEqualTo("BLOCK");
        assertThat(result.actions()).hasSize(4);
    }

    /**
     * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
     */
    @Test void allowsWellGroundedAnswer() {
        var result = service.evaluate(new CitationQualityService.Request("A-200", 8, 8, 4, 0, 0, true));
        assertThat(result.decision()).isEqualTo("ALLOW");
        assertThat(result.qualityScore()).isEqualTo(80);
    }
}
