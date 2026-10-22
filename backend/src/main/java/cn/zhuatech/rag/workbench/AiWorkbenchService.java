/* Copyright 2026 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ */
package cn.zhuatech.rag.workbench;
import org.springframework.stereotype.Service;
import java.math.*;
import java.util.*;
import static cn.zhuatech.rag.workbench.WorkbenchInput.*;
/** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
@Service
public class AiWorkbenchService {
    /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
    public Map<String,Object> execute(Map<String,Object> r){
        validate(r);

        keys(r,"rankConstant","topK","sources");int k=integer(r,"rankConstant",1,1000),top=integer(r,"topK",1,100);
        Map<String,Double> scores=new TreeMap<>();Map<String,List<String>> provenance=new TreeMap<>();Set<String> names=new HashSet<>();
        for(var source:rows(r,"sources",1,20)){keys(source,"name","weight","hits");String name=id(source,"name");if(!names.add(name))throw new IllegalArgumentException("来源名称重复");
            double weight=decimal(source,"weight",0.0001,100).doubleValue();var hits=strings(source,"hits",0,200);Set<String> seen=new HashSet<>();
            for(int i=0;i<hits.size();i++){String key=hits.get(i);if(!seen.add(key))continue;scores.merge(key,weight/(k+i+1),Double::sum);provenance.computeIfAbsent(key,x->new ArrayList<>()).add(name);}
        }
        List<Map<String,Object>> result=scores.entrySet().stream().sorted(Map.Entry.<String,Double>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey())).limit(top).map(e->Map.<String,Object>of("id",e.getKey(),"score",round(e.getValue()),"sources",List.copyOf(provenance.get(e.getKey())))).toList();
        return Map.of("results",result,"uniqueCandidates",scores.size(),"algorithm","weighted-rrf");

    }
}
