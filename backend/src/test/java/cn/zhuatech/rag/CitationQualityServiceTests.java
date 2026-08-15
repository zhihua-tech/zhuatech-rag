/* Copyright 2026 上海如静知华信息科技有限公司 */
package cn.zhuatech.rag;

import cn.zhuatech.rag.service.CitationQualityService;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class CitationQualityServiceTests {
    private final CitationQualityService service = new CitationQualityService();

    @Test void blocksContradictoryUnfilteredAnswer() {
        var result = service.evaluate(new CitationQualityService.Request("A-100", 10, 5, 1, 2, 1, false));
        assertThat(result.decision()).isEqualTo("BLOCK");
        assertThat(result.actions()).hasSize(4);
    }

    @Test void allowsWellGroundedAnswer() {
        var result = service.evaluate(new CitationQualityService.Request("A-200", 8, 8, 4, 0, 0, true));
        assertThat(result.decision()).isEqualTo("ALLOW");
        assertThat(result.qualityScore()).isEqualTo(80);
    }
}
