package nnu.wyz.systemMS.service.iml;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.model.dto.DscCode.GeoActModelDTO;
import nnu.wyz.systemMS.model.entity.codeModel.GeoActModel;
import nnu.wyz.systemMS.model.entity.codeModel.GeoActModelParams.GeoActModelInnerParams;
import nnu.wyz.systemMS.model.entity.codeModel.GeoActModelParams.GeoActParamsConstrains;
import nnu.wyz.systemMS.model.entity.codeModel.ModelRecommend.DifyKnowledgePiece;
import nnu.wyz.systemMS.model.entity.codeModel.ModelRecommend.Scenario;
import nnu.wyz.systemMS.service.GeoActAiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

@Service
public class GeoActAiServiceImpl implements GeoActAiService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${DIFY_API_BASE}")
    private String difyIp;

    @Value("${DIFY_KB_API_KEY}")
    private String difyKbKey;

    @Value("${DIFY_CHAT_API_KEY}")
    private String difyChatKey;

        @Override
    public CommonResult<JSONObject> generateDescription(String script) {

        String prompt =
                "你现在的任务是根据给定的 Python 模型源码生成一个严格格式的 JSON 对象。" +
                        "\n⚠️ 请注意：不要输出思考链，不要输出<think>标签，不要输出中间推理过程。" +
                        "\n⚠️ 只输出最终 JSON，且不能包含任何解释文字。" +
                        "\n请按以下四个字段生成 JSON：" +
                        "\n1. description: 字符串，描述该模型主要作用（50字内）" +
                        "\n2. core_scenario: 字符串，推测该模型适用的场景（尽可能全面）" +
                        "\n3. keywords: 字符串数组，列出与该模型相关的五个关键词" +
                        "\n4. common_questions: 字符串数组，三个可能推荐用户使用该模型的问题" +
                        "\n输出格式示例（请严格遵守 JSON 标准格式）：" +
                        "\n{\"description\":\"...\", \"core_scenario\":\"...\", \"keywords\":[\"...\",\"...\",\"...\",\"...\",\"...\"], \"common_questions\":[\"...\",\"...\",\"...\"]}" +
                        "\n下面是模型源码：" +
                        "\n----------------------------------\n" +
                        script +
                        "\n----------------------------------";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("model", "deepseek-r1:7b");
        body.put("prompt", prompt);
        body.put("stream", false);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        final int MAX_RETRY = 3;

        for (int retry = 1; retry <= MAX_RETRY; retry++) {
            try {

                ResponseEntity<String> response = restTemplate.postForEntity(
                        "http://116as214wo099.vicp.fun/api/generate",
                        requestEntity,
                        String.class
                );

                if (!response.getStatusCode().is2xxSuccessful()) {
                    return CommonResult.failed("LLM服务返回错误状态: " + response.getStatusCode());
                }

                String bodyText = response.getBody();
                System.out.println(bodyText);

                // ✅ 外层 JSON
                JSONObject root = JSONObject.parseObject(bodyText);
                String jsonStr = root.getString("response");

                if (jsonStr == null || jsonStr.trim().isEmpty()) {
                    throw new RuntimeException("LLM 未返回 response 内容");
                }

                // ✅ 去除 <think> 内容（DeepSeek-R1 常见问题）
                jsonStr = removeThinkBlocks(jsonStr);

                // ✅ 内部 JSON
                JSONObject inner = JSONObject.parseObject(jsonStr);

                // ✅ 字段完整性和类型校验
                validateModelJson(inner);

                Scenario scenario = new Scenario();
                scenario.setCoreScenario(inner.getString("core_scenario"));
                scenario.setKeywords(inner.getJSONArray("keywords").toJavaList(String.class));
                scenario.setCommonQuestions(inner.getJSONArray("common_questions").toJavaList(String.class));

                JSONObject res = new JSONObject();
                res.put("description", inner.getString("description"));
                res.put("scenario", scenario);

                System.out.println(res.toJSONString());

                // ✅ 直接返回 JSON 对象（keywords 和 common_questions 为 JSONArray）
                return CommonResult.success(res, "生成模型描述成功");

            } catch (Exception e) {

                if (retry == MAX_RETRY) {
                    return CommonResult.failed("调用LLM服务异常（重试" + MAX_RETRY + "次仍失败）: " + e.getMessage());
                }

                System.err.println("解析失败，第 " + retry + " 次重试。错误原因：" + e.getMessage());
            }
        }

        return CommonResult.failed("未知错误");
    }

    public JSONObject queryDifyDescription(String script) {
        String url = difyIp + "/chat-messages";
        // 构造请求体
        Map<String, Object> payload = new HashMap<>();
        payload.put("inputs", new HashMap<String, Object>());
        payload.put("query", script);
        payload.put("response_mode", "blocking");
        payload.put("conversation_id", null);
        payload.put("user", "admin");

        try {
            WebClient client = WebClient.builder()
                    .baseUrl(difyIp)
                    .defaultHeader("Authorization", "Bearer " + difyChatKey)
                    .defaultHeader("Content-Type", "application/json")
                    .build();

            JsonNode response = client.post()
                    .uri("/chat-messages")
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (response == null || !response.has("answer")) {
                throw new RuntimeException("Dify API未返回 answer 内容");
            }

            String answer = response.get("answer").asText();

            // ✅ 去除 <think> 内容
            answer = removeThinkBlocks(answer);

            // ✅ 转成 JSONObject
            JSONObject json;
            try {
                json = JSONObject.parseObject(answer);
            } catch (Exception ex) {
                throw new RuntimeException("返回结果不是合法 JSON: " + answer);
            }

            // ✅ 校验字段完整性和类型
            validateModelJson(json);

            return json;

        } catch (Exception e) {
            // 统一抛出异常，由调用方处理
            throw new RuntimeException("调用Dify API失败: " + e.getMessage(), e);
        }
    }

    public boolean uploadToKnowledgeBase(DifyKnowledgePiece piece) {
        String url = difyIp +
                "/datasets/f98ab798-2ced-48be-922e-1874572fab8c" +
                "/documents/536f21e3-e020-43a4-a21f-3eb258c94924/segments";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + difyKbKey);

        // ✅ content必须为纯字符串
        String content = buildPlainTextContent(piece);

        Map<String, Object> segment = new HashMap<>();
        segment.put("content", content);

        // ✅ answer可选，必须是string
        segment.put("answer", piece.getId());

        // ✅ keywords必须是字符串数组
        List<String> keywords = piece.getScenario() != null
                ? piece.getScenario().getKeywords()
                : new ArrayList<>();
        segment.put("keywords", keywords);

        Map<String, Object> payload = new HashMap<>();
        payload.put("segments", Collections.singletonList(segment));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            System.out.println("Dify KB Response: " + response.getBody());
            return response.getStatusCode().is2xxSuccessful();
        } catch (HttpClientErrorException e) {
            System.err.println("Dify KB error: " + e.getResponseBodyAsString());
            return false;
        }
    }

    public String buildPlainTextContent(DifyKnowledgePiece piece) {
        StringBuilder sb = new StringBuilder();

        sb.append("Model Name: ").append(piece.getName()).append("\n");
        sb.append("Model ID: ").append(piece.getId()).append("\n");
        sb.append("Description: ").append(piece.getDescription() == null ? "" : piece.getDescription()).append("\n\n");

        sb.append("== Scenario ==\n");
        if (piece.getScenario() != null) {
            sb.append("Core Scenario: ").append(piece.getScenario().getCoreScenario()).append("\n");
            sb.append("Keywords: ").append(String.join(", ", piece.getScenario().getKeywords())).append("\n");
            sb.append("Common Questions:\n");
            for (String q : piece.getScenario().getCommonQuestions()) {
                sb.append("- ").append(q).append("\n");
            }
        } else {
            sb.append("No scenario provided.\n");
        }
        sb.append("\n");

        sb.append("== Parameters ==\n");
        sb.append("Inputs:\n");
        appendParamsList(sb, piece.getParams() != null ? piece.getParams().getInputs() : null);

        sb.append("Options:\n");
        appendParamsList(sb, piece.getParams() != null ? piece.getParams().getOptions() : null);

        sb.append("Outputs:\n");
        appendParamsList(sb, piece.getParams() != null ? piece.getParams().getOutputs() : null);

        return sb.toString();
    }

    private void appendParamsList(StringBuilder sb, List<GeoActModelInnerParams> list) {
        if (list == null || list.isEmpty()) {
            sb.append("  (none)\n");
            return;
        }

        for (GeoActModelInnerParams p : list) {
            sb.append("  - Name: ").append(p.getName()).append("\n");
            sb.append("    Identifier: ").append(p.getIdentifier()).append("\n");
            sb.append("    Category: ").append(p.getCategory()).append("\n");
            sb.append("    Type: ").append(p.getType()).append("\n");
            sb.append("    Optional: ").append(p.isOptional()).append("\n");

            GeoActParamsConstrains cons = p.getConstraints();
            if (cons != null) {
                sb.append("    Constraints: ").append(cons.toString()).append("\n");
            }
            sb.append("\n");
        }
    }





    private void validateModelJson(JSONObject obj) {

        Set<String> required = new HashSet<String>();
        required.add("description");
        required.add("core_scenario");
        required.add("keywords");
        required.add("common_questions");

        // 缺字段检查
        for (String key : required) {
            if (!obj.containsKey(key)) {
                throw new RuntimeException("JSON 缺少字段：" + key);
            }
        }

        // 多字段检查
        if (obj.keySet().size() != required.size()) {
            throw new RuntimeException("JSON 存在额外字段：" + obj.keySet());
        }

        // 类型检查
        if (!(obj.get("description") instanceof String)) {
            throw new RuntimeException("description 必须是 string");
        }

        if (!(obj.get("core_scenario") instanceof String)) {
            throw new RuntimeException("core_scenario 必须是 string");
        }

        if (!(obj.get("keywords") instanceof JSONArray)) {
            throw new RuntimeException("keywords 必须是数组");
        }
        if (!(obj.get("common_questions") instanceof JSONArray)) {
            throw new RuntimeException("common_questions 必须是数组");
        }

        JSONArray keywords = obj.getJSONArray("keywords");
        if (keywords.size() != 5) {
            throw new RuntimeException("keywords 必须包含 5 个字符串");
        }
        for (Object k : keywords) {
            if (!(k instanceof String)) {
                throw new RuntimeException("keywords 中每个元素必须为 string");
            }
        }

        JSONArray questions = obj.getJSONArray("common_questions");
        if (questions.size() != 3) {
            throw new RuntimeException("common_questions 必须包含 3 个字符串");
        }
        for (Object q : questions) {
            if (!(q instanceof String)) {
                throw new RuntimeException("common_questions 中每个元素必须为 string");
            }
        }
    }

    private String removeThinkBlocks(String input) {
        if (input == null) return "";
        return input.replaceAll("(?s)<think>.*?</think>", "").trim();
    }


}
