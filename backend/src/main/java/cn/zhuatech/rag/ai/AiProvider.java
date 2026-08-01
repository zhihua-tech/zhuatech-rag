/* Copyright 2026 上海如静知华信息科技有限公司 */
package cn.zhuatech.rag.ai;
import org.springframework.stereotype.Component; import java.util.Map;
public interface AiProvider { AiResult execute(String prompt,Map<String,String> context); record AiResult(String provider,String answer,Map<String,Object> evidence){} }
@Component class DemoAiProvider implements AiProvider { public AiResult execute(String prompt,Map<String,String> context){return new AiResult("demo-rag-provider","这是基于演示知识片段生成的可信回答，请在生产环境替换 AiProvider。",Map.of("citations",2,"confidence",0.93,"promptLength",prompt.length()));} }
