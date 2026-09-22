/* Copyright 2026 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ */
package cn.zhuatech.rag;
import org.junit.jupiter.api.*; import org.springframework.beans.factory.annotation.Autowired; import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc; import org.springframework.boot.test.context.SpringBootTest; import org.springframework.http.MediaType; import org.springframework.test.web.servlet.MockMvc; import java.util.regex.*; import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*; import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
/**
 * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
 */
@SpringBootTest @AutoConfigureMockMvc class RagApiIntegrationTests {
    @Autowired MockMvc mvc; private String operatorToken; private String plannerToken;
    /**
     * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
     */
    @BeforeEach void login()throws Exception{operatorToken=token("operator","Demo@2026");plannerToken=token("planner","Demo@2026");}
    /**
     * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
     */
    private String token(String u,String p)throws Exception{String json=mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{\"username\":\""+u+"\",\"password\":\""+p+"\"}")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();Matcher matcher=Pattern.compile("\\\"token\\\":\\\"([^\\\"]+)\\\"").matcher(json);if(!matcher.find())throw new AssertionError("登录响应中缺少 token");return matcher.group(1);}
    /**
     * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
     */
    @Test void operatorCanReadShopfloorDashboard()throws Exception{mvc.perform(get("/api/shopfloor/dashboard").header("Authorization","Bearer "+operatorToken)).andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true)).andExpect(jsonPath("$.data.metrics[0].label").value("文档计划数量"));}
    /**
     * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
     */
    @Test void plannerCanReadWorkRecords()throws Exception{mvc.perform(get("/api/admin/work-orders").header("Authorization","Bearer "+plannerToken)).andExpect(status().isOk()).andExpect(jsonPath("$.data.length()").value(3));}
    /**
     * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
     */
    @Test void operatorCanSubmitProductionReport()throws Exception{mvc.perform(post("/api/shopfloor/work-orders/1/reports").header("Authorization","Bearer "+operatorToken).contentType(MediaType.APPLICATION_JSON).content("{\"operationName\":\"索引复核\",\"goodQty\":2,\"defectQty\":1,\"remark\":\"数据完整\"}")).andExpect(status().isOk()).andExpect(jsonPath("$.message").value("反馈提交成功")).andExpect(jsonPath("$.data.completedQty").value(3974));}
    /**
     * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
     */
    @Test void operatorCanEvaluateRetrievalQuality()throws Exception{mvc.perform(post("/api/shopfloor/retrieval-quality").header("Authorization","Bearer "+operatorToken).contentType(MediaType.APPLICATION_JSON).content("{\"query\":\"客户合同中的退款条款是什么\",\"retrievedChunks\":5,\"citedChunks\":2,\"averageSimilarity\":0.82,\"sensitiveQuery\":false}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.citationCoverage").value(0.4))
        .andExpect(jsonPath("$.data.recommendation").value("RETRIEVE_MORE"))
        .andExpect(jsonPath("$.data.suggestedTopK").value(8));}
    /**
     * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
     */
    @Test void operatorCanRunGroundedAnswerGuard()throws Exception{mvc.perform(post("/api/shopfloor/answer-guard").header("Authorization","Bearer "+operatorToken).contentType(MediaType.APPLICATION_JSON).content("{\"question\":\"现行退款条款是什么\",\"totalClaims\":5,\"citedClaims\":3,\"averageSimilarity\":0.82,\"sourceFreshnessDays\":240,\"promptInjectionDetected\":false,\"sensitiveDataDetected\":false}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.citationCoverage").value(0.6))
        .andExpect(jsonPath("$.data.riskScore").value(44.3))
        .andExpect(jsonPath("$.data.decision").value("HUMAN_REVIEW"));}
    /**
     * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
     */
    @Test void operatorCanDecideRetrievalAccess()throws Exception{mvc.perform(post("/api/enterprise/rag/retrieval-access-decision").header("Authorization","Bearer "+operatorToken).contentType(MediaType.APPLICATION_JSON).content("{\"tenantId\":\"tenant-a\",\"documentTenantId\":\"tenant-a\",\"userGroups\":[\"sales\"],\"allowedGroups\":[\"sales\"],\"purpose\":\"support\",\"allowedPurposes\":[\"support\"],\"classification\":\"INTERNAL\",\"identityVerified\":true,\"sourceActive\":true,\"aclFresh\":true,\"legalHoldRestricted\":false,\"sourceAgeDays\":10,\"maxSourceAgeDays\":30,\"citationCoverage\":0.9,\"minCitationCoverage\":0.8,\"piiDetected\":false,\"redactionAvailable\":true,\"humanApprovalComplete\":true}"))
        .andExpect(status().isOk()).andExpect(jsonPath("$.data.decision").value("ALLOW"))
        .andExpect(jsonPath("$.data.auditKey").isNotEmpty());}
    /**
     * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
     */
    @Test void anonymousRequestIsDenied()throws Exception{mvc.perform(get("/api/admin/dashboard")).andExpect(status().isUnauthorized());}
}
