/* Copyright 2026 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ */
package cn.zhuatech.rag.workbench;
import org.junit.jupiter.api.Test;
import java.math.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.security.authentication.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.http.MediaType;
/** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
class AiWorkbenchServiceTest {
 /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
 @Test void coversAdditionalBoundaryOne(){var r=service.execute(Map.of("rankConstant",1,"topK",10,"sources",List.of(Map.of("name","s","weight",1,"hits",List.of("a","a")))));assertEquals(new BigDecimal("0.5"),((Map<?,?>)((List<?>)r.get("results")).get(0)).get("score"));}
 /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
 @Test void coversAdditionalBoundaryTwo(){assertEquals(List.of(),service.execute(Map.of("rankConstant",1,"topK",10,"sources",List.of(Map.of("name","s","weight",1,"hits",List.of())))).get("results"));}
 private final AiWorkbenchService service=new AiWorkbenchService();
 /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
 private Map<String,Object> sample(){return Map.<String,Object>ofEntries(Map.entry("rankConstant",60),Map.entry("topK",3),Map.entry("sources",List.of(Map.<String,Object>ofEntries(Map.entry("name","keyword"),Map.entry("weight",1),Map.entry("hits",List.of("a","b","a"))),Map.<String,Object>ofEntries(Map.entry("name","vector"),Map.entry("weight",1),Map.entry("hits",List.of("b","c"))))));}
 /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
 @Test void computesBusinessResult(){var result=service.execute(sample());assertEquals("b", ((Map<?,?>)((List<?>)result.get("results")).get(0)).get("id")); assertEquals(3, ((List<?>)result.get("results")).size());}
 /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
 @Test void coversFeatureBoundary(){Map<String,Object> invalid=new HashMap<>(sample());invalid.put("rankConstant",0);assertThrows(IllegalArgumentException.class,()->service.execute(invalid));}
 /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
 @Test void deterministicAndDoesNotMutateInput(){var input=sample();String before=input.toString();assertEquals(service.execute(input),service.execute(input));assertEquals(before,input.toString());}
 /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
 @Test void rejectsNull(){assertThrows(IllegalArgumentException.class,()->service.execute(null));}
 /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
 @Test void rejectsMissingFields(){assertThrows(IllegalArgumentException.class,()->service.execute(Map.of()));}
 /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
 @Test void rejectsUnknownFields(){var input=new HashMap<>(sample());input.put("__unknown",true);assertThrows(IllegalArgumentException.class,()->service.execute(input));}
 /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
 @Test void rejectsOversizedText(){assertThrows(IllegalArgumentException.class,()->service.execute(Map.of("payload","x".repeat(64001))));}
 /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
 @Test void rejectsDeepInput(){Map<String,Object> v=Map.of("x",1);for(int i=0;i<14;i++)v=Map.of("x",v);final var input=v;assertThrows(IllegalArgumentException.class,()->service.execute(input));}
 /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
 @Test void httpRequiresAuthentication()throws Exception{var mvc=MockMvcBuilders.standaloneSetup(new AiWorkbenchController(service)).build();mvc.perform(post("/api/workbench/rank-fusion").contentType(MediaType.APPLICATION_JSON).content("{\"rankConstant\":60,\"topK\":3,\"sources\":[{\"name\":\"keyword\",\"weight\":1,\"hits\":[\"a\",\"b\",\"a\"]},{\"name\":\"vector\",\"weight\":1,\"hits\":[\"b\",\"c\"]}]}")).andExpect(status().isUnauthorized());}
 /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
 @Test void httpAllowsAuthenticatedComputation()throws Exception{var mvc=MockMvcBuilders.standaloneSetup(new AiWorkbenchController(service)).build();mvc.perform(post("/api/workbench/rank-fusion").principal(new TestingAuthenticationToken("test-user","unused","ROLE_OPERATOR")).contentType(MediaType.APPLICATION_JSON).content("{\"rankConstant\":60,\"topK\":3,\"sources\":[{\"name\":\"keyword\",\"weight\":1,\"hits\":[\"a\",\"b\",\"a\"]},{\"name\":\"vector\",\"weight\":1,\"hits\":[\"b\",\"c\"]}]}")).andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true));}
 /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
 @Test void httpRejectsInvalidInput()throws Exception{var mvc=MockMvcBuilders.standaloneSetup(new AiWorkbenchController(service)).build();mvc.perform(post("/api/workbench/rank-fusion").principal(new TestingAuthenticationToken("test-user","unused","ROLE_OPERATOR")).contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isBadRequest()).andExpect(jsonPath("$.success").value(false));}
 /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
 @Test void rejectsAnonymousAuthenticationObject(){var actor=new AnonymousAuthenticationToken("test-key","anonymous",List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ANONYMOUS")));assertEquals(401,new AiWorkbenchController(service).run(sample(),actor).getStatusCode().value());}
}
