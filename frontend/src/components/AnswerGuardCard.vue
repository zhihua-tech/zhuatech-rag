<!-- Copyright 2026 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ -->
<script setup>
import { ref } from 'vue'
import http from '../api/http'

const result = ref(null)
const loading = ref(false)
const decisionName = { ALLOW: '允许展示', RETRIEVE_MORE: '补充检索', HUMAN_REVIEW: '人工复核', BLOCK: '安全阻断' }

const localEvaluate = payload => {
  const citationCoverage = Number((payload.citedClaims / payload.totalClaims).toFixed(1))
  const riskScore = Math.min(100, Number((((1 - citationCoverage) * 45) + ((1 - payload.averageSimilarity) * 35) + Math.min(20, payload.sourceFreshnessDays / 9)).toFixed(1)))
  const decision = citationCoverage < .7 || payload.averageSimilarity < .75 || payload.sourceFreshnessDays > 180 ? 'HUMAN_REVIEW' : citationCoverage < .9 || payload.averageSimilarity < .85 || payload.sourceFreshnessDays > 90 ? 'RETRIEVE_MORE' : 'ALLOW'
  return { citationCoverage, riskScore, decision, nextAction: decision === 'HUMAN_REVIEW' ? '交由知识管理员核验后发布' : '扩大检索范围并重新生成引用答案' }
}

const runGuard = async () => {
  loading.value = true
  const payload = { question: '现行退款条款是什么', totalClaims: 5, citedClaims: 3, averageSimilarity: .82, sourceFreshnessDays: 240, promptInjectionDetected: false, sensitiveDataDetected: false }
  try {
    result.value = import.meta.env.VITE_DEMO_MODE === 'true'
      ? localEvaluate(payload)
      : (await http.post('/shopfloor/answer-guard', payload)).data.data
  } finally { loading.value = false }
}
</script>

<template>
  <section class="guard-card">
    <div><span>可信回答治理</span><h2>回答引用与安全门禁</h2><p>答案展示前校验声明引用、来源时效与安全信号。</p></div>
    <div v-if="result" class="guard-result" :class="result.decision.toLowerCase()"><strong>{{ decisionName[result.decision] }}</strong><span>引用覆盖 {{ Math.round(result.citationCoverage * 100) }}% · 风险 {{ result.riskScore }}</span><small>{{ result.nextAction }}</small></div>
    <button :disabled="loading" @click="runGuard">{{ loading ? '评估中' : result ? '重新评估' : '运行门禁演示' }}</button>
  </section>
</template>

<style scoped>
.guard-card{display:grid;grid-template-columns:1.4fr 1fr auto;align-items:center;gap:24px;margin:16px 0;padding:18px 22px;border:1px solid #dfe5eb;border-radius:12px;background:linear-gradient(100deg,#fff,#f7fafc);box-shadow:0 5px 18px rgba(28,43,57,.04)}.guard-card span{color:#73818d;font-size:11px}.guard-card h2{margin:4px 0;font-size:17px}.guard-card p{margin:0;color:#788791;font-size:11px}.guard-card button{padding:10px 15px;border:0;border-radius:8px;background:#244b72;color:#fff;font-size:12px}.guard-card button:disabled{opacity:.6}.guard-result{display:flex;flex-direction:column;gap:3px;padding-left:14px;border-left:3px solid #d29535}.guard-result.human_review,.guard-result.block{border-color:#c74d49}.guard-result.allow{border-color:#258d6c}.guard-result strong{font-size:14px}.guard-result small{color:#687983;font-size:10px}@media(max-width:800px){.guard-card{grid-template-columns:1fr}.guard-result{padding:10px 0 0;border-top:3px solid #d29535;border-left:0}}
</style>
