package nnu.wyz.systemMS.service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import nnu.wyz.domain.CommonResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;
import java.util.concurrent.*;

@Service
public class RagService {

    @Value("${DIFY_API_BASE}")
    private String difyApiBase;

    @Value("${DIFY_KB_API_KEY}")
    private String apiKey;

    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    public CommonResult<Map<String, Object>> queryMultipleMethods(List<String> methods) throws ExecutionException, InterruptedException {
        List<CompletableFuture<Map.Entry<String, String>>> futures = new ArrayList<>();

        for (String method : methods) {
            futures.add(CompletableFuture.supplyAsync(() -> queryDify(method), executor));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        Map<String, String> result = new HashMap<>();
        for (CompletableFuture<Map.Entry<String, String>> f : futures) {
            Map.Entry<String, String> entry = f.get();
            result.put(entry.getKey(), entry.getValue());
        }
        return CommonResult.success(mergeModels(result));
    }

    private Map.Entry<String, String> queryDify(String method) {
        String url = difyApiBase + "/chat-messages";

        // 构造请求体
        Map<String, Object> payload = new HashMap<>();
        payload.put("inputs", new HashMap<String, Object>());
        payload.put("query", method);
        payload.put("response_mode", "blocking");
        payload.put("conversation_id", null);
        payload.put("user", "admin");

        try {
            WebClient client = WebClient.builder()
                    .baseUrl(difyApiBase)
                    .defaultHeader("Authorization", "Bearer " + apiKey)
                    .defaultHeader("Content-Type", "application/json")
                    .build();

            JsonNode response = client.post()
                    .uri("/chat-messages")
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            String answer = (response != null && response.has("answer"))
                    ? response.get("answer").asText()
                    : "无返回结果";

            return new AbstractMap.SimpleEntry<>(method, answer);
        } catch (Exception e) {
            return new AbstractMap.SimpleEntry<>(method, "请求失败: " + e.getMessage());
        }
    }

    public Map<String, Object> mergeModels(Map<String, String> originalData) {
        ObjectMapper objectMapper = new ObjectMapper();
        List<Map<String, Object>> allModels = new ArrayList<>();
        Set<String> uniqueModelJsons = new HashSet<>();

        for (Map.Entry<String, String> entry : originalData.entrySet()) {
            String method = entry.getKey();
            String jsonStr = entry.getValue();

            try {
                JsonNode root = objectMapper.readTree(jsonStr);
                JsonNode modelsNode = root.path("models");
                if (modelsNode.isArray()) {
                    for (JsonNode modelNode : modelsNode) {
                        // 为了比较是否重复，去掉 method 字段后序列化成 JSON 字符串作为唯一 key
                        Map<String, Object> modelMap = objectMapper.convertValue(modelNode, Map.class);
                        String modelJsonKey = objectMapper.writeValueAsString(modelMap);

                        if (uniqueModelJsons.add(modelJsonKey)) {
                            // 只有没出现过的模型才加上 method 字段后存入列表
                            modelMap.put("method", method);
                            allModels.add(modelMap);
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("解析失败: " + method + " -> " + e.getMessage());
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("models", allModels);
        return result;
    }


}
