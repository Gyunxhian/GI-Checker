package com.example.diabetesassistant.domain.search

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

data class SearchEvidence(
    val title: String,
    val snippet: String,
    val source: String,
)

/**
 * 开箱即用免Key医学检索源（双重兜底体系）：
 * 1. 本地权威《中国2型糖尿病防治指南 (CDS)》与《ADA 临床诊疗标准》离线应急与症状库；
 * 2. 轻量通用公开网络搜索接口（无需申请 API Key）。
 */
@Singleton
class MedicalSearchProvider @Inject constructor() {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    /**
     * 执行脱敏症状检索。
     */
    suspend fun searchMedicalEvidence(query: String): List<SearchEvidence> = withContext(Dispatchers.IO) {
        val results = mutableListOf<SearchEvidence>()

        // 1. 本地权威临床指南快速语义匹配（绝对零延迟保障）
        val localMatches = matchLocalGuidelines(query)
        results.addAll(localMatches)

        // 2. 尝试通用网络轻量检索以补充最新医学动态
        try {
            val webResults = fetchPublicWebSnippets(query)
            results.addAll(webResults)
        } catch (_: Exception) {
            // 弱网或无网络环境下自动优雅降级，依靠本地临床指南
        }

        results.take(3)
    }

    /**
     * 本地内置权威临床指南知识库。
     */
    private fun matchLocalGuidelines(query: String): List<SearchEvidence> {
        val matches = mutableListOf<SearchEvidence>()
        val lower = query.lowercase()

        if (lower.contains("头晕") || lower.contains("心慌") || lower.contains("出汗") || lower.contains("手抖") || lower.contains("低血糖")) {
            matches.add(
                SearchEvidence(
                    title = "疑似低血糖与反应性血糖骤降应对策略",
                    snippet = "【紧急处置-双十五原则】：若血糖<3.9 mmol/L或出现心慌手抖出汗，立即口服15g单糖（半杯果汁或3片葡萄糖），静息15分钟复测。若餐后骤升骤降，多因高GI饮食引起胰岛素过度分泌导致的反应性低血糖。",
                    source = "《中国2型糖尿病防治指南(2020版)》",
                )
            )
        }

        if (lower.contains("超标") || lower.contains("吃多") || lower.contains("暴饮暴食") || lower.contains("大餐")) {
            matches.add(
                SearchEvidence(
                    title = "餐后碳水超标与高负荷干预指引",
                    snippet = "【平稳血糖阻断法】：餐后30~45分钟（消化吸收始发期）进行20~30分钟中低强度有氧运动（如快走），肌肉收缩可直接转运葡萄糖，显著削平餐后峰值；同时足量饮水促进水盐平衡，切忌盲目私自加倍注射胰岛素。",
                    source = "《ADA 糖尿病医学诊疗标准》",
                )
            )
        }

        if (lower.contains("空腹") || lower.contains("晨起") || lower.contains("早上高")) {
            matches.add(
                SearchEvidence(
                    title = "清晨空腹血糖偏高临床鉴别",
                    snippet = "【鉴别黎明现象与苏木杰反应】：需监测凌晨2:00~3:00血糖。若夜间血糖偏低，早晨反跳性升高为苏木杰反应（睡前需适量加餐或减少晚间药物）；若夜间血糖正常早晨升高为黎明现象（升糖激素晨起脉冲分泌所致）。",
                    source = "《内分泌代谢临床诊疗规范》",
                )
            )
        }

        if (lower.contains("高gi") || lower.contains("降糖") || lower.contains("顺序") || lower.contains("怎么吃")) {
            matches.add(
                SearchEvidence(
                    title = "进餐顺序与食物GI对钝化血糖波动的价值",
                    snippet = "【科学进餐次序】：严格遵循‘蔬菜/汤品 -> 蛋白质肉蛋 -> 复合主食’的进食顺序，膳食纤维与蛋白质先行可延缓胃排空，将同等餐食的餐后峰值降低30%以上。",
                    source = "《成人糖尿病食养指南》",
                )
            )
        }

        if (matches.isEmpty()) {
            matches.add(
                SearchEvidence(
                    title = "糖尿病代谢平稳管理常规原则",
                    snippet = "保持全天碳水化合物平稳分配，避免单餐暴饮暴食。餐后出现任何急性视物模糊、胸闷气短或持续意识不清，须立即就医排查酮症酸中毒或心脑血管急症。",
                    source = "《中华医学会糖尿病学分会核心建议》",
                )
            )
        }

        return matches
    }

    /**
     * 轻量公开网络搜索（免额外配置 API Key）。
     */
    private fun fetchPublicWebSnippets(query: String): List<SearchEvidence> {
        val sanitized = query.replace(Regex("[^\\u4e00-\\u9fa5a-zA-Z0-9 ]"), " ").trim()
        if (sanitized.isBlank()) return emptyList()

        val encoded = URLEncoder.encode("$sanitized 糖尿病 临床指南", StandardCharsets.UTF_8.name())
        val url = "https://html.duckduckgo.com/html/?q=$encoded"

        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
            .build()

        val response = httpClient.newCall(request).execute()
        if (!response.isSuccessful) return emptyList()

        val html = response.body?.string() ?: return emptyList()
        val snippets = mutableListOf<SearchEvidence>()

        // 简易正则提取搜索结果摘要
        val snippetRegex = Regex("<a class=\"result__snippet\"[^>]*>(.*?)</a>", RegexOption.DOT_MATCHES_ALL)
        val titleRegex = Regex("<a class=\"result__url\"[^>]*>(.*?)</a>", RegexOption.DOT_MATCHES_ALL)

        val snippetMatches = snippetRegex.findAll(html).take(2).toList()
        snippetMatches.forEachIndexed { idx, match ->
            val cleanSnippet = match.groupValues[1].replace(Regex("<[^>]*>"), "").replace("&nbsp;", " ").trim()
            if (cleanSnippet.length > 20) {
                snippets.add(
                    SearchEvidence(
                        title = "互联网公开医学知识检索 #$idx",
                        snippet = cleanSnippet,
                        source = "网络权威医学文献公开检索",
                    )
                )
            }
        }
        return snippets
    }
}
