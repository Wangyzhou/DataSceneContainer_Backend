package nnu.wyz.systemMS.controller.DscCode;

import nnu.wyz.domain.CommonResult;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import org.springframework.http.HttpHeaders;

import org.springframework.web.reactive.function.client.WebClient;


@RestController
@RequestMapping("/execute-code")
public class KernelGatewayController {
    private final WebClient webClient = WebClient.builder()
            .baseUrl("http://119.45.181.127:8888")
            .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64)") // 模拟浏览器请求
            .build();

    // 创建 kernel
    @PostMapping("/kernels")
    public CommonResult<String> createKernel() {
        String kernelResponse = webClient.post()
                .uri("/api/kernels")
                .retrieve()
                .bodyToMono(String.class)
                .block();   // 阻塞拿到实际结果
        return CommonResult.success(kernelResponse);
    }

    // 删除 kernel
    @DeleteMapping("/stop/{kernelId}")
    public CommonResult<Mono<String>> deleteKernel(@PathVariable String kernelId) {
        String kernelResponse = webClient.post()
                .uri("/api/kernels/{kernelId}/interrupt")
                .retrieve()
                .bodyToMono(String.class)
                .block();   // 阻塞拿到实际结果
        return CommonResult.success(kernelResponse);
    }



    // 获取 kernel 列表
    @GetMapping("/kernels")
    public CommonResult<Mono<String>> listKernels() {
        return CommonResult.success(webClient.get()
                .uri("/api/kernels")
                .retrieve()
                .bodyToMono(String.class));
    }
}
