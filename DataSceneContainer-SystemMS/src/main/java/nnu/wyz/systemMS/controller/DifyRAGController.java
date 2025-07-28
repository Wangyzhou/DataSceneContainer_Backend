package nnu.wyz.systemMS.controller;

import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.model.dto.RagMethods;
import nnu.wyz.systemMS.service.RagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/Rag")
public class DifyRAGController {

    @Autowired
    private RagService ragService;

    @PostMapping("/multi_rag")
    public CommonResult<Map<String, Object>> multiRag(@RequestBody RagMethods req) throws ExecutionException, InterruptedException {
        return ragService.queryMultipleMethods(req.getMethods());
    }
}
