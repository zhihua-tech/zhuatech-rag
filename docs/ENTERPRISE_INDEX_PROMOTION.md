# 企业级 RAG 知识索引晋级

`POST /api/enterprise/rag/knowledge-index-promotion` 校验来源授权、ACL 同步、隐私与恶意文件扫描、引用和检索评测、来源新鲜度、回滚快照及审批，返回 `PROMOTE / CANARY / BLOCKED`。

生产环境应保存语料清单、切分与嵌入版本、权限快照、评测集和审批证据，并在索引晋级后持续检测越权召回、过期来源与无依据回答。
